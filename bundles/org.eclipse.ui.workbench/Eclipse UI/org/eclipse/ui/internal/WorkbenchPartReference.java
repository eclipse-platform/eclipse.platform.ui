/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;
import java.util.BitSet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.util.Util;

/**
 * 
 */
public abstract class WorkbenchPartReference implements IWorkbenchPartReference {

	protected IWorkbenchPart part;

	private String id;
	protected PartPane pane;
	private String title;
	private String tooltip;
	private Image image;
	private ImageDescriptor imageDescriptor;
	private ListenerList propChangeListeners = new ListenerList(2);		
	private String partName;
	private String contentDescription;

	/**
	 * Used to remember which events have been queued.
	 */
	private BitSet queuedEvents = new BitSet();
	private boolean queueEvents = false;
	
	private IPropertyListener propertyChangeListener = new IPropertyListener() {
		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
		 */
		public void propertyChanged(Object source, int propId) {
			partPropertyChanged(source, propId);
		}
	};
	
	public WorkbenchPartReference() {
	    //no-op
	}
	
	/**
	 * Calling this with deferEvents(true) will queue all property change events until a subsequent
	 * call to deferEvents(false). This should be used at the beginning of a batch of related changes
	 * to prevent duplicate property change events from being sent.
	 * 
	 * @param shouldQueue
	 */
	private void deferEvents(boolean shouldQueue) {
		queueEvents = shouldQueue;
		
		if (queueEvents == false) { 
			for (int eventIdx = queuedEvents.nextSetBit(0); eventIdx >= 0; eventIdx = queuedEvents
					.nextSetBit(eventIdx + 1)) {

				firePropertyChange(eventIdx);
			}
			
			queuedEvents.clear();
		}
	}
	
	protected void setTitle(String newTitle) {
		if (Util.equals(title, newTitle)) {
			return;
		}
		
		title = newTitle;
		firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
	}
	
	protected void setPartName(String newPartName) {
		if (Util.equals(partName, newPartName)) {
			return;
		}
		
		partName = newPartName;
		firePropertyChange(IWorkbenchPartConstants.PROP_PART_NAME);
	}
	
	protected void setContentDescription(String newContentDescription) {
		if (Util.equals(contentDescription, newContentDescription)) {
			return;
		}
		
		contentDescription = newContentDescription;
		firePropertyChange(IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION);
	}
	
	protected void setToolTip(String newToolTip) {
		if (Util.equals(tooltip, newToolTip)) {
			return;
		}
		
		tooltip = newToolTip;
		firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
	}
	
	protected void partPropertyChanged(Object source, int propId) {
		
		// We handle these properties directly (some of them may be transformed
		// before firing events to workbench listeners)
		if (propId == IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION 
				|| propId == IWorkbenchPartConstants.PROP_PART_NAME
				|| propId == IWorkbenchPartConstants.PROP_TITLE) {
			
			refreshFromPart();			
		} else {
			// Any other properties are just reported to listeners verbatim
			firePropertyChange(propId);
		}

	}
	
	/**
	 * Refreshes all cached values with the values from the real part 
	 */
	protected void refreshFromPart() {
		deferEvents(true);
		
		setPartName(computePartName());
		setTitle(computeTitle());
		setContentDescription(computeContentDescription());
		setToolTip(getRawToolTip());
		
		if (!Util.equals(this.image, part.getTitleImage())) {
			firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
		}
		
		deferEvents(false);
	}
	
	public void init(String id,String title,String tooltip,ImageDescriptor desc, String paneName, String contentDescription) {
		this.id = id;
		this.title = title;
		this.tooltip = tooltip;
		this.imageDescriptor = desc;
		this.partName = paneName;
		this.contentDescription = contentDescription;
	}
	
	/**
	 * Releases any references maintained by this part reference
	 * when its actual part becomes known (not called when it is disposed).
	 */
	public void releaseReferences() {
		
		id = null;
		if (image != null && imageDescriptor != null) {
			//make sure part has inc. the reference count.
			if(part != null)
				part.getTitleImage();
			ReferenceCounter imageCache = WorkbenchImages.getImageCache();
			image = (Image)imageCache.get(imageDescriptor);
			if(image != null) {
				imageCache.removeRef(imageDescriptor);
			}
			image = null;
			imageDescriptor = null;
		}
	}
	/**
	 * @see IWorkbenchPart
	 */
	public void addPropertyListener(IPropertyListener listener) {
		propChangeListeners.add(listener);
	}
	
	/**
	 * @see IWorkbenchPart
	 */
	public void removePropertyListener(IPropertyListener listener) {
		propChangeListeners.remove(listener);
	}
	
