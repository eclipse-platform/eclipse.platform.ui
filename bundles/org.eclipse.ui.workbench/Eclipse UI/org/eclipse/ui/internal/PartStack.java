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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.presentations.PresentationSerializer;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Implements the common behavior for stacks of Panes (ie: EditorStack and ViewStack)
 * This layout container has PartPanes as children and belongs to a PartSashContainer.
 * 
 * @since 3.0
 */
public abstract class PartStack extends LayoutPart implements ILayoutContainer {

    private List children = new ArrayList(3);

    protected int appearance = PresentationFactoryUtil.ROLE_VIEW;

    // inactiveCurrent is only used when restoring the persisted state of
    // perspective on startup.
    private LayoutPart current;

    private boolean ignoreSelectionChanges = false;

    protected IMemento savedPresentationState = null;

    private DefaultStackPresentationSite presentationSite = new DefaultStackPresentationSite() {

        public void close(IPresentablePart part) {
            PartStack.this.close(part);
        }

        public void close(IPresentablePart[] parts) {
            PartStack.this.close(parts);
        }

        public void dragStart(IPresentablePart beingDragged,
                Point initialLocation, boolean keyboard) {
            PartStack.this.dragStart(beingDragged, initialLocation, keyboard);
        }

        public void dragStart(Point initialLocation, boolean keyboard) {
            PartStack.this.dragStart(null, initialLocation, keyboard);
        }

        public boolean isCloseable(IPresentablePart part) {
            return PartStack.this.isCloseable(part);
        }

        public boolean isPartMoveable(IPresentablePart part) {
            return PartStack.this.isMoveable(part);
        }

        public void selectPart(IPresentablePart toSelect) {
            PartStack.this.presentationSelectionChanged(toSelect);
        }

        public boolean supportsState(int state) {
            return PartStack.this.supportsState(state);
        }

        public void setState(int newState) {
            PartStack.this.setState(newState);
        }

        public IPresentablePart getSelectedPart() {
            return PartStack.this.getSelectedPart();
        }

        public void addSystemActions(IMenuManager menuManager) {
            PartStack.this.addSystemActions(menuManager);
        }

        public boolean isStackMoveable() {
            return canMoveFolder();
        }
        
        public void flushLayout() {
        	PartStack.this.flushLayout();
        }
    };

    protected abstract boolean isMoveable(IPresentablePart part);

    protected abstract boolean isCloseable(IPresentablePart part);

    protected abstract void addSystemActions(IMenuManager menuManager);

    protected abstract boolean supportsState(int newState);

    protected abstract boolean canMoveFolder();

    protected abstract void derefPart(LayoutPart toDeref);

    protected abstract boolean allowsDrop(PartPane part);

    protected static void appendToGroupIfPossible(IMenuManager m,
            String groupId, ContributionItem item) {
        try {
            m.appendToGroup(groupId, item);
        } catch (IllegalArgumentException e) {
            m.add(item);
        }
    }

    /**
     * Creates a new PartStack, given a constant determining which presentation to use
     * 
     * @param appearance one of the PresentationFactoryUtil.ROLE_* constants
     */
    public PartStack(int appearance) {
        super("PartStack"); //$NON-NLS-1$

        this.appearance = appearance;
    }

    /**
     * Returns the currently selected IPresentablePart, or null if none
     * 
     * @return
     */
    protected IPresentablePart getSelectedPart() {
        if (current == null) {
            return null;
        }

        return current.getPresentablePart();
    }

    protected IStackPresentationSite getPresentationSite() {
        return presentationSite;
    }

