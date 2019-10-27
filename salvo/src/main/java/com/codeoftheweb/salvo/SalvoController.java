package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api")
public class SalvoController {

   @Autowired
   GameRepository gameRepository;

   @Autowired
   GamePlayerRepository gamePlayerRepository;

   @RequestMapping(path="/games")
    public Map<String,Object> getGames(){

      Map<String,Object> dto = new LinkedHashMap<>();
      dto.put("player","Guest");
      dto.put("games",gameRepository.findAll().stream().map(game -> game.makeGameDTO()).collect(Collectors.toList()));

       return dto;
   }

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
