package com.twinc.halmato.autowhatsappmessage;

import android.app.Activity;
import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.R.id.message;

/**
 * Created by Tiaan on 12/6/2016.
 */

public class PresetMessagesAdapter extends RecyclerView.Adapter<PresetMessagesAdapter.MyViewHolder> {

    private List<PresetMessage> presetMessagesList;
    private MainActivity mainActivity;

    // Constructor
    public PresetMessagesAdapter(List<PresetMessage> presetMessagesList, MainActivity mainActivity)  {

        this.presetMessagesList = presetMessagesList;
        this.mainActivity = mainActivity;
    }

    // Changing colour of selected views
    private SparseBooleanArray selectedItems;
    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.preset_messages_view, parent, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                mainActivity.sendMessage(presetMessagesList.get(parent.indexOfChild(v)).message);
            }
         });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(mainActivity.actionMode != null)    {
                    return false;
                }

                int index = parent.indexOfChild(v);

                mainActivity.actionMode = mainActivity.startSupportActionMode(mainActivity.mActionModeCallback);
                mainActivity.actionMode.setTag(index);
                v.setSelected(true);

                return true;
            }
        });

        return new MyViewHolder(itemView);
    }

    // You get passed a ViewHolder and the position of the next view that needs to be created.
    // Then you must create this ViewHolder (that contains all the views that you have defined, how your view in the list should look)
    // using the List<> of data.
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        PresetMessage presetMessage = presetMessagesList.get(position);

        holder.presetmessage.setText(presetMessage.message);
        holder.lastUsed.setText(presetMessage.lastUsed);
    }

    @Override
    public int getItemCount() {
        return presetMessagesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView presetmessage, lastUsed;

        public MyViewHolder(View view)  {

            super(view);
            presetmessage = (TextView) view.findViewById(R.id.tv_preset_message);
            lastUsed = (TextView) view.findViewById(R.id.tv_last_used);

        }
    }




}
