package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.URL;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.core.model.URLEntryModel;
import org.eclipse.update.internal.core.*;

/**
 * Default implementation of IURLEntry
 */

public class URLEntry extends URLEntryModel implements IURLEntry{

	/**
	 * Constructor for URLEntry
	 */
	public URLEntry() {
		super();
	}
	
	/*
	 * @see Object#toString()
	 */
	public String toString() {
		String result = "IURLEntry: ";
		 result = result +( (getAnnotation()==null)?getURL().toExternalForm():getAnnotation() + " : "+getURL().toExternalForm());
		return result;
	}

	
	}

