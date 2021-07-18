package com.br0ke.travelbookjava.adaptor;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.BinderThread;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Placeholder;
import androidx.recyclerview.widget.RecyclerView;

import com.br0ke.travelbookjava.databinding.RecyclerRowBinding;
import com.br0ke.travelbookjava.model.Place;
import com.br0ke.travelbookjava.view.MainActivity;
import com.br0ke.travelbookjava.view.MapsActivity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlaceAdaptor extends RecyclerView.Adapter<PlaceAdaptor.PlaceHolder> {

    List<Place> placeList;

    public PlaceAdaptor(List<Place> placeList){
        this.placeList = placeList;
    }


    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PlaceHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceAdaptor.PlaceHolder holder, int position) {
        holder.recyclerRowBinding.recyclerViewTextView.setText(placeList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",placeList.get(position));
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class PlaceHolder extends RecyclerView.ViewHolder{
        RecyclerRowBinding recyclerRowBinding;

        public PlaceHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }
}
