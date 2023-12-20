package com.example.twinfileshare.mvc.controller;

import com.example.twinfileshare.service.GoogleAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
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
    public HttpServletResponse callback(HttpServletRequest req, HttpServletResponse res) throws IOException, GeneralSecurityException {
        String code = req.getParameter("code");
        if (code != null) googleAuthorizationService.saveToken(code);

        res.getWriter().println("You are successfully connected, You can get back to the application:");
        return res;
    }
}
