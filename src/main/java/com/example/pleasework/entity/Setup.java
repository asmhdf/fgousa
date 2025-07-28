package com.example.pleasework.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "setup")
public class Setup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer setupid;

    @Lob
    private byte[] file;

    private LocalDateTime dateop;
    private LocalDateTime datetl;
    private LocalDateTime dateq;

    private Integer idop;
    private Integer idtl;
    private Integer idq;

    private Integer postid;

    public Integer getSetupid() {
        return setupid;
    }

    public void setSetupid(Integer setupid) {
        this.setupid = setupid;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public LocalDateTime getDateop() {
        return dateop;
    }

    public void setDateop(LocalDateTime dateop) {
        this.dateop = dateop;
    }

    public LocalDateTime getDatetl() {
        return datetl;
    }

    public void setDatetl(LocalDateTime datetl) {
        this.datetl = datetl;
    }

    public LocalDateTime getDateq() {
        return dateq;
    }

    public void setDateq(LocalDateTime dateq) {
        this.dateq = dateq;
    }

    public Integer getIdop() {
        return idop;
    }

    public void setIdop(Integer idop) {
        this.idop = idop;
    }

    public Integer getIdtl() {
        return idtl;
    }

    public void setIdtl(Integer idtl) {
        this.idtl = idtl;
    }

    public Integer getIdq() {
        return idq;
    }

    public void setIdq(Integer idq) {
        this.idq = idq;
    }

    public Integer getPostid() {
        return postid;
    }

    public void setPostid(Integer postid) {
        this.postid = postid;
    }


}

