/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.core;

import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;

/**
 * Convenience implementation of an annotated URL.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
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
			(getURL() == null) ? Policy.bind("Feature.NoURL") : getURL().toExternalForm();	//$NON-NLS-1$
		result =
			result
				+ ((getAnnotation() == null) ? URLString : getAnnotation() + " : " + URLString); //$NON-NLS-1$
		return result;
	}
}
