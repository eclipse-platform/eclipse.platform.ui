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

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.presentations.PresentablePart;
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
public abstract class PartPane extends LayoutPart implements Listener {

    protected PresentablePart presentableAdapter = new PresentablePart(this);

    public static final String PROP_ZOOMED = "zoomed"; //$NON-NLS-1$

    private boolean isZoomed = false;

    private MenuManager paneMenuManager;

    protected IWorkbenchPartReference partReference;

    protected WorkbenchPage page;

    protected Composite control;

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
                        parentControl.traverse(e.detail);
                        e.doit = false;
                    }
                }
            }
        }

    };

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
        ((WorkbenchPartReference) partReference).setPane(this);
    }
	
    /**
     * Factory method for creating the SWT Control hierarchy for this Pane's child.
     */
    protected void createChildControl() {
        final IWorkbenchPart part[] = new IWorkbenchPart[] { partReference
                .getPart(true) };
        if (part[0] == null)
            return;

        Assert.isNotNull(control);
        
        // must call createControl(..) first.
        if (control == null)
            return;

        // Make sure the child control has not been created yet
        if (control.getChildren().length != 0)
            return;

        final Composite content = new Composite(control, SWT.NONE);
        content.setLayout(new FillLayout());

        String error = WorkbenchMessages
                .format(
                        "PartPane.unableToCreate", new Object[] { partReference.getTitle() }); //$NON-NLS-1$
        Platform.run(new SafeRunnable(error) {
            public void run() {
                try {
                    UIStats.start(UIStats.CREATE_PART_CONTROL, id);
                    part[0].createPartControl(content);

                    Rectangle oldBounds = control.getBounds();

                    ((WorkbenchPartReference) getPartReference())
                            .refreshFromPart();

                    // Unless refreshing the part has somehow triggered a layout, 
                    // we need to force a layout now. (SWT only triggers a layout if the
                    // bounds change, so check that case here).
                    if (oldBounds.equals(control.getBounds())) {
                        control.layout(true);
                    }
                } finally {
                    UIStats.end(UIStats.CREATE_PART_CONTROL, id);
                }
            }

            public void handleException(Throwable e) {
                // Log error.
                Workbench wb = (Workbench) PlatformUI.getWorkbench();
                if (!wb.isStarting())
                    super.handleException(e);

                // Dispose old part.
                Control children[] = content.getChildren();
                for (int i = 0; i < children.length; i++) {
                    children[i].dispose();
                }

                // Create new part.
                IWorkbenchPart newPart = createErrorPart(part[0]);
                part[0].getSite().setSelectionProvider(null);
                newPart.createPartControl(content);
                ((WorkbenchPartReference) partReference).setPart(newPart);
                part[0] = newPart;
            }
        });
        page.addPart(partReference);
        page.firePartOpened(part[0]);
    }

    public void addSizeMenuItem(Menu menu, int index) {
        //Add size menu
        MenuItem item = new MenuItem(menu, SWT.CASCADE, index);
        item.setText(WorkbenchMessages.getString("PartPane.size")); //$NON-NLS-1$
        Menu sizeMenu = new Menu(menu);
        item.setMenu(sizeMenu);
        addSizeItems(sizeMenu);
    }

    /**
     * 
     */
    public void createControl(Composite parent) {
        if (getControl() != null)
            return;

        // Create view form.	
        control = new Composite(parent, SWT.NONE);
        control.setLayout(new FillLayout());
        // the part should never be visible by default.  It will be made visible 
        // by activation.  This allows us to have views appear in tabs without 
        // becoming active by default.
        control.setVisible(false);

        // Create a title bar.
        createTitleBar();

        // When the pane or any child gains focus, notify the workbench.
        control.addListener(SWT.Activate, this);

        control.addTraverseListener(traverseListener);
    }

    protected abstract IWorkbenchPart createErrorPart(IWorkbenchPart oldPart);

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
        if (getWindow() instanceof IWorkbenchWindow)
            page.toggleZoom(partReference);
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

//    /*
//     * @see LayoutPart#getMinimumHeight()
//     */
//    public int computeMinimumSize(boolean width, int knownHeight) {
//        if (control == null || control.isDisposed())
//            return super.computeMinimumSize(width, knownHeight);
//
//        // account for the borders
//        int result = Geometry.getDimension(control.computeTrim(0, 0, 0, 0), width);
//        return result;
//    }

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
        if (event.type == SWT.Activate)
            requestActivation();
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
        if (getControl() != null)
            getControl().moveAbove(refControl);
    }

    /**
     * Notify the workbook page that the part pane has
     * been activated by the user.
     */
    protected void requestActivation() {
        this.page.requestActivation(partReference.getPart(true));
    }

    /**
     * Sets the parent for this part.
     */
    public void setContainer(ILayoutContainer container) {
        super.setContainer(container);
    }

    /**
     * Shows the receiver if <code>visible</code> is true otherwise hide it.
     */
    public void setVisible(boolean makeVisible) {
        if (makeVisible) {
        	createChildControl();
        }
        super.setVisible(makeVisible);
    }

    /**
     * Sets focus to this part.
     */
    public void setFocus() {
        requestActivation();
        IWorkbenchPart part = partReference.getPart(true);
        if (part != null) {
            part.setFocus();
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
        if (this.isZoomed == isZoomed)
            return; // do nothing if we're already in the right state.

        super.setZoomed(isZoomed);

        this.isZoomed = isZoomed;

        final Object[] listeners = getPropertyListeners().getListeners();
        if (listeners.length > 0) {
            Boolean oldValue = isZoomed ? Boolean.FALSE : Boolean.TRUE;
            Boolean zoomed = isZoomed ? Boolean.TRUE : Boolean.FALSE;
            PropertyChangeEvent event = new PropertyChangeEvent(this,
                    PROP_ZOOMED, oldValue, zoomed);
            for (int i = 0; i < listeners.length; ++i)
                ((IPropertyChangeListener) listeners[i]).propertyChange(event);
        }
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
            folder.showSystemMenu();
        }
    }

    /**
     * Show the context menu for this part.
     */
    public void showViewMenu() {
        PartStack folder = getStack();

        if (folder != null) {
            folder.showPaneMenu();
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
                    if (toGetFocusWhenDone != null)
                        toGetFocusWhenDone.setFocus();
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
        item.setText(labelMessage); //$NON-NLS-1$
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
                WorkbenchMessages.getString("PartPane.sizeLeft"), sashes.left); //$NON-NLS-1$
        addSizeItem(sizeMenu,
                WorkbenchMessages.getString("PartPane.sizeRight"), sashes.right); //$NON-NLS-1$
        addSizeItem(sizeMenu,
                WorkbenchMessages.getString("PartPane.sizeTop"), sashes.top); //$NON-NLS-1$
        addSizeItem(sizeMenu, WorkbenchMessages
                .getString("PartPane.sizeBottom"), sashes.bottom); //$NON-NLS-1$
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
        //Do nothing by default
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
     * Ensure that we are not in the zoomed before reparenting.
     * TODO: I am certain this isn't correct but I'll be damned if I know what is.
     */
    public void reparent(Composite newParent) {
        if (isZoomed())
            setZoomed(false);
        super.reparent(newParent);
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
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#getPresentablePart()
     */
    public IPresentablePart getPresentablePart() {
        return presentableAdapter;
    }

}