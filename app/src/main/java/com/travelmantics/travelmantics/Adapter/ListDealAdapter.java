package com.travelmantics.travelmantics.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.travelmantics.travelmantics.Interface.AdapterRefresh;
import com.travelmantics.travelmantics.Interface.DataAvailability;
import com.travelmantics.travelmantics.Interface.GotoListActivity;
import com.travelmantics.travelmantics.R;
import com.travelmantics.travelmantics.Models.TravelDeal;
import com.travelmantics.travelmantics.Util.FirebaseUtil;

import java.util.ArrayList;

public class ListDealAdapter extends RecyclerView.Adapter<ListDealAdapter.ListDealViewHolder> {

    private Context mContext;
    private ArrayList<TravelDeal> mTravelDeals;
    private GotoListActivity listener2;
    private DataAvailability dataListener;


    public ListDealAdapter(Context mContext,GotoListActivity listener2,DataAvailability dataListener) {
        this.mContext = mContext;
        this.dataListener = dataListener;
        this.listener2 = listener2;
        FirebaseUtil.openFbReference("traveldeals",null);
        FirebaseDatabase mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        DatabaseReference mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mTravelDeals = FirebaseUtil.mDeals;
        ChildEventListener mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                td.setId(dataSnapshot.getKey());
                mTravelDeals.add(td);
                notifyItemInserted(mTravelDeals.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @NonNull
    @Override
    public ListDealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view;
        view = inflater.inflate(R.layout.travel_deal_list_item,parent,false);
        return new ListDealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListDealViewHolder holder,final int position) {

            holder.bind(mTravelDeals.get(position));
            holder.card_deal_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TravelDeal selectedDeal = mTravelDeals.get(position);
                    listener2.editTravelDeal(selectedDeal);
                }
            });
    }


    @Override
    public int getItemCount() {
        if(mTravelDeals.size() > 0){
            dataListener.itemCheck();
        }
        return mTravelDeals.size();
    }



    public class ListDealViewHolder extends RecyclerView.ViewHolder {

        ImageView deal_list_img;
        TextView deal_list_title;
        TextView deal_list_description;
        TextView deal_list_price;
        public MaterialCardView card_deal_item;

        public ListDealViewHolder(@NonNull View itemView) {
            super(itemView);
            deal_list_img = itemView.findViewById(R.id.deal_list_img);
            deal_list_title = itemView.findViewById(R.id.deal_list_title);
            deal_list_description = itemView.findViewById(R.id.deal_list_description);
            deal_list_price = itemView.findViewById(R.id.deal_list_price);
            card_deal_item = itemView.findViewById(R.id.card_deal_item);

        }

        public void bind(TravelDeal deal){
            deal_list_title.setText(deal.getTitle());
            deal_list_description.setText(deal.getDescription());
            deal_list_price.setText(deal.getPrice());
            showImage(deal.getImage());
        }

        private void showImage(String url){
            if(url != null && !url.isEmpty()){
                Picasso.with(deal_list_img.getContext())
                        .load(url)
                        .fit()
                        .into(deal_list_img);
            }else{
                Picasso.with(deal_list_img.getContext())
                        .load(R.drawable.test_img)
                        .fit()
                        .into(deal_list_img);
            }
        }

    }


}

