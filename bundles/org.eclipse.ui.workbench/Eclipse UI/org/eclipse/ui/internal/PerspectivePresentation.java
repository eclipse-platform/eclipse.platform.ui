/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

import java.util.*;
import java.util.List;

/**
 * A perspective presentation is a collection of parts with a layout.
 * Each part is parented to a main window, so you can create more
 * than one presentation on a set of parts and change the layout
 * just by activating / deactivating a presentation.
 *
 * In addition, the user can change the position of any part by mouse
 * manipulation (drag & drop). If a part is removed, we leave a
 * placeholder behind to indicate where it goes should the part be 
 * added back.
 */
public class PerspectivePresentation {
	private WorkbenchPage page;
	private Composite parentWidget;
	private RootLayoutContainer mainLayout;
	private IWorkbenchPartReference zoomPart;
	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * private ArrayList detachedWindowList = new ArrayList(1);
	 * private ArrayList detachedPlaceHolderList = new ArrayList(1);
	 * private boolean detachable = false;
	 */
	private boolean active = false;
	// Flag used during drag and drop to specify if a fast
	// view was active when the drag started.
	private boolean wasFastViewActive = false;
	
	private Map dragParts = new HashMap();  // key is the LayoutPart object, value is the PartDragDrop object
	private IPartDropListener partDropListener;

/**
 * Constructs a new object.
 */
public PerspectivePresentation(WorkbenchPage workbenchPage, RootLayoutContainer mainLayout) {
	this.page = workbenchPage;
	this.mainLayout = mainLayout;

	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * // Should ask the actual control
	 * this.detachable = workbenchPage.getWorkbenchWindow().getShell().isReparentable();
	 */
	
	this.partDropListener = new IPartDropListener() {
		public void dragOver(PartDropEvent e) {
			onPartDragOver(e);
		};
		public void drop(PartDropEvent e){
			onPartDrop(e);
		};
	};
}
/**
 * Show the presentation.
 */
public void activate(Composite parent) {

	if (active)
		return;

	parentWidget = parent;
		
	// Activate main layout
	// make sure all the views have been properly parented
	Vector children = new Vector();
	collectViewPanes(children, mainLayout.getChildren());
	Enumeration enum = children.elements();
	while (enum.hasMoreElements()) {
		LayoutPart part = (LayoutPart)enum.nextElement();
		part.reparent(parent);
	}
	mainLayout.createControl(parent);

	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * // Open the detached windows.
	 * for (int i = 0, length = detachedWindowList.size(); i < length; i++){
	 * 	DetachedWindow dwindow = (DetachedWindow)detachedWindowList.get(i);
	 * 	dwindow.open();
	 * }
	 */

	enableAllDrag();
	enableAllDrop();

	active = true;
}
/**
 * Adds a part to the presentation.  If a placeholder exists for the part then
 * swap the part in. Otherwise, add the part in the bottom right corner
 * of the presentation.
 */
public void addPart(LayoutPart part) {
	// If part added / removed always zoom out.
	if (isZoomed())
		zoomOut();
		
	// Look for a placeholder.
	PartPlaceholder placeholder = null;
	LayoutPart testPart = findPart(part.getID());
	if (testPart != null && testPart instanceof PartPlaceholder)
		placeholder = (PartPlaceholder)testPart;

	// If there is no placeholder do a simple add. Otherwise, replace the placeholder.
	if (placeholder == null) {
		/*
		 * Detached window no longer supported - remove when confirmed
		 * 
		 * part.reparent(mainLayout.getParent());
		 */
		LayoutPart relative = mainLayout.findBottomRight();
		if(relative != null && !(relative instanceof EditorArea)) {
			stack(part,relative);
		} else {
			mainLayout.add(part);
		}
	} else {
		ILayoutContainer container = placeholder.getContainer();
		if (container != null) {
			/*
			 * Detached window no longer supported - remove when confirmed
			 * 
			 * if (container instanceof DetachedPlaceHolder) {
			 * 	//Create a detached window add the part on it.
			 * 	DetachedPlaceHolder holder = (DetachedPlaceHolder)container;
			 * 	detachedPlaceHolderList.remove(holder);
			 * 	container.remove(testPart);
			 * 	DetachedWindow window = new DetachedWindow(page);
			 * 	detachedWindowList.add(window);
			 * 	window.create();
			 * 	part.createControl(window.getShell());
			 * 	// Open window.
			 * 	window.getShell().setBounds(holder.getBounds());
			 * 	window.open();
			 * 	// add part to detached window.
			 * 	ViewPane pane = (ViewPane) part;
			 * 	window.getShell().setText(pane.getPart().getTitle());
			 * 	window.add(pane, partDropListener);
			 * 	LayoutPart otherChildren[] = holder.getChildren();
			 * 	for (int i = 0; i < otherChildren.length; i++)
			 * 		part.getContainer().add(otherChildren[i]);
			 * } else {
			 */
				// reconsistute parent if necessary
				if (container instanceof ContainerPlaceholder) {
					ContainerPlaceholder containerPlaceholder = (ContainerPlaceholder)container;
					ILayoutContainer parentContainer = containerPlaceholder.getContainer();
					container = (ILayoutContainer)containerPlaceholder.getRealContainer();
					if (container instanceof LayoutPart) {
						parentContainer.replace(containerPlaceholder, (LayoutPart)container);
					}
					containerPlaceholder.setRealContainer(null);
				}
				
				// reparent part.
				if (container instanceof PartTabFolder) {
					PartTabFolder folder = (PartTabFolder)container;
					part.reparent(folder.getControl().getParent());
				} else {
					part.reparent(mainLayout.getParent());
				}
				
				// replace placeholder with real part
				container.replace(placeholder, part);
			/*
			 * Detached window no longer supported - remove when confirmed
			 * 
			 * }
			 */
		}
	}	
	
	// enable direct manipulation
	if (part instanceof ViewPane)
		enableDrag((ViewPane)part);
	enableDrop(part);
}
/**
 * Return whether detachable parts can be supported.
 */
/*
 * Detached window no longer supported - remove when confirmed
 *
 * public boolean canDetach() {
 * 	return detachable;
 * }
 */
 
/**
 * Bring a part forward so it is visible.
 *
 * @return true if the part was brought to top, false if not.
 */
public boolean bringPartToTop(LayoutPart part) {
	ILayoutContainer container = part.getContainer();
	if (container != null && container instanceof PartTabFolder) {
		PartTabFolder folder = (PartTabFolder)container;
		int nIndex = folder.indexOf(part);
		if (folder.getSelection() != nIndex) {
			folder.setSelection(nIndex);
			return true;
		}
	}
	return false;
}
/**
 * Returns true is not in a tab folder or if it is the top one in
 * a tab folder.
 */
public boolean isPartVisible(String partId) {
	LayoutPart part = findPart(partId);
	if(part == null)
		return false;
	if(part instanceof PartPlaceholder)
		return false;
		
	ILayoutContainer container = part.getContainer();
	if (container != null && container instanceof ContainerPlaceholder)
		return false;
			
	if (container != null && container instanceof PartTabFolder) {
		PartTabFolder folder = (PartTabFolder)container;
		if(folder.getVisiblePart() == null)
			return false;
		return part.getID().equals(folder.getVisiblePart().getID());
	}
	return true;
}
/**
 * Returns true is not in a tab folder or if it is the top one in
 * a tab folder.
 */
public boolean willPartBeVisible(String partId) {
	LayoutPart part = findPart(partId);
	if(part == null)
		return false;
	ILayoutContainer container = part.getContainer();
	if (container != null && container instanceof ContainerPlaceholder)
		container = (ILayoutContainer)((ContainerPlaceholder)container).getRealContainer();
			
	if (container != null && container instanceof PartTabFolder) {
		PartTabFolder folder = (PartTabFolder)container;
		if(folder.getVisiblePart() == null)
			return false;
		return part.getID().equals(folder.getVisiblePart().getID());
	}
	return true;
}

/**
 * Answer a list of the view panes.
 */
/*
 * Detached window no longer supported - remove when confirmed
 *
 * private DetachedWindow[] collectDetachedWindows(Window[] windows) {
 * 	DetachedWindow[] result = new DetachedWindow[0];
 * 	for (int i = 0, length = windows.length; i < length; i++) {
 * 		if (windows[i] instanceof DetachedWindow && ((DetachedWindow)windows[i]).belongsToWorkbenchPage(page)) {
 * 			DetachedWindow[] newResult = new DetachedWindow[result.length + 1];
 * 			System.arraycopy(result, 0, newResult, 0, result.length);
 * 			newResult[result.length] = (DetachedWindow)windows[i];
 * 			result = newResult;
 * 		}
 * 	}
 * 	return result;
 * }
 */

/**
 * Open the tracker to allow the user to move
 * the specified part using keyboard.
 */
public void openTracker(ViewPane pane) {
	PartDragDrop dnd = (PartDragDrop)dragParts.get(pane);
	dnd.openTracker();
}
/**
 * Answer a list of the view panes.
 */
private void collectDragParts(List result, LayoutPart [] parts) {
	for (int i = 0, length = parts.length; i < length; i++) {
		LayoutPart part = parts[i];
		if (part instanceof ViewPane) {
			result.add(part);
		} else if (part instanceof ILayoutContainer) {
			collectDragParts(result, ((ILayoutContainer)part).getChildren());
		}
	}
}
/**
 * Answer a list of the view panes.
 */
private void collectDropTargets(List result, LayoutPart [] parts) {
	for (int i = 0, length = parts.length; i < length; i++) {
		LayoutPart part = parts[i];
		if (part instanceof EditorWorkbook) {
			result.add(part);
		} else if (part instanceof ViewPane) {
			result.add(part);
		} else if (part instanceof ILayoutContainer) {
			collectDropTargets(result, ((ILayoutContainer)part).getChildren());
		}
	}
}
/**
 * Answer a list of the PartPlaceholder objects.
 */
private PartPlaceholder[] collectPlaceholders() {
	// Scan the main window.
	PartPlaceholder[] results = collectPlaceholders(mainLayout.getChildren());

	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * // Scan each detached window.
	 * if (detachable) {
	 * 	for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
	 * 		DetachedWindow win = (DetachedWindow)detachedWindowList.get(i);
	 * 		PartPlaceholder [] moreResults = collectPlaceholders(win.getChildren());
	 * 		if (moreResults.length > 0) {
	 * 			int newLength = results.length + moreResults.length;
	 * 			PartPlaceholder [] newResults = new PartPlaceholder[newLength];
	 * 			System.arraycopy(results, 0, newResults, 0, results.length);
	 * 			System.arraycopy(moreResults, 0, newResults, results.length, moreResults.length);
	 * 			results = newResults;
	 * 		}
	 * 	}		
	 * }
	 */
	 
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
			PartPlaceholder[] newParts = collectPlaceholders(((ILayoutContainer)part).getChildren());
			PartPlaceholder[] newResult = new PartPlaceholder[result.length + newParts.length];
			System.arraycopy(result, 0, newResult, 0, result.length);
			System.arraycopy(newParts, 0, newResult, result.length, newParts.length);
			result = newResult;
		} else if (part instanceof PartPlaceholder) {
			PartPlaceholder[] newResult = new PartPlaceholder[result.length+1];
			System.arraycopy(result, 0, newResult, 0, result.length);
			newResult[result.length] = (PartPlaceholder)part;
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

	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * // Scan each detached window.
	 * if (detachable) {
	 * 	for (int i = 0, length = detachedWindowList.size(); i < length; i++) {
	 * 		DetachedWindow win = (DetachedWindow)detachedWindowList.get(i);
	 * 		collectViewPanes(result, win.getChildren());
	 * 	}		
	 * }
	 */
}
/**
 * Answer a list of the view panes.
 */
