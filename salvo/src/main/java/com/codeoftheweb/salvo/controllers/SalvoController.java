package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.GameRepository;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import com.codeoftheweb.salvo.repositories.SalvoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api")
public class SalvoController {

   @Autowired
   GameRepository gameRepository;

   @Autowired
   GamePlayerRepository gamePlayerRepository;

   @Autowired
   PlayerRepository playerRepository;

   @Autowired
   SalvoRepository salvoRepository;


   @RequestMapping("/game_view/{nn}")
   public ResponseEntity<Map<String, Object>> GetGameView(@PathVariable Long nn, Authentication authentication) {
      Player player = getAuthentication(authentication);
      GamePlayer gamePlayer = gamePlayerRepository.getOne(nn);
      if (gamePlayer.getPlayer().equals(player)) {
         return new ResponseEntity<>(gameViewByID(nn), HttpStatus.OK);
      } else {
         return new ResponseEntity<>(makeMap("error", "No esta autorizado."), HttpStatus.UNAUTHORIZED);
      }
   }

   public Map<String, Object> gameViewByID(@PathVariable long nn) {
      GamePlayer gamePlayer = gamePlayerRepository.findById(nn).get();
      Map<String, Object> dto = new LinkedHashMap<>();
      dto.put("id", gamePlayer.getGame().getId());
      dto.put("created", gamePlayer.getGame().getCreationDate());
      dto.put("gameState", "PLACESHIPS");
      dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.makeGamePlayerDTO()).collect(Collectors.toList()));
      dto.put("ships", gamePlayer.getShips().stream().map(ship -> ship.makeShipDTO()).collect(Collectors.toList()));
      dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.getSalvos()).flatMap(x -> x.stream()).map(salvo -> salvo.makeSalvoDTO()).collect(Collectors.toList()));
      dto.put("hits",hits(nn));
      return dto;

   }
   public Map<String,Object> hits(Long gpid){
      Map<String,Object> dto = new LinkedHashMap<>();
      GamePlayer self = getSelf(gpid);
      GamePlayer opponent = getOponente(self);

      if(opponent != null) {

         dto.put("self", gamePlayerRepository.getOne(gpid)
                 .getSalvos()
                 .stream()
                 .map(salvo -> salvo.getHitsDTO(self,
                         opponent))
                 .collect(Collectors.toList()));
         dto.put("opponent", gamePlayerRepository
                 .getOne(gpid)
                 .getSalvos()
                 .stream()
                 .map(salvo -> salvo.getHitsDTO(opponent, self)).collect(Collectors.toList()));

      }
      else {
         dto.put("self",new ArrayList<>());
         dto.put("opponent",new ArrayList<>());
      }
      return dto;
   }

   public GamePlayer getSelf(Long id){

      return gamePlayerRepository.findById(id).get();
   }

   public GamePlayer getOponente(GamePlayer gpSelf) {
      return gpSelf.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != gpSelf.getId()).findAny().orElse(null);
   }


   @RequestMapping(path = "/games", method = RequestMethod.POST)
   public ResponseEntity<Map<String, Object>> newGame(Authentication authentication) {

      Player playerAuthenticated = getAuthentication(authentication);

      if (playerAuthenticated == null)
         return new ResponseEntity<>(HttpStatus.FORBIDDEN);

      Date date = Date.from(java.time.ZonedDateTime.now().toInstant());
      Game newGame = new Game(date);

      GamePlayer newGamePlayer = new GamePlayer(newGame, playerAuthenticated, new Date());
      playerAuthenticated.addGamePlayer(newGamePlayer);

      gameRepository.save(newGame);
      gamePlayerRepository.save(newGamePlayer);

      return new ResponseEntity<>(makeMap("gpid",newGamePlayer.getId()),HttpStatus.CREATED);

   }

   @RequestMapping(path = "game/{id}/players", method = RequestMethod.POST)
   public ResponseEntity joinGame(Authentication authentication, @PathVariable long id) {
      Player authenticatedPlayer = getAuthentication(authentication);

      Game gameActual = gameRepository.findById(id).orElse(null);

      if (authenticatedPlayer == null)
         return new ResponseEntity<>(makeMap("error", "no such player"), HttpStatus.UNAUTHORIZED);

      if (gameActual == null)
         return new ResponseEntity<>(makeMap("error", "no such game"), HttpStatus.CONFLICT);

      if (gameActual.getGamePlayers().size() > 1)
         return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);

      GamePlayer newGamePlayer = new GamePlayer(gameActual, authenticatedPlayer, new Date());
      gameActual.addGamePlayer(newGamePlayer);
      gamePlayerRepository.save(newGamePlayer);
      return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
   }

   @RequestMapping(path = "/games/players/{nn}/salvoes",method = RequestMethod.POST)
   public ResponseEntity<Map> addSalvo(@PathVariable Long nn, @RequestBody Salvo salvo, Authentication authentication){
      if(isGuest(authentication)){
         return new ResponseEntity<>(makeMap("error","Es Guest"), HttpStatus.UNAUTHORIZED);
      }
       GamePlayer gamePlayer = gamePlayerRepository.findById(nn).orElse(null);
      Player player = playerRepository.findPlayerByUserName(authentication.getName());
      if(gamePlayer == null) {
         return new ResponseEntity<>(makeMap("error","El GAMEPLAYER no existe"), HttpStatus.UNAUTHORIZED);
      }else if(gamePlayer.getPlayer().getId() != player.getId() ) {

         return new ResponseEntity<>(makeMap("error","No coinciden los Players"), HttpStatus.UNAUTHORIZED);
      }else{
         salvo.setTurn(gamePlayer.getSalvos().size() + 1);
         gamePlayer.addSalvo(salvo);
         salvo.setGamePlayer(gamePlayer);
         salvoRepository.save(salvo);

         return new ResponseEntity<>(makeMap("OK", "Los Disparos Se Han Lanzado!"), HttpStatus.CREATED);
      }
   }

   private boolean isGuest(Authentication authentication){
      return authentication == null || authentication instanceof AnonymousAuthenticationToken;
   }

   private Player getAuthentication(Authentication authentication) {
      if (authentication == null || authentication instanceof AnonymousAuthenticationToken)
         return null;
      else
         return (playerRepository.findPlayerByUserName(authentication.getName()));
   }

   private Map<String,Object> makeMap(String key,Object value){
      Map<String,Object> map = new LinkedHashMap<>();
      map.put(key,value);
      return map;
   }

}


