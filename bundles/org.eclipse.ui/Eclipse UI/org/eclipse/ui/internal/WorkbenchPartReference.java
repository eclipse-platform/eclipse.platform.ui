package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 */
public abstract class WorkbenchPartReference implements IWorkbenchPartReference {
	
	private ListenerList propChangeListeners = new ListenerList(2);	
	
	/**
	 * @see IWorkbenchPart
	 */
	public void addPropertyListener(IPropertyListener listener) {
		IWorkbenchPart part = getPart(false);
		if(part != null)
			part.addPropertyListener(listener);
		else
			propChangeListeners.add(listener);
	}
	/**
	 * @see IWorkbenchPart
	 */
	public void removePropertyListener(IPropertyListener listener) {
		IWorkbenchPart part = getPart(false);
		if(part != null)
			part.removePropertyListener(listener);
		else
			propChangeListeners.remove(listener);
	}
	public void setPart(IWorkbenchPart part) {
		Object listeners[] = propChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			part.addPropertyListener((IPropertyListener)listeners[i]);
		}
	}
	public abstract void setPane(PartPane pane);
	public abstract PartPane getPane();
}
