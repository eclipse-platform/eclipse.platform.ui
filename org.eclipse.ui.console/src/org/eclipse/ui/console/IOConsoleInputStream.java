/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * InputStream used to read input from an IOConsole. 
 * This stream will buffer input that it receives from the console
 * until it has been read.
 * 
 * @since 3.1
 *
 */
public class IOConsoleInputStream extends InputStream {
    /**
     * Buffer to hold data from console until it is read.
     */
    private byte[] input = new byte[1024*2];
    
    /**
     * Location in the buffer that the next byte of data from the
     * console should be stored.
     */
    private int inPointer = 0;
    
    /**
     * Location in the buffer that the next byte of data read from
     * this stream should come from.
     */
    private int outPointer = 0;
    
    /**
     * The number of bytes of real data currently in the buffer. 
     */
    private int size = 0;
    
    /**
     * Flag to indicate that EOF has been sent already.
     */
    private boolean eofSent = false;
    
    /**
     * Flag to indicate that the stream has been closed.
     */
    private boolean closed = false;
    
    /**
     * Flag to indicate that the console has been disconnected from this
     * inputStream. EOF will sent once data remaining in buffer has
     * been read.
     */
    private boolean disconnected;
    
    /**
     * The console that this stream is connected to.
     */
    private IOConsole console;
    
    /**
     * The color used to display input in the console.
     */
    private Color color;
    
    /**
     * The font stye used to decorate input in the console.
     */
    private int fontStyle = SWT.NORMAL;


    IOConsoleInputStream(IOConsole console) {
        this.console = console;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        synchronized(input) {
            waitForData();
	        if (available() == -1) {
	            return -1;
	        }
        
            int toCopy = Math.min(len, size);
            if(input.length-outPointer > toCopy) {
                System.arraycopy(input, outPointer, b, off, toCopy);
                outPointer += toCopy;
                size -= toCopy;
            } else {
                int bytesToEnd = input.length-outPointer;
                System.arraycopy(input, outPointer, b, off, bytesToEnd);
                System.arraycopy(input, 0, b, off+bytesToEnd, toCopy-bytesToEnd);
                outPointer = toCopy-bytesToEnd;
                size -=toCopy;
            }
            return toCopy;
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /*
     *  (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        synchronized(input) {
            waitForData();
	        if (available() == -1) { 
	            return -1;
	        }
            
            byte b = input[outPointer];
            outPointer++;
            if (outPointer == input.length) {
                outPointer = 0;
            }
            return b;
        }
    }
    
    /**
     * blocks until data is available to be read.
     */
    private void waitForData() {
        while (size == 0 && !disconnected && !closed) {
            try {
                input.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * appends data to this input stream's buffer
     * @param text The data to append to the buffer.
     */
    public void appendData(String text) {
        byte[] newData = text.getBytes();
        synchronized(input) {
            while(input.length-size < newData.length) {
                growArray();
            }
            
            if (size == 0) { //inPointer == outPointer
                System.arraycopy(newData, 0, input, 0, newData.length);
                inPointer = newData.length;
                size = newData.length;
                outPointer = 0;
            } else if (inPointer < outPointer || input.length - inPointer > newData.length) {
                System.arraycopy(newData, 0, input, inPointer, newData.length);
                inPointer += newData.length;
                size += newData.length;
            } else {
                System.arraycopy(newData, 0, input, inPointer, input.length-inPointer);
                System.arraycopy(newData, input.length-inPointer, input, 0, newData.length-(input.length-inPointer));
                inPointer = newData.length-(input.length-inPointer);
                size += newData.length;
            }
            
            if (inPointer == input.length) {
                inPointer = 0;
            }
            input.notifyAll();
        }
    }
    
    /**
     * Enlarges the buffer.
     */
    private void growArray() {
        byte[] newInput = new byte[input.length+1024];
        if (outPointer < inPointer) {
            System.arraycopy(input, outPointer, newInput, 0, size);
        } else {
            System.arraycopy(input, outPointer, newInput, 0, input.length-outPointer);
            System.arraycopy(input, 0, newInput, input.length-outPointer, inPointer);
        }
        outPointer = 0;
        inPointer = size;
        input = newInput;
        newInput = null;
    }

    /**
     * Returns this console's font style.
     * @return The font style used to decorate input in the associated console
     */
    public int getFontStyle() {
        return fontStyle;
    }

    /**
     * Sets the font style
     * @param newFontStyle The font style to be used to decorate input in the associated console
     */
    public void setFontStyle(int newFontStyle) {
        if (newFontStyle != fontStyle) {
            int old = fontStyle;
            fontStyle = newFontStyle;
            console.firePropertyChange(this, IOConsole.P_FONT_STYLE, new Integer(old), new Integer(fontStyle));
        }
    }
    
    /**
     * Sets the color to used to decorate input in the associated console.
     * @param newColor The color to used to decorate input in the associated console.
     */
    public void setColor(Color newColor) {
        Color old = color;
        if (old == null || !old.equals(newColor)) {
            color = newColor;
            console.firePropertyChange(this, IOConsole.P_STREAM_COLOR, old, newColor);
        }
    }
    
    /**
     * Returns the color used to decorate input in the associated console
     * @return The color used to decorate input in the associated console
     */
    public Color getColor() {
        return color;
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        if (closed && eofSent) {
            throw new IOException("Input Stream Closed"); //$NON-NLS-1$
        } else if (size == 0 && disconnected) {
            if (!eofSent) {
                eofSent = true;
                return -1;
            } 
            throw new IOException("Input Stream Closed"); //$NON-NLS-1$
        }
        
        return size;
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        if(closed) {
            throw new IOException("Input Stream Closed"); //$NON-NLS-1$
        }
        closed = true;
        synchronized(input) {
            input.notifyAll();
        }
        console = null;
    }

    /**
     *  Disconnects the console from this stream. 
     */
    public void disconnect() {
        disconnected = true;
        synchronized(input) {
            input.notifyAll();
        }
        console = null;
    }
}
