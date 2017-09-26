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

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.List;

import fr.bmartel.smartcard.passwordwallet.adapter.PasswordAdapter;
import fr.bmartel.smartcard.passwordwallet.model.Password;

/**
 * Common fragment.
 *
 * @author Bertrand Martel
 */
public abstract class ListFragmentAbstr extends MainFragmentAbstr {

    protected RecyclerView mPasswordListView;

    protected PasswordAdapter mPasswordAdapter;

    protected List<Password> mPasswordList;

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected FrameLayout mEmptyFrame;

    protected RelativeLayout mDisplayFrame;

    protected void setTitle(String title) {
        getRootActivity().setToolbarTitle(title);
    }
}