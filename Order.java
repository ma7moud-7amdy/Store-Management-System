package StoreManagement;

import java.time.LocalDate;
import java.util.Date;

public class Order {
    private int orderId;
    private int customerId;
    private String customerName;

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    private String staffName;
    private Date orderDate;
    private double totalAmount;

    public Order() {
    }

    public Order(int orderId, String customerName, Date orderDate, double totalAmount) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
    }
    public Order(int orderId, String customerName, String staffName ,Date orderDate, double totalAmount) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.staffName = staffName;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
    }
    public Order(int orderId, int customerId, Date orderDate, double totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
    }


    // Getter and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}