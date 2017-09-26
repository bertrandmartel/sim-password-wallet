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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.orangegangsters.lollipin.lib.PinCompatActivity;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.smartcard.passwordwallet.application.PasswordApplication;
import fr.bmartel.smartcard.passwordwallet.db.PasswordReaderDbHelper;
import fr.bmartel.smartcard.passwordwallet.inter.IBaseActivity;
import fr.bmartel.smartcard.passwordwallet.inter.ICompletionListener;
import fr.bmartel.smartcard.passwordwallet.inter.IDeletionListener;
import fr.bmartel.smartcard.passwordwallet.inter.IFragmentOptions;
import fr.bmartel.smartcard.passwordwallet.model.Password;
import fr.bmartel.smartcard.passwordwallet.uicc.ApduResponse;
import fr.bmartel.smartcard.passwordwallet.uicc.UiccUtils;
import fr.bmartel.smartcard.passwordwallet.utils.MenuUtils;

/**
 * Shared activity.
 *
 * @author Bertrand Martel
 */
public abstract class BaseActivity extends PinCompatActivity implements IBaseActivity {

    private final static String TAG = BaseActivity.class.getSimpleName();

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
     * activity layout ressource id
     */
    private int layoutId;

    /**
     * list of password retrieved from database or from UICC.
     */
    private List<Password> mPasswordList;

    /**
     * the current fragment.
     */
    protected Fragment mFragment;

    /**
     * set activity ressource id
     *
     * @param resId
     */
    protected void setLayout(int resId) {
        layoutId = resId;
    }

    protected SharedPreferences mSharedPref;

    protected IDeletionListener mDeletionListener;

    private PasswordReaderDbHelper mDbHelper;

