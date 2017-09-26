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

/**
 * Pin code response data model.
 *
 * @author Bertrand Martel
 */
public class PinCodeResult {

    /**
     * pin code state.
     */
    private boolean valid;

    /**
     * number of retry.
     */
    private int retry;

    public PinCodeResult(boolean valid, int retry) {
        this.valid = valid;
        this.retry = retry;
    }

    public boolean isValid() {
        return valid;
    }

    public int getRetry() {
        return retry;
    }
}
