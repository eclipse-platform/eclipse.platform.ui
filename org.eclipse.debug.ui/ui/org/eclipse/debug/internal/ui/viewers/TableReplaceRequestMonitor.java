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
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.debug.internal.ui.model.viewers.AsynchronousModel;
import org.eclipse.debug.internal.ui.model.viewers.AsynchronousModelRequestMonitor;
import org.eclipse.debug.internal.ui.model.viewers.ModelNode;

/**
 * @since 3.2
 *
 */
public class TableReplaceRequestMonitor extends AsynchronousModelRequestMonitor {
	
	private Object fOriginal;
	private Object fReplacement;

	/**
	 * @param node
	 * @param model
	 */
	TableReplaceRequestMonitor(ModelNode node, Object element, Object replacement, AsynchronousModel model) {
		super(node, model);
		fReplacement = replacement;
		fOriginal = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousModelRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		((AsynchronousTableModel)getModel()).replaced(fOriginal, fReplacement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.AsynchronousModelRequestMonitor#contains(org.eclipse.debug.internal.ui.model.viewers.AsynchronousModelRequestMonitor)
	 */
	protected boolean contains(AsynchronousModelRequestMonitor update) {
		return false;
	}

}
