package org.eclipse.ui.console;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.internal.console.IOConsolePartitioner;

/**
 * This class is new and experimental. It will likely be subject to significant change before
 * it is finalized.
 * 
 * @since 3.1
 */
public class IOConsoleOutputStream extends OutputStream {
    private boolean closed = false;

    private IOConsolePartitioner partitioner;
    private IOConsole console;
    private boolean activateOnWrite = true;
    private Color color;
    private String streamId;

    private int fontStyle;
    

    IOConsoleOutputStream(String streamId, IOConsole console) {
        this.streamId = streamId;
        this.console = console;
        this.partitioner = console.getPartitioner();
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
    
    public boolean isActivateOnWrite() {
        return activateOnWrite;
    }

    public void setActivateOnWrite(boolean activateOnWrite) {
        this.activateOnWrite = activateOnWrite;
    }
    
	public void setColor(Color newColor) {
		Color old = color;
		if (old == null || old.equals(newColor)) {
		    color = newColor;
		    console.firePropertyChange(this, IOConsole.P_STREAM_COLOR, old, newColor);
		}
	}
	
	public Color getColor() {
	    return color;
	}

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
