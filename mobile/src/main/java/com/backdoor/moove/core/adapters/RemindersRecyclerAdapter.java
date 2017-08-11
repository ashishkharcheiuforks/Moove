package com.backdoor.moove.core.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.data.ReminderModel;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.Contacts;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.interfaces.RecyclerListener;
import com.backdoor.moove.core.utils.ReminderUtils;
import com.backdoor.moove.core.utils.TimeUtil;

import java.util.ArrayList;

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RemindersRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private Coloring cs;
    private ArrayList<ReminderModel> provider;
    private RecyclerListener mEventListener;
    private boolean is24;

    public RemindersRecyclerAdapter(Context context, ArrayList<ReminderModel> provider) {
        this.mContext = context;
        this.provider = provider;
        cs = new Coloring(context);
        is24 = SharedPrefs.getInstance(context).loadBoolean(Prefs.IS_24_TIME_FORMAT);
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        public TextView taskTitle, taskDate, reminder_type, reminder_phone,
                reminder_contact_name, listHeader;
        public ToggleButton check;
        public CardView itemCard;

        public RelativeLayout reminderContainer;
        public LinearLayout contactGroup;

        public ViewHolder(View v) {
            super(v);
            reminderContainer = v.findViewById(R.id.reminderContainer);
            contactGroup = v.findViewById(R.id.contactGroup);
            listHeader = v.findViewById(R.id.listHeader);
            check = v.findViewById(R.id.itemCheck);
            taskDate = v.findViewById(R.id.taskDate);
            taskDate.setText("");
            reminder_type = v.findViewById(R.id.reminder_type);
            reminder_type.setText("");
            reminder_phone = v.findViewById(R.id.reminder_phone);
            reminder_phone.setText("");
            reminder_contact_name = v.findViewById(R.id.reminder_contact_name);
            reminder_contact_name.setText("");

            taskTitle = v.findViewById(R.id.taskText);
            taskTitle.setText("");
            itemCard = v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEventListener != null) {
                        mEventListener.onItemSwitched(getAdapterPosition(), check);
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            View view = itemCard;
            if (mEventListener != null) {
                mEventListener.onItemClicked(getAdapterPosition(), view);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), itemCard);
            }
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card, parent, false);

        // create ViewHolder
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ReminderModel item = provider.get(position);
        String title = item.getTitle();
        String type = item.getType();
        String number = item.getNumber();
        long due = item.getStartTime();
        double lat = item.getPlace()[0];
        double lon = item.getPlace()[1];
        int isDone = item.getStatusDb();

        holder.reminderContainer.setVisibility(View.VISIBLE);

        holder.taskTitle.setText("");
        holder.reminder_contact_name.setText("");
        holder.taskDate.setText("");
        holder.reminder_type.setText("");
        holder.reminder_phone.setText("");

        holder.taskTitle.setText(title);
        holder.reminder_type.setText(ReminderUtils.getTypeString(mContext, type));

        if (type.contains(Constants.TYPE_CALL) ||
                type.contains(Constants.TYPE_MESSAGE)) {
            holder.reminder_phone.setText(number);
            String name = Contacts.getContactNameFromNumber(number, mContext);
            if (name != null) {
                holder.reminder_contact_name.setText(name);
            } else {
                holder.reminder_contact_name.setText("");
            }
            holder.contactGroup.setVisibility(View.VISIBLE);
        } else {
            holder.contactGroup.setVisibility(View.GONE);
        }

        if (lat != 0.0 || lon != 0.0) {
            holder.taskDate.setText(String.format("%.5f", lat) + "\n" + String.format("%.5f", lon));
        } else {
            holder.taskDate.setText(TimeUtil.getFullDateTime(due, is24));
        }

        if (isDone == Constants.DISABLE) {
            holder.check.setChecked(false);
        } else {
            holder.check.setChecked(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return provider.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return provider.size();
    }

    public RecyclerListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(RecyclerListener eventListener) {
        mEventListener = eventListener;
    }
}