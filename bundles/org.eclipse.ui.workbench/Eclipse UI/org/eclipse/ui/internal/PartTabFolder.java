package org.eclipse.ui.internal;

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials! are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation Cagatay
 * Kavukcuoglu <cagatayk@acm.org>- Fix for bug 10025 - Resizing views should
 * not use height ratios
 ******************************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.presentations.SystemMenuClose;
import org.eclipse.ui.internal.presentations.SystemMenuFastView;
import org.eclipse.ui.internal.presentations.SystemMenuMaximize;
import org.eclipse.ui.internal.presentations.SystemMenuMinimize;
import org.eclipse.ui.internal.presentations.SystemMenuMoveView;
import org.eclipse.ui.internal.presentations.SystemMenuRestore;
import org.eclipse.ui.internal.presentations.SystemMenuSize;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Manages a set of ViewPanes that are docked into the workbench window. The container for a PartTabFolder
 * is always a PartSashContainer (or null), and its children are always either PartPlaceholders or PartPanes.
 * 
 * TODO: write a common base class for EditorWorkbook and PartTabFolder.
 * TODO: remove the LayoutPart superclass. All code can either refer to PartPane, 
 * PartPlaceholder, PartSashContainer, or the common base class for EditorWorkbook and PartTabFolder.
 * The frequent downcasting is due to the fact that much of the code tries to use LayoutPart 
 * or ILayoutContainer references for many different purposes.
 */
public class PartTabFolder extends LayoutPart implements ILayoutContainer {

    private boolean active = false;

    private List children = new ArrayList(3);

    // inactiveCurrent is only used when restoring the persisted state of
    // perspective on startup.
    private LayoutPart current;

    private int flags;

    private LayoutPart inactiveCurrent;

    private WorkbenchPage page;

