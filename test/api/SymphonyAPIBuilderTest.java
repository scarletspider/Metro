package api;

import java.util.ArrayList;
import java.util.List;
import json.RequestDeserializer;
import mecard.customer.Customer;
import mecard.customer.FlatUserFormatter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Note on usage: the environment.properties file must have Symphony set in all 
 * ILS interaction methods.
 * @author Andrew Nisbet <anisbet@epl.ca>
 */
public class SymphonyAPIBuilderTest
{
    
    public SymphonyAPIBuilderTest()
    {
    }

    /**
     * Test of getCustomerCommand method, of class SymphonyAPIBuilder.
     */
    @Test
    public void testGetUser()
    {
        System.out.println("==getUser==");
        String userId = "21221012345678";
        String userPin = "64058";
        SymphonyAPIBuilder api = new SymphonyAPIBuilder();
        Response responder = new Response();
        Command command = api.getCustomerCommand(userId, userPin, responder);
        System.out.println("CMD:" + command.toString());
    }
    
    /**
     * Test of getCreateUserCommand method, of class SymphonyAPIBuilder.
     */
    @Test
    public void testCreateUser()
    {
        System.out.println("== createUser ==");
        String custReq =
                "{\"code\":\"GET_CUSTOMER\",\"authorityToken\":\"12345678\",\"userId\":\"21221012345678\",\"pin\":\"64058\",\"customer\":\"{\\\"ID\\\":\\\"21221012345678\\\",\\\"PIN\\\":\\\"64058\\\",\\\"NAME\\\":\\\"Billy, Balzac\\\",\\\"STREET\\\":\\\"12345 123 St.\\\",\\\"CITY\\\":\\\"Edmonton\\\",\\\"PROVINCE\\\":\\\"Alberta\\\",\\\"POSTALCODE\\\":\\\"H0H0H0\\\",\\\"GENDER\\\":\\\"M\\\",\\\"EMAIL\\\":\\\"ilsteam@epl.ca\\\",\\\"PHONE\\\":\\\"7804964058\\\",\\\"DOB\\\":\\\"19750822\\\",\\\"PRIVILEGE_EXPIRES\\\":\\\"20140602\\\",\\\"RESERVED\\\":\\\"X\\\",\\\"DEFAULT\\\":\\\"X\\\",\\\"ISVALID\\\":\\\"Y\\\",\\\"ISMINAGE\\\":\\\"Y\\\",\\\"ISRECIPROCAL\\\":\\\"N\\\",\\\"ISRESIDENT\\\":\\\"Y\\\",\\\"ISGOODSTANDING\\\":\\\"Y\\\",\\\"ISLOSTCARD\\\":\\\"N\\\",\\\"FIRSTNAME\\\":\\\"Balzac\\\",\\\"LASTNAME\\\":\\\"Billy\\\"}\"}";
        RequestDeserializer deserializer = new RequestDeserializer();
        Request request = deserializer.getDeserializedRequest(custReq);
        Customer customer = request.getCustomer();
        FlatUserFormatter formatter = new FlatUserFormatter();
        List<String> expResult = new ArrayList<String>();
        expResult.add("*** DOCUMENT BOUNDARY ***\n");
        expResult.add(".USER_ID.   |a21221012345678\n");
        expResult.add(".USER_FIRST_NAME.   |aBalzac\n");
        expResult.add(".USER_LAST_NAME.   |aBilly\n");
        expResult.add(".USER_PREFERRED_NAME.   |aBilly, Balzac\n");
        expResult.add(".USER_LIBRARY.   |aEPLMNA\n");
        expResult.add(".USER_PROFILE.   |aEPL-METRO\n");
        expResult.add(".USER_PREF_LANG.   |aENGLISH\n");
        expResult.add(".USER_PIN.   |a64058\n");
        expResult.add(".USER_STATUS.   |aOK\n");
        expResult.add(".USER_ROUTING_FLAG.   |aY\n");
        expResult.add(".USER_CHG_HIST_RULE.   |aALLCHARGES\n");
        expResult.add(".USER_PRIV_GRANTED.   |a20130724\n");
        expResult.add(".USER_PRIV_EXPIRES.   |a20140602\n");
        expResult.add(".USER_BIRTH_DATE.   |a19750822\n");
        expResult.add(".USER_CATEGORY2.   |aM\n");
        expResult.add(".USER_ACCESS.   |aPUBLIC\n");
        expResult.add(".USER_ENVIRONMENT.   |aPUBLIC\n");
        expResult.add(".USER_ADDR1_BEGIN.\n");
        expResult.add(".STREET.   |a12345 123 St.\n");
        expResult.add(".CITY/STATE.   |aEdmonton, ALBERTA\n");
        expResult.add(".POSTALCODE.   |aH0H0H0\n");
        expResult.add(".PHONE.   |a7804964058\n");
        expResult.add(".EMAIL.   |ailsteam@epl.ca\n");
        expResult.add(".USER_ADDR1_END.\n");

        List<String> result = formatter.setCustomer(customer);
        for (String s: result)
        {
            System.out.print("=>"+s);
        }
        
        SymphonyAPIBuilder api = new SymphonyAPIBuilder();
        Response response = new Response();
        Command command = api.getCreateUserCommand(customer, response);
        System.out.println("CMD:" + command.toString());
        assertEquals(expResult, result);
    }

    /**
     * Test of getFormatter method, of class SymphonyAPIBuilder.
     */
    @Test
    public void testGetFormatter()
    {
        System.out.println("==getFormatter==");
        SymphonyAPIBuilder instance = new SymphonyAPIBuilder();
        assertTrue(instance.getFormatter() != null);
    }

    /**
     * Test of getCustomerCommand method, of class SymphonyAPIBuilder.
     */
    @Test
    public void testGetCustomer()
    {
        System.out.println("===getCustomer===");
//        String userId = "21221012345678";
//        String userPin = "64058";
//        Response response = new Response();
//        SymphonyAPIBuilder instance = new SymphonyAPIBuilder();
//        String expResult = "/home/metro/bimport/dumpflatuser ";
//        Command result = instance.getCustomerCommand(userId, userPin, response);
//        assertEquals(expResult, result.toString());
    }
}