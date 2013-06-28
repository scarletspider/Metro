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
package site.mecard;

import epl.EPLPolicy;
import java.text.ParseException;
import java.util.Properties;
import mecard.Exception.UnsupportedLibraryException;
import mecard.Protocol;
import mecard.config.ConfigFileTypes;
import mecard.config.LibraryPropertyTypes;
import mecard.config.PropertyReader;
import mecard.customer.Customer;
import mecard.customer.CustomerFieldTypes;
import mecard.util.DateComparer;

/**
 * This class needs to be sub-classed by all libraries. All customer's must meet 
 * the defined MeCard policy rules. They must be resident, not reciprocal, must
 * be of minimum age of 18, must have an email address, must be in good standing
 * with their home library, must have all mandatory account information present
 * and valid (within reason), and all must have a valid membership expiry date.
 * @author Andrew Nisbet <anisbet@epl.ca>
 */
public abstract class MeCardPolicy
{

    public final static int MINIMUM_YEARS_OF_AGE = 18;
    public final static int MINIMUM_EXPIRY_DAYS = 1;
    public final static int MAXIMUM_EXPIRY_DAYS = 365;
    protected static boolean DEBUG;

    public static EPLPolicy getInstanceOf(boolean debug)
    {
        DEBUG = debug;
        Properties props = PropertyReader.getProperties(ConfigFileTypes.ENVIRONMENT);
        String libCode = props.getProperty(LibraryPropertyTypes.LIBRARY_CODE.toString());
        if (libCode.equalsIgnoreCase(MemberTypes.EPL.name()))
        {
            return new EPLPolicy(DEBUG);
        } else
        {
            throw new UnsupportedLibraryException(libCode);
        }
    }

    /**
     * Each library must decide how they compute if a customer is a resident
     * customer. Usually this is done by comparing the btype of profile of the
     * customer.
     *
     * @param customerData the value of customerData
     * @param status the value of status
     * @return the boolean
     */
    public abstract boolean isResident(Customer customer, String meta);

    /**
     * Each library must decide how they compute if a customer is a reciprocal
     * customer. Usually this is done by comparing the btype of profile of the
     * customer.
     *
     * @param status the value of status
     * @return the boolean
     */
    public abstract boolean isReciprocal(Customer customer, String meta);

    /**
     *
     * @param customerFormatter the value of customerFormatter
     * @param status the value of status
     * @return the boolean
     */
    public abstract boolean isInGoodStanding(Customer customer, String meta);

    /**
     * Another way to compute the customer age. If the library doesn't collect
     * DOB for customers, or they do it inconsistently this method provides
     * checking of the customers profile (or btype) as a test for minimum age.
     * If your library doesn't have birth dates for customers, or they are kept
     * inconsistently, override this method in your library's strategy so that
     * the customer can always be checked for age restrictions.
     *
     * @param customer the customer as a list of fields formatted from raw ILS
     * query.
     * @param meta The extra customer data that the ILS returned when it was
     * queried with getCustomer, but is not required by MeCard customer
     * creation. Things like PROFILE and bType.
     * @return true if customer is of minimum age and false otherwise.
     */
    public abstract boolean isMinimumAge(Customer customer, String meta);

