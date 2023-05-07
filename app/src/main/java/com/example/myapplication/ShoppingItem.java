package com.example.myapplication;

public class ShoppingItem {
    private String name;
    private String info;
    private String price;
    private String category;
    private float ratedInfo;
    private int imageResource;

    public ShoppingItem(String name, String info, String price,String category, float ratedInfo, int imageResource) {
        this.name = name;
        this.info = info;
        this.price = price;
        this.category=category;
        this.ratedInfo = ratedInfo;
        this.imageResource = imageResource;
    }

    public ShoppingItem() {
    }

    public String getName() {
        return name;
    }
    public String getInfo() {
        return info;
    }
    public String getPrice() {
        return price;
    }
    public String getCategory() {
        return category;
    }
    public float getRatedInfo() {
        return ratedInfo;
    }
    public int getImageResource() {
        return imageResource;
    }
}
