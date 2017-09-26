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

import javacard.framework.JCSystem;
import javacard.framework.Util;

/**
 * Password Entry model used to store title, username and password.
 *
 * @author Bertrand Martel
 */
public class PasswordEntry {

    public static short SIZE_ID = 32;
    public static short SIZE_USERNAME = 64;
    public static short SIZE_PASSWORD = 128;

    private PasswordEntry next;
    private static PasswordEntry first;
    private static PasswordEntry deleted;

    private byte[] id;
    private byte[] username;
    private byte[] password;

    private byte idLength;
    private byte userNameLength;
    private byte passwordLength;

    private PasswordEntry() {
        id = new byte[SIZE_ID];
        username = new byte[SIZE_USERNAME];
        password = new byte[SIZE_PASSWORD];
        next = first;
        first = this;
    }

    static PasswordEntry getInstance() {
        if (deleted == null) {
            return new PasswordEntry();
        } else {
            PasswordEntry instance = deleted;
            deleted = instance.next;
            instance.next = first;
            first = instance;
            return instance;
        }
    }

    static PasswordEntry search(byte[] buf, short ofs, byte len) {
        for (PasswordEntry pe = first; pe != null; pe = pe.next) {
            if (pe.idLength != len) continue;
            if (Util.arrayCompare(pe.id, (short) 0, buf, ofs, len) == 0)
                return pe;
        }
        return null;
    }

    public static PasswordEntry getFirst() {
        return first;
    }

    private void remove() {
        if (first == this) {
            first = next;
        } else {
            for (PasswordEntry pe = first; pe != null; pe = pe.next)
                if (pe.next == this)
                    pe.next = next;
        }
    }

    private void recycle() {
        next = deleted;
        idLength = 0;
        userNameLength = 0;
        passwordLength = 0;
        deleted = this;
    }

    static void delete(byte[] buf, short ofs, byte len) {
        PasswordEntry pe = search(buf, ofs, len);
        if (pe != null) {
            JCSystem.beginTransaction();
            pe.remove();
            pe.recycle();
            JCSystem.commitTransaction();
        }
    }

    byte getId(byte[] buf, short ofs) {
        Util.arrayCopy(id, (short) 0, buf, ofs, idLength);
        return idLength;
    }

    byte getUserName(byte[] buf, short ofs) {
        Util.arrayCopy(username, (short) 0, buf, ofs, userNameLength);
        return userNameLength;
    }

    byte getPassword(byte[] buf, short ofs) {
        Util.arrayCopy(password, (short) 0, buf, ofs, passwordLength);
        return passwordLength;
    }

    public byte getIdLength() {
        return idLength;
    }

    public PasswordEntry getNext() {
        return next;
    }

    public void setId(byte[] buf, short ofs, byte len) {
        Util.arrayCopy(buf, ofs, id, (short) 0, len);
        idLength = len;
    }

    public void setUserName(byte[] buf, short ofs, byte len) {
        Util.arrayCopy(buf, ofs, username, (short) 0, len);
        userNameLength = len;
    }

    public void setPassword(byte[] buf, short ofs, byte len) {
        Util.arrayCopy(buf, ofs, password, (short) 0, len);
        passwordLength = len;
    }

    public byte getPasswordLength() {
        return passwordLength;
    }

    public byte[] getPasword() {
        return password;
    }
}
