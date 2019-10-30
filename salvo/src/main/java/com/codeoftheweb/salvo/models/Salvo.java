package com.codeoftheweb.salvo.models;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native",strategy = "native")
    private long id;

    @ElementCollection
    @Column(name="location")
    List<String> locations;

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
        this.locations = locations;
        this.turn = turn;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public List<String> getLocations() {
        return locations;
    }

    public Map<String,Object> makeSalvoDTO(){
        Map<String,Object> dto = new LinkedHashMap<>();
        dto.put("turn",this.getTurn());
        dto.put("player",this.getGamePlayer().getPlayer().getId());
        dto.put("locations",this.getLocations());

        return dto;
    }
}
