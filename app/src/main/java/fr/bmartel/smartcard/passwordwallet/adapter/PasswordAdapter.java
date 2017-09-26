/*********************************************************************************
 * This file is part of SIM Password Wallet                                      *
 * <p/>                                                                          *
 * Copyright (C) 2017  Bertrand Martel                                           *
 * <p/>                                                                          *
 * SIM Password Wallet is free software: you can redistribute it and/or modify   *
 * it under the terms of the GNU General Public License as published by          *
 * the Free Software Foundation, either version 3 of the License, or             *
 * (at your option) any later version.                                           *
 * <p/>                                                                          *
 * SIM Password Wallet is distributed in the hope that it will be useful,        *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                 *
 * GNU General Public License for more details.                                  *
 * <p/>                                                                          *
 * You should have received a copy of the GNU General Public License             *
 * along with SIM Password Wallet.  If not, see <http://www.gnu.org/licenses/>.  *
 */
package fr.bmartel.smartcard.passwordwallet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.smartcard.passwordwallet.R;
import fr.bmartel.smartcard.passwordwallet.inter.IBaseActivity;
import fr.bmartel.smartcard.passwordwallet.inter.IViewHolderClickListener;
import fr.bmartel.smartcard.passwordwallet.model.Password;

/**
 * Password Adapter
 *
 * @author Bertrand Martel
 */
public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.ViewHolder> {

    List<Password> passwordList = new ArrayList<>();

    /**
     * Android context
     */
    private Context context = null;

    /**
     * click listener
     */
    private IViewHolderClickListener mListener;

    private int selected_position = -1;

    private IBaseActivity mActivity;

    public PasswordAdapter(IBaseActivity activity, List<Password> list, Context context, IViewHolderClickListener listener) {
        this.passwordList = list;
        this.context = context;
        this.mActivity = activity;
        this.mListener = listener;
    }

    @Override
    public PasswordAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.password_item, parent, false);
        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Password item = passwordList.get(position);
        holder.passwordTitle.setText("" + item.getTitle());

        if (selected_position == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#e1e1e1"));
            mActivity.getToolbar().getMenu().findItem(R.id.button_delete).setVisible(true);
        } else {
            mActivity.getToolbar().getMenu().findItem(R.id.button_delete).setVisible(false);
            holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.ripple));
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                notifyItemChanged(selected_position);
                selected_position = position;
                notifyItemChanged(selected_position);
                return true;
            }
        });
    }

    public boolean isSelected(int position) {
        return (selected_position == position);
    }

    public void unselect() {
        selected_position = -1;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return passwordList.size();
    }

    public int getSelectedItem() {
        return selected_position;
    }

    /**
     * ViewHolder for Password item
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        /**
         * layout
         */
        public LinearLayout layout;

        /**
         * password title
         */
        public TextView passwordTitle;

        /**
         * click listener
         */
        public IViewHolderClickListener mListener;

        /**
         * ViewHolder for Contact item
         *
         * @param v
         * @param listener
         */
        public ViewHolder(View v, IViewHolderClickListener listener) {
            super(v);
            mListener = listener;
            passwordTitle = v.findViewById(R.id.password_title);
            layout = v.findViewById(R.id.group_layout);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
    }

}