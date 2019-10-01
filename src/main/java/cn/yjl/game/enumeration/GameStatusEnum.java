package cn.yjl.game.enumeration;

import lombok.Getter;

public enum GameStatusEnum {
    NOT_ENOUGH_USER(1),
    WAITING_START(2),
    WAITING_LORD(3),
    PLAYING(4),
    COMPLETE(5);

    @Getter
    private int value;

    GameStatusEnum(int value) {
        this.value = value;
    }
}
