package com.example.twinfileshare.mvc.controller;

import com.example.twinfileshare.service.GoogleAuthorizationService;
import jakarta.servlet.http.HttpServletResponse;
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
    public void callback(String code, String scope, HttpServletResponse res) throws IOException, GeneralSecurityException {
        googleAuthorizationService.saveToken(code, scope);

        res.getWriter().println("Account successfully connected!! you can close the tab now.");
    }
}
