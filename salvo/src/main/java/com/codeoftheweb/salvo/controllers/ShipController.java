package com.codeoftheweb.salvo.controllers;

import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.models.Ship;
import com.codeoftheweb.salvo.repositories.GamePlayerRepository;
import com.codeoftheweb.salvo.repositories.PlayerRepository;
import com.codeoftheweb.salvo.repositories.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShipController {
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    GamePlayerRepository gamePlayerRepository;
    @Autowired
    ShipRepository shipRepository;

    @RequestMapping(path = "/games/players/{nn}/ships",method = RequestMethod.POST)
    public ResponseEntity<Map> addShip(@PathVariable Long nn, @RequestBody List<Ship> ships, Authentication authentication){
        if(isGuest(authentication)){
            return new ResponseEntity<>(makeMap("error","Es Guest"), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findById(nn).orElse(null);
        Player player = playerRepository.findPlayerByUserName(authentication.getName());
        if(gamePlayer == null){
            return new ResponseEntity<>(makeMap("error","El GamePlayer no Existe"), HttpStatus.UNAUTHORIZED);

        }if(player == null){
            return new ResponseEntity<>(makeMap("error","El Player no Existe"), HttpStatus.UNAUTHORIZED);
        }
        if(gamePlayer.getPlayer().getId() != player.getId()){
            return new ResponseEntity<>(makeMap("error","Los player no coinciden"), HttpStatus.FORBIDDEN);
        }
        if(!gamePlayer.getShips().isEmpty()){
            return new ResponseEntity<>(makeMap("error","Los Ship Ya estan Creados"), HttpStatus.FORBIDDEN);
        }
        ships.forEach(ship ->{ship.setGamePlayer(gamePlayer);
        shipRepository.save(ship);});
        return new ResponseEntity<>(makeMap("OK","Los Ship Han Sido Creados"), HttpStatus.CREATED);
    }
    private Map<String,Object> makeMap(String key,Object value){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put(key,value);
        return map;
    }
    private boolean isGuest(Authentication authentication){
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
}
