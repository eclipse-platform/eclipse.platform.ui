package org.eclipse.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
/**
 * Implements a reference to a IWorkbenchPart.
 * The IWorkbenchPart will not be instanciated until the part 
 * becomes visible or the API getPart is sent with true;
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IWorkbenchPartReference {
	/**
	 * Returns the IEditorPart referenced by this object.
	 * Returns null if the editors was not instanciated or
	 * it failed to be restored. Tries to restore the editor
	 * if <code>restore</code> is true.
	 */
	public IWorkbenchPart getPart(boolean restore);
	/**
	 * @see IWorkbenchPart
	 */	
	public String getTitle();
	/**
	 * @see IWorkbenchPart
	 */	
	public Image getTitleImage();
	/**
	 * @see IWorkbenchPart
	 */		
	public String getTitleToolTip();

	public String getId();
	/**
	 * @see IWorkbenchPart
	 */
	public void addPropertyListener(IPropertyListener listener);
	/**
	 * @see IWorkbenchPart
	 */
	public void removePropertyListener(IPropertyListener listener);
}
