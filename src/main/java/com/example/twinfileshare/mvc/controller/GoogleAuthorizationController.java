package com.example.twinfileshare.mvc.controller;

import com.example.twinfileshare.service.GoogleAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
public class GoogleAuthorizationController {

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;

    @GetMapping("/callback")
    public String callback(String code, String scope, HttpServletRequest req) throws IOException, GeneralSecurityException {
        googleAuthorizationService.checkAndSaveToken(code, scope);

        return "/web/callback.html";
    }
}
