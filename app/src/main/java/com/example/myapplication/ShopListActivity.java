package com.example.myapplication;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ShopListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String LOG_TAG = ShopListActivity.class.getName();
    private FirebaseUser user;

    private FrameLayout redCircle;
    private EditText maxAr;
    private EditText minAr;
    private TextView countTextView;
    private int cartItems = 0;
    private int gridNumber = 1;
    private Integer itemLimit = 10;

    // Member variables.
    private RecyclerView mRecyclerView;
    private ArrayList<ShoppingItem> mItemsData;
    private ShoppingItemAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;

    private SharedPreferences preferences;

    Spinner spinner;
    private boolean viewRow = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            Log.d(LOG_TAG, "Authenticated user!");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user!");
            finish();
        }


        spinner = findViewById(R.id.categorySpinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_labels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // recycle view
        mRecyclerView = findViewById(R.id.recyclerView);
        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new GridLayoutManager(
                this, gridNumber));
        // Initialize the ArrayList that will contain the data.
        mItemsData = new ArrayList<>();
        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new ShoppingItemAdapter(this, mItemsData);
        mRecyclerView.setAdapter(mAdapter);
        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");
        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver, filter);

        EditText minAr = (EditText) findViewById(R.id.minAr);
        EditText maxAr = (EditText) findViewById(R.id.maxAr);
        minAr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                queryData();
                Log.i("asd",  charSequence.toString());
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                ArrayList<ShoppingItem> min = new ArrayList<>();
                                for (int j=0;j<mItemsData.size();j++){
                                    Log.i("asdnum",  mItemsData.get(j).getPrice().split(" Dv")[0]);
                                    if(Integer.parseInt(mItemsData.get(j).getPrice().split(" Dv")[0])>=Integer.parseInt(charSequence.toString())){
                                        Log.i("asd",  mItemsData.get(j).getPrice().split(" Dv")[0]);
                                        min.add(mItemsData.get(j));
                                        Log.i("asd",  mItemsData.toString());

                                    }
                                }
                                mItemsData.clear();
                                mItemsData.addAll(min);
                                mAdapter.notifyDataSetChanged();
                            }
                        }, 2000);

            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        maxAr.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                queryData();
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                ArrayList<ShoppingItem> max = new ArrayList<>();
                                for (int j=0;j<mItemsData.size();j++){
                                    if(Integer.parseInt(mItemsData.get(j).getPrice().split(" Dv")[0])<=Integer.parseInt(charSequence.toString())){
                                        Log.i("asd",  mItemsData.get(j).getPrice().split(" Dv")[0]);
                                        max.add(mItemsData.get(j));
                                        Log.i("asd",  mItemsData.toString());
                                    }
                                }
                                mItemsData.clear();
                                mItemsData.addAll(max);
                                max.clear();
                                mAdapter.notifyDataSetChanged();
                            }
                        }, 2000);

            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }
    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();

            if (intentAction == null)
                return;

            switch (intentAction) {
                case Intent.ACTION_POWER_CONNECTED:
                    itemLimit = 10;
                    queryData();
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    itemLimit = 5;
                    queryData();
                    break;
            }
        }
    };
    private void initializeData() {
        // Get the resources from the XML file.
        String[] itemsList = getResources()
                .getStringArray(R.array.shopping_item_names);
        String[] itemsCategory = getResources()
                .getStringArray(R.array.shopping_item_categories);
        String[] itemsInfo = getResources()
                .getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources()
                .getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResources =
                getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        // Clear the existing data (to avoid duplication).
        //mItemsData.clear();


        for (int i = 0; i < itemsList.length; i++) {
            mItems.add(new ShoppingItem(
                    itemsList[i],
                    itemsInfo[i],
                    itemsPrice[i],
                    itemsCategory[i],
                    itemRate.getFloat(i, 0),
                    itemsImageResources.getResourceId(i, 0)));
        }

        // Recycle the typed array.
        itemsImageResources.recycle();

        // Notify the adapter of the change.
        //mAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }
    private void queryData() {
        mItemsData.clear();
        mItems.orderBy("name").limit(itemLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShoppingItem item = document.toObject(ShoppingItem.class);
                mItemsData.add(item);
            }

            if (mItemsData.size() == 0) {
                initializeData();
                queryData();
            }

            // Notify the adapter of the change.
            mAdapter.notifyDataSetChanged();
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out_button:
                Log.d(LOG_TAG, "Logout clicked!");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.settings_button:
                Log.d(LOG_TAG, "Setting clicked!");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.cart:
                Log.d(LOG_TAG, "Cart clicked!");
                return true;
            case R.id.view_selector:
                if (viewRow) {
                    changeSpanCount(item, R.drawable.ic_view_grid, 1);
                } else {
                    changeSpanCount(item, R.drawable.ic_view_row, 2);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        countTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon() {
        cartItems = (cartItems + 1);
        if (0 < cartItems) {
            countTextView.setText(String.valueOf(cartItems));
        } else {
            countTextView.setText("");
        }

        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = parent.getItemAtPosition(position).toString();
        mItemsData.clear();
        if(selectedItem.equals("All")){
            queryData();
            return;
        }
        mItems.orderBy("name").whereEqualTo("category",selectedItem).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShoppingItem item = document.toObject(ShoppingItem.class);
                mItemsData.add(item);
            }



            // Notify the adapter of the change.
            mAdapter.notifyDataSetChanged();
        });
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}