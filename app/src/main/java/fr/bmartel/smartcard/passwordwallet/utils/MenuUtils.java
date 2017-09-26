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
package fr.bmartel.smartcard.passwordwallet.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;

import com.github.orangegangsters.lollipin.lib.managers.AppLock;

import java.io.IOException;

import fr.bmartel.smartcard.passwordwallet.CustomPinActivity;
import fr.bmartel.smartcard.passwordwallet.MainActivity;
import fr.bmartel.smartcard.passwordwallet.R;
import fr.bmartel.smartcard.passwordwallet.application.PasswordApplication;
import fr.bmartel.smartcard.passwordwallet.dialog.AboutDialog;
import fr.bmartel.smartcard.passwordwallet.dialog.OpenSourceItemsDialog;
import fr.bmartel.smartcard.passwordwallet.inter.IDialog;

/**
 * Some functions used to manage Menu.
 *
 * @author Bertrand Martel
 */
public class MenuUtils {

    private final static String TAG = MenuUtils.class.getSimpleName();

    /**
     * Execute actions according to selected menu item.
     *
     * @param menuItem MenuItem object
     * @param mDrawer  navigation drawer
     */
    public static void selectDrawerItem(Activity currentActivity,
                                        PasswordApplication application,
                                        MenuItem menuItem,
                                        DrawerLayout mDrawer,
                                        IDialog activity) {

        switch (menuItem.getItemId()) {
            case R.id.close_session:
                application.getUicc().closeChannel();
                try {
                    application.getUicc().openChannel();
                } catch (IOException e) {
                    Log.e(TAG, "IOException", e);
                }
                Intent intent = new Intent(currentActivity, CustomPinActivity.class);
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);
                currentActivity.startActivityForResult(intent, MainActivity.REQUEST_UNLOCK_PIN);
                break;
            case R.id.change_pincode:
                application.getUicc().closeChannel();
                try {
                    application.getUicc().openChannel();
                } catch (IOException e) {
                    Log.e(TAG, "IOException", e);
                }
                intent = new Intent(currentActivity, CustomPinActivity.class);
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN);
                currentActivity.startActivityForResult(intent, MainActivity.REQUEST_UNLOCK_PIN);
                break;
            case R.id.open_source_components: {
                OpenSourceItemsDialog dialog = new OpenSourceItemsDialog(currentActivity);
                activity.setCurrentDialog(dialog);
                dialog.show();
                break;
            }
            case R.id.about_app: {
                AboutDialog dialog = new AboutDialog(currentActivity);
                activity.setCurrentDialog(dialog);
                dialog.show();
                break;
            }
        }
        mDrawer.closeDrawers();
    }
}