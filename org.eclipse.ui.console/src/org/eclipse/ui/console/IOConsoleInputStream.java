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
 * InputStream used to read input from an IOConsole
 * @since 3.1
 *
 */
public class IOConsoleInputStream extends InputStream {
    
    private byte[] input = new byte[1024*2];
    private int inPointer = 0;
    private int outPointer = 0;
    private int size = 0;
    
    private boolean eofSent = false;
    private boolean closed = false;
    private boolean terminated;
    private IOConsole console;
    private Color color;
    private int fontStyle = SWT.NORMAL;

    private String streamId;

    IOConsoleInputStream(IOConsole console) {
        this.console = console;
        streamId = "IO_CONSOLE_INPUT_STREAM"; //$NON-NLS-1$
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        if (available() == -1) {
            return -1;
        }
        
        synchronized(input) {
            waitForData();
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
    
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read() throws IOException {
        if (available() == -1) { 
            return -1;
        }
        
        synchronized(input) {
           waitForData();
            
            byte b = input[outPointer];
            outPointer++;
            if (outPointer == input.length) {
                outPointer = 0;
            }
            return b;
        }
    }
    
    private void waitForData() {
        while (size == 0) {
            try {
                input.wait();
            } catch (InterruptedException e) {
            }
        }
    }

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

    public String getStreamId() {
        return streamId;
    }
    
    public int getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(int newFontStyle) {
        if (newFontStyle != fontStyle) {
            int old = fontStyle;
            fontStyle = newFontStyle;
            console.firePropertyChange(this, IOConsole.P_FONT_STYLE, new Integer(old), new Integer(fontStyle));
        }
    }
    
    public void setColor(Color newColor) {
        Color old = color;
        if (old == null || !old.equals(newColor)) {
            color = newColor;
            console.firePropertyChange(this, IOConsole.P_STREAM_COLOR, old, newColor);
        }
    }
    
    public Color getColor() {
        return color;
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        if (closed) {
            throw new IOException("Input Stream Closed"); //$NON-NLS-1$
        } else if (terminated) {
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
    }




}