private void collectViewPanes(List result, LayoutPart [] parts) {
	for (int i = 0, length = parts.length; i < length; i++) {
		LayoutPart part = parts[i];
		if (part instanceof ViewPane) {
			result.add(part);
		} else if (part instanceof ILayoutContainer) {
			collectViewPanes(result, ((ILayoutContainer)part).getChildren());
		}
	}
}
/**
 * Hide the presentation.
 */
public void deactivate() {
	if (!active) return;

	disableAllDrag();

	// Reparent all views to the main window
	Composite parent = (Composite)mainLayout.getParent();
	Vector children = new Vector();
	collectViewPanes(children, mainLayout.getChildren());
	
	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * for (int i = 0, length = detachedWindowList.size(); i < length; i++){
	 * 	DetachedWindow window = (DetachedWindow)detachedWindowList.get(i);
	 * 	collectViewPanes(children, window.getChildren());
	 * }
	 */

	// *** Do we even need to do this if detached windows not supported?	 
	Enumeration enum = children.elements();
	while (enum.hasMoreElements()) {
		LayoutPart part = (LayoutPart)enum.nextElement();
		part.reparent(parent);
	}

	// Dispose main layout.	
	mainLayout.dispose();

	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * // Dispose the detached windows
	 * for (int i = 0, length = detachedWindowList.size(); i < length; i++){
	 * 	DetachedWindow window = (DetachedWindow)detachedWindowList.get(i);
	 * 	window.close();
	 * }
	 */

	active = false;
}
/**
 * Deref a given part.  Deconstruct its container as required.
 * Do not remove drag listeners.
 */
