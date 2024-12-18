package StoreManagement;

public class Staff {

    private int id;
    private String name;
    private String phone;
    private String gender;

    private int orders_sold;


    // Constructor

    public Staff(int id, String name, String phone, String gender , int orders_sold) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.orders_sold = orders_sold;
        this.gender = gender;
    }


    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getOrders_sold() {
        return orders_sold;
    }

    public void setOrders_sold(int orders_sold) {
        this.orders_sold = orders_sold;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

}