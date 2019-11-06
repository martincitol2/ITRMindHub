package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
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

   @Autowired
   ScoreRepository scoreRepository;


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
      dto.put("gameState", gameState(getSelf(nn)));
      dto.put("gamePlayers", gamePlayer.getGame().getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.makeGamePlayerDTO()).collect(Collectors.toList()));
      dto.put("ships", gamePlayer.getShips().stream().map(ship -> ship.makeShipDTO()).collect(Collectors.toList()));
      dto.put("salvoes", gamePlayer.getGame().getGamePlayers().stream().map(gamePlayer1 -> gamePlayer1.getSalvos()).flatMap(x -> x.stream()).map(salvo -> salvo.makeSalvoDTO()).collect(Collectors.toList()));
      dto.put("hits",hits(nn));
      return dto;

   }
   public Map<String, Object> hits(long gpid) {
      Map<String, Object> dto = new LinkedHashMap<>();
      GamePlayer self = getSelf(gpid);
      GamePlayer opponent = getOpponent(self);

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

   public GamePlayer getOpponent(GamePlayer self) {
      return self.getGame().getGamePlayers().stream().filter(gp -> gp.getId() != self.getId()).findAny().orElse(null);
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

   @RequestMapping(path = "/games/players/{id}/salvoes", method = RequestMethod.POST)
   public ResponseEntity<Map> addSalvoes(Authentication authentication, @RequestBody Salvo salvo, @PathVariable Long id) {

      if (isGuest(authentication)) {
         return new ResponseEntity<>(makeMap("error", "Es Guest"), HttpStatus.UNAUTHORIZED);
      }
      Player player = playerRepository.findPlayerByUserName(authentication.getName());
      GamePlayer gamePlayer = gamePlayerRepository.findById(id).orElse(null);
      if (gamePlayer == null) {
         return new ResponseEntity<>(makeMap("error", "El GamePlayer no exist"), HttpStatus.UNAUTHORIZED);
      }
      if (player == null) {
         return new ResponseEntity<>(makeMap("error", "El GamePlayer no exist"), HttpStatus.UNAUTHORIZED);
      }

      if (gamePlayer.getPlayer().getId() != player.getId()) {
         return new ResponseEntity<>(makeMap("error", "Los player no coinciden"), HttpStatus.FORBIDDEN);
      }


      salvo.setTurn(gamePlayer.getSalvos().size() + 1);
      gamePlayer.addSalvo(salvo);
      salvo.setGamePlayer(gamePlayer);
      salvoRepository.save(salvo);
      return new ResponseEntity<>(makeMap("OK", "¡Salvos posicionados! Prepárate para ganar."), HttpStatus.CREATED);

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

   public String gameState (GamePlayer self) {

      GamePlayer oponent = getOpponent(self);

      if (self.getShips().size() == 0) {
         return "PLACESHIPS";
      }
      if (getOpponent(self) == null) {
         return "WAITINGFOROPP";
      }
      long turn = getTurn(self, oponent);

      if (self.getSalvos().size() == oponent.getSalvos().size()) {

         Date date = new Date();
         date = Date.from(date.toInstant());

         if (allSunk(self, oponent) && allSunk(oponent,self)) {

            Score scoreSelf = new Score(self.getPlayer(),self.getGame(),date,0.5);
            if (!existsScore(scoreSelf, self.getGame())) {scoreRepository.save(scoreSelf);}
            Score scoreOpponent = new Score(oponent.getPlayer(),oponent.getGame(),date,0.5);
            if (!existsScore(scoreOpponent, oponent.getGame())) {scoreRepository.save(scoreOpponent);}

            return "TIE";

         }
         if (allSunk(self, oponent) && (getSalvos(oponent).size()!= 0)) {
            Score scoreSelf = new Score( self.getPlayer(),self.getGame(),date, 1);
            if (!existsScore(scoreSelf, self.getGame()))  {scoreRepository.save(scoreSelf);}


            return "WON";

         }
         if (allSunk(oponent,self)) {
            Score newScore = new Score(self.getPlayer(), self.getGame(),date, 0);
            if (!existsScore(newScore, self.getGame())) { scoreRepository.save(newScore);}

            return "LOST";

         }
         if (self.getSalvos().size() != turn || oponent.getSalvos().size() != turn){
            return "PLAY";}
      }

      return "WAIT";
   }

   private Boolean existsScore(Score score, Game game) {

      Set<Score> scores = game.getScores();
      for (Score s : scores) {
         if (score.getPlayer().getUserName().equals(s.getPlayer().getUserName()))
            return true;
      }
      return false;
   }

   private long getTurn(GamePlayer self, GamePlayer opponent) {

      int selfSalvoes = self.getSalvos().size();
      int opponentSalvoes = opponent.getSalvos().size();

      int totalSalvoes = selfSalvoes + opponentSalvoes;
      if (totalSalvoes % 2 == 0) return totalSalvoes / 2 + 1;

      return (int) (totalSalvoes / 2.0 + 0.5);
   }

   public List<String> getShips(GamePlayer gamePlayer){
      List<String> posiciones = gamePlayer.getShips().stream().map(ship -> ship.getLocations()).flatMap(locati -> locati.stream()).collect(Collectors.toList());
      return posiciones;
   }
   public List<String> getSalvos(GamePlayer gamePlayer){
      List<String> posiciones = gamePlayer.getSalvos().stream().map(salvo -> salvo.getSalvoLocations()).flatMap(locati -> locati.stream()).collect(Collectors.toList());
      return posiciones;
   }

   public boolean allSunk(GamePlayer self,GamePlayer opponent){
      Boolean allSunk = false;
      List<String> lista = new ArrayList<>();
      for(String location : getSalvos(self))
      {
         if(getShips(opponent).contains(location)){
            lista.add(location);
         }
      }
      if(lista.size() == getShips(opponent).size()){
         allSunk = true;
      }
      return allSunk;
   }

}


