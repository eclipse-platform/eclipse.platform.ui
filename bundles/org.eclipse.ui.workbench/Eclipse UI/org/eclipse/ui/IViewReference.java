package org.eclipse.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
/**
 * Defines a reference to an IViewPart.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IViewReference extends IWorkbenchPartReference {
	/**
	 * Returns the IViewPart referenced by this object.
	 * Returns null if the view was not instanciated or
	 * it failed to be restored. Tries to restore the view
	 * if <code>restore</code> is true.
	 */
	public IViewPart getView(boolean restore);
	/**
	 * Returns true if the view is a fast view otherwise returns false.
	 */	
	public boolean isFastView();
}
