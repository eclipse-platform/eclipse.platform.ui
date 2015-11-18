/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.migration;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;

public class WorkbenchMementoReader extends MementoReader {

	WorkbenchMementoReader(IMemento memento) {
		super(memento);
	}

	List<WindowReader> getWindowReaders() {
		IMemento[] windowMems = getChildren(IWorkbenchConstants.TAG_WINDOW);
		List<WindowReader> windows = new ArrayList<>(windowMems.length);
		for (IMemento windowMem : windowMems) {
			windows.add(new WindowReader(windowMem));
		}
		return windows;
	}

	IMemento getMruMemento() {
		XMLMemento root = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKBENCH);
		IMemento mruList = memento.getChild(IWorkbenchConstants.TAG_MRU_LIST);
		if (mruList != null) {
			root.copyChild(mruList);
		}
		return root;
	}

}
