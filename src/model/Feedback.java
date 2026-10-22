package model;

import java.time.LocalDateTime;

public class Feedback {
    private final long id;
    private final String customerName;
    private final String productName;
    private final int rating;
    private final String comment;
    private final LocalDateTime createdAt;
    private final Response response;
    private final long helpfulVotes;
    private final long notHelpfulVotes;

    public Feedback(long id, String customerName, String productName, int rating, String comment,
                    LocalDateTime createdAt, Response response) {
        this(id, customerName, productName, rating, comment, createdAt, response, 0, 0);
    }

    public Feedback(long id, String customerName, String productName, int rating, String comment,
                    LocalDateTime createdAt, Response response, long helpfulVotes, long notHelpfulVotes) {
        this.id = id;
        this.customerName = customerName;
        this.productName = productName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.response = response;
        this.helpfulVotes = helpfulVotes;
        this.notHelpfulVotes = notHelpfulVotes;
    }

    public long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getProductName() {
        return productName;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Response getResponse() {
        return response;
    }

    public long getHelpfulVotes() {
        return helpfulVotes;
    }

    public long getNotHelpfulVotes() {
        return notHelpfulVotes;
    }
}
