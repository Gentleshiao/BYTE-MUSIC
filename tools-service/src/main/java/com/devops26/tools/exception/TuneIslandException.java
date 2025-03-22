package com.devops26.tools.exception;

public class TuneIslandException extends RuntimeException{
    public TuneIslandException(String message) { super(message); }

    public static TuneIslandException uploadFailed() {
        return new TuneIslandException("上传失败!");
    }

}
