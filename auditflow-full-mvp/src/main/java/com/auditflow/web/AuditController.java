package com.auditflow.web;

import com.auditflow.repo.AuditRepository;
import com.auditflow.scanner.WebsiteScanner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuditController
{
    private final WebsiteScanner scanner;
    private final AuditRepository repo;

    public AuditController(WebsiteScanner s, AuditRepository r)
    {
        scanner = s;
        repo = r;
    }

    @GetMapping("/")
    String home(Model m)
    {
        m.addAttribute("audits", repo.findAll());
        return "index";
    }

    @PostMapping("/scan")
    String scan(@RequestParam String url)
    {
        return "redirect:/audit/" + repo.save(scanner.scan(url)).id;
    }

    @GetMapping("/audit/{id}")
    String audit(@PathVariable Long id, Model m)
    {
        m.addAttribute("audit", repo.findById(id).orElseThrow());
        return "audit";
    }
}