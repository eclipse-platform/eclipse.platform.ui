/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

/**
 * Print out selection listener events.
 */
public class TestSelectionListener implements org.eclipse.ui.ISelectionListener {
/**
 * TestSelectionListener constructor comment.
 */
public TestSelectionListener() {
	super();
}
/**
 * Notifies this listener that the selection has changed.
 *
 * @param part the workbench part containing the selection
 * @param selection the new selection, or <code>null</code> if none
 */
public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	System.out.println("selectionChanged(" + selection + ")");//$NON-NLS-2$//$NON-NLS-1$
}
}
