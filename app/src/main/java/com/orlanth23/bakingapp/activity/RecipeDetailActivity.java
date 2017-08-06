package com.orlanth23.bakingapp.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.orlanth23.bakingapp.Constants;
import com.orlanth23.bakingapp.R;
import com.orlanth23.bakingapp.domain.Recipe;
import com.orlanth23.bakingapp.fragment.RecipeDetailFragment;
import com.orlanth23.bakingapp.fragment.StepDetailFragment;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String TAG_STEP_DETAIL_FRAGMENT = "TAG_STEP_DETAIL_FRAGMENT";
    public static final String ARG_RECIPE = "ARG_RECIPE";
    private static final String TAG_RECIPE_DETAIL_FRAGMENT = "TAG_RECIPE_DETAIL_FRAGMENT";
    private static final String TAG_RECIPE = "TAG_RECIPE";
    private RecipeDetailFragment mRecipeDetailFragment;
    private StepDetailFragment mStepDetailFragment;
    private Recipe mRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Are we in two panel mode
        boolean mTwoPane = findViewById(R.id.frame_step_container) != null;

        if (savedInstanceState != null) {
            mRecipe = savedInstanceState.getParcelable(TAG_RECIPE);
            mRecipeDetailFragment = (RecipeDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, TAG_RECIPE_DETAIL_FRAGMENT);
            mStepDetailFragment = (StepDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, TAG_STEP_DETAIL_FRAGMENT);
        } else {
            mRecipe = getIntent().getParcelableExtra(ARG_RECIPE);
        }

        if (mRecipeDetailFragment != null) {
            mRecipeDetailFragment.updateFragment(mTwoPane, mRecipe);
        } else {
            mRecipeDetailFragment = RecipeDetailFragment.newInstance(mRecipe, mTwoPane);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_detail_recipe, mRecipeDetailFragment, TAG_RECIPE_DETAIL_FRAGMENT)
                    .commit();
        }

        // Show the Up button in the action bar.
        if (findViewById(R.id.toolbar_title) != null) {
            TextView textView = (TextView) findViewById(R.id.toolbar_title);
            textView.setTypeface(Typeface.createFromAsset(getAssets(), Constants.DANCING_FONT));
            textView.setText(mRecipe.getName());

            Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(null);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                NavUtils.navigateUpTo(this, new Intent(this, RecipeListActivity.class));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TAG_RECIPE, mRecipe);

        getSupportFragmentManager().putFragment(outState, TAG_RECIPE_DETAIL_FRAGMENT, mRecipeDetailFragment);

        mStepDetailFragment = (StepDetailFragment) getSupportFragmentManager().findFragmentByTag(TAG_STEP_DETAIL_FRAGMENT);
        if (mStepDetailFragment != null) {
            getSupportFragmentManager().putFragment(outState, TAG_STEP_DETAIL_FRAGMENT, mStepDetailFragment);
        }
    }
}
