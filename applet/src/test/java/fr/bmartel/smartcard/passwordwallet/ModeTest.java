package fr.bmartel.smartcard.passwordwallet;

import org.junit.Before;
import org.junit.Test;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import fr.bmartel.smartcard.passwordwallet.utils.TestUtils;
import javacard.framework.ISO7816;

import static org.junit.Assert.assertEquals;

public class ModeTest extends JavaCardTest {

    @Before
    public void setup() throws CardException {
        TestSuite.setup();
        verifyPinCode(TestUtils.TEST_PIN_CODE, 0x9000, new byte[]{});
    }

    private void checkMode(byte expectedMode) throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x40, 0x00, 0x00}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertEquals("data length", 1, response.getData().length);
        assertEquals(expectedMode, response.getData()[0]);
    }

    private void setMode(byte mode) throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x41, 0x00, 0x00, 0x01, mode}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertEquals("data length", 0, response.getData().length);
    }

    @Test
    public void getMode() throws CardException {
        checkMode((byte) 0x01);
    }

    @Test
    public void getModeInvalid() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x40, 0x00, 0x00, 0x01, 0x02}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_DATA_INVALID, response.getSW());
        assertEquals("data length", 0, response.getData().length);
    }

    @Test
    public void setMode() throws CardException {
        setMode((byte) 0x02);
        checkMode((byte) 0x02);
        setMode((byte) 0x01);
        checkMode((byte) 0x01);
    }

    @Test
    public void setModeInvalidLength() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x41, 0x00, 0x00, 0x02, 0x01, 0x02}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_DATA_INVALID, response.getSW());
        assertEquals("data length", 0, response.getData().length);
    }

    @Test
    public void setModeInvalidMode() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x41, 0x00, 0x00, 0x01, 0x03}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_DATA_INVALID, response.getSW());
        assertEquals("data length", 0, response.getData().length);
    }
}
