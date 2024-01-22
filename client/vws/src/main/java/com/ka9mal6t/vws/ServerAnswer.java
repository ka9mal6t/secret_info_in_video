package com.ka9mal6t.vws;

public class ServerAnswer {
    private int codeAnswer;
    private String sessionKey;
    private String aesKey;
    private String error;
    private String message;
    public ServerAnswer(int codeAnswer, String sessionKey, String aesKey){
        this.codeAnswer = codeAnswer;
        this.sessionKey = sessionKey;
        this.aesKey = aesKey;
    }
    public ServerAnswer(int codeAnswer){
        this.codeAnswer = codeAnswer;
    }
    public ServerAnswer(int codeAnswer, String sessionKey, String aesKey, String error){
        this.codeAnswer = codeAnswer;
        this.sessionKey = sessionKey;
        this.aesKey = aesKey;
        this.error = error;
    }
    public ServerAnswer(int codeAnswer, String sessionKey, String aesKey, String error, String message){
        this.codeAnswer = codeAnswer;
        this.sessionKey = sessionKey;
        this.aesKey = aesKey;
        this.error = error;
        this.message = message;
    }

    public int getCodeAnswer() {
        return codeAnswer;
    }

    public String getAesKey() {
        return aesKey;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionKey() {
        return sessionKey;
    }
}
