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
	 * Returns <code>null</code> if the editors was not instantiated or
	 * it failed to be restored. Tries to restore the editor
	 * if <code>restore</code> is true.
	 */
	public IWorkbenchPart getPart(boolean restore);

	/**
	 * @see IWorkbenchPart#getTitle
	 */	
	public String getTitle();

	/**
	 * @see IWorkbenchPart#getTitleImage
	 */	
	public Image getTitleImage();

	/**
	 * @see IWorkbenchPart#getTitleToolTip
	 */		
	public String getTitleToolTip();

	/**
	 * @see IWorkbenchPartSite#getId
	 */		
	public String getId();

	/**
	 * @see IWorkbenchPart#addPropertyListener
	 */
	public void addPropertyListener(IPropertyListener listener);
	
	/**
	 * @see IWorkbenchPart#removePropertyListener
	 */
	public void removePropertyListener(IPropertyListener listener);
}
