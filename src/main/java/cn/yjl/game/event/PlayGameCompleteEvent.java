package cn.yjl.game.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PlayGameCompleteEvent extends ApplicationEvent {

    private int gameId;

    private String lastPlayUserId;

    public PlayGameCompleteEvent(Object source) {
        super(source);
    }
}
