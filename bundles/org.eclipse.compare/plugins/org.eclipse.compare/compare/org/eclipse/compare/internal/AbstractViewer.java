/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;


public abstract class AbstractViewer extends Viewer {

	public void setInput(Object input) {
	}
	
	public Object getInput() {
		return null;
	}
	
	public ISelection getSelection() {
		return null;
	}
	
	public void setSelection(ISelection s, boolean reveal) {
	}
	
	public void refresh() {
	}
}
