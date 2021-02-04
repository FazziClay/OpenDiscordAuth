package ru.fazziclay.opendiscordauth;

import org.bukkit.entity.Player;



public class Code {
    public static Integer TYPE_REGISTRATION_CODE    = 0;
    public static Integer TYPE_LOGIN_CODE           = 1;

    String code;
    Integer type;
    Player player;

    public Code() {
    }

    public Code(String code) {
        this.code = code;
    }

    public Code(String code, Integer type) {
        this.code = code;
        this.type = type;
    }

    public Code(String code, Integer type, Player player) {
        this.code = code;
        this.type = type;
        this.player = player;
    }
}