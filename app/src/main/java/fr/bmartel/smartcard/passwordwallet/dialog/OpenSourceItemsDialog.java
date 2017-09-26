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
package fr.bmartel.smartcard.passwordwallet.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ListView;

import fr.bmartel.smartcard.passwordwallet.R;
import fr.bmartel.smartcard.passwordwallet.adapter.OpenSourceItemsAdapter;

/**
 * open source components dialog.
 *
 * @author Bertrand Martel
 */
public class OpenSourceItemsDialog extends AlertDialog {

    public OpenSourceItemsDialog(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        ListView listview = (ListView) inflater.inflate(R.layout.open_source_list, null);
        listview.setAdapter(new OpenSourceItemsAdapter(context));

        setView(listview);
        setTitle(R.string.open_source_items);
        setButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.dialog_ok),
                (OnClickListener) null);
    }
}