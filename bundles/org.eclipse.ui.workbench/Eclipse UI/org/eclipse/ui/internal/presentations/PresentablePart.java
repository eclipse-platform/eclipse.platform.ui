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
package org.eclipse.ui.internal.presentations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * This is a lightweight adapter that allows PartPanes to be used by a StackPresentation. All methods
 * either redirect directly to PartPane or do trivial type conversions. All listeners registered by
 * the presentation are kept here rather than registering them directly on the PartPane. This allows
 * us to remove all listeners registered by a presentation that has been disposed, offering some
 * protection against memory leaks.
 */
public class PresentablePart implements IPresentablePart {

    private PartPane part;

    /**
     * Local listener list -- we use this rather than registering listeners directly on the part
     * in order to protect against memory leaks in badly behaved presentations.
     */
    private List listeners = new ArrayList();

    // Lazily initialized. Use getPropertyListenerProxy() to access.
    private IPropertyListener lazyPropertyListenerProxy;

    // Lazily initialized. Use getMenu() to access 
    private IPartMenu viewMenu;

    /**
     * Constructor
     * 
     * @param part
     */
    public PresentablePart(PartPane part) {
        this.part = part;
    }

    private IPropertyListener getPropertyListenerProxy() {
        if (lazyPropertyListenerProxy == null) {
            lazyPropertyListenerProxy = new IPropertyListener() {
                public void propertyChanged(Object source, int propId) {
                    firePropertyChange(propId);
                }
            };
        }

        return lazyPropertyListenerProxy;
    }

    private WorkbenchPartReference getPartReference() {
        return (WorkbenchPartReference) part.getPartReference();
    }

    /**
     * Detach this PresentablePart from the real part. No further methods should be invoked
     * on this object.
     */
    public void dispose() {
        // Ensure that the property listener is detached (necessary to prevent leaks)
        getPartReference().removePropertyListener(getPropertyListenerProxy());

        // Null out the various fields to ease garbage collection (optional)
        part = null;
        listeners.clear();
        listeners = null;
    }

    public void firePropertyChange(int propertyId) {
        for (int i = 0; i < listeners.size(); i++) {
            ((IPropertyListener) listeners.get(i)).propertyChanged(this,
                    propertyId);
        }
    }

    public void addPropertyListener(final IPropertyListener listener) {
        if (listeners.isEmpty()) {
            getPartReference().addPropertyListener(getPropertyListenerProxy());
        }

        listeners.add(listener);
    }

    public void removePropertyListener(final IPropertyListener listener) {
        listeners.remove(listener);

        if (listeners.isEmpty()) {
            getPartReference().removePropertyListener(
                    getPropertyListenerProxy());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#setBounds(org.eclipse.swt.graphics.Rectangle)
     */
    public void setBounds(Rectangle bounds) {
        if (!SwtUtil.isDisposed(part.getControl())) {
            part.setBounds(bounds);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#setVisible(boolean)
     */
    public void setVisible(boolean isVisible) {
        part.setVisible(isVisible);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#setFocus()
     */
    public void setFocus() {
        if (!SwtUtil.isDisposed(part.getControl())) {
            if (part.getPage().getActivePart() == part.getPartReference().getPart(false)) { 
                part.setFocus();
            } else {
                part.requestActivation();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#getName()
     */
    public String getName() {
        return getPartReference().getPartName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#getTitle()
     */
    public String getTitle() {
        return getPartReference().getTitle();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#getTitleStatus()
     */
    public String getTitleStatus() {
        return getPartReference().getContentDescription();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#getTitleImage()
     */
    public Image getTitleImage() {
        return getPartReference().getTitleImage();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#getTitleToolTip()
     */
    public String getTitleToolTip() {
        return getPartReference().getTitleToolTip();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#isDirty()
     */
    public boolean isDirty() {
        return getPartReference().isDirty();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#isBusy()
     */
    public boolean isBusy() {
        return part.isBusy();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#getToolBar()
     */
    public Control getToolBar() {
        return part.getToolBar();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#getMenu()
     */
    public IPartMenu getMenu() {
        if (!part.hasViewMenu()) {
            return null;
        }

        if (viewMenu == null) {
            viewMenu = new IPartMenu() {
                public void showMenu(Point location) {
                    part.showViewMenu(location);
                }
            };
        }

        return viewMenu;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#isCloseable()
     */
    public boolean isCloseable() {        
        return part.isCloseable();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#getControl()
     */
    public Control getControl() {
        return part.getControl();
    }

}
