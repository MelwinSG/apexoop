package model;

import java.time.LocalDateTime;

public class Response {
    private final long id;
    private final long feedbackId;
    private final String adminName;
    private final String text;
    private final LocalDateTime respondedAt;

    public Response(long id, long feedbackId, String adminName, String text, LocalDateTime respondedAt) {
        this.id = id;
        this.feedbackId = feedbackId;
        this.adminName = adminName;
        this.text = text;
        this.respondedAt = respondedAt;
    }

    public long getId() {
        return id;
    }

    public long getFeedbackId() {
        return feedbackId;
    }

    public String getAdminName() {
        return adminName;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }
}
