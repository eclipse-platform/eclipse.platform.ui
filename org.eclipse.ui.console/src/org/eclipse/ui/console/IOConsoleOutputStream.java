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
import java.io.OutputStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.internal.console.IOConsolePartitioner;

/**
 * OutputStream used to write to an IOConsole.
 * 
 * @since 3.1
 */
public class IOConsoleOutputStream extends OutputStream {
    /**
     * Flag indicating whether this stream has been closed.
     */
    private boolean closed = false;

    /**
     * The console's document partitioner.
     */
    private IOConsolePartitioner partitioner;
    
    /**
     * The console this stream is attached to.
     */
    private IOConsole console;
    
    /**
     * Flag indicating that the console should be activated when data
     * is written to this stream.
     */
    private boolean activateOnWrite = false;
    
    /**
     * The color used to decorate data written to this stream.
     */
    private Color color;
    
    /**
     * The font style used to decorate data written to this stream.
     */
    private int fontStyle;

    private String fEncoding;
    private String fDefaultEncoding = WorkbenchEncoding.getWorkbenchDefaultEncoding();
    
    IOConsoleOutputStream(IOConsole console) {
        this.console = console;
        this.partitioner = (IOConsolePartitioner) console.getPartitioner();
    }

    /**
     * Returns the font style used to decorate data written to this stream
     * @return The font style used to decorate data written to this stream
     */
    public int getFontStyle() {
        return fontStyle;
    }
    
    /**
     * Sets the font style to be used to decorate data written to this stream
     * @param newFontStyle The font style to be used to decorate data written to this stream
     */
    public void setFontStyle(int newFontStyle) {
        if (newFontStyle != fontStyle) {
            int old = fontStyle;
            fontStyle = newFontStyle;
            console.firePropertyChange(this, IConsoleConstants.P_FONT_STYLE, new Integer(old), new Integer(fontStyle));
        }
    }
    
    /**
     * Returns the value of activateOnWrite
     * @return true if console is activated automatically when data is written to this stream, false if the 
     * console is not activated by data being written to this stream.
     */
    public boolean isActivateOnWrite() {
        return activateOnWrite;
    }

    /**
     * Sets the value of activateOnWrite
     * @param activateOnWrite true if the console should be activated when data is written to this
     * stream, false if it should not be activated.
     */
    public void setActivateOnWrite(boolean activateOnWrite) {
        this.activateOnWrite = activateOnWrite;
    }
    
	/**
	 * Sets the color of this message stream
	 * 
	 * @param color color of this message stream, possibly <code>null</code>
	 */
	public void setColor(Color newColor) {
		Color old = color;
		if (old == null || !old.equals(newColor)) {
		    color = newColor;
		    console.firePropertyChange(this, IConsoleConstants.P_STREAM_COLOR, old, newColor);
		}
	}
	
	/**
	 * Returns the color of this message stream, or <code>null</code>
	 * if default.
	 * 
	 * @return the color of this message stream, or <code>null</code>
	 */
	public Color getColor() {
	    return color;
	}

	
	/**
	 * Returns the console this stream is connected to.
	 * 
	 * @return the console this stream is connected to
	 */
	public Object getConsole() {
        return console;
    }
	
	/*
	 *  (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
    public synchronized void close() throws IOException {
        if(closed) {
            throw new IOException("Output Stream is closed"); //$NON-NLS-1$
        }
        console.streamClosed(this);
        closed = true;
        console = null;
        partitioner = null;
    }

    /*
     *  (non-Javadoc)
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws IOException {
        if(closed) {
            throw new IOException("Output Stream is closed"); //$NON-NLS-1$
        }
    }
    
    /*
     *  (non-Javadoc)
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if (fEncoding!=null && !fEncoding.equals(fDefaultEncoding)) {
            encodedWrite(new String(b, off, len, fEncoding));
        } else {
            encodedWrite(new String(b, off, len));
        }
    }
    /*
     *  (non-Javadoc)
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    /*
     *  (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException {
        write(new byte[] {(byte)b}, 0, 1);
    }    
    
    /**
     * Writes a String to the attached console.
     * @param str The string to write to the attached console.
     * @throws IOException if the stream is closed.
     */
    public synchronized void write(String str) throws IOException {
        if (fEncoding!=null && !fEncoding.equals(fDefaultEncoding)) {
	        byte[] defaultBytes = str.getBytes();
	        str = new String(defaultBytes, fEncoding);
        }
        encodedWrite(str);
    }
    
    private void encodedWrite(String encodedString) throws IOException {
        if(closed) {
            throw new IOException("Output Stream is closed"); //$NON-NLS-1$
        }
        partitioner.streamAppended(this, encodedString);
        if (activateOnWrite) {
            console.activate();
        }
    }

    /**
     * @param encoding
     */
    public void setEncoding(String encoding) {
        fEncoding = encoding;
    }
}
