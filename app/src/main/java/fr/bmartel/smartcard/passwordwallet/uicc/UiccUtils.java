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
package fr.bmartel.smartcard.passwordwallet.uicc;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.smartcard.passwordwallet.model.Password;

/**
 * UICC utility functions used to generate/parse payload.
 *
 * @author Bertrand Martel
 */
public class UiccUtils {

    /**
     * Build create password entry payload.
     *
     * @param title    password title
     * @param username username
     * @param password password value
     * @return data payload
     */
    public static byte[] buildAddPassword(String title, String username, String password) {
        byte[] titleBa = title.getBytes();
        byte[] usernameBa = username.getBytes();
        byte[] passwordBa = password.getBytes();

        byte[] res = new byte[titleBa.length + usernameBa.length + passwordBa.length + 3 + 3];

        int offset = 0;

        res[offset++] = (byte) 0xF1;
        res[offset++] = (byte) titleBa.length;
        System.arraycopy(titleBa, 0, res, offset, titleBa.length);
        offset += titleBa.length;
        res[offset++] = (byte) 0xF2;
        res[offset++] = (byte) usernameBa.length;
        System.arraycopy(usernameBa, 0, res, offset, usernameBa.length);
        offset += usernameBa.length;
        res[offset++] = (byte) 0xF3;
        res[offset++] = (byte) passwordBa.length;
        System.arraycopy(passwordBa, 0, res, offset, passwordBa.length);
        return res;
    }

    /**
     * Build update password entry payload.
     *
     * @param oldTitle former password title
     * @param title    new password title
     * @param username new username value
     * @param password new password value
     * @return data payload
     */
    public static byte[] buildEditPassword(String oldTitle, String title, String username, String password) {
        byte[] oldTitleBa = oldTitle.getBytes();
        byte[] titleBa = title.getBytes();
        byte[] usernameBa = username.getBytes();
        byte[] passwordBa = password.getBytes();

        byte[] res = new byte[oldTitleBa.length + titleBa.length + usernameBa.length + passwordBa.length + 4 + 4];

        int offset = 0;

        res[offset++] = (byte) 0xF4;
        res[offset++] = (byte) oldTitleBa.length;
        System.arraycopy(oldTitleBa, 0, res, offset, oldTitleBa.length);
        offset += oldTitleBa.length;

        res[offset++] = (byte) 0xF1;
        res[offset++] = (byte) titleBa.length;
        System.arraycopy(titleBa, 0, res, offset, titleBa.length);
        offset += titleBa.length;

        res[offset++] = (byte) 0xF2;
        res[offset++] = (byte) usernameBa.length;
        System.arraycopy(usernameBa, 0, res, offset, usernameBa.length);
        offset += usernameBa.length;

        res[offset++] = (byte) 0xF3;
        res[offset++] = (byte) passwordBa.length;
        System.arraycopy(passwordBa, 0, res, offset, passwordBa.length);

        return res;
    }

    /**
     * Parse GET password list response.
     *
     * @param data data payload.
     * @return list of password entry
     */
    public static List<Password> parsePaswordList(byte[] data) {
        int state = 0;
        byte[] currentTag = null;
        int index = 0;
        List<Password> passwordList = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            switch (state) {
                case 0:
                    if ((data[i] & 0xFF) == 0xF1) {
                        state = 1;
                    }
                    break;
                case 1:
                    currentTag = new byte[data[i] & 0xFF];
                    state = 2;
                    index = 0;
                    break;
                case 2:
                    currentTag[index++] = data[i];
                    if (index == currentTag.length) {
                        state = 0;
                        passwordList.add(new Password(new String(currentTag), null, null));
                    }
                    break;
                default:
                    break;
            }
        }
        return passwordList;
    }

    /**
     * Build delete password entry data payload.
     *
     * @param title password title
     * @return data payload
     */
    public static byte[] buildDeletePassword(String title) {
        byte[] titleBa = title.getBytes();

        byte[] res = new byte[titleBa.length + 2];
        res[0] = (byte) 0xF1;
        res[1] = (byte) titleBa.length;
        System.arraycopy(titleBa, 0, res, 2, titleBa.length);
        return res;
    }

    /**
     * Build get password entry data payload.
     *
     * @param title password title
     * @return data payload
     */
    public static byte[] buildGetPassword(String title) {
        byte[] titleBa = title.getBytes();

        byte[] res = new byte[titleBa.length + 2];
        res[0] = (byte) 0xF1;
        res[1] = (byte) titleBa.length;
        System.arraycopy(titleBa, 0, res, 2, titleBa.length);
        return res;
    }

    /**
     * Parse Get password entry response.
     *
     * @param title password title (from the request)
     * @param data  data payload
     * @return password entry object
     */
    public static Password parsePassword(String title, byte[] data) {
        int state = 0;
        byte[] currentTag = null;
        int index = 0;
        String username = null;
        byte[] password = null;

        for (int i = 0; i < data.length; i++) {
            switch (state) {
                case 0:
                    if ((data[i] & 0xFF) == 0xF2) {
                        state = 1;
                    }
                    break;
                case 1:
                    currentTag = new byte[data[i] & 0xFF];
                    state = 2;
                    index = 0;
                    break;
                case 2:
                    currentTag[index++] = data[i];
                    if (index == currentTag.length) {
                        state = 3;
                        username = new String(currentTag);
                    }
                    break;
                case 3:
                    if ((data[i] & 0xFF) == 0xF3) {
                        state = 4;
                    }
                    break;
                case 4:
                    currentTag = new byte[data[i] & 0xFF];
                    state = 5;
                    index = 0;
                    break;
                case 5:
                    currentTag[index++] = data[i];
                    if (index == currentTag.length) {
                        state = 0;
                        password = currentTag;
                    }
                    break;
                default:
                    break;
            }
        }
        if (username != null && password != null) {
            return new Password(title, username, password);
        }
        return null;
    }

    /**
     * Convert string to byte array with pin code values.
     */
    public static byte[] convertPinCode(String pass) {
        if (isNumeric(pass)) {
            byte[] data = new byte[pass.length()];
            for (int i = 0; i < pass.length(); i++) {
                data[i] = (byte) ((int) pass.charAt(i) - 48);
            }
            return data;
        }
        return null;
    }

    /**
     * https://stackoverflow.com/a/1102916/2614364.
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}