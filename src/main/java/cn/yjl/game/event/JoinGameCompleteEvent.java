package cn.yjl.game.event;

import cn.yjl.game.service.GameService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class JoinGameCompleteEvent extends BaseGameEvent {

    private List<String> userList;

    public JoinGameCompleteEvent(GameService source) {
        super(source);
    }
}
