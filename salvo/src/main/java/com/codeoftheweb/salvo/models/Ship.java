package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name = "location")
    private List<String> locations;

    private String type;

    public List<String> getLocations() {
        return locations;
    }

    public Ship() {
    }

    public Ship(String type, List<String> locations, GamePlayer gamePlayer) {
        this.type = type;
        this.locations = locations;
        this.gamePlayer = gamePlayer;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> makeShipDTO() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", this.getType());
        dto.put("locations", this.getLocations());

        return dto;
    }


}