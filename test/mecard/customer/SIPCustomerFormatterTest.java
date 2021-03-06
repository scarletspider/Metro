/*
 * Metro allows customers from any affiliate library to join any other member library.
 *    Copyright (C) 2013  Edmonton Public Library
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
package mecard.customer;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andrew Nisbet <anisbet@epl.ca>
 */
public class SIPCustomerFormatterTest
{
    private String customerString;
    
    public SIPCustomerFormatterTest()
    {
        this.customerString = "64YYYY      Y   00020130606    115820000000000000000100000000AO|AA21221012345678|AEBilly, Balzac|AQEPLMNA|BZ0025|CA0041|CB0040|BLY|CQY|BV 12.00|BD7 Sir Winston Churchill Square Edmonton, AB T5J 2V4|BEilsteam@epl.ca|BHUSD|PA20140321    235900|PD20050303|PCEPL-THREE|PFM|DB$0.00|DM$0.00|AFUser BLOCKED|AY0AZACC6";
    }

    /**
     * Test of getCustomer method, of class SIPFormatter.
     */
    @Test
    public void testGetCustomer_String()
    {
        System.out.println("== getCustomer ==");
        SIPFormatter instance = new SIPFormatter();
        String expResult = "[\"21221012345678\",\"X\",\"Billy, Balzac\",\"7 Sir Winston Churchill Square\",\""
                + "Edmonton\",\"AB\",\"T5J2V4\",\"M\",\"ilsteam@epl.ca\",\"X\",\"20050303\",\"20140321\",\"X\",\"X\",\"X\",\"X\",\"X\",\"X\",\"X\",\"X\",\"Balzac\",\"Billy\"]";
        Customer result = instance.getCustomer(this.customerString);
        assertEquals(expResult.compareTo(result.toString()), 0);
    }

    /**
     * Test of getCustomer method, of class SIPFormatter.
     */
    @Test
    public void testGetCustomer_List()
    {
        System.out.println("== getCustomer (list) ==");
        List<String> s = new ArrayList<String>();
        s.add(this.customerString);
        SIPFormatter instance = new SIPFormatter();
        // SIP doesn't return the PIN and currently does not return phone number.
        String expResult = "21221012345678\",\"X\",\"Billy, Balzac\",\"7 Sir Winston Churchill Square\",\""
                + "Edmonton\",\"AB\",\"T5J2V4\",\"M\",\"ilsteam@epl.ca\",\"X\",\"20050303\",\"20140321\",\"X\",\"X\",\"X\",\"X\",\"X\",\"X\",\"X\",\"X\",\"Balzac\",\"Billy\"]";
        Customer result = instance.getCustomer(s);
        assertEquals(expResult.compareTo(result.toString()), 0);
    }
}