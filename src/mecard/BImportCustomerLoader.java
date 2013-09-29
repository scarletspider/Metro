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
package mecard;

import api.APICommand;
import api.Command;
import api.CommandStatus;
import api.DummyCommand;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mecard.customer.BImportBat;
import mecard.customer.UserFile;
import mecard.exception.BImportException;
import mecard.requestbuilder.BImportRequestBuilder;
import static mecard.requestbuilder.BImportRequestBuilder.BAT_FILE;
import static mecard.requestbuilder.BImportRequestBuilder.DATA_FILE;
import static mecard.requestbuilder.BImportRequestBuilder.FILE_NAME_PREFIX;
import static mecard.requestbuilder.BImportRequestBuilder.HEADER_FILE;

/**
 * This class queues the bimport customers and, run as a timed event, will load 
 * all the customers found in the Customer directory since the last time it ran.
 * This is done because bimport is not meant to run concurrently.
 * java -cp MeCard.jar mecard.BImportCustomerLoader
 * @author Andrew Nisbet <anisbet@epl.ca>
 */
public class BImportCustomerLoader 
{
    /**
     * 
     * @param args 
     */
    public static void main(String args[])
    {
        BImportCustomerLoader loader = new BImportCustomerLoader();
        loader.run();
    }

    private BImportLoadRequestBuilder loadRequestBuilder;
    
    public BImportCustomerLoader()
    {
        this.loadRequestBuilder = new BImportLoadRequestBuilder(true);
    }
    
    /**
     *
     */
    public void run()
    {
        List<String> fileList = getFileList(
                this.loadRequestBuilder.getLoadDir(), 
                BImportRequestBuilder.HEADER_FILE);
        touchHeader(fileList.get(0));
        clean(fileList); // get rid of the header files.
        fileList = getFileList(
                this.loadRequestBuilder.getLoadDir(), 
                BImportRequestBuilder.DATA_FILE);
        Command command = this.loadRequestBuilder.loadCustomers(fileList);
        CommandStatus status = command.execute();
        clean(fileList); // get rid of the data files. All contents are in the main data file.
        System.out.println(new Date() + " LOAD_STDOUT:"+status.getStdout());
        System.out.println(new Date() + " LOAD_STDERR:"+status.getStderr());
        fileList = getFileList(
                this.loadRequestBuilder.getLoadDir(), 
                BImportRequestBuilder.BAT_FILE);
        clean(fileList); // get rid of the bat files.
    }
   
    /**
     *
     * @param loadDir the value of loadDir
     * @param fileSuffix the value of fileSuffix
     */
    protected List<String> getFileList(String loadDir, String fileSuffix)
    {
        List<String> textFiles = new ArrayList<>();
        File dir = new File(loadDir);
        for (File file : dir.listFiles()) 
        {
            if (file.getName().endsWith((fileSuffix))) 
            {
                textFiles.add(file.getAbsolutePath());
            }
        }
        return textFiles;
    }
    
    /**
     * Removes any preexisting header file, creating a new one.
     */
    protected void deletePreExistingHeader()
    {
        // Get rid of the old header if it exists.
        File fTest = new File(this.loadRequestBuilder.getHeaderName());
        if (fTest.exists())
        {
            fTest.delete();
        }
    }

    /**
     * Creates a header file from the data in one of the existing headers. If no
     * header files are present a FileNotFoundException is thrown.
     * 
     * @param fileName
     */
    protected void touchHeader(String fileName)
    {
        deletePreExistingHeader();
        File fSrc  = new File(fileName);
        File fDest = new File(this.loadRequestBuilder.getHeaderName());
        if (fSrc.renameTo(fDest))
        {
            System.out.println("File '" + fDest.getAbsolutePath() + "' created.");
        }
        else
        {
            System.out.println("File '" + fileName + "' could not be moved to '" +
                    fDest.getAbsolutePath() + "'\nThere must be a pre-existing header"
                    + " to act as a template for the bimport header that matches"
                    + " the customer's data files.");
        }
    }

