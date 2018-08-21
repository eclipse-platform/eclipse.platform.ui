/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.core;

import java.net.URL;

/**
 * Represents an Ant classpath entry. Clients may implement this interface.
 * 
 * @since 3.0
 */
public interface IAntClasspathEntry {

	/**
	 * Returns the label for this classpath entry.
	 * 
	 * @return the label for this entry.
	 */
	public String getLabel();

	/**
	 * Returns the URL for this classpath entry or <code>null</code> if it cannot be resolved.
	 * 
	 * @return the url for this classpath entry.
	 */
	public URL getEntryURL();

	/**
	 * Returns whether this classpath entry requires the Eclipse runtime to be relevant. Defaults value is <code>true</code>
	 * 
	 * @return whether this classpath entry requires the Eclipse runtime
	 */
	public boolean isEclipseRuntimeRequired();
}
