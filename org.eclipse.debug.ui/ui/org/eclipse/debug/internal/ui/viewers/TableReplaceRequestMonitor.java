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


/**
 * @since 3.2
 *
 */
public class TableReplaceRequestMonitor extends AsynchronousRequestMonitor {
	
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
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		((AsynchronousTableModel)getModel()).replaced(fOriginal, fReplacement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousRequestMonitor#contains(org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousRequestMonitor)
	 */
	protected boolean contains(AsynchronousRequestMonitor update) {
		return false;
	}

}
