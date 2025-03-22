package com.devops26.music.exception;

public class TuneIslandException extends RuntimeException {
    public TuneIslandException(String message) {
        super(message);
    }

    public static TuneIslandException songNotFound() {
        return new TuneIslandException("歌曲不存在");
    }

    public static TuneIslandException songAlreadyExists() {
        return new TuneIslandException("歌曲已存在");
    }

    public static TuneIslandException playlistNotFound() {
        return new TuneIslandException("播放列表不存在");
    }

    public static TuneIslandException playlistAlreadyExists() {
        return new TuneIslandException("播放列表已存在");
    }

    public static TuneIslandException songAlreadyInPlaylist() {
        return new TuneIslandException("歌曲已在播放列表中");
    }

    public static TuneIslandException songNotInPlaylist() {
        return new TuneIslandException("歌曲不在播放列表中");
    }

    public static TuneIslandException invalidRate() {
        return new TuneIslandException("评分必须在0-5之间");
    }

    public static TuneIslandException alreadyRated() {
        return new TuneIslandException("您已经评分过这首歌曲");
    }

    public static TuneIslandException uploadFailed() {
        return new TuneIslandException("上传失败");
    }

    public static TuneIslandException permissionDenied() {
        return new TuneIslandException("权限不足");
    }

    public static TuneIslandException songlistNotFound() {
        return new TuneIslandException("歌单不存在");
    }

    public static TuneIslandException songlistAlreadyExists() {
        return new TuneIslandException("歌单已存在");
    }

    public static TuneIslandException songAlreadyCollected() {
        return new TuneIslandException("歌曲已被收藏");
    }

    public static TuneIslandException songlistAlreadyCollected() {
        return new TuneIslandException("歌单已被收藏");
    }
} 