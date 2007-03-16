/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.testing;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.testing.IWorkbenchPartTestable;

/**
 * Implementation of {@link IWorkbenchPartTestable}.
 * 
 * @since 3.3
 */
public class WorkbenchPartTestable implements IWorkbenchPartTestable {

	private Composite composite;
	
	/**
	 * Create a new instance of this class based on the provided part.
	 * 
	 * @param partSite the part to test
	 */
	public WorkbenchPartTestable(PartSite partSite) {
		Composite paneComposite = (Composite)partSite.getPane().getControl();
		Control [] paneChildren = paneComposite.getChildren();
		this.composite = ((Composite) paneChildren[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.testing.IWorkbenchPartTestable#getControl()
	 */
	public Composite getControl() {
		return composite;
	}
}