	public String getId() {
		if(part != null) {			
		    IWorkbenchPartSite site = part.getSite();
		    if (site != null)
		        return site.getId();
		}
		return Util.safeString(id);
	}

	public String getTitleToolTip() {
		return Util.safeString(tooltip);
	}
	
	protected final String getRawToolTip() {
		return Util.safeString(part.getTitleToolTip());
	}
	
	/**
	 * Returns the pane name for the part
	 * 
	 * @return the pane name for the part
	 */
	public String getPartName() {
		return Util.safeString(partName);
	}
	
	/**
	 * Gets the part name directly from the associated workbench part,
	 * or the empty string if none.
	 * 
	 * @return
	 */
	protected final String getRawPartName() {
		String result = ""; //$NON-NLS-1$
		
		if (part instanceof IWorkbenchPart2) {
			IWorkbenchPart2 part2 = (IWorkbenchPart2)part;
			
			result = Util.safeString(part2.getPartName());
		} 
		
		return result;
	}
	
	protected String computePartName() {
		return getRawPartName();
	}
		
	/**
	 * Returns the content description for this part.
	 * 
	 * @return the pane name for the part
	 */
	public String getContentDescription() {
		return Util.safeString(contentDescription);
	}

	/**
	 * Computes a new content description for the part. Subclasses may override to change the
	 * default behavior
	 * 
	 * @return the new content description for the part
	 */
	protected String computeContentDescription() {
		return getRawContentDescription();
	}
	
	/**
	 * Returns the content description as set directly by the part, or the empty string if none
	 * 
	 * @return the unmodified content description from the part (or the empty string if none)
	 */
	protected final String getRawContentDescription() {
		if (part instanceof IWorkbenchPart2) {
			IWorkbenchPart2 part2 = (IWorkbenchPart2)part;
			
			return part2.getContentDescription();
		} 
		
		return ""; //$NON-NLS-1$				
	}
	
	public boolean isDirty() {
		return false;
	}
		
	
	public String getTitle() {
		return Util.safeString(title);
	}
	
	/**
	 * Computes a new title for the part. Subclasses may override to change the default behavior.
	 * 
	 * @return the title for the part
	 */
	protected String computeTitle() {
		return getRawTitle();
	}
	
	/**
	 * Returns the unmodified title for the part, or the empty string if none
	 * 
	 * @return the unmodified title, as set by the IWorkbenchPart. Returns the empty string if none.
	 */
	protected final String getRawTitle() {
		return Util.safeString(part.getTitle());
	}
	
	public Image getTitleImage() {
		if(part != null)
			return part.getTitleImage();
		if(image != null)
			return image;
		if(imageDescriptor == null)
			return null;
		ReferenceCounter imageCache = WorkbenchImages.getImageCache();
		image = (Image)imageCache.get(imageDescriptor);
		if(image != null) {
			imageCache.addRef(imageDescriptor);
			return image;
		}
		image = imageDescriptor.createImage();
		imageCache.put(imageDescriptor,image);
		return image;		
	}	
	
	private void firePropertyChange(int id) {
	
		if (queueEvents) {
			queuedEvents.set(id);
			return;
		}
		
		Object listeners[] = propChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IPropertyListener)listeners[i]).propertyChanged(part, id);
		}
	}
	
	public void setPart(IWorkbenchPart part) {
		this.part = part;
		if(part == null)
			return;

		part.addPropertyListener(propertyChangeListener);
		PartSite site = (PartSite)part.getSite();
		if(site != null && this.pane != null) {
			site.setPane(this.pane);
			this.pane = null;
		}
		
		// Note: it might make sense to call refreshFromPart() here to immediately
		// get the updated values from the part itself. However, we wait until after
		// the widgetry is created to avoid breaking parts that can't return meaningful
		// values until their widgetry exists.
	}
	
	public void setPane(PartPane pane) {
		if(pane == null)
			return;
		if(part != null) {
			PartSite site = (PartSite)part.getSite();
			if (site != null) {
			    site.setPane(pane);
			    return;
			}
		}
		this.pane = pane;
	}
	public PartPane getPane() {	    
		PartPane result = null;
		if(part != null) {			
		    PartSite partSite = (PartSite)part.getSite();
		    if (partSite != null)
		        result = partSite.getPane();
		}
		if(result == null)
			result = pane;
		return result;
	}	
	public void dispose() {
	
		propChangeListeners.clear();
		if(image != null && imageDescriptor != null) {
			ReferenceCounter imageCache = WorkbenchImages.getImageCache();
			if(image != null) {
				int count = imageCache.removeRef(imageDescriptor);
				if(count <= 0)
					image.dispose();				
			}
			imageDescriptor = null;
			image = null;
		}
		if(part != null) {
			part.removePropertyListener(propertyChangeListener);
			part.dispose();
		}
		part = null;
	}	
	
}
