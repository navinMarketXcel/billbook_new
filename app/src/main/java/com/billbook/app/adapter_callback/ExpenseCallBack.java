package com.billbook.app.adapter_callback;

import com.billbook.app.database.models.Expense;

public interface ExpenseCallBack {
    void callback(String action, Expense data  , Integer position);
}
