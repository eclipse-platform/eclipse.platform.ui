/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 194734, 262287
 ******************************************************************************/

package org.eclipse.core.databinding.property;

/**
 * Listener for changes to properties on a particular source object
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.2
 */
public interface ISimplePropertyListener {
	/**
	 * Handle the described property event.
	 * 
	 * @param event
	 *            the event which occured
	 */
	public void handleEvent(SimplePropertyEvent event);
}
