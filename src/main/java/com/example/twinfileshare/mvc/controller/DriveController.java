package com.example.twinfileshare.mvc.controller;

import com.example.twinfileshare.fx.service.DriveService;
import com.google.api.services.drive.Drive;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
public class DriveController {

    @Autowired
    private DriveService driveService;

    @GetMapping("/callback")
    public HttpServletResponse callback(HttpServletRequest req, HttpServletResponse res) throws IOException, GeneralSecurityException {
        String code = req.getParameter("code");
        if (code != null) {
            System.out.println("Authorization code: " + code);
            driveService.showToken(code);
        }

        res.getWriter().println("You are successfully connected, You can get back to the application:");
        return res;
    }
}
