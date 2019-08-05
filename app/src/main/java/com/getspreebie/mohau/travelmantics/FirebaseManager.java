package com.getspreebie.mohau.travelmantics;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FirebaseManager {
    private static FirebaseDatabase db;
    private DatabaseReference dbReference;
    private static ArrayList<TravelDeal> deals;
    private static FirebaseManager instance = null;

    private FirebaseManager() {}

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
            db = FirebaseDatabase.getInstance();
        }
        return instance;
    }

    public DatabaseReference getDatabaseReference(String path) {
        return db.getReference(path);
    }

    public FirebaseDatabase getDatabase() {
        return db;
    }

    public TravelDeal getDeal(String id) {
        for (TravelDeal deal : deals) {
            if (deal.getId().equals(id)) {
                return deal;
            }
        }
        return null;
    }

    public ArrayList<TravelDeal> getDeals() {
        return deals;
    }

    public void addDeal(TravelDeal deal) {
        deals.add(deal);
    }

//    public void addDeal(TravelDeal newDeal) {
//        for (TravelDeal deal : deals) {
//            if (deal.getId().equals(newDeal.getId())) {
//                return;
//            }
//        }
//
//        deals.add(newDeal);
//    }

    public void removeDeal(String id) {
        for (TravelDeal deal : deals) {
            if (deal.getId().equals(id)) {
                deals.remove(deal);
                return;
            }
        }
    }

    public void updateDeal(TravelDeal newDeal) {
        int index = 0;
        for (TravelDeal deal : deals) {
            if (deal.getId().equals(newDeal.getId())) {
                deals.add(index, newDeal);
                deals.remove(index+1);
                return;
            }
            index++;
        }
    }

    public void resetDealList() {
        deals = new ArrayList<TravelDeal>();
    }

}
