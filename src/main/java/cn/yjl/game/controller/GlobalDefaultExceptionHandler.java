package cn.yjl.game.controller;

import cn.yjl.game.dto.ResponseJsonDto;
import cn.yjl.game.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalDefaultExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    @ResponseBody
    public ResponseJsonDto handleApplicationException(ApplicationException e) {
        log.error(e.getMessage(), e);
        return new ResponseJsonDto().fail(e.getMessage(), e.getErrCode()).setApplicationError(true);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseJsonDto handleException(Throwable e) {
        log.error(e.getMessage(), e);
        return new ResponseJsonDto().fail(e.getMessage());
    }
}