private void derefPart(LayoutPart part) {
	// Get vital part stats before reparenting.
	Window oldWindow = part.getWindow();
	ILayoutContainer oldContainer = part.getContainer();
	
	/*
	 * Detached window no longer supported - remove when confirmed
	 * 
	 * // Reparent the part back to the main window
	 * part.reparent((Composite)mainLayout.getParent());
	 */

	// Update container.
	if (oldContainer == null) 
		return;
		
	oldContainer.remove(part);
	updateContainerVisibleTab(oldContainer);
	
	LayoutPart[] children = oldContainer.getChildren();
	if (oldWindow instanceof WorkbenchWindow) {
		boolean hasChildren = (children != null) && (children.length > 0);
		if (hasChildren) {
			// make sure one is at least visible
			int childVisible = 0;
			for (int i = 0; i < children.length; i++)
				if(children[i].getControl() != null)
					childVisible++;

			// none visible, then reprarent and remove container
			if (oldContainer instanceof PartTabFolder) {
				PartTabFolder folder = (PartTabFolder)oldContainer;
				if(childVisible == 0) {
					ILayoutContainer parentContainer = folder.getContainer();
					for (int i = 0; i < children.length; i++) {
						folder.remove(children[i]);
						parentContainer.add(children[i]);
					}	
					hasChildren = false;
				} else if(childVisible == 1) {
					LayoutTree layout = mainLayout.getLayoutTree();
					layout = layout.find(folder);
					layout.setBounds(layout.getBounds());
				}
			}
		}
		
		if (!hasChildren){
			// There are no more children in this container, so get rid of it
			if (oldContainer instanceof LayoutPart) {
				LayoutPart parent = (LayoutPart)oldContainer;
				ILayoutContainer parentContainer = parent.getContainer();
				if (parentContainer != null) {
					parentContainer.remove(parent);
					parent.dispose();
				}
			}
		}
	}
	/*
	 * Detached window no longer supported - remove when confirmed
	 * 
	 * else if (oldWindow instanceof DetachedWindow) {
	 * 	if (children == null || children.length == 0) {
	 * 		// There are no more children in this container, so get rid of it
	 * 		// Turn on redraw again just in case it was off.
	 * 		oldWindow.getShell().setRedraw(true);
	 * 		oldWindow.close();
	 * 		detachedWindowList.remove(oldWindow);
	 * 	} else {
	 * 		// There are children.  If none are visible hide detached window.
	 * 		boolean allInvisible = true;
	 * 		for (int i = 0, length = children.length; i < length; i++){
	 * 			if (!(children[i] instanceof PartPlaceholder)) {
	 * 				allInvisible = false;
	 * 				break;
	 * 			}
	 * 		}
	 * 		if (allInvisible) {
	 * 			DetachedPlaceHolder placeholder = new DetachedPlaceHolder("", //$NON-NLS-1$
	 * 				oldWindow.getShell().getBounds());
	 * 			for (int i = 0, length = children.length; i < length; i++){
	 * 				oldContainer.remove(children[i]);
	 * 				children[i].setContainer(placeholder);
	 * 				placeholder.add(children[i]);
	 * 			}
	 * 			detachedPlaceHolderList.add(placeholder);
	 * 			oldWindow.close();
	 * 			detachedWindowList.remove(oldWindow);
	 * 		}
	 * 	}
	 * }
	 */
}
/**
 * Create a detached window containing a part.
 */
/*
 * Detached window no longer supported - remove when confirmed
 * 
 * private void detach(LayoutPart part, int x, int y) {
 * 	// Detaching is disabled on some platforms ..
 * 	if (!detachable) 
 * 		return;
 * 		
 * 	// Calculate detached window size.
 * 	Point size = part.getSize();
 * 	if (size.x == 0 || size.y == 0) {
 * 		ILayoutContainer container = part.getContainer();
 * 		if (container instanceof LayoutPart) {
 * 			size = ((LayoutPart)container).getSize();
 * 		}
 * 	}
 * 	int width = Math.max(size.x, MIN_DETACH_WIDTH);
 * 	int height = Math.max(size.y, MIN_DETACH_HEIGHT);
 * 	
 * 	// Create detached window.
 * 	DetachedWindow window = new DetachedWindow(page);
 * 	detachedWindowList.add(window);
 * 	
 * 	// Open window.
 * 	window.create();
 * 	window.getShell().setBounds(x, y, width, height);
 * 	window.open();
 * 
 * 	if (part instanceof PartTabFolder) {
 * 		window.getShell().setRedraw(false);
 * 		parentWidget.setRedraw(false);
 * 		LayoutPart visiblePart = ((PartTabFolder)part).getVisiblePart();
 * 		LayoutPart children[] = ((PartTabFolder)part).getChildren();
 * 		for (int i = 0; i < children.length; i++) {
 * 			if (children[i] instanceof ViewPane) {
 * 				// remove the part from its current container
 * 				derefPart(children[i]);
 * 				// add part to detached window.
 * 				ViewPane pane = (ViewPane) children[i];
 * 				window.getShell().setText(pane.getPart().getTitle());
 * 				window.add(pane, partDropListener);
 * 			}
 * 		}
 * 		if (visiblePart != null) {
 * 			bringPartToTop(visiblePart);
 * 			visiblePart.setFocus();
 * 		}
 * 		window.getShell().setRedraw(true);
 * 		parentWidget.setRedraw(true);
 * 	}
 * 	else {
 * 		// remove the part from its current container
 * 		derefPart(part);
 * 		// add part to detached window.
 * 		ViewPane pane = (ViewPane) part;
 * 		window.getShell().setText(pane.getPart().getTitle());
 * 		window.add(pane, partDropListener);
 * 		part.setFocus();
 * 	}
 * 
 * }
 */
 
/**
 * Create a detached window containing a part.
 */
/*
 * Detached window no longer supported - remove when confirmed
 * 
 * public void addDetachedPart(LayoutPart part) {
 * 	// Detaching is disabled on some platforms ..
 * 	if (!detachable) {
 * 		addPart(part);
 * 		return;
 * 	}
 * 		
 * 	// Calculate detached window size.
 * 	int width = 300;
 * 	int height = 300;
 * 	Rectangle bounds = parentWidget.getShell().getBounds();
 * 	int x = bounds.x + (bounds.width - width) / 2;
 * 	int y = bounds.y + (bounds.height - height) / 2;
 * 	
 * 	// Create detached window.
 * 	DetachedWindow window = new DetachedWindow(page);
 * 	detachedWindowList.add(window);
 * 	window.create();
 * 
 * 	// add part to detached window.
 * 	part.createControl(window.getShell());
 * 	ViewPane pane = (ViewPane) part;
 * 	window.getShell().setText(pane.getPart().getTitle());
 * 	window.add(pane, partDropListener);
 * 
 * 	// Open window.
 * 	window.getShell().setBounds(x, y, width, height);
 * 	window.open();
 * 
 * 	part.setFocus();
 * 
 * 	// enable direct manipulation
 * 	enableDrag(pane);
 * 	enableDrop(part);
 * }
 */
 
/**
 * disableDragging.
 */
private void disableAllDrag() {

	// release Drag parts
	Iterator enum = dragParts.values().iterator();
	while(enum.hasNext()) {
		PartDragDrop part = (PartDragDrop)enum.next();
		
		// remove d&d to start from the view's tab if in a folder
		ILayoutContainer container = part.getSourcePart().getContainer();
		if (container instanceof PartTabFolder)
			((PartTabFolder)container).disableDrag(part.getSourcePart());

		part.dispose();
	}

	dragParts.clear();
}
/**
 * disableDragging.
 */
/*package*/ void disableDrag(ViewPane part) {
	// remove view from the drag sources
	if (dragParts.containsKey(part)){
		PartDragDrop partDragDrop = (PartDragDrop)dragParts.get(part);
		partDragDrop.dispose();
		dragParts.remove(part);
	}

	// remove d&d to start from the view's tab if in a folder
	ILayoutContainer container = part.getContainer();
	if (container instanceof PartTabFolder)
		((PartTabFolder)container).disableDrag(part);
}
/**
 * Dispose all sashs used in this perspective.
 */
public void disposeSashes() {
	mainLayout.disposeSashes();
}
/**
 * enableDragging.
 */
