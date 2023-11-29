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

package org.eclipse.ui.internal.navigator.dnd;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

/**
 * A Skeleton implementation of {@link CommonDropAdapterAssistant}.
 *
 * @since 3.2
 */
public class SkeletonCommonDropAssistant extends CommonDropAdapterAssistant {

	/**
	 * The singleton instance.
	 */
	public static final SkeletonCommonDropAssistant INSTANCE = new SkeletonCommonDropAssistant();

	private SkeletonCommonDropAssistant() {
	}

	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter,
			DropTargetEvent aDropTargetEvent, Object aTarget) {
		return Status.CANCEL_STATUS;
	}

	@Override
	public IStatus validateDrop(Object target, int operation,
			TransferData transferType) {
		return Status.CANCEL_STATUS;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// org.eclipse.ui.navigator.CommonDropAdapterAssistant#findSupportedTransferData(org.eclipse.swt.dnd.TransferData[])
	// */
	// public TransferData findSupportedTransferData(
	// TransferData[] theAvailableTransferData) {
	//
	// return null;
	// }

}
