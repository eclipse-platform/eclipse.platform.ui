/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui;

import org.eclipse.jface.viewers.ISelection;


/**
 * 2.1 - WORK_IN_PROGRESS do not use.
 */
public abstract class NavigationLocation {
	public abstract void restoreLocation(IEditorPart part);
	public abstract boolean differsFromCurrentLocation(IEditorPart part);
	public abstract void dispose();
	public abstract boolean mergeInto(NavigationLocation entry);
	
	public abstract void saveAndDeactivate(IEditorPart part, IMemento memento);
	public abstract void restoreAndActivate(IEditorPart part, IMemento memento);
}