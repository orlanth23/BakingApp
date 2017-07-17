package com.orlanth23.bakingapp.activity;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.FloatingActionButton;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.orlanth23.bakingapp.IdlingResource.SimpleIdlingResource;
import com.orlanth23.bakingapp.R;
import com.orlanth23.bakingapp.adapter.RecipeAdapter;
import com.orlanth23.bakingapp.broadcast.NetworkReceiver;
import com.orlanth23.bakingapp.domain.Recipe;
import com.orlanth23.bakingapp.network.NetworkUtils;
import com.orlanth23.bakingapp.provider.ProviderUtilities;
import com.orlanth23.bakingapp.singleton.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class RecipeListActivity extends AppCompatActivity implements NetworkReceiver.NetworkChangeListener {

    private static final String TAG = RecipeListActivity.class.getName();

    private Context mContext = this;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mRefreshButton;
    private static final int NUMBER_GRID_COLUMN = 3;
    private boolean isConnected;
    private NetworkReceiver mNetworkReceiver;

    // The Idling Resource which will be null in production.
    @Nullable
    private SimpleIdlingResource mIdlingResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(false);
        }

        mNetworkReceiver = new NetworkReceiver(this);
        registerReceiver(mNetworkReceiver, NetworkReceiver.CONNECTIVITY_CHANGE_INTENT_FILTER);

        setContentView(R.layout.activity_recipe_list);

        TextView textView = (TextView) findViewById(R.id.toolbar_title);
        mRefreshButton = (FloatingActionButton) findViewById(R.id.refresh_button);

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData();
            }
        });

        textView.setTypeface(Typeface.createFromAsset(getAssets(), Constants.DANCING_FONT));
        textView.setText(getTitle());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, NUMBER_GRID_COLUMN);
        mRecyclerView = (RecyclerView) findViewById(R.id.recipe_list);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        // Récupération des données à partir du contentProvider
        ArrayList<Recipe> recipeList = ProviderUtilities.getListRecipeFromContentProvider(this);
        if (recipeList.size() > 0) {
            setupRecyclerView(mRecyclerView, recipeList);
        } else {
            if (mNetworkReceiver.checkConnection(this)) {
                refreshData();
            } else {
                Toast.makeText(this, "Pas de connexion réseau pour récupérer les recettes.", Toast.LENGTH_LONG).show();
            }
        }

        assert mRecyclerView != null;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, ArrayList<Recipe> recipeArrayList) {
        recyclerView.setAdapter(new RecipeAdapter(recipeArrayList));

        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(true);
        }
    }

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
            mIdlingResource.setIdleState(false);
        }
        return mIdlingResource;
    }

    private void refreshData() {
        new GetRecipesFromNetwork().execute();
    }

    @Override
    public void OnNetworkEnable() {
        mRefreshButton.setVisibility(View.VISIBLE);
        isConnected = true;
    }

    @Override
    public void OnNetworkDisable() {
        mRefreshButton.setVisibility(View.GONE);
        isConnected = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkReceiver);
    }

    private class GetRecipesFromNetwork extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            return NetworkUtils.makeServiceCall(Constants.API_URL);
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            if (json != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Recipe>>() {
                }.getType();
                try {
                    final ArrayList<Recipe> tempList = gson.fromJson(json, listType);
                    setupRecyclerView(mRecyclerView, tempList);

                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            ProviderUtilities.populateContentProviderFromList(mContext, tempList);
                        }
                    };
                    runnable.run();

                } catch (JsonSyntaxException e) {
                    Log.e(TAG, e.getMessage(), e);
                    Toast.makeText(mContext, "Le chargement des recettes n'a pas fonctionné.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
