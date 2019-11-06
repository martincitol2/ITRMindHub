package com.codeoftheweb.salvo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class RedirectController {
    @RequestMapping(value = "/")
    public String welcome() {
        return "redirect:/web/games.html";
    }
}