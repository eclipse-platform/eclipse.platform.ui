/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Gross chris.gross@us.ibm.com Bug 107443
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Geometry;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.layout.ITrimManager;
import org.eclipse.ui.internal.layout.IWindowTrim;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.presentations.PresentationSerializer;
import org.eclipse.ui.internal.util.PrefUtil;
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

    public static final int PROP_SELECTION = 0x42;
    
    private List children = new ArrayList(3);
    private boolean isActive = true;
    private ArrayList presentableParts = new ArrayList();
    
    private Map properties = new HashMap();
    
    protected int appearance = PresentationFactoryUtil.ROLE_VIEW;
    
    /**
     * Stores the last value passed to setSelection. If UI updates are being deferred,
     * this may be significantly different from the other current pointers. Once UI updates
     * are re-enabled, the stack will update the presentation selection to match the requested
     * current pointer.
     */ 
    private LayoutPart requestedCurrent;
    
    /**
     * Stores the current part for the stack. Whenever the outside world asks a PartStack
     * for the current part, this is what gets returned. This pointer is only updated after
     * the presentation selection has been restored and the stack has finished updating its
     * internal state. If the stack is still in the process of updating the presentation,
     * it will still point to the previous part until the presentation is up-to-date.
     */
    private LayoutPart current;
    
    /**
     * Stores the presentable part sent to the presentation. Whenever the presentation
     * asks for the current part, this is what gets returned. This is updated before sending
     * the part to the presentation, and it is not updated while UI updates are disabled.
     * When UI updates are enabled, the stack first makes presentationCurrent match 
     * requestedCurrent. Once the presentation is displaying the correct part, the "current"
     * pointer on PartStack is updated.
     */
    private PresentablePart presentationCurrent;

    private boolean ignoreSelectionChanges = false;

    protected IMemento savedPresentationState = null;

    protected DefaultStackPresentationSite presentationSite = new DefaultStackPresentationSite() {

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

        public IPresentablePart[] getPartList() {
            List parts = getPresentableParts();
            
            return (IPresentablePart[]) parts.toArray(new IPresentablePart[parts.size()]);
        }

        public String getProperty(String id) {            
            return PartStack.this.getProperty(id);
        }
    };

    private static final class PartStackDropResult extends AbstractDropTarget {
        private PartPane pane;
        
        // Result of the presentation's dragOver method or null if we are stacking over the
        // client area of the pane.
        private StackDropResult dropResult;
        private PartStack stack;
        
        /**
         * Resets the target of this drop result (allows the same drop result object to be
         * reused)
         * 
         * @param stack
         * @param pane
         * @param result result of the presentation's dragOver method, or null if we are
         * simply stacking anywhere.
         * @since 3.1
         */
        public void setTarget(PartStack stack, PartPane pane, StackDropResult result) {
            this.pane = pane;
            this.dropResult = result;
            this.stack = stack;
        }
        
        public void drop() {
            // If we're dragging a pane over itself do nothing
            //if (dropResult.getInsertionPoint() == pane.getPresentablePart()) { return; };

            Object cookie = null;
            if (dropResult != null) {
                cookie = dropResult.getCookie();
            }

            // Handle cross window drops by opening a new editor
            if (pane instanceof EditorPane) {
            	if (pane.getWorkbenchWindow() != stack.getWorkbenchWindow()) {
            		EditorPane editor = (EditorPane) pane;
            		try {
						IEditorInput input = editor.getEditorReference().getEditorInput();
						
						// Close the old editor and capture the actual closed state incase of a 'cancel'
						boolean editorClosed = editor.getPage().closeEditor(editor.getEditorReference(), true);
						
						// Only open open the new editor if the old one closed
						if (editorClosed)
							stack.getPage().openEditor(input, editor.getEditorReference().getId());
						return;
					} catch (PartInitException e) {
						e.printStackTrace();
					}
            		
            	}
            }
            
            if (pane.getContainer() != stack) {
                // Moving from another stack
                stack.derefPart(pane);
                pane.reparent(stack.getParent());
                stack.add(pane, cookie);
                stack.setSelection(pane);
                pane.setFocus();
            } else if (cookie != null) {
                // Rearranging within this stack
                stack.getPresentation().movePart(stack.getPresentablePart(pane), cookie);
            }
        }

        public Cursor getCursor() {
            return DragCursors.getCursor(DragCursors.CENTER);
        }

        public Rectangle getSnapRectangle() {
            if (dropResult == null) {
                return DragUtil.getDisplayBounds(stack.getControl());
            }
            return dropResult.getSnapRectangle();
        }
    }

    private static final PartStackDropResult dropResult = new PartStackDropResult();

    protected boolean isMinimized;

    private ListenerList listeners = new ListenerList();

    /**
     * Custom presentation factory to use for this stack, or null to
     * use the default
     */
    private AbstractPresentationFactory factory;

	private boolean smartZoomed = false;
	private boolean doingUnzoom = false;
            
    protected abstract boolean isMoveable(IPresentablePart part);

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
        this(appearance, null);
    }
    
    /**
     * Creates a new part stack that uses the given custom presentation factory
     * @param appearance
     * @param factory custom factory to use (or null to use the default)
     */
    public PartStack(int appearance, AbstractPresentationFactory factory) {
        super("PartStack"); //$NON-NLS-1$

        this.appearance = appearance;
        this.factory = factory;
    }

    /**
     * Adds a property listener to this stack. The listener will receive a PROP_SELECTION
     * event whenever the result of getSelection changes
     * 
     * @param listener
     */
    public void addListener(IPropertyListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(IPropertyListener listener) {
        listeners.remove(listener);
    }
    
    protected final boolean isStandalone() {
        return (appearance == PresentationFactoryUtil.ROLE_STANDALONE 
             || appearance == PresentationFactoryUtil.ROLE_STANDALONE_NOTITLE);
    }
    
    /**
     * Returns the currently selected IPresentablePart, or null if none
     * 
     * @return
     */
    protected IPresentablePart getSelectedPart() {
        return presentationCurrent;
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
            IPresentablePart part = getPresentablePart(child);
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
        
        // Check to that we're displaying the zoomed icon iff we're actually maximized
        Assert.isTrue((getState() == IStackPresentationSite.STATE_MAXIMIZED) 
                == (getContainer() != null && getContainer().childIsZoomed(this)));

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

                if (next == requestedCurrent) {
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
        add(child, null);
    }

    /**
     * Add a part at a particular position
     */
    protected void add(LayoutPart newChild, Object cookie) {
        children.add(newChild);
        
        // Fix for bug 78470:
        if(!(newChild.getContainer() instanceof ContainerPlaceholder)) {
			newChild.setContainer(this);
		}
        
        showPart(newChild, cookie);
    }

    public boolean allowsAdd(LayoutPart toAdd) {
        return !isStandalone();
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

        return super.allowsAutoFocus();
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
        
        if (factory != null) {
            return factory;
        }
        
        return ((WorkbenchWindow) getPage()
                .getWorkbenchWindow()).getWindowConfigurer()
                .getPresentationFactory();
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
        getControl().moveBelow(null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#getDropTarget(java.lang.Object, org.eclipse.swt.graphics.Point)
     */
    public IDropTarget getDropTarget(Object draggedObject, Point position) {

        if (!(draggedObject instanceof PartPane)) {
            return null;
        }

        final PartPane pane = (PartPane) draggedObject;
        if (isStandalone() 
                || !allowsDrop(pane)) {
            return null;
        }

        // Don't allow views to be dragged between windows
        boolean differentWindows = pane.getWorkbenchWindow() != getWorkbenchWindow();
        boolean editorDropOK = ((pane instanceof EditorPane) && 
        		pane.getWorkbenchWindow().getWorkbench() == 
        			getWorkbenchWindow().getWorkbench());
        if (differentWindows && !editorDropOK) {
            return null;
        }

        StackDropResult dropResult = getPresentation().dragOver(
                getControl(), position);
        
        if (dropResult == null) {
        	return null;
        }
        
        return createDropTarget(pane, dropResult); 
    }
    
    public void setActive(boolean isActive) {
 
        this.isActive = isActive;
        // Add all visible children to the presentation
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart part = (LayoutPart) iter.next();
            
            part.setContainer(isActive ? this : null);
        }
        
        for (Iterator iterator = presentableParts.iterator(); iterator.hasNext();) {
            PresentablePart next = (PresentablePart) iterator.next();
            
            next.enableInputs(isActive);
            next.enableOutputs(isActive);
        }
    }
    
    public void createControl(Composite parent, StackPresentation presentation) {

        Assert.isTrue(isDisposed());

        if (presentationSite.getPresentation() != null) {
			return;
		}

        presentationSite.setPresentation(presentation);

        // Add all visible children to the presentation
        // Use a copy of the current set of children to avoid a ConcurrentModificationException
        // if a part is added to the same stack while iterating over the children (bug 78470)
        LayoutPart[] childParts = (LayoutPart[]) children.toArray(new LayoutPart[children.size()]);
        for (int i = 0; i < childParts.length; i++) {
			LayoutPart part = childParts[i];
            showPart(part, null);
        }
        
        if (savedPresentationState!=null) {
			PresentationSerializer serializer = new PresentationSerializer(
					getPresentableParts());
			presentation.restoreState(serializer, savedPresentationState);
		}

        Control ctrl = getPresentation().getControl();

        ctrl.setData(this);

        // We should not have a placeholder selected once we've created the widgetry
        if (requestedCurrent instanceof PartPlaceholder) {
            requestedCurrent = null;
            updateContainerVisibleTab();
        }

        refreshPresentationSelection();
    }

    public IDropTarget createDropTarget(PartPane pane, StackDropResult result) {
        dropResult.setTarget(this, pane, result);
        return dropResult;
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

        if (isDisposed()) {
			return;
		}

        savePresentationState();

        presentationSite.dispose();
        
        for (Iterator iter = presentableParts.iterator(); iter.hasNext();) {
            PresentablePart part = (PresentablePart) iter.next();
            
            part.dispose();
        }
        presentableParts.clear();
        
        presentationCurrent = null;
        current = null;
        fireInternalPropertyChange(PROP_SELECTION);
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
        if (part == null || !(part instanceof PresentablePart)) {
            return null;
        }

        return ((PresentablePart)part).getPane();
    }

    /**
     * Get the parent control.
     */
    public Composite getParent() {
        return getControl().getParent();
    }

    /**
     * Returns a list of IPresentablePart
     * 
     * @return
     */
    public List getPresentableParts() {
        return presentableParts;
    }

    private PresentablePart getPresentablePart(LayoutPart pane) {
        for (Iterator iter = presentableParts.iterator(); iter.hasNext();) {
            PresentablePart part = (PresentablePart) iter.next();
            
            if (part.getPane() == pane) {
                return part;
            }
        }
        
        return null;
    }
    
    protected StackPresentation getPresentation() {
        return presentationSite.getPresentation();
    }

    /**
     * Returns the visible child.
     * @return the currently visible part, or null if none
     */
    public PartPane getSelection() {
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

        if (newPart == requestedCurrent) {
            return;
        }

        setSelection(newPart);

        if (newPart != null) {
            newPart.setFocus();
        }

    }

    /**
     * See IVisualContainer#remove
     */
    public void remove(LayoutPart child) {
        PresentablePart presentablePart = getPresentablePart(child);

        // Need to remove it from the list of children before notifying the presentation
        // since it may setVisible(false) on the part, leading to a partHidden notification,
        // during which findView must not find the view being removed.  See bug 60039. 
        children.remove(child);

        StackPresentation presentation = getPresentation();

        if (presentablePart != null && presentation != null) {
            ignoreSelectionChanges = true;
            presentableParts .remove(presentablePart);
            presentation.removePart(presentablePart);
            presentablePart.dispose();
            ignoreSelectionChanges = false;
        }

        if (!isDisposed()) {
            child.setContainer(null);
        }

        if (child == requestedCurrent) {
            updateContainerVisibleTab();
        }
    }

    /**
     * Reparent a part. Also reparent visible children...
     */
    public void reparent(Composite newParent) {

        Control control = getControl();
        if ((control == null) || (control.getParent() == newParent) || !control.isReparentable()) {
			return;
		}

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
        int idx = children.indexOf(oldChild);
        int numPlaceholders = 0;
        //subtract the number of placeholders still existing in the list 
        //before this one - they wont have parts.
        for (int i = 0; i < idx; i++) {
            if (children.get(i) instanceof PartPlaceholder) {
				numPlaceholders++;
			}
        }
        Integer cookie = new Integer(idx - numPlaceholders);
        children.add(idx, newChild);

        showPart(newChild, cookie);

        if (oldChild == requestedCurrent && !(newChild instanceof PartPlaceholder)) {
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
        StackPresentation presentation = getPresentation();
        
        if (presentation != null) {
            return presentation.getSizeFlags(horizontal);
        } 
        
        return 0;
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
                    setSelection(part);
                    // Mark this as the active part.
                    //current = part;
                }
            }
        }

        IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
        boolean useNewMinMax = preferenceStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
        final Integer expanded = memento.getInteger(IWorkbenchConstants.TAG_EXPANDED);
        if (useNewMinMax && expanded != null) {
            StartupThreading.runWithoutExceptions(new StartupRunnable() {
    			public void runWithException() throws Throwable {    		        
    		        setState((expanded == null || expanded.intValue() != IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_RESTORED
    		                : IStackPresentationSite.STATE_MINIMIZED);
    			}
            });
        }
        else {
	        setState((expanded == null || expanded.intValue() != IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_RESTORED
	                : IStackPresentationSite.STATE_MINIMIZED);
        }

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

        IMemento propertiesState = memento.getChild(IWorkbenchConstants.TAG_PROPERTIES);
        if (propertiesState != null) {
            IMemento[] props = propertiesState.getChildren(IWorkbenchConstants.TAG_PROPERTY);
            for (int i = 0; i < props.length; i++) {
                properties.put(props[i].getID(), props[i].getTextData());
            }
        }
                
        
        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#setVisible(boolean)
     */
    public void setVisible(boolean makeVisible) {
        Control ctrl = getControl();
        
        boolean useShortcut = makeVisible || !isActive;
        
        if (!SwtUtil.isDisposed(ctrl) && useShortcut) {
            if (makeVisible == ctrl.getVisible()) {
				return;
			}
        }        
        
        if (makeVisible) {
            for (Iterator iterator = presentableParts.iterator(); iterator.hasNext();) {
                PresentablePart next = (PresentablePart) iterator.next();
                
                next.enableInputs(isActive);
                next.enableOutputs(isActive);
            }
        }
        
        super.setVisible(makeVisible);
        
        StackPresentation presentation = getPresentation();
        
        if (presentation != null) {
            presentation.setVisible(makeVisible);
        }
        
        if (!makeVisible) {
            for (Iterator iterator = presentableParts.iterator(); iterator.hasNext();) {
                PresentablePart next = (PresentablePart) iterator.next();
                
                next.enableInputs(false);
            }
        }
    }
    
    /**
     * @see IPersistable
     */
    public IStatus saveState(IMemento memento) {

        // Save the active tab.
        if (requestedCurrent != null) {
			memento.putString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID, requestedCurrent
                    .getCompoundId());
		}

        // Write out the presentable parts (in order)
        Set cachedIds = new HashSet();
        Iterator ppIter = getPresentableParts().iterator();
        while (ppIter.hasNext()) {
            PresentablePart presPart = (PresentablePart) ppIter.next();

            IMemento childMem = memento
                    .createChild(IWorkbenchConstants.TAG_PAGE);
            PartPane part = presPart.getPane();
            String tabText = part.getPartReference().getPartName();

            childMem.putString(IWorkbenchConstants.TAG_LABEL, tabText);
            childMem.putString(IWorkbenchConstants.TAG_CONTENT, presPart.getPane().getPlaceHolderId());
            
            // Cache the id so we don't write it out later
            cachedIds.add(presPart.getPane().getPlaceHolderId());
        }

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            PartPane part = null;
            if (next instanceof PartPane) {
            	// Have we already written it out?
            	if (cachedIds.contains(((PartPane)next).getPlaceHolderId()))
            		continue;
            	
                part = (PartPane)next;
            }

            IMemento childMem = memento
                    .createChild(IWorkbenchConstants.TAG_PAGE);

            String tabText = "LabelNotFound"; //$NON-NLS-1$ 
            if (part != null) {
                tabText = part.getPartReference().getPartName();
            }
            childMem.putString(IWorkbenchConstants.TAG_LABEL, tabText);
            childMem.putString(IWorkbenchConstants.TAG_CONTENT, next
                    .getCompoundId());
        }

        IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
        boolean useNewMinMax = preferenceStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
        if (useNewMinMax) {
            memento.putInteger(IWorkbenchConstants.TAG_EXPANDED, presentationSite.getState());
        }
        else {
            memento
            .putInteger(
                    IWorkbenchConstants.TAG_EXPANDED,
                    (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_MINIMIZED
                            : IStackPresentationSite.STATE_RESTORED);
        }

        memento.putInteger(IWorkbenchConstants.TAG_APPEARANCE, appearance);

        savePresentationState();

        if (savedPresentationState != null) {
            IMemento presentationState = memento
                    .createChild(IWorkbenchConstants.TAG_PRESENTATION);
            presentationState.putMemento(savedPresentationState);
        }
        
        if (!properties.isEmpty()) {
            IMemento propertiesState = memento.createChild(IWorkbenchConstants.TAG_PROPERTIES);
            Set ids = properties.keySet();
            for (Iterator iterator = ids.iterator(); iterator.hasNext();) {   
                String id = (String)iterator.next();
                
                if (properties.get(id) == null) continue;
                
                IMemento prop = propertiesState.createChild(IWorkbenchConstants.TAG_PROPERTY, id);
                prop.putTextData((String)properties.get(id));
            }
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

        if (activeState == StackPresentation.AS_ACTIVE_FOCUS && isMinimized) {
            setMinimized(false);
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
        if (part == requestedCurrent) {
            return;
        }

        requestedCurrent = part;
        
        refreshPresentationSelection();
    }

    /**
     * Subclasses should override this method to update the enablement state of their
     * actions
     */
    protected abstract void updateActions(PresentablePart current);

    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#handleDeferredEvents()
	 */
	protected void handleDeferredEvents() {
		super.handleDeferredEvents();
		
		refreshPresentationSelection();
	}
    
    private void refreshPresentationSelection() {        
        // If deferring UI updates, exit.
    	if (isDeferred()) {
    		return;
    	}
        
        // If the presentation is already displaying the desired part, then there's nothing
        // to do.
        if (current == requestedCurrent) {
            return;
        }

        StackPresentation presentation = getPresentation();
        if (presentation != null) {
        
            presentationCurrent = getPresentablePart(requestedCurrent);
            
            if (!isDisposed()) {
                updateActions(presentationCurrent);
            }
            
            if (presentationCurrent != null && presentation != null) {
                requestedCurrent.createControl(getParent());
                if (requestedCurrent.getControl().getParent() != getControl()
                        .getParent()) {
                    requestedCurrent.reparent(getControl().getParent());
                }

               
                presentation.selectPart(presentationCurrent);
                
             }
        
            // Update the return value of getVisiblePart
            current = requestedCurrent;
            fireInternalPropertyChange(PROP_SELECTION);
        }
    }

    public int getState() {
    	return presentationSite.getState();
    }

	/**
	 * Sets the minimized state for this stack. The part may call this method to
	 * minimize or restore itself. The minimized state only affects the view
	 * when unzoomed in the 3.0 presentation (in 3.3 it's handled by the
	 * ViewStack directly and works as expected).
	 */
	public void setMinimized(boolean minimized) {
		if (minimized != isMinimized) {
			isMinimized = minimized;

			refreshPresentationState();
		}
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.ILayoutContainer#obscuredByZoom(org.eclipse.ui.internal.LayoutPart)
     */
    public boolean childObscuredByZoom(LayoutPart toTest) {
        return isObscuredByZoom();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#requestZoom(org.eclipse.ui.internal.LayoutPart)
     */
    public void childRequestZoomIn(LayoutPart toZoom) {
        super.childRequestZoomIn(toZoom);
        
        requestZoomIn();
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#requestZoomOut()
     */
    public void childRequestZoomOut() {
        super.childRequestZoomOut();
        
        requestZoomOut();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.ILayoutContainer#isZoomed(org.eclipse.ui.internal.LayoutPart)
     */
    public boolean childIsZoomed(LayoutPart toTest) {
        return isZoomed();
    }
    
    /**
     * This is a hack that allows us to preserve the old
     * min/max behavior for the stack containing the IntroPart.
     * This is required to have the initial Intro (Welcome)
     * pane to show correctly but will induce strange
     * effects should a user re-locate the part to
     * stacks other that its initial one...
     *  
     * @return true if the stack contains the intro
     * as a ViewPane (not if it's only a placeholder)
     */
    private boolean isIntroInStack() {
    	LayoutPart[] kids = getChildren();
    	for (int i = 0; i < kids.length; i++) {
    		if (kids[i] instanceof ViewPane) {
    			ViewPane vp = (ViewPane) kids[i];
    			if (vp.getID().equals(IIntroConstants.INTRO_VIEW_ID))
    				return true;
    		}
		}
    	return false;
    }

    private void smartZoom() {
		WorkbenchWindow wbw = (WorkbenchWindow) getPage().getWorkbenchWindow();
		if (wbw == null || wbw.getShell() == null)
			return;

		Perspective perspective = getPage().getActivePerspective();
		FastViewManager fvm = perspective.getFastViewManager();

		fvm.deferUpdates(true);

		// Cache the layout bounds
		perspective.getPresentation().updateBoundsMap();
		
		LayoutPart[] children = perspective.getPresentation().getLayout().getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != this) {
				if (children[i] instanceof ViewStack) {
					((ViewStack) children[i]).setMinimized(true);
					ViewStackTrimToolBar vstb = fvm
							.getViewStackTrimToolbar(children[i]
									.getID());
					vstb.setRestoreOnUnzoom(true);
				}
				else if (children[i] instanceof EditorSashContainer && !(this instanceof EditorStack)) {
					perspective.setEditorAreaState(IStackPresentationSite.STATE_MINIMIZED);
					perspective.setEditorAreaRestoreOnUnzoom(true);
				}
			}
		}

		// If the editor area has changed state tell the perspective
		if (this instanceof EditorStack)
			perspective.setEditorAreaState(IStackPresentationSite.STATE_MAXIMIZED);

		// Clear the boundsMap
		perspective.getPresentation().resetBoundsMap();
		
		// We're done batching...
		fvm.deferUpdates(false);
		
		perspective.getPresentation().setMaximizedStack(this);
		smartZoomed = true;
    }

    protected void smartUnzoom() {
    	// Prevent recursion through 'setMinimized'
    	if (doingUnzoom)
    		return;
    	doingUnzoom = true;
    	
		WorkbenchWindow wbw = (WorkbenchWindow) getPage().getWorkbenchWindow();
		if (wbw == null || wbw.getShell() == null)
			return;

		ITrimManager tbm = wbw.getTrimManager();
		Perspective perspective = getPage().getActivePerspective();
		FastViewManager fvm = perspective.getFastViewManager();

		ILayoutContainer root = getContainer();

		// We go up one more level when maximizing an editor stack
		// so that we 'zoom' the editor area
		boolean restoringEditorArea = false;
		if (root instanceof EditorSashContainer) {
			root = ((EditorSashContainer) root).getContainer();
			restoringEditorArea = true;
		}

		// This is a compound operation
		fvm.deferUpdates(true);
		
		LayoutPart[] children = root.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != this) {
				IWindowTrim trim = tbm.getTrim(children[i].getID());
				if (trim == null)
					continue;

				if (trim instanceof ViewStackTrimToolBar) {
					ViewStackTrimToolBar vstb = (ViewStackTrimToolBar) trim;
					if (vstb.restoreOnUnzoom()
							&& children[i] instanceof ContainerPlaceholder) {
						// In the current presentation its a
						// container placeholder
						ViewStack realStack = (ViewStack) ((ContainerPlaceholder) children[i])
								.getRealContainer();
						realStack.setMinimized(false);

						vstb.setRestoreOnUnzoom(false);
					}
				} else if (trim instanceof EditorAreaTrimToolBar) {
					if (perspective.getEditorAreaRestoreOnUnzoom())
					perspective.setEditorAreaState(IStackPresentationSite.STATE_RESTORED);
				}
			}
		}

		// If the editor area has changed state tell the perspective
		if (restoringEditorArea)
			perspective.setEditorAreaState(IStackPresentationSite.STATE_RESTORED);

		perspective.getPresentation().setMaximizedStack(null);
		
		fvm.deferUpdates(false);
		smartZoomed = false;
		
		doingUnzoom = false;
    }
    
	protected void setState(final int newState) {
		final int oldState = presentationSite.getState();
		if (!supportsState(newState) || newState == oldState) {
			return;
		}

		final WorkbenchWindow wbw = (WorkbenchWindow) getPage().getWorkbenchWindow();
		if (wbw == null || wbw.getShell() == null || wbw.getActiveWorkbenchPage() == null)
			return;

		WorkbenchPage page = wbw.getActiveWorkbenchPage();
		if (page == null)
			return;
		
		boolean useNewMinMax = Perspective.useNewMinMax(page.getActivePerspective());

		// we have to fiddle with the zoom behavior to satisfy Intro req's
		// by usning the old zoom behavior for its stack
		if (newState == IStackPresentationSite.STATE_MAXIMIZED)
			useNewMinMax = useNewMinMax && !isIntroInStack();
		else if (newState == IStackPresentationSite.STATE_RESTORED) {
			PartStack maxStack = page.getActivePerspective().getPresentation().getMaximizedStack();
			useNewMinMax = useNewMinMax && maxStack == this;
		}

		if (useNewMinMax) {
        	StartupThreading.runWithoutExceptions(new StartupRunnable() {
				public void runWithException() throws Throwable {
					wbw.getPageComposite().setRedraw(false);
					try {
						if (newState == IStackPresentationSite.STATE_MAXIMIZED) {
							smartZoom();
						} else if (oldState == IStackPresentationSite.STATE_MAXIMIZED) {
							smartUnzoom();
						}
						
						if (newState == IStackPresentationSite.STATE_MINIMIZED) {
							setMinimized(true);
						}
					} finally {
						wbw.getPageComposite().setRedraw(true);

						// Force a redraw (fixes Mac refresh)
						wbw.getShell().redraw();
					}

					setPresentationState(newState);
				}
			});
		} else {
			boolean minimized = (newState == IStackPresentationSite.STATE_MINIMIZED);
			setMinimized(minimized);

			if (newState == IStackPresentationSite.STATE_MAXIMIZED) {
				requestZoomIn();
			} else if (oldState == IStackPresentationSite.STATE_MAXIMIZED) {
				requestZoomOut();
				
				if (newState == IStackPresentationSite.STATE_MINIMIZED)
					setMinimized(true);
			}
		}
	}
    

    /**
     * Called by the workbench page to notify this part that it has been zoomed or unzoomed.
     * The PartStack should not call this method itself -- it must request zoom changes by 
     * talking to the WorkbenchPage.
     */
    public void setZoomed(boolean isZoomed) {
        
        super.setZoomed(isZoomed);
        
        LayoutPart[] children = getChildren();
        
        for (int i = 0; i < children.length; i++) {
            LayoutPart next = children[i];
            
            next.setZoomed(isZoomed);
        }
        
        refreshPresentationState();
    }
    
    public boolean isZoomed() {
        ILayoutContainer container = getContainer();
        
        if (container != null) {
            return container.childIsZoomed(this);
        }
        
        return false;
    }
    
    protected void refreshPresentationState() {
        if (isZoomed() || smartZoomed) {
            presentationSite.setPresentationState(IStackPresentationSite.STATE_MAXIMIZED);
        } else {
            
            boolean wasMinimized = (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED);
            
            if (isMinimized) {
                presentationSite.setPresentationState(IStackPresentationSite.STATE_MINIMIZED);
            } else {
                presentationSite.setPresentationState(IStackPresentationSite.STATE_RESTORED);
            }
            
            if (isMinimized != wasMinimized) {
                flushLayout();
                
                if (isMinimized) {
	                WorkbenchPage page = getPage();
	
	                if (page != null) {
	                    page.refreshActiveView();
	                }
                }
            }
        }
    }

    /**
     * Makes the given part visible in the presentation.
     * @param part the part to add to the stack
     * @param cookie other information
     */
    private void showPart(LayoutPart part, Object cookie) {

        if (isDisposed()) {
            return;
        }
        
        if ((part instanceof PartPlaceholder)) {
        	part.setContainer(this);
        	return;
        }

        if (!(part instanceof PartPane)) {
			WorkbenchPlugin.log(NLS.bind(
					WorkbenchMessages.PartStack_incorrectPartInFolder, part
							.getID()));
			return;
		}
        
        PartPane pane = (PartPane)part;
        
        PresentablePart presentablePart = new PresentablePart(pane, getControl().getParent());
        presentableParts.add(presentablePart);
        
        if (isActive) {
            part.setContainer(this);
            
            // The active part should always be enabled
            if (part.getControl() != null)
            	part.getControl().setEnabled(true);
        }
        
        presentationSite.getPresentation().addPart(presentablePart, cookie);

        if (requestedCurrent == null) {
            setSelection(part);
        }
        
        if (childObscuredByZoom(part)) {
			presentablePart.enableInputs(false);
		}
    }

    /**
	 * Update the container to show the correct visible tab based on the
	 * activation list.
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
    
    public Control[] getTabList(LayoutPart part) {
        if (part != null) {
            IPresentablePart presentablePart = getPresentablePart(part);
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
    private void dragStart(IPresentablePart beingDragged, Point initialLocation,
            boolean keyboard) {
        if (beingDragged == null) {
            paneDragStart((LayoutPart)null, initialLocation, keyboard);
        } else {
            if (presentationSite.isPartMoveable(beingDragged)) {
                LayoutPart pane = getPaneFor(beingDragged);

                if (pane != null) {
                    paneDragStart(pane, initialLocation, keyboard);
                }
            }
        }
    }
    
    public void paneDragStart(LayoutPart pane, Point initialLocation,
            boolean keyboard) {
        if (pane == null) {
            if (canMoveFolder()) {            	
                if (presentationSite.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
                	// Calculate where the initial location was BEFORE the 'restore'...as a percentage
                	Rectangle bounds = Geometry.toDisplay(getParent(), getPresentation().getControl().getBounds());
                	float xpct = (initialLocation.x - bounds.x) / (float)(bounds.width);
                	float ypct = (initialLocation.y - bounds.y) / (float)(bounds.height);

                	// Only restore if we're dragging views/view stacks
                	if (this instanceof ViewStack)
                		setState(IStackPresentationSite.STATE_RESTORED);

                	// Now, adjust the initial location to be within the bounds of the restored rect
                	bounds = Geometry.toDisplay(getParent(), getPresentation().getControl().getBounds());
                	initialLocation.x = (int) (bounds.x + (xpct * bounds.width));
                	initialLocation.y = (int) (bounds.y + (ypct * bounds.height));
                }
    
                DragUtil.performDrag(PartStack.this, Geometry
                        .toDisplay(getParent(), getPresentation().getControl()
                                .getBounds()), initialLocation, !keyboard);
            }
        } else {
            if (presentationSite.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
            	// Calculate where the initial location was BEFORE the 'restore'...as a percentage
            	Rectangle bounds = Geometry.toDisplay(getParent(), getPresentation().getControl().getBounds());
            	float xpct = (initialLocation.x - bounds.x) / (float)(bounds.width);
            	float ypct = (initialLocation.y - bounds.y) / (float)(bounds.height);
            	
            	// Only restore if we're dragging views/view stacks
            	if (this instanceof ViewStack)
            		setState(IStackPresentationSite.STATE_RESTORED);

            	// Now, adjust the initial location to be within the bounds of the restored rect
            	// See bug 100908
            	bounds = Geometry.toDisplay(getParent(), getPresentation().getControl().getBounds());
            	initialLocation.x = (int) (bounds.x + (xpct * bounds.width));
            	initialLocation.y = (int) (bounds.y + (ypct * bounds.height));
            }
    
            DragUtil.performDrag(pane, Geometry.toDisplay(getParent(),
                    getPresentation().getControl().getBounds()),
                    initialLocation, !keyboard);
        }
    }

    /**
     * @return Returns the savedPresentationState.
     */
    public IMemento getSavedPresentationState() {
        return savedPresentationState;
    }
    
    private void fireInternalPropertyChange(int id) {
        Object listeners[] = this.listeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyListener) listeners[i]).propertyChanged(this, id);
        }
    }
    
    // TrimStack Support
    
    /**
     * Explicitly sets the presentation state. This is used by the
     * new min/max code to force the CTabFolder to show the proper
     * state without going through the 'setState' code (which causes
     * nasty side-effects.
     * @param newState The state to set the presentation to
     */
    public void setPresentationState(int newState) {
    	presentationSite.setPresentationState(newState);
    }

    //
    // Support for passing perspective layout properties to the presentation

    
    public String getProperty(String id)  {
        return (String)properties.get(id);
    }
    
    public void setProperty(String id, String value) {
    	if (value==null) {
    		properties.remove(id);
    	} else {
    		properties.put(id, value);
    	}
    }
    
    /**
     * Copies all appearance related data from this stack to the given stack.
     */
    public void copyAppearanceProperties(PartStack copyTo) {
        copyTo.appearance = this.appearance;
        if (!properties.isEmpty()) {
            Set ids = properties.keySet();
            for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
                String id = (String)iterator.next();
                copyTo.setProperty(id, (String)properties.get(id));
            }
        }
    }
}
