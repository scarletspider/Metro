/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mecard.responder;

import mecard.ResponseTypes;
import mecard.responder.ResponderStrategy;

/**
 *
 * @author andrew
 */
public class SIP2Responder extends ResponderStrategy
{

    public SIP2Responder(String command)
    {
        super(command);
        this.state = ResponseTypes.BUSY;
        this.command = splitCommand(command);
    }

    @Override
    public String getResponse()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
