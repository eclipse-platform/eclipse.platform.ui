/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDragOverListener;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.misc.StringMatcher;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * A perspective presentation is a collection of parts with a layout. Each part
 * is parented to a main window, so you can create more than one presentation
 * on a set of parts and change the layout just by activating / deactivating a
 * presentation.
 * 
 * In addition, the user can change the position of any part by mouse
 * manipulation (drag & drop). If a part is removed, we leave a placeholder
 * behind to indicate where it goes should the part be added back.
 */
public class PerspectiveHelper {
    private WorkbenchPage page;
    
    protected Perspective perspective;

    protected Composite parentWidget;

    private ViewSashContainer mainLayout;
    
	private PartStack maximizedStack;

	/**
	 * If there is a ViewStack maximized on shutdown the id is
	 * cached and restored into this field on 'restoreState'.
	 * This is then used to bash the ViewStack's presentation state
	 * into the correct value on activation (the startup life-cycle
	 * is such that we have to use this 'latch' because the window
	 * state isn't valid until the activate happens.
	 */
	private String maximizedStackId;

    private ArrayList detachedWindowList = new ArrayList(1);

    private ArrayList detachedPlaceHolderList = new ArrayList(1);

	/**
	 * Maps a stack's id to its current bounds
	 * this is used to capture the current bounds of all
	 * stacks -before- starting a maximize (since the
	 * iterative 'minimize' calls cause the intial stack's
	 * bounds to change.
	 */
	private Map boundsMap = new HashMap();

    private boolean detachable = false;

    protected boolean active = false;

    // key is the LayoutPart object, value is the PartDragDrop object
    //private IPartDropListener partDropListener;

    private static final int MIN_DETACH_WIDTH = 150;

    private static final int MIN_DETACH_HEIGHT = 250;

    protected ActualDropTarget dropTarget;
    
    private IDragOverListener dragTarget = new IDragOverListener() {

        public IDropTarget drag(Control currentControl, Object draggedObject,
                Point position, final Rectangle dragRectangle) {
            
            if (!(draggedObject instanceof ViewPane || draggedObject instanceof ViewStack)) {
                return null;
            }
            final LayoutPart part = (LayoutPart) draggedObject;

            if (part.getWorkbenchWindow() != page.getWorkbenchWindow()) {
                return null;
            }

            if (dropTarget == null) {
                dropTarget = new ActualDropTarget(part, dragRectangle); 
            } else {
                dropTarget.setTarget(part, dragRectangle);
            }
            
            return dropTarget;
        }

    };

    private final class ActualDropTarget extends AbstractDropTarget {
        private LayoutPart part;

        private Rectangle dragRectangle;

        private ActualDropTarget(LayoutPart part, Rectangle dragRectangle) {
            super();
            setTarget(part, dragRectangle);
        }

        /**
         * @param part
         * @param dragRectangle
         * @since 3.1
         */
        private void setTarget(LayoutPart part, Rectangle dragRectangle) {
            this.part = part;
            this.dragRectangle = dragRectangle;
        }

        public void drop() {

            Shell shell = part.getShell();
            if (shell.getData() instanceof DetachedWindow) {
                // only one tab folder in a detach window, so do window
                // move
                if (part instanceof ViewStack) {
                    shell.setLocation(dragRectangle.x,
                            dragRectangle.y);
                    return;
                }
                // if only one view in tab folder then do a window move
                ILayoutContainer container = part.getContainer();
                if (container instanceof ViewStack) {
                    if (((ViewStack) container).getItemCount() == 1) {
                        shell.setLocation(dragRectangle.x,
                                dragRectangle.y);
                        return;
                    }
                }
            }

            // If layout is modified always zoom out.
            if (isZoomed()) {
				zoomOut();
			}
            // do a normal part detach
            detach(part, dragRectangle.x, dragRectangle.y);
        }

        public Cursor getCursor() {
            return DragCursors.getCursor(DragCursors.OFFSCREEN);
        }
    }

    private class MatchingPart implements Comparable {
        String pid;

        String sid;

        LayoutPart part;

        boolean hasWildcard;

        int len;

        MatchingPart(String pid, String sid, LayoutPart part) {
            this.pid = pid;
            this.sid = sid;
            this.part = part;
            this.len = (pid == null ? 0 : pid.length())
                    + (sid == null ? 0 : sid.length());
            this.hasWildcard = (pid != null && pid
                    .indexOf(PartPlaceholder.WILD_CARD) != -1)
                    || (sid != null && sid.indexOf(PartPlaceholder.WILD_CARD) != -1);
        }

        public int compareTo(Object a) {
            // specific ids always outweigh ids with wildcards
            MatchingPart ma = (MatchingPart) a;
            if (this.hasWildcard && !ma.hasWildcard) {
                return -1;
            }
            if (!this.hasWildcard && ma.hasWildcard) {
                return 1;
            }
            // if both are specific or both have wildcards, simply compare based on length
            return ma.len - this.len;
        }
    }

    
    /**
     * Constructs a new object.
     */
    public PerspectiveHelper(WorkbenchPage workbenchPage,
            ViewSashContainer mainLayout, Perspective perspective) {
        this.page = workbenchPage;
        this.mainLayout = mainLayout;
        this.perspective = perspective;
        
		// Views can be detached if the feature is enabled (true by default,
		// use the plug-in customization file to disable), and if the platform
		// supports detaching.
         
        final IPreferenceStore store = PlatformUI.getPreferenceStore();
        this.detachable = store.getBoolean(IWorkbenchPreferenceConstants.ENABLE_DETACHED_VIEWS);

        if (this.detachable) {
			// Check if some arbitrary Composite supports reparenting. If it
			// doesn't, views cannot be detached.

			Composite client = workbenchPage.getClientComposite();
			if (client == null) {
				// The workbench page is not initialized. I don't think this can happen,
				// but if it does, silently set detachable to false.
				this.detachable = false;
			} else {
				Composite testChild = new Composite(client, SWT.NONE);
				this.detachable = testChild.isReparentable();
				testChild.dispose();
			}
		}
    }

