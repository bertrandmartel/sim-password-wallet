package fr.bmartel.smartcard.passwordwallet;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import fr.bmartel.smartcard.passwordwallet.utils.TestUtils;

public class JavaCardTest {

    private final static byte[] CMD_VERIFY_PIN_CODE = new byte[]{(byte) 0x90, 0x20, 0x00, (byte) 0x80};

    public ResponseAPDU transmitCommand(CommandAPDU data) throws CardException {
        if (System.getProperty("testMode") != null && System.getProperty("testMode").equals("smartcard")) {
            return TestSuite.getCard().getBasicChannel().transmit(data);
        } else {
            return TestSuite.getSimulator().transmitCommand(data);
        }
    }

    protected void verifyPinCode(byte[] data, int expectedSw, byte[] expectedResponse) throws CardException {
        TestUtils.sendCmdBatch(this, CMD_VERIFY_PIN_CODE, data, expectedSw, expectedResponse);
    }
}