private void enableAllDrag() {

	Vector draggableParts = new Vector();
	collectDragParts(draggableParts, mainLayout.getChildren());

	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * for (int i = 0, length = detachedWindowList.size(); i < length; i++){
	 * 	DetachedWindow window = (DetachedWindow)detachedWindowList.get(i);
	 * 	collectDragParts(draggableParts, window.getChildren());
	 * }
	 */

	Enumeration enum = draggableParts.elements();
	while (enum.hasMoreElements()) {
		LayoutPart part = (LayoutPart)enum.nextElement();
		if (part instanceof ViewPane)
			enableDrag((ViewPane)part);
	}

	
}
/**
 * enableDragging.
 */
private void enableAllDrop() {

	Vector dropTargets = new Vector();
	collectDropTargets(dropTargets, mainLayout.getChildren());
	
	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * for (int i = 0, length = detachedWindowList.size(); i < length; i++){
	 * 	DetachedWindow window = (DetachedWindow)detachedWindowList.get(i);
	 * 	collectDropTargets(dropTargets, window.getChildren());
	 * }
	 */

	Enumeration enum = dropTargets.elements();
	while (enum.hasMoreElements()) {
		LayoutPart part = (LayoutPart)enum.nextElement();
		enableDrop(part);
	}
}
/**
 * enableDrag
 */
/*package*/ void enableDrag(ViewPane part) {
	// allow d&d to start from the view's title bar
	Control control = part.getDragHandle();
	if (control != null) {
		PartDragDrop dragSource = new PartDragDrop(part, control);
		dragSource.addDropListener(partDropListener);
		dragParts.put(part, dragSource);
	}

	// allow d&d to start from the view's tab if in a folder
	ILayoutContainer container = part.getContainer();
	if (container instanceof PartTabFolder)
		((PartTabFolder)container).enableDrag(part, partDropListener);
}
private void enableDrop(LayoutPart part) {
	Control control = part.getControl();
	if (control != null)
		control.setData((IPartDropTarget)part);
}
/**
 * Find the first part with a given ID in the presentation.  
 */
private LayoutPart findPart(String id) {
	// Check main window.
	LayoutPart part = findPart(id, mainLayout.getChildren());
	if (part != null)
		return part;

	/*
	 * Detached window no longer supported - remove when confirmed
	 * 
	 * // Check each detached windows
	 * for (int i = 0, length = detachedWindowList.size(); i < length; i++){
	 * 	DetachedWindow window = (DetachedWindow)detachedWindowList.get(i);
	 * 	part = findPart(id, window.getChildren());
	 * 	if (part != null)
	 * 		return part;
	 * }
	 * for (int i = 0; i < detachedPlaceHolderList.size(); i++){
	 * 	DetachedPlaceHolder holder = (DetachedPlaceHolder)detachedPlaceHolderList.get(i);
	 * 	part = findPart(id,holder.getChildren());
	 * 	if(part != null)
	 * 		return part;
	 * }
	 */
	 
	// Not found.
	return null;
}
/**
 * Find the first part with a given ID in the presentation.  
 */
private LayoutPart findPart(String id, LayoutPart[] parts) {
	for (int i = 0, length = parts.length; i < length; i++) {
		LayoutPart part = parts[i];
		if (part.getID().equals(id)) {
			return part; 
		} else if (part instanceof EditorArea) {
			// Skip.
		} else if (part instanceof ILayoutContainer) {
			part = findPart(id, ((ILayoutContainer)part).getChildren());
			if (part != null)
				return part;
		}
	}
	return null;
}
/**
 * Returns true if a placeholder exists for a given ID.
 */
public boolean hasPlaceholder(String id) {
	LayoutPart testPart = findPart(id);
	return (testPart != null && testPart instanceof PartPlaceholder);
}
/**
 * Returns the layout container.
 */
public RootLayoutContainer getLayout() {
	return mainLayout;
}
///**
// * Returns the zoomed part.
// * <p>
// * If the zoomed part is an editor, it will be the 
// * editor which caused the workbook it is in to be zoomed. It may not be the
// * visible editor. The zoomed part will always be an editor in the zoomed
// * workbench.
// * </p>
// */
///*package*/ IWorkbenchPart getZoomPart() {
//	return zoomPart;
//}
/**
 * Gets the active state.
 */
public boolean isActive() {
	return active;
}
/**
 * Returns whether the part is a fast view or not
 */
private boolean isFastView(IWorkbenchPartReference ref) {
	if (ref instanceof IViewReference) {
		WorkbenchPage page = (WorkbenchPage)ref.getPage();
		return page.isFastView((IViewReference)ref);
	}
	return false;
}
/**
 * Returns whether the presentation is zoomed.
 */
public boolean isZoomed() {
	return (zoomPart != null);
}
/**
 * Place the part on the shortcut bar as a fast view
 */
private void makeFast(LayoutPart part) {
	if (part instanceof PartTabFolder) {
		LayoutPart[] children = ((PartTabFolder)part).getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof ViewPane)
				page.addFastView(((ViewPane)children[i]).getViewReference());
		}
	}
	else {
		page.addFastView(((ViewPane)part).getViewReference());
	}
}
/**
 * Move a part from one position to another.
 * Supports moving a part within the same window
 * and moving a part from a detach window into
 * the main window.
 */
private void movePart(LayoutPart part, int position, LayoutPart relativePart) {
	ILayoutContainer newContainer = relativePart.getContainer();
	
	if (newContainer instanceof RootLayoutContainer) {
		// Determine the position
		RootLayoutContainer sashContainer = (RootLayoutContainer) newContainer;
		int relativePosition = IPageLayout.LEFT;
		if (position == PartDragDrop.RIGHT)
			relativePosition = IPageLayout.RIGHT;
		else if (position == PartDragDrop.TOP)
			relativePosition = IPageLayout.TOP;
		else if (position == PartDragDrop.BOTTOM)
			relativePosition = IPageLayout.BOTTOM;

		/*
		 * Detached window no longer supported - remove when confirmed
		 *
		 * // folder part from detach window is special
		 * if (part instanceof PartTabFolder) {
		 * 	Window window = part.getWindow();
		 * 	if (window instanceof DetachedWindow) {
		 * 		window.getShell().setRedraw(false);
		 * 		parentWidget.setRedraw(false);
		 * 		LayoutPart visiblePart = ((PartTabFolder)part).getVisiblePart();
		 * 		// create a new folder and add the children to it
		 * 		PartTabFolder folder = new PartTabFolder();
		 * 		sashContainer.add(folder, relativePosition, (float) 0.5, relativePart);
		 * 		LayoutPart[] children = ((PartTabFolder)part).getChildren();
		 * 		for (int i = 0; i < children.length; i++) {
		 * 			derefPart(children[i]);
		 * 			folder.add(children[i]);
		 * 			if (children[i] instanceof ViewPane)
		 * 				folder.enableDrag((ViewPane)children[i], partDropListener);
		 * 		}
		 * 		if (visiblePart != null) {
		 * 			bringPartToTop(visiblePart);
		 * 			visiblePart.setFocus();
		 * 		}
		 * 		// No need to set redraw on detach window as it should
		 * 		// be closed now.
		 * 		parentWidget.setRedraw(true);
		 * 		// return so not to add folder from detach window
		 * 		return;
		 * 	}
		 * }
		 */

		ILayoutContainer oldContainer = part.getContainer();
		if(oldContainer != sashContainer) {
			// just a normal move
			derefPart(part);
			sashContainer.add(part, relativePosition, (float) 0.5, relativePart);
		} else {
			//Move the part to its new position but keep its bounds if possible.
			sashContainer.move(part,relativePosition,relativePart);			
		}
		part.setFocus();
	} 
	else if (newContainer instanceof PartTabFolder) {
		// move this part relative to the folder
		// rather than relative to the part in the folder
		movePart(part, position, (PartTabFolder)newContainer);
	}
}
/**
 * Notification sent during drag and drop operation.
 * Only allow views, tab folders, and fast view icons to participate
 * in the drag. Only allow the drop on a view, tab
 * folder, the shortcut bar, or editor area.
 */
