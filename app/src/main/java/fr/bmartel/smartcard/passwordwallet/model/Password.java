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
package fr.bmartel.smartcard.passwordwallet.model;

/**
 * Password model.
 *
 * @author Bertrand Martel
 */
public class Password {

    private String mTitle;

    private String mUsername;

    private byte[] mPassword;

    public Password(String title, String username, byte[] password) {
        mTitle = title;
        mUsername = username;
        mPassword = password;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUsername() {
        return mUsername;
    }

    public byte[] getPassword() {
        return mPassword;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void setPassword(byte[] password) {
        mPassword = password;
    }
}