/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mecard.responder;

import java.util.ArrayList;
import java.util.List;
import mecard.Protocol;
import mecard.QueryTypes;
import mecard.ResponseTypes;

/**
 *
 * @author metro
 */
public abstract class ResponderStrategy
{
    protected List<String> command;
    protected List<String> response;
    protected ResponseTypes state;
    protected final String originalCommand;
    protected final QueryTypes cmdType;
    
    protected ResponderStrategy(String cmd)
    {
        this.state = ResponseTypes.INIT;
        this.command = new ArrayList<String>();
        this.response= new ArrayList<String>();
        this.originalCommand = cmd;
        this.cmdType = Protocol.getCommand(cmd);
    }
    
    /**
     * Split the command on the Protocol's delimiter breaking the command 
     * into chunks. The first element on the list is the command itself which
     * can be ignored since it was already dealt with when this object was 
     * created. The second is the MD5 hash of the query salted with the senders 
     * shared secret. The rest of the elements (if any) are arguments to the 
     * command.
     * @param cmd
     * @return 
     */
    protected static final List<String> splitCommand(String cmd)
    {
        List<String> args = new ArrayList<String>();
        for (String s: cmd.split("|"))
        {
            args.add(s);
        }
        return args;
    }
    
    /**
     * Creates a well formatted response string. The format of the response is:
     * CODE|Optional Text 1|Optional Text 2|...|
     * @param args
     * @return 
     */
    protected final String pack(List<String> args)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.state.toString());
        sb.append(Protocol.DELIMITER);
        for (String s: response)
        {
            sb.append(s);
            sb.append(Protocol.DELIMITER);
        }
        return sb.toString();
    }

    public abstract String getResponse();

    public ResponseTypes getState()
    {
        return this.state;
    }
}