/*package*/ void onPartDragOver(PartDropEvent e) {
	
	// If a fast view is active, and the dragged element is the active
	// fast view or an icon for a fast view, minimize the active fast
	// view when the drag begins. This is to allow it to be dropped
	// somewhere that the active fast view was covering.
	Perspective persp = page.getActivePerspective();
	if (persp.getActiveFastView() != null) {
		if (e.dragSource instanceof ViewPane) {
			IViewReference ref = (IViewReference)((ViewPane)e.dragSource).getPartReference();
			if (ref == persp.getActiveFastView()) {
				persp.setActiveFastView(null, 0);
			}
		} 
		else if (e.dragSource instanceof ShortcutBarPart) {
			if (persp.getActiveFastView() != null) {
				WorkbenchWindow window = (WorkbenchWindow)page.getWorkbenchWindow();
				ToolItem icon = window.getShortcutDND().getDraggedItem();
				IViewReference ref = (IViewReference)icon.getData(ShowFastViewContribution.FAST_VIEW);
				// If the dragged element is an icon for the active 
				// fast view, remove it instantly. It will be reactivated
				// if the icon is dropped somewhere invalid.
				if (ref == persp.getActiveFastView())
					persp.setActiveFastView(null, 0);
				// If the dragged element is an icon for a different
				// fast view, scroll the active fast view off the page,
				// just like any other instance when the active fast
				// view loses focus.
				else
					persp.setActiveFastView(null); // slide the fast view off the page.
				// Set flag to tell that a fast view was active when the drag began.
				wasFastViewActive = true; 
			}
		}
	}
	
	// Handle the case of an icon being dragged.
	if (e.dragSource instanceof ShortcutBarPart) {
		onFastViewIconDrag(e);
		return;		
	}
	
	/*
	 * Detached window no longer supported - remove when confirmed
	 * 
	 * Note, any drop that is considered invalid for
	 * stack or move, will be set as OFF_SCREEN causing
	 * either a new detach window to be created or
	 * it the source was a detach window, a location move.
	 * 
	 * int offScreenPosition = PartDragDrop.OFFSCREEN;
	 * if (!detachable)
	 * 	offScreenPosition = PartDragDrop.INVALID;
	 */
	
	int offScreenPosition = PartDragDrop.INVALID;
	
	// If source and target are in different windows reject.
	if (e.dragSource != null && e.dropTarget != null) {
		if (e.dragSource.getWorkbenchWindow() != e.dropTarget.getWorkbenchWindow()) {
			e.dropTarget = null;
			e.relativePosition = offScreenPosition;
			return;
		}
	}	

	// If drop target is short cut bar force center.
	if (e.dropTarget instanceof ShortcutBarPart) {
		// If the drag source is a fast view, the shortcut bar is
		// invalid drop target.
		if (e.dragSource instanceof ViewPane) {
			IViewReference ref = (IViewReference)((ViewPane)e.dragSource).getPartReference();
			if (isFastView(ref)) {
				e.dropTarget = null;
				e.relativePosition = PartDragDrop.INVALID;
				return;
			}
		}
		e.relativePosition = PartDragDrop.CENTER;
		return;
	}

	// If drop target is editor area exclude center.
	if (e.dropTarget instanceof EditorArea) {
		if (e.relativePosition == PartDragDrop.CENTER){
			e.dropTarget = null;
			e.relativePosition = offScreenPosition;
			return;
		}
		return;
	}

	// If drop target is offscreen ..
	if (e.relativePosition == PartDragDrop.OFFSCREEN) {
		/*
		 * Detached window no longer supported - remove when confirmed
		 * 
		 * // If detaching is not supported then exclude.
		 * if (!detachable) {
		 * 	e.relativePosition = PartDragDrop.INVALID;
		 * 	return;
		 * }
		 */
		 e.relativePosition = PartDragDrop.INVALID;

		// If source is in detach window by itself then allow as window move.
/*		Window window = e.dragSource.getWindow();
		if (window instanceof DetachedWindow) {
			if (e.dragSource instanceof PartTabFolder) {
				// there is only one tab folder in a detach window
				e.relativePosition = PartDragDrop.INVALID;
				return;
			}
			ILayoutContainer container = e.dragSource.getContainer();
			if (container instanceof PartTabFolder) {
				if (((PartTabFolder)container).getItemCount() == 1) {
					// only 1 view in folder.
					e.relativePosition = PartDragDrop.INVALID;
					return;
				}
			}
		}
*/
		// All seems well
		return;
	}

	// If drop target is not registered object then reject.
	if (e.dropTarget == null && e.relativePosition != PartDragDrop.OFFSCREEN) {
		e.dropTarget = null;
		e.relativePosition = offScreenPosition;
		return;
	}

	// If drop target is not over view, or tab folder, reject.
	if (!(e.dropTarget instanceof ViewPane || e.dropTarget instanceof PartTabFolder)) {
		e.dropTarget = null;
		e.relativePosition = offScreenPosition;
		return;
	}

	// If drag source is view ..
	if (e.dragSource instanceof ViewPane) {
		if (e.dragSource == e.dropTarget) {
			// Reject stack onto same view
			if (e.relativePosition == PartDragDrop.CENTER) {
				e.dropTarget = null;
				e.relativePosition = PartDragDrop.INVALID;
				return;
			}
			// Reject attach & detach to ourself
			ILayoutContainer container = e.dragSource.getContainer();
			if (!(container instanceof PartTabFolder)) {
				e.dropTarget = null;
				e.relativePosition = PartDragDrop.INVALID;
				return;
			}
			if (((PartTabFolder)container).getItemCount() == 1) {
				e.dropTarget = null;
				e.relativePosition = PartDragDrop.INVALID;
				return;
			}
		}

		// If drag source's folder same as target
		if (e.dragSource.getContainer() == e.dropTarget) {
			// Reject stack/detach/attach to ourself
			if (((PartTabFolder)e.dropTarget).getItemCount() == 1) {
				e.dropTarget = null;
				e.relativePosition = PartDragDrop.INVALID;
				return;
			}
		}
		
		/*
		 * Detached window no longer supported - remove when confirmed
		 * 
		 * // If target is detached window force stacking.
		 * Window window = e.dropTarget.getWindow();
		 * if (window instanceof DetachedWindow) {
		 * 	e.relativePosition = PartDragDrop.CENTER;
		 * 	return;
		 * }
		 */
	
		// All seems well
		return;
	}

	// If drag source is tab folder..
	if (e.dragSource instanceof PartTabFolder) {
		// Reject stack in same tab folder
		if (e.dragSource == e.dropTarget) {
			e.dropTarget = null;
			e.relativePosition = PartDragDrop.INVALID;
			return;
		}
		
		// Reject stack on view in same tab folder
		if (e.dropTarget instanceof ViewPane) {
			if (e.dropTarget.getContainer() == e.dragSource) {
				e.dropTarget = null;
				e.relativePosition = PartDragDrop.INVALID;
				return;
			}
		}
		
		/*
		 * Detached window no longer supported - remove when confirmed
		 * 
		 * // If target is detached window force stacking.
		 * Window window = e.dropTarget.getWindow();
		 * if (window instanceof DetachedWindow) {
		 * 	e.relativePosition = PartDragDrop.CENTER;
		 * 	return;
		 * }
		 */
		
		// All seems well
		return;
	}

	// If invalid case reject drop.
	e.dropTarget = null;
	e.relativePosition = offScreenPosition;
}
/**
 * A fast view icon is the dragged element.
 */
