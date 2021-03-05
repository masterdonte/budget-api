package com.donte.budget.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.donte.budget.api.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

}