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
package org.eclipse.team.core.variants;

import org.eclipse.core.resources.IResource;

/**
 * Listener that can receive notification from a <code>ThreeWaySynchronizer</code>
 * when the synchronization state of one or more resources has changed.
 */
public interface ISynchronizerChangeListener {
	
	/**
	 * Notification of synchronization state changes for the given resources.
	 * @param resources the resources whose synchronization state has changed
	 */
	public void syncStateChanged(IResource[] resources);
}
