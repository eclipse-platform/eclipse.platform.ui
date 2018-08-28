/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public Transfer[] getSupportedTransferTypes() {
		return NO_TRANSFER_TYPES;
	}

	@Override
	public boolean setDragData(DragSourceEvent anEvent,
			IStructuredSelection aSelection) {
		return false;

	}

}
