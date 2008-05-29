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
import java.io.OutputStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.internal.console.IOConsolePartitioner;

/**
 * OutputStream used to write to an IOConsole.
 * <p>
 * Clients are not intended to instantiate this class directly, instead
 * use <code>IOConsole.newOutputStream()</code>. 
 * </p>
 * <p>
 * Clients should avoid writing large amounts of output to this stream in the UI
 * thread. The console needs to process the output in the UI thread and if the client
 * hogs the UI thread writing output to the console, the console will not be able
 * to process the output.
 * </p>
 * @since 3.1
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
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

    private boolean fNeedsEncoding = false;

    private boolean prependCR;
    
    /**
     * Constructs a new output stream on the given console.
     * 
     * @param console I/O console
     */
    IOConsoleOutputStream(IOConsole console) {
        this.console = console;
        this.partitioner = (IOConsolePartitioner) console.getPartitioner();
    }

    /**
     * Returns the font style used to decorate data written to this stream.
     * 
     * @return the font style used to decorate data written to this stream
     */
    public int getFontStyle() {
        return fontStyle;
    }
    
    /**
     * Sets the font style to be used to decorate data written to this stream.
     * 
     * @param newFontStyle the font style to be used to decorate data written to this stream
     */
    public void setFontStyle(int newFontStyle) {
        if (newFontStyle != fontStyle) {
            int old = fontStyle;
            fontStyle = newFontStyle;
            console.firePropertyChange(this, IConsoleConstants.P_FONT_STYLE, new Integer(old), new Integer(fontStyle));
        }
    }
    
    /**
     * Returns whether the console this stream is writing to will be activated when this stream
     * is written to.
     * 
     * @return whether the console this stream is writing to will be activated when this stream
     * is written to.
     */
    public boolean isActivateOnWrite() {
        return activateOnWrite;
    }

    /**
     * Sets whether to activate the console this stream is writing to when this stream
     * is written to.
     * 
     * @param activateOnWrite whether the console this stream is writing to will be activated when this stream
     * is written to.
     */
    public void setActivateOnWrite(boolean activateOnWrite) {
        this.activateOnWrite = activateOnWrite;
    }
    
	/**
	 * Sets the color of this stream. Use <code>null</code> to indicate
     * the default color.
	 * 
	 * @param newColor color of this stream, or <code>null</code>
	 */
	public void setColor(Color newColor) {
		Color old = color;
		if (old == null || !old.equals(newColor)) {
		    color = newColor;
		    console.firePropertyChange(this, IConsoleConstants.P_STREAM_COLOR, old, newColor);
		}
	}
	
	/**
	 * Returns the color of this stream, or <code>null</code>
	 * if default.
	 * 
	 * @return the color of this stream, or <code>null</code>
	 */
	public Color getColor() {
	    return color;
	}
	
    /**
     * Returns true if the stream has been closed
     * @return true is the stream has been closed, false otherwise.
     */
    public synchronized boolean isClosed() {
        return closed;
    }
    
	/*
	 *  (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
    public synchronized void close() throws IOException {
        if(closed) {
            throw new IOException("Output Stream is closed"); //$NON-NLS-1$
        }
        if (prependCR) { // force writing of last /r
            prependCR = false;
            notifyParitioner("\r"); //$NON-NLS-1$
        }
        console.streamClosed(this);
        closed = true;
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
        if (fNeedsEncoding) {
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
     * Writes a string to the attached console.
     * 
     * @param str the string to write to the attached console.
     * @throws IOException if the stream is closed.
     */
    public synchronized void write(String str) throws IOException {
        if (fNeedsEncoding) {
	        byte[] defaultBytes = str.getBytes();
	        str = new String(defaultBytes, fEncoding);
        }
        encodedWrite(str);
    }
    
    private void encodedWrite(String encodedString) throws IOException {
        if(closed) {
            throw new IOException("Output Stream is closed"); //$NON-NLS-1$
        }
        if (prependCR){
            encodedString="\r"+encodedString; //$NON-NLS-1$
            prependCR=false;
        }
        if (encodedString.endsWith("\r")) { //$NON-NLS-1$
            prependCR = true;
            encodedString = new String(encodedString.substring(0, encodedString.length()-1));
        }
        notifyParitioner(encodedString);
    }

    private void notifyParitioner(String encodedString) throws IOException {
        try {
            partitioner.streamAppended(this, encodedString);

            if (activateOnWrite) {
            	console.activate();
            } else {
            	ConsolePlugin.getDefault().getConsoleManager().warnOfContentChange(console);
            }
        } catch (IOException e) {
            if (!closed) {
                close();
            }
            throw e;
        }
    }

    /**
     * Sets the character encoding used to interpret characters written to this steam. 
     * 
     * @param encoding encoding identifier
     */
    public void setEncoding(String encoding) {
        fEncoding = encoding;
        fNeedsEncoding = (fEncoding!=null) && (!fEncoding.equals(fDefaultEncoding));
    }
}
