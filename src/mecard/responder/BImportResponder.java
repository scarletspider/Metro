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

package mecard.responder;

import java.io.File;
import java.util.Properties;
import mecard.ResponseTypes;
import mecard.config.BImportPropertyTypes;
import mecard.config.ConfigFileTypes;
import mecard.config.PropertyReader;
import mecard.util.BImportBat;

/**
 * BImport responder has special capabilities to write files to the local file
 * system and then execute local commands against that file.
 * @since 1.1
 * @author metro
 */
public class BImportResponder extends ResponderStrategy
{
    // Use this to prefix all our files.
    protected final static String FILE_NAME_PREFIX = "metro-";
    protected final static String BAT_FILE = "-bimp.bat";
    protected final static String HEADER_FILE = "-header.txt";
    protected final static String DATA_FILE   = "-data.txt";
    private String bimportDir;
    private String serverName;
    private String password;
    private String userName;
    private String database; // we may need another way to distinguish DBs on a server.
    private String serverAlias;
    private String bimportVersion; // like fm41
    private String defaultBtype; // like bawb
    private String mailType;
    private String location; // branch? see 'lalap'
    private String isIndexed; // "y = NOT indexed"
    private String bimpTableFile; // location of the bimport table config XML file.
    
    private String batFile;
    private String headerFile;
    private String dataFile;

    public BImportResponder(String cmd)
    {
        super(cmd);
        this.state = ResponseTypes.BUSY;
        this.command = splitCommand(cmd);
        Properties bimpProps = PropertyReader.getProperties(ConfigFileTypes.BIMPORT);
        bimportDir = bimpProps.getProperty(BImportPropertyTypes.BIMPORT_DIR.toString());
        serverName = bimpProps.getProperty(BImportPropertyTypes.SERVER.toString());
        password = bimpProps.getProperty(BImportPropertyTypes.PASSWORD.toString());
        userName = bimpProps.getProperty(BImportPropertyTypes.USER.toString());
        database = bimpProps.getProperty(BImportPropertyTypes.DATABASE.toString()); // we may need another way to distinguish DBs on a server.
        serverAlias = bimpProps.getProperty(BImportPropertyTypes.SERVER_ALIAS.toString());
        bimportVersion = bimpProps.getProperty(BImportPropertyTypes.VERSION.toString()); // like fm41
        defaultBtype = bimpProps.getProperty(BImportPropertyTypes.DEFAULT_BTYPE.toString()); // like bawb
        mailType = bimpProps.getProperty(BImportPropertyTypes.MAIL_TYPE.toString());
        location = bimpProps.getProperty(BImportPropertyTypes.LOCATION.toString()); // branch? see 'lalap'
        isIndexed = bimpProps.getProperty(BImportPropertyTypes.IS_INDEXED.toString());
        bimpTableFile = bimpProps.getProperty(BImportPropertyTypes.TABLE_CONFIG.toString());
        
        // compute header and data file names.
        batFile    = bimportDir+File.pathSeparator+FILE_NAME_PREFIX+getTransactionId()+BAT_FILE;
        headerFile = bimportDir+File.pathSeparator+FILE_NAME_PREFIX+getTransactionId()+HEADER_FILE;
        dataFile   = bimportDir+File.pathSeparator+FILE_NAME_PREFIX+getTransactionId()+DATA_FILE;
        
        // create the bat file.
        new BImportBat.Builder(batFile).server(serverName).password(password)
            .user(userName).database(database)
                .header(headerFile).data(dataFile)
            .alias(serverAlias).format(bimportVersion).bType(defaultBtype)
            .mType(mailType).location(location).setIndexed(Boolean.valueOf(isIndexed))
            .build();
    }

    @Override
    public String getResponse()
    {
        // test for the operations that this responder is capable of performing
        // SIP can't create customers, BImport can't query customers.
        StringBuffer responseBuffer = new StringBuffer();
        switch(this.cmdType)
        {
            case CREATE_CUSTOMER: // Customers are added if not exist and modified if they do.
            case UPDATE_CUSTOMER:
                this.state = addModifyCustomer(responseBuffer);
                this.response.add(responseBuffer.toString());
                break;
            default:
                this.state = ResponseTypes.ERROR;
                this.response.add(BImportResponder.class.getName() + 
                        " cannot " + this.cmdType.toString());
        }
        return pack(response);
    }
    
    protected ResponseTypes addModifyCustomer(StringBuffer responseBuffer)
    {
        return ResponseTypes.SUCCESS;
    }

    /**
     * Returns a unique id of the transaction. Each customer has one so each
     * transaction is unique and each set of files won't clobber any other
     * BImportResponder thread.
     * @return the transaction id, which is the second field of the request,
     * which in-turn is a hash of the customer's account salted with the metro
     * server's id.
     */
    private String getTransactionId()
    {
        return this.command.get(1);
    }
}