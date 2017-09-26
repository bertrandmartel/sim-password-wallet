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
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.github.orangegangsters.lollipin.lib.interfaces.ILockCallback;
import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;

import fr.bmartel.smartcard.passwordwallet.application.PasswordApplication;
import fr.bmartel.smartcard.passwordwallet.inter.IDialog;
import fr.bmartel.smartcard.passwordwallet.uicc.ApduResponse;
import fr.bmartel.smartcard.passwordwallet.uicc.PinCodeResult;
import fr.bmartel.smartcard.passwordwallet.uicc.UiccUtils;
import fr.bmartel.smartcard.passwordwallet.utils.MenuUtils;

/**
 * Pin Code Activity.
 *
 * @author Bertrand Martel
 */
public class CustomPinActivity extends AppLockActivity implements IDialog {

    protected Toolbar toolbar = null;

    protected DrawerLayout mDrawer = null;

    /**
     * toggle on the hamburger button
     */
    protected ActionBarDrawerToggle drawerToggle;

    /**
     * navigation view
     */
    protected NavigationView nvDrawer;

    /**
     * one dialog to show above the activity. We dont want to have multiple Dialog above each other.
     */
    private Dialog mDialog;

    private final static int PIN_CODE_LENGTH = 4;

    private PasswordApplication mApplication;

    private String mOldPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolbar_item);
        setSupportActionBar(toolbar);

        setToolbarTitle();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.inflateMenu(R.menu.toolbar_menu);

        // Find our drawer view
        mDrawer = findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();
        mDrawer.setDrawerListener(drawerToggle);
        nvDrawer = findViewById(R.id.nvView);

        mApplication = (PasswordApplication) getApplication();

        // Setup drawer view
        setupDrawerContent(nvDrawer);

        final LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.getAppLock().setCallback(new ILockCallback() {
            @Override
            public boolean onCheckPasscode(String inputPasscode) {
                byte[] data = UiccUtils.convertPinCode(inputPasscode);
                if (data != null) {
                    PinCodeResult res = mApplication.getUicc().checkPin(UiccUtils.convertPinCode(inputPasscode));
                    if (res.isValid()) {
                        if (mApplication.isCardSecured() && (getType() == AppLock.CHANGE_PIN)) {
                            //store old pin only for change
                            mOldPin = inputPasscode;
                        }
                        return true;
                    } else {
                        displayRetry(res.getRetry());
                    }
                }
                return false;
            }

            @Override
            public boolean onSetPasscode(String passcode) {
                byte[] data = UiccUtils.convertPinCode(passcode);
                if (data != null) {
                    if (!mApplication.isCardSecured()) {
                        ApduResponse res = mApplication.getUicc().updatePin(true, null, UiccUtils.convertPinCode(passcode));
                        if (res.isSuccessful()) {
                            PinCodeResult pinCodeRes = mApplication.getUicc().checkPin(UiccUtils.convertPinCode(passcode));
                            if (pinCodeRes.isValid()) {
                                return true;
                            } else {
                                displayRetry(pinCodeRes.getRetry());
                                return false;
                            }
                        }
                    } else {
                        if (!mOldPin.equals("")) {
                            ApduResponse res = mApplication.getUicc().updatePin(false, UiccUtils.convertPinCode(mOldPin), UiccUtils.convertPinCode(passcode));
                            //clear the old pin
                            mOldPin = "";
                            if (res.isSuccessful()) {
                                PinCodeResult pinCodeRes = mApplication.getUicc().checkPin(UiccUtils.convertPinCode(passcode));
                                if (pinCodeRes.isValid()) {
                                    return true;
                                } else {
                                    displayRetry(pinCodeRes.getRetry());
                                    return false;
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * Display retry number if wrong pincode was detected.
     *
     * @param retry retry number
     */
    private void displayRetry(int retry) {
        if (retry != 0) {
            Toast.makeText(CustomPinActivity.this, "wrong pin code : " + retry + " retry left",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(CustomPinActivity.this, "card is locked", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getContentView() {
        return R.layout.keyguard_fragment;
    }

    @Override
    public void setCurrentDialog(Dialog dialog) {
        mDialog = dialog;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (this.mDrawer.isDrawerOpen(GravityCompat.START)) {
            this.mDrawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    protected void setToolbarTitle() {
        getSupportActionBar().setTitle(getResources().getString(R.string.app_title));
    }

    /**
     * setup navigation view.
     *
     * @param navigationView
     */
    private void setupDrawerContent(final NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        MenuUtils.selectDrawerItem(CustomPinActivity.this, mApplication, menuItem, mDrawer, CustomPinActivity.this);
                        return false;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        hideMenuButton();
        return super.onCreateOptionsMenu(menu);
    }

    public void hideMenuButton() {
        toolbar.getMenu().findItem(R.id.button_save).setVisible(false);
        toolbar.getMenu().findItem(R.id.button_mode).setVisible(false);
        toolbar.getMenu().findItem(R.id.button_delete).setVisible(false);
        toolbar.getMenu().findItem(R.id.button_add_password).setVisible(false);
    }

    /**
     * setup action drawer.
     *
     * @return
     */
    protected ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        };
    }

    @Override
    public void showForgotDialog() {
    }

    @Override
    public void onPinFailure(int attempts) {
    }

    @Override
    public void onPinSuccess(int attempts) {
    }

    @Override
    public int getPinLength() {
        return PIN_CODE_LENGTH;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
}
