package fr.bmartel.smartcard.passwordwallet;

import org.junit.Before;
import org.junit.Test;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import fr.bmartel.smartcard.passwordwallet.utils.TestUtils;
import javacard.framework.ISO7816;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class EncryptDecryptTest extends JavaCardTest {

    private final static byte[] DATA16_WITHPADDING = new byte[]{0x01, 0x02};
    private final static byte[] DATA16_NOPADDING = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
    private final static byte[] DATA32_WITHPADDING = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11};
    private final static byte[] DATA32_NOPADDING = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F};

    @Before
    public void setup() throws CardException {
        TestSuite.setup();
        verifyPinCode(TestUtils.TEST_PIN_CODE, 0x9000, new byte[]{});
    }

    @Test
    public void encryptEmpty() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x10, 0x00, 0x00}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_DATA_INVALID, response.getSW());
        assertEquals("encrypted length", 0, response.getData().length);
    }

    @Test
    public void encryptWithPadding16() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x10, 0x00, 0x00}, DATA16_WITHPADDING));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertEquals("encrypted length", 16, response.getData().length);
    }

    @Test
    public void encryptWithPadding32() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x10, 0x00, 0x00}, DATA32_WITHPADDING));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertEquals("encrypted length", 32, response.getData().length);
    }

    @Test
    public void badCla() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x80, (byte) 0x10, 0x00, 0x00}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_CLA_NOT_SUPPORTED, response.getSW());
        assertEquals("data length", 0, response.getData().length);
    }

    @Test
    public void badP1() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x10, 0x01, 0x00}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_INCORRECT_P1P2, response.getSW());
        assertEquals("data length", 0, response.getData().length);
    }

    @Test
    public void badP2() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x10, 0x00, 0x01}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_INCORRECT_P1P2, response.getSW());
        assertEquals("data length", 0, response.getData().length);
    }

    @Test
    public void decryptEmpty() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x11, 0x00, 0x00}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_DATA_INVALID, response.getSW());
        assertEquals("data length", 0, response.getData().length);
    }

    private void encryptDecryptTest(byte[] data, int expectedLength) throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x10, 0x00, 0x00}, data));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertEquals("encrypted length", expectedLength, response.getData().length);

        c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x11, 0x00, 0x00}, response.getData()));
        response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertArrayEquals("original decrypted data", data, response.getData());
    }

    @Test
    public void encryptDecryptWithPadding16() throws CardException {
        encryptDecryptTest(DATA16_WITHPADDING, 16);
    }

    @Test
    public void encryptDecryptNoPadding16() throws CardException {
        encryptDecryptTest(DATA16_NOPADDING, 16);
    }

    @Test
    public void encryptDecryptWithPadding32() throws CardException {
        encryptDecryptTest(DATA32_WITHPADDING, 32);
    }

    @Test
    public void encryptDecryptNoPadding32() throws CardException {
        encryptDecryptTest(DATA32_NOPADDING, 32);
    }

    @Test
    public void invalidInstruction() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x09, 0x00, 0x00}, DATA16_WITHPADDING));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_FUNC_NOT_SUPPORTED, response.getSW());
    }

    @Test
    public void invalidDataLength() throws CardException {
        CommandAPDU c = new CommandAPDU(new byte[]{(byte) 0x90, (byte) 0x10, 0x00, 0x00, 0x00});
        ResponseAPDU response = transmitCommand(c);
        assertEquals(ISO7816.SW_DATA_INVALID, response.getSW());
    }

}