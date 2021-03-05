package com.donte.budget.api.repository;

import javax.persistence.PostLoad;

import org.springframework.util.StringUtils;

import com.donte.budget.api.BudgetApiApplication;
import com.donte.budget.api.model.Lancamento;
import com.donte.budget.api.storage.S3;

public class LancamentoAnexoListener {
	
	@PostLoad
	public void postLoad(Lancamento lancamento) {
		if (StringUtils.hasText(lancamento.getAnexo())) {
			S3 s3 = BudgetApiApplication.getBean(S3.class);
			lancamento.setUrlAnexo(s3.configurarUrl(lancamento.getAnexo()));
		}
	}

}