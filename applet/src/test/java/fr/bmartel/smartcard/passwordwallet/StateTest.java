package fr.bmartel.smartcard.passwordwallet;

import org.globalplatform.GPSystem;
import org.junit.Before;
import org.junit.Test;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import fr.bmartel.smartcard.passwordwallet.utils.TestUtils;

import static org.junit.Assert.assertEquals;

public class StateTest extends JavaCardTest {

    @Before
    public void setup() throws CardException {
        TestSuite.setup();
        verifyPinCode(TestUtils.TEST_PIN_CODE, 0x9000, new byte[]{});
    }

    @Test
    public void checkState() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x50, 0x00, 0x00}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertEquals("data length", 1, response.getData().length);
        assertEquals(GPSystem.CARD_SECURED, response.getData()[0]);
    }

    @Test
    public void checkPinCodeState() throws CardException {
        CommandAPDU c = new CommandAPDU(TestUtils.buildApdu(new byte[]{(byte) 0x90, (byte) 0x51, 0x00, 0x00}, new byte[]{}));
        ResponseAPDU response = transmitCommand(c);
        assertEquals(0x9000, response.getSW());
        assertEquals("data length", 0, response.getData().length);
    }
}
