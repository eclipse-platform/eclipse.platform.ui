package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IValue;

/**
 * Notified of detailed value descriptions.
 * 
 * @see IDebugModelPresentation
 * @since 2.0
 */

public interface IValueDetailListener {
	/**
	 * Notifies this listener that the details for the given
	 * value have been computed as the specified result.
	 *  
	 * @param value the value for which the detail is provided
	 * @param result the detailed description of the given value
	 */
	public void detailComputed(IValue value, String result);
}