/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jface.window;

import org.eclipse.swt.widgets.Shell;

/**
 * Interface for objects that can return a shell. This is normally used for
 * opening child windows. An object that wants to open child shells can take
 * an IShellProvider in its constructor, and the object that implements IShellProvider
 * can dynamically choose where child shells should be opened.
 *
 * @since 3.1
 */
public interface IShellProvider {
	/**
	 * Returns the current shell (or null if none). This return value may
	 * change over time, and should not be cached.
	 *
	 * @return the current shell or null if none
	 */
	Shell getShell();
}
