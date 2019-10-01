package cn.yjl.game.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class JoinGameCompleteEvent extends ApplicationEvent {

    private int gameId;

    private List<String> userList;

    public JoinGameCompleteEvent(Object source) {
        super(source);
    }
}
