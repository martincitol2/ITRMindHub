package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api")
public class SalvoController {

   @Autowired
   GameRepository gameRepository;

   @RequestMapping(path="/games")
    public List<Map<String,Object>> getGames(){

       return gameRepository.findAll().stream().map(game -> game.makeGameDTO()).collect(Collectors.toList());
   }
}
