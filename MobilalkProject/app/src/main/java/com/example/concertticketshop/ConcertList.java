package com.example.concertticketshop;

public class ConcertList {
    private String ID;
    private String name;
    private String info;
    private String price;
    private int imageResource;
    private int reserved_count;

    public ConcertList() {
    }

    public ConcertList(String name, String info, String price, int imageResource, int reserved_count) {
        this.name = name;
        this.info = info;
        this.price = price;
        this.imageResource = imageResource;
        this.reserved_count = reserved_count;
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

    public int getImageResource() {
        return imageResource;
    }

    public int getReserved_count(){
        return reserved_count;
    }

    public String _getID(){
        return ID;
    }

    public void setID(String ID){
        this.ID = ID;
    }

}
