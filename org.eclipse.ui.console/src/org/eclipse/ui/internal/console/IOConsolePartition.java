package org.eclipse.ui.internal.console;

import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * This class is new and experimental. It will likely be subject to significant change before
 * it is finalized.
 * 
 * @since 3.1
 *
 */
public class IOConsolePartition implements ITypedRegion {
	public static final String OUTPUT_PARTITION_TYPE = ConsolePlugin.getUniqueIdentifier() + ".io_console_output_partition_type"; //$NON-NLS-1$
	public static final String INPUT_PARTITION_TYPE = ConsolePlugin.getUniqueIdentifier() + ".io_console_input_partition_type"; //$NON-NLS-1$
	
	private StringBuffer buffer;
    private String type;
    private int offset;
    private boolean readOnly;
    private IOConsoleOutputStream outputStream;
    private IOConsoleInputStream inputStream;
    
    public IOConsolePartition(IOConsoleOutputStream outputStream, String text) {
        this.outputStream = outputStream;
        buffer = new StringBuffer(text);
        this.type = OUTPUT_PARTITION_TYPE;
        this.readOnly = true;
    }
    
    public IOConsolePartition(IOConsoleInputStream inputStream, String text) {
        this.inputStream = inputStream;
        buffer = new StringBuffer(text);
        this.type = INPUT_PARTITION_TYPE;
        this.readOnly = false;
    }
    
    public void append(String s) {
        buffer.append(s);
    }
      
    public void delete(int offset, int length) {
        buffer.delete(offset, offset+length);
    }
    
    public String getType() {
        return type;
    }

    public int getLength() {
        return buffer.length();
    }

    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public String getString() {
        return buffer.toString();
    }
    
    public StyleRange getStyleRange(int rangeOffset, int rangeLength) {
        return new StyleRange(rangeOffset, rangeLength, getColor(), null, getFontStyle());
    }

    private int getFontStyle() {
        if (type.equals(INPUT_PARTITION_TYPE)) {
            return inputStream.getFontStyle();
        } 
        return outputStream.getFontStyle();
    }

    private Color getColor() {
        if (type.equals(INPUT_PARTITION_TYPE)) {
            return inputStream.getColor();
        } 
        return outputStream.getColor();
    }

    public String getStreamId() {
        if (type.equals(INPUT_PARTITION_TYPE)) {
            return inputStream.getStreamId();
        } 
        return outputStream.getStreamId();
    }

    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean newSetting) {
        readOnly = newSetting;
    }


}
