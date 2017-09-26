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
package fr.bmartel.smartcard.passwordwallet;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.github.orangegangsters.lollipin.lib.managers.AppLock;

import fr.bmartel.smartcard.passwordwallet.application.PasswordApplication;
import fr.bmartel.smartcard.passwordwallet.fragment.PasswordFragment;
import fr.bmartel.smartcard.passwordwallet.inter.IServiceConnection;

/**
 * Main activity.
 *
 * @author Bertrand Martel
 */
public class MainActivity extends BaseActivity {

    /**
     * one dialog to show above the activity. We dont want to have multiple Dialog above each other.
     */
    private Dialog mDialog;

    /**
     * code received when the activity is unlocked.
     */
    public static final int REQUEST_UNLOCK_PIN = 11;

    private PasswordApplication mApplication;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setLayout(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        mApplication = (PasswordApplication) getApplication();

        if (mApplication.isConnected()) {
            unlock();
        } else {
            mApplication.setServiceConnection(new IServiceConnection() {
                @Override
                public void onServiceConnected() {
                    unlock();
                }
            });
        }

    }

    /**
     * unlock activity.
     */
    private void unlock() {
        if (!mApplication.isPinCodeChecked()) {
            Intent intent = new Intent(MainActivity.this, CustomPinActivity.class);
            if (mApplication.isCardSecured()) {
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);
                startActivityForResult(intent, REQUEST_UNLOCK_PIN);
            } else {
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                startActivityForResult(intent, REQUEST_UNLOCK_PIN);
            }
        } else {
            onCorrectPinCode();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_UNLOCK_PIN:
                if (mApplication.isPinCodeChecked()) {
                    onCorrectPinCode();
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onReady() {
        mFragment = new PasswordFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, mFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }


    @Override
    public void setCurrentDialog(Dialog dialog) {
        mDialog = dialog;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}