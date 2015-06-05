package com.elka.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kkurdupov
 */
public class CredentialsStorage {

    private static final Logger logger = Logger.getLogger(CredentialsStorage.class.getName());
    private static final String CREDENTIALS_FILENAME = "cred.db";

    private static class CredentialsStorageHolder {

        private static final CredentialsStorage INSTANCE = new CredentialsStorage();
    }
    Credentials credentials;

    private CredentialsStorage() {
    }

    public static CredentialsStorage getInstance() {
        return CredentialsStorageHolder.INSTANCE;
    }

    public boolean saveToFile() {
        boolean result = true;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(CREDENTIALS_FILENAME);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(bos);
                oos.writeObject(credentials);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                result = false;
            } finally {
                try {
                    if (oos != null) {
                        oos.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Unable to close object output stream", ex);
                    result = false;
                }
                try {
                    bos.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Unable to close buffered output stream", ex);
                    result = false;
                }
            }
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "Not found file with credentials - " + CREDENTIALS_FILENAME, ex);
            result = false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Unable to close file output stream", ex);
                result = false;
            }
        }
        return result;
    }

    public void loadFromFile() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(CREDENTIALS_FILENAME);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bis);
                this.credentials = (Credentials) ois.readObject();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                logger.log(Level.SEVERE, "Not found class - " + Credentials.class.getName(), ex);
            } finally {
                try {
                    if (ois != null) {
                        ois.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Unable to close object input stream", ex);
                }
                try {
                    bis.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Unable to close buffered input stream", ex);
                }
            }
        } catch (FileNotFoundException ex) {
            logger.info("Not found file with credentials - " + CREDENTIALS_FILENAME);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Unable to close file input stream", ex);
            }
        }
    }

    public Credentials get() {
        return credentials;
    }

    public void add(Credentials credentials) {
        this.credentials = credentials;
    }

    public boolean isEmpty() {
        return credentials == null;
    }
}
