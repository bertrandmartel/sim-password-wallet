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

import android.util.Log;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

import java.io.IOException;
import java.util.List;

import fr.bmartel.smartcard.passwordwallet.model.Password;
import fr.bmartel.smartcard.passwordwallet.utils.HexUtils;

/**
 * Manage all I/O with UICC.
 *
 * @author Bertrand Martel
 */
public class Uicc {

    private final static String TAG = Uicc.class.getSimpleName();

    private final static String APPLET_ID = "D2760001180002FF49502589C0019B01";

    private final static byte INS_ENCRYPT = (byte) 0x10;
    private final static byte INS_DECRYPT = (byte) 0x11;
    private final static byte INS_GET_MODE = (byte) 0x40;
    private final static byte INS_SET_MODE = (byte) 0x41;
    private final static byte INS_ADD_PASSWORD = (byte) 0x30;
    private final static byte INS_LIST_PASSWORD = (byte) 0x36;
    private final static byte INS_DELETE_PASSWORD = (byte) 0x34;
    private final static byte INS_GET_PASSWORD = (byte) 0x32;
    private final static byte INS_EDIT_PASSWORD = (byte) 0x33;
    private final static byte INS_VERIFY = (byte) 0x20;
    private final static byte INS_GET_STATE = (byte) 0x50;
    private final static byte INS_PIN_CHECK = (byte) 0x51;
    private final static byte INS_CHANGE_REFERENCE_DATA = (byte) 0x24;

    private SEService mService;

    private Channel mChannel;

    /**
     * Init with SEService.
     *
     * @param service
     */
    public Uicc(SEService service) {
        mService = service;
    }

    /**
     * Open logical channel with applet.
     *
     * @throws SecurityException
     * @throws IOException
     */
    public void openChannel() throws SecurityException, IOException {
        Reader[] readers = mService.getReaders();
        if (readers.length < 1)
            return;
        Session session = readers[0].openSession();

        mChannel = session.openLogicalChannel(HexUtils.hexStringToByteArray(APPLET_ID));
    }

    /**
     * Send APDU to UICC.
     *
     * @param data      data payload
     * @param operation instruction
     * @return APDU response object
     */
    private ApduResponse requestSE(byte[] data, byte operation) {
        return requestSE(data, (byte) 0x00, (byte) 0x00, operation);
    }

    /**
     * Send APDU to UICC.
     *
     * @param data      data payload
     * @param P1        P1 param
     * @param P2        P2 param
     * @param operation instruction
     * @return APDU response object
     */
    private ApduResponse requestSE(byte[] data, byte P1, byte P2, byte operation) {
        if (mChannel != null) {
            try {
                byte[] respApdu;

                if (data.length > 0) {
                    respApdu = mChannel.transmit(new CommandApdu((byte) 0x90, operation, P1, P2, data).toBytes());
                } else {
                    respApdu = mChannel.transmit(new CommandApdu((byte) 0x90, operation, P1, P2, 0x00).toBytes());
                }
                return new ApduResponse(respApdu);
            } catch (Exception e) {
                Log.e(TAG, "Error occurred:", e);
            }
        }
        return new ApduResponse(new byte[]{});
    }

    /**
     * Get password list.
     *
     * @return
     */
    public List<Password> getPasswordList() {
        ApduResponse result = requestSE(new byte[]{}, INS_LIST_PASSWORD);

        if (result.isSuccessful()) {
            return UiccUtils.parsePaswordList(result.getData());
        }
        return null;
    }

    /**
     * Encrypt data on UICC.
     *
     * @param data clear text data
     * @return APDU response
     */
    public ApduResponse encrypt(byte[] data) {
        return requestSE(data, INS_ENCRYPT);
    }

    /**
     * Decrypt data on UICC.
     *
     * @param data encrypted data
     * @return clear text data
     */
    public ApduResponse decrypt(byte[] data) {
        return requestSE(data, INS_DECRYPT);
    }

