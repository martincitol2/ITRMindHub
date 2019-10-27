package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.models.GamePlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api")
public class SalvoController {

   @Autowired
   GameRepository gameRepository;

   @Autowired
   GamePlayerRepository gamePlayerRepository;

   @RequestMapping(path="/game_view/{nn}")
    public Map<String,Object> gameViewByID(@PathVariable long nn){
       GamePlayer gamePlayer = gamePlayerRepository.findById(nn).get();
       Map<String,Object> dto = new LinkedHashMap<>();
       dto.put("id",gamePlayer.getGame().getId());
       dto.put("created",gamePlayer.getGame().getCreationDate());
       dto.put("gamePlayers",gamePlayer.getGame().getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.makeGamePlayerDTO()).collect(Collectors.toList()));
       dto.put("ships",gamePlayer.getShips().stream().map(ship -> ship.makeShipDTO()).collect(Collectors.toList()));
       dto.put("salvoes",gamePlayer.getGame().getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.getSalvos()).flatMap(x -> x.stream()).map(salvo -> salvo.makeSalvoDTO()).collect(Collectors.toList()));

       return dto;

   }
}
