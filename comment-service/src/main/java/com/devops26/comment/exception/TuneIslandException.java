package com.devops26.comment.exception;

public class TuneIslandException extends RuntimeException{
    public TuneIslandException(String message) { super(message); }

    public static TuneIslandException permissionDenied(){ return new TuneIslandException("权限不足"); }

    public static TuneIslandException commentNotFound() {
        return new TuneIslandException("评论不存在");
    }

    public static TuneIslandException alreadyLiked() {
        return new TuneIslandException("您已经点过赞了");
    }

}
