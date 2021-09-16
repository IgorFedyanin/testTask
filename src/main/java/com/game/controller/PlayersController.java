package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@RestController
public class PlayersController {

    private PlayerServiceImpl playerService;

    @Autowired
    public PlayersController(PlayerServiceImpl playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/rest/players/count")
    public Integer allPlayersCount(){
        return playerService.getAll().size();
    }

    @GetMapping("/rest/players")
    public List<Player> allPlayers(@RequestParam (value = "name", required = false) String name,
                                   @RequestParam (value = "title", required = false) String title,
                                   @RequestParam (value = "race", required = false) Race race,
                                   @RequestParam (value = "profession", required = false) Profession profession,
                                   @RequestParam (value = "after", required = false) Long after,
                                   @RequestParam (value = "before", required = false) Long before,
                                   @RequestParam (value = "banned", required = false) Boolean banned,
                                   @RequestParam (value = "minExperience", required = false) Integer minExperience,
                                   @RequestParam (value = "maxExperience", required = false) Integer maxExperience,
                                   @RequestParam (value = "minLevel", required = false) Integer minLevel,
                                   @RequestParam (value = "maxLevel", required = false) Integer maxLevel,
                                   @RequestParam (value = "order",  required = false) PlayerOrder order,
                                   @RequestParam (value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                   @RequestParam (value = "pageSize", defaultValue = "3", required = false) Integer pageSize){
        List<Player> listPlayers = playerService.getAll();
        if (!Objects.isNull(name)){
            listPlayers = listPlayers.stream().filter(s->s.getName().matches("(.*)" + name + "(.*)")).collect(Collectors.toList());
        }
        if (!Objects.isNull(title)){
            listPlayers = listPlayers.stream().filter(s->s.getTitle().matches("(.*)" + name + "(.*)")).collect(Collectors.toList());
        }
        if (!Objects.isNull(race)){
            listPlayers = listPlayers.stream().filter(s->s.getRace().equals(race)).collect(Collectors.toList());
        }
        if (!Objects.isNull(profession)){
            listPlayers = listPlayers.stream().filter(s->s.getProfession().equals(profession)).collect(Collectors.toList());
        }
        if (!Objects.isNull(title)){
            listPlayers = listPlayers.stream().filter(s->s.getName().equals(title)).collect(Collectors.toList());
        }
        if (!Objects.isNull(before) && !Objects.isNull(after) && (after > before)){
            listPlayers = listPlayers.stream().filter(s->(s.getBirthday().getTime() > after) &&
                                                         (s.getBirthday().getTime() < before) ).collect(Collectors.toList());
        }
        else if (!Objects.isNull(after)){
            listPlayers = listPlayers.stream().filter(s->(s.getBirthday().getTime() > after)).collect(Collectors.toList());
        }
        else if (!Objects.isNull(before)){
            listPlayers = listPlayers.stream().filter(s->(s.getBirthday().getTime() < before)).collect(Collectors.toList());
        }


        if (!Objects.isNull(banned)){
            listPlayers = listPlayers.stream().filter(s->s.isBanned().equals(banned)).collect(Collectors.toList());
        }


        if (!Objects.isNull(minExperience) && !Objects.isNull(maxExperience) && (maxExperience > minExperience)){
            listPlayers = listPlayers.stream().filter(s->(s.getExperience() >= minExperience) &&
                    (s.getExperience() <= maxExperience)).collect(Collectors.toList());
        }
        else if (!Objects.isNull(minExperience)) {
            listPlayers = listPlayers.stream().filter(s->(s.getExperience() >= minExperience)).collect(Collectors.toList());
        }
        else if (!Objects.isNull(maxExperience)){
            listPlayers = listPlayers.stream().filter(s->(s.getExperience() <= maxExperience)).collect(Collectors.toList());
        }

        if (!Objects.isNull(minLevel) && !Objects.isNull(maxLevel) && (maxLevel > minLevel)){
            listPlayers = listPlayers.stream().filter(s->(s.getLevel() >= minLevel) &&
                    (s.getLevel() <= maxLevel)).collect(Collectors.toList());
        }
        else if (!Objects.isNull(minLevel)) {
            listPlayers = listPlayers.stream().filter(s->(s.getLevel() >= minLevel)).collect(Collectors.toList());
        }
        else if (!Objects.isNull(maxLevel)){
            listPlayers = listPlayers.stream().filter(s->(s.getLevel() <= maxLevel)).collect(Collectors.toList());
        }

        if (Objects.nonNull(order)){
            switch (order){
                case NAME: listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getName)).collect(Collectors.toList()); break;
                case EXPERIENCE: listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getExperience)).collect(Collectors.toList()); break;
                case BIRTHDAY: listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getBirthday)).collect(Collectors.toList()); break;
                case LEVEL: listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getLevel)).collect(Collectors.toList()); break;
                case ID:
                default: listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getId)).collect(Collectors.toList()); break;
            }
        }

        if (Objects.nonNull(pageNumber) && Objects.nonNull(pageSize)){
            if (pageNumber >= 1){
                listPlayers = listPlayers.stream().skip((long) (pageNumber - 1) * pageSize).limit(pageSize).collect(Collectors.toList());
            }
            else {
                listPlayers = listPlayers.stream().limit(pageSize).collect(Collectors.toList());
            }

        }


        return listPlayers;

    }
}
