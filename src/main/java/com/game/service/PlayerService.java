package com.game.service;

import com.game.entity.Player;

import java.util.List;

public interface PlayerService {
    Player addPlayer(Player player);
    void delete(long id);
    Player getById(Long id);
    Player editPlayer(Player player);
    List<Player> getAll();

    public Player getByName(String name);
}
