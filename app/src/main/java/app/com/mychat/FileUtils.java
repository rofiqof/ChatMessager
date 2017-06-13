package app.com.mychat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by isquare3 on 12/26/16.
 */

public class FileUtils
{

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FileUtils() { }

    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;



    /*public static String readFileToByteArray
            (
            File file, String encoding) throws IOException {
        InputStream in = new java.io.FileInputStream(file);
        try
        {
            return IOUtils.toString(in, encoding);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }*/
}
