package org.eclipse.ui.internal.console;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * A region in an IOConsole's document.
 * @since 3.1
 *
 */
public class IOConsolePartition implements IConsolePartition {
	public static final String OUTPUT_PARTITION_TYPE = ConsolePlugin.getUniqueIdentifier() + ".io_console_output_partition_type"; //$NON-NLS-1$
	public static final String INPUT_PARTITION_TYPE = ConsolePlugin.getUniqueIdentifier() + ".io_console_input_partition_type"; //$NON-NLS-1$

	/**
	 * The data contained by this partition.
	 */
	private StringBuffer buffer;
    private String type;
    private int offset;
    /**
     * Output partitions are all read only.
     * Input partitions are read only once they have been appended to the console's input stream.
     */
    private boolean readOnly;
    
    /**
     * Only one of inputStream or outputStream will be null depending on the partitions type.
     */
    private IOConsoleOutputStream outputStream;
    private IOConsoleInputStream inputStream;
    private int length;
    
    /**
     * Creates a new partition to contain output to console.
     */
    public IOConsolePartition(IOConsoleOutputStream outputStream, int length) {
        this.outputStream = outputStream;
        this.length = length;
        this.type = OUTPUT_PARTITION_TYPE;
        this.readOnly = true;
    }
    
    /**
     * Creates a new partition to contain input from a console
     */
    public IOConsolePartition(IOConsoleInputStream inputStream, String text) {
        this.inputStream = inputStream;
        buffer = new StringBuffer(text);
        length = text.length();
        this.type = INPUT_PARTITION_TYPE;
        this.readOnly = false;
    }
    
    /**
     * appends a string to this partition
     * @param s
     */
    public void append(String s) {
        buffer.append(s);
        length += s.length();
    }
      
    /**
     * Deletes data from this partition.
     * @param delOffset
     * @param delLength
     */
    public void delete(int delOffset, int delLength) {
        buffer.delete(delOffset, delOffset+delLength);
        length -= delLength;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.text.ITypedRegion#getType()
     */
    public String getType() {
        return type;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.text.IRegion#getLength()
     */
    public int getLength() {
        return length;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.text.IRegion#getOffset()
     */
    public int getOffset() {
        return offset;
    }
    
    /**
     * Sets this partitions offset in the document.
     * @param offset This partitions offset in the document.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    /**
     * Sets this partitions length
     * 
     * @param length
     */
    public void setLength(int length) {
    	this.length = length;
    }
    
    /**
     * Returns the data contained in this partition.
     * @return The data contained in this partition.
     */
    public String getString() {
        return buffer != null ? buffer.toString() : ""; //$NON-NLS-1$
    }
    
    /**
     * Returns a StyleRange object which may be used for setting the style
     * of this partition in a viewer.
     */
    public StyleRange getStyleRange(int rangeOffset, int rangeLength) {
        return new StyleRange(rangeOffset, rangeLength, getColor(), null, getFontStyle());
    }

    private int getFontStyle() {
        if (type.equals(INPUT_PARTITION_TYPE)) {
            return inputStream.getFontStyle();
        } 
        return outputStream.getFontStyle();
    }

    public Color getColor() {
        if (type.equals(INPUT_PARTITION_TYPE)) {
            return inputStream.getColor();
        } 
        return outputStream.getColor();
    }

    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly() {
        readOnly = true;
    }

    public void clearBuffer() {
        buffer = null;
    }
}
