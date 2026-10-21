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

    public Feedback(long id, String customerName, String productName, int rating, String comment,
                    LocalDateTime createdAt, Response response) {
        this.id = id;
        this.customerName = customerName;
        this.productName = productName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.response = response;
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
}
