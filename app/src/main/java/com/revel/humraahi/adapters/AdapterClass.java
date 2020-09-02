package com.revel.humraahi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.revel.humraahi.R;
import com.revel.humraahi.classes.Model;

import java.util.List;

import static com.revel.humraahi.fragments.HumraahiFragment.nearbyMails;

public class AdapterClass extends PagerAdapter {

    private List<Model> models;
    private LayoutInflater layoutInflater;
    private Context context;

    public AdapterClass(List<Model> models, Context context) {
        this.models = models;
        this.context = context;
    }


    @Override
    public int getCount() {
        if (models.size() == 0) {
            return nearbyMails.size();
        } else {
            return models.size();
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.item, container, false);
        TextView tv = v.findViewById(R.id.textItem);
        ImageView imv = v.findViewById(R.id.itemImage);
        ProgressBar imageProgressbar = v.findViewById(R.id.imageProgressBar);
        if (models.size() > 0) {
            String name = models.get(position).getUsername();
            if (name == null || name.isEmpty()) {
                tv.setText("null");
            } else {
                tv.setText(models.get(position).getUsername());
            }
            Glide.with(context).load(models.get(position).getImage()).centerCrop().into(imv);
            imageProgressbar.setVisibility(View.GONE);
        }
        container.addView(v);

        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
