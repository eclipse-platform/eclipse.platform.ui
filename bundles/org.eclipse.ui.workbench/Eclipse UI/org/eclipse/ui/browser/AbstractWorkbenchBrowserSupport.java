/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.browser;

/**
 * Implements <code>IWorkbenchBrowserSupport</code> while leaving some methods
 * to the implementors. Classes that extend this abstract class are meant to be
 * contributed via 'org.eclipse.ui.browserSupport' extension point.
 * 
 * @since 3.1
 */
public abstract class AbstractWorkbenchBrowserSupport implements
		IWorkbenchBrowserSupport {

	/**
	 * The default constructor.
	 */
	public AbstractWorkbenchBrowserSupport() {
	}
}