    private PasswordApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layoutId);

        mApplication = (PasswordApplication) getApplication();

        mDbHelper = new PasswordReaderDbHelper(getApplicationContext());

        mSharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName(), 0);

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

        // Setup drawer view
        setupDrawerContent(nvDrawer);
    }

    /**
     * called on valid pin code.
     */
    protected void onCorrectPinCode() {
        nvDrawer.getMenu().findItem(R.id.close_session).setVisible(true);
        nvDrawer.getMenu().findItem(R.id.change_pincode).setVisible(true);
        updateMode();
        initModel();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onReady();
            }
        });
    }

    /**
     * update the mode icon.
     */
    protected void updateMode() {
        mApplication.refreshMode();
        switch (mApplication.mode) {
            case PasswordApplication.MODE_APP_STORAGE:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toolbar.getMenu().findItem(R.id.button_mode).setIcon(R.drawable.ic_memory);
                    }
                });
                break;
            case PasswordApplication.MODE_SIM_STORAGE:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toolbar.getMenu().findItem(R.id.button_mode).setIcon(R.drawable.ic_sim_card);
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * initialize model.
     */
    private void initModel() {
        switch (mApplication.mode) {
            case PasswordApplication.MODE_APP_STORAGE:
                SQLiteDatabase db = mDbHelper.getReadableDatabase();

                String sortOrder = PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_TITLE + " DESC";
                Cursor cursor = db.query(PasswordReaderDbHelper.PasswordEntry.TABLE_NAME, null, null, null, null, null, sortOrder);

                mPasswordList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    mPasswordList.add(new Password(cursor.getString(cursor.getColumnIndexOrThrow(PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_USERNAME)),
                            cursor.getBlob(cursor.getColumnIndexOrThrow(PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_PASSWORD))));
                }
                cursor.close();
                break;
            case PasswordApplication.MODE_SIM_STORAGE:
                List<Password> passwordList = mApplication.getUicc().getPasswordList();
                if (passwordList != null) {
                    mPasswordList = passwordList;
                } else {
                    Toast.makeText(this, "failed to retrieve password on UICC", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Set toolbar title in initialization
     */
    protected void setToolbarTitle() {
        getSupportActionBar().setTitle(getResources().getString(R.string.app_title));
    }

    /**
     * setup navigation view
     *
     * @param navigationView
     */
    private void setupDrawerContent(final NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        MenuUtils.selectDrawerItem(BaseActivity.this, mApplication, menuItem, mDrawer, BaseActivity.this);
                        return false;
                    }
                });
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
            super.onBackPressed();
        }
    }

    /**
     * Save password entry validated by user.
     *
     * @param title    password title
     * @param username username
     * @param password password value
     * @return APDU data result
     */
    @Override
    public byte[] saveNewPassword(String title, String username, String password) {

        switch (mApplication.mode) {
            case PasswordApplication.MODE_APP_STORAGE:
                ApduResponse result = mApplication.getUicc().encrypt(password.getBytes());

                if (result.isSuccessful()) {
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put(PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_TITLE, title);
                    values.put(PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_USERNAME, username);
                    values.put(PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_PASSWORD, result.getData());

                    db.insert(PasswordReaderDbHelper.PasswordEntry.TABLE_NAME, null, values);

                    Password passwordObj = new Password(title, username, result.getData());
                    mPasswordList.add(passwordObj);

                    return result.getData();
                }
                break;
            case PasswordApplication.MODE_SIM_STORAGE:
                result = mApplication.getUicc().addPassword(title, username, password);
                if (result == null) {
                    Log.e(TAG, "write operation failed");
                } else {
                    Password passwordObj = new Password(title, null, null);
                    mPasswordList.add(passwordObj);
                    return result.getData();
                }
                break;
        }
        return null;
    }

    /**
     * Update a password entry.
     *
     * @param formerTitle last password title
     * @param newTitle    new password title
     * @param username    updated username
     * @param password    updated password value
     * @return APDU data result
     */
    @Override
    public byte[] saveExistingPassword(String formerTitle, String newTitle, String username, String password) {
        switch (mApplication.mode) {
            case PasswordApplication.MODE_APP_STORAGE:
                ApduResponse result = mApplication.getUicc().encrypt(password.getBytes());

                if (result.isSuccessful()) {
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put(PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_TITLE, newTitle);
                    values.put(PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_USERNAME, username);
                    values.put(PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_PASSWORD, result.getData());

                    String[] whereArgs = new String[]{formerTitle};

                    db.update(PasswordReaderDbHelper.PasswordEntry.TABLE_NAME,
                            values,
                            PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_TITLE + "=?", whereArgs);

                    return result.getData();
                }
                break;
            case PasswordApplication.MODE_SIM_STORAGE:
                result = mApplication.getUicc().editPassword(formerTitle, newTitle, username, password);
                if (result == null) {
                    Log.e(TAG, "write operation failed");
                } else {
                    for (int i = 0; i < mPasswordList.size(); i++) {
                        if (mPasswordList.get(i).getTitle().equals(formerTitle)) {
                            mPasswordList.set(i, new Password(newTitle, null, null));
                            break;
                        }
                    }
                    return result.getData();
                }
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * decrypt password on UICC.
     *
     * @param password encrypted password
     * @return password decrypted
     */
    @Override
    public String decrypt(byte[] password) {
        ApduResponse data = mApplication.getUicc().decrypt(password);
        if (data.isSuccessful()) {
            return new String(data.getData());
        }
        return null;
    }

    /**
     * Update the mode (storage on application or on UICC).
     *
     * @param mode     mode
     * @param progress progressbar object
     * @param listener completion listener
     */
    @Override
    public void setMode(final byte mode, final ProgressBar progress, final ICompletionListener listener) {
        mApplication.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "set mode " + mode);
                ApduResponse res = mApplication.getUicc().setMode(mode);
                if (!res.isSuccessful()) {
                    Log.e(TAG, "set mode failed");
                } else {
                    mApplication.setMode(mode);

                    final List<Password> tempList = new ArrayList<>();
                    //transition between modes
                    switch (mApplication.mode) {
                        case PasswordApplication.MODE_APP_STORAGE:
                            for (Password password : mPasswordList) {
                                tempList.add(new Password(password.getTitle(), password.getUsername(), password.getPassword()));
                            }
                            mPasswordList.clear();
                            for (int i = 0; i < tempList.size(); i++) {

                                ApduResponse result = mApplication.getUicc().getPassword(tempList.get(i).getTitle());

                                if (result.isSuccessful()) {
                                    Password realPassword = UiccUtils.parsePassword(tempList.get(i).getTitle(), result.getData());
                                    //add password in database
                                    saveNewPassword(realPassword.getTitle(), realPassword.getUsername(), new String(realPassword.getPassword()));
                                    // delete on UICC
                                    mApplication.getUicc().deletePassword(realPassword.getTitle());
                                    final int finalI = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress.setProgress((finalI * 100) / tempList.size());
                                        }
                                    });
                                }
                            }
                            progress.setProgress(100);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toolbar.getMenu().findItem(R.id.button_mode).setIcon(R.drawable.ic_memory);
                                }
                            });
                            break;
                        case PasswordApplication.MODE_SIM_STORAGE:
                            for (Password password : mPasswordList) {
                                tempList.add(new Password(password.getTitle(), password.getUsername(), password.getPassword()));
                            }
                            mPasswordList.clear();
                            for (int i = 0; i < tempList.size(); i++) {
                                //add password in UICC
                                saveNewPassword(tempList.get(i).getTitle(), tempList.get(i).getUsername(), decrypt(tempList.get(i).getPassword()));
                                final int finalI = i;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.setProgress((finalI * 100) / tempList.size());
                                    }
                                });
                            }
                            // drop all data from database
                            dropAll();
                            progress.setProgress(100);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toolbar.getMenu().findItem(R.id.button_mode).setIcon(R.drawable.ic_sim_card);
                                }
                            });
                            break;
                        default:
                            break;
                    }
                    initModel();
                }
                listener.onComplete();
            }
        });
    }

    /**
     * Get the password entry from local storage or from UICC.
     *
     * @param index password index in password list
     * @return password entry or null
     */
    @Override
    public Password getPassword(int index) {
        switch (mApplication.mode) {
            case PasswordApplication.MODE_APP_STORAGE:
                return mPasswordList.get(index);
            case PasswordApplication.MODE_SIM_STORAGE:
                Password password = mPasswordList.get(index);

                ApduResponse result = mApplication.getUicc().getPassword(password.getTitle());

                if (result.isSuccessful()) {
                    return UiccUtils.parsePassword(password.getTitle(), result.getData());
                } else {
                    Toast.makeText(this, "failed to retrieve password on UICC", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return null;
    }

    /**
     * remove all passwords from local db.
     */
    private void dropAll() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("delete from " + PasswordReaderDbHelper.PasswordEntry.TABLE_NAME);
    }

    /**
     * Delete a password entry.
     *
     * @param title password title
     */
    @Override
    public void deletePassword(String title) {
        for (Password password : mPasswordList) {
            if (password.getTitle().equals(title)) {

                switch (mApplication.mode) {
                    case PasswordApplication.MODE_APP_STORAGE:
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        String whereClause = PasswordReaderDbHelper.PasswordEntry.COLUMN_NAME_TITLE + "=?";
                        String[] whereArgs = new String[]{password.getTitle()};
                        db.delete(PasswordReaderDbHelper.PasswordEntry.TABLE_NAME,
                                whereClause,
                                whereArgs);
                        mPasswordList.remove(password);
                        break;
                    case PasswordApplication.MODE_SIM_STORAGE:
                        ApduResponse result = mApplication.getUicc().deletePassword(title);

                        if (!result.isSuccessful()) {
                            Toast.makeText(this, "failed to delete password on UICC", Toast.LENGTH_SHORT).show();
                        } else {
                            mPasswordList.remove(password);
                        }
                        break;
                    default:
                        break;
                }
                break;
            }
        }
    }

    /**
     * Check for duplicate password.
     *
     * @param title password title
     * @return true if password duplicate found
     */
    @Override
    public boolean checkDuplicatePassword(String title) {
        for (Password password : mPasswordList) {
            if (password.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        hideMenuButton();

        if (mFragment != null) {
            ((IFragmentOptions) mFragment).onUpdateToolbar();
        }
        MenuItem deleteButton = toolbar.getMenu().findItem(R.id.button_delete);

        deleteButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (mDeletionListener != null) {
                    mDeletionListener.onDelete();
                }
                return false;
            }
        });
        updateMode();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void hideMenuButton() {
        toolbar.getMenu().findItem(R.id.button_save).setVisible(false);
        toolbar.getMenu().findItem(R.id.button_mode).setVisible(false);
        toolbar.getMenu().findItem(R.id.button_delete).setVisible(false);
        toolbar.getMenu().findItem(R.id.button_add_password).setVisible(false);
    }

    @Override
    public List<Password> getPasswordList() {
        return mPasswordList;
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void setDeletionListener(IDeletionListener listener) {
        mDeletionListener = listener;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

