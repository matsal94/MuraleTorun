package co.jwwebdev.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.master.glideimageview.GlideImageView;

import java.util.ArrayList;
import java.util.List;

import co.jwwebdev.MuralDetails;
import co.jwwebdev.model.Mural;
import co.jwwebdev.muraletorun.R;


public class MuralAdapter extends RecyclerView.Adapter<MuralAdapter.ViewHolder> {

    private ArrayList<Mural> muralsList;
    private Context context;


    public MuralAdapter(Context context, ArrayList<Mural> muralsList) {

        this.context = context;
        this.muralsList = muralsList;
    }


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    @Override
    public int getItemCount() {
        return muralsList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    public Object getItem(int position) {
        return muralsList.get(position);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Glide.with(context)
                .load(context.getResources().getDrawable(muralsList.get(position).getMuralsImageList().get(0)))
                .error(Glide.with(context).load(R.drawable.no_image))
                .into(holder.imageGIV);

        holder.nameTV.setText(muralsList.get(position).getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Adapter");

                Intent intent = new Intent(context.getApplicationContext(), MuralDetails.class);

                Bundle extras = new Bundle();
                extras.putIntegerArrayList("Images", (ArrayList<Integer>) muralsList.get(position).getMuralsImageList());
                extras.putString("Name", muralsList.get(position).getName());
                extras.putString("Address", muralsList.get(position).getAddress());
                extras.putDouble("Lat", muralsList.get(position).getLat());
                extras.putDouble("Lon", muralsList.get(position).getLon());
                extras.putString("Description", muralsList.get(position).getDescription());
                intent.putExtra("Mural", extras);
/*
                Pair<View, String> pair1 = Pair.create((View) holder.nameTV, holder.nameTV.getTransitionName());
                //Pair<View, String> pair2 = Pair.create((View) holder.addressTV, holder.addressTV.getTransitionName());

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context, pair1);
                context.startActivity(intent, activityOptions.toBundle());*/

                context.startActivity(intent);

                Activity activity = (Activity) context;
                activity.overridePendingTransition(R.anim.left_to_right_enter, R.anim.left_to_right_exit);
            }
        });
    }


    public void reload(List<Mural> listD) {

        muralsList = new ArrayList<>();
        muralsList.addAll(listD);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.item_murals_list, parent, false);
        return new ViewHolder(itemView);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        GlideImageView imageGIV;
        TextView nameTV;

        ViewHolder(View itemView) {
            super(itemView);

            imageGIV = (GlideImageView) itemView.findViewById(R.id.item_murals_list_imageGIV);
            nameTV = (TextView) itemView.findViewById(R.id.item_murals_list_nameTV);
        }
    }
}
