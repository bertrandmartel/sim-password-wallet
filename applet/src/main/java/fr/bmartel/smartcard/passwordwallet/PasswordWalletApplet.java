/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2017 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.smartcard.passwordwallet;

import org.globalplatform.GPSystem;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;
import javacard.security.AESKey;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;
import javacardx.crypto.Cipher;

/**
 * Password Wallet applet.
 *
 * @author Bertrand Martel
 */
public class PasswordWalletApplet extends Applet {

    private OwnerPIN pin;

    private Cipher aesCipher;
    private AESKey aesKey;
    private final static short KEY_SIZE = 32;

    private final static byte INS_ADD_PASSWORD = (byte) 0x30;
    private final static byte INS_RETRIEVE_PASSWORD = (byte) 0x32;
    private final static byte INS_EDIT_PASSWORD = (byte) 0x33;
    private final static byte INS_DELETE_PASSWORD = (byte) 0x34;
    private final static byte INS_LIST_PASSWORD = (byte) 0x36;

    private final static byte INS_ENCRYPT = (byte) 0x10;
    private final static byte INS_DECRYPT = (byte) 0x11;

    private final static byte INS_GET_MODE = (byte) 0x40;
    private final static byte INS_SET_MODE = (byte) 0x41;

    private final static byte INS_VERIFY = (byte) 0x20;
    private final static byte INS_CHANGE_REFERENCE_DATA = (byte) 0x24;

    private final static byte INS_CARD_STATE = (byte) 0x50;
    private final static byte INS_PIN_CHECK = (byte) 0x51;

    public final static byte PIN_TRY_LIMIT = (byte) 3;
    public final static byte PIN_MAX_SIZE = (byte) 16;

    private final static short SW_WRONG_PIN = (short) 0x63c0;

    public final static short SW_DUPLICATE_IDENTIFIER = (short) 0x6A8A;
    public final static short SW_IDENTIFIER_NOT_FOUND = (short) 0x6A82;

    public final static byte TAG_IDENTIFIER = (byte) 0xF1;
    public final static byte TAG_USERNAME = (byte) 0xF2;
    public final static byte TAG_PASSWORD = (byte) 0xF3;
    public final static byte TAG_OLD_IDENTIFIER = (byte) 0xF4;

    private PasswordEntry current;

    private final static byte MODE_APP_STORAGE = 0x01;
    private final static byte MODE_SIM_STORAGE = 0x02;

    private byte mode = MODE_APP_STORAGE;

    private PasswordWalletApplet() {
        pin = new OwnerPIN(PIN_TRY_LIMIT, PIN_MAX_SIZE);

        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        byte[] keyBytes = JCSystem.makeTransientByteArray(KEY_SIZE, JCSystem.CLEAR_ON_DESELECT);
        try {
            RandomData rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
            rng.generateData(keyBytes, (short) 0, KEY_SIZE);
            aesKey.setKey(keyBytes, (short) 0);
        } finally {
            Util.arrayFillNonAtomic(keyBytes, (short) 0, KEY_SIZE, (byte) 0);
        }
    }

    public static void install(byte[] buffer, short offset, byte length) {
        (new PasswordWalletApplet()).register();
    }

