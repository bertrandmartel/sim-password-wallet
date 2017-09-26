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
package fr.bmartel.smartcard.passwordwallet.application;

import android.app.Application;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.github.orangegangsters.lollipin.lib.managers.LockManager;

import org.simalliance.openmobileapi.SEService;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import fr.bmartel.smartcard.passwordwallet.CustomPinActivity;
import fr.bmartel.smartcard.passwordwallet.R;
import fr.bmartel.smartcard.passwordwallet.inter.IServiceConnection;
import fr.bmartel.smartcard.passwordwallet.uicc.ApduResponse;
import fr.bmartel.smartcard.passwordwallet.uicc.Uicc;

/**
 * Password Application which bounds to SEService.
 *
 * @author Bertrand Martel
 */
public class PasswordApplication extends Application implements SEService.CallBack {

    private final static String TAG = PasswordApplication.class.getSimpleName();

    /**
     * SEService used to interact with SmartCard API.
     */
    private SEService seService;

    /**
     * UICC object used to manage UICC I/O.
     */
    private Uicc mUicc;

    /**
     * thread pool.
     */
    private ScheduledExecutorService mExecutor;

    /**
     * working mode.
     */
    public byte mode = 0x00;

    //store encrypted passwords on application or on SIM card
    public final static byte MODE_APP_STORAGE = 0x01;
    public final static byte MODE_SIM_STORAGE = 0x02;

    /**
     * Value for Global Platform secured state.
     */
    public static final byte CARD_SECURED = 15;

    /**
     * service connection object.
     */
    private IServiceConnection mServiceConnection;

    private boolean connected = false;

    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();

        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, CustomPinActivity.class);
        lockManager.getAppLock().setShouldShowForgot(false);
        lockManager.getAppLock().setLogoId(R.drawable.sim_logo);

        initSeService();
        mExecutor = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void onTerminate() {
        mUicc.closeChannel();
        if (seService != null && seService.isConnected()) {
            seService.shutdown();
        }
        super.onTerminate();
    }

    public void setServiceConnection(IServiceConnection callback) {
        mServiceConnection = callback;
    }

    /**
     * Bind to SEService.
     */
    private void initSeService() {
        try {
            Log.v(TAG, "creating SEService object");
            seService = new SEService(this, this);
            mUicc = new Uicc(seService);
        } catch (SecurityException e) {
            Log.e(TAG, "Binding not allowed, uses-permission org.simalliance.openmobileapi.SMARTCARD?");
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Connection callback for SEService.
     *
     * @param service
     */
    public void serviceConnected(SEService service) {
        Log.v(TAG, "serviceConnected()");
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //open logical channel
                    mUicc.openChannel();
                } catch (final SecurityException e) {
                    Log.e(TAG, "SecurityException", e);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (e.getMessage().contains("no APDU access allowed")) {
                                Toast.makeText(PasswordApplication.this, "Application not authorized.\nCheck Access Control Rules on this card", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PasswordApplication.this, "SIM card not inserted", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (final IOException e) {
                    Log.e(TAG, "IOException", e);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (e.getMessage().contains("iccOpenLogicalChannel failed")) {
                                Toast.makeText(PasswordApplication.this, "smartcard has been disconnected or applet not installed ?", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(PasswordApplication.this, "SIM card not inserted", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                if (mServiceConnection != null) {
                    mServiceConnection.onServiceConnected();
                }
                connected = true;
            }
        });
    }

    /**
     * Get the current mode from UICC.
     */
    public void refreshMode() {
        ApduResponse modeRes = mUicc.getMode();
        if (!modeRes.isSuccessful()) {
            Log.e(TAG, "get mode failed");
        } else {
            mode = modeRes.getData()[0];
        }
    }

    public Uicc getUicc() {
        return mUicc;
    }

    public ScheduledExecutorService getExecutor() {
        return mExecutor;
    }

    public void setMode(byte mode) {
        this.mode = mode;
    }

    /**
     * Check if card is secured from UICC (eg if the pin code has been already set before).
     *
     * @return
     */
    public boolean isCardSecured() {
        ApduResponse cardStateRes = mUicc.getCardState();
        if (cardStateRes.getData().length != 0) {
            return (cardStateRes.getData()[0] == CARD_SECURED);
        }
        return true;
    }

    public boolean isPinCodeChecked() {
        return mUicc.getPinCodeState().isSuccessful();
    }

    public boolean isConnected() {
        return connected;
    }
}
