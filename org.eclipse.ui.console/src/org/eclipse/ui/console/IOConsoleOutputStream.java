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
import org.eclipse.ui.internal.console.IOConsolePartitioner;

/**
 * OutputStream used to write to an IOConsole.
 * 
 * @since 3.1
 */
public class IOConsoleOutputStream extends OutputStream {
    private boolean closed = false;

    private IOConsolePartitioner partitioner;
    private IOConsole console;
    private boolean activateOnWrite = false;
    private Color color;
    private int fontStyle;
    

    IOConsoleOutputStream(IOConsole console) {
        this.console = console;
        this.partitioner = (IOConsolePartitioner) console.getPartitioner();
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
    
    public boolean isActivateOnWrite() {
        return activateOnWrite;
    }

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
		if (old == null || old.equals(newColor)) {
		    color = newColor;
		    console.firePropertyChange(this, IOConsole.P_STREAM_COLOR, old, newColor);
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
	
    public void close() throws IOException {
        if(closed) {
            throw new IOException("Output Stream is closed"); //$NON-NLS-1$
        }
        closed = true;
    }

    public void flush() throws IOException {
        if(closed) {
            throw new IOException("Output Stream is closed"); //$NON-NLS-1$
        }
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        write(new String(b, off, len));
    }
    public void write(byte[] b) throws IOException {
        write(new String(b));
    }
    public void write(int b) throws IOException {
        write(new String(new byte[] {(byte)b}));
    }    
    
    protected void write(String s) throws IOException {
        if(closed) {
            throw new IOException("Output Stream is closed"); //$NON-NLS-1$
        }
        partitioner.streamAppended(this, s);
        if (activateOnWrite) {
            console.activate();
        }
    }



}