    /**
     * Tests the integrity of this object. Throws an exception if the object's state
     * is invalid. For use in test suites.
     */
    public void testInvariants() {
        Control focusControl = Display.getCurrent().getFocusControl();

        boolean currentFound = false;

        LayoutPart[] children = getChildren();

        for (int idx = 0; idx < children.length; idx++) {
            LayoutPart child = children[idx];

            // No null children allowed
            Assert.isNotNull(child,
                    "null children are not allowed in PartStack"); //$NON-NLS-1$

            // This object can only contain placeholders or PartPanes
            Assert.isTrue(child instanceof PartPlaceholder
                    || child instanceof PartPane,
                    "PartStack can only contain PartPlaceholders or PartPanes"); //$NON-NLS-1$

            // Ensure that all the PartPanes have an associated presentable part 
            IPresentablePart part = child.getPresentablePart();
            if (child instanceof PartPane) {
                Assert.isNotNull(part,
                        "All PartPanes must have a non-null IPresentablePart"); //$NON-NLS-1$
            }

            // Ensure that the child's backpointer points to this stack
            ILayoutContainer childContainer = child.getContainer();

            // Disable tests for placeholders -- PartPlaceholder backpointers don't
            // obey the usual rules -- they sometimes point to a container placeholder
            // for this stack instead of the real stack.
            if (!(child instanceof PartPlaceholder)) {

                if (isDisposed()) {

                    // Currently, we allow null backpointers if the widgetry is disposed.
                    // However, it is never valid for the child to have a parent other than
                    // this object
                    if (childContainer != null) {
                        Assert
                                .isTrue(childContainer == this,
                                        "PartStack has a child that thinks it has a different parent"); //$NON-NLS-1$
                    }
                } else {
                    // If the widgetry exists, the child's backpointer must point to us
                    Assert
                            .isTrue(childContainer == this,
                                    "PartStack has a child that thinks it has a different parent"); //$NON-NLS-1$

                    // If this child has focus, then ensure that it is selected and that we have
                    // the active appearance.

                    if (SwtUtil.isChild(child.getControl(), focusControl)) {
                        Assert.isTrue(child == current,
                                "The part with focus is not the selected part"); //$NON-NLS-1$
                        //  focus check commented out since it fails when focus workaround in LayoutPart.setVisible is not present       			
                        //        			Assert.isTrue(getActive() == StackPresentation.AS_ACTIVE_FOCUS);
                    }
                }
            }

            // Ensure that "current" points to a valid child
            if (child == current) {
                currentFound = true;
            }

            // Test the child's internal state
            child.testInvariants();
        }

        // If we have at least one child, ensure that the "current" pointer points to one of them
        if (!isDisposed() && getPresentableParts().size() > 0) {
            Assert.isTrue(currentFound);

            if (!isDisposed()) {
                StackPresentation presentation = getPresentation();

                // If the presentation controls have focus, ensure that we have the active appearance
                if (SwtUtil.isChild(presentation.getControl(), focusControl)) {
                    Assert
                            .isTrue(
                                    getActive() == StackPresentation.AS_ACTIVE_FOCUS,
                                    "The presentation has focus but does not have the active appearance"); //$NON-NLS-1$
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#describeLayout(java.lang.StringBuffer)
     */
    public void describeLayout(StringBuffer buf) {
        int activeState = getActive();
        if (activeState == StackPresentation.AS_ACTIVE_FOCUS) {
            buf.append("active "); //$NON-NLS-1$
        } else if (activeState == StackPresentation.AS_ACTIVE_NOFOCUS) {
            buf.append("active_nofocus "); //$NON-NLS-1$
        }

        buf.append("("); //$NON-NLS-1$

        LayoutPart[] children = ((ILayoutContainer) this).getChildren();

        int visibleChildren = 0;

        for (int idx = 0; idx < children.length; idx++) {

            LayoutPart next = children[idx];
            if (!(next instanceof PartPlaceholder)) {
                if (idx > 0) {
                    buf.append(", "); //$NON-NLS-1$
                }

                if (next == current) {
                    buf.append("*"); //$NON-NLS-1$
                }

                next.describeLayout(buf);

                visibleChildren++;
            }
        }

        buf.append(")"); //$NON-NLS-1$
    }

    /**
     * See IVisualContainer#add
     */
    public void add(LayoutPart child) {
        children.add(child);
        showPart(child, null);
    }

    /**
     * Add a part at a particular position
     */
    protected void add(LayoutPart newChild, Object cookie) {
        children.add(newChild);

        showPart(newChild, cookie);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.ILayoutContainer#allowsAutoFocus()
     */
    public boolean allowsAutoFocus() {
        if (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) {
            return false;
        }

        ILayoutContainer parent = getContainer();

        if (parent != null && !parent.allowsAutoFocus()) {
            return false;
        }

        return true;
    }

    /**
     * @param parts
     */
    protected void close(IPresentablePart[] parts) {
        for (int idx = 0; idx < parts.length; idx++) {
            IPresentablePart part = parts[idx];

            close(part);
        }
    }

    /**
     * @param part
     */
    protected void close(IPresentablePart part) {
        if (!presentationSite.isCloseable(part)) {
            return;
        }

        LayoutPart layoutPart = getPaneFor(part);

        if (layoutPart != null && layoutPart instanceof PartPane) {
            PartPane viewPane = (PartPane) layoutPart;

            viewPane.doHide();
        }
    }

    public boolean isDisposed() {
        return getPresentation() == null;
    }

    protected AbstractPresentationFactory getFactory() {
        AbstractPresentationFactory factory = ((WorkbenchWindow) getPage()
                .getWorkbenchWindow()).getWindowConfigurer()
                .getPresentationFactory();

        return factory;
    }

    public void createControl(Composite parent) {
        if (!isDisposed()) {
            return;
        }

        AbstractPresentationFactory factory = getFactory();

        PresentationSerializer serializer = new PresentationSerializer(
                getPresentableParts());

        StackPresentation presentation = PresentationFactoryUtil
                .createPresentation(factory, appearance, parent,
                        presentationSite, serializer, savedPresentationState);

        createControl(parent, presentation);
    }

    public void createControl(Composite parent, StackPresentation presentation) {

        Assert.isTrue(isDisposed());

        if (presentationSite.getPresentation() != null)
            return;

        presentationSite.setPresentation(presentation);

        // Add all visible children to the presentation
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart part = (LayoutPart) iter.next();

            showPart(part, null);
        }

        Control ctrl = getPresentation().getControl();

        // Add a drop target that lets us drag views directly to a particular
        // tab
        DragUtil.addDragTarget(ctrl, new IDragOverListener() {

            public IDropTarget drag(Control currentControl,
                    final Object draggedObject, Point position,
                    Rectangle dragRectangle) {

                if (!(draggedObject instanceof PartPane)) {
                    return null;
                }

                final PartPane pane = (PartPane) draggedObject;
                if (!allowsDrop(pane)) {
                    return null;
                }

                // Don't allow views to be dragged between windows
                if (pane.getWorkbenchWindow() != getWorkbenchWindow()) {
                    return null;
                }

                // Regardless of the wishes of the presentation, ignore 4 pixels around the edge of the control.
                // This ensures that it will always be possible to dock around the edge of the control.
                {
                    Point controlCoordinates = currentControl.getParent()
                            .toControl(position);
                    Rectangle bounds = currentControl.getBounds();
                    int closestSide = Geometry.getClosestSide(bounds,
                            controlCoordinates);

                    if (Geometry.getDistanceFromEdge(bounds,
                            controlCoordinates, closestSide) < 5) {
                        return null;
                    }
                }
                // End of check for stacking on edge

                final StackDropResult dropResult = getPresentation().dragOver(
                        currentControl, position);

                if (dropResult == null) {
                    return null;
                }

                return new IDropTarget() {

                    public void drop() {

                        // If we're dragging a pane over itself do nothing
                        //if (dropResult.getInsertionPoint() == pane.getPresentablePart()) { return; };

                        // Don't worry about reparenting the view if we're
                        // simply rearranging tabs within this folder
                        if (pane.getContainer() != PartStack.this) {
                            derefPart(pane);
                            pane.reparent(getParent());
                        } else {
                            remove(pane);
                        }

                        add(pane, dropResult.getCookie());
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

        updateActions();

        // We should not have a placeholder selected once we've created the widgetry
        if (current instanceof PartPlaceholder) {
            current = null;
            updateContainerVisibleTab();
        }

        refreshPresentationSelection();
    }

    /**
     * Saves the current state of the presentation to savedPresentationState, if the
     * presentation exists.
     */
    protected void savePresentationState() {
        if (isDisposed()) {
            return;
        }

        {// Save the presentation's state before disposing it
            XMLMemento memento = XMLMemento
                    .createWriteRoot(IWorkbenchConstants.TAG_PRESENTATION);
            memento.putString(IWorkbenchConstants.TAG_ID, getFactory().getId());

            PresentationSerializer serializer = new PresentationSerializer(
                    getPresentableParts());

            getPresentation().saveState(serializer, memento);

            // Store the memento in savedPresentationState
            savedPresentationState = memento;
        }
    }

    /**
     * See LayoutPart#dispose
     */
    public void dispose() {

        if (isDisposed())
            return;

        savePresentationState();

        presentationSite.dispose();

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            next.setContainer(null);
        }
    }

    public void findSashes(LayoutPart part, PartPane.Sashes sashes) {
        ILayoutContainer container = getContainer();

        if (container != null) {
            container.findSashes(this, sashes);
        }
    }

    /**
     * Gets the presentation bounds.
     */
    public Rectangle getBounds() {
        if (getPresentation() == null) {
            return new Rectangle(0, 0, 0, 0);
        }

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

        if (presentation == null) {
            return null;
        }

        return presentation.getControl();
    }

    /**
     * Answer the number of children.
     */
    public int getItemCount() {
        if (isDisposed()) {
            return children.size();
        }
        return getPresentableParts().size();
    }
    
    /**
     * Returns the LayoutPart for the given IPresentablePart, or null if the given
     * IPresentablePart is not in this stack. Returns null if given a null argument.
     * 
     * @param part to locate or null
     * @return
     */
    protected LayoutPart getPaneFor(IPresentablePart part) {
        if (part == null) {
            return null;
        }

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            if (next.getPresentablePart() == part) {
                return next;
            }
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

        if (idx >= 0 && idx < presentableParts.size()) {
            return (IPresentablePart) presentableParts.get(idx);
        }

        return null;
    }

    /**
     * Returns a list of IPresentablePart
     * 
     * @return
     */
    public List getPresentableParts() {
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

    protected StackPresentation getPresentation() {
        return presentationSite.getPresentation();
    }

    /**
     * Returns the visible child.
     */
    public PartPane getVisiblePart() {
        if (current instanceof PartPane) {
            return (PartPane) current;
        }
        return null;
    }

    private void presentationSelectionChanged(IPresentablePart newSelection) {
        // Ignore selection changes that occur as a result of removing a part
        if (ignoreSelectionChanges) {
            return;
        }
        LayoutPart newPart = getPaneFor(newSelection);

        // This method should only be called on objects that are already in the layout
        Assert.isNotNull(newPart);

        if (newPart == current) {
            return;
        }

        setSelection(newPart);

        if (newPart != null) {
            newPart.setFocus();
        }

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

        // Need to remove it from the list of children before notifying the presentation
        // since it may setVisible(false) on the part, leading to a partHidden notification,
        // during which findView must not find the view being removed.  See bug 60039. 
        children.remove(child);

        StackPresentation presentation = getPresentation();

        if (presentablePart != null && presentation != null) {
            ignoreSelectionChanges = true;
            presentation.removePart(presentablePart);
            ignoreSelectionChanges = false;
        }

        if (!isDisposed()) {
            child.setContainer(null);
        }

        if (child == current) {
            updateContainerVisibleTab();
        }
    }

    /**
     * Reparent a part. Also reparent visible children...
     */
    public void reparent(Composite newParent) {
        if (!newParent.isReparentable())
            return;

        Control control = getControl();
        if ((control == null) || (control.getParent() == newParent))
            return;

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
        // commented out but left for future reference - we're using this
        // as the cookie for the part presentation but this will
        // almost always be null (oldChilds being PartPlaceholders)
        // even if they aren't null, we don't handle 
        // IPresentableParts as cookies
        //
        // IPresentablePart oldPart = oldChild.getPresentablePart();
        IPresentablePart newPart = newChild.getPresentablePart();

        int idx = children.indexOf(oldChild);
        int numPlaceholders = 0;
        //subtract the number of placeholders still existing in the list 
        //before this one - they wont have parts.
        for (int i = 0; i < idx; i++) {
            if (children.get(i) instanceof PartPlaceholder)
                numPlaceholders++;
        }
        Integer cookie = new Integer(idx - numPlaceholders);
        children.add(idx, newChild);

        showPart(newChild, cookie);

        if (oldChild == current && !(newChild instanceof PartPlaceholder)) {
            setSelection(newChild);
        }

        remove(oldChild);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#computePreferredSize(boolean, int, int, int)
	 */
	public int computePreferredSize(boolean width, int availableParallel,
			int availablePerpendicular, int preferredParallel) {
		
		return getPresentation().computePreferredSize(width, availableParallel, 
				availablePerpendicular, preferredParallel);
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#getSizeFlags(boolean)
     */
    public int getSizeFlags(boolean horizontal) {
        return getPresentation().getSizeFlags(horizontal);
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
                    current = part;
                }
            }
        }

        Integer expanded = memento.getInteger(IWorkbenchConstants.TAG_EXPANDED);
        setState((expanded == null || expanded.intValue() != IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_RESTORED
                : IStackPresentationSite.STATE_MINIMIZED);

        Integer appearance = memento
                .getInteger(IWorkbenchConstants.TAG_APPEARANCE);
        if (appearance != null) {
            this.appearance = appearance.intValue();
        }

        // Determine if the presentation has saved any info here
        savedPresentationState = null;
        IMemento[] presentationMementos = memento
                .getChildren(IWorkbenchConstants.TAG_PRESENTATION);

        for (int idx = 0; idx < presentationMementos.length; idx++) {
            IMemento child = presentationMementos[idx];

            String id = child.getString(IWorkbenchConstants.TAG_ID);

            if (Util.equals(id, getFactory().getId())) {
                savedPresentationState = child;
                break;
            }
        }

        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    /**
     * @see IPersistable
     */
    public IStatus saveState(IMemento memento) {

        // Save the active tab.
        if (current != null)
            memento.putString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID, current
                    .getCompoundId());

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
            childMem.putString(IWorkbenchConstants.TAG_CONTENT, next
                    .getCompoundId());
        }

        memento
                .putInteger(
                        IWorkbenchConstants.TAG_EXPANDED,
                        (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_MINIMIZED
                                : IStackPresentationSite.STATE_RESTORED);

        memento.putInteger(IWorkbenchConstants.TAG_APPEARANCE, appearance);

        savePresentationState();

        if (savedPresentationState != null) {
            IMemento presentationState = memento
                    .createChild(IWorkbenchConstants.TAG_PRESENTATION);
            presentationState.putMemento(savedPresentationState);
        }

        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    protected WorkbenchPage getPage() {
        WorkbenchWindow window = (WorkbenchWindow) getWorkbenchWindow();

        if (window == null) {
            return null;
        }

        return (WorkbenchPage) window.getActivePage();
    }

    /**
     * Set the active appearence on the tab folder.
     * 
     * @param active
     */
    public void setActive(int activeState) {

        if (activeState == StackPresentation.AS_ACTIVE_FOCUS) {
            if (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) {
                setState(IStackPresentationSite.STATE_RESTORED);
            }
        }

        presentationSite.setActive(activeState);
    }

    public int getActive() {
        return presentationSite.getActive();
    }

    /**
     * Sets the presentation bounds.
     */
    public void setBounds(Rectangle r) {
    	
        if (getPresentation() != null) {
            getPresentation().setBounds(r);
        }
    }

    public void setSelection(LayoutPart part) {
        if (current == part) {
            return;
        }

        current = part;

        if (!isDisposed()) {
            updateActions();
        }
        refreshPresentationSelection();
    }

    /**
     * Subclasses should override this method to update the enablement state of their
     * actions
     */
    protected void updateActions() {

    }

    private void refreshPresentationSelection() {
        if (current != null) {
            IPresentablePart presentablePart = current.getPresentablePart();
            StackPresentation presentation = getPresentation();

            if (presentablePart != null && presentation != null) {

                current.createControl(getParent());
                if (current.getControl().getParent() != getControl()
                        .getParent()) {
                    current.reparent(getControl().getParent());
                }

                current.moveAbove(getPresentation().getControl());

                presentation.selectPart(presentablePart);
            }
        }
    }

    public int getState() {
    	return presentationSite.getState();
    }
    
    protected void setState(int newState) {
        if (!supportsState(newState) || newState == presentationSite.getState()) {
            return;
        }

        Rectangle initialBounds = Geometry.toDisplay(getControl().getParent(), getBounds());
        
        int oldState = presentationSite.getState();

        if (current != null) {
            if (newState == IStackPresentationSite.STATE_MAXIMIZED) {
                PartPane pane = getVisiblePart();
                if (pane != null) {
                    pane.doZoom();
                }
            } else {
                presentationSite.setPresentationState(newState);

                WorkbenchPage page = getPage();
                if (page != null) {
                    if (page.isZoomed()) {
                        page.zoomOut();
                    }

                    flushLayout();
                }
            }
        }

        if (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) {
            WorkbenchPage page = getPage();

            if (page != null) {
                page.refreshActiveView();
            }
        }
        
        Rectangle finalBounds = Geometry.toDisplay(getControl().getParent(), getBounds());
        RectangleAnimation animation = new RectangleAnimation(getWorkbenchWindow().getShell(), initialBounds, finalBounds);
        animation.schedule(1);
    }

    public void setZoomed(boolean isZoomed) {
        super.setZoomed(isZoomed);

        if (isZoomed) {
            presentationSite
                    .setPresentationState(IStackPresentationSite.STATE_MAXIMIZED);
        } else if (presentationSite.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
            presentationSite
                    .setPresentationState(IStackPresentationSite.STATE_RESTORED);
        }
    }

    /**
     * Makes the given part visible in the presentation
     * 
     * @param presentablePart
     */
    private void showPart(LayoutPart part, Object cookie) {

        if (isDisposed()) {
            return;
        }

        part.setContainer(this);

        IPresentablePart presentablePart = part.getPresentablePart();

        if (presentablePart == null) {
            return;
        }

        presentationSite.getPresentation().addPart(presentablePart, cookie);

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
        WorkbenchPage page = getPage();

        if (page != null) {
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

        }

        if (selPart == null) {
            List presentableParts = getPresentableParts();
            if (presentableParts.size() != 0) {
                IPresentablePart part = (IPresentablePart) getPresentableParts()
                        .get(0);

                selPart = (PartPane) getPaneFor(part);
            }
        }

        setSelection(selPart);
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

    public void showPartList() {
        getPresentation().showPartList();
    }

    /**
     * @param pane
     * @return
     */
    public Control[] getTabList(LayoutPart part) {
        if (part != null) {
            IPresentablePart presentablePart = part.getPresentablePart();
            StackPresentation presentation = getPresentation();

            if (presentablePart != null && presentation != null) {
                return presentation.getTabList(presentablePart);
            }
        }

        return new Control[0];
    }

    /**
     * 
     * @param beingDragged
     * @param initialLocation
     * @param keyboard
     */
    public void dragStart(IPresentablePart beingDragged, Point initialLocation,
            boolean keyboard) {
        if (beingDragged == null) {
            if (canMoveFolder()) {
                if (presentationSite.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
                    setState(IStackPresentationSite.STATE_RESTORED);
                }

                DragUtil.performDrag(PartStack.this, Geometry
                        .toDisplay(getParent(), getPresentation().getControl()
                                .getBounds()), initialLocation, !keyboard);
            }
        } else {
            if (presentationSite.isPartMoveable(beingDragged)) {
                LayoutPart pane = getPaneFor(beingDragged);

                if (pane != null) {
                    if (presentationSite.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
                        presentationSite
                                .setState(IStackPresentationSite.STATE_RESTORED);
                    }

                    DragUtil.performDrag(pane, Geometry.toDisplay(getParent(),
                            getPresentation().getControl().getBounds()),
                            initialLocation, !keyboard);
                }
            }
        }
    }

    /**
     * @return Returns the savedPresentationState.
     */
    public IMemento getSavedPresentationState() {
        return savedPresentationState;
    }
}