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
package fr.bmartel.smartcard.passwordwallet.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Helper for local database.
 *
 * @author Bertrand Martel
 */
public class PasswordReaderDbHelper extends SQLiteOpenHelper {

    public final static short TITLE_MAX_SIZE = 32;
    public final static short USERNAME_MAX_SIZE = 64;
    public final static short PASSWORD_MAX_SIZE = 127;

    public static class PasswordEntry implements BaseColumns {
        public static final String TABLE_NAME = "pass";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_PASSWORD = "password";
    }

    /**
     * create table.
     */
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PasswordEntry.TABLE_NAME + " (" +
                    PasswordEntry._ID + " INTEGER PRIMARY KEY," +
                    PasswordEntry.COLUMN_NAME_TITLE + " VARCHAR(" + TITLE_MAX_SIZE + ")," +
                    PasswordEntry.COLUMN_NAME_USERNAME + " VARCHAR(" + USERNAME_MAX_SIZE + ")," +
                    PasswordEntry.COLUMN_NAME_PASSWORD + " VARCHAR(" + PASSWORD_MAX_SIZE + "))";

    /**
     * drop table.
     */
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PasswordEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PasswordReader.db";

    public PasswordReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}