    /**
	 * Show the presentation.
	 */
    public void activate(Composite parent) {

        if (active) {
			return;
		}

        parentWidget = parent;

        // Activate main layout
        // make sure all the views have been properly parented
        Vector children = new Vector();
        collectViewPanes(children, mainLayout.getChildren());
        Enumeration itr = children.elements();
        while (itr.hasMoreElements()) {
            LayoutPart part = (LayoutPart) itr.nextElement();
            part.reparent(parent);
        }
        mainLayout.createControl(parent);
        mainLayout.setActive(true);

        // Open the detached windows.
        for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
            DetachedWindow dwindow = (DetachedWindow) detachedWindowList.get(i);
            dwindow.open();
        }

        enableAllDrag();

        // Ensure that the maximized stack's presentation state is correct
        if (maximizedStackId != null) {
        	LayoutPart part = findPart(maximizedStackId);
        	if (part instanceof PartStack) {
        		maximizedStack = (PartStack) part;
            	maximizedStackId = null;
        	}
        }
        
        // NOTE: we only handle ViewStacks here; Editor Stacks are handled by the
        // perspective
        if (maximizedStack instanceof ViewStack) {
        	maximizedStack.setPresentationState(IStackPresentationSite.STATE_MAXIMIZED);            	
        }
        
