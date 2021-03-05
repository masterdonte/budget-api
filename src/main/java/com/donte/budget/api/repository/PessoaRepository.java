package com.donte.budget.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.donte.budget.api.model.Pessoa;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
	
	public Page<Pessoa> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
	public Page<Pessoa> findByNomeContaining(String nome, Pageable pageable);

}