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


/**
 * 2.1 - WORK_IN_PROGRESS do not use.
 */
public abstract class NavigationLocation {
	
	private IEditorPart editorPart;
	
	protected NavigationLocation(IEditorPart editorPart) {
		this.editorPart= editorPart;
	}
	
	protected IEditorPart getEditorPart() {
		return editorPart;
	}
	
	public void setEditorPart(IEditorPart editorPart) {
		this.editorPart= editorPart;
	}
	
	public void dispose() {
		editorPart= null;
	}
	
	public void clearState() {
		editorPart= null;
	}
	
	public void saveState(IMemento memento) {
	}
	
	public void restoreState(IMemento memento) {
	}
	
	public abstract void restore();
	public abstract boolean equalsLocationOf(IEditorPart part);
	public abstract boolean mergeInto(NavigationLocation entry);
		
}