    /**
     * Tests if the customer is of minimum age. If a date field is available
     * this method will compute how many years old the customer is. If the date
     * field is empty, compute using the abstract method isMinimumAge().
     *
     * @param customer
     * @param meta The extra customer data that the ILS returned when it was
     * queried with getCustomer, but is not required by MeCard customer
     * creation. Things like PROFILE and bType.
     * @return true if the customer was of minimum age and false otherwise.
     */
    public boolean isMinimumAgeByDate(Customer customer, String meta)
    {
        String dateOfBirth = customer.get(CustomerFieldTypes.DOB);
        if (dateOfBirth.compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG)
            {
                System.out.println("customer " + customer.get(CustomerFieldTypes.ID)
                        + " failed minimum age requirement.");
            }
            return false;
        }
        try
        {
            int yearsOld = DateComparer.getYearsOld(dateOfBirth);
            if (yearsOld >= MeCardPolicy.MINIMUM_YEARS_OF_AGE)
            {
                customer.set(CustomerFieldTypes.ISMINAGE, Protocol.TRUE);
                return true;
            }
        } catch (ParseException ex)
        {
            if (DEBUG)
            {
                System.out.println("customer " + customer.get(CustomerFieldTypes.ID)
                        + " tested but failed parse DOB.");
            }
            return false; // no longer an issue to not have a date. Some libraries don't collect them.
        }
        return false;
    }

    /**
     * Tests and sets the customer's email flag.
     *
     * @param customer
     * @param meta The extra customer data that the ILS returned when it was
     * queried with getCustomer, but is not required by MeCard customer
     * creation. Things like PROFILE and bType.
     * @return true if the customer has an email and false otherwise.
     */
    public boolean isEmailable(Customer customer, String meta)
    {
        if (customer.get(CustomerFieldTypes.EMAIL).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG)
            {
                System.out.println("customer " + customer.get(CustomerFieldTypes.ID)
                        + " failed email requirement.");
            }
            return false;
        }
        return true;
    }

    /**
     * Tests and sets customer is valid flag.
     *
     * @param customer
     * @param status
     * @return true if the customer is
     */
    public boolean isValidCustomerData(Customer customer)
    {
        // Test customer fields that they are somewhat valid.
        if (customer.get(CustomerFieldTypes.ID).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG) System.out.println("customer failed barcode requirement.");
            return false;
        }
        if (customer.get(CustomerFieldTypes.PIN).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG) System.out.println("customer "+customer.get(CustomerFieldTypes.ID)
                    +" failed pin requirement.");
            return false;
        }
        if (customer.get(CustomerFieldTypes.EMAIL).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG) System.out.println("customer "+customer.get(CustomerFieldTypes.ID)
                    +" failed email requirement.");
            return false;
        }
        if (customer.get(CustomerFieldTypes.NAME).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG) System.out.println("customer "+customer.get(CustomerFieldTypes.ID)
                    +" failed name requirement.");
            return false;
        }
        if (customer.get(CustomerFieldTypes.PRIVILEGE_EXPIRES).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG) System.out.println("customer "+customer.get(CustomerFieldTypes.ID)
                    +" failed expiry requirement.");
            return false;
        }
        if (customer.get(CustomerFieldTypes.STREET).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG) System.out.println("customer "+customer.get(CustomerFieldTypes.ID)
                    +" failed address: street requirement.");
            return false;
        }
        if (customer.get(CustomerFieldTypes.CITY).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG) System.out.println("customer "+customer.get(CustomerFieldTypes.ID)
                    +" failed address: city requirement.");
            return false;
        }
        if (customer.get(CustomerFieldTypes.PROVINCE).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG) System.out.println("customer "+customer.get(CustomerFieldTypes.ID)
                    +" failed address: province requirement.");
            return false;
        }
        if (customer.get(CustomerFieldTypes.POSTALCODE).compareTo(Protocol.DEFAULT_FIELD) == 0)
        {
            if (DEBUG) System.out.println("customer "+customer.get(CustomerFieldTypes.ID)
                    +" failed address: postal code requirement.");
            return false;
        }

        return true;
    }

    /**
     * Tests if the customer has a valid expiry time limit.
     *
     * @param customer
     * @param status
     * @return true if customer has at least 1 day expiry and false otherwise.
     */
    public boolean isValidExpiryDate(Customer customer, String meta)
    {
        String expiryDate = customer.get(CustomerFieldTypes.PRIVILEGE_EXPIRES);
        // TODO set Max expiry, no greater than 365 days.
        try
        {
            int expiryDays = DateComparer.getDaysUntilExpiry(expiryDate);
            if (expiryDays >= MeCardPolicy.MINIMUM_EXPIRY_DAYS)
            {
                return true;
            }
        } catch (ParseException ex)
        {
            return false;
        }
        return false;
    }
    
    /**
     * When a customer reports a lost card, the home library should set this flag
     * on the customer's account. The MeCard web site can also tell if a previously
     * registered customer is returning to register a lost card. If so, the customer's
     * lost card flag should be set and it is upto the library what they do with it.
     * @param customer The customer information as will be inserted into the guest ILS.
     * @param meta additional information from the ILS that is not sent to the guest
     * library, stuff like their profile and number of holds etc.
     * @return true if this account is a lost card.
     * @see mecard.customer.Customer
     */
    public abstract boolean isLostCard(Customer customer, String meta);
}