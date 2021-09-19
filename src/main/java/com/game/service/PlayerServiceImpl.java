package com.game.service;

import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService{

    private PlayerRepository playerRepository;

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }


    @Override
    public Player addPlayer(Player player) {
        return playerRepository.saveAndFlush(player);
    }

    @Override
    public void delete(long id) {
        playerRepository.deleteById(id);
    }

    @Override
    public Player getById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public Player editPlayer(Player player) {
        return playerRepository.saveAndFlush(player);
    }

    @Override
    public List<Player> getAll() {
        return playerRepository.findAll();
    }
    @Override
    public Player getByName(String name) {
        return playerRepository.getPlayerByName(name);
    }

}
