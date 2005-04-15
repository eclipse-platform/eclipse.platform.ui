package org.eclipse.debug.internal.core;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LazyInputStream extends FilterInputStream {
    
    private static int TIMEOUT = 100;
    private boolean eofReceived = false;

    public LazyInputStream(InputStream in) {
        super(in);
    }
    

    /* (non-Javadoc)
     * @see java.io.BufferedInputStream#read(byte[], int, int)
     */
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (eofReceived) {
            eofReceived = false; //next read should throw an IOException
            return -1;
        }
        
        long start = System.currentTimeMillis();
        int read = super.read(b, off, len);
        while (read != -1 && read < (len-off) && len-off-read > 0 && System.currentTimeMillis()-start < TIMEOUT) {
            int bytesRead = super.read(b, off+read, len-read);
            if (bytesRead == -1) {
                eofReceived = true;
                break;
            } 
            read += bytesRead;
        }
        return read;
    }

    /* (non-Javadoc)
     * @see java.io.FilterInputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    

}
