package com.libre.client.activity;

public class DataItem {
       private Integer ItemID;
       private String ItemName;
       private String ItemType;

        public DataItem() {

            ItemID = null;
            ItemName = null;
            ItemType = null;
        }

    public Integer getItemID() {
        return ItemID;
    }

    public String getItemName() {
        return ItemName;
    }

    public String getItemType() {
        return ItemType;
    }

    public void setItemID(Integer itemID) {
        ItemID = itemID;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public void setItemType(String itemType) {
        ItemType = itemType;
    }
}