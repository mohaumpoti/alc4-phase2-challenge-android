package com.getspreebie.mohau.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<TravelDeal> deals = null;
    private FirebaseStorage firebaseStorage;


    public DealAdapter(Context context, ArrayList<TravelDeal> deals) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.deals = deals;
        this.firebaseStorage = FirebaseStorage.getInstance();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewPrice;
        private ImageView imageViewDeal;
        private TravelDeal deal;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            imageViewDeal = itemView.findViewById(R.id.imageViewDealThumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Click to edit an existing deal

                    // Hold the image in a Settings instance in order to retrieve it in the next activity
                    Settings settings = Settings.getInstance();
                    settings.setDrawable(imageViewDeal.getDrawable());

                    Intent intent = new Intent(context, DealActivity.class);
                    intent.putExtra(DealActivity.DEAL_OBJECT, deal);
                    intent.putExtra(DealActivity.IS_NEW_DEAL, false);
                    context.startActivity(intent);
                }
            });
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.list_item_deal, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (deals != null) {
            TravelDeal deal = deals.get(position);

            holder.deal = deal;
            holder.textViewTitle.setText(deal.getTitle());
            holder.textViewDescription.setText(deal.getDescription());

            // Format the price as a currency
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
            Integer priceInt = Integer.valueOf(deal.getPrice());
            String currency = format.format(priceInt);
            holder.textViewPrice.setText(currency);

            String imagePath = deal.getImagePath();
            if (imagePath != null) {
                downloadImage(holder.imageViewDeal, deal.getImagePath());
            } else {
                holder.imageViewDeal.setImageResource(R.drawable.ic_photo_black_24dp);
            }
        }
    }

    private void downloadImage(final ImageView imageViewDeal, String path) {
        StorageReference pathReference =
                firebaseStorage.getReference().child(path);

        pathReference.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("download_error", "Error: "+e.getLocalizedMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get()
                        .load(uri)
                        .placeholder(R.drawable.ic_photo_black_24dp)
                        .into(imageViewDeal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deals == null ? 0 : deals.size();
    }

    public void updateList(ArrayList<TravelDeal> deals) {
        this.deals = deals;
        notifyDataSetChanged();
    }
}
