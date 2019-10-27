package com.codeoftheweb.salvo.repositories;

import com.codeoftheweb.salvo.models.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamePlayerRepository extends JpaRepository<GamePlayer,Long> {
}
