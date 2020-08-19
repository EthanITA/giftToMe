package com.gifttome.gifttome;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Integer mExpandedPosition = -1;

    private ArrayList<AvailableObjectsData> AvailableObjectDataList = new ArrayList<AvailableObjectsData>();
    private Context context;
    private static ItemClickListener itemClickListener;
    private  View.OnClickListener buttonClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public View view;
        public TextView name;
        public TextView username;
        public TextView description;
        public TextView category;
        public TextView lon;
        public TextView lat;
        private Button chatButton;


        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            name = view.findViewById(R.id.object_name);
            username = view.findViewById(R.id.username);
            description = view.findViewById(R.id.description);
            category = view.findViewById(R.id.category);
            lon = view.findViewById(R.id.lon);
            lat = view.findViewById(R.id.lat);
            chatButton = view.findViewById(R.id.object_button);
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList myDataset, Context context, View.OnClickListener onClickListener) {

        AvailableObjectDataList = myDataset;
        this.context = context;
        this.buttonClickListener = onClickListener;
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
        holder.name.setText(AvailableObjectDataList.get(position).getName());
        holder.username.setText(AvailableObjectDataList.get(position).getIssuer());
        holder.description.setText(AvailableObjectDataList.get(position).getDescription());
        holder.category.setText(AvailableObjectDataList.get(position).getCategory());
        holder.lon.setText(String.valueOf(AvailableObjectDataList.get(position).getLon()));
        holder.lat.setText(String.valueOf(AvailableObjectDataList.get(position).getLat()));
        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
         */
        holder.chatButton.setText("Manda un messaggio");
        holder.chatButton.setId(position);
        holder.chatButton.setOnClickListener(buttonClickListener);

        final boolean isExpanded = position == mExpandedPosition;
        holder.description.setVisibility(isExpanded ? View.VISIBLE:View.GONE);
        holder.description.setVisibility(isExpanded ? View.VISIBLE:View.GONE);
        holder.category.setVisibility(isExpanded ? View.VISIBLE:View.GONE);
        holder.lon.setVisibility(isExpanded ? View.VISIBLE:View.GONE);
        holder.lat.setVisibility(isExpanded ? View.VISIBLE:View.GONE);
        holder.chatButton.setVisibility(isExpanded ? View.VISIBLE:View.GONE);

        holder.itemView.setActivated(isExpanded);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1:position;
                notifyItemChanged(position);
            }
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return AvailableObjectDataList.size();

    }


    public void setClickListener(ItemClickListener itemClickListener){
        Log.v("tag", "tag");
        MyAdapter.itemClickListener = itemClickListener;
    }

}