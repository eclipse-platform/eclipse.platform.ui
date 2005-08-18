/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class ViewWithState extends ViewPart {

	private static final String STATE = "state";

	public int fState = 0;

	public void createPartControl(Composite parent) {
		Label l = new Label(parent, SWT.NONE);
		l.setText("This view should have some saved state: " + fState);
	}

	public void setFocus() {
		// do nothing
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Integer i = memento.getInteger(STATE);
			if (i != null) {
				fState = i.intValue();
			}
		}
	}

	public void saveState(IMemento memento) {
		memento.putInteger(STATE, fState);
		super.saveState(memento);
	}
}