        active = true;
    }

	/**
     * Adds a part to the presentation. If a placeholder exists for the part
     * then swap the part in. Otherwise, add the part in the bottom right
     * corner of the presentation.
     */
    public void addPart(LayoutPart part) {
        
    	// Look for a placeholder.
        PartPlaceholder placeholder = null;
        LayoutPart testPart = null;
        String primaryId = part.getID();
        String secondaryId = null;

        IViewReference ref = null;
        if (part instanceof ViewPane) {
            ViewPane pane = (ViewPane) part;
            ref = (IViewReference) pane.getPartReference();
            secondaryId = ref.getSecondaryId();
        }
        if (secondaryId != null) {
			testPart = findPart(primaryId, secondaryId);
		} else {
			testPart = findPart(primaryId);
		}

        // validate the testPart
        if (testPart != null && testPart instanceof PartPlaceholder) {
			placeholder = (PartPlaceholder) testPart;
		}

        // If there is no placeholder do a simple add. Otherwise, replace the
        // placeholder if its not a pattern matching placholder
        if (placeholder == null) {
            part.reparent(mainLayout.getParent());
            LayoutPart relative = mainLayout.findBottomRight();
            if (relative != null && relative instanceof ILayoutContainer) {
                ILayoutContainer stack = (ILayoutContainer)relative;
                if (stack.allowsAdd(part)) {
                    mainLayout.stack(part, stack);
                } else {
                    mainLayout.add(part);
                }
            } else {
                mainLayout.add(part);
            }
        } else {
            ILayoutContainer container = placeholder.getContainer();
            if (container != null) {
                if (container instanceof DetachedPlaceHolder) {
                    //Create a detached window add the part on it.
                    DetachedPlaceHolder holder = (DetachedPlaceHolder) container;
                    detachedPlaceHolderList.remove(holder);
                    container.remove(testPart);
                    DetachedWindow window = new DetachedWindow(page);
                    detachedWindowList.add(window);
                    window.create();
                    part.createControl(window.getShell());
                    // Open window.
                    window.getShell().setBounds(holder.getBounds());
                    window.open();
                    // add part to detached window.
                    ViewPane pane = (ViewPane) part;
                    window.add(pane);
                    LayoutPart otherChildren[] = holder.getChildren();
                    for (int i = 0; i < otherChildren.length; i++) {
						part.getContainer().add(otherChildren[i]);
					}
                } else {
                    // show parent if necessary
                    if (container instanceof ContainerPlaceholder) {
                        ContainerPlaceholder containerPlaceholder = (ContainerPlaceholder) container;                        
                        ILayoutContainer parentContainer = containerPlaceholder
                                .getContainer();
                        container = (ILayoutContainer) containerPlaceholder
                                .getRealContainer();
                        if (container instanceof LayoutPart) {
                            parentContainer.replace(containerPlaceholder,
                                    (LayoutPart) container);
                        }
                        containerPlaceholder.setRealContainer(null);
                    }

                    // reparent part.
                    if (!(container instanceof ViewStack)) {
                        // We don't need to reparent children of PartTabFolders since they will automatically
                        // reparent their children when they become visible. This if statement used to be 
                        // part of an else branch. Investigate if it is still necessary.
                        part.reparent(mainLayout.getParent());
                    }

                    // see if we should replace the placeholder
                    if (placeholder.hasWildCard()) {
                        if (container instanceof PartSashContainer) {
							((PartSashContainer) container)
                                    .addChildForPlaceholder(part, placeholder);
						} else {
							container.add(part);
						}
                    } else {
						container.replace(placeholder, part);
					}
                }
            }
        }
    }
    
    /**
     * Attaches a part that was previously detached to the mainLayout. 
     * 
     * @param ref
     */
    public void attachPart(IViewReference ref) {
		ViewPane pane = (ViewPane)((WorkbenchPartReference)ref).getPane();	
		
		// Restore any maximized part before re-attaching.
		// Note that 'getMaximizedStack != null' implies 'useNewMinMax'
		if (getMaximizedStack() != null) {
			getMaximizedStack().setState(IStackPresentationSite.STATE_RESTORED);
		}
		
		derefPart(pane);
		addPart(pane);
		bringPartToTop(pane);
		pane.setFocus();
    }

    /**
     * Return whether detachable parts can be supported.
     */
    public boolean canDetach() {
        return detachable;
    }

    /**
     * Bring a part forward so it is visible.
     * 
     * @return true if the part was brought to top, false if not.
     */
    public boolean bringPartToTop(LayoutPart part) {
        ILayoutContainer container = part.getContainer();
        if (container != null && container instanceof PartStack) {
            PartStack folder = (PartStack) container;
            if (folder.getSelection() != part) {
                folder.setSelection(part);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given part is visible.
     * A part is visible if it's top-level (not in a tab folder) or if it is the top one 
     * in a tab folder.
     */
    public boolean isPartVisible(IWorkbenchPartReference partRef) {
        LayoutPart foundPart;
        if (partRef instanceof IViewReference) {
			foundPart = findPart(partRef.getId(), ((IViewReference) partRef).getSecondaryId());
		} else {
			foundPart = findPart(partRef.getId());
		}
        if (foundPart == null) {
			return false;
		}
        if (foundPart instanceof PartPlaceholder) {
			return false;
		}

        ILayoutContainer container = foundPart.getContainer();
        
        if (container instanceof ContainerPlaceholder) {
			return false;
		}

        if (container instanceof ViewStack) {
            ViewStack folder = (ViewStack) container;
            PartPane visiblePart = folder.getSelection();
            if (visiblePart == null) {
				return false;
			}
            return partRef.equals(visiblePart.getPartReference());
        }
        return true;
    }

    /**
     * Returns true is not in a tab folder or if it is the top one in a tab
     * folder.
     */
    public boolean willPartBeVisible(String partId) {
        return willPartBeVisible(partId, null);
    }

    public boolean willPartBeVisible(String partId, String secondaryId) {
        LayoutPart part = findPart(partId, secondaryId);
        if (part == null) {
			return false;
		}
        ILayoutContainer container = part.getContainer();
        if (container != null && container instanceof ContainerPlaceholder) {
			container = (ILayoutContainer) ((ContainerPlaceholder) container)
                    .getRealContainer();
		}

        if (container != null && container instanceof ViewStack) {
            ViewStack folder = (ViewStack) container;
            if (folder.getSelection() == null) {
				return false;
			}
            return part.getCompoundId().equals(
                    folder.getSelection().getCompoundId());
        }
        return true;
    }

    /**
     * Answer a list of the PartPlaceholder objects.
     */
    private PartPlaceholder[] collectPlaceholders() {
        // Scan the main window.
        PartPlaceholder[] results = collectPlaceholders(mainLayout
                .getChildren());

        // Scan each detached window.
        if (detachable) {
            for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
                DetachedWindow win = (DetachedWindow) detachedWindowList.get(i);
                PartPlaceholder[] moreResults = collectPlaceholders(win
                        .getChildren());
                if (moreResults.length > 0) {
                    int newLength = results.length + moreResults.length;
                    PartPlaceholder[] newResults = new PartPlaceholder[newLength];
                    System.arraycopy(results, 0, newResults, 0, results.length);
                    System.arraycopy(moreResults, 0, newResults,
                            results.length, moreResults.length);
                    results = newResults;
                }
            }
        }
        return results;
    }

    /**
     * Answer a list of the PartPlaceholder objects.
     */
    private PartPlaceholder[] collectPlaceholders(LayoutPart[] parts) {
        PartPlaceholder[] result = new PartPlaceholder[0];

        for (int i = 0, length = parts.length; i < length; i++) {
            LayoutPart part = parts[i];
            if (part instanceof ILayoutContainer) {
                // iterate through sub containers to find sub-parts
                PartPlaceholder[] newParts = collectPlaceholders(((ILayoutContainer) part)
                        .getChildren());
                PartPlaceholder[] newResult = new PartPlaceholder[result.length
                        + newParts.length];
                System.arraycopy(result, 0, newResult, 0, result.length);
                System.arraycopy(newParts, 0, newResult, result.length,
                        newParts.length);
                result = newResult;
            } else if (part instanceof PartPlaceholder) {
                PartPlaceholder[] newResult = new PartPlaceholder[result.length + 1];
                System.arraycopy(result, 0, newResult, 0, result.length);
                newResult[result.length] = (PartPlaceholder) part;
                result = newResult;
            }
        }

        return result;
    }

    /**
     * Answer a list of the view panes.
     */
    public void collectViewPanes(List result) {
        // Scan the main window.
        collectViewPanes(result, mainLayout.getChildren());

        // Scan each detached window.
        if (detachable) {
            for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
                DetachedWindow win = (DetachedWindow) detachedWindowList.get(i);
                collectViewPanes(result, win.getChildren());
            }
        }
    }

    /**
     * Answer a list of the view panes.
     */
    private void collectViewPanes(List result, LayoutPart[] parts) {
        for (int i = 0, length = parts.length; i < length; i++) {
            LayoutPart part = parts[i];
            if (part instanceof ViewPane) {
                result.add(part);
            } else if (part instanceof ILayoutContainer) {
                collectViewPanes(result, ((ILayoutContainer) part)
                        .getChildren());
            }
        }
    }

    /**
     * Hide the presentation.
     */
    public void deactivate() {
        if (!active) {
			return;
		}

        disableAllDrag();

        // Reparent all views to the main window
        Composite parent = mainLayout.getParent();
        Vector children = new Vector();
        collectViewPanes(children, mainLayout.getChildren());

        for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
            DetachedWindow window = (DetachedWindow) detachedWindowList.get(i);
            collectViewPanes(children, window.getChildren());
        }

        // *** Do we even need to do this if detached windows not supported?
        Enumeration itr = children.elements();
        while (itr.hasMoreElements()) {
            LayoutPart part = (LayoutPart) itr.nextElement();
            part.reparent(parent);
        }
        
        // Dispose main layout.

        mainLayout.setActive(false);
        
        // Dispose the detached windows
        for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
            DetachedWindow window = (DetachedWindow) detachedWindowList.get(i);
            window.close();
        }
        
        active = false;
    }
    
    public void dispose() {
        mainLayout.dispose();
        mainLayout.disposeSashes();
    }
    
    /**
     * Writes a description of the layout to the given string buffer.
     * This is used for drag-drop test suites to determine if two layouts are the
     * same. Like a hash code, the description should compare as equal iff the
     * layouts are the same. However, it should be user-readable in order to
     * help debug failed tests. Although these are english readable strings,
     * they should not be translated or equality tests will fail.
     * <p>
     * This is only intended for use by test suites.
     * </p>
     * 
     * @param buf
     */
    public void describeLayout(StringBuffer buf) {

        if (detachable) {
            if(detachedWindowList.size() != 0){
                buf.append("detachedWindows ("); //$NON-NLS-1$
          
	            for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
	                DetachedWindow window = (DetachedWindow) detachedWindowList.get(i);
	                LayoutPart[] children = window.getChildren();
	                if(children.length != 0){
	                    buf.append("dWindow ("); //$NON-NLS-1$
	                    for(int j = 0; j < children.length; j++){
	                        buf.append(((ViewPane)children[j]).getViewReference().getPartName());
	                        if(j < (children.length - 1)) {
								buf.append(", "); //$NON-NLS-1$
							}
	                    }
	                    buf.append(")"); //$NON-NLS-1$
	                }
	                
	            }
	            buf.append("), "); //$NON-NLS-1$
            }
        }
            
        getLayout().describeLayout(buf);
    }

    /**
     * Deref a given part. Deconstruct its container as required. Do not remove
     * drag listeners.
     */
    /* package */void derefPart(LayoutPart part) {
        if (part instanceof ViewPane) {
        	IViewReference ref = ((ViewPane) part).getViewReference();
        	if (perspective.isFastView(ref)) {
        		// Special check: if it's a fast view then it's actual ViewStack
        		// may only contain placeholders and the stack is represented in
        		// the presentation by a container placeholder...make sure the
        		// PartPlaceHolder for 'ref' is removed from the ViewStack
        		String id = perspective.getFastViewManager().getIdForRef(ref);
        		LayoutPart parentPart = findPart(id, null);
        		if (parentPart instanceof ContainerPlaceholder) {
        			ViewStack vs = (ViewStack) ((ContainerPlaceholder)parentPart).getRealContainer();
        			LayoutPart[] kids = vs.getChildren();
        			for (int i = 0; i < kids.length; i++) {
						if (kids[i] instanceof PartPlaceholder) {
							if (ref.getId().equals(kids[i].id))
								vs.remove(kids[i]);
						}
					}
        		}
        		perspective.getFastViewManager().removeViewReference(ref, true, true);
        	}
        }

        // Get vital part stats before reparenting.
        ILayoutContainer oldContainer = part.getContainer();
        boolean wasDocked = part.isDocked();
        Shell oldShell = part.getShell();

        // Reparent the part back to the main window
        part.reparent(mainLayout.getParent());

        // Update container.
        if (oldContainer == null) {
			return;
		}

        oldContainer.remove(part);

        LayoutPart[] children = oldContainer.getChildren();
        if (wasDocked) {
            boolean hasChildren = (children != null) && (children.length > 0);
            if (hasChildren) {
                // make sure one is at least visible
                int childVisible = 0;
                for (int i = 0; i < children.length; i++) {
					if (children[i].getControl() != null) {
						childVisible++;
					}
				}

                // none visible, then reprarent and remove container
                if (oldContainer instanceof ViewStack) {
                    ViewStack folder = (ViewStack) oldContainer;
                    
                    // Is the part in the trim?
                    boolean inTrim = false;
                    // Safety check...there may be no FastViewManager
                    if (perspective.getFastViewManager() != null)
                    	inTrim = perspective.getFastViewManager().getFastViews(folder.getID()).size() > 0;
                    	
                    if (childVisible == 0 && !inTrim) {
                        ILayoutContainer parentContainer = folder
                                .getContainer();
                        for (int i = 0; i < children.length; i++) {
                            folder.remove(children[i]);
                            parentContainer.add(children[i]);
                        }
                        hasChildren = false;
                    } else if (childVisible == 1) {
                        LayoutTree layout = mainLayout.getLayoutTree();
                        layout = layout.find(folder);
                        layout.setBounds(layout.getBounds());
                    }
                }
            }

            if (!hasChildren) {
                // There are no more children in this container, so get rid of
                // it
                if (oldContainer instanceof LayoutPart) {
                    LayoutPart parent = (LayoutPart) oldContainer;
                    ILayoutContainer parentContainer = parent.getContainer();
                    if (parentContainer != null) {
                        parentContainer.remove(parent);
                        parent.dispose();
                    }
                }
            }
        } else if (!wasDocked) {
            if (children == null || children.length == 0) {
                // There are no more children in this container, so get rid of
                // it
                // Turn on redraw again just in case it was off.
                //oldShell.setRedraw(true);
                DetachedWindow w = (DetachedWindow)oldShell.getData();
                oldShell.close();
                detachedWindowList.remove(w);
            } else {
                // There are children. If none are visible hide detached
                // window.
                boolean allInvisible = true;
                for (int i = 0, length = children.length; i < length; i++) {
                    if (!(children[i] instanceof PartPlaceholder)) {
                        allInvisible = false;
                        break;
                    }
                }
                if (allInvisible) {
                    DetachedPlaceHolder placeholder = new DetachedPlaceHolder(
                            "", //$NON-NLS-1$
                            oldShell.getBounds());
                    for (int i = 0, length = children.length; i < length; i++) {
                        oldContainer.remove(children[i]);
                        children[i].setContainer(placeholder);
                        placeholder.add(children[i]);
                    }
                    detachedPlaceHolderList.add(placeholder);
                    DetachedWindow w = (DetachedWindow)oldShell.getData();
                    oldShell.close();
                    detachedWindowList.remove(w);
                }
            }
        }

    }

    /**
     * Create a detached window containing a part.
     */
    private void detach(LayoutPart source, int x, int y) {

        // Detaching is disabled on some platforms ..
        if (!detachable) {
			return;
		}

        LayoutPart part = source.getPart();
        // Calculate detached window size.
        Point size = part.getSize();
        if (size.x == 0 || size.y == 0) {
            ILayoutContainer container = part.getContainer();
            if (container instanceof LayoutPart) {
                size = ((LayoutPart) container).getSize();
            }
        }
        int width = Math.max(size.x, MIN_DETACH_WIDTH);
        int height = Math.max(size.y, MIN_DETACH_HEIGHT);

        // Create detached window.
        DetachedWindow window = new DetachedWindow(page);
        detachedWindowList.add(window);

        // Open window.
        window.create();
        window.getShell().setBounds(x, y, width, height);
        window.open();

        if (part instanceof ViewStack) {
            window.getShell().setRedraw(false);
            parentWidget.setRedraw(false);
            LayoutPart visiblePart = ((ViewStack) part).getSelection();
            LayoutPart children[] = ((ViewStack) part).getChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof ViewPane) {
                    // remove the part from its current container
                    derefPart(children[i]);
                    // add part to detached window.
                    ViewPane pane = (ViewPane) children[i];
                    window.add(pane);
                }
            }
            if (visiblePart != null) {
                bringPartToTop(visiblePart);
                visiblePart.setFocus();
            }
            window.getShell().setRedraw(true);
            parentWidget.setRedraw(true);
        } else {
            // remove the part from its current container
            derefPart(part);
            // add part to detached window.
            ViewPane pane = (ViewPane) part;
            window.add(pane);
            part.setFocus();
        }

    }
    
    /**
     * Detached a part from the mainLayout. Presently this does not use placeholders
     * since the current implementation is not robust enough to remember a view's position
     * in more than one root container. For now the view is simply derefed and will dock
     * in the default position when attachPart is called. 
     * 
     * By default parts detached this way are set to float on top of the workbench
     * without docking. It is assumed that people that want to drag a part back onto
     * the WorkbenchWindow will detach it via drag and drop. 
     * 
     * @param ref
     */
    public void detachPart(IViewReference ref) {
		ViewPane pane = (ViewPane)((WorkbenchPartReference)ref).getPane();
    	if (canDetach() && pane != null) {
    		if (getMaximizedStack() != null)
    			getMaximizedStack().setState(IStackPresentationSite.STATE_RESTORED);
    		
    		Rectangle bounds = pane.getParentBounds();
    	    detach(pane, bounds.x ,bounds.y);
    	}
    }

    /**
     * Create a detached window containing a part.
     */
    public void addDetachedPart(LayoutPart part) {
        // Calculate detached window size.
        Rectangle bounds = parentWidget.getShell().getBounds();
        bounds.x = bounds.x + (bounds.width - 300) / 2;
        bounds.y = bounds.y + (bounds.height - 300) / 2;
        
        addDetachedPart(part, bounds);
    }
    
    public void addDetachedPart(LayoutPart part, Rectangle bounds) {
        // Detaching is disabled on some platforms ..
        if (!detachable) {
            addPart(part);
            return;
        }
        
        // Create detached window.
        DetachedWindow window = new DetachedWindow(page);
        detachedWindowList.add(window);
        window.create();

        // add part to detached window.
        part.createControl(window.getShell());
        ViewPane pane = (ViewPane) part;
        window.add(pane);

        // Open window.
        window.getShell().setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        window.open();

        part.setFocus();
        
    }

    /**
     * disableDragging.
     */
    private void disableAllDrag() {
        DragUtil.removeDragTarget(null, dragTarget);
    }

    /**
     * enableDragging.
     */
    private void enableAllDrag() {
        DragUtil.addDragTarget(null, dragTarget);
    }

    /**
     * Find the first part with a given ID in the presentation.
     * Wild cards now supported.
     */
    private LayoutPart findPart(String id) {
        return findPart(id, null);
    }

    /**
     * Find the first part that matches the specified 
     * primary and secondary id pair.  Wild cards
     * are supported.
     */
    public LayoutPart findPart(String primaryId, String secondaryId) {
        // check main window.
        ArrayList matchingParts = new ArrayList();
        LayoutPart part = (secondaryId != null) ? findPart(primaryId,
                secondaryId, mainLayout.getChildren(), matchingParts)
                : findPart(primaryId, mainLayout.getChildren(), matchingParts);
        if (part != null) {
			return part;
		}

        // check each detached windows.
        for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
            DetachedWindow window = (DetachedWindow) detachedWindowList.get(i);
            part = (secondaryId != null) ? findPart(primaryId, secondaryId,
                    window.getChildren(), matchingParts) : findPart(primaryId,
                    window.getChildren(), matchingParts);
            if (part != null) {
				return part;
			}
        }
        for (int i = 0; i < detachedPlaceHolderList.size(); i++) {
            DetachedPlaceHolder holder = (DetachedPlaceHolder) detachedPlaceHolderList
                    .get(i);
            part = (secondaryId != null) ? findPart(primaryId, secondaryId,
                    holder.getChildren(), matchingParts) : findPart(primaryId,
                    holder.getChildren(), matchingParts);
            if (part != null) {
				return part;
			}
        }

        // sort the matching parts
        if (matchingParts.size() > 0) {
            Collections.sort(matchingParts);
            MatchingPart mostSignificantPart = (MatchingPart) matchingParts
                    .get(0);
            if (mostSignificantPart != null) {
				return mostSignificantPart.part;
			}
        }

        // Not found.
        return null;
    }

    /**
     * Find the first part with a given ID in the presentation.
     */
    private LayoutPart findPart(String id, LayoutPart[] parts,
            ArrayList matchingParts) {
        for (int i = 0, length = parts.length; i < length; i++) {
            LayoutPart part = parts[i];
            // check for part equality, parts with secondary ids fail
            if (part.getID().equals(id)) {
                if (part instanceof ViewPane) {
                    ViewPane pane = (ViewPane) part;
                    IViewReference ref = (IViewReference) pane
                            .getPartReference();
                    if (ref.getSecondaryId() != null) {
						continue;
					}
                }
                return part;
            }
            // check pattern matching placeholders
            else if (part instanceof PartPlaceholder
                    && ((PartPlaceholder) part).hasWildCard()) {
                StringMatcher sm = new StringMatcher(part.getID(), true, false);
                if (sm.match(id)) {
					matchingParts
                            .add(new MatchingPart(part.getID(), null, part));
				}
            } else if (part instanceof EditorSashContainer) {
                // Skip.
            } else if (part instanceof ILayoutContainer) {
                part = findPart(id, ((ILayoutContainer) part).getChildren(),
                        matchingParts);
                if (part != null) {
					return part;
				}
            }
        }
        return null;
    }

    /**
     * Find the first part that matches the specified 
     * primary and secondary id pair.  Wild cards
     * are supported.
     */
    private LayoutPart findPart(String primaryId, String secondaryId,
            LayoutPart[] parts, ArrayList matchingParts) {
        for (int i = 0, length = parts.length; i < length; i++) {
            LayoutPart part = parts[i];
            // check containers first
            if (part instanceof ILayoutContainer) {
                LayoutPart testPart = findPart(primaryId, secondaryId,
                        ((ILayoutContainer) part).getChildren(), matchingParts);
                if (testPart != null) {
					return testPart;
				}
            }
            // check for view part equality
            if (part instanceof ViewPane) {
                ViewPane pane = (ViewPane) part;
                IViewReference ref = (IViewReference) pane.getPartReference();
                if (ref.getId().equals(primaryId)
                        && ref.getSecondaryId() != null
                        && ref.getSecondaryId().equals(secondaryId)) {
					return part;
				}
            }
            // check placeholders
            else if ((parts[i] instanceof PartPlaceholder)) {
                String id = part.getID();

                // optimization: don't bother parsing id if it has no separator -- it can't match
                String phSecondaryId = ViewFactory.extractSecondaryId(id);
                if (phSecondaryId == null) {
                    // but still need to check for wildcard case
                    if (id.equals(PartPlaceholder.WILD_CARD)) {
						matchingParts.add(new MatchingPart(id, null, part));
					}
                    continue;
                }

                String phPrimaryId = ViewFactory.extractPrimaryId(id);
                // perfect matching pair
                if (phPrimaryId.equals(primaryId)
                        && phSecondaryId.equals(secondaryId)) {
                    return part;
                }
                // check for partial matching pair
                StringMatcher sm = new StringMatcher(phPrimaryId, true, false);
                if (sm.match(primaryId)) {
                    sm = new StringMatcher(phSecondaryId, true, false);
                    if (sm.match(secondaryId)) {
                        matchingParts.add(new MatchingPart(phPrimaryId,
                                phSecondaryId, part));
                    }
                }
            } else if (part instanceof EditorSashContainer) {
                // Skip.
            }
        }
        return null;
    }

    /**
     * Returns true if a placeholder exists for a given ID.
     */
    public boolean hasPlaceholder(String id) {
        return hasPlaceholder(id, null);
    }

    /**
     * Returns true if a placeholder exists for a given ID.
     * @since 3.0
     */
    public boolean hasPlaceholder(String primaryId, String secondaryId) {
        LayoutPart testPart;
        if (secondaryId == null) {
			testPart = findPart(primaryId);
		} else {
			testPart = findPart(primaryId, secondaryId);
		}
        return (testPart != null && testPart instanceof PartPlaceholder);
    }

    /**
     * Returns the layout container.
     */
    public ViewSashContainer getLayout() {
        return mainLayout;
    }

    /**
     * Gets the active state.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns whether the presentation is zoomed.
     * 
     * <strong>NOTE:</strong> As of 3.3 this method should always return 'false'
     * when using the new min/max behavior. It is only used for
     * legacy 'zoom' handling.
     */
    public boolean isZoomed() {
        return mainLayout.getZoomedPart() != null;
    }

    /**
	 * @return The currently maxmized stack (if any)
	 */
	public PartStack getMaximizedStack() {
		return maximizedStack;
	}

	/**
	 * Sets the currently maximized stack. Used for query
	 * and 'unZoom' purposes in the 3.3 presentation.
	 * 
	 * @param stack The newly maximized stack
	 */
	public void setMaximizedStack(PartStack stack) {
		if (stack == maximizedStack)
			return;
		
		maximizedStack = stack;
	}
	
	/**
     * Returns the ratio that should be used when docking the given source
     * part onto the given target
     * 
     * @param source newly added part
     * @param target existing part being dragged over
     * @return the final size of the source part (wrt the current size of target)
     * after it is docked
     */
    public static float getDockingRatio(LayoutPart source, LayoutPart target) {
        if ((source instanceof ViewPane || source instanceof ViewStack)
                && target instanceof EditorSashContainer) {
            return 0.25f;
        }
        return 0.5f;
    }

    /**
     * Returns whether changes to a part will affect zoom. There are a few
     * conditions for this .. - we are zoomed. - the part is contained in the
     * main window. - the part is not the zoom part - the part is not a fast
     * view - the part and the zoom part are not in the same editor workbook
	 * - the part and the zoom part are not in the same view stack.
     */
    public boolean partChangeAffectsZoom(LayoutPart pane) {
        return pane.isObscuredByZoom();
    }
    
    /**
     * Remove all references to a part.
     */
    public void removePart(LayoutPart part) {

        // Reparent the part back to the main window
        Composite parent = mainLayout.getParent();
        part.reparent(parent);

        // Replace part with a placeholder
        ILayoutContainer container = part.getContainer();
        if (container != null) {
            String placeHolderId = part.getPlaceHolderId();
            container.replace(part, new PartPlaceholder(placeHolderId));

            // If the parent is root we're done. Do not try to replace
            // it with placeholder.
            if (container == mainLayout) {
				return;
			}

            // If the parent is empty replace it with a placeholder.
            LayoutPart[] children = container.getChildren();
            if (children != null) {
                boolean allInvisible = true;
                for (int i = 0, length = children.length; i < length; i++) {
                    if (!(children[i] instanceof PartPlaceholder)) {
                        allInvisible = false;
                        break;
                    }
                }
                if (allInvisible && (container instanceof LayoutPart)) {
                    // what type of window are we in?
                    LayoutPart cPart = (LayoutPart) container;
                    //Window oldWindow = cPart.getWindow();
                    boolean wasDocked = cPart.isDocked();
                    Shell oldShell = cPart.getShell();
                    if (wasDocked) {
                        
                        // PR 1GDFVBY: ViewStack not disposed when page
                        // closed.
                        if (container instanceof ViewStack) {
							((ViewStack) container).dispose();
						}
                        
                        // replace the real container with a
                        // ContainerPlaceholder
                        ILayoutContainer parentContainer = cPart.getContainer();
                        ContainerPlaceholder placeholder = new ContainerPlaceholder(
                                cPart.getID());
                        placeholder.setRealContainer(container);
                        parentContainer.replace(cPart, placeholder);
                        
                    } else {
                        DetachedPlaceHolder placeholder = new DetachedPlaceHolder(
                                "", oldShell.getBounds()); //$NON-NLS-1$
                        for (int i = 0, length = children.length; i < length; i++) {
                            children[i].getContainer().remove(children[i]);
                            children[i].setContainer(placeholder);
                            placeholder.add(children[i]);
                        }
                        detachedPlaceHolderList.add(placeholder);
                        DetachedWindow w = (DetachedWindow)oldShell.getData();
                        oldShell.close();
                        detachedWindowList.remove(w);
                    }
                }
            }
        }
    }

    /**
     * Add a part to the presentation.
     * 
     * Note: unlike all other LayoutParts, PartPlaceholders will still point to
     * their parent container even when it is inactive. This method relies on this
     * fact to locate the parent.
     */
    public void replacePlaceholderWithPart(LayoutPart part) {
        
        // Look for a PartPlaceholder that will tell us how to position this
        // object
        PartPlaceholder[] placeholders = collectPlaceholders();
        for (int i = 0, length = placeholders.length; i < length; i++) {
            if (placeholders[i].getCompoundId().equals(part.getCompoundId())) {
                // found a matching placeholder which we can replace with the
                // new View
                ILayoutContainer container = placeholders[i].getContainer();
                if (container != null) {
                    if (container instanceof ContainerPlaceholder) {
                        // One of the children is now visible so replace the
                        // ContainerPlaceholder with the real container
                        ContainerPlaceholder containerPlaceholder = (ContainerPlaceholder) container;
                        ILayoutContainer parentContainer = containerPlaceholder
                                .getContainer();
                        container = (ILayoutContainer) containerPlaceholder
                                .getRealContainer();
                        if (container instanceof LayoutPart) {
                            parentContainer.replace(containerPlaceholder,
                                    (LayoutPart) container);
                        }
                        containerPlaceholder.setRealContainer(null);
                    }
                    container.replace(placeholders[i], part);
                    return;
                }
            }
        }

    }

    /**
     * @see org.eclipse.ui.IPersistable
     */
    public IStatus restoreState(IMemento memento) {
        // Restore main window.
        IMemento childMem = memento
                .getChild(IWorkbenchConstants.TAG_MAIN_WINDOW);
        IStatus r = mainLayout.restoreState(childMem);

        // Restore each floating window.
        if (detachable) {
            IMemento detachedWindows[] = memento
                    .getChildren(IWorkbenchConstants.TAG_DETACHED_WINDOW);
            for (int nX = 0; nX < detachedWindows.length; nX++) {
                DetachedWindow win = new DetachedWindow(page);
                detachedWindowList.add(win);
                win.restoreState(detachedWindows[nX]);
            }
            IMemento childrenMem[] = memento
                    .getChildren(IWorkbenchConstants.TAG_HIDDEN_WINDOW);
            for (int i = 0, length = childrenMem.length; i < length; i++) {
                DetachedPlaceHolder holder = new DetachedPlaceHolder(
                        "", new Rectangle(0, 0, 0, 0)); //$NON-NLS-1$
                holder.restoreState(childrenMem[i]);
                detachedPlaceHolderList.add(holder);
            }
        }
        
        // Get the cached id of the currently maximized stack
        maximizedStackId = childMem.getString(IWorkbenchConstants.TAG_MAXIMIZED);
        
        return r;
    }

    /**
     * @see org.eclipse.ui.IPersistable
     */
    public IStatus saveState(IMemento memento) {
        // Persist main window.
        IMemento childMem = memento
                .createChild(IWorkbenchConstants.TAG_MAIN_WINDOW);
        IStatus r = mainLayout.saveState(childMem);

        if (detachable) {
            // Persist each detached window.
            for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
                DetachedWindow window = (DetachedWindow) detachedWindowList
                        .get(i);
                childMem = memento
                        .createChild(IWorkbenchConstants.TAG_DETACHED_WINDOW);
                window.saveState(childMem);
            }
            for (int i = 0, length = detachedPlaceHolderList.size(); i < length; i++) {
                DetachedPlaceHolder holder = (DetachedPlaceHolder) detachedPlaceHolderList
                        .get(i);
                childMem = memento
                        .createChild(IWorkbenchConstants.TAG_HIDDEN_WINDOW);
                holder.saveState(childMem);
            }
        }
        
        // Write out the id of the maximized (View) stack (if any)
        // NOTE: we only write this out if it's a ViewStack since the
        // Editor Area is handled by the perspective
        if (maximizedStack instanceof ViewStack) {
        	childMem.putString(IWorkbenchConstants.TAG_MAXIMIZED, maximizedStack.getID());
        }
        else if (maximizedStackId != null) {
        	// Maintain the cache if the perspective has never been activated
        	childMem.putString(IWorkbenchConstants.TAG_MAXIMIZED, maximizedStackId);        	
        }
        
        return r;
    }

    /**
     * Zoom in on a particular layout part.
     */
    public void zoomIn(IWorkbenchPartReference ref) {
        PartPane pane = ((WorkbenchPartReference) ref).getPane();


        parentWidget.setRedraw(false);
        try {
            pane.requestZoomIn();
        } finally {
            parentWidget.setRedraw(true);
        }
    }

    /**
     * Zoom out.
     */
    public void zoomOut() {
    	// New 3.3 behavior
		if (Perspective.useNewMinMax(perspective)) {
			 if (maximizedStack != null)
				 maximizedStack.setState(IStackPresentationSite.STATE_RESTORED);
			 return;
		}
		
        LayoutPart zoomPart = mainLayout.getZoomedPart();
        if (zoomPart != null) {
            zoomPart.requestZoomOut();
        }
    }

    /**
     * Forces the perspective to have no zoomed or minimized parts.
     * This is used when switching to the 3.3 presentation...
     */
    public void forceNoZoom() {
    	// Ensure that nobody's zoomed
    	zoomOut();
    	
    	// Now, walk the layout ensuring that nothing is minimized
    	LayoutPart[] kids = mainLayout.getChildren();
    	for (int i = 0; i < kids.length; i++) {
			if (kids[i] instanceof ViewStack) {
				((ViewStack)kids[i]).setMinimized(false);
			}
			else if (kids[i] instanceof EditorSashContainer) {
				LayoutPart[] editorStacks = ((EditorSashContainer)kids[i]).getChildren();
				for (int j = 0; j < editorStacks.length; j++) {
					if (editorStacks[j] instanceof EditorStack) {
						((EditorStack)editorStacks[j]).setMinimized(false);
					}
				}
			}
		}
    }

    /**
     * Captures the current bounds of all ViewStacks and the editor
     * area and puts them into an ID -> Rectangle map. This info is
     * used to cache the bounds so that we can correctly place minimized
     * stacks during a 'maximized' operation (where the iterative min's
     * affect the current layout while being performed.
     */
    public void updateBoundsMap() {
    	boundsMap.clear();
    	
    	// Walk the layout gathering the current bounds of each stack
    	// and the editor area
    	LayoutPart[] kids = mainLayout.getChildren();
    	for (int i = 0; i < kids.length; i++) {
			if (kids[i] instanceof ViewStack) {
				ViewStack vs = (ViewStack)kids[i];
				boundsMap.put(vs.getID(), vs.getBounds());
			}
			else if (kids[i] instanceof EditorSashContainer) {
				EditorSashContainer esc = (EditorSashContainer)kids[i];
				boundsMap.put(esc.getID(), esc.getBounds());
			}
		}
    }

	/**
	 * Resets the bounds map so that it won't interfere with normal minimize
	 * operayions
	 */
	public void resetBoundsMap() {
		boundsMap.clear();
	}
	
	public Rectangle getCachedBoundsFor(String id) {
		return (Rectangle) boundsMap.get(id);
	}
}
