package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.events.VerifyEvent;


/**
 * Represents a text modification as a document replace command. The text modification is given
 * as a <code>VerifyEvent</code> and translated into a document replace command relative
 * to a given offset. A document command can also be used to initialize a given <code>VerifyEvent</code>.
 */
public final class DocumentCommand {
	
	/** Must the command be updated */
	public boolean doit= false;
	/** The offset of the command */
	public int offset;
	/** The length of the command */
	public int length;
	/** The text to be inserted */
	public String text;
	
	
	/**
	 * Creates a new document command.
	 */
	DocumentCommand() {
	}
	
	/**
	 * Translates a verify event into a document replace command using the given offset.
	 *
	 * @param event the event to be translated
	 * @param offset the offset used for the translation
	 */
	void setEvent(VerifyEvent event, int offset) {
		
		doit= true;
		
		text=  event.text;
		
		this.offset= event.start;
		length= event.end - event.start;
				
		if (length < 0) {
			this.offset += length;
			length= -length;
		}
		
		this.offset += offset;
	}
	
	/**
	 * Fills the given verify event with the replace text and the doit
	 * flag of this document command. Returns whether the document command
	 * covers the same range as the verify event considering the given offset.
	 *
	 * @param event the event to be changed
	 * @param offset to be considered for range comparison
	 * @return <code>true</code> if this command and the event cover the same range
	 */
	boolean fillEvent(VerifyEvent event, int offset) {
		
		int start= this.offset - offset;
		
		event.text= text;
		event.doit= (start == event.start && start + length == event.end) && doit;
		return event.doit;
	}
}


