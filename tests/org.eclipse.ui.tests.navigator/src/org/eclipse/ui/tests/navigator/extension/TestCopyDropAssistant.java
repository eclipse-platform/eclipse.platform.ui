/*******************************************************************************
 * Copyright (c) 2009 Oakland Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.resources.ResourceDropAdapterAssistant;

public class TestCopyDropAssistant extends ResourceDropAdapterAssistant {

	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter,
			DropTargetEvent aDropTargetEvent, Object aTarget) {
		return super.handleDrop(aDropAdapter, aDropTargetEvent, aTarget);
	}

	@Override
	public IStatus validateDrop(Object target, int operation,
			TransferData transferType) {

		// Make sure we have the event
		DropTargetEvent event = getCurrentEvent();
		Assert.isTrue(event != null);
		
		// Switch to copy
		getCommonDropAdapter().overrideOperation(DND.DROP_COPY);
		return super.validateDrop(target, operation, transferType);
	}

}
