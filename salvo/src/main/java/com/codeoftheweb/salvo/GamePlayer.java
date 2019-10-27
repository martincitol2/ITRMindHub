package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    public void setId(long id) {
        this.id = id;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void setShips(Set<Ship> ships) {
        this.ships = ships;
    }

    @OneToMany(mappedBy = "gamePlayer",fetch = FetchType.EAGER)
    private Set<Ship> ships;


    public GamePlayer() {}

    public GamePlayer(Game game,Player player,Date joinDate) {
        this.player = player;
        this.game = game;
        this.joinDate = joinDate;
    }

    public long getId() {
        return id;
    }

    Map<String,Object> makeGamePlayerDTO(){
        Map<String,Object> dto = new LinkedHashMap<String,Object>();
        dto.put("id",this.getId());
        dto.put("player",this.player.makePlayerDTO());
        return dto;
     }


    public void addShip(Ship ship) {
        ship.setGamePlayer(this);
        this.ships.add(ship);
    }
}
