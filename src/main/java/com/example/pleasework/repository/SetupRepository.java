package com.example.pleasework.repository;

import com.example.pleasework.entity.Setup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetupRepository extends JpaRepository<Setup, Integer> {
    List<Setup> findByIdopNotNull();
    List<Setup> findByIdtlNotNull();
    List<Setup> findByPostid(Integer postid);
    List<Setup> findTop2ByPostidAndFileIsNotNullOrderByDateqDesc(Integer postid);
    List<Setup> findByPostidOrderByDateqDesc(Integer postid);
    List<Setup> findByPostidOrderBySetupidDesc(Integer postid);


        // 2 derniers enregistrements (opérateur/TL) avec fichier non nul, pour ce post
        List<Setup> findTop2ByPostidAndFileIsNotNullAndIdqIsNullOrderBySetupidDesc(Integer postid);

        // (optionnel) récupérer les 2 derniers TOUT COURT si tu préfères te baser sur setupid
        // List<Setup> findTop2ByPostidAndFileIsNotNullOrderBySetupidDesc(Integer postid);




}

