/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.presentations.IPartMenu;
import org.eclipse.ui.presentations.IPresentablePart;

public class PresentableViewPart implements IPresentablePart {

    private final List listeners = new ArrayList();
 
    private ViewPane pane;
    
    private IPartMenu viewMenu = new IPartMenu() {
		public void showMenu(Point location) {
			pane.showViewMenu(location);
		}
    };

    private final IPropertyListener propertyListenerProxy = new IPropertyListener() {

        public void propertyChanged(Object source, int propId) {
            for (int i = 0; i < listeners.size(); i++)
                ((IPropertyListener) listeners.get(i)).propertyChanged(
                        PresentableViewPart.this, propId);
        }
    };

    public PresentableViewPart(ViewPane pane) {
        this.pane = pane;
    }

    public void firePropertyChange(int propertyId) {
		 for (int i = 0; i < listeners.size(); i++)
            ((IPropertyListener) listeners.get(i)).propertyChanged(
                    this, propertyId);    	
    }
    
    public void addPropertyListener(final IPropertyListener listener) {
        if (listeners.isEmpty())
                getViewReference().addPropertyListener(propertyListenerProxy);

        listeners.add(listener);
    }

    public String getName() {
        WorkbenchPartReference ref = (WorkbenchPartReference) pane
                .getPartReference();
        return ref.getRegisteredName();
    }

    public String getTitle() {
        return getViewReference().getTitle();
    }

    public Image getTitleImage() {
        return getViewReference().getTitleImage();
    }

    public String getTitleToolTip() {
        return getViewReference().getTitleToolTip();
    }

    private IViewReference getViewReference() {
        return pane.getViewReference();
    }

    public boolean isDirty() {
        return false;
    }

    public void removePropertyListener(final IPropertyListener listener) {
        listeners.remove(listener);

        if (listeners.isEmpty())
                getViewReference()
                        .removePropertyListener(propertyListenerProxy);
    }

    public void setBounds(Rectangle bounds) {
        pane.setBounds(bounds);
    }

    public void setFocus() {
        pane.setFocus();
    }

    public void setVisible(boolean isVisible) {
        pane.setVisible(isVisible);
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#isBusy()
	 */
	public boolean isBusy() {
		return pane.isBusy();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getToolBar()
	 */
	public Control getToolBar() {
		
		if (!pane.toolbarIsVisible()) {
			return null;
		}
		
		ToolBarManager toolbarManager = pane.getToolBarManager();
		
		if (toolbarManager == null) {
			return null;
		}
		
		ToolBar control = toolbarManager.getControl();
		
		if (control == null || control.isDisposed() ) {
			return null;
		}		
		
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getPartMenu()
	 */
	public IPartMenu getMenu() {
		if (pane.hasViewMenu()) {
			return viewMenu;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getControl()
	 */
	public Control getControl() {
		return pane.getControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getTitleStatus()
	 */
	public String getTitleStatus() {
		String title = getTitle();
		
		String name = getName();
		
		// Return the empty string if the title is unmodified
		if (title.equals(name)) {
			return new String();
		} 

		if (title.startsWith(name)) {
			String substr = title.substring(name.length(), title.length());
			
			substr = substr.trim();
			
			if (substr.startsWith("(")) {
				int end = substr.indexOf(")", 1);
				
				if (end != -1) {
					return substr.substring(1, end);
				}
			}			
		}
		
		return title;
	}
	
	
}