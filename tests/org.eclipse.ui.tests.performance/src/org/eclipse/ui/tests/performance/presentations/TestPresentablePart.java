/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance.presentations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;

public class TestPresentablePart implements IPresentablePart {

    private List listeners = new ArrayList();
    
    private Composite control;
    private String name = "";
    private String title = "";
    private String status = "";
    private Image image;
    private String tooltip = "";
    private boolean dirty;
    private ToolBar toolbar;
    
    public TestPresentablePart(Composite parent, Image image) {
        control = new Composite(parent, SWT.NONE);
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                disposed();
            } 
        });
        
        // Add some items to the toolbar
        toolbar = new ToolBar(parent, SWT.WRAP);
        for (int idx = 0; idx < 6; idx++) {
            ToolItem item = new ToolItem(toolbar, SWT.PUSH);
            item.setImage(image);
        }
        this.image = image;
    }
    
    // Set methods called from presentation (all ignored)
    public void setBounds(Rectangle bounds) {
        control.setBounds(bounds);
    }
    
    public void setVisible(boolean isVisible) {
        control.setVisible(isVisible);
        toolbar.setVisible(isVisible);
    }
    
    public void setFocus() {
        control.setFocus();
    }

    public void addPropertyListener(IPropertyListener listener) {
        listeners.add(listener);
    }

    public void removePropertyListener(IPropertyListener listener) {
        listeners.remove(listener);
    }

    private void firePropertyChange(int propertyId) {
        for (int i = 0; i < listeners.size(); i++) {
            ((IPropertyListener) listeners.get(i)).propertyChanged(this,
                    propertyId);
        }
    }
    
    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleStatus() {
        return status;
    }

    public Image getTitleImage() {
        return image;
    }

    public String getTitleToolTip() {
        return tooltip;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isBusy() {
        return false;
    }

    public boolean isCloseable() {
        return true;
    }

    public Control getToolBar() {
        return toolbar;
    }

    public IPartMenu getMenu() {
        return null;
    }

    public Control getControl() {
        return control;
    }

    public void disposed() {
        toolbar.dispose();
        toolbar = null;
    }
    
    public void setTitle(String title) {
        this.title = title;
        firePropertyChange(IPresentablePart.PROP_TITLE);
    }
    
    public void setName(String name) {
        this.name = name;
        firePropertyChange(IPresentablePart.PROP_PART_NAME);
    }
    
    public void setImage(Image newImage) {
        this.image = newImage;
        firePropertyChange(IPresentablePart.PROP_TITLE);
    }
    
    public void setContentDescription(String descr) {
        this.status = descr;
        firePropertyChange(IPresentablePart.PROP_CONTENT_DESCRIPTION);
    }
    
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
        firePropertyChange(IPresentablePart.PROP_TITLE);
    }
    
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        firePropertyChange(IPresentablePart.PROP_DIRTY);
    }

    public ToolItem addToToolbar(Image toAdd) {
        ToolItem item = new ToolItem(toolbar, SWT.PUSH);
        item.setImage(toAdd);
        firePropertyChange(IPresentablePart.PROP_TOOLBAR);
        return item;
    }
    
    public void removeFromToolbar(ToolItem toRemove) {
        toRemove.dispose();
        firePropertyChange(IPresentablePart.PROP_TOOLBAR);
    }

    private ListenerList partPropertyListeners = new ListenerList();
    
    private Map partProperties = new HashMap();
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#addPartPropertyListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPartPropertyListener(IPropertyChangeListener listener) {
		partPropertyListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getPartProperty(java.lang.String)
	 */
	public String getPartProperty(String key) {
		return (String) partProperties.get(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#removePartPropertyListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePartPropertyListener(IPropertyChangeListener listener) {
		partPropertyListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISizeProvider#computePreferredSize(boolean, int, int, int)
	 */
	public int computePreferredSize(boolean width, int availableParallel,
			int availablePerpendicular, int preferredResult) {
		return preferredResult;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISizeProvider#getSizeFlags(boolean)
	 */
	public int getSizeFlags(boolean width) {
		return 0;
	}
    
}
