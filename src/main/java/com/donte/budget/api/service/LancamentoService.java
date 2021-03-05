package com.donte.budget.api.service;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.donte.budget.api.dto.LancamentoEstatisticaPessoa;
import com.donte.budget.api.mail.Mailer;
import com.donte.budget.api.model.Lancamento;
import com.donte.budget.api.model.Pessoa;
import com.donte.budget.api.model.Usuario;
import com.donte.budget.api.repository.LancamentoRepository;
import com.donte.budget.api.repository.PessoaRepository;
import com.donte.budget.api.repository.UsuarioRepository;
import com.donte.budget.api.service.exception.PessoaInexistenteOuInativaException;
import com.donte.budget.api.storage.S3;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class LancamentoService {
	
	private static final String ROLE_PERMISSAO = "ROLE_PESQUISAR_LANCAMENTO";
	
	private static final Logger logger = LoggerFactory.getLogger(LancamentoService.class);

	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private Mailer mailer;
	
	@Autowired
	private S3 s3;
	
	//Chama ao iniciar a aplicacao e roda apos passar o intervalo definido 
	//e so comeca a contar de novo quando o metodo terminar de rodar
	//@Scheduled(fixedDelay = 1000 * 60 * 30)
	@Scheduled(cron = "0 0 12 23 * *")
	public void avisarSobreLancamentosVencidos() {
		if (logger.isDebugEnabled()) {
			logger.debug("Preparando envio de e-mails de aviso de lançamentos vencidos.");
		}
		
		List<Lancamento> vencidos = lancamentoRepository.findByDataVencimentoLessThanEqualAndDataPagamentoIsNull(LocalDate.now().minusDays(20));
		
		if (vencidos.isEmpty()) {
			logger.info("Sem lançamentos vencidos para aviso.");
			return;
		}
		
		logger.info("Exitem {} lançamentos vencidos.", vencidos.size());
		List<Usuario> destinatarios = usuarioRepository.findByPermissoesDescricao(ROLE_PERMISSAO);
		
		if (destinatarios.isEmpty()) {
			logger.warn("Existem lançamentos vencidos, mas o sistema não encontrou destinatários.");
			return;
		}
		
		mailer.avisarSobreLancamentosVencidos(vencidos, destinatarios);
		logger.info("Envio de e-mail de aviso concluído."); 
	}

	public byte[] relatorioPorPessoa(LocalDate inicio, LocalDate fim) throws JRException{
		List<LancamentoEstatisticaPessoa> dados = lancamentoRepository.porPessoa(inicio, fim);
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("DT_INICIO", Date.valueOf(inicio));
		parametros.put("DT_FIM", Date.valueOf(fim));
		parametros.put("REPORT_LOCALE", new Locale("pt", "BR"));
		
		InputStream inputStream = this.getClass().getResourceAsStream("/relatorios/lancamentos-por-pessoa.jasper");
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parametros,new JRBeanCollectionDataSource(dados));
		return JasperExportManager.exportReportToPdf(jasperPrint);
	}
	
	public Lancamento salvar(@Valid Lancamento lancamento) {
		Optional<Pessoa> optPessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo());
		if(!optPessoa.isPresent() || optPessoa.get().isInativo()) throw new PessoaInexistenteOuInativaException();
		
		if (StringUtils.hasText(lancamento.getAnexo())) {
			s3.salvar(lancamento.getAnexo());
		}
		
		return lancamentoRepository.save(lancamento);
	}


	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		Lancamento lancamentoSalvo = lancamentoRepository.findById(codigo).orElseThrow(() -> new IllegalArgumentException());
		if(!lancamentoSalvo.getPessoa().equals(lancamento.getPessoa())){
			Optional<Pessoa> optPessoa = pessoaRepository.findById(lancamentoSalvo.getPessoa().getCodigo());
			if(!optPessoa.isPresent() || optPessoa.get().isInativo()) throw new PessoaInexistenteOuInativaException();
		}
		
		if (StringUtils.isEmpty(lancamento.getAnexo()) && StringUtils.hasText(lancamentoSalvo.getAnexo())) {
			s3.remover(lancamentoSalvo.getAnexo());
		} else if (StringUtils.hasLength(lancamento.getAnexo()) && !lancamento.getAnexo().equals(lancamentoSalvo.getAnexo())) {
			s3.substituir(lancamentoSalvo.getAnexo(), lancamento.getAnexo());
		}
		
		BeanUtils.copyProperties(lancamento, lancamentoSalvo, "codigo");
		return lancamentoRepository.save(lancamentoSalvo);
	}

}