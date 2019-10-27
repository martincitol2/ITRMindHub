package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    private String userName;

    public String getUserName() {
        return userName;
    }

    public Player() {
    }

    public long getId() {
        return id;
    }


    public Player(String userName) {
        this.userName = userName;
    }

    public Map<String,Object> makePlayerDTO(){
        Map<String,Object> dto = new LinkedHashMap<String,Object>();
        dto.put("id",this.getId());
        dto.put("email",this.getUserName());
        return dto;

    }

}
