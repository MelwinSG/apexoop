package model;

public class Customer extends User {
    public Customer(long id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }
}
