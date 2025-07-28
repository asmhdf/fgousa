package com.example.pleasework.repository;

import com.example.pleasework.entity.Setup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetupRepository extends JpaRepository<Setup, Integer> {
    List<Setup> findByIdopNotNull();

}

