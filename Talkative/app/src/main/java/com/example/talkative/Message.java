package com.example.talkative;

public class Message {
    private String content;
    private String email1;
    private String name1;
    private String email2;
    private String name2;
    private int sender;
    private String date;

    public Message() {}

    public Message(String content, String date, String email1, String name1, String email2, String name2, int sender) {
        this.content = content;
        this.date = date;
        this.email1 = email1;
        this.email2 = email2;
        this.name1 = name1;
        this.name2 = name2;
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public String getEmail1() {
        return email1;
    }

    public String getEmail2() {
        return email2;
    }

    public String getName1() {
        return name1;
    }

    public String getName2() {
        return name2;
    }

    public int getSender() {
        return sender;
    }
}
