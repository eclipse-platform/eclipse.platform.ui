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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPerspectiveRegistry;

public class PerspContentProvider implements IStructuredContentProvider {
/**
 * PerspContentProvider constructor comment.
 */
public PerspContentProvider() {
	super();
}
public void dispose() {
}
public Object[] getElements(Object element) {
	if (element instanceof IPerspectiveRegistry) {
		IPerspectiveRegistry reg = (IPerspectiveRegistry)element;
		return reg.getPerspectives();
	}
	return null;
}
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
}
public boolean isDeleted(Object element) {
	return false;
}
}
