package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */



/**
 * Standard implementation of <code>ILineTracker</code>.
 * The line tracker considers the three common line 
 * delimiters which are '\n', '\r', '\r\n'.<p>
 * This class is not intended to be subclassed.
 */
public class DefaultLineTracker extends AbstractLineTracker {
	
	/** The predefined delimiters of this tracker */
	public final static String[] DELIMITERS= { "\r", "\n", "\r\n" }; //$NON-NLS-3$ //$NON-NLS-1$ //$NON-NLS-2$
	/** A predefined delimiter info which is always reused as return value */
	private DelimiterInfo fDelimiterInfo= new DelimiterInfo();
	
	
	/**
	 * Creates a standard line tracker.
	 */
	public DefaultLineTracker() {
	}
	
	/*
	 * @see ILineDelimiter@getLegalLineDelimiters
	 */
	public String[] getLegalLineDelimiters() {
		return DELIMITERS;
	}

	/*
	 * @see AbstractLineTracker#nextDelimiterInfo(String, int)
	 */
	protected DelimiterInfo nextDelimiterInfo(String text, int offset) {
		
		char ch;
		int length= text.length();
		for (int i= offset; i < length; i++) {
			
			ch= text.charAt(i);
			if (ch == '\r') {
				
				if (i + 1 < length) {
					if (text.charAt(i + 1) == '\n') {
						fDelimiterInfo.delimiter= DELIMITERS[2];
						fDelimiterInfo.delimiterIndex= i;
						fDelimiterInfo.delimiterLength= 2;
						return fDelimiterInfo;
					}
				}
				
				fDelimiterInfo.delimiter= DELIMITERS[0];
				fDelimiterInfo.delimiterIndex= i;
				fDelimiterInfo.delimiterLength= 1;
				return fDelimiterInfo;
				
			} else if (ch == '\n') {
				
				fDelimiterInfo.delimiter= DELIMITERS[1];
				fDelimiterInfo.delimiterIndex= i;
				fDelimiterInfo.delimiterLength= 1;
				return fDelimiterInfo;
			}
		}
		
		return null;
	}
}
