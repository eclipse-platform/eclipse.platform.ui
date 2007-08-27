/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Xenos, IBM; Chris Torrence, ITT Visual Information Solutions - bug 51580
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * Provides the common behavior for both views
 * and editor panes.
 * 
 * TODO: Delete ViewPane and EditorPane, and make this class non-abstract.
 * 
 * TODO: Stop subclassing LayoutPart. This class cannot be interchanged with other LayoutParts.
 * Pointers that refer to PartPane instances should do so directly rather than referring to
 * LayoutPart and downcasting. The getPresentablePart() method only applies to PartPanes, and
 * should be removed from LayoutPart.
 */
public abstract class PartPane extends LayoutPart implements IPropertyListener,
		Listener, IPropertyChangeListener {

    public static final String PROP_ZOOMED = "zoomed"; //$NON-NLS-1$

    private boolean isZoomed = false;

    private MenuManager paneMenuManager;
    private ListenerList listeners = new ListenerList();
    private ListenerList partListeners = new ListenerList();

    protected IWorkbenchPartReference partReference;

    protected WorkbenchPage page;

    protected Composite control;

    private boolean inLayout = true;
    
    private TraverseListener traverseListener = new TraverseListener() {
        /* (non-Javadoc)
         * @see org.eclipse.swt.events.TraverseListener#keyTraversed(org.eclipse.swt.events.TraverseEvent)
         */
        public void keyTraversed(TraverseEvent e) {
            // Hack: Currently, SWT sets focus whenever we call Control.traverse. This doesn't
            // cause too much of a problem for ctrl-pgup and ctrl-pgdn, but it is seriously unexpected
            // for other traversal events. When (and if) it becomes possible to call traverse() without
            // forcing a focus change, this if statement should be removed and ALL events should be
            // forwarded to the container.
            if (e.detail == SWT.TRAVERSE_PAGE_NEXT
                    || e.detail == SWT.TRAVERSE_PAGE_PREVIOUS) {
                ILayoutContainer container = getContainer();
                if (container != null && container instanceof LayoutPart) {
                    LayoutPart parent = (LayoutPart) container;
                    Control parentControl = parent.getControl();
                    if (parentControl != null && !parentControl.isDisposed()) {
                        e.doit = parentControl.traverse(e.detail);
                        if (e.doit) {
							e.detail = SWT.TRAVERSE_NONE;
						}
                    }
                }
            }
        }

    };

	private boolean busy;

    public static class Sashes {
        public Sash left;

        public Sash right;

        public Sash top;

        public Sash bottom;
    }

    /**
     * Construct a pane for a part.
     */
    public PartPane(IWorkbenchPartReference partReference,
            WorkbenchPage workbenchPage) {
        super(partReference.getId());
        this.partReference = partReference;
        this.page = workbenchPage;
    }

    public void addSizeMenuItem(Menu menu, int index) {
        //Add size menu
        MenuItem item = new MenuItem(menu, SWT.CASCADE, index);
        item.setText(WorkbenchMessages.PartPane_size);
        Menu sizeMenu = new Menu(menu);
        item.setMenu(sizeMenu);
        addSizeItems(sizeMenu);
    }

    /**
     * 
     */
    public void createControl(Composite parent) {
        if (getControl() != null) {
			return;
		}

        partReference.addPropertyListener(this);
        partReference.addPartPropertyListener(this);
        // Create view form.	
        control = new Composite(parent, SWT.NONE);
        control.setLayout(new FillLayout());
        // the part should never be visible by default.  It will be made visible 
        // by activation.  This allows us to have views appear in tabs without 
        // becoming active by default.
        control.setVisible(false);
        control.moveAbove(null);
        // Create a title bar.
        createTitleBar();

        
        // When the pane or any child gains focus, notify the workbench.
        control.addListener(SWT.Activate, this);

        control.addTraverseListener(traverseListener);
    }

    /**
     * Create a title bar for the pane if required.
     */
    protected abstract void createTitleBar();

    /**
     * @private
     */
    public void dispose() {
        super.dispose();

        if ((control != null) && (!control.isDisposed())) {
            control.removeListener(SWT.Activate, this);
            control.removeTraverseListener(traverseListener);
            control.dispose();
            control = null;
        }
        if ((paneMenuManager != null)) {
            paneMenuManager.dispose();
            paneMenuManager = null;
        }
        
        partReference.removePropertyListener(this);
        partReference.removePartPropertyListener(this);
    }

    /**
     * User has requested to close the pane.
     * Take appropriate action depending on type.
     */
    abstract public void doHide();

    /**
     * Zooms in on the part contained in this pane.
     */
    protected void doZoom() {
        if (isDocked()) {
			page.toggleZoom(partReference);
		}
    }

    /**
     * Gets the presentation bounds.
     */
    public Rectangle getBounds() {
        return getControl().getBounds();
    }

    /**
     * Get the control.
     */
    public Control getControl() {
        return control;
    }

    /**
     * Answer the part child.
     */
    public IWorkbenchPartReference getPartReference() {
        return partReference;
    }

    /**
     * @see Listener
     */
    public void handleEvent(Event event) {
        if (event.type == SWT.Activate) {
            if (inLayout) {
                requestActivation();
            }
        }
    }

    /**
     * Return whether the pane is zoomed or not
     */
    public boolean isZoomed() {
        return isZoomed;
    }

    /**
     * Move the control over another one.
     */
    public void moveAbove(Control refControl) {
        if (getControl() != null) {
			getControl().moveAbove(refControl);
		}
    }

    /**
     * Notify the workbook page that the part pane has
     * been activated by the user.
     */
    public void requestActivation() {
        IWorkbenchPart part = partReference.getPart(true);
        // Cannot activate the outer bit of a MultiEditor. In previous versions of the 
        // workbench, MultiEditors had their own implementation of EditorPane for the purpose
        // of overriding requestActivation with a NOP... however, keeping the old pattern would
        // mean it is necessary to eagerly activate an editor's plugin in order to determine
        // what type of pane to create.
        if (part instanceof MultiEditor) {
            return;
        }
        
        this.page.requestActivation(part);
    }

    /**
     * Sets the parent for this part.
     */
    public void setContainer(ILayoutContainer container) {
        
        if (container instanceof LayoutPart) {
            LayoutPart part = (LayoutPart) container;
            
            Control containerControl = part.getControl();
            
            if (containerControl != null) {
                Control control = getControl();
                Composite newParent = containerControl.getParent();
                if (control != null && newParent != control.getParent()) {
                    reparent(newParent);
                }
            }
        }
        super.setContainer(container);
    }

    /**
     * Shows the receiver if <code>visible</code> is true otherwise hide it.
     */
    public void setVisible(boolean makeVisible) {
        // Avoid redundant visibility changes
        if (makeVisible == getVisible()) {
            return;
        }
        
    	if (makeVisible) {
    	    partReference.getPart(true);
    	}
    	
        super.setVisible(makeVisible);
        
        ((WorkbenchPartReference) partReference).fireVisibilityChange();
    }
    
    /**
     * Sets focus to this part.
     */
    public void setFocus() {
        requestActivation();

        IWorkbenchPart part = partReference.getPart(true);
        if (part != null) {
            Control control = getControl();
            if (!SwtUtil.isFocusAncestor(control)) {
                // First try to call part.setFocus
                part.setFocus();
            }
        }
    }

    /**
     * Sets the workbench page of the view. 
     */
    public void setWorkbenchPage(WorkbenchPage workbenchPage) {
        this.page = workbenchPage;
    }

    /**
     * Set whether the pane is zoomed or not
     */
    public void setZoomed(boolean isZoomed) {
        if (this.isZoomed == isZoomed) {
			return; // do nothing if we're already in the right state.
		}

        super.setZoomed(isZoomed);

        this.isZoomed = isZoomed;

        ((WorkbenchPartReference) partReference).fireZoomChange();        
    }
    
    /**
     * Informs the pane that it's window shell has
     * been activated.
     */
    /* package */abstract void shellActivated();

    /**
     * Informs the pane that it's window shell has
     * been deactivated.
     */
    /* package */abstract void shellDeactivated();

    /**
     * Indicate focus in part.
     */
    public abstract void showFocus(boolean inFocus);

    /**
     * @see IPartDropTarget::targetPartFor
     */
    public LayoutPart targetPartFor(LayoutPart dragSource) {
        return this;
    }

    /**
     * Returns the PartStack that contains this PartPane, or null if none.
     * 
     * @return
     */
    public PartStack getStack() {
        ILayoutContainer container = getContainer();
        if (container instanceof PartStack) {
            return (PartStack) container;
        }

        return null;
    }

    /**
     * Show a title label menu for this pane.
     */
    public void showPaneMenu() {
        PartStack folder = getStack();

        if (folder != null) {
            folder.showPaneMenu();
        }
    }

    /**
     * Show the context menu for this part.
     */
    public void showSystemMenu() {
        PartStack folder = getStack();

        if (folder != null) {
            folder.showSystemMenu();
        }
    }

    /**
     * Finds and return the sashes around this part.
     */
    protected Sashes findSashes() {
        Sashes result = new Sashes();

        ILayoutContainer container = getContainer();

        if (container == null) {
            return result;
        }

        container.findSashes(this, result);
        return result;
    }

    /**
     * Enable the user to resize this part using
     * the keyboard to move the specified sash
     */
    protected void moveSash(final Sash sash) {
        moveSash(sash, this);
    }

    public static void moveSash(final Sash sash,
            final LayoutPart toGetFocusWhenDone) {
        final KeyListener listener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.ESC || e.character == '\r') {
                    if (toGetFocusWhenDone != null) {
						toGetFocusWhenDone.setFocus();
					}
                }
            }
        };
        sash.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                sash.setBackground(sash.getDisplay().getSystemColor(
                        SWT.COLOR_LIST_SELECTION));
                sash.addKeyListener(listener);
            }

            public void focusLost(FocusEvent e) {
                sash.setBackground(null);
                sash.removeKeyListener(listener);
            }
        });
        sash.setFocus();

    }

    /**
     * Add a menu item to the Size Menu
     */
    protected void addSizeItem(Menu sizeMenu, String labelMessage,
            final Sash sash) {
        MenuItem item = new MenuItem(sizeMenu, SWT.NONE);
        item.setText(labelMessage);
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                moveSash(sash);
            }
        });
        item.setEnabled(!isZoomed() && sash != null);
    }

    /**
     * Returns the workbench page of this pane.
     */
    public WorkbenchPage getPage() {
        return page;
    }

    /**
     * Add the Left,Right,Up,Botton menu items to the Size menu.
     */
    protected void addSizeItems(Menu sizeMenu) {
        Sashes sashes = findSashes();
        addSizeItem(sizeMenu,
                WorkbenchMessages.PartPane_sizeLeft, sashes.left);
        addSizeItem(sizeMenu,
                WorkbenchMessages.PartPane_sizeRight, sashes.right); 
        addSizeItem(sizeMenu,
                WorkbenchMessages.PartPane_sizeTop, sashes.top); 
        addSizeItem(sizeMenu, WorkbenchMessages.PartPane_sizeBottom, sashes.bottom);
    }

    /**
     * Pin this part.
     */
    protected void doDock() {
        // do nothing
    }

    /**
     * Set the busy state of the pane.
     */
    public void setBusy(boolean isBusy) {
        if (isBusy != busy) {
            busy = isBusy;
            firePropertyChange(IPresentablePart.PROP_BUSY);
        }
    }

    /**
     * Show a highlight for the receiver if it is 
     * not currently the part in the front of its
     * presentation.
     *
     */
    public void showHighlight() {
        //No nothing by default
    }

    /**
     * @return
     */
    public abstract Control getToolBar();

    /**
     * @return
     */
    public boolean hasViewMenu() {
        return false;
    }

    /**
     * @param location
     */
    public void showViewMenu(Point location) {

    }
    
    public boolean isBusy() {
        return busy;
    }

    /**
     * Writes a description of the layout to the given string buffer.
     * This is used for drag-drop test suites to determine if two layouts are the
     * same. Like a hash code, the description should compare as equal iff the
     * layouts are the same. However, it should be user-readable in order to
     * help debug failed tests. Although these are english readable strings,
     * they do not need to be translated.
     * 
     * @param buf
     */
    public void describeLayout(StringBuffer buf) {

        IWorkbenchPartReference part = getPartReference();

        if (part != null) {
            buf.append(part.getPartName());
            return;
        }
    }
    
    /**
     * @return
     * @since 3.1
     */
    public abstract boolean isCloseable();
    
    public void setInLayout(boolean inLayout) {
        this.inLayout = inLayout;
    }
    
    public boolean getInLayout() {
        return inLayout;
    }
        
    public boolean allowsAutoFocus() {
        if (!inLayout) {
            return false;
        }
        
        return super.allowsAutoFocus();
    }

    /**
     * Clears all contribution items from the contribution managers (this is done separately
     * from dispose() since it is done after the part is disposed). This is a bit of a hack.
     * Really, the contribution managers should be part of the site, not the PartPane. If these
     * were moved elsewhere, then disposal of the PartPane would be atomic and this method could
     * be removed.
     */
	public void removeContributions() {

	}

    public void addPropertyListener(IPropertyListener listener) {
        listeners.add(listener);
    }

    public void removePropertyListener(IPropertyListener listener) {
        listeners.remove(listener);
    }
 
    public void firePropertyChange(int propertyId) {
        Object listeners[] = this.listeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyListener) listeners[i]).propertyChanged(this, propertyId);
        }
    }
    
    public void propertyChanged(Object source, int propId) {
        firePropertyChange(propId);
    }
    
    public void addPartPropertyListener(IPropertyChangeListener listener) {
    	partListeners.add(listener);
    }
    
    public void removePartPropertyListener(IPropertyChangeListener listener) {
    	partListeners.remove(listener);
    }
    
    public void firePartPropertyChange(PropertyChangeEvent event) {
    	Object[] l = partListeners.getListeners();
    	for (int i = 0; i < l.length; i++) {
			((IPropertyChangeListener)l[i]).propertyChange(event);
		}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
    	firePartPropertyChange(event);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#computePreferredSize(boolean, int, int, int)
     */
    public int computePreferredSize(boolean width, int availableParallel,
            int availablePerpendicular, int preferredParallel) {

        return ((WorkbenchPartReference)partReference).computePreferredSize(width,
                availableParallel, availablePerpendicular, preferredParallel);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#getSizeFlags(boolean)
     */
    public int getSizeFlags(boolean horizontal) {
        return ((WorkbenchPartReference)partReference).getSizeFlags(horizontal);
    }
    
}
