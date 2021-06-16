package com.billbook.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.billbook.app.R;
import com.billbook.app.adapters.CategoriesSelectionAdapter;
import com.billbook.app.database.models.Category;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SelectOtherCategoriesActivity extends AppCompatActivity {
    private final String TAG = "categoriesSElect";
    private ArrayList<Category> categories = new ArrayList<>();
    private CategoriesSelectionAdapter categoriesSelectionAdapter;
    private EditText otherCatEdt;
    private RecyclerView categorySelectionRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_other_categories);
        setDataForOther();
        initUI();
    }

    private void setDataForOther() {
        categories.clear();
        Category category = new Category();
        category.setName("Garments");
        categories.add(category);

        category = new Category();
        category.setName("Medical Shop");
        categories.add(category);

        category = new Category();
        category.setName("General/Kiryana store");
        categories.add(category);

        category = new Category();
        category.setName("Restaurant");
        categories.add(category);

        category = new Category();
        category.setName("Stationary shop");
        categories.add(category);

        category = new Category();
        category.setName("Confectionary shop");
        categories.add(category);

        category = new Category();
        category.setName("Salon");
        categories.add(category);

        category = new Category();
        category.setName("Cosmetic shop");
        categories.add(category);

        category = new Category();
        category.setName("Gift shop");
        categories.add(category);

        category = new Category();
        category.setName("Cosmetic shop");
        categories.add(category);
    }

    private void initUI() {
        otherCatEdt = findViewById(R.id.otherCatEdt);
        categorySelectionRV = findViewById(R.id.categorySelectionRV);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        categorySelectionRV.setLayoutManager(mLayoutManager);
        categorySelectionRV.setItemAnimator(new DefaultItemAnimator());
        if (categories != null) {
            Log.v(TAG, "categoryArrayList::" + categories.size());
            categoriesSelectionAdapter = new CategoriesSelectionAdapter(SelectOtherCategoriesActivity.this, categories);
            categorySelectionRV.setAdapter(categoriesSelectionAdapter);
        }
    }

    public void gotoOTP(View v) {
        try {
            JSONObject userObj = new JSONObject(getIntent().getStringExtra("userData"));
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).isSelected()) {
                    stringBuffer.append(categories.get(i).getName());
                    stringBuffer.append(",");
                }
            }
            if (!otherCatEdt.getText().toString().isEmpty()) {
                stringBuffer.append(otherCatEdt.getText().toString());
            }

            userObj.put("product_category", stringBuffer.toString());
            JSONArray array = new JSONArray(new ArrayList());
            userObj.put("category", array);
            Log.v(TAG, userObj.toString());
//            userObj.put("product_category",DealerCodeYes.isChecked());

            Intent intent = new Intent(this, OTPActivity.class);
            intent.putExtra("userData", userObj.toString());
            intent.putExtra("other", true);

            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
