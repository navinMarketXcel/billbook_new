package com.billbook.app.adapter_bill_callback;

import com.billbook.app.model.InvoicesData;

public interface BillCallback {
    void callback(String action, InvoicesData data, Integer pos);
}
