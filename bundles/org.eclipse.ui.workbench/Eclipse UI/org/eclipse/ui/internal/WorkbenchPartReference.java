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
package org.eclipse.ui.internal;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;

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
	
	public WorkbenchPartReference() {
	    //no-op
	}
	
	public void init(String id,String title,String tooltip,ImageDescriptor desc) {
		this.id = id;
		this.title = title;
		this.tooltip = tooltip;
		this.imageDescriptor = desc;
	}
	
	/**
	 * Releases any references maintained by this part reference
	 * when its actual part becomes known (not called when it is disposed).
	 */
	public void releaseReferences() {
		id = null;
		tooltip = null;
		title = null;
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
		propChangeListeners.clear();
	}
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
	public String getId() {
		if(part != null) {			
		    IWorkbenchPartSite site = part.getSite();
		    if (site != null)
		        return site.getId();
		}
		return id;
	}

	public String getTitleToolTip() {
		if(part != null)
			return part.getTitleToolTip();
		else
			return tooltip;
	}	
	public String getTitle() {
		String result = title;
		if(part != null)
			result = part.getTitle();
		if(result == null)
			result = new String();
		return result;
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
	public void setPart(IWorkbenchPart part) {
		this.part = part;
		if(part == null)
			return;
		Object listeners[] = propChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			part.addPropertyListener((IPropertyListener)listeners[i]);
		}
		PartSite site = (PartSite)part.getSite();
		if(site != null && this.pane != null) {
			site.setPane(this.pane);
			this.pane = null;
		}
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
		if(part != null)
			part.dispose();
		part = null;
	}	
	public abstract String getRegisteredName();
}
