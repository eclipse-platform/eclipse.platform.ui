/*******************************************************************************
 * Copyright (c) 2000, 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import org.eclipse.core.resources.IResource;

/**
 * Interface for context-menu launch shortcut visibility.
 * <p>
 * An optional <code>filterClass</code> attribute in the <code>ILaunchShortcut</code>
 * extension allows a shortcut provider to specify a Java class that will
 * answer filtering questions. The shortcut extension point accepts a list
 * of "contextFilter" elements that specify tests. If the tests all return
 * <code>true</code>, the shortcut will appear on the context menu for the
 * selected resource.
 * </p><p>
 * Each <code>contextFilter</code> test receives an <code>IResource</code>
 * object, the name of a test attribute, and the expected attribute value.
 * </p>
 * <p>
 * This interface is yet experimental.
 * </p>
 * @see org.eclipse.debug.ui.ILaunchShortcut
 * @since 3.0
 */
public interface ILaunchFilter {
	/**
	 * Returns whether the specific attribute matches the state of the target
	 * resource object.
	 *
	 * @param target the target resource object
	 * @param name the attribute name
	 * @param value expected attribute value
	 * @return <code>true</code> if the attribute matches; <code>false</code> otherwise
	 */
	public boolean testAttribute(IResource target, String name, String value);
}
