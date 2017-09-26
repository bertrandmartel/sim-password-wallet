package fr.bmartel.smartcard.passwordwallet.model;

import fr.bmartel.smartcard.passwordwallet.utils.TestUtils;

public class Password {

    private byte[] id;
    private byte[] username;
    private byte[] password;
    private byte[] oldId;

    public Password(byte[] oldId, byte[] id, byte[] username, byte[] password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.oldId = oldId;
    }

    public Password(byte[] id, byte[] username, byte[] password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public byte[] getFullApdu() {
        return TestUtils.concatByteArray(oldId, id, username, password);
    }

    public byte[] getId() {
        return id;
    }

    public byte[] getData() {
        return TestUtils.concatByteArray(username, password);
    }
}