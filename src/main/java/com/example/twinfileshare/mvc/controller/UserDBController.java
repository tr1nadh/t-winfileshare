package com.example.twinfileshare.mvc.controller;

import com.example.twinfileshare.entity.GoogleUserCRED;
import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

@Controller
@RequestMapping("/dev/user")
public class UserDBController {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @GetMapping("/db/fetch")
    public String fetch(HttpServletResponse res) throws IOException {
        res.getWriter().println(googleUserCREDRepository.findAll());

        return "/web/panel/user-panel.html";
    }

    @GetMapping("/db/clear")
    public String clear() {
        googleUserCREDRepository.deleteAll();

        return "/web/panel/user-panel.html";
    }

    @PostMapping("/db/create")
    public String create() {
        System.out.println("Created");

        return "/web/panel/user-panel.html";
    }

    @GetMapping("/db/create-data")
    public String createData() {
        for (var i = 0; i < 10; i++) {
            var googleUserCRED = new GoogleUserCRED();
            googleUserCRED.setId("user||id__" + UUID.randomUUID() + "---" + i);
            googleUserCRED.setEmail("genuine-" + i + "-email@gmail.com");
            googleUserCRED.setExpires(new Random().nextLong());
            googleUserCRED.setAccessToken("Y98k.access-token.-" + UUID.randomUUID() + "----" + i);
            googleUserCRED.setRefreshToken("/4/refresh-token-" + UUID.randomUUID() + "----" + i);
            googleUserCREDRepository.save(googleUserCRED);
        }

        return "/web/panel/user-panel.html";
    }
}
