/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Tests if any Perspective is open or not.
 * 
 * @since 3.3
 * 
 */
public class OpenPerspectivePropertyTester extends PropertyTester {
	private static final String PROPERTY_IS_PERSPECTIVE_OPEN = "isPerspectiveOpen"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object,
	 *      java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (args.length == 0 && receiver instanceof WorkbenchWindow) {
			final WorkbenchWindow window = (WorkbenchWindow) receiver;
			if (PROPERTY_IS_PERSPECTIVE_OPEN.equals(property)) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IPerspectiveDescriptor persp = page.getPerspective();
					if (persp != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
