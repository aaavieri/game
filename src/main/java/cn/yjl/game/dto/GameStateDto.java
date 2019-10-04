package cn.yjl.game.dto;

import cn.yjl.game.enumeration.GameStatusEnum;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.yjl.game.enumeration.GameStatusEnum.NOT_ENOUGH_USER;
import static cn.yjl.game.enumeration.UserGameStatusEnum.WAITING_OTHER_JOIN;

@Data
@Accessors(chain = true)
public class GameStateDto {

    private int gameId;

    private String lordUser;

    private List<CardWrapDto> lordCardList;

    @Getter
    private List<OnceSendCardDto> sentHistory = new ArrayList<>();

    @Getter
    private Map<String, UserGameStateDto> userInfo = new HashMap<>();

    @Getter
    private List<String> userList = new ArrayList<>();

    private GameStatusEnum statusEnum = NOT_ENOUGH_USER;

    public GameStateDto addUser(String userId) {
        this.userInfo.put(userId, new UserGameStateDto().setUserId(userId).setStatus(WAITING_OTHER_JOIN));
        this.userList.add(userId);
        return this;
    }
    
    public GameStateDto onceSend(OnceSendCardDto onceSendCardDto) {
        this.sentHistory.add(onceSendCardDto);
        return this;
    }
}