    /**
     * Update a password entry.
     *
     * @param formerTitle former password title
     * @param newTitle    new password title
     * @param username    updated username
     * @param password    updated password value
     * @return APDU response
     */
    public ApduResponse editPassword(String formerTitle, String newTitle, String username, String password) {
        return requestSE(UiccUtils.buildEditPassword(formerTitle, newTitle, username, password), INS_EDIT_PASSWORD);
    }

    /**
     * Add a new password entry.
     *
     * @param title    password title
     * @param username username
     * @param password password value
     * @return APDU response
     */
    public ApduResponse addPassword(String title, String username, String password) {
        return requestSE(UiccUtils.buildAddPassword(title, username, password), INS_ADD_PASSWORD);
    }

    /**
     * Get password entry from password title.
     *
     * @param title password title
     * @return APDU response
     */
    public ApduResponse getPassword(String title) {
        return requestSE(UiccUtils.buildGetPassword(title), INS_GET_PASSWORD);
    }

    /**
     * Delete password entry.
     *
     * @param title password title
     * @return APDU response
     */
    public ApduResponse deletePassword(String title) {
        return requestSE(UiccUtils.buildDeletePassword(title), INS_DELETE_PASSWORD);
    }

    /**
     * Set working mode (storage on app or on UICC).
     *
     * @param mode mode value
     * @return APDU response
     */
    public ApduResponse setMode(byte mode) {
        return requestSE(new byte[]{mode}, INS_SET_MODE);
    }

    /**
     * Get working mode
     *
     * @return mode
     */
    public ApduResponse getMode() {
        return requestSE(new byte[]{}, INS_GET_MODE);
    }

    /**
     * Get the card state (secured mean the pin code has already been set).
     *
     * @return GP card state
     */
    public ApduResponse getCardState() {
        return requestSE(new byte[]{}, INS_GET_STATE);
    }

    /**
     * Check if pin code is already checked for this session.
     *
     * @return APDU response
     */
    public ApduResponse getPinCodeState() {
        return requestSE(new byte[]{}, INS_PIN_CHECK);
    }

    /**
     * Check pin code.
     *
     * @param data pincode
     * @return pin code result
     */
    public PinCodeResult checkPin(byte[] data) {
        ApduResponse res = requestSE(data, (byte) 0x00, (byte) 0x80, INS_VERIFY);

        if (res.isSuccessful()) {
            return new PinCodeResult(true, -1);
        } else {
            if ((res.getStatus() & 0xFFF0) != 0x63C0) {
                return new PinCodeResult(false, 0);
            } else {
                return new PinCodeResult(false, res.getStatus() & 0x000F);
            }
        }
    }

    /**
     * Update pin code.
     *
     * @param noPin  true if it's the first time we set pin code
     * @param oldPin old pin code (if noPin is false)
     * @param newPin new pin code
     * @return APDU response
     */
    public ApduResponse updatePin(boolean noPin, byte[] oldPin, byte[] newPin) {
        if (noPin) {
            byte[] request = new byte[newPin.length + 1];
            request[0] = (byte) newPin.length;
            System.arraycopy(newPin, 0, request, 1, newPin.length);
            return requestSE(request, (byte) 0x00, (byte) 0x80, INS_CHANGE_REFERENCE_DATA);
        } else {
            byte[] request = new byte[oldPin.length + 1 + newPin.length + 1];
            int index = 0;
            request[index++] = (byte) oldPin.length;
            System.arraycopy(oldPin, 0, request, index, oldPin.length);
            index += oldPin.length;
            request[index++] = (byte) newPin.length;
            System.arraycopy(newPin, 0, request, index, newPin.length);
            return requestSE(request, (byte) 0x01, (byte) 0x80, INS_CHANGE_REFERENCE_DATA);
        }
    }

    /**
     * Close current session.
     */
    public void closeChannel() {
        mChannel.close();
    }
}
