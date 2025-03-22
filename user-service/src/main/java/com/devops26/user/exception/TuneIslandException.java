package com.devops26.user.exception;

public class TuneIslandException extends RuntimeException{
    public TuneIslandException(String message) { super(message); }

    public static TuneIslandException notLogin(){
        return new TuneIslandException("未登录!");
    }

    public static TuneIslandException phoneAlreadyExists() {
        return new TuneIslandException("手机号已经被注册过了!");
    }

    public static TuneIslandException phoneOrPasswordError() {
        return new TuneIslandException("手机号或密码错误!");
    }

}
