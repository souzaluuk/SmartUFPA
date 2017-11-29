package br.ufpa.smartufpa.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import br.ufpa.smartufpa.R;

/**
 * Created by kaeuchoa on 29/11/2017.
 */

public class AddPlaceOptionAdapter extends RecyclerView.Adapter {

    private Context parentContext;
    private String[] placeCategories;
    private int[] placeCategoriesIcons;
    private AddPlaceOptionAdapter.OnItemClickListener onItemClickListener;

    public AddPlaceOptionAdapter(Context parentContext) {
        this.parentContext = parentContext;
        // List of categories defined on values>array.xml
        placeCategories = parentContext.getResources().getStringArray(R.array.default_places);
        placeCategoriesIcons = parentContext.getResources().getIntArray(R.array.default_places_drawables);
    }

    /**
     * Interface to mimic the OnItemClick from list view
     */
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(AddPlaceOptionAdapter.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parentContext)
                .inflate(R.layout.add_place_option_item,parent,false);

        return new AddPlaceOptionAdapter.AddPlaceOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        AddPlaceOptionViewHolder viewHolder = (AddPlaceOptionViewHolder) holder;

        // Sets the title for the card
        viewHolder.txtAddPlaceCategory.setText(placeCategories[position]);

        // Sets the image for the card
        Drawable currentDrawable = ContextCompat.getDrawable(parentContext,placeCategoriesIcons[position]);
        viewHolder.imgAddPlaceCategory.setImageDrawable(currentDrawable);
    }


    @Override
    public int getItemCount() {
        return placeCategories.length;
    }



    private class AddPlaceOptionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView txtAddPlaceCategory;
        private ImageView imgAddPlaceCategory;


        private AddPlaceOptionViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            txtAddPlaceCategory = itemView.findViewById(R.id.txt_add_place_category);
            imgAddPlaceCategory = itemView.findViewById(R.id.img_add_place_category);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null)
                onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

}
