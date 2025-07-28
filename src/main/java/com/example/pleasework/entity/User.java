package com.example.pleasework.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "user")
public class User {

    @Id
    private Integer userid;

    private String role;

    private int matricule;

    // Getters & Setters
    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getMatricule() {
        return matricule;
    }

    public void setMatricule(int matricule) {
        this.matricule = matricule;
    }
}
