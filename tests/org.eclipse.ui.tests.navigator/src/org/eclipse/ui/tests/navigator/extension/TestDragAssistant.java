/*******************************************************************************
 * Copyright (c) 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francis Upton IV, Oakland Software - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.ui.navigator.resources.ResourceDragAdapterAssistant;

/**
 */
public class TestDragAssistant extends ResourceDragAdapterAssistant {

	public static DragSourceEvent _finishedEvent;
	public static IStructuredSelection _finishedSelection;

	public static boolean _dragSetDataCalled;

	public static boolean _doit;

	public static void resetTest() {
		_doit = true;
		_dragSetDataCalled = false;
		_finishedEvent = null;
		_finishedSelection = null;
	}
	
	public void dragStart(DragSourceEvent anEvent,
			IStructuredSelection aSelection) {
		super.dragStart(anEvent, aSelection);
		anEvent.doit = _doit;
	}

	public boolean setDragData(DragSourceEvent anEvent,
			IStructuredSelection aSelection) {
		super.setDragData(anEvent, aSelection);
		_dragSetDataCalled = true;
		return true;
	}

	public void dragFinished(DragSourceEvent anEvent,
			IStructuredSelection aSelection) {

		_finishedEvent = anEvent;
		_finishedSelection = aSelection;
	}

}
