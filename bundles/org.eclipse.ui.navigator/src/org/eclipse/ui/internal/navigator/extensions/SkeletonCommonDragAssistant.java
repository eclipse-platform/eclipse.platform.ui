/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

/**
 * A Skeleton implementation of {@link CommonDragAdapterAssistant}.
 * 
 * @since 3.2
 * 
 */
public final class SkeletonCommonDragAssistant extends
		CommonDragAdapterAssistant {

	/**
	 * The singleton instance.
	 */
	public static final SkeletonCommonDragAssistant INSTANCE = new SkeletonCommonDragAssistant();

	private static final Transfer[] NO_TRANSFER_TYPES = new Transfer[0];

	private SkeletonCommonDragAssistant() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#getSupportedTransferTypes()
	 */
	public Transfer[] getSupportedTransferTypes() {
		return NO_TRANSFER_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#setDragData(org.eclipse.swt.dnd.DragSourceEvent,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean setDragData(DragSourceEvent anEvent,
			IStructuredSelection aSelection) {
		return false;

	}

}
