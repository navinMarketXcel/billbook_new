package com.billbook.app.networkcommunication;

public class InventoryAPIRequest {
    private String serial_no;
    private int user;
    private int product;
    private int quantity;
    private int price;
    private int tax;
    private int selling_cost;
    private String category_name;
    private String brand_name;
    private String product_name;


    @Override
    public String toString() {
        return "InventoryAPIRequest{" +
                "serial_no='" + serial_no + '\'' +
                ", user=" + user +
                ", product=" + product +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }

    public String getSerial_no() {
        return serial_no;
    }

    public void setSerial_no(String serial_no) {
        this.serial_no = serial_no;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getProduct() {
        return product;
    }

    public void setProduct(int product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public int getSelling_cost() {
        return selling_cost;
    }

    public void setSelling_cost(int selling_cost) {
        this.selling_cost = selling_cost;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getBrand_name() {
        return brand_name;
    }

    public void setBrand_name(String brand_name) {
        this.brand_name = brand_name;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }
}
