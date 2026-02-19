package com.iot.platform.controller;

import com.iot.platform.model.Site;
import com.iot.platform.repository.SiteRepository;
import com.iot.platform.security.TenantContext;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteRepository siteRepository;

    public SiteController(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @GetMapping
    public List<Site> listSites(Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return siteRepository.findByTenantId(ctx.getTenantId());
    }

    @PostMapping
    public Site createSite(@RequestBody Map<String, String> body, Authentication auth) {
        TenantContext ctx = (TenantContext) auth.getDetails();
        return siteRepository.save(new Site(
                ctx.getTenantId(), UUID.randomUUID(),
                body.get("name"), body.getOrDefault("location", "")
        ));
    }
}
