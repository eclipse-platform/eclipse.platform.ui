package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.update.core.model.URLEntryModel;
import org.eclipse.update.internal.core.Policy;

/**
 * Convenience implementation of an annotated URL.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.IURLEntry
 * @see org.eclipse.update.core.model.URLEntryModel
 * @since 2.0
 */
public class URLEntry extends URLEntryModel implements IURLEntry {

	/**
	 * Default constructor for annotated URL
	 * 
	 * @since 2.0
	 */
	public URLEntry() {
		super();
	}

	/**
	 * Returns a string representation of an annotated URL.
	 * 
	 * @return annotated url as String
	 * @since 2.0
	 */
	public String toString() {
		String result = "IURLEntry: "; //$NON-NLS-1$
		String URLString =
			(getURL() == null) ? Policy.bind("Feature.NoURL") : getURL().toExternalForm();
		//$NON-NLS-1$
		result =
			result
				+ ((getAnnotation() == null) ? URLString : getAnnotation() + " : " + URLString);
		//$NON-NLS-1$
		return result;
	}
}