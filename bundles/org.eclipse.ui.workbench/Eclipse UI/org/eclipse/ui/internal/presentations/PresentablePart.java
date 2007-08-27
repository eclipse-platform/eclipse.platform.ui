/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos, IBM; Chris Torrence, ITT Visual Information Solutions - bug 51580
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
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
    
    private ListenerList partPropertyChangeListeners = new ListenerList();
    
    private IPropertyChangeListener lazyPartPropertyChangeListener;

    // Lazily initialized. Use getMenu() to access 
    private IPartMenu viewMenu;
    
    // True iff the "set" methods on this object are talking to the real part (disabled
    // if the part is currently being managed by another presentation stack)
    private boolean enableInputs = true;
    
    // True iff the "get" methods are returning up-to-date info from the real part (disabled
    // for efficency if the presentation is invisible)
    private boolean enableOutputs = true;
    private Rectangle savedBounds = new Rectangle(0,0,0,0);
    private boolean isVisible = false;
    
    // Saved state (only used when the part is inactive)
    private String name = ""; //$NON-NLS-1$
    private String titleStatus = ""; //$NON-NLS-1$
    private boolean isDirty = false;
    private boolean isBusy = false;
    private boolean hasViewMenu = false;
   
    /**
     * Constructor
     * 
     * @param part
     */
    public PresentablePart(PartPane part, Composite parent) {
        this.part = part;
        getPane().addPropertyListener(getPropertyListenerProxy());
        getPane().addPartPropertyListener(getPartPropertyListenerProxy());
    }

    public PartPane getPane() {
        return part;
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
    
    private IPropertyChangeListener getPartPropertyListenerProxy() {
		if (lazyPartPropertyChangeListener == null) {
			lazyPartPropertyChangeListener = new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					PropertyChangeEvent e = new PropertyChangeEvent(this,
							event.getProperty(), event.getOldValue(), event.getNewValue());
					firePartPropertyChange(e);
				}
			};
		}
		return lazyPartPropertyChangeListener;
	}

    /**
	 * Detach this PresentablePart from the real part. No further methods should
	 * be invoked on this object.
	 */
    public void dispose() {
        // Ensure that the property listener is detached (necessary to prevent leaks)
        getPane().removePropertyListener(getPropertyListenerProxy());
        getPane().removePartPropertyListener(getPartPropertyListenerProxy());

        // Null out the various fields to ease garbage collection (optional)
        part = null;
        listeners.clear();
        listeners = null;
        partPropertyChangeListeners.clear();
        partPropertyChangeListeners = null;
    }

    public void firePropertyChange(int propertyId) {
        for (int i = 0; i < listeners.size(); i++) {
            ((IPropertyListener) listeners.get(i)).propertyChanged(this, propertyId);
        }
    }

    public void addPropertyListener(final IPropertyListener listener) {
        listeners.add(listener);
    }

    public void removePropertyListener(final IPropertyListener listener) {
        listeners.remove(listener);
    }

    protected void firePartPropertyChange(PropertyChangeEvent event) {
		Object[] l = partPropertyChangeListeners.getListeners();
		for (int i = 0; i < l.length; i++) {
			((IPropertyChangeListener) l[i]).propertyChange(event);
		}
	}
    
    public void addPartPropertyListener(IPropertyChangeListener listener) {
    	partPropertyChangeListeners.add(listener);
    }
    
    public void removePartPropertyListener(IPropertyChangeListener listener) {
    	partPropertyChangeListeners.remove(listener);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#setBounds(org.eclipse.swt.graphics.Rectangle)
     */
    public void setBounds(Rectangle bounds) {
        savedBounds = bounds;
        if (enableInputs && !SwtUtil.isDisposed(part.getControl())) {
            part.setBounds(bounds);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#setVisible(boolean)
     */
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        if (enableInputs) {
            part.setVisible(isVisible);
        }
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

    
    private WorkbenchPartReference getPartReference() {
        return (WorkbenchPartReference) part.getPartReference();
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#getName()
     */
    public String getName() {
        if (enableOutputs) {
            return getPartReference().getPartName();
		}
		return name;
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
        if (enableOutputs) {
			return getPartReference().getContentDescription();
		}

		return titleStatus;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.IPresentablePart#getTitleImage()
	 */
    public Image getTitleImage() {
//        
//        return PlatformUI.getWorkbench().getSharedImages().getImage(
//                ISharedImages.IMG_DEF_VIEW); 
//        
        if (enableOutputs) {
            return getPartReference().getTitleImage();
		}

		return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_DEF_VIEW);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.IPresentablePart#getTitleToolTip()
	 */
    public String getTitleToolTip() {
        return getPartReference().getTitleToolTip();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentablePart#isDirty()
     */
    public boolean isDirty() {
        if (enableOutputs) {
			return getPartReference().isDirty();
		}
		return isDirty;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.IPresentablePart#isBusy()
	 */
    public boolean isBusy() {
        if (enableOutputs) {
			return part.isBusy();
		}
		return isBusy;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.IPresentablePart#getToolBar()
	 */
    public Control getToolBar() {
        if (enableOutputs) {
			return getPane().getToolBar();
		}
		return null;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.presentations.IPresentablePart#getMenu()
	 */
    public IPartMenu getMenu() {
        boolean hasMenu;
        
        if (enableOutputs) {
            hasMenu = part.hasViewMenu();
        } else {
            hasMenu = this.hasViewMenu;
        }
        
        if (!hasMenu) {
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

    public void enableOutputs(boolean isActive) {
        if (isActive == this.enableOutputs) {
            return;
        }
        
        this.enableOutputs = isActive;
        
        if (isActive) {
            if (isBusy != getPane().isBusy()) {
                firePropertyChange(PROP_BUSY);
            }
            if (isDirty != isDirty()) {
                firePropertyChange(PROP_DIRTY);
            }
            if (!name.equals(getName())) {
                firePropertyChange(PROP_PART_NAME);
            }
            if (!titleStatus.equals(getTitleStatus())) {
                firePropertyChange(PROP_CONTENT_DESCRIPTION);
            }
            if (hasViewMenu != getPane().hasViewMenu()) {
                firePropertyChange(PROP_PANE_MENU);
            }
            // Always assume that the toolbar and title has changed (keeping track of this for real
            // would be too expensive)
            firePropertyChange(PROP_TOOLBAR);
            firePropertyChange(PROP_TITLE);
            
            getPane().addPropertyListener(getPropertyListenerProxy());
        } else {
            getPane().removePropertyListener(getPropertyListenerProxy());
            
            WorkbenchPartReference ref = getPartReference();
            isBusy = getPane().isBusy();
            isDirty = ref.isDirty();
            name = ref.getPartName();
            titleStatus = ref.getContentDescription();
            hasViewMenu = getPane().hasViewMenu();
            firePropertyChange(PROP_TITLE);
            firePropertyChange(PROP_TOOLBAR);
        }
    }
    
    public void enableInputs(boolean isActive) {
        if (isActive == this.enableInputs) {
            return;
        }
        
        this.enableInputs = isActive;
        
        if (isActive) {
            if (isActive && !SwtUtil.isDisposed(part.getControl())) {
                part.setBounds(savedBounds);
            }
            
            part.setVisible(isVisible);
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IPresentablePart#getPartProperty(java.lang.String)
	 */
	public String getPartProperty(String key) {
		return getPartReference().getPartProperty(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISizeProvider#computePreferredSize(boolean, int, int, int)
	 */
	public int computePreferredSize(boolean width, int availableParallel,
	        int availablePerpendicular, int preferredResult) {

	    return getPane().computePreferredSize(width, availableParallel,
	            availablePerpendicular, preferredResult);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISizeProvider#getSizeFlags(boolean)
	 */
	public int getSizeFlags(boolean width) {
	    return getPane().getSizeFlags(width);
	}


}
