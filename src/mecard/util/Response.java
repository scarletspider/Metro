/*
 * Copyright (C) 2013 metro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mecard.util;

import mecard.Protocol;
import mecard.ProtocolPayload;
import mecard.ResponseTypes;
import mecard.customer.Customer;

/**
 * Simple object to order responses.
 * @author metro
 */
public class Response extends ProtocolPayload
{
    protected ResponseTypes code;
    
    public Response()
    {
        code = ResponseTypes.INIT;
    }
    
    public Response(ResponseTypes rt)
    {
        code = rt;
    }
    
    public void setResponse(String s)
    {
        this.addResponse(s);
    }

    public void setCode(ResponseTypes code) 
    {
        this.code = code;
    }
    
    public void setCustomer(Customer c)
    {
        this.payload = c.getPayload();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(code);
        sb.append(Protocol.DELIMITER);
        sb.append(super.toString());
        return sb.toString();
    }
}
