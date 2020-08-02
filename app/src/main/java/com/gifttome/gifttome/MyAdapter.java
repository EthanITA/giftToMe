package com.gifttome.gifttome;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {


    public List<AvailableObjectsData> AvailableObjectDataList = new ArrayList<AvailableObjectsData>();
    private Context context;
    private static ItemClickListener itemClickListener;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public View view;
        public TextView textView1;
        public TextView textView2;
        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            view = v;
            textView1 = v.findViewById(R.id.object_name);
            textView2 = v.findViewById(R.id.username);
        }


        @Override
        public void onClick(View v) {
            if(itemClickListener!=null) {
                itemClickListener.onClick(v, getAdapterPosition());
            }
            else
                Log.v("FUCK", "fuckcufkcfufk");
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List myDataset, Context context) {

        AvailableObjectDataList.addAll(myDataset);
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.available_object_lay, parent, false);

        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView1.setText(AvailableObjectDataList.get(position).getName());
        holder.textView2.setText(AvailableObjectDataList.get(position).getIssuer());
        /*holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(this, NewActivity.class);
                intent.putExtra("FileName", list.get(position));
                .startActivity(intent);


                goToChatsFragment();
                Toast.makeText(v.getContext() , String.valueOf(position), Toast.LENGTH_SHORT).show();
            }



        });

         */
    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return AvailableObjectDataList.size();

    }


    public void setClickListener(ItemClickListener itemClickListener){
        Log.v("Fucck1", "fufuufufufuo");
        this.itemClickListener = itemClickListener;
    }

}