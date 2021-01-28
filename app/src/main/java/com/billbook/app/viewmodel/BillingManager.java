package com.billbook.app.viewmodel;

import com.billbook.app.database.models.Purchase;

import java.util.ArrayList;

public class BillingManager {
    private static BillingManager billingManager;
    private static ArrayList<Purchase> purchaseArrayList = new ArrayList<>();

    private BillingManager() {

    }

    public static BillingManager getInstance() {
        if (billingManager == null) {
            billingManager = new BillingManager();
        }
        return billingManager;
    }

    public static void reset() {
        purchaseArrayList = new ArrayList<>();
    }

    public static void remove(int position) {
        purchaseArrayList.remove(position);
    }

    public ArrayList<Purchase> getCurrentPurchaseList() {
        return purchaseArrayList;

    }
}
