package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.ReferenceCounter;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * Abstract base implementation of all workbench parts.
 * <p>
 * This class is not intended to be subclassed by clients outside this
 * package; clients should instead subclass <code>ViewPart</code> or
 * <code>EditorPart</code>.
 * </p>
 * 
 * @see ViewPart
 * @see EditorPart
 */
public abstract class WorkbenchPart implements IWorkbenchPart, IExecutableExtension {
	private String title;
	private ImageDescriptor imageDescriptor;
	private Image titleImage;
	private String toolTip;
	private IConfigurationElement configElement;
	private IWorkbenchPartSite partSite;
	private ListenerList propChangeListeners = new ListenerList(2);
	
/**
 * Creates a new workbench part.
 */
protected WorkbenchPart() {
	super();
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void addPropertyListener(IPropertyListener l) {
	propChangeListeners.add(l);
}
/* (non-Javadoc)
 * Creates the SWT controls for this workbench part.
 * <p>
 * Subclasses must implement this method.  For a detailed description of the
 * requirements see <code>IWorkbenchPart</code>
 * </p>
 *
 * @param parent the parent control
 * @see IWorkbenchPart
 */
public abstract void createPartControl(Composite parent);
/**
 * The <code>WorkbenchPart</code> implementation of this 
 * <code>IWorkbenchPart</code> method disposes the title image
 * loaded by <code>setInitializationData</code>. Subclasses may extend.
 */
public void dispose() {
	ReferenceCounter imageCache = WorkbenchImages.getImageCache();
	Image image = (Image)imageCache.get(imageDescriptor);
	if (image != null) {
		int count = imageCache.removeRef(imageDescriptor);
		if(count <= 0)
			image.dispose();
	}
	
	// Clear out the property change listeners as we
	// should not be notifying anyone after the part
	// has been disposed.
	if (!propChangeListeners.isEmpty()) {
		propChangeListeners = new ListenerList(1);
	}
}
/**
 * Fires a property changed event.
 *
 * @param propertyId the id of the property that changed
 */
protected void firePropertyChange(final int propertyId) {
	Object [] array = propChangeListeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPropertyListener l = (IPropertyListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.propertyChanged(WorkbenchPart.this, propertyId);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				//If and unexpected exception happens, remove it
				//to make sure the workbench keeps running.
				propChangeListeners.remove(l);
			}
		});
	}
}
/**
 * The <code>WorkbenchPart</code> implementation of this <code>IAdaptable</code>
 * method returns <code>null</code>. Subclasses may reimplement.
 */
public Object getAdapter(Class key) {
	return null;
}
/**
 * Returns the configuration element for this part. The configuration element
 * comes from the plug-in registry entry for the extension defining this part.
 *
 * @return the configuration element for this part
 */
protected IConfigurationElement getConfigurationElement() {
	return configElement;
}
/**
 * Returns the default title image.
 *
 * @return the default image
 */
protected Image getDefaultImage() {
	return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEF_VIEW);
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public IWorkbenchPartSite getSite() {
	return partSite;
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public String getTitle() {
	return title;
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public Image getTitleImage() {
	if (titleImage != null) {
		return titleImage;
	}
	return getDefaultImage();
}
/* (non-Javadoc)
 * Gets the title tool tip text of this part.
 *
 * @return the tool tip text
 */
public String getTitleToolTip() {
	return toolTip;
}
/* (non-Javadoc)
 * Method declared on IWorkbenchPart.
 */
public void removePropertyListener(IPropertyListener l) {
	propChangeListeners.remove(l);
}
/* (non-Javadoc)
 * Asks this part to take focus within the workbench.
 * <p>
 * Subclasses must implement this method.  For a detailed description of the
 * requirements see <code>IWorkbenchPart</code>
 * </p>
 *
 * @see IWorkbenchPart
 */
public abstract void setFocus();
/**
 * The <code>WorkbenchPart</code> implementation of this
 * <code>IExecutableExtension</code> records the configuration element in
 * and internal state variable (accessible via <code>getConfigElement</code>).
 * It also loads the title image, if one is specified in the configuration element.
 * Subclasses may extend.
 * 
 * Should not be called by clients. It is called by the core plugin when creating
 * this executable extension.
 */
public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {

	// Save config element.
	configElement = cfig;

	// Title.
	title = cfig.getAttribute("name");//$NON-NLS-1$
	if (title == null) {
		title = "Unknown";//$NON-NLS-1$
	}

	// Icon.
	String strIcon = cfig.getAttribute("icon");//$NON-NLS-1$
	if (strIcon != null) {
		imageDescriptor = 
			WorkbenchImages.getImageDescriptorFromExtension(
				configElement.getDeclaringExtension(), 
				strIcon); 
					
		
		/* remember the image in a separatly from titleImage,
		 * since it must be disposed even if the titleImage is changed
		 * to something else*/
	 	ReferenceCounter imageCache = WorkbenchImages.getImageCache();
		Image image = (Image)imageCache.get(imageDescriptor);
		if(image != null) {
			imageCache.addRef(imageDescriptor);
		} else {
			image = imageDescriptor.createImage();
			imageCache.put(imageDescriptor,image);
		}
		titleImage = image;
	}
}
/**
 * Sets the part site.
 * <p>
 * Subclasses must invoke this method from <code>IEditorPart.init</code>
 * and <code>IViewPart.init</code>.
 *
 * @param site the workbench part site
 */
protected void setSite(IWorkbenchPartSite site) {
	this.partSite = site;
}
/**
 * Sets or clears the title of this part.
 *
 * @param title the title, or <code>null</code> to clear
 */
protected void setTitle(String title) {
	this.title = title;
	firePropertyChange(IWorkbenchPart.PROP_TITLE);
}
/**
 * Sets or clears the title image of this part.
 *
 * @param titleImage the title image, or <code>null</code> to clear
 */
protected void setTitleImage(Image titleImage) {
	this.titleImage = titleImage;
	firePropertyChange(IWorkbenchPart.PROP_TITLE);
}
/**
 * Sets or clears the title tool tip text of this part.
 *
 * @param text the new tool tip text
 */
protected void setTitleToolTip(String text) {
	this.toolTip = text;
	firePropertyChange(IWorkbenchPart.PROP_TITLE);
}

}
