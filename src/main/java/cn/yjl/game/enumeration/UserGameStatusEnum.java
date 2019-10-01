package cn.yjl.game.enumeration;

import lombok.Getter;

public enum UserGameStatusEnum {
    WAITING_OTHER_JOIN(1),
    WAITING_SELF_START(2),
    WAITING_OTHER_START(3),
    WAITING_SELF_LORD(4),
    WAITING_OTHER_LORD(5),
    WAITING_SELF_PLAY(6),
    WAITING_OTHER_PLAY(7);

    @Getter
    private int value;

    UserGameStatusEnum(int value) {
        this.value = value;
    }
}
