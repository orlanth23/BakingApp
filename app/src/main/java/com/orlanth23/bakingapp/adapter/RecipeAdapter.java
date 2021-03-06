package com.orlanth23.bakingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.orlanth23.bakingapp.R;
import com.orlanth23.bakingapp.activity.RecipeDetailActivity;
import com.orlanth23.bakingapp.domain.Recipe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by orlanth23 on 24/06/2017.
 */

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final List<Recipe> mRecipes;
    private Activity mActivity;

    public RecipeAdapter(AppCompatActivity activity, List<Recipe> recipes) {
        mActivity =activity;
        mRecipes = recipes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mRecipe = mRecipes.get(position);
        holder.mContentView.setText(mRecipes.get(position).getName());

        if (!TextUtils.isEmpty(holder.mRecipe.getImage())) {
            holder.mRecipeImage.setVisibility(View.VISIBLE);
            Glide.with(mActivity)
                    .load(holder.mRecipe.getImage())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.mRecipeImage);
        } else {
            holder.mRecipeImage.setVisibility(View.GONE);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailActivity.ARG_RECIPE, holder.mRecipe);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        @BindView(R.id.recipe_name)
        TextView mContentView;

        @BindView(R.id.recipe_image)
        ImageView mRecipeImage;

        Recipe mRecipe;

        ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, mView);
        }
    }
}
