package com.example.kvantoriumproject.Chat;

public class AwesomeMessage {
    private String text;
    private String name;
    private String nameRecipient;
    private String imgUrl;
    private String sender;
    private String recipient;
    private boolean isMine;

    public AwesomeMessage() {
    }

    public AwesomeMessage(String text, String name, String imgUrl, String sender, String recipient, boolean isMine) {
        this.text = text;
        this.name = name;
        this.imgUrl = imgUrl;
        this.sender = sender;
        this.recipient = recipient;
        this.isMine = isMine;
    }

    public String getNameRecipient() {
        return nameRecipient;
    }

    public void setNameRecipient(String nameRecipient) {
        this.nameRecipient = nameRecipient;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
