package fr.bmartel.smartcard.passwordwallet.uicc;

import java.util.Arrays;

/**
 * APDU Response object.
 */
public class ApduResponse {

    private byte[] data;

    public ApduResponse(byte[] data) {
        this.data = data;
    }

    /**
     * Check if state is successful (0x9000 received).
     *
     * @return success state
     */
    public boolean isSuccessful() {
        if (data.length >= 2) {
            return (data[data.length - 2] == (byte) 0x90) && data[data.length - 1] == (byte) 0x00;
        }
        return false;
    }

    /**
     * Get data payload.
     *
     * @return
     */
    public byte[] getData() {
        if (data.length < 2) {
            return new byte[]{};
        }
        return Arrays.copyOfRange(data, 0, data.length - 2);
    }

    /**
     * Get Status word.
     *
     * @return
     */
    public short getStatus() {
        if (data.length < 2) {
            return 0x0000;
        }
        return (short) (((data[data.length - 2] & 0xFF) << 8) + (data[data.length - 1] & 0xFF));
    }

    public byte[] getResponse() {
        return data;
    }
}
