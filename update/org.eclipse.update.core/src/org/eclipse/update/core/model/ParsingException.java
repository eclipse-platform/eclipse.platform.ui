package org.eclipse.update.core.model;
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
	
	public ParsingException(Throwable exception){
		super();
		this.exception = exception;
	}
	
	public Throwable getException(){
		return exception;
	}
}

