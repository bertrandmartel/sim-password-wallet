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
package fr.bmartel.smartcard.passwordwallet.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.Comparator;

import fr.bmartel.smartcard.passwordwallet.BaseActivity;
import fr.bmartel.smartcard.passwordwallet.R;
import fr.bmartel.smartcard.passwordwallet.adapter.PasswordAdapter;
import fr.bmartel.smartcard.passwordwallet.common.SimpleDividerItemDecoration;
import fr.bmartel.smartcard.passwordwallet.dialog.ModeDialog;
import fr.bmartel.smartcard.passwordwallet.inter.IDeletionListener;
import fr.bmartel.smartcard.passwordwallet.inter.IFragmentOptions;
import fr.bmartel.smartcard.passwordwallet.inter.IViewHolderClickListener;
import fr.bmartel.smartcard.passwordwallet.model.Password;

/**
 * Password Fragment.
 *
 * @author Bertrand Martel
 */
public class PasswordFragment extends ListFragmentAbstr implements IFragmentOptions {

    private PasswordItemFragment mFragment;

    private FragmentActivity mActivity;

    public PasswordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.password_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDeletionListener();

        mEmptyFrame = view.findViewById(R.id.waiting_frame);
        mDisplayFrame = view.findViewById(R.id.display_frame);

        if (getRootActivity().getPasswordList().size() > 0) {
            mEmptyFrame.setVisibility(View.GONE);
            mDisplayFrame.setVisibility(View.VISIBLE);
        }

        mPasswordListView = view.findViewById(R.id.password_list);

        mPasswordList = ((BaseActivity) getActivity()).getPasswordList();

        //sort by topic
        if (mPasswordList.size() > 0) {
            Collections.sort(mPasswordList, new Comparator<Password>() {
                @Override
                public int compare(final Password object1, final Password object2) {
                    return object1.getTitle().compareTo(object2.getTitle());
                }
            });
        }

        mPasswordAdapter = new PasswordAdapter(getRootActivity(), mPasswordList, getActivity(), new IViewHolderClickListener() {
            @Override
            public void onClick(View view) {
                final int index = mPasswordListView.getChildAdapterPosition(view);
                if (!mPasswordAdapter.isSelected(index)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mFragment = new PasswordItemFragment();
                            Bundle args = new Bundle();
                            args.putInt("index", index);
                            mFragment.setArguments(args);

                            final FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.fragment_frame, mFragment, "PaswordItem");
                            ft.addToBackStack(null);
                            ft.commit();
                        }
                    }, 200);
                } else {
                    mPasswordAdapter.unselect();
                }
            }
        });

        //set layout manager
        mPasswordListView.setLayoutManager(new GridLayoutManager(getActivity(), 1, LinearLayoutManager.VERTICAL, false));

        //set line decoration
        mPasswordListView.addItemDecoration(new SimpleDividerItemDecoration(
                getActivity().getApplicationContext()
        ));

        mPasswordListView.setAdapter(mPasswordAdapter);

        //setup swipe refresh
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPasswordList = ((BaseActivity) getActivity()).getPasswordList();
                mPasswordAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        onUpdateToolbar();
    }

    @Override
    public void onUpdateToolbar() {
        MenuItem buttonCreate = getRootActivity().getToolbar().getMenu().findItem(R.id.button_add_password);
        buttonCreate.setVisible(true);
        buttonCreate.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                mFragment = new PasswordItemFragment();
                ft.replace(R.id.fragment_frame, mFragment, "PasswordItem");
                ft.addToBackStack(null);
                ft.commit();
                return false;
            }
        });

        MenuItem buttonMode = getRootActivity().getToolbar().getMenu().findItem(R.id.button_mode);
        buttonMode.setVisible(true);
        buttonMode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ModeDialog dialog = new ModeDialog(getActivity());
                getRootActivity().setCurrentDialog(dialog);
                dialog.show();
                return false;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setDeletionListener();
        setTitle(getString(R.string.title_password));

        getRootActivity().hideMenuButton();
        getRootActivity().getToolbar().getMenu().findItem(R.id.button_mode).setVisible(true);
        getRootActivity().getToolbar().getMenu().findItem(R.id.button_add_password).setVisible(true);

        mPasswordList = ((BaseActivity) getActivity()).getPasswordList();
        mPasswordAdapter.notifyDataSetChanged();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setDeletionListener() {

        getRootActivity().setDeletionListener(new IDeletionListener() {
            @Override
            public void onDelete() {
                getRootActivity().deletePassword(mPasswordList.get(mPasswordAdapter.getSelectedItem()).getTitle());
                mPasswordList = ((BaseActivity) getActivity()).getPasswordList();
                mPasswordAdapter.notifyDataSetChanged();

                if (getRootActivity().getPasswordList().size() > 0) {
                    mEmptyFrame.setVisibility(View.GONE);
                    mDisplayFrame.setVisibility(View.VISIBLE);
                } else {
                    mEmptyFrame.setVisibility(View.VISIBLE);
                    mDisplayFrame.setVisibility(View.GONE);
                }
                getRootActivity().getToolbar().getMenu().findItem(R.id.button_delete).setVisible(false);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (mFragment != null) {
            mFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
