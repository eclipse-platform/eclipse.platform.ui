/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * InputStream used to read input from an {@link IOConsole}. 
 * This stream will buffer input that it receives until it has been read.
 * An input stream is available from its {@link IOConsole}.
 * @since 3.1
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 *
 */
public class IOConsoleInputStream extends InputStream {
    /**
     * Buffer to hold data from console until it is read.
     */
    private byte[] input = new byte[100];
    
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
     * The console that this stream is connected to.
     */
    private IOConsole console;
    
    /**
     * The color used to display input in the console.
     */
    private Color color;
    
    /**
     * The font style used to decorate input in the console.
     */
    private int fontStyle = SWT.NORMAL;


    /**
     * Constructs a new input stream on the given console.
     * 
     * @param console I/O console
     */
    IOConsoleInputStream(IOConsole console) {
        this.console = console;
    }
    
    /*
     *  (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public synchronized int read(byte[] b, int off, int len) throws IOException {
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
    public synchronized int read() throws IOException {
        waitForData();
        if (available() == -1) { 
            return -1;
        }
        
        byte b = input[outPointer];
        outPointer++;
        if (outPointer == input.length) {
            outPointer = 0;
        }
        size -= 1;
        return b;
    }
    
    /**
     * Blocks until data is available to be read.
     * Ensure that the monitor for this object is obtained before
     * calling this method.
     */
    private void waitForData() {
        while (size == 0 && !closed) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Appends text to this input stream's buffer.
     * 
     * @param text the text to append to the buffer.
     */
    public synchronized void appendData(String text) {
    	String encoding = console.getEncoding();
        byte[] newData;
        if (encoding!=null)
			try {
				newData = text.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				newData = text.getBytes();	
			}
		else
        	newData = text.getBytes();
        
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
        notifyAll();
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
     * Returns this stream's font style.
     * 
     * @return the font style used to decorate input in the associated console
     */
    public int getFontStyle() {
        return fontStyle;
    }

    /**
     * Sets this stream's font style.
     * 
     * @param newFontStyle the font style to be used to decorate input in the associated console
     */
    public void setFontStyle(int newFontStyle) {
        if (newFontStyle != fontStyle) {
            int old = fontStyle;
            fontStyle = newFontStyle;
            console.firePropertyChange(this, IConsoleConstants.P_FONT_STYLE, new Integer(old), new Integer(fontStyle));
        }
    }
    
    /**
     * Sets the color to used to decorate input in the associated console.
     * 
     * @param newColor the color to used to decorate input in the associated console.
     */
    public void setColor(Color newColor) {
        Color old = color;
        if (old == null || !old.equals(newColor)) {
            color = newColor;
            console.firePropertyChange(this, IConsoleConstants.P_STREAM_COLOR, old, newColor);
        }
    }
    
    /**
     * Returns the color used to decorate input in the associated console
     * 
     * @return the color used to decorate input in the associated console
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
        } else if (size == 0) {
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
    public synchronized void close() throws IOException {
        if(closed) {
            throw new IOException("Input Stream Closed"); //$NON-NLS-1$
        }
        closed = true;
        notifyAll();
        console.streamClosed(this);
    }
}
