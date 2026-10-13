package com.auditflow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Finding
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String category, severity, title, details, recommendation;

    public Finding(){
    }

    public Finding(String c, String s, String t, String d, String r) {
        category = c;
        severity = s;
        title = t;
        details = d;
        recommendation = r;
    }
}