/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;

import java.util.*;


public interface Selector {

	/*
	 * Method is called to pre-select a specific xml type. Pre-selected
	 * elements are then fully parsed and result in calls to full
	 * select method.
	 * @return <code>true</code> is the element should be considered,
	 * <code>false</code> otherwise
	 */
	public boolean select(String entry);

	/*
	 * Method is called with a fully parsed element.
	 * @return <code>true</code> to select this element and terminate the parse,
	 * <code>false</code> otherwise
	 */
	public boolean select(String element, HashMap attributes);
}