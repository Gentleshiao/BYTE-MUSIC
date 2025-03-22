package com.devops26.comment.exception;


import com.devops26.comment.entity.ResultVO;
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
