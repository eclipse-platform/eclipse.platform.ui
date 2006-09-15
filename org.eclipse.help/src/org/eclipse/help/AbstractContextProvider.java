/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

/**
 * An <code>AbstractContextProvider</code> is a mechanism to provide arbitrary
 * context-sensitive help for any part of the UI. <code>AbstractContextProvider
 * </code>s must be registered via the <code>org.eclipse.help.contexts</code>
 * extension point.
 * 
 * @since 3.3
 */
public abstract class AbstractContextProvider {

	/**
	 * Returns the context-sensitive help content for the UI element with the
	 * given context help ID, and for the given locale.
	 * 
	 * @param id the unique context help ID, e.g. "org.my.plugin.my_context_id"
	 * @return the context help, or <code>null</code> if not available
	 */
	public abstract IContext getContext(String id, String locale);
}
