package com.devops26.gateway.exception;

public class TuneIslandException extends RuntimeException{
    public TuneIslandException(String message) { super(message); }

    public static TuneIslandException notLogin(){
        return new TuneIslandException("未登录!");
    }


}
