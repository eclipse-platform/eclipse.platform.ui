/*******************************************************************************
 * Copyright (c) 2003, 2006 BBDO Detroit and others.
 *
 * This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Thierry Lach (thierry.lach@bbdodetroit.com) - initial API and implementation for bug 40502
 *******************************************************************************/
package org.eclipse.ant.core;

/**
 * An interface that must be implemented by plug-ins that wish to contribute predefined variables to an Ant project when run from within Eclipse.
 * Clients may implement this interface.
 * 
 * @since 3.0
 */
public interface IAntPropertyValueProvider {

	/**
	 * Returns a value that the Ant entry point will use to set the value of the Ant property.
	 * 
	 * @param antPropertyName
	 *            the Ant property to set
	 * @return the value for the property, or <code>null</code> if the property should not be set
	 */
	public String getAntPropertyValue(String antPropertyName);
}