    public void process(APDU apdu) throws ISOException {
        if (selectingApplet())
            return;

        byte[] buffer = apdu.getBuffer();

        short len = apdu.setIncomingAndReceive();

        switch (GPSystem.getCardContentState()) {

            case GPSystem.APPLICATION_SELECTABLE:
                switch (buffer[ISO7816.OFFSET_INS]) {
                    case INS_CHANGE_REFERENCE_DATA:
                        processChangeReferenceData(len);
                        break;
                    case INS_CARD_STATE:
                        processCardState();
                        break;
                    default:
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                }
                break;
            case GPSystem.CARD_SECURED:

                if (buffer[ISO7816.OFFSET_INS] != INS_LIST_PASSWORD)
                    current = null;
                if (((byte) (buffer[ISO7816.OFFSET_CLA] & (byte) 0xFC)) != (byte) 0x90) {
                    ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
                }

                if ((buffer[ISO7816.OFFSET_INS] != INS_CHANGE_REFERENCE_DATA && (buffer[ISO7816.OFFSET_INS] != INS_VERIFY)) &&
                        (buffer[ISO7816.OFFSET_P1] != 0 || buffer[ISO7816.OFFSET_P2] != 0))
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);

                if (len != (short) (buffer[ISO7816.OFFSET_LC] & 0xFF))
                    ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

                switch (buffer[ISO7816.OFFSET_INS]) {
                    case INS_GET_MODE:
                        checkAuthentication();
                        sendMode();
                        break;
                    case INS_SET_MODE:
                        checkAuthentication();
                        setMode();
                        break;
                    case INS_ENCRYPT:
                        checkAuthentication();
                        encrypt();
                        break;
                    case INS_DECRYPT:
                        checkAuthentication();
                        decrypt();
                        break;
                    case INS_ADD_PASSWORD:
                        checkAuthentication();
                        processAddPasswordEntry();
                        break;
                    case INS_RETRIEVE_PASSWORD:
                        checkAuthentication();
                        processRetrievePasswordEntry();
                        break;
                    case INS_EDIT_PASSWORD:
                        checkAuthentication();
                        processEditPasswordEntry();
                        break;
                    case INS_DELETE_PASSWORD:
                        checkAuthentication();
                        processDeletePasswordEntry();
                        break;
                    case INS_LIST_PASSWORD:
                        checkAuthentication();
                        processListIdentifiers();
                        break;
                    case INS_VERIFY:
                        processVerify(len);
                        break;
                    case INS_CHANGE_REFERENCE_DATA:
                        processChangeReferenceData(len);
                        break;
                    case INS_CARD_STATE:
                        processCardState();
                        break;
                    case INS_PIN_CHECK:
                        checkAuthentication();
                        break;
                    default:
                        ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
                }
                break;
            default:
                ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
    }

    /**
     * Send the working mode.
     */
    private void sendMode() {
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buffer = APDU.getCurrentAPDUBuffer();

        if ((short) (buffer[ISO7816.OFFSET_LC] & 0xFF) != 0)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        buffer[0] = mode;
        apdu.setOutgoingAndSend((short) 0x00, (short) 1);
    }

    /**
     * return the card state.
     */
    private void processCardState() {
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buffer = APDU.getCurrentAPDUBuffer();

        if ((short) (buffer[ISO7816.OFFSET_LC] & 0xFF) != 0)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        buffer[0] = GPSystem.getCardContentState();
        apdu.setOutgoingAndSend((short) 0x00, (short) 1);
    }

    /**
     * Set working mode.
     */
    private void setMode() {
        byte[] buffer = APDU.getCurrentAPDUBuffer();

        if ((short) (buffer[ISO7816.OFFSET_LC] & 0xFF) != 1)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        if (buffer[ISO7816.OFFSET_CDATA] != MODE_APP_STORAGE && buffer[ISO7816.OFFSET_CDATA] != MODE_SIM_STORAGE) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        mode = buffer[ISO7816.OFFSET_CDATA];
    }

