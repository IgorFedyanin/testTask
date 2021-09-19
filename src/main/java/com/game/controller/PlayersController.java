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
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


@RestController
public class PlayersController {

    private PlayerServiceImpl playerService;
    private Calendar rightNow = Calendar.getInstance();

    @Autowired
    public PlayersController(PlayerServiceImpl playerService) {
        this.playerService = playerService;
    }

    @DeleteMapping("/rest/players/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable("id") long id){
        try {
            if (playerService.getById(id) == null && id > 0) throw new RuntimeException();
        }
        catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        try {
            playerService.delete(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/rest/players/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") long id, @RequestBody Player player){
        Player player1;
        try {
            if ((player1 = playerService.getById(id)) == null && id > 0) throw new RuntimeException();
        }
        catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        int count = 0;
        try {
            for (Field f : player.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.get(player) != null && !f.getName().equals("id") && !f.getName().equals("level") && !f.getName().equals("untilNextLevel")) {
                    if (f.getName().equals("experience")){
                        f.set(player1, f.get(player));
                        player1.setLevel(player1.computeLevel(player1.getExperience()));
                        player1.setUntilNextLevel(player1.computeUntilNextLevel(player1.getLevel(), player1.getExperience()));
                    }
                    else {
                        f.set(player1, f.get(player));
                        count++;
                    }
                }
            }
            if (count == 0) return new ResponseEntity<>(player1, HttpStatus.OK);
            rightNow.setTime(player1.getBirthday());
            int playerYear = rightNow.get(Calendar.YEAR);

            if (id <= 0 || Objects.requireNonNull(player1).getName().length() > 12 || player1.getTitle().length() > 30 || player1.getName().equals("") ||
                    player1.getExperience() < 0 || player1.getExperience() > 10_000_000L ||  playerYear < 2000 ||
                    playerYear > 3000) throw new RuntimeException();
            playerService.editPlayer(player1);
            return new ResponseEntity<>(player1, HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/rest/players")
//    public void createPlayer(@RequestParam (value = "name") String name,
//                               @RequestParam (value = "title") String title,
//                               @RequestParam (value = "race") Race race,
//                               @RequestParam (value = "profession") Profession profession,
//                               @RequestParam (value = "birthday") Long birthday,
//                               @RequestParam (value = "banned", required = false) Boolean banned,
//                               @RequestParam (value = "experience") Integer experience){
    public ResponseEntity<Player> createPlayer(@RequestBody Player player){

        try{
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(player.getBirthday());
            int playerYear = rightNow.get(Calendar.YEAR);
            if (player.getName().length() > 12 || player.getTitle().length() > 30 || player.getName().equals("") ||
                player.getExperience() < 0 || player.getExperience() > 10_000_000L ||  playerYear < 2000 ||
                    playerYear > 3000) throw new RuntimeException();
            Player _player = playerService.addPlayer(new Player(player.getName(), player.getTitle(), player.getRace(), player.getProfession(), player.getBirthday(),
                    player.isBanned(), player.getExperience()));
            return new ResponseEntity<>(_player, HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/rest/players/{id}")
    public Player getPlayerById(@PathVariable("id") long id){
        Player player;
        try {
            player = playerService.getById(id);
            if (id <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request is bad");
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request is bad");
        }

        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "player not found");
        }
        return player;

    }

    @GetMapping("/rest/players/count")
    public Integer allPlayersCount(@RequestParam (value = "name", required = false) String name,
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
        return filerByParam(name, title, race, profession, after, before, banned, minExperience, maxExperience,
                minLevel, maxLevel, order, pageNumber, pageSize, listPlayers, "count").size();

    }

    public static List<Player> filerByParam(String name,
                                            String title,
                                            Race race,
                                            Profession profession,
                                            Long after,
                                            Long before,
                                            Boolean banned,
                                            Integer minExperience,
                                            Integer maxExperience,
                                            Integer minLevel,
                                            Integer maxLevel,
                                            PlayerOrder order,
                                            Integer pageNumber,
                                            Integer pageSize,
                                            List<Player> listPlayers, String type){
        if (!Objects.isNull(name)) {
            listPlayers = listPlayers.stream().filter(s -> s.getName().matches("(.*)" + name + "(.*)")).collect(Collectors.toList());
        }
        if (!Objects.isNull(title)) {
            listPlayers = listPlayers.stream().filter(s -> s.getTitle().matches("(.*)" + title + "(.*)")).collect(Collectors.toList());
        }
        if (!Objects.isNull(race)) {
            listPlayers = listPlayers.stream().filter(s -> s.getRace().equals(race)).collect(Collectors.toList());
        }
        if (!Objects.isNull(profession)) {
            listPlayers = listPlayers.stream().filter(s -> s.getProfession().equals(profession)).collect(Collectors.toList());
        }
        if (!Objects.isNull(before) && !Objects.isNull(after) && (after < before)) {
            listPlayers = listPlayers.stream().filter(s -> (s.getBirthday().getTime() > after) &&
                    (s.getBirthday().getTime() < before)).collect(Collectors.toList());
        } else if (!Objects.isNull(after)) {
            listPlayers = listPlayers.stream().filter(s -> (s.getBirthday().getTime() > after)).collect(Collectors.toList());
        } else if (!Objects.isNull(before)) {
            listPlayers = listPlayers.stream().filter(s -> (s.getBirthday().getTime() < before)).collect(Collectors.toList());
        }
        if (!Objects.isNull(banned)) {
            listPlayers = listPlayers.stream().filter(s -> s.isBanned().equals(banned)).collect(Collectors.toList());
        }
        if (!Objects.isNull(minExperience) && !Objects.isNull(maxExperience) && (maxExperience > minExperience)) {
            listPlayers = listPlayers.stream().filter(s -> (s.getExperience() >= minExperience) &&
                    (s.getExperience() <= maxExperience)).collect(Collectors.toList());
        } else if (!Objects.isNull(minExperience)) {
            listPlayers = listPlayers.stream().filter(s -> (s.getExperience() >= minExperience)).collect(Collectors.toList());
        } else if (!Objects.isNull(maxExperience)) {
            listPlayers = listPlayers.stream().filter(s -> (s.getExperience() <= maxExperience)).collect(Collectors.toList());
        }
        if (!Objects.isNull(minLevel) && !Objects.isNull(maxLevel) && (maxLevel > minLevel)) {
            listPlayers = listPlayers.stream().filter(s -> (s.getLevel() >= minLevel) &&
                    (s.getLevel() <= maxLevel)).collect(Collectors.toList());
        } else if (!Objects.isNull(minLevel)) {
            listPlayers = listPlayers.stream().filter(s -> (s.getLevel() >= minLevel)).collect(Collectors.toList());
        } else if (!Objects.isNull(maxLevel)) {
            listPlayers = listPlayers.stream().filter(s -> (s.getLevel() <= maxLevel)).collect(Collectors.toList());
        }
        if (Objects.nonNull(order)) {
            switch (order) {
                case NAME:
                    listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getName)).collect(Collectors.toList());
                    break;
                case EXPERIENCE:
                    listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getExperience)).collect(Collectors.toList());
                    break;
                case BIRTHDAY:
                    listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getBirthday)).collect(Collectors.toList());
                    break;
                case LEVEL:
                    listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getLevel)).collect(Collectors.toList());
                    break;
                case ID:
                default:
                    listPlayers = listPlayers.stream().sorted(Comparator.comparing(Player::getId)).collect(Collectors.toList());
                    break;
            }
        }
        if (type.equals("players")) {
            if (Objects.nonNull(pageNumber) && Objects.nonNull(pageSize)) {
                if (pageNumber >= 1) {
                    listPlayers = listPlayers.stream().skip((long) (pageNumber) * pageSize).limit(pageSize).collect(Collectors.toList());
                } else {
                    listPlayers = listPlayers.stream().limit(pageSize).collect(Collectors.toList());
                }

            }
        }
        return listPlayers;
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
                                   @RequestParam (value = "pageSize", defaultValue = "3", required = false) Integer pageSize) {
        List<Player> listPlayers = playerService.getAll();
        return filerByParam(name, title, race, profession, after, before, banned, minExperience, maxExperience,
                    minLevel, maxLevel, order,pageNumber, pageSize, listPlayers, "players");
    }
}
