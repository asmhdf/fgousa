package com.example.pleasework.entity;



import jakarta.persistence.*;

@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postid;

    private String nom;

    private Integer templateid;

    // Getters & Setters
    public Integer getPostid() {
        return postid;
    }

    public void setPostid(Integer postid) {
        this.postid = postid;
    }

    public String getNom() {
        return nom;
    }


    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getTemplateid() {
        return templateid;
    }

    public void setTemplateid(int templateid) {
        this.templateid = templateid;
    }
}

