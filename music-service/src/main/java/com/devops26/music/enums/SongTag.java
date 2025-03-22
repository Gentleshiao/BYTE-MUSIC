package com.devops26.music.enums;

public enum SongTag {

    // 音乐类型 (Genre)
    POP("流行"),
    ROCK("摇滚"),
    RAP("说唱"),
    RNB("R&B"),
    CLASSICAL("古典"),
    FOLK("民谣"),
    ELECTRONIC("电子"),

    // 情感氛围 (Mood)
    HAPPY("愉快"),
    SAD("悲伤"),
    RELAXED("放松"),
    MOTIVATIONAL("励志"),
    ROMANTIC("浪漫"),

    // 使用场景 (Scene)
    WORKOUT("健身"),
    STUDY("学习"),
    DRIVING("驾车"),
    PARTY("聚会"),
    SLEEP("睡前");

    private final String displayName;

    SongTag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