    private DefaultStackPresentationSite presentationSite = new DefaultStackPresentationSite() {

        public void close(IPresentablePart part) {
            PartTabFolder.this.close(part);
        }

        public void dragStart(IPresentablePart beingDragged,
                Point initialLocation, boolean keyboard) {
            LayoutPart pane = getPaneFor(beingDragged);

            if (pane != null) {
                DragUtil.performDrag(pane, Geometry.toDisplay(getParent(),
                        getPresentation().getControl().getBounds()),
                        initialLocation, !keyboard);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.internal.skins.IPresentationSite#dragStart(boolean)
         */
        public void dragStart(Point initialPosition, boolean keyboard) {
            DragUtil.performDrag(PartTabFolder.this, Geometry.toDisplay(
                    getParent(), getPresentation().getControl().getBounds()),
                    initialPosition, !keyboard);
        }

        public boolean isCloseable(IPresentablePart part) {
            Perspective perspective = page.getActivePerspective();

            if (perspective == null) {
            // Shouldn't happen -- can't have a PartTabFolder without a
            // perspective
            return false; }

            ViewPane pane = (ViewPane) getPaneFor(part);

            if (pane == null) {
            // Shouldn't happen -- this should only be called for ViewPanes
            // that are already in the tab folder
            return false; }

            return !perspective.isFixedView(pane.getViewReference());
        }

        public boolean isMoveable(IPresentablePart part) {
            return isCloseable(part);
        }

        public void selectPart(IPresentablePart toSelect) {
            presentationSelectionChanged(toSelect);
        }

        public void setPresentation(StackPresentation newPresentation) {
            super.setPresentation(newPresentation);

            updateSystemMenu();
        }

        public void setState(int newState) {
        	if (newState == STATE_MINIMIZED) {
        		if ((flags & SWT.MIN) == 0) {
        			return;
        		}
        	}
        	if (newState == STATE_MINIMIZED) {
        		if ((flags & SWT.MAX) == 0) {
        			return;
        		}
        	}
            PartTabFolder.this.setState(newState);
        }
    };

    private class SystemMenuContribution extends ContributionItem {
        
        private SystemMenuClose systemMenuClose;
        private SystemMenuFastView systemMenuFastView;
        private SystemMenuMaximize systemMenuMaximize;
        private SystemMenuMinimize systemMenuMinimize;
        private SystemMenuMoveView systemMenuMoveView;
        private SystemMenuRestore systemMenuRestore;
        private SystemMenuSize systemMenuSize;
        
        SystemMenuContribution(IStackPresentationSite stackPresentationSite, ViewPane viewPane) {
            systemMenuClose = new SystemMenuClose(viewPane.getPresentablePart(), stackPresentationSite);
            systemMenuFastView = new SystemMenuFastView(stackPresentationSite, viewPane);
            systemMenuMaximize = new SystemMenuMaximize(stackPresentationSite);
            systemMenuMinimize = new SystemMenuMinimize(stackPresentationSite);
            systemMenuMoveView = new SystemMenuMoveView(viewPane.getPresentablePart(), stackPresentationSite);
            systemMenuRestore = new SystemMenuRestore(stackPresentationSite);
            systemMenuSize = new SystemMenuSize(viewPane);            
        }
        
        public void fill(Menu menu, int index) {
            systemMenuFastView.fill(menu, index);
            systemMenuRestore.fill(menu, index);
            systemMenuMoveView.fill(menu, index);
            systemMenuSize.fill(menu, index);
            systemMenuMinimize.fill(menu, index);
            systemMenuMaximize.fill(menu, index);
            new MenuItem(menu, SWT.SEPARATOR);
            systemMenuClose.fill(menu, index);
        }
        
        public void dispose() {
            systemMenuClose.dispose();
            systemMenuFastView.dispose();
            systemMenuMaximize.dispose();
            systemMenuMinimize.dispose();
            systemMenuMoveView.dispose();
            systemMenuRestore.dispose();
            systemMenuSize.dispose();
        }
    }
    
    private IContributionItem systemMenuContribution;
    
    /**
     * PartTabFolder constructor comment.
     */
    public PartTabFolder(WorkbenchPage page) {
        this(page, SWT.MIN | SWT.MAX);
    }

    public PartTabFolder(WorkbenchPage page, int flags) {
        super("PartTabFolder"); //$NON-NLS-1$

        setID(this.toString());
        // Each folder has a unique ID so relative positioning is unambiguous.

        // save off a ref to the page
        //@issue is it okay to do this??
        //I think so since a PartTabFolder is
        //not used on more than one page.
        this.page = page;
        this.flags = flags;
    }

    /**
     * See IVisualContainer#add
     */
    public void add(LayoutPart child) {

        children.add(child);
        if (active) {
            showPart(child, null);
        }
    }

    /**
     * Add a part at a particular position
     */
    private void add(LayoutPart newChild, int idx) {
        IPresentablePart position = getPresentablePartAtIndex(idx);
        LayoutPart targetPart = getPaneFor(position);
        int childIdx = children.indexOf(targetPart);

        if (childIdx == -1) {
            children.add(newChild);
        } else {
            children.add(idx, newChild);
        }

        if (active) {
            showPart(newChild, position);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.ILayoutContainer#allowsAutoFocus()
     */
    public boolean allowsAutoFocus() {
        if (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) { return false; }

        ILayoutContainer parent = getContainer();

        if (parent != null && !parent.allowsAutoFocus()) { return false; }

        return true;
    }

    /**
     * See ILayoutContainer::allowBorder
     * 
     * There is already a border around the tab folder so no need for one from
     * the parts.
     */
    public boolean allowsBorder() {
        // @issue need to support old look even if a theme is set (i.e. show
        // border
        //   even when only one item) -- separate theme attribute, or derive this
        //   from existing attributes?
        // @issue this says to show the border only if there are no items, but
        //   in this case the folder should not be visible anyway
        //		if (tabThemeDescriptor != null)
        //			return (mapTabToPart.size() < 1);
        //		return mapTabToPart.size() <= 1;
        return false;
    }

    /**
     * @param part
     */
    protected void close(IPresentablePart part) {
        if (!presentationSite.isCloseable(part)) { return; }

        LayoutPart layoutPart = getPaneFor(part);

        if (layoutPart != null && layoutPart instanceof ViewPane) {
            ViewPane viewPane = (ViewPane) layoutPart;

            //getPresentation().removePart(part);
            viewPane.doHide();
        }
    }

    public void createControl(Composite parent) {

        if (presentationSite.getPresentation() != null) return;

        AbstractPresentationFactory factory = ((WorkbenchWindow) page
                .getWorkbenchWindow()).getWindowConfigurer()
                .getPresentationFactory();
        presentationSite.setPresentation(factory.createPresentation(parent,
                presentationSite, AbstractPresentationFactory.ROLE_DOCKED_VIEW,
                flags, page.getPerspective().getId(), getID()));

        active = true;

        // Add all visible children to the presentation
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart part = (LayoutPart) iter.next();

            showPart(part, null);
        }

        // Set current page.
        if (getItemCount() > 0) {
            int newPage = 0;
            if (current != null) newPage = indexOf(current);
            setSelection(newPage);
        }

        Control ctrl = getPresentation().getControl();

        // Add a drop target that lets us drag views directly to a particular
        // tab
        DragUtil.addDragTarget(ctrl, new IDragOverListener() {

            public IDropTarget drag(Control currentControl,
                    final Object draggedObject, Point position,
                    Rectangle dragRectangle) {

                if (!(draggedObject instanceof ViewPane)) { return null; }

                final ViewPane pane = (ViewPane) draggedObject;

                // Don't allow views to be dragged between windows
                if (pane.getWorkbenchWindow() != getWorkbenchWindow()) { return null; }

                final StackDropResult dropResult = getPresentation().dragOver(
                        currentControl, position);

                if (dropResult == null) { return null; }

                IPresentablePart draggedControl = getPresentablePartAtIndex(dropResult
                        .getDropIndex());

                // If we're dragging a pane over itself do nothing
                if (draggedControl == pane.getPresentablePart()) { return null; }

                return new IDropTarget() {

                    public void drop() {

                        // Don't worry about reparenting the view if we're
                        // simply
                        // rearranging tabs within this folder
                        if (pane.getContainer() != PartTabFolder.this) {
                            page.getActivePerspective().getPresentation()
                                    .derefPart(pane);
                            pane.reparent(getParent());
                        } else {
                            remove(pane);
                        }

                        add(pane, dropResult.getDropIndex());
                        setSelection(pane);
                        pane.setFocus();
                    }

                    public Cursor getCursor() {
                        return DragCursors.getCursor(DragCursors.CENTER);
                    }

                    public Rectangle getSnapRectangle() {
                        return dropResult.getSnapRectangle();
                    }
                };
            }

        });

        ctrl.setData(this);
    }

    /**
     * See LayoutPart#dispose
     */
    public void dispose() {

        if (!active) return;

        StackPresentation presentation = presentationSite.getPresentation();

        presentationSite.dispose();

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            next.setContainer(null);
        }

        active = false;

        updateSystemMenu();
    }

    public void findSashes(LayoutPart part, ViewPane.Sashes sashes) {
        ILayoutContainer container = getContainer();

        if (container != null) {
            container.findSashes(this, sashes);
        }
    }

    /**
     * Forces the layout to be recomputed for all parts
     */
    private void forceLayout() {
        PartSashContainer cont = (PartSashContainer) getContainer();
        if (cont != null) {
            LayoutTree tree = cont.getLayoutTree();
            tree.setBounds(getParent().getClientArea());
        }
    }

    /**
     * Gets the presentation bounds.
     */
    public Rectangle getBounds() {
        if (getPresentation() == null) { return new Rectangle(0, 0, 0, 0); }

        return getPresentation().getControl().getBounds();
    }

    /**
     * See IVisualContainer#getChildren
     */
    public LayoutPart[] getChildren() {
        return (LayoutPart[]) children.toArray(new LayoutPart[children.size()]);
    }

    public Control getControl() {
        StackPresentation presentation = getPresentation();

        if (presentation == null) { return null; }

        return presentation.getControl();
    }

    /**
     * Answer the number of children.
     */
    public int getItemCount() {
        if (active) { return getPresentableParts().size(); }

        return children.size();
    }

    /**
     * Returns the layout part for the given presentable part
     * 
     * @param toFind
     * @return
     */
    private LayoutPart getLayoutPart(IPresentablePart toFind) {
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            if (next.getPresentablePart() == toFind) { return next; }
        }

        return null;
    }

    // getMinimumHeight() added by cagatayk@acm.org
    /**
     * @see LayoutPart#getMinimumHeight()
     */
    public int getMinimumHeight() {
        if (getPresentation() == null) { return 0; }

        return getPresentation().computeMinimumSize().y;
    }

    private LayoutPart getPaneFor(IPresentablePart part) {
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            if (next.getPresentablePart() == part) { return next; }
        }

        return null;
    }

