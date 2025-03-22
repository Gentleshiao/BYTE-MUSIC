package com.devops26.user.exception;


import com.devops26.user.entity.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = TuneIslandException.class)
    public ResultVO<String> handleAIExternalException(TuneIslandException e) {
        e.printStackTrace();
        log.error(e.getMessage());
        return ResultVO.buildFailure(e.getMessage());
    }
}
