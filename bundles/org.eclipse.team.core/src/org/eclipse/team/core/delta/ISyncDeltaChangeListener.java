/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.delta;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 * @see ISyncDeltaTree
 * @since 3.2
 */
public interface ISyncDeltaChangeListener {

	/**
	 * @param event
	 * @param monitor
	 */
	void syncDeltaTreeChanged(ISyncDeltaChangeEvent event, IProgressMonitor monitor);

	/**
	 * @param tree
	 * @param monitor
	 */
	void syncDeltaTreeReset(ISyncDeltaTree tree, IProgressMonitor monitor);

}
