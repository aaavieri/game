package cn.yjl.game.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ApplicationException extends RuntimeException {
    private int errCode;
    private String message;
}
