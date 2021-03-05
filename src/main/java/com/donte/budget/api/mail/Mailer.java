package com.donte.budget.api.mail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.donte.budget.api.model.Lancamento;
import com.donte.budget.api.model.Usuario;
import com.donte.budget.api.repository.LancamentoRepository;

@Component
public class Mailer {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private TemplateEngine thymeleaf;
		
	@Autowired
	private LancamentoRepository repo;
	
	//@EventListener
	@SuppressWarnings("unused")
	private void teste(ApplicationReadyEvent event) {
		String template = "mail/aviso-lancamentos-vencidos";
		List<Lancamento> lista = repo.findAll();
		Map<String, Object> variaveis = new HashMap<>();
		variaveis.put("lancamentos", lista);
		this.processarEmail("master.donte@hotmail.com", Arrays.asList("donte.master@gmail.com"), "Testers", template, variaveis);
		System.out.println("Terminado o envio de e-mail...");
	}
	
	public void avisarSobreLancamentosVencidos(List<Lancamento> vencidos, List<Usuario> destinatarios) {
		Map<String, Object> variaveis = new HashMap<>();
		variaveis.put("lancamentos", vencidos);
		List<String> emails = destinatarios.stream().map(u -> u.getEmail()).collect(Collectors.toList());
		this.processarEmail("nerosapiens@gmail.com", emails, "Lançamentos vencidos", "mail/aviso-lancamentos-vencidos", variaveis);
	}
	
	private void processarEmail(String remetente, List<String> destinatarios, String assunto, String template, Map<String, Object> variaveis) {
		Context context = new Context(new Locale("pt", "BR"));
		variaveis.entrySet().forEach(e -> context.setVariable(e.getKey(), e.getValue()));
		String mensagem = thymeleaf.process(template, context);
		this.enviarEmail(remetente, destinatarios, assunto, mensagem);
	}
	
	private void enviarEmail(String remetente, List<String> destinatarios, String assunto, String mensagem) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
			//helper.setFrom(remetente);
			helper.setTo(destinatarios.toArray(new String[destinatarios.size()]));
			helper.setSubject(assunto);
			helper.setText(mensagem, true);
			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new RuntimeException("Problemas com o envio de e-mail!", e); 
		}
	}
}