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
public class TableRemoveRequestMonitor extends TableAddRequestMonitor {

	/**
	 * @param parent
	 * @param elements
	 * @param model
	 */
	TableRemoveRequestMonitor(ModelNode parent, Object[] elements, AsynchronousModel model) {
		super(parent, elements, model);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.AsynchronousRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		((AsynchronousTableModel)getModel()).removed(fElements);
	}
}
