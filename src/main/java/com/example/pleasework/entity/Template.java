package com.example.pleasework.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "template")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer templateid;

    @Lob
    @Column(name = "file", columnDefinition = "LONGBLOB")
    private byte[] file;
    @Column(name = "filename")
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Template() {
    }

    // Constructeur avec fichier binaire
    public Template(byte[] file) {
        this.file = file;
    }

    // Getters et Setters
    public Integer getId() {
        return templateid;
    }

    public void setId(Integer id) {
        this.templateid = id;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }
}