private void onFastViewIconDrag(PartDropEvent e) {
	// If the drop target is not a view, folder, or the
	// editor area, we can not drop.
	if (!(e.dropTarget instanceof ViewPane || 
		  e.dropTarget instanceof PartTabFolder ||
		  e.dropTarget instanceof EditorArea ||
		  e.dropTarget instanceof ShortcutBarPart)) {
			e.dropTarget = null;
			e.relativePosition = PartDragDrop.INVALID;
	}
	// If the drop target is the editor area, we can not drop
	// in the center.
	if (e.dropTarget instanceof EditorArea &&
	    e.relativePosition == PartDragDrop.CENTER) {
			e.dropTarget = null;
			e.relativePosition = PartDragDrop.INVALID;
	}
	// If the drop target is the shortcut bar, we can 
	// only drop in the middle
	if (e.dropTarget instanceof ShortcutBarPart &&
	    !(e.relativePosition == PartDragDrop.CENTER)) {
			e.dropTarget = null;
			e.relativePosition = PartDragDrop.INVALID;
	}	
}
/**
 * Notification sent when drop happens. Only views and
 * tab folders were allowed to participate.
 */
/*package*/ void onPartDrop(PartDropEvent e) {
	// If invalid drop position ignore the drop (except for possibly reactivating previous 
	// active fast view.
	if (e.relativePosition == PartDragDrop.INVALID) {
		Perspective persp = page.getActivePerspective();
		if (e.dragSource instanceof ViewPane) {
			IViewReference ref = (IViewReference)((ViewPane)(e.dragSource)).getPartReference();
			// If the view is a fast view, then it must have been the active fast view.
			// Make it the active fast view again if it is dropped somewhere invalid.
			if (isFastView(ref)) 
				persp.setActiveFastView(ref);
		} else if (e.dragSource instanceof ShortcutBarPart) {
			WorkbenchWindow window = (WorkbenchWindow)page.getWorkbenchWindow();
			ToolItem icon = window.getShortcutDND().getDraggedItem();
			IViewReference ref = (IViewReference)icon.getData(ShowFastViewContribution.FAST_VIEW);
			// If the icon being dropped is the icon for a fast view that was active when
			// the drag began, reactivate the fast view when the icon is dropped somewhere invalid.
			if (ref == persp.getPreviousActiveFastView() && wasFastViewActive)
				persp.setActiveFastView(ref);
		}
		// Reset the flag that determines if a fast view was active when the drag began			
		wasFastViewActive = false;	
		return;
	}
		
	// Reset the flag that determines if a fast view was active when the drag began
	wasFastViewActive = false; 
	
	// If the drag source is a fast view, make it a regular view
	// when it is dropped somewhere valid.
	if (e.dragSource instanceof ViewPane) {
		IViewReference ref = (IViewReference)((ViewPane)(e.dragSource)).getPartReference();
		if (isFastView(ref)) {
			page.removeFastView(ref);
		}
	}
	
	// If the dragged element is a fast view icon, set the dragSource to be the
	// view represented by the icon before it is dropped into the page layout,
	// and remove the view from the fast view list.
	if (e.dragSource instanceof ShortcutBarPart && !(e.dropTarget instanceof ShortcutBarPart)) {
		WorkbenchWindow window = (WorkbenchWindow)page.getWorkbenchWindow();
		ToolItem icon = window.getShortcutDND().getDraggedItem();
		IViewReference ref = (IViewReference)icon.getData(ShowFastViewContribution.FAST_VIEW);
		//Make sure the view is restored.
		if(ref.getPart(true) == null)
			return;
		e.dragSource = ((WorkbenchPartReference)ref).getPane();
		page.removeFastView(ref);		
	}
		
	switch (e.relativePosition) {
		case PartDragDrop.OFFSCREEN:
			/*
			 * Detached window no longer supported - remove when confirmed
			 * 
			 * We should not get this case anymore...
			 * 
			 * Window window = e.dragSource.getWindow();
			 * if (window instanceof DetachedWindow) {
			 * 	// only one tab folder in a detach window, so do window move
			 * 	if (e.dragSource instanceof PartTabFolder) {
			 * 		window.getShell().setLocation(e.x, e.y);
			 * 		break;
			 * 	}
			 * 	// if only one view in tab folder then do a window move
			 * 	ILayoutContainer container = e.dragSource.getContainer();
			 * 	if (container instanceof PartTabFolder) {
			 * 		if (((PartTabFolder)container).getItemCount() == 1) {
			 * 			window.getShell().setLocation(e.x, e.y);
			 * 			break;
			 * 		}
			 * 	}
			 * }
			 * 
			 * // If layout is modified always zoom out.
			 * if (isZoomed())
			 * 	zoomOut();
			 * // do a normal part detach
			 * detach(e.dragSource, e.x, e.y);
			 */
			break;
		case PartDragDrop.CENTER:
			// If layout is modified always zoom out.
			if (isZoomed())
				zoomOut();
			if (e.dropTarget instanceof ShortcutBarPart) {
				if (e.dragSource instanceof ShortcutBarPart) 
					/* fast view is beig dragged, move it to the new position
					 * PR 6988 */
					moveFastView((ShortcutBarPart)e.dragSource, new Point(e.cursorX, e.cursorY));
				else {
					//First create the fast view
					makeFast(e.dragSource);
					//Then move it to the intended position.  PR 6988
					if (e.dragSource instanceof ViewPane) {
						ViewPane pane = (ViewPane)e.dragSource;
						/* Convert the point to its display-relative coordinates, 
						 * then back to the toolbar-relative coordinates. */
						Point point = pane.getControl().toDisplay(new Point(e.cursorX, e.cursorY));
						Point destination = ((ShortcutBarPart)e.dropTarget).getControl().toControl(point);
						reorderFastViews((ShortcutBarPart)e.dropTarget, ((ViewPane)e.dragSource).getViewReference(), destination);
					}
				}
				break;
			}
			if (e.dragSource instanceof ViewPane && e.dropTarget instanceof PartTabFolder) {
				if (e.dragSource.getContainer() == e.dropTarget) {
					((PartTabFolder)e.dropTarget).reorderTab((ViewPane)e.dragSource, e.cursorX, e.cursorY);
					break;
				}
			}
			stack(e.dragSource, e.dropTarget);
			break;
		case PartDragDrop.LEFT:
		case PartDragDrop.RIGHT:
		case PartDragDrop.TOP:
		case PartDragDrop.BOTTOM:
			// If layout is modified always zoom out.
			if (isZoomed())
				zoomOut();
			movePart(e.dragSource, e.relativePosition, e.dropTarget);
			break;
	}
}
/**
 * Method moveFastView.
 * @param shortcutBarPart
 * @param x
 * @param y
 */
