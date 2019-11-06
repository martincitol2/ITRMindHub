package com.codeoftheweb.salvo.models;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native",strategy = "native")
    private long id;

    @ElementCollection
    @Column(name="salvoLocations")
    List<String> salvoLocations;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    public void setTurn(int turn) {
        this.turn = turn;
    }

    private int turn;

    public int getTurn() {
        return turn;
    }

    public Salvo(){}

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Salvo(int turn, GamePlayer gamePlayer, List<String> locations){
        this.gamePlayer = gamePlayer;
        this.salvoLocations = locations;
        this.turn = turn;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    public Map<String,Object> makeSalvoDTO(){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("turn",this.getTurn());
        dto.put("player",this.getGamePlayer().getPlayer().getId());
        dto.put("locations",this.getSalvoLocations());

        return dto;
    }

    public Map<String, Object> getHitsDTO(GamePlayer self, GamePlayer opponent) {
        Integer carrierPorTurno = 0;
        Integer battleshipPorTurno = 0;
        Integer submarinePorTurno = 0;
        Integer destroyerPorTurno = 0;
        Integer patrolboatPorTurno = 0;
        Integer carrierTotal = 0;
        Integer battleshipTotal = 0;
        Integer submarineTotal = 0;
        Integer destroyerTotal = 0;
        Integer patrolboatTotal = 0;
        List<String> salvoLocations = new ArrayList<>();
        Integer missed = 0;
       Salvo salvoe = opponent.getSalvos().stream().filter(salvo -> salvo.getTurn() == this.getTurn()).findAny().orElse(null);
       if(salvoe != null) {
           missed = salvoe.getSalvoLocations().size();
           for (Ship ship : self.getShips()) {

               for (String shipLocation : ship.getLocations()) {
                   if (salvoe.getSalvoLocations().contains(shipLocation)) {
                       missed--;
                       salvoLocations.add(shipLocation);
                       switch (ship.getType().toLowerCase()) {
                           case "carrier":
                               carrierPorTurno++;
                               break;
                           case "battleship":
                               battleshipPorTurno++;
                               break;
                           case "submarine":
                               submarinePorTurno++;
                               break;
                           case "destroyer":
                               destroyerPorTurno++;
                               break;
                           case "patrolboat":
                               patrolboatPorTurno++;
                               break;
                       }
                   }
               }
           }
       }
        List<String> salvoLocation = opponent
                .getSalvos()
                .stream()
                .map(salvo -> salvo.getSalvoLocations())
                .flatMap(location -> location.stream())
                .collect(Collectors.toList());

        for (Ship ship : self.getShips()) {

            for (String shipLocation : ship.getLocations()) {
                if(salvoLocation.contains(shipLocation)){


                    switch (ship.getType().toLowerCase()) {
                        case "carrier":
                            carrierTotal++;
                            break;
                        case "battleship":
                            battleshipTotal++;
                            break;
                        case "submarine":
                            submarineTotal++;
                            break;
                        case "destroyer":
                            destroyerTotal++;
                            break;
                        case "patrolboat":
                            patrolboatTotal++;
                            break;
                    }
                }
            }
        }


        Map<String, Object> damages = new LinkedHashMap<>();
        Map<String, Object> dtu = new LinkedHashMap<>();
        dtu.put("turn", this.getTurn());
        dtu.put("hitLocations", salvoLocations);
        dtu.put("damages", damages);
        dtu.put("missed", missed);
        damages.put("carrierHits", carrierPorTurno);
        damages.put("battleshipHits", battleshipPorTurno);
        damages.put("submarineHits", submarinePorTurno);
        damages.put("destroyerHits", destroyerPorTurno);
        damages.put("patrolboatHits", patrolboatPorTurno);
        damages.put("carrier", carrierTotal);
        damages.put("battleship", battleshipTotal);
        damages.put("submarine", submarineTotal);
        damages.put("destroyer", destroyerTotal);
        damages.put("patrolboat", patrolboatTotal);
        return dtu;



    }
}