    /**
     * Get the parent control.
     */
    public Composite getParent() {
        return getControl().getParent();
    }

    private IPresentablePart getPresentablePartAtIndex(int idx) {
        List presentableParts = getPresentableParts();

        if (idx >= 0 && idx < presentableParts.size()) { return (IPresentablePart) presentableParts
                .get(idx); }

        return null;
    }

    /**
     * Returns a list of IPresentablePart
     * 
     * @return
     */
    private List getPresentableParts() {
        List result = new ArrayList(children.size());

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart part = (LayoutPart) iter.next();

            IPresentablePart presentablePart = part.getPresentablePart();

            if (presentablePart != null) {
                result.add(presentablePart);
            }
        }

        return result;
    }

    private StackPresentation getPresentation() {
        return presentationSite.getPresentation();
    }

    public int getSelection() {
        if (!active) return 0;

        return indexOf(current);
    }

    /**
     * Returns the visible child.
     */
    public LayoutPart getVisiblePart() {
        if (current == null) return inactiveCurrent;
        return current;
    }

    private void hidePart(LayoutPart part) {
        IPresentablePart presentablePart = part.getPresentablePart();

        if (presentablePart == null) { return; }

        getPresentation().removePart(presentablePart);
        if (active) {
            part.setContainer(null);
        }
    }

    private int indexOf(IPresentablePart part) {
        int result = getPresentableParts().indexOf(part);

        if (result < 0) {
            result = 0;
        }

        return result;
    }

    public int indexOf(LayoutPart item) {
        return indexOf(item.getPresentablePart());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.IWorkbenchDragSource#isDragAllowed(org.eclipse.swt.graphics.Point)
     */
    public boolean isDragAllowed(Point point) {
        return true;
    }

    /**
     * Open the tracker to allow the user to move the specified part using
     * keyboard.
     */
    public void openTracker(LayoutPart part) {
        DragUtil
                .performDrag(part, DragUtil.getDisplayBounds(part.getControl()));
    }

    private void presentationSelectionChanged(IPresentablePart newSelection) {
        setSelection(getLayoutPart(newSelection));

        // set the title of the detached window to reflect the active tab
        Window window = getWindow();
        if (window instanceof DetachedWindow) {
            window.getShell().setText(newSelection.getTitle());
        }
    }

    /**
     * See IVisualContainer#remove
     */
    public void remove(LayoutPart child) {
        IPresentablePart presentablePart = child.getPresentablePart();

        if (presentablePart != null) {
            presentationSite.getPresentation().removePart(presentablePart);
        }

        children.remove(child);

        if (active) {
            child.setContainer(null);
        }

        updateContainerVisibleTab();
    }

    /**
     * Reparent a part. Also reparent visible children...
     */
    public void reparent(Composite newParent) {
        if (!newParent.isReparentable()) return;

        Control control = getControl();
        if ((control == null) || (control.getParent() == newParent)) return;

        super.reparent(newParent);

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();
            next.reparent(newParent);
        }
    }

    /**
     * See IVisualContainer#replace
     */
    public void replace(LayoutPart oldChild, LayoutPart newChild) {
        IPresentablePart oldPart = oldChild.getPresentablePart();
        IPresentablePart newPart = newChild.getPresentablePart();

        int idx = children.indexOf(oldChild);
        children.add(idx, newChild);

        if (active) {
            showPart(newChild, oldPart);
        }

        if (oldChild == inactiveCurrent) {
            setSelection(newChild);
            inactiveCurrent = null;
        }

        remove(oldChild);

    }

    public boolean resizesVertically() {
        return presentationSite.getState() != IStackPresentationSite.STATE_MINIMIZED;
    }

    /**
     * @see IPersistable
     */
    public IStatus restoreState(IMemento memento) {
        // Read the active tab.
        String activeTabID = memento
                .getString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID);

        // Read the page elements.
        IMemento[] children = memento.getChildren(IWorkbenchConstants.TAG_PAGE);
        if (children != null) {
            // Loop through the page elements.
            for (int i = 0; i < children.length; i++) {
                // Get the info details.
                IMemento childMem = children[i];
                String partID = childMem
                        .getString(IWorkbenchConstants.TAG_CONTENT);

                // Create the part.
                LayoutPart part = new PartPlaceholder(partID);
				part.setContainer(this);            
                add(part);
                //1FUN70C: ITPUI:WIN - Shouldn't set Container when not active
                //part.setContainer(this);
                if (partID.equals(activeTabID)) {
                    // Mark this as the active part.
                    inactiveCurrent = part;
                }
            }
        }

        Integer expanded = memento.getInteger(IWorkbenchConstants.TAG_EXPANDED);
        setState((expanded == null || expanded.intValue() != IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_RESTORED
                : IStackPresentationSite.STATE_MINIMIZED);

        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    /**
     * @see IPersistable
     */
    public IStatus saveState(IMemento memento) {

        // Save the active tab.
        if (current != null)
                memento.putString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID,
                        current.getID());

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            IMemento childMem = memento
                    .createChild(IWorkbenchConstants.TAG_PAGE);

            IPresentablePart part = next.getPresentablePart();
            String tabText = "LabelNotFound"; //$NON-NLS-1$ 
            if (part != null) {
                tabText = part.getName();
            }
            childMem.putString(IWorkbenchConstants.TAG_LABEL, tabText);
            childMem.putString(IWorkbenchConstants.TAG_CONTENT, next.getID());
        }

        memento
                .putInteger(
                        IWorkbenchConstants.TAG_EXPANDED,
                        (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_MINIMIZED
                                : IStackPresentationSite.STATE_RESTORED);

        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    /**
     * Set the active appearence on the tab folder.
     * 
     * @param active
     */
    public void setActive(boolean activeState) {
        if (activeState) {
            if (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) {
                setState(IStackPresentationSite.STATE_RESTORED);
            }
            if (page.isZoomed()) {
                presentationSite
                        .setPresentationState(IStackPresentationSite.STATE_MAXIMIZED);
            }
        }

        getPresentation().setActive(activeState);
    }

    /**
     * Sets the presentation bounds.
     */
    public void setBounds(Rectangle r) {
        if (getPresentation() != null) {
            getPresentation().setBounds(r);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.LayoutPart#setContainer(org.eclipse.ui.internal.ILayoutContainer)
     */
    public void setContainer(ILayoutContainer container) {

        super.setContainer(container);

        if (presentationSite.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
            if (!page.isZoomed()) {
                setState(IStackPresentationSite.STATE_RESTORED);
            }
        }
    }

    public void setSelection(int index) {
        if (!active) return;

        setSelection(getLayoutPart((IPresentablePart) getPresentableParts()
                .get(index)));
    }

    public void setSelection(LayoutPart part) {
        // TODO stefan: ok that i comment this out?
        //		if (current == part) {
        //			return;
        //		}

        current = part;

        updateSystemMenu();

        if (part != null) {
            IPresentablePart presentablePart = part.getPresentablePart();
            StackPresentation presentation = getPresentation();

            if (presentablePart != null && presentation != null) {
                presentation.selectPart(presentablePart);
            }
        }
    }

    private void setState(int newState) {
        if (newState == presentationSite.getState()) { return; }

        int oldState = presentationSite.getState();

        if (current != null) {
            if (newState == IStackPresentationSite.STATE_MAXIMIZED) {
                ((PartPane) current).doZoom();
            } else {
                presentationSite.setPresentationState(newState);

                WorkbenchPage page = ((PartPane) current).getPage();
                if (page.isZoomed()) {
                    page.zoomOut();
                }

                updateControlBounds();

                if (oldState == IStackPresentationSite.STATE_MINIMIZED) {
                    forceLayout();
                }
            }
        }

        if (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) {
            page.refreshActiveView();
        }
    }

    public void setZoomed(boolean isZoomed) {
    	super.setZoomed(isZoomed);
    	
        if (isZoomed) {
            presentationSite
                    .setPresentationState(IStackPresentationSite.STATE_MAXIMIZED);
        } else if (presentationSite.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
        	presentationSite.setPresentationState(IStackPresentationSite.STATE_RESTORED);
        }
    }

    //	/**
    //	 * Sets the theme id.
    //	 *
    //	 * @param theme the theme id to set.
    //	 */
    //	public void setTheme(String theme) {
    //		if ((theme != null) && (theme.length() > 0)) {
    //			this.themeid = theme;
    //			tabThemeDescriptor =
    // WorkbenchThemeManager.getInstance().getTabThemeDescriptor(theme);
    //		}
    //	}

    /**
     * Makes the given part visible in the presentation
     * 
     * @param presentablePart
     */
    private void showPart(LayoutPart part, IPresentablePart position) {

        part.setContainer(this);
        
        IPresentablePart presentablePart = part.getPresentablePart();

        if (presentablePart == null) { return; }

        part.createControl(getParent());
        if (part.getControl().getParent() != getControl().getParent()) {
        	part.reparent(getControl().getParent());
        }
        part.moveAbove(getPresentation().getControl());

        presentationSite.getPresentation().addPart(presentablePart, position);

        if (current == null) {
            setSelection(part);
        }
    }

    /**
     * Update the container to show the correct visible tab based on the
     * activation list.
     * 
     * @param org.eclipse.ui.internal.ILayoutContainer
     */
    private void updateContainerVisibleTab() {
        LayoutPart[] parts = getChildren();
        
        if (parts.length < 1) {
            setSelection(null);
            return;
        }
        
        PartPane selPart = null;
        int topIndex = 0;
        IWorkbenchPartReference sortedPartsArray[] = page.getSortedParts();
        List sortedParts = Arrays.asList(sortedPartsArray);
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] instanceof PartPane) {
                IWorkbenchPartReference part = ((PartPane) parts[i])
                        .getPartReference();
                int index = sortedParts.indexOf(part);
                if (index >= topIndex) {
                    topIndex = index;
                    selPart = (PartPane) parts[i];
                }
            }
        }

        setSelection(selPart);
    }

    private void updateControlBounds() {
        Rectangle bounds = getPresentation().getControl().getBounds();
        int minimumHeight = getMinimumHeight();

        if (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED
                && bounds.height != minimumHeight) {
            bounds.width = getMinimumWidth();
            bounds.height = minimumHeight;
            getPresentation().setBounds(bounds);

            forceLayout();
        }
    }

    private void updateSystemMenu() {
        StackPresentation presentation = getPresentation();

        if (presentation == null) {
            if (systemMenuContribution != null) {
                // TODO spec says not to call this directly
                systemMenuContribution.dispose();
                systemMenuContribution = null;
            }
        } else {	
	        IMenuManager systemMenuManager = presentation.getSystemMenuManager();
	                
	        if (systemMenuContribution != null) {
	            systemMenuManager.remove(systemMenuContribution);
                // TODO spec says not to call this directly
	            systemMenuContribution.dispose();
	            systemMenuContribution = null;
	        }
	
	        if (current != null && current instanceof ViewPane) {
	            systemMenuContribution = new SystemMenuContribution(
	                    presentationSite, (ViewPane) current);
	            systemMenuManager.add(systemMenuContribution);
	        }
        }
    }

	/**
	 * 
	 */
	public void showSystemMenu() {
		getPresentation().showSystemMenu();
	}
	
	public void showPaneMenu() {
		getPresentation().showPaneMenu();
	}
}