private void moveFastView(ShortcutBarPart shortcutBarPart, Point point) {
	
	// Get the fast view that is being dragged
	WorkbenchWindow window = (WorkbenchWindow)page.getWorkbenchWindow();
	ToolItem draggedItem = window.getShortcutDND().getDraggedItem();
	IViewReference draggedView = (IViewReference)draggedItem.getData(ShowFastViewContribution.FAST_VIEW);
	
	//move the fast view to the new position
	reorderFastViews(shortcutBarPart, draggedView, point);
}
/**
 * Method reorderFastViews.
 * @param shortcutBarPart
 * @param draggedView
 * @param x
 * @param y
 */
private void reorderFastViews(
	ShortcutBarPart shortcutBarPart,
	IViewReference draggedView,
	Point point) {
		
	IViewReference destinationView = null;
	boolean placeAtEnd = true;	
	
	// Get the fast view that it is being dragged to
	ToolBar bar = (ToolBar)shortcutBarPart.getControl();
	//get the ToolItem the cursor is over
	ToolItem destItem = bar.getItem(point);
	
	//determine where to place the view
	if (destItem != null) { //user is over a toolitem, either fast or perspecitve
		destinationView = (IViewReference)destItem.getData(ShowFastViewContribution.FAST_VIEW);
		placeAtEnd = false;
	}
	page.getActivePerspective().moveFastView(draggedView, destinationView, placeAtEnd);
	
	//refresh the menu to show the change
	((WorkbenchWindow)page.getWorkbenchWindow()).getShortcutBar().update(true);
}

/**
 * Returns whether changes to a part will affect zoom.
 * There are a few conditions for this ..
 *		- we are zoomed.
 *		- the part is contained in the main window.
 *		- the part is not the zoom part
 *      - the part is not a fast view
 *      - the part and the zoom part are not in the same editor workbook
 */
public boolean partChangeAffectsZoom(PartPane pane) {
	if (zoomPart == null)
		return false;
	if (pane.getWindow().getShell() != 
		page.getWorkbenchWindow().getShell())
		return false;
	if (pane.isZoomed())
		return false;
	if(isFastView(pane.getPartReference()))
		return false;

	PartPane zoomPane = (PartPane)((WorkbenchPartReference)zoomPart).getPane();
	if(pane instanceof EditorPane && zoomPane instanceof EditorPane) {
		if(((EditorPane)pane).getWorkbook().equals(((EditorPane)zoomPane).getWorkbook()))
			return false;
	}

	return true;
}
/**
 * Remove all references to a part.
 */
public void removePart(LayoutPart part) {
	// If part added / removed always zoom out.
	if (isZoomed())
		zoomOut();
	
	// Disable drag.
	if (part instanceof ViewPane)
		disableDrag((ViewPane)part);
				
	// Reparent the part back to the main window
	Composite parent = (Composite)mainLayout.getParent();
	part.reparent(parent);

	// Replace part with a placeholder	
	ILayoutContainer container = part.getContainer();
	if (container != null) {
		container.replace(part, new PartPlaceholder(part.getID()));
		updateContainerVisibleTab(container);

		// If the parent is root we're done.  Do not try to replace
		// it with placeholder.
		if (container == mainLayout)
			return;

		// If the parent is empty replace it with a placeholder.
		LayoutPart[] children = container.getChildren();
		if (children != null) {
			boolean allInvisible = true;
			for (int i = 0, length = children.length; i < length; i++){
				if (!(children[i] instanceof PartPlaceholder)) {
					allInvisible = false;
					break;
				}
			}
			if (allInvisible && (container instanceof LayoutPart)) {
				// what type of window are we in?
				LayoutPart cPart = (LayoutPart)container;
				Window oldWindow = cPart.getWindow();
				if (oldWindow instanceof WorkbenchWindow) {
					// PR 1GDFVBY: PartTabFolder not disposed when page closed.
					if (container instanceof PartTabFolder)
						((PartTabFolder)container).dispose();
						
					// replace the real container with a ContainerPlaceholder
					ILayoutContainer parentContainer = cPart.getContainer();
					ContainerPlaceholder placeholder = new ContainerPlaceholder(cPart.getID());
					placeholder.setRealContainer(container);
					parentContainer.replace(cPart, placeholder);
				}
				/*
				 * Detached window no longer supported - remove when confirmed
				 * 
				 * else if (oldWindow instanceof DetachedWindow) {
				 * 	DetachedPlaceHolder placeholder = new DetachedPlaceHolder("",oldWindow.getShell().getBounds());//$NON-NLS-1$
				 * 	for (int i = 0, length = children.length; i < length; i++){
				 * 		children[i].getContainer().remove(children[i]);
				 * 		children[i].setContainer(placeholder);
				 * 		placeholder.add(children[i]);
				 * 	}
				 * 	detachedPlaceHolderList.add(placeholder);
				 * 	oldWindow.close();
				 * 	detachedWindowList.remove(oldWindow);
				 * }
				 */
			}
		}
	}
}
/**
 * Add a part to the presentation.
 */
public void replacePlaceholderWithPart(LayoutPart part) {
	// If part added / removed always zoom out.
	if (isZoomed())
		zoomOut();

	// Look for a PartPlaceholder that will tell us how to position this object
	PartPlaceholder[] placeholders = collectPlaceholders();
	for (int i = 0, length = placeholders.length; i < length; i++){
		if (placeholders[i].getID().equals(part.getID())){
			// found a matching placeholder which we can replace with the
			// new View
			ILayoutContainer container = placeholders[i].getContainer();
			if (container != null) {
				if (container instanceof ContainerPlaceholder) {
					// One of the children is now visible so replace the
					// ContainerPlaceholder with the real container
					ContainerPlaceholder containerPlaceholder = (ContainerPlaceholder)container;
					ILayoutContainer parentContainer = containerPlaceholder.getContainer();
					container = (ILayoutContainer)containerPlaceholder.getRealContainer();
					if (container instanceof LayoutPart) {
						parentContainer.replace(containerPlaceholder, (LayoutPart)container);
					}
					containerPlaceholder.setRealContainer(null);
					
				}
				container.replace(placeholders[i], part);
				return;
			}
		}
	}

	// If there was no placeholder then the editor workbook is not in the workbench.
	// That's OK.  Just return.
}
/**
 * @see IPersistablePart
 */
