package com.donte.budget.api.repository.lancamento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.donte.budget.api.dto.LancamentoEstatisticaCategoria;
import com.donte.budget.api.dto.LancamentoEstatisticaDia;
import com.donte.budget.api.dto.LancamentoEstatisticaPessoa;
import com.donte.budget.api.model.Lancamento;
import com.donte.budget.api.repository.filter.LancamentoFilter;
import com.donte.budget.api.repository.projection.ResumoLancamento;

public interface LancamentoRepositoryQuery {

	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable);
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable);
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia);
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia);
	public List<LancamentoEstatisticaPessoa> porPessoa(LocalDate inicio, LocalDate fim);
	
}