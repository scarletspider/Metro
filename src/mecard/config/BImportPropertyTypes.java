/*
 * Metro allows customers from any affiliate library to join any other member library.
 *    Copyright (C) 2013  Andrew Nisbet
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 *
 */
package mecard.config;

/**
 * Mandatory property types of the bimp config file. Note that there are
 * fields 
 * @author metro
 */
public enum BImportPropertyTypes
{
    LOAD_DIR("load-dir"),     // Directory where to find customer files to load.
    BIMPORT_DIR("bimport-dir"), // Directory where to find customer files to load.
    SERVER("server"),         // '/s' switch the name of the server.
    PASSWORD("password"),     // password for server function.
    USER("user"),             // bimport user, should be admin privileges in Horizon.
    DATABASE("database"),     // Name of the database.
    SERVER_ALIAS("server-alias"), // This is identified as the server alias.
    VERSION("version"),       // like fm41 any other version is currently untested.
    DEFAULT_BTYPE("btype"),   // like bawb
    MAIL_TYPE("mail-type"),   // dom seems to be the default type.
    LOCATION("location"),     // Like lalap
    IS_INDEXED("indexed");    // "y = NOT indexed"
//    DATE_FORMAT("date-format"); // the prefered date formatting. now in Environment file.
    
    private String type;

    private BImportPropertyTypes(String s)
    {
        this.type = s;
    }

    @Override
    public String toString()
    {
        return this.type;
    }
}