public IStatus restoreState(IMemento memento) {
	// Restore main window.
	IMemento childMem = memento.getChild(IWorkbenchConstants.TAG_MAIN_WINDOW);
	return mainLayout.restoreState(childMem);

	/*
	 * Detached window no longer supported - remove when confirmed
	 * 
	 * // Restore each floating window.
	 * if (detachable) {
	 * 	IMemento detachedWindows[] = memento.getChildren(IWorkbenchConstants.TAG_DETACHED_WINDOW);
	 * 	for (int nX = 0; nX < detachedWindows.length; nX ++) {
	 * 		DetachedWindow win = new DetachedWindow(page);
	 * 		detachedWindowList.add(win);
	 * 		win.restoreState(detachedWindows[nX]);
	 * 	}
	 * 	IMemento childrenMem[] = memento.getChildren(IWorkbenchConstants.TAG_HIDDEN_WINDOW);
	 * 	for (int i = 0, length = childrenMem.length; i < length; i++){
	 * 		DetachedPlaceHolder holder = new DetachedPlaceHolder("",new Rectangle(0,0,0,0));//$NON-NLS-1$
	 * 		holder.restoreState(childrenMem[i]);
	 * 		detachedPlaceHolderList.add(holder);
	 * 	}
	 * }
	 */
}
/**
 * @see IPersistablePart
 */
public IStatus saveState(IMemento memento) {
	// Persist main window.
	IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_MAIN_WINDOW);
	return mainLayout.saveState(childMem);

	/*
	 * Detached window no longer supported - remove when confirmed
	 * 
	 * if (detachable) {	
	 * 	// Persist each detached window.
	 * 	for (int i = 0, length = detachedWindowList.size(); i < length; i++){
	 * 		DetachedWindow window = (DetachedWindow)detachedWindowList.get(i);
	 * 		childMem = memento.createChild(IWorkbenchConstants.TAG_DETACHED_WINDOW);
	 * 		window.saveState(childMem);
	 * 	}
	 * 	for (int i = 0, length = detachedPlaceHolderList.size(); i < length; i++){
	 * 		DetachedPlaceHolder holder = (DetachedPlaceHolder)detachedPlaceHolderList.get(i);
	 * 		childMem = memento.createChild(IWorkbenchConstants.TAG_HIDDEN_WINDOW);
	 * 		holder.saveState(childMem);
	 * 	}
	 * }
	 */
}
/**
 * Stack a layout part on the reference part
 */
private void stack(LayoutPart part, LayoutPart refPart) {
	parentWidget.setRedraw(false);
	if (part instanceof PartTabFolder) {
		LayoutPart visiblePart = ((PartTabFolder)part).getVisiblePart();
		LayoutPart[] children = ((PartTabFolder)part).getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof ViewPane)
				stackView((ViewPane)children[i], refPart);
		}
		if (visiblePart != null) {
			bringPartToTop(visiblePart);
			visiblePart.setFocus();
		}
	}
	else {
		stackView((ViewPane)part, refPart);
		bringPartToTop(part);
		part.setFocus();
	}
	parentWidget.setRedraw(true);
}
/**
 * Stack a view on a reference part.
 */
private void stackView(ViewPane newPart, LayoutPart refPart) {
	// derefence the part from its current container and shell
	derefPart(newPart);

	// determine the new container in which to add the part
	ILayoutContainer newContainer;
	if (refPart instanceof ILayoutContainer)
		newContainer = (ILayoutContainer)refPart;
	else
		newContainer = refPart.getContainer();

	if (newContainer instanceof PartTabFolder) {
		// Reparent part.  We may be adding it to a different shell !!!
		PartTabFolder folder = (PartTabFolder)newContainer;
		Composite newParent = folder.getParent();
		newPart.reparent(newParent);
		
		// Add part to existing folder
		folder.add(newPart);
		folder.enableDrag(newPart, partDropListener);
	} 
	else if (newContainer instanceof RootLayoutContainer) {
		// Create a new folder and add both items
		PartTabFolder folder = new PartTabFolder();
		((RootLayoutContainer)newContainer).replace(refPart, folder);
		folder.add(refPart);
		folder.add(newPart);
		if (refPart instanceof ViewPane)
			folder.enableDrag((ViewPane)refPart, partDropListener);
		folder.enableDrag(newPart, partDropListener);
	}
}
/**
 * Update the container to show the correct visible
 * tab based on the activation list.
 * 
 * @param org.eclipse.ui.internal.ILayoutContainer
 */
private void updateContainerVisibleTab(ILayoutContainer container) {
	if (!(container instanceof PartTabFolder))
		return;
		
	PartTabFolder folder = (PartTabFolder) container;
	LayoutPart[] parts = folder.getChildren();
	if (parts.length < 1)
		return;
		
	PartPane selPart = null;
	int topIndex = 0;
	IWorkbenchPartReference sortedPartsArray[] = ((WorkbenchPage)page).getSortedParts();
	List sortedParts = Arrays.asList(sortedPartsArray);
	for (int i = 0; i < parts.length; i++) {
		if (parts[i] instanceof PartPane) {
			IWorkbenchPartReference part = ((PartPane) parts[i]).getPartReference();
			int index = sortedParts.indexOf(part);
			if (index >= topIndex) {
				topIndex = index;
				selPart = (PartPane)parts[i];
			}
		}
	}
	
	if (selPart != null) {
		//Make sure the new visible part is restored.
		//If part can't be restored an error part is created.
		selPart.getPartReference().getPart(true);		
		int selIndex = folder.indexOf(selPart);
		if (folder.getSelection() != selIndex)
			folder.setSelection(selIndex);
	}
}
/**
 * Zoom in on a particular layout part.
 */
public void zoomIn(IWorkbenchPartReference ref) {
	PartPane pane = ((WorkbenchPartReference)ref).getPane();

	// Save zoom part.
	zoomPart = ref;

	// If view ..
	if (pane instanceof ViewPane) {
		parentWidget.setRedraw(false);
		Perspective persp = page.getActivePerspective();
		if (persp != null && ref instanceof IViewReference && page.isFastView((IViewReference)ref)) {
			persp.hideFastViewSash();
		}
		mainLayout.zoomIn(pane);
		pane.setZoomed(true);
		parentWidget.setRedraw(true);
	}

	// If editor ..
	else if (pane instanceof EditorPane) {
		parentWidget.setRedraw(false);
		EditorWorkbook wb = ((EditorPane)pane).getWorkbook();
		EditorArea ea = wb.getEditorArea();
		mainLayout.zoomIn(ea);
		ea.zoomIn(wb);
		wb.zoomIn();
		pane.setZoomed(true);
		parentWidget.setRedraw(true);
	}

	// Otherwise.
	else {
		zoomPart = null;
		return;
	}
}
/**
 * Zoom out.
 */
public void zoomOut() {
	// Sanity check.
	if (zoomPart == null)
		return;

	PartPane pane = ((WorkbenchPartReference)zoomPart).getPane();
	
	if (pane instanceof ViewPane) {
		parentWidget.setRedraw(false);
		mainLayout.zoomOut();
		pane.setZoomed(false);
		Perspective persp = page.getActivePerspective();
		if (persp != null && zoomPart instanceof IViewReference && page.isFastView((IViewReference)zoomPart)) {
			persp.showFastView((IViewReference)zoomPart);
		}
		parentWidget.setRedraw(true);
	} else if (pane instanceof EditorPane) {
		parentWidget.setRedraw(false);
		EditorWorkbook wb = ((EditorPane)pane).getWorkbook();
		EditorArea ea = wb.getEditorArea();
		wb.zoomOut();
		ea.zoomOut();
		mainLayout.zoomOut();
		pane.setZoomed(false);
		parentWidget.setRedraw(true);
	} else { //if null
		parentWidget.setRedraw(false);
		mainLayout.zoomOut();
		parentWidget.setRedraw(true);		
	}

	// Deref all.
	zoomPart = null;
}
}
