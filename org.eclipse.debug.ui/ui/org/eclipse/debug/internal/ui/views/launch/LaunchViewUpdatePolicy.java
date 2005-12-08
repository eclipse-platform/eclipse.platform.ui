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
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.DefaultUpdatePolicy;

/**
 * @since 3.2
 *
 */
public class LaunchViewUpdatePolicy extends DefaultUpdatePolicy {
	
	private LaunchView fView = null;
	
	public LaunchViewUpdatePolicy(LaunchView view) {
		fView = view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.AbstractUpdatePolicy#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		fView = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DefaultUpdatePolicy#handleChange(org.eclipse.debug.internal.ui.viewers.IModelDeltaNode)
	 */
	protected void handleChange(IModelDelta node) {
		super.handleChange(node);
		if ((node.getFlags() & IModelDelta.STATE) != 0) {
			fView.possibleContextChange(node.getElement());
		}
	}
	
}
