package com.gifttome.gifttome;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/*
        #LAM_giftToMe_2020 -reply{
        "id":  "04367a30 -e487 -439d-8f10 -3 f891f0883cc",
        "sender ": "senderUsername",
        "target ": "9a61bc0c -ce39 -4d3b -b72d -e63af675522a",
        "receiver ": "username",
        "message ": "Ciao , sono  interessato ."
*/

public class Reply{

    private UUID id;
    private String sender;
    private UUID targetid;
    private String receiver;
    private String message;
    private Long twitterId;
    private Long replyToId;
    private AvailableObjectsData objectRepliedTo;
    private boolean objectRepliedToIsDeleted;

    public Reply getReplyRepliedTo() {
        return replyRepliedTo;
    }

    public void setReplyRepliedTo(Reply replyRepliedTo) {
        this.replyRepliedTo = replyRepliedTo;
    }

    private Reply replyRepliedTo;

    public AvailableObjectsData getObjectRepliedTo() {
        return this.objectRepliedTo;
    }

    public void setObjectRepliedTo(AvailableObjectsData objectRepliedTo) {
        this.objectRepliedTo = objectRepliedTo;
    }

    public Long getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(Long replyToId) {
        this.replyToId = replyToId;
    }

    public Long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(Long twitterId) {
        this.twitterId = twitterId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public UUID getTargetid() {
        return targetid;
    }

    public void setTargetid(UUID targetid) {
        this.targetid = targetid;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Reply(UUID id, String sender, UUID targetid, String receiver, String message) {
        this.id = id;
        this.sender = sender;
        this.targetid = targetid;
        this.receiver = receiver;
        this.message = message;
        this.objectRepliedTo = null;
        this.replyRepliedTo = null;
    }

    public String formatToString() throws JSONException {
        String string = "#LAM_giftToMe_2020 -reply \n";
        JSONObject json =  new JSONObject();
        json.put("id", this.getId());
        json.put("sender", this.getSender());
        json.put("target", this.getTargetid());
        json.put("receiver", this.getReceiver());
        json.put("message", this.getMessage());

        string = string + json.toString(1);

        return string;
    }
}