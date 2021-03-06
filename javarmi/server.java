

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


/*
 * Class - RMI Server Class
 * @author Rohit Tirmanwar (G01030038)
 *
 */
public class server extends UnicastRemoteObject implements serverInterface  {


    /*
     * Constructor
     */
    server() throws RemoteException {

    }

    /**
     * Stops the RMI server
     */
    private String shutDown() {
        try {
            Naming.unbind("rmi://localhost/FileServer");
            return "Stopped the server successfully";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error 205: Error while shutting down the server";
    }

    /**
     * Processess common client requests like dir, mkdir, rmdir, rm etc.
     */
    public String processCommand(String command) throws RemoteException {

        String retMessage = "Error 400: INVALID COMMAND FROM CLIENT! Please try again with valid command...";
        try {

            String[] commands = command.split(":");
            if(commands.length <= 0) {
                return retMessage;
            }

            // Validate and accept the right command
            switch (commands[0]) {
                case "dir": {
                    if (commands.length <= 1) {
                        retMessage = this.displayFilesOfDirectory("./");
                    } else {
                        retMessage = this.displayFilesOfDirectory(commands[1]);
                    }
                    break;
                }
                case "shutdown": {
                    retMessage = this.shutDown();
                    break;
                }
                case "mkdir": {
                    if (commands.length > 1) {
                        retMessage = this.createDir(commands[1]);
                    }
                    break;
                }
                case "rmdir": {
                    if (commands.length > 1) {
                        retMessage = this.removeDir(commands[1]);
                    }
                    break;
                }
                case "rm": {
                    if (commands.length > 1) {
                        retMessage = this.removeFile(commands[1]);
                    }
                    break;
                }
                default: {
                    System.out.println("Error 400: INVALID COMMAND FROM CLIENT! Please try again with valid command...");
                    break;
                }
            }

        }
        catch(Exception e) {
            // handle Exception
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return retMessage;

    }

    /**
     * Returns the length of bytes of the file, if exists, else returns -1
     */
    public int getServerFileLength(String filePath) throws RemoteException {
        int fileSize = -1;
        try {

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();

            if(0 == filePath.indexOf("/") || 0 == filePath.indexOf("\\")) {
                filePath = filePath.substring(1);
            }

            filePath = s + "/" + filePath;

            File file = new File(filePath);
            if (file.exists()) {
                fileSize = (int)file.length();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return fileSize;
    }

    /**
     * returns bytes of data of the file if exists. else returns null
     */
    public byte[] downloadFile(String filePath, int bytesDownloaded) throws RemoteException {

        byte[] bytesArr = null;
        try {

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();

            if(0 == filePath.indexOf("/") || 0 == filePath.indexOf("\\")) {
                filePath = filePath.substring(1);
            }

            filePath = s + "/" + filePath;

            // Create the file object and check if the file existss
            File file = new File(filePath);
            if (file.exists()) {

                FileInputStream fis = new FileInputStream(file);

                // skip the data if already dowloaded
                if (bytesDownloaded > 0) {
                    fis.skip(bytesDownloaded);
                }

                // read the bytes of data
                bytesArr = new byte[1024];
                int val = fis.read(bytesArr);
                if (val <= 0) {
                    bytesArr = null;
                }

                // close the file
                fis.close();
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return bytesArr;

    }

    /**
     * Recieves the data bytes from client and saves into a file
     */
    public boolean uploadFile(String filePath, byte[] bytes, boolean isAppend) throws RemoteException {

        try {

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();

            if(0 == filePath.indexOf("/") || 0 == filePath.indexOf("\\")) {
                filePath = filePath.substring(1);
            }

            filePath = s + "/" + filePath;

            // Create file object
            File file = new File(filePath);

            // Open a file in append/create mode
            FileOutputStream fos = new FileOutputStream(file, isAppend);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            // write data to the file
            bos.write(bytes, 0, bytes.length);
            bos.flush();

            // close the file
            fos.close();

            return true;

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /*
     * Sends the dir information of given path to client
     */

    private String displayFilesOfDirectory(String dirPath) {

        String retValue = "";
        try {
            if(0 == dirPath.indexOf("/") || 0 == dirPath.indexOf("\\")) {
                dirPath = dirPath.substring(1);
            }

            File theFile = new File(dirPath);
            String fileNames = "";
            if (theFile.exists()) {
                String[] files = new File(dirPath).list();
                fileNames = "Root Directory: /";
                for (String file : files) {
                    fileNames = fileNames + "\n" + file;
                }
                retValue = fileNames;
            }
            else {
                // DIRECTORY DOESNOT EXISTS
                retValue = "Error-202: Directory not found";
                return retValue;
            }
        } catch(Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return retValue;
    }

    /*
     * Deletes a file from server location
     */
    private String removeFile(String filePath) {
        String retValue = "";

        try {

            if(0 == filePath.indexOf("/") || 0 == filePath.indexOf("\\")) {
                filePath = filePath.substring(1);
            }

            File theFile = new File(filePath);

            if (theFile.exists()) {
                if (theFile.listFiles() != null) {
                    if (theFile.listFiles().length > 0) {
                        retValue = "Error-201: File not found";
                        return retValue;
                    }
                }
                theFile.delete();
                retValue = "Successfully deleted the file.";
                return retValue;

            } else {
                retValue = "Error-201: File not found";
                return retValue;
            }
        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return retValue;

    }

    /*
     * Deletes a directory from server location
     */

    private String removeDir(String dirPath) {

        String retValue = "";
        try {

            if(0 == dirPath.indexOf("/") || 0 == dirPath.indexOf("\\")) {
                dirPath = dirPath.substring(1);
            }

            File theDir = new File(dirPath);

            if (theDir.exists()) {
                if (theDir.listFiles().length > 0) {
                    retValue = "Error-203: Directory is not empty";
                }
                else {
                    theDir.delete();
                    retValue = "Successfully deleted the directory.";
                }
            } else {
                retValue = "Error-202: Directory not found";
            }
        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return retValue;

    }

    /*
    * Creates a new directory from server location
    */
    private String createDir(String dirPath) {

        String retVal = "";
        try {
            if(0 == dirPath.indexOf("/") || 0 == dirPath.indexOf("\\")) {
                dirPath = dirPath.substring(1);
            }

            File theDir = new File(dirPath);

            if (!theDir.exists()) {
                theDir.mkdir();

                // send success code to client
                retVal = "Successfully created the directory.";

            } else {
                retVal = "Error-210: Directory already exists";
            }

        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return retVal;
    }

    /*
     * Server main Function
     */
    public static void main(String[] args) {

        try {

            if (args.length >= 1 && 0 == args[0].compareTo("start")) {
                server s1 = new server();
                Naming.rebind("rmi://localhost/FileServer", s1);
                System.out.println("Server is running on localhost...");
            } else {
                System.out.println("Please use the right command to start the server. use server start");
            }
        }
        catch (Exception e) {
            System.out.println("FileServer Server has a problem.");
            // EXCEPTION HANDLING
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


}