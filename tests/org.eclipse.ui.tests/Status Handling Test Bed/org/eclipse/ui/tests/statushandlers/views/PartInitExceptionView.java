/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.statushandlers.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * A sample view throwing a PartInitException initialization.
 */
public class PartInitExceptionView extends ViewPart {

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		throw new PartInitException(
				"A sample PartInitException thrown during viewpart initialization.");
	}

	public void createPartControl(Composite parent) {

	}

	public void setFocus() {

	}

}
