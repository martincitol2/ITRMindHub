package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.Date;

@SpringBootApplication //SERVIDOR TOMCAT INCORPORADO
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository,GameRepository gameRepository,GamePlayerRepository gamePlayerRepository){
		return(args) -> {

			Player j_bauer = playerRepository.save(new Player("j.bauer@ctu.gov"));
			Player c_obrian = playerRepository.save(new Player("c.obrian@ctu.gov"));
			Player kim_bauer = playerRepository.save(new Player("kim_bauer@gmail.com"));
			Player t_almeida = playerRepository.save(new Player("t.almeida@ctu.gov"));
			Player d_palmer = playerRepository.save(new Player("d.palmer@whitehouse.gov"));
			Game juego1 = gameRepository.save(new Game(Date.from( Instant.parse("2018-02-17T18:20:15.000Z"))));
			Game juego2 = gameRepository.save(new Game(Date.from( Instant.parse("2018-02-17T19:20:15.000Z"))));
			Game juego3 = gameRepository.save(new Game(Date.from( Instant.parse("2018-02-17T20:20:15.000Z"))));
			Game juego4 = gameRepository.save(new Game(Date.from( Instant.parse("2018-02-17T21:20:15.000Z"))));
			Game juego5 = gameRepository.save(new Game(Date.from( Instant.parse("2018-02-17T22:20:15.000Z"))));
			Game juego6 = gameRepository.save(new Game(Date.from( Instant.parse("2018-02-17T23:20:15.000Z"))));
			GamePlayer gamePlayer1 = new GamePlayer(j_bauer,juego1,new Date());
			GamePlayer gamePlayer2 = new GamePlayer(c_obrian,juego1,new Date());
			GamePlayer gamePlayer3 = new GamePlayer(j_bauer,juego2,new Date());
			GamePlayer gamePlayer4 = new GamePlayer(c_obrian,juego2,new Date());
			GamePlayer gamePlayer5 = new GamePlayer(c_obrian,juego3,new Date());
			GamePlayer gamePlayer6 = new GamePlayer(t_almeida,juego3,new Date());
			GamePlayer gamePlayer7 = new GamePlayer(j_bauer,juego4,new Date());
			GamePlayer gamePlayer8 = new GamePlayer(c_obrian,juego4,new Date());
			GamePlayer gamePlayer9 = new GamePlayer(t_almeida,juego5,new Date());
			GamePlayer gamePlayer10 = new GamePlayer(j_bauer,juego5,new Date());
			GamePlayer gamePlayer11 = new GamePlayer(d_palmer,juego6,new Date());
			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);
			gamePlayerRepository.save(gamePlayer6);
			gamePlayerRepository.save(gamePlayer7);
			gamePlayerRepository.save(gamePlayer8);
			gamePlayerRepository.save(gamePlayer9);
			gamePlayerRepository.save(gamePlayer10);
			gamePlayerRepository.save(gamePlayer11);

		};
	}

}