    /**
     * Encrypt data.
     */
    private void encrypt() {
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buffer = APDU.getCurrentAPDUBuffer();

        if ((short) (buffer[ISO7816.OFFSET_LC] & 0xFF) < 1)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        aesCipher.init(aesKey, Cipher.MODE_ENCRYPT);
        short newLength = addPadding(buffer, (short) ISO7816.OFFSET_CDATA, (short) buffer[ISO7816.OFFSET_LC]);
        byte[] temp = JCSystem.makeTransientByteArray(newLength, JCSystem.CLEAR_ON_DESELECT);
        aesCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, newLength, temp, (short) 0x00);
        Util.arrayCopyNonAtomic(temp, (short) 0x00, buffer, (short) 0x00, newLength);
        apdu.setOutgoingAndSend((short) 0x00, newLength);
    }

    /**
     * Decrypt data.
     */
    private void decrypt() {
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buffer = APDU.getCurrentAPDUBuffer();

        if ((short) (buffer[ISO7816.OFFSET_LC] & 0xFF) < 1)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        aesCipher.init(aesKey, Cipher.MODE_DECRYPT);
        byte[] temp = JCSystem.makeTransientByteArray((short) buffer[ISO7816.OFFSET_LC], JCSystem.CLEAR_ON_DESELECT);
        aesCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, (short) buffer[ISO7816.OFFSET_LC], temp, (short) 0x00);
        short newLength = removePadding(temp, (short) buffer[ISO7816.OFFSET_LC]);
        Util.arrayCopyNonAtomic(temp, (short) 0x00, buffer, (short) 0x00, newLength);
        apdu.setOutgoingAndSend((short) 0x00, newLength);
    }

    /**
     * Add padding for AES encryption.
     *
     * @param data
     * @param offset
     * @param length
     * @return
     */
    private short addPadding(byte[] data, short offset, short length) {
        data[(short) (offset + length++)] = (byte) 0x80;
        while (length < 16 || (length % 16 != 0)) {
            data[(short) (offset + length++)] = 0x00;
        }
        return length;
    }

    /**
     * remove padding from decrypted result.
     *
     * @param buffer
     * @param length
     * @return
     */
    private short removePadding(byte[] buffer, short length) {
        while ((length != 0) && buffer[(short) (length - 1)] == (byte) 0x00) {
            length--;
        }
        if (buffer[(short) (length - 1)] != (byte) 0x80) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        length--;
        return length;
    }

    /**
     * Check TAG Length Value.
     *
     * @param buffer
     * @param inOfs
     * @param tag
     * @param maxLen
     * @return
     */
    short checkTLV(byte[] buffer, short inOfs, byte tag, short maxLen) {
        if (buffer[inOfs++] != tag)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        short len = buffer[inOfs++];
        if (len > maxLen)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        return (short) (inOfs + len);
    }

    void processAddPasswordEntry() {
        byte[] buf = APDU.getCurrentAPDUBuffer();

        if ((short) (buf[ISO7816.OFFSET_LC] & 0xFF) < 3)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        short ofsId = ISO7816.OFFSET_CDATA;
        short ofsUserName = checkTLV(buf, ofsId, TAG_IDENTIFIER, PasswordEntry.SIZE_ID);

        if (buf[ISO7816.OFFSET_LC] < (short) (ofsUserName - 3))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        short ofsPassword = checkTLV(buf, ofsUserName, TAG_USERNAME, PasswordEntry.SIZE_USERNAME);

        if (buf[ISO7816.OFFSET_LC] < (short) (ofsPassword - 3))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        if (checkTLV(buf, ofsPassword, TAG_PASSWORD, PasswordEntry.SIZE_PASSWORD) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        if (PasswordEntry.search(buf, (short) (ofsId + 2), buf[(short) (ofsId + 1)]) != null)
            ISOException.throwIt(SW_DUPLICATE_IDENTIFIER);

        JCSystem.beginTransaction();
        PasswordEntry pe = PasswordEntry.getInstance();
        pe.setId(buf, (short) (ofsId + 2), buf[(short) (ofsId + 1)]);
        pe.setUserName(buf, (short) (ofsUserName + 2), buf[(short) (ofsUserName + 1)]);

        aesCipher.init(aesKey, Cipher.MODE_ENCRYPT);
        short newLength = addPadding(buf, (short) (ofsPassword + 2), buf[(short) (ofsPassword + 1)]);
        byte[] temp = JCSystem.makeTransientByteArray(newLength, JCSystem.CLEAR_ON_DESELECT);
        aesCipher.doFinal(buf, (short) (ofsPassword + 2), newLength, temp, (short) 0x00);
        pe.setPassword(temp, (short) 0x00, (byte) newLength);

        JCSystem.commitTransaction();
    }

    void processDeletePasswordEntry() {
        byte[] buf = APDU.getCurrentAPDUBuffer();

        if ((short) (buf[ISO7816.OFFSET_LC] & 0xFF) < 3)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        short ofsId = ISO7816.OFFSET_CDATA;
        if (checkTLV(buf, ISO7816.OFFSET_CDATA, TAG_IDENTIFIER, PasswordEntry.SIZE_ID) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        PasswordEntry pe = PasswordEntry.search(buf,
                (short) (ISO7816.OFFSET_CDATA + 2),
                buf[ISO7816.OFFSET_CDATA + 1]);
        if (pe == null)
            ISOException.throwIt(SW_IDENTIFIER_NOT_FOUND);

        PasswordEntry.delete(buf, (short) (ofsId + 2), buf[(short) (ofsId + 1)]);
    }

    void processEditPasswordEntry() {
        byte[] buf = APDU.getCurrentAPDUBuffer();

        if ((short) (buf[ISO7816.OFFSET_LC] & 0xFF) < 3)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        short ofsOldId = ISO7816.OFFSET_CDATA;

        short ofsId = checkTLV(buf, ofsOldId, TAG_OLD_IDENTIFIER, PasswordEntry.SIZE_ID);

        if (buf[ISO7816.OFFSET_LC] < (short) (ofsId - 3))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        short ofsUserName = checkTLV(buf, ofsId, TAG_IDENTIFIER, PasswordEntry.SIZE_ID);

        if (buf[ISO7816.OFFSET_LC] < (short) (ofsUserName - 3))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        short ofsPassword = checkTLV(buf, ofsUserName, TAG_USERNAME, PasswordEntry.SIZE_USERNAME);

        if (buf[ISO7816.OFFSET_LC] < (short) (ofsPassword - 3))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        if (checkTLV(buf, ofsPassword, TAG_PASSWORD, PasswordEntry.SIZE_PASSWORD) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        PasswordEntry pe = PasswordEntry.search(buf, (short) (ofsOldId + 2), buf[(short) (ofsOldId + 1)]);

        if (pe == null)
            ISOException.throwIt(SW_IDENTIFIER_NOT_FOUND);

        JCSystem.beginTransaction();
        pe.setId(buf, (short) (ofsId + 2), buf[(short) (ofsId + 1)]);
        pe.setUserName(buf, (short) (ofsUserName + 2), buf[(short) (ofsUserName + 1)]);

        //store encrypted password
        aesCipher.init(aesKey, Cipher.MODE_ENCRYPT);
        short newLength = addPadding(buf, (short) (ofsPassword + 2), buf[(short) (ofsPassword + 1)]);
        byte[] temp = JCSystem.makeTransientByteArray(newLength, JCSystem.CLEAR_ON_DESELECT);
        aesCipher.doFinal(buf, (short) (ofsPassword + 2), newLength, temp, (short) 0x00);
        pe.setPassword(temp, (short) 0x00, (byte) newLength);

        JCSystem.commitTransaction();
    }

    void processRetrievePasswordEntry() {
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buf = APDU.getCurrentAPDUBuffer();

        if ((short) (buf[ISO7816.OFFSET_LC] & 0xFF) < 3)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        if (checkTLV(buf, ISO7816.OFFSET_CDATA, TAG_IDENTIFIER, PasswordEntry.SIZE_ID) !=
                (short) (ISO7816.OFFSET_CDATA + (short) (buf[ISO7816.OFFSET_LC] & 0xFF)))
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        PasswordEntry pe = PasswordEntry.search(buf, (short) (ISO7816.OFFSET_CDATA + 2), buf[ISO7816.OFFSET_CDATA + 1]);
        if (pe == null)
            ISOException.throwIt(SW_IDENTIFIER_NOT_FOUND);

        short outOfs = 0;
        buf[outOfs++] = TAG_USERNAME;
        byte len = pe.getUserName(buf, (short) (outOfs + 1));
        buf[outOfs++] = len;
        outOfs += len;

        buf[outOfs++] = TAG_PASSWORD;

        aesCipher.init(aesKey, Cipher.MODE_DECRYPT);
        byte[] temp = JCSystem.makeTransientByteArray((short) pe.getPasswordLength(), JCSystem.CLEAR_ON_DESELECT);
        aesCipher.doFinal(pe.getPasword(), (short) 0, (short) pe.getPasswordLength(), temp, (short) 0);
        short newLength = removePadding(temp, (short) pe.getPasswordLength());
        Util.arrayCopyNonAtomic(temp, (short) 0x00, buf, (short) (outOfs + 1), newLength);

        buf[outOfs++] = (byte) newLength;
        outOfs += newLength;

        apdu.setOutgoingAndSend((short) 0, outOfs);
    }

    void processListIdentifiers() {
        APDU apdu = APDU.getCurrentAPDU();
        byte[] buf = APDU.getCurrentAPDUBuffer();

        if (buf[ISO7816.OFFSET_P1] == 0)
            current = PasswordEntry.getFirst();
        else if ((buf[ISO7816.OFFSET_P1] != 1) || (current == null))
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);

        short offset = 0;
        while (current != null) {
            byte len = current.getIdLength();
            if ((short) ((short) (offset + len) + 2) > 255)
                break;

            buf[offset++] = TAG_IDENTIFIER;
            buf[offset++] = len;
            current.getId(buf, offset);

            offset += len;
            current = current.getNext();
        }
        apdu.setOutgoingAndSend((short) 0, offset);
    }

    void processVerify(short len) {
        byte[] buf = APDU.getCurrentAPDUBuffer();

        if (buf[ISO7816.OFFSET_P2] != (byte) 0x80)
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);

        if (pin.getTriesRemaining() == 0)
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);

        if (buf[ISO7816.OFFSET_LC] == 0) {
            if (pin.isValidated())
                return;
            else
                ISOException.throwIt((short) (SW_WRONG_PIN + pin.getTriesRemaining()));
        }
        verifyPIN(buf, ISO7816.OFFSET_CDATA, (byte) len);
    }

    void verifyPIN(byte[] buffer, short index, byte len) {
        if (len > PIN_MAX_SIZE)
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        if (!pin.check(buffer, index, len)) {
            if (pin.getTriesRemaining() == 0)
                ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            else
                ISOException.throwIt((short) (SW_WRONG_PIN + pin.getTriesRemaining()));
        }
    }

    void processChangeReferenceData(short len) {
        byte[] buf = APDU.getCurrentAPDUBuffer();

        if (buf[ISO7816.OFFSET_P2] != (byte) 0x80)
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);

        byte p1 = buf[ISO7816.OFFSET_P1];
        switch (p1) {
            case 0:
                if (GPSystem.getCardContentState() != GPSystem.APPLICATION_SELECTABLE)
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                break;
            case 1:
                if (GPSystem.getCardContentState() != GPSystem.CARD_SECURED)
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }
        if (len < 2)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        short index = ISO7816.OFFSET_CDATA;

        if (p1 == 1) {
            byte oldPinLen = buf[index++];
            if (len < (short) (oldPinLen + 3))
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            verifyPIN(buf, index, oldPinLen);
            index += oldPinLen;
        }

        byte newPinLen = buf[index++];
        if (len != (short) (index + (short) (newPinLen - ISO7816.OFFSET_CDATA)))
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        if (newPinLen > PIN_MAX_SIZE)
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);

        pin.update(buf, index, newPinLen);

        if (p1 == 0) {
            GPSystem.setCardContentState(GPSystem.CARD_SECURED);
        }
    }

    void checkAuthentication() {
        if (!pin.isValidated())
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
    }

    /**
     * Resets the PIN's validated flag upon deselection of the applet.
     */
    public void deselect() {
        pin.reset();
    }
}
