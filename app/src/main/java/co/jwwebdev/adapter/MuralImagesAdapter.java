package co.jwwebdev.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.master.glideimageview.GlideImageView;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;

import co.jwwebdev.muraletorun.R;


public class MuralImagesAdapter extends RecyclerView.Adapter<MuralImagesAdapter.ViewHolder> {

    private Context context;
    private List<Integer> muralImagesList = new ArrayList<>();
    private List<Uri> imagesPathList = new ArrayList<>();


    public MuralImagesAdapter() {
        super();
    }


    public MuralImagesAdapter(Context context, List<Integer> muralImagesList) {

        this.context = context;
        this.muralImagesList = muralImagesList;
    }


    @NonNull
    @Override
    public MuralImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.item_mural_images_list, parent, false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull MuralImagesAdapter.ViewHolder holder, int position) {

        YoYo.with(Techniques.FadeIn).duration(600).playOn(holder.imageCV); // animacja cardView podczas przewijania
        holder.imageGIV.layout(0, 0, 0, 0); // zablokowanie zmniejszania sie zdjec podczas scrollowania

        Glide.with(context)
                .load(context.getResources().getDrawable(muralImagesList.get(position)))
                .error(Glide.with(context).load(R.drawable.no_image))
                .into(holder.imageGIV)
                .waitForLayout();

        Uri uri = new Uri.Builder() // zamiana z R.drawable.mural1_1 na Uri
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(muralImagesList.get(position)))
                .build();

        imagesPathList.add(uri);

        holder.imageGIV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Fresco.initialize(context);
                new ImageViewer.Builder(context, imagesPathList)
                        .setStartPosition(position)
                        .hideStatusBar(true)
                        .allowZooming(true)
                        .allowSwipeToDismiss(true)
                        .show();
            }
        });
    }


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    @Override
    public int getItemCount() {
        return muralImagesList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        GlideImageView imageGIV;
        CardView imageCV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageGIV = (GlideImageView) itemView.findViewById(R.id.item_mural_images_list_imageGIV);
            imageCV = (CardView) itemView.findViewById(R.id.item_mural_images_listCV);
        }
    }
}