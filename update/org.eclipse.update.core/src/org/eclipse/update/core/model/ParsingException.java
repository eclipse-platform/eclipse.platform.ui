package org.eclipse.update.core.model;

// FIXME: this class is being retired

import org.xml.sax.SAXParseException;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
 /**
  * Exception thrown when an error occured parsing the 
  * site or the feature. We cannot return a SAXException
  * as future parser may not be SAX parser.
  */
 
public class ParsingException extends Exception {

	private Throwable exception;
	
	/**
	 * @deprecated
	 */
	public ParsingException(Throwable exception){
		super(exception.getMessage());
		this.exception = exception;
	}
	
	public Throwable getException(){
		return exception;
	}
	/*
	 * @see Object#toString()
	 */
	public String toString() {
		
		return "org.eclipse.update.core.ParsingException: "+exception.toString(); //$NON-NLS-1$
	}

}

