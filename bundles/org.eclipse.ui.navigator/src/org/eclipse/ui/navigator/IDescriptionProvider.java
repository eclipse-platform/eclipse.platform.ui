/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 3.2
 */
public interface IDescriptionProvider {

	/**
	 * <p>
	 * Provide a description for the status bar view, if available. A default
	 * string of the form "(x) items selected" will be used if this method
	 * choosed to return null.
	 * </p>
	 * 
	 * <p>
	 * The empty string ("") will be respected as a valid value if returned.
	 * Return <b>null </b> if the extension defers to the default method of
	 * supplying status bar descriptions.
	 * </p>
	 * 
	 * @param anElement
	 *            The element selected in the Navigator
	 * @return A description for the status bar view, or null if not available.
	 */
	String getDescription(Object anElement);

}
