/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.components.framework.IDisposable;
import org.eclipse.ui.internal.part.components.services.IDirtyHandler;
import org.eclipse.ui.internal.part.components.services.IInputHandler;
import org.eclipse.ui.internal.part.components.services.INameable;
import org.eclipse.ui.internal.part.components.services.IPartDescriptor;
import org.eclipse.ui.internal.util.Util;

/**
 * Note: this class is used in an addInstance call -- if this class implements an
 * interface, that interface will override anything that the object had in its local
 * context.
 * 
 * @since 3.1
 */
public class PartPropertyProvider implements IPartPropertyProvider, INameable, IDirtyHandler, IDisposable, IInputHandler {

    // Dependencies
    private INameable nameable;
    private IDirtyHandler dirty;
    private List listeners = new ArrayList();   
    
    private String tooltip = ""; //$NON-NLS-1$
    private String partName;
    private String contentDescription = ""; //$NON-NLS-1$
    private String title = ""; //$NON-NLS-1$
    private boolean isDirty = false;
    private ImageDescriptor titleImage;
    private Image image = null;
    private IEditorInput input;
    private ResourceManager mgr;
    
    private final static class ListenerRec {
        public ListenerRec(IPropertyListener l, IWorkbenchPart p) {
            this.l = l;
            this.part = p;
        }
        
        public boolean equals(Object other) {
            if (!(other instanceof ListenerRec)) return false;
            
            ListenerRec lr = (ListenerRec)other;
            return (lr.l == l) && (lr.part == part);
        }
        
        public IPropertyListener l;
        public IWorkbenchPart part;
    }
    
    public PartPropertyProvider(ResourceManager manager, INameable parentNameable, 
            IDirtyHandler dirtyListener, IPartDescriptor descriptor, IEditorInput initialInput) {
        this.nameable = parentNameable;
        this.dirty = dirtyListener;
        this.partName = descriptor.getLabel();
        this.titleImage = descriptor.getImage();
        this.input = initialInput;
        this.mgr = manager;
    }
    
    private ResourceManager getManager() {
        if (mgr == null) {
            mgr = JFaceResources.getResources();
        }
        return mgr;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#addPropertyListener(org.eclipse.ui.IWorkbenchPart, org.eclipse.ui.IPropertyListener)
     */
    public void addPropertyListener(IWorkbenchPart part, IPropertyListener l) {
        listeners.add(new ListenerRec(l, part));
    }

    public void removePropertyListener(IWorkbenchPart part, IPropertyListener l) {
        listeners.remove(new ListenerRec(l, part));
    }
        
    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getTitleToolTip()
     */
    public String getTitleToolTip() {
        return tooltip;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getTitleImage()
     */
    public Image getTitleImage() {
        if (image == null) {
            if (titleImage == null) {
                return PlatformUI.getWorkbench().getSharedImages().getImage(
                        ISharedImages.IMG_DEF_VIEW);
            }
            
            image = getManager().createImageWithDefault(titleImage);
        }
        
        return image;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getPartName()
     */
    public String getPartName() {
        return partName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getContentDescription()
     */
    public String getContentDescription() {
        return contentDescription;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#getEditorInput()
     */
    public IEditorInput getEditorInput() {
        return input;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.IPartPropertyProvider#isDirty()
     */
    public boolean isDirty() {
        return isDirty;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.component.IDisposable#dispose()
     */
    public void dispose() {
        listeners.clear();
        
        if (image != null) {
            getManager().destroy(titleImage);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.services.INameable#setContentDescription(java.lang.String)
     */
    public void setContentDescription(String contentDescription) {
        if (this.contentDescription.equals(contentDescription)) {
            return;
        }
        
        this.contentDescription = contentDescription;
        
        if (nameable != null) {
            nameable.setContentDescription(contentDescription);
        }
        
        firePropertyChange(IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION);
    }
    
    public void setDirty(boolean isDirty) {
        if (isDirty == this.isDirty) {
            return;
        }
        
        this.isDirty = isDirty;
        
        if (dirty != null) {
            dirty.setDirty(isDirty);
        }
        
        firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.services.INameable#setImage(org.eclipse.jface.resource.ImageDescriptor)
     */
    public void setImage(ImageDescriptor theImage) {
        ImageDescriptor oldImageDescriptor = this.titleImage;
        Image oldImage = image;
        
        if (oldImageDescriptor == theImage) {
            return;
        }
        
        this.titleImage = theImage;
        this.image = null;
        
        if (nameable != null) {
            nameable.setImage(theImage);
        }
        
        firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
        
        if (oldImage != null) {
            getManager().destroy(oldImageDescriptor);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.services.INameable#setName(java.lang.String)
     */
    public void setName(String newName) {
        if (this.partName.equals(newName)) {
            return;
        }
        
        this.partName = newName;
        
        if (nameable != null) {
            nameable.setName(newName);
        }
        
        firePropertyChange(IWorkbenchPartConstants.PROP_PART_NAME);
        
        setDefaultTitle();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.services.INameable#setTooltip(java.lang.String)
     */
    public void setTooltip(String toolTip) {
        if (this.tooltip.equals(toolTip)) {
            return;
        }
        
        this.tooltip = toolTip;
        
        if (nameable != null) {
            nameable.setTooltip(toolTip);
        }
        
        firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
    }

    /**
     * @param input The input to set.
     */
    public void setEditorInput(IEditorInput input) {
        if (this.input.equals(input)) {
            return;   
        }
        
        this.input = input;
        
        firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
    }

    private void setTitle(String newTitle) {
        if (newTitle.equals(this.title)) {
            return;
        }
        
        this.title = newTitle;
        firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
    }
    
    void setDefaultTitle() {
        String description = getContentDescription();
        String name = getPartName();
        String newTitle = name;

        if (!Util.equals(description, "")) { //$NON-NLS-1$
            newTitle = MessageFormat
                    .format(
                            WorkbenchMessages.WorkbenchPart_AutoTitleFormat, new String[] { name, description });
        }

        setTitle(newTitle);
    }
    
    /**
     * Fires a property changed event.
     *
     * @param propertyId the id of the property that changed
     */
    protected void firePropertyChange(final int propertyId) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            final ListenerRec rec = (ListenerRec) iter.next();

            Platform.run(new SafeRunnable() {
                public void run() {
                    rec.l.propertyChanged(rec.part, propertyId);
                }
            });
        }        
    }
}
