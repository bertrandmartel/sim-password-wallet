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
package fr.bmartel.smartcard.passwordwallet.inter;

import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;

import java.util.List;

import fr.bmartel.smartcard.passwordwallet.model.Password;

/**
 * Interface used for the fragment to communicate with the main activity.
 *
 * @author Bertrand Martel
 */
public interface IBaseActivity extends IDialog {

    /**
     * Get password list.
     *
     * @return
     */
    List<Password> getPasswordList();

    /**
     * Get toolbar object.
     *
     * @return
     */
    Toolbar getToolbar();

    /**
     * Set toolbar title.
     *
     * @param title
     */
    void setToolbarTitle(String title);

    /**
     * hide button in toolbar.
     */
    void hideMenuButton();

    /**
     * Set the deletion listener to be called when user click on delete button.
     *
     * @param listener
     */
    void setDeletionListener(IDeletionListener listener);

    /**
     * called when a new password should be created.
     *
     * @param title    password title
     * @param username username
     * @param password password value
     * @return data payload
     */
    byte[] saveNewPassword(String title, String username, String password);

    /**
     * called when a password should be updated.
     *
     * @param formerTitle former password title
     * @param newTitle    new password title
     * @param username    new username value
     * @param password    new password value
     * @return data payload
     */
    byte[] saveExistingPassword(String formerTitle, String newTitle, String username, String password);

    /**
     * Delete password entry.
     *
     * @param title password title
     */
    void deletePassword(String title);

    /**
     * Check if password is duplicate.
     *
     * @param title password tittle
     * @return true if password is duplicated
     */
    boolean checkDuplicatePassword(String title);

    /**
     * Decrypt password.
     *
     * @param password encrypted password
     * @return clear text password
     */
    String decrypt(byte[] password);

    /**
     * Set working mode.
     *
     * @param mode     working mode
     * @param progress progress bar
     * @param listener deletion listener
     */
    void setMode(byte mode, ProgressBar progress, ICompletionListener listener);

    /**
     * called when correct pin code have been set and password fragment should be opened.
     */
    void onReady();

    /**
     * Get password entry.
     *
     * @param index index in password list
     * @return password entry
     */
    Password getPassword(int index);
}