package com.example.talkative;

public class Request {
    private String senderEmail;
    private String senderName;
    private String receiverEmail;
    private String receiverName;

    public Request() {
    }

    public Request(String senderEmail, String senderName, String receiverEmail, String receiverName) {
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.receiverEmail = receiverEmail;
        this.receiverName = receiverName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public String getReceiverName() {
        return receiverName;
    }
}
