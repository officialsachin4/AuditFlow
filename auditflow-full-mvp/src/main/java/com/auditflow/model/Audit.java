package com.auditflow.model;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Audit
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String url;
    public int overallScore, securityScore, accessibilityScore, seoScore, privacyScore, performanceScore, technicalScore, mobileScore;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Finding> findings = new ArrayList<>();

    public Audit(){
    }

    public Audit(String u) {
        url = u;
    }

}