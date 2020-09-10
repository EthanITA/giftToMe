package com.gifttome.gifttome;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.MyViewHolder> {
    Integer mExpandedPosition = -1;

    private ArrayList<AvailableObjectsData> AvailableObjectDataList;
    private View.OnClickListener buttonClickListener;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public View view;
        public TextView name;
        public TextView username;
        public TextView description;
        public TextView category;
        public TextView lon;
        public TextView lat;
        public Button removeOrModifyButton;

        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            name = view.findViewById(R.id.object_name);
            username = view.findViewById(R.id.username);
            description = view.findViewById(R.id.description);
            category = view.findViewById(R.id.category);
            lon = view.findViewById(R.id.lon);
            lat = view.findViewById(R.id.lat);
            removeOrModifyButton = view.findViewById(R.id.object_button);
        }
    }

    public MyPostsAdapter(ArrayList<AvailableObjectsData> myDataset, View.OnClickListener onClickListener) {

        AvailableObjectDataList = myDataset;
        this.buttonClickListener = onClickListener;
    }

    @NotNull
    @Override
    public MyPostsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.available_object_lay, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.name.setText(AvailableObjectDataList.get(position).getName());
        holder.username.setText(AvailableObjectDataList.get(position).getIssuer());
        holder.description.setText(AvailableObjectDataList.get(position).getDescription());
        holder.category.setText(AvailableObjectDataList.get(position).getCategory());
        holder.lon.setText(String.valueOf(AvailableObjectDataList.get(position).getLon()));
        holder.lat.setText(String.valueOf(AvailableObjectDataList.get(position).getLat()));
        holder.removeOrModifyButton.setId(position);
        holder.removeOrModifyButton.setOnClickListener(buttonClickListener);

        final boolean isExpanded = position == mExpandedPosition;
        holder.description.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.description.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.category.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.lon.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.lat.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.removeOrModifyButton.setVisibility(isExpanded?View.VISIBLE:View.GONE);

        holder.itemView.setActivated(isExpanded);

        holder.removeOrModifyButton.setId(position);
        holder.itemView.setOnClickListener(v -> {
            mExpandedPosition = isExpanded ? -1:position;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return AvailableObjectDataList.size();
    }
}