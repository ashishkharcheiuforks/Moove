package com.backdoor.moove.core.adapters;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.data.ReminderDataProvider;
import com.backdoor.moove.core.data.ReminderModel;
import com.backdoor.moove.core.helper.ColorSetter;
import com.backdoor.moove.core.helper.Contacts;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.interfaces.RecyclerListener;
import com.backdoor.moove.core.utils.ReminderUtils;
import com.backdoor.moove.core.utils.TimeUtil;
import com.backdoor.moove.core.utils.ViewUtils;

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RemindersRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private ColorSetter cs;
    private ReminderDataProvider provider;
    private RecyclerListener mEventListener;
    private boolean is24;

    public RemindersRecyclerAdapter(Context context, ReminderDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        SharedPrefs prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        public TextView taskTitle, taskDate, reminder_type, reminder_phone,
                reminder_contact_name, listHeader;
        public SwitchCompat check;
        public ImageView taskIcon;
        public CardView itemCard;

        public RelativeLayout reminderContainer;

        public ViewHolder(View v) {
            super(v);
            reminderContainer = (RelativeLayout) v.findViewById(R.id.reminderContainer);
            listHeader = (TextView) v.findViewById(R.id.listHeader);
            check = (SwitchCompat) v.findViewById(R.id.itemCheck);
            check.setVisibility(View.VISIBLE);
            taskIcon = (ImageView) v.findViewById(R.id.taskIcon);
            taskDate = (TextView) v.findViewById(R.id.taskDate);
            taskDate.setText("");
            reminder_type = (TextView) v.findViewById(R.id.reminder_type);
            reminder_type.setText("");
            reminder_phone = (TextView) v.findViewById(R.id.reminder_phone);
            reminder_phone.setText("");
            reminder_contact_name = (TextView) v.findViewById(R.id.reminder_contact_name);
            reminder_contact_name.setText("");

            taskTitle = (TextView) v.findViewById(R.id.taskText);
            taskTitle.setText("");
            itemCard = (CardView) v.findViewById(R.id.itemCard);
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
        ViewHolder vh = new ViewHolder(itemLayoutView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ReminderModel item = provider.getItem(position);
        String title = item.getTitle();
        String type = item.getType();
        String number = item.getNumber();
        long due = item.getStartTime();
        double lat = item.getPlace()[0];
        double lon = item.getPlace()[1];
        int isDone = item.getStatusDb();

        String simpleDate = TimeUtil.getSimpleDate(due);

        /*if (archived == 1){
            if (position > 0 && simpleDate.equals(TimeUtil.getSimpleDate(provider.getItem(position - 1).getDue()))) {
                holder.listHeader.setVisibility(View.GONE);
            } else {
                if (due == 0){
                    simpleDate = mContext.getString(R.string.permanent_reminders);
                } else {
                    if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis()))) {
                        simpleDate = mContext.getString(R.string._today);
                    } else if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY))) {
                        simpleDate = mContext.getString(R.string._tomorrow);
                    }
                }
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            }
        } else {
            if (isDone == 1 && position > 0 && provider.getItem(position - 1).getCompleted() == 0){
                simpleDate = mContext.getString(R.string.simple_disabled);
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            } else if (isDone == 1 && position > 0 && provider.getItem(position - 1).getCompleted() == 1){
                holder.listHeader.setVisibility(View.GONE);
            } else if (isDone == 1 && position == 0){
                simpleDate = mContext.getString(R.string.simple_disabled);
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            } else if (isDone == 0 && position > 0 && simpleDate.equals(TimeUtil.getSimpleDate(provider.getItem(position - 1).getDue()))){
                holder.listHeader.setVisibility(View.GONE);
            } else {
                if (due <= 0 || due < (System.currentTimeMillis() - AlarmManager.INTERVAL_DAY)){
                    simpleDate = mContext.getString(R.string.permanent_reminders);
                } else {
                    if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis()))) {
                        simpleDate = mContext.getString(R.string._today);
                    } else if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY))) {
                        simpleDate = mContext.getString(R.string._tomorrow);
                    }
                }
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            }
        }*/

        holder.reminderContainer.setVisibility(View.VISIBLE);

        holder.taskTitle.setText("");
        holder.reminder_contact_name.setText("");
        holder.taskDate.setText("");
        holder.reminder_type.setText("");
        holder.reminder_phone.setText("");

        //holder.taskIcon.setImageDrawable();
        holder.taskTitle.setText(title);
        holder.reminder_type.setText(ReminderUtils.getTypeString(mContext, type));

        if (type.matches(Constants.TYPE_LOCATION_CALL) ||
                type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
            holder.reminder_phone.setText(number);
            String name = Contacts.getContactNameFromNumber(number, mContext);
            if (name != null) {
                holder.reminder_contact_name.setText(name);
            } else {
                holder.reminder_contact_name.setText("");
            }
        } else if (type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
            holder.reminder_phone.setText(number);
            String name = Contacts.getContactNameFromNumber(number, mContext);
            if (name != null) {
                holder.reminder_contact_name.setText(name);
            } else {
                holder.reminder_contact_name.setText("");
            }
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
        return provider.getItem(position).getId();
    }

    @Override
    public int getItemCount() {
        return provider.getCount();
    }

    public RecyclerListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(RecyclerListener eventListener) {
        mEventListener = eventListener;
    }
}