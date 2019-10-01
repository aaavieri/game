package cn.yjl.game.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class StartGameCompleteEvent extends ApplicationEvent {

    private int gameId;

    public StartGameCompleteEvent(Object source) {
        super(source);
    }
}
