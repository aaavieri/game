package cn.yjl.game.event;

import cn.yjl.game.service.GameService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SkipLordGameEvent extends BaseGameEvent {

    private String nextLordUser;

    public SkipLordGameEvent(GameService source) {
        super(source);
    }
}
