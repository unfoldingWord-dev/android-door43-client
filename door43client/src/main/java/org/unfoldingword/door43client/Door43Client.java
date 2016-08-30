package org.unfoldingword.door43client;

import java.io.File;

/**
 * Created by joel on 8/30/16.
 */
public class Door43Client {

    private final File databaseFile;
    private final File resourceDir;

    /**
     * Initializes the new api client
     * @param databaseFile the path to the sqlite db where information will be indexed
     * @param resourceDir the directory where resource containers will be stored
     */
    public Door43Client(File databaseFile, File resourceDir) {
        this.databaseFile = databaseFile;
        this.resourceDir = resourceDir;

        // TODO: 8/30/16 create new sqlite helper
        // TODO: 8/30/16 create new library
    }
}
