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

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import fr.bmartel.smartcard.passwordwallet.R;
import fr.bmartel.smartcard.passwordwallet.application.PasswordApplication;
import fr.bmartel.smartcard.passwordwallet.inter.IBaseActivity;
import fr.bmartel.smartcard.passwordwallet.inter.ICompletionListener;

/**
 * Mode dialog.
 *
 * @author Bertrand Martel
 */
public class ModeDialog extends Dialog {

    public ModeDialog(final Activity rootActivity) {
        super(rootActivity);

        setContentView(R.layout.mode_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);

        final RadioButton simRadio = findViewById(R.id.radio_sim);
        final RadioButton appRadio = findViewById(R.id.radio_app);
        final ProgressBar transferProgress = findViewById(R.id.transferProgress);

        final IBaseActivity activity = (IBaseActivity) rootActivity;

        final PasswordApplication app = (PasswordApplication) rootActivity.getApplication();

        switch (app.mode) {
            case PasswordApplication.MODE_APP_STORAGE:
                appRadio.setChecked(true);
                break;
            case PasswordApplication.MODE_SIM_STORAGE:
                simRadio.setChecked(true);
                break;
            default:
                break;
        }

        setTitle(R.string.mode_title);

        final Button button = findViewById(R.id.confirm);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setEnabled(false);
                if ((appRadio.isChecked() && app.mode == PasswordApplication.MODE_APP_STORAGE) ||
                        (simRadio.isChecked() && app.mode == PasswordApplication.MODE_SIM_STORAGE)) {
                    cancel();
                }
                if (appRadio.isChecked() && app.mode != PasswordApplication.MODE_APP_STORAGE) {
                    activity.setMode(PasswordApplication.MODE_APP_STORAGE, transferProgress, new ICompletionListener() {
                        @Override
                        public void onComplete() {
                            cancel();
                        }
                    });
                } else if (simRadio.isChecked() && app.mode != PasswordApplication.MODE_SIM_STORAGE) {
                    activity.setMode(PasswordApplication.MODE_SIM_STORAGE, transferProgress, new ICompletionListener() {
                        @Override
                        public void onComplete() {
                            cancel();
                        }
                    });
                }
            }
        });
    }
}
