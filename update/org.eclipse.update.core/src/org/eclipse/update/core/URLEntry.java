package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.URL;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.core.model.URLEntryModel;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.core.Policy;

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
		String result = "IURLEntry: "; //$NON-NLS-1$
		String URLString = (getURL()==null)?Policy.bind("Feature.NoURL"):getURL().toExternalForm(); //$NON-NLS-1$
		 result = result +( (getAnnotation()==null)?URLString:getAnnotation() + " : "+URLString); //$NON-NLS-1$
		return result;
	}

	
	}