    /**
     * 
     * @param fileList names of all files that are to be deleted.
     */
    protected void clean(List<String> fileList)
    {
        for (String file: fileList)
        {
            File f = new File(file);
            f.delete();  
        }
    }
    
    /**
     *
     */
    final class BImportLoadRequestBuilder extends BImportRequestBuilder
    {
        public BImportLoadRequestBuilder(boolean b)
        {
            super(b);
            // compute header and data file names.
            if (loadDir.endsWith(File.separator) == false)
            {
                loadDir += File.separator;
            }
            if (bimportDir.endsWith(File.separator) == false)
            {
                bimportDir += File.separator;
            } 
            Date today = new Date();
            long longTime = today.getTime();
            batFile    = loadDir + FILE_NAME_PREFIX + longTime + BAT_FILE;
            headerFile = loadDir + FILE_NAME_PREFIX + longTime + HEADER_FILE;
            dataFile   = loadDir + FILE_NAME_PREFIX + longTime + DATA_FILE;
        }
        
        final String getLoadDir()
        {
            return this.loadDir;
        }
        
        final Command loadCustomers(List<String> files)
        {
            File fTest = new File(headerFile);
            if (fTest.exists() == false)
            {
                throw new BImportException(BImportRequestBuilder.class.getName()
                        + " Could not create header file: '" + headerFile + "'.");
            }
            UserFile bimportDataFile = new UserFile(dataFile);
            bimportDataFile.addUserData(getCustomerData(files));
            // if there were no commands return a command that does nothing.
            if (bimportDataFile.isEmpty())
            {
                return new DummyCommand.Builder().setStatus(0).setStdout("Nothing to do.").build(); // empty command.
            }
            fTest = new File(dataFile);
            if (fTest.exists() == false)
            {
                throw new BImportException(BImportRequestBuilder.class.getName()
                        + " Could not create data file: '" + dataFile + "'.");
            }
            // Ok the goal is to get the path to the batch file here with the name.
            BImportBat batch = new BImportBat.Builder(batFile)
                    .setBimportPath(bimportDir)
                    .server(serverName).password(password)
                    .user(userName).database(database)
                    .header(headerFile).data(dataFile)
                    .borrowerTableKey(uniqueBorrowerTableKey).format(bimportVersion).bType(defaultBtype)
                    .mType(mailType).location(location).setIndexed(Boolean.valueOf(isIndexed))
    //                .setDebug(debug) // not used in class yet.
                    .build();
            List<String> bimportBatExec = new ArrayList<>();
            batch.getCommandLine(bimportBatExec);
            APICommand command = new APICommand.Builder().commandLine(bimportBatExec).build();
            return command;
        }

        /**
         * 
         * @param files
         * @return List of the contents of all the files in argument files.
         */
        protected List<String> getCustomerData(List<String> files)
        {
            // for each file open it get the lines append them to the big list and close
            List<String> bigListOfAllCustomerData = new ArrayList<>();
            for (String file: files)
            {
                BufferedReader br = null;
                try 
                {
                    File f = new File(file);
                    if (! f.exists())
                    {
                        continue;
                    }
                    br = new BufferedReader(new FileReader(f));
                    String line;
                    // add all the lines.
                    while ((line = br.readLine()) != null) 
                    {
                       bigListOfAllCustomerData.add(line);
                    }
                    br.close();
                } 
                catch (FileNotFoundException ex)
                {
                    Logger.getLogger(BImportCustomerLoader.class.getName()).log(Level.SEVERE, null, ex);
                } 
                catch (IOException ex)
                {
                    Logger.getLogger(BImportCustomerLoader.class.getName()).log(Level.SEVERE, null, ex);
                } 
                finally
                {
                    try
                    {
                        br.close();
                    } catch (IOException | NullPointerException ex)
                    {
                        return bigListOfAllCustomerData;
                    }
                }
            }
            return bigListOfAllCustomerData;
        }

        protected String getHeaderName()
        {
            return this.headerFile;
        }
    }
}