package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;
import java.util.List; // otherwise ambiguous with org.eclipse.swt.widgets.List

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.part.MultiEditor;

import org.eclipse.jface.action.*;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.preference.IPreferenceStore;
 
/**
 * A collection of views and editors in a workbench.
 */
public class WorkbenchPage implements IWorkbenchPage {
	private WorkbenchWindow window;
	private IAdaptable input;
	private IWorkingSet workingSet;
	private Composite composite;
	private ControlListener resizeListener;
	private IWorkbenchPart activePart; //Could be delete. This information is in the active part list;
	private ActivationList activationList = new ActivationList();
	private IEditorPart lastActiveEditor;
	private EditorManager editorMgr;
	private EditorPresentation editorPresentation;
	private PartListenerList partListeners = new PartListenerList();
	private ListenerList propertyChangeListeners = new ListenerList();
	private PageSelectionService selectionService = new PageSelectionService(this);
	private IActionBars actionBars;
	private ViewFactory viewFactory;
	private PerspectiveList perspList = new PerspectiveList();
	private Listener mouseDownListener;
	private IMemento deferredMemento;
	private PerspectiveDescriptor deferredActivePersp;
	private IPropertyChangeListener propertyChangeListener= new IPropertyChangeListener() {
		/*
		 * Remove the working set from the page if the working set is deleted.
		 */
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();
			if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property) && 
				event.getOldValue().equals(workingSet)) {
				setWorkingSet(null);
			}
		}
	};
	private ActionSwitcher actionSwitcher = new ActionSwitcher();
	/**
	 * Manages editor contributions and action set part associations.
	 */
	private class ActionSwitcher {
		private IWorkbenchPart activePart;
		private IEditorPart topEditor;
		private ArrayList actionSets = new ArrayList();
	
		/** 
		 * Updates the contributions given the new part as the active part.
		 *  
		 * @param newPart the new active part, may be <code>null</code>
		 */
		public void updateActivePart(IWorkbenchPart newPart) {
			if (activePart == newPart)
				return;
				
			boolean isNewPartAnEditor = newPart instanceof IEditorPart;
			if (isNewPartAnEditor) {
				String oldId = null;
				if (topEditor != null)
					oldId = topEditor.getSite().getId();
				String newId = newPart.getSite().getId();
				
				// if the active part is an editor and the new editor
				// is the same kind of editor, then we don't have to do anything
				if (activePart == topEditor && newId.equals(oldId))
					return;
				
				// remove the contributions of the old editor
				// if it is a different kind of editor
				if (oldId != null && !oldId.equals(newId)) 
					deactivateContributions(topEditor, true);

				// if a view was the active part, disable its contributions
				if (activePart != null && activePart != topEditor)
					deactivateContributions(activePart, true);
					
				// show (and enable) the contributions of the new editor
				// if it is a different kind of editor or if the 
				// old active part was a view
				if (!newId.equals(oldId) || activePart != topEditor)
					activateContributions(newPart, true);
				
			} else if (newPart == null) {
				if (activePart != null) 
					// remove all contributions
					deactivateContributions(activePart, true);
			} else {
				// new part is a view

			    // if old active part is a view, remove all contributions,
			    // but if old part is an editor only disable
				if (activePart != null) 
					deactivateContributions(activePart, activePart instanceof IViewPart);	

				activateContributions(newPart, true);
			}

			ArrayList newActionSets = null;
			if (isNewPartAnEditor) 
				 newActionSets = calculateActionSets(newPart, null);
			else
				 newActionSets = calculateActionSets(newPart, topEditor);
				 
			if (!updateActionSets(newActionSets));
				updateActionBars();
			
			if (isNewPartAnEditor) {
				topEditor = (IEditorPart)newPart;
			} else if (activePart == topEditor && newPart == null) {
				// since we removed all the contributions, we clear the top editor
				topEditor = null;
			}
			
			activePart = newPart;
		}

		/** 
		 * Updates the contributions given the new part as the topEditor.
		 * 
		 * @param newEditor the new top editor, may be <code>null</code>
		 */
		public void updateTopEditor(IEditorPart newEditor) {
			if (topEditor == newEditor)
				return;
				
			String oldId = null;
			if (topEditor != null)
				oldId = topEditor.getSite().getId();
			String newId = null;
			if (newEditor != null)
				newId = newEditor.getSite().getId();
			if (oldId == null ? newId == null : oldId.equals(newId)) {
				// we don't have to change anything 
				topEditor = newEditor;
				return;
			}
			
			// Remove the contributions of the old editor
			if (topEditor != null)
				deactivateContributions(topEditor, true);
				
			// Show (disabled) the contributions of the new editor
			if (newEditor != null)
				activateContributions(newEditor, false);
							
			ArrayList newActionSets = calculateActionSets(activePart, newEditor);
			if (!updateActionSets(newActionSets));
				updateActionBars();
				
			topEditor = newEditor;	
		}

		/**
		 * Activates the contributions of the given part.
		 * If <code>enable</code> is <code>true</code> the contributions are
		 * visible and enabled, otherwise they are disabled.
		 * 
		 * @param part the part whose contributions are to be activated
		 * @param enable <code>true</code> the contributions are to be enabled, 
		 *  not just visible.
 		 */
		private void activateContributions(IWorkbenchPart part, boolean enable) {
			PartSite site = (PartSite) part.getSite();
			SubActionBars actionBars = (SubActionBars) site.getActionBars();
			actionBars.activate(enable);
		}

		/**
		 * Deactivates the contributions of the given part.
		 * If <code>remove</code> is <code>true</code> the contributions are
		 * removed, otherwise they are disabled.
		 * 
		 * @param part the part whose contributions are to be deactivated
		 * @param remove <code>true</code> the contributions are to be removed, 
		 *  not just disabled.
 		 */
		private void deactivateContributions(IWorkbenchPart part, boolean remove) {
			PartSite site = (PartSite) part.getSite();
			SubActionBars actionBars = (SubActionBars) site.getActionBars();
			actionBars.deactivate(remove);
		}
	
		/**
		 * Calculates the action sets to show for the given part and editor
		 * 
		 * @param part the active part, may be <code>null</code>
		 * @param editor the current editor, may be <code>null</code>, 
		 *  may be the active part
		 * @return the new action sets
		 */
		private ArrayList calculateActionSets(IWorkbenchPart part, IEditorPart editor) {
			ArrayList newActionSets = new ArrayList();
			if (part != null) {
				IActionSetDescriptor[] partActionSets = 
					WorkbenchPlugin.getDefault().getActionSetRegistry().getActionSetsFor(
						part.getSite().getId());
				for (int i = 0; i < partActionSets.length; i++) {
					newActionSets.add(partActionSets[i]);
				}
			}
			if (editor != null && editor != part) {
				IActionSetDescriptor[] editorActionSets = 
					WorkbenchPlugin.getDefault().getActionSetRegistry().getActionSetsFor(
						editor.getSite().getId());
				for (int i = 0; i < editorActionSets.length; i++) {
					newActionSets.add(editorActionSets[i]);
				}
			}
			return newActionSets;
		}
			
		
		/**
		 * Updates the actions we are showing for the active part and current editor.
		 * 
		 * @param newActionSets the action sets to show
		 * @return  <code>true</code> if the action sets changed
		 */
		private boolean updateActionSets(ArrayList newActionSets) {
			if(actionSets.equals(newActionSets))
				return false;
				
			Perspective persp = getActivePerspective();
			if (persp == null) {
				actionSets = newActionSets;
				return false;
			}
			
			// hide the old 
			for (int i = 0; i < actionSets.size(); i++) {
				persp.hideActionSet(((IActionSetDescriptor)actionSets.get(i)).getId());
			}
			
			// show the new 
			for (int i = 0; i < newActionSets.size(); i++) {
				persp.showActionSet(((IActionSetDescriptor)newActionSets.get(i)).getId());
			}
			
			actionSets = newActionSets;
			
			window.updateActionSets(); // this calls updateActionBars
			window.firePerspectiveChanged(WorkbenchPage.this, getPerspective(), CHANGE_ACTION_SET_SHOW);
			return true;
		} 
		
	}

/**
 * Constructs a new page with a given perspective and input.
 *
 * @param w the parent window
 * @param layoutID must not be <code>null</code>
 * @param input the page input
 */
public WorkbenchPage(WorkbenchWindow w, String layoutID, IAdaptable input) 
	throws WorkbenchException
{
	super();
	if (layoutID == null)
		throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchPage.UndefinedPerspective")); //$NON-NLS-1$
	init(w, layoutID, input);
}
/**
 * Constructs an old page from data stored in a persistance file.
 *
 * @param w the parent window
 * @param memento result from previous call to saveState
 * @param input the page input
 */
public WorkbenchPage(WorkbenchWindow w, IMemento memento, IAdaptable input) 
	throws WorkbenchException
{
	super();
	if (true) {
		init(w, null, input);
		restoreState(memento);
	} else {
		deferInit(w, memento, input);
	}
}
/**
 * Activates a part.  The part will be brought to the front and given focus.
 *
 * @param part the part to activate
 */
public void activate(IWorkbenchPart part) {
	// Sanity check.
	if (!certifyPart(part))
		return;
		
	// If zoomed, unzoom.
	if (isZoomed() && partChangeAffectsZoom(part))
		zoomOut();
	
	PartPane pane = ((PartSite)part.getSite()).getPane();
	if(part instanceof MultiEditor) { 
		part = ((MultiEditor)part).getActiveEditor();
	}
			
	// Activate part.
	if(window.getActivePage() == this) {
		bringToTop(part);
		setActivePart(part);
	} else {
		activationList.setActive(part);
		activePart = part;
	}
}

/**
 * Activates a part.  The part is given focus, the pane is hilighted.
 */
private void activatePart(final IWorkbenchPart part) {
	Platform.run(new SafeRunnableAdapter(WorkbenchMessages.getString("WorkbenchPage.ErrorActivatingView")) { //$NON-NLS-1$
		public void run() {
			if (part != null) {
				part.setFocus();
				PartSite site = (PartSite)part.getSite();
				site.getPane().showFocus(true);
				updateTabList(part);
				SubActionBars bars = (SubActionBars)site.getActionBars();
				bars.partChanged(part);
			}
		}
	});
}
/**
 * Add a fast view.
 */
public void addFastView(IViewPart view) {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;
		
	// If view is zoomed unzoom.
	if (isZoomed() && partChangeAffectsZoom(view))
		zoomOut();

	// Do real work.	
	persp.addFastView(view);

	// The view is now invisible.
	// If it is active then deactivate it.
	if (view == activePart) {
		activate(activationList.getActive());
	}
		
	// Notify listeners.
	window.getShortcutBar().update(true);
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_FAST_VIEW_ADD);
}
/**
 * Adds an IPartListener to the part service.
 */
public void addPartListener(IPartListener l) {
	partListeners.addPartListener(l);
}
/**
 * Implements IWorkbenchPage
 * 
 * @see org.eclipse.ui.IWorkbenchPage#addPropertyChangeListener(IPropertyChangeListener)
 * @since 2.0
 */
public void addPropertyChangeListener(IPropertyChangeListener listener) {
	propertyChangeListeners.add(listener);
}

/*
 * (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void addSelectionListener(ISelectionListener listener) {
	selectionService.addSelectionListener(listener);
}

/*
 * (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void addSelectionListener(String partId, ISelectionListener listener) {
	selectionService.addSelectionListener(partId, listener);
}

/**
 * Moves a part forward in the Z order of a perspective so it is visible.
 *
 * @param part the part to bring to move forward
 */
public void bringToTop(IWorkbenchPart part) {
	// Sanity check.
	Perspective persp = getActivePerspective();
	if (persp == null || !certifyPart(part))
		return;
		
	// If zoomed then ignore.
	if (isZoomed() && partChangeAffectsZoom(part))
		return;

	// Move part.
	boolean broughtToTop = false;
	if (part instanceof IEditorPart) {
		broughtToTop = getEditorManager().setVisibleEditor((IEditorPart)part, false);
		if (broughtToTop) {
				actionSwitcher.updateTopEditor((IEditorPart)part);
				lastActiveEditor = null;
		}
	} else if (part instanceof IViewPart) {
		broughtToTop = persp.bringToTop((IViewPart)part);
	}
	if (broughtToTop) {
		firePartBroughtToTop(part);
	}
}
/**
 * Resets the layout for the perspective.  The active part in the old layout is activated
 * in the new layout for consistent user context.
 *
 * Assumes the busy cursor is active.
 */
private void busyResetPerspective() {
	// Always unzoom
	if (isZoomed())
		zoomOut();
		
	// Get the current perspective.
	// This describes the working layout of the page and differs from
	// the original template.
	Perspective oldPersp = getActivePerspective();

	// Map the current perspective to the original template.
	// If the original template cannot be found then it has been deleted.  In
	// that case just return. (PR#1GDSABU).
	IPerspectiveRegistry reg = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	PerspectiveDescriptor desc = (PerspectiveDescriptor)reg.findPerspectiveWithId(oldPersp.getDesc().getId());
	if (desc == null)
		desc = (PerspectiveDescriptor)reg.findPerspectiveWithId(((PerspectiveDescriptor)oldPersp.getDesc()).getOriginalId());
	if (desc == null)		
		return;

	// Create new persp from original template.
	Perspective newPersp = createPerspective(desc);
	if (newPersp == null)
		return;

	// Update the perspective list and shortcut
	perspList.swap(oldPersp, newPersp);
	IContributionItem item = window.findPerspectiveShortcut(oldPersp, this);
	SetPagePerspectiveAction action = (SetPagePerspectiveAction) ((ActionContributionItem)item).getAction();
	action.setPerspective(newPersp);

	// Install new persp.
	setPerspective(newPersp);

	IToolBarManager toolsMgr = window.getToolsManager();
	if (toolsMgr instanceof CoolBarManager) {
		CoolBarManager coolBarMgr = (CoolBarManager)toolsMgr;
		coolBarMgr.resetLayout();
	}

	// Notify listeners.
	window.firePerspectiveChanged(this, desc, CHANGE_RESET);
	
	// Destroy old persp.
	disposePerspective(oldPersp);
}
/**
 * Implements <code>setPerspective</code>.
 *
 * Assumes that busy cursor is active.
 * 
 * @param persp identifies the new perspective.
 */
private void busySetPerspective(IPerspectiveDescriptor desc) {
	// If zoomed unzoom.
	if (isZoomed())
		zoomOut();

	// Create new layout.
	PerspectiveDescriptor realDesc = (PerspectiveDescriptor)desc;
	Perspective newPersp = findPerspective(realDesc);
	if (newPersp == null) {
		newPersp = createPerspective(realDesc);
		window.addPerspectiveShortcut(newPersp, this);
		if (newPersp == null)
			return;
	}

	// Change layout.
	setPerspective(newPersp);
}
/**
 * Opens a view.
 *
 * Assumes that a busy cursor is active.
 */
private IViewPart busyShowView(String viewID, boolean activate) 
	throws PartInitException
{
	Perspective persp = getActivePerspective();
	if (persp == null)
		return null;

	// If this view is already visible just return.
	IViewPart view = persp.findView(viewID);
	if (view != null) {
		if (activate)
			activate(view);
		else
			bringToTop(view);
		return view;
	}
		
	// Show the view.  
	view = persp.showView(viewID);
	if (view != null) {
		zoomOutIfNecessary(view);
		if (activate)
			activate(view);
		else
			bringToTop(view);
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_VIEW_SHOW);
		// Just in case view was fast.
		window.getShortcutBar().update(true);
	}
	return view;
}
/**
 * Returns whether a part exists in the current page.
 */
private boolean certifyPart(IWorkbenchPart part) {
	if (part instanceof IEditorPart)
		return getEditorManager().containsEditor((IEditorPart)part);
	if (part instanceof IViewPart)
		return getActivePerspective().containsView((IViewPart)part);
	return false;
}
/**
 * Closes the perspective.  
 */
public boolean close() {
	final boolean [] ret = new boolean[1];;
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			ret[0] = window.closePage(WorkbenchPage.this, true);
		}
	});
	return ret[0];
}
/**
 * See IWorkbenchPage
 */
public boolean closeAllEditors(boolean save) {
	// If part is added / removed always unzoom.
	if (isZoomed())
		zoomOut();
		
	// Save part.
	if (save && !getEditorManager().saveAll(true, true))
		return false;

	// Deactivate part.
	boolean deactivate = activePart instanceof IEditorPart;
	if (deactivate)
		setActivePart(null);
	lastActiveEditor = null;
	actionSwitcher.updateTopEditor(null);
			
	// Close all editors.
	IEditorPart [] editors = getEditorManager().getEditors();
	getEditorManager().closeAll();
	for (int nX = 0; nX < editors.length; nX ++) {
		IEditorPart editor = editors[nX];
		activationList.remove(editor);
		firePartClosed(editor);
		editor.dispose();
	}
	if (deactivate)
		activate(activationList.getActive());
		
	// Notify interested listeners
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_CLOSE);

	// Return true on success.
	return true;
}

/**
 * See IWorkbenchPage#closeEditor
 */
public boolean closeEditor(IEditorPart editor, boolean save) {
	// Sanity check.	
	if (!certifyPart(editor))
		return false;
	
	// Save part.
	if (save && !getEditorManager().saveEditor(editor, true))
		return false;

	boolean partWasVisible = (editor == getActiveEditor());
	activationList.remove(editor);
	boolean partWasActive = (editor == activePart);

	// Deactivate part.
	if (partWasActive)
		setActivePart(null);
	if (lastActiveEditor == editor) {
		lastActiveEditor = null;
	}

	// Close the part.
	getEditorManager().closeEditor(editor);
	firePartClosed(editor);
	editor.dispose();

	// Notify interested listeners
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_CLOSE);
	
	// Activate new part.
	if (partWasActive) {
		IWorkbenchPart top = activationList.getTopEditor();
		zoomOutIfNecessary(top);
		if (top == null)
			top = activationList.getActive();
		if (top != null)
			activate(top);
		else
			setActivePart(null);
	} else if(partWasVisible) {
		IWorkbenchPart top = activationList.getTopEditor();
		zoomOutIfNecessary(top);
		if (top != null)
			bringToTop(top);
	}
	
	// Return true on success.
	return true;
}
/**
 * Closes all perspectives in the page. The page is kept so as
 * not to lose the input.
 * 
 * @param save whether the page's editors should be saved
 */
/* package */ void closeAllPerspectives(boolean save) {
	
	if (perspList.isEmpty())
		return;
		
	// Always unzoom
	if (isZoomed())
		zoomOut();
		
	// Close all editors
	if (!closeAllEditors(save))
		return;

	// Deactivate the active perspective and part
	setPerspective((Perspective)null);
	
	// Close each perspective in turn
	PerspectiveList oldList = perspList;
	perspList = new PerspectiveList();
	Iterator enum = oldList.iterator();
	while (enum.hasNext())
		closePerspective((Perspective)enum.next(), false);
}
/**
 * Closes the specified perspective. If last perspective, then
 * entire page is closed.
 * 
 * @param persp the perspective to be closed
 * @param save whether the page's editors should be save if last perspective
 */
/* package */ void closePerspective(Perspective persp, boolean save) {

	// Always unzoom
	if (isZoomed())
		zoomOut();
		
	// Close all editors on last perspective close
	if (perspList.size() == 1 && getEditorManager().getEditorCount() > 0) {
		// Close all editors
		if (!closeAllEditors(save))
			return;
	}
	
	// Dispose of the perspective
	boolean isActive = (perspList.getActive() == persp);
	window.removePerspectiveShortcut(persp, this);
	if (isActive)
		setPerspective(perspList.getNextActive());
	disposePerspective(persp);
}
/**
 * Creates the client composite.
 */
private void createClientComposite() {
	final Composite parent = window.getClientComposite();
	composite = new Composite(parent, SWT.NONE);
	composite.setVisible(false); // Make visible on activate.
	composite.setBounds(parent.getClientArea());
	resizeListener = new ControlAdapter() {
		public void controlResized(ControlEvent e) {
			composite.setBounds(parent.getClientArea());
		}
	};
	parent.addControlListener(resizeListener);
}
/**
 * Creates a new view set.  Return null on failure.
 */
private Perspective createPerspective(PerspectiveDescriptor desc) {
	try {
		Perspective persp = new Perspective(desc, this);
		perspList.add(persp);
		window.firePerspectiveOpened(this, desc);
		IViewPart parts[] = persp.getViews();
		for (int i = 0; i < parts.length; i++) {
			addPart(parts[i]);
		}
		return persp;
	} catch (WorkbenchException e) {
		return null;
	}
}
/**
 * Cycles the editors forward or backward.
 * 
 * @param forward true to cycle forward, false to cycle backward
 */
public void cycleEditors(boolean forward) {
	IEditorPart editor = activationList.cycleEditors(forward);
	if (editor != null) {
		activate(editor);
	}
}

/**
 * Open the tracker to allow the user to move
 * the specified part using keyboard.
 */
public void openTracker(ViewPane pane) {
	Perspective persp = getActivePerspective();
	if (persp != null)
		persp.openTracker(pane);
}
/**
 * Add a editor to the activation list.
 */
protected void addPart(IWorkbenchPart part) {
	activationList.add(part);
}
/**
 * Deactivate the last known active editor to force its
 * action items to be removed, not just disabled.
 */
private void deactivateLastEditor() {
	if (lastActiveEditor == null)
		return;
	PartSite site = (PartSite) lastActiveEditor.getSite();
	SubActionBars actionBars = (SubActionBars) site.getActionBars();
	actionBars.deactivate(true);
}
/**
 * Deactivates a part.  The pane is unhilighted.
 */
private void deactivatePart(IWorkbenchPart part) {
	if (part != null) {
		PartSite site = (PartSite)part.getSite();
		site.getPane().showFocus(false);
	}
}
/**
 * Cleanup.
 */
public void dispose() {
	// If we were never created just return.
	if (deferredMemento != null)
		return;
		
	// Always unzoom
	if (isZoomed())
		zoomOut();
		
	// Close and dispose the editors.
	closeAllEditors(false);

	// Capture views.
	IViewPart [] views = viewFactory.getViews();
	
	// Get rid of perspectives.  This will close the views.
	Iterator enum = perspList.iterator();
	while (enum.hasNext()) {
		Perspective perspective = (Perspective) enum.next();
		window.removePerspectiveShortcut(perspective, this);
		window.firePerspectiveClosed(this, perspective.getDesc());
		perspective.dispose();
	}
	perspList = new PerspectiveList();

	// Dispose views.
	for (int nX = 0; nX < views.length; nX ++) {
		IViewPart view = views[nX];
		firePartClosed(view);
		view.dispose();
	}
	activePart = null;
	activationList = new ActivationList();;

	// Get rid of editor presentation.
	editorPresentation.dispose();

	// Get rid of composite.
	window.getClientComposite().removeControlListener(resizeListener);
	composite.dispose();
}
/**
 * Dispose a perspective.
 */
private void disposePerspective(Perspective persp) {
	// Get views.
	IViewPart [] views = persp.getViews();
	
	// Get rid of perspective.
	perspList.remove(persp);
	window.firePerspectiveClosed(this, persp.getDesc());
	persp.dispose();

	// Loop through the views.
	for (int nX = 0; nX < views.length; nX ++) {
		IViewPart view = views[nX];
		
		// If the part is no longer reference then dispose it.
		boolean exists = viewFactory.hasView(view.getSite().getId());
		if (!exists) {
			firePartClosed(view);
			activationList.remove(view);
			view.dispose();
		}
	}
}
/**
 * Edits the action sets.
 */
public boolean editActionSets() {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return false;
		
	// Create list dialog.
	ActionSetSelectionDialog dlg =
		new ActionSetSelectionDialog(
			window.getShell(),
			persp);

	// Open.
	boolean ret = (dlg.open() == Window.OK);
	if (ret) {
		window.updateActionSets();
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_RESET);
	}
	return ret;
}
/**
 * Returns the first view manager with given ID.
 */
private Perspective findPerspective(IPerspectiveDescriptor desc) {
	Iterator enum = perspList.iterator();
	while (enum.hasNext()) {
		Perspective mgr = (Perspective)enum.next();
		if (desc.getId().equals(mgr.getDesc().getId()))
			return mgr;
	}
	return null;
}
/**
 * See IWorkbenchPage@findView.
 */
public IViewPart findView(String id) {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.findView(id);
	else
		return null;
}
/**
 * Fire part activation out.
 */
private void firePartActivated(IWorkbenchPart part) {
	partListeners.firePartActivated(part);
	selectionService.partActivated(part);
}
/**
 * Fire part brought to top out.
 */
private void firePartBroughtToTop(IWorkbenchPart part) {
	partListeners.firePartBroughtToTop(part);
	selectionService.partBroughtToTop(part);
}
/**
 * Fire part close out.
 */
private void firePartClosed(IWorkbenchPart part) {
	partListeners.firePartClosed(part);
	selectionService.partClosed(part);
}
/**
 * Fire part deactivation out.
 */
private void firePartDeactivated(IWorkbenchPart part) {
	partListeners.firePartDeactivated(part);
	selectionService.partDeactivated(part);
}
/**
 * Fire part open out.
 */
public void firePartOpened(IWorkbenchPart part) {
	partListeners.firePartOpened(part);
	selectionService.partOpened(part);
}
/**
 * Notify property change listeners about a property change.
 * 
 * @param changeId the change id
 * @param oldValue old property value
 * @param newValue new property value
 */
private void firePropertyChange(String changeId, Object oldValue, Object newValue) {
	Object[] listeners = propertyChangeListeners.getListeners();
	PropertyChangeEvent event = new PropertyChangeEvent(this, changeId, oldValue, newValue);

	for (int i = 0; i < listeners.length; i++) {
		((IPropertyChangeListener) listeners[i]).propertyChange(event);
	}
}
/*
 * Returns the action bars.
 */
public IActionBars getActionBars() {
	if (actionBars == null)
		actionBars = new WWinActionBars(window);
	return actionBars;
}
/**
 * Returns an array of the visible action sets. 
 */
public IActionSetDescriptor[] getActionSets() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getActionSets();
	else
		return new IActionSetDescriptor[0];
}
/**
 * Returns the activation list
 */
/*package*/ ActivationList getActivationList() {
	return activationList;
}
/**
 * @see IWorkbenchPage
 */
public IEditorPart getActiveEditor() {
	return getEditorManager().getVisibleEditor();
}
/*
 * Returns the active part within the <code>IWorkbenchPage</code>
 */
public IWorkbenchPart getActivePart() {
	return activePart;
}
/**
 * Returns the active perspective for the page, <code>null</code>
 * if none.
 */
/* package */ Perspective getActivePerspective() {
	return perspList.getActive();
}
/**
 * Returns the client composite.
 */
public Composite getClientComposite() {
	return composite;
}
/**
 * Answer the editor manager for this window.
 */
private EditorManager getEditorManager() {
	return editorMgr;
}
/**
 * Answer the editor presentation.
 */
public EditorPresentation getEditorPresentation() {
	return editorPresentation;
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart [] getEditors() {
	return getEditorManager().getEditors();
}
/**
 * Returns the docked views.
 */
public IViewPart [] getFastViews() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getFastViews();
	else
		return new IViewPart[0];
}
/**
 * @see IWorkbenchPage
 */
public IAdaptable getInput() {
	return input;
}
/**
 * Returns the page label.  This is a combination of the page input
 * and active perspective.
 */
public String getLabel() {
	String label = WorkbenchMessages.getString("WorkbenchPage.UnknownLabel"); //$NON-NLS-1$
	if (input != null) {
		IWorkbenchAdapter adapter = (IWorkbenchAdapter)input.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null)
			label = adapter.getLabel(input);
	}
	Perspective persp = getActivePerspective();
	if (persp != null)
		label = WorkbenchMessages.format("WorkbenchPage.PerspectiveFormat", new Object[] { label, persp.getDesc().getLabel() }); //$NON-NLS-1$
	else if (deferredActivePersp != null)
		label = WorkbenchMessages.format("WorkbenchPage.PerspectiveFormat", new Object[] { label, deferredActivePersp.getLabel() }); //$NON-NLS-1$	
	return label;
}
/**
 * Mouse down listener to hide fast view when
 * user clicks on empty editor area or sashes.
 */
protected Listener getMouseDownListener() {
	return mouseDownListener;
}
/**
 * Returns the new wizard actions the page.
 * This is List of Strings.
 */
public ArrayList getNewWizardActions() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getNewWizardActions();
	else
		return new ArrayList();
}
/**
 * Returns an iterator over the opened perspectives
 */
/* package */ Iterator getOpenedPerspectives() {
	return perspList.iterator();
}
/**
 * Returns the perspective.
 */
public IPerspectiveDescriptor getPerspective() {
	if (deferredActivePersp != null)
		return deferredActivePersp;
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getDesc();
	else
		return null;
}
/**
 * Returns the perspective actions for this page.
 * This is List of Strings.
 */
public ArrayList getPerspectiveActions() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getPerspectiveActions();
	else
		return new ArrayList();
}
/*
 * (non-Javadoc)
 * Method declared on ISelectionService
 */
public ISelection getSelection() {
	return selectionService.getSelection();
}

/*
 * (non-Javadoc)
 * Method declared on ISelectionService
 */
public ISelection getSelection(String partId) {
	return selectionService.getSelection(partId);
}


/**
 * Returns the show view actions the page.
 * This is List of Strings.
 */
public ArrayList getShowViewActions() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getShowViewActions();
	else
		return new ArrayList();
}
/**
 * Returns the unprotected window.
 */
protected WorkbenchWindow getUnprotectedWindow() {
	return window;
}
/*
 * Returns the view factory.
 */
public ViewFactory getViewFactory() {
	if (viewFactory == null) {
		viewFactory = new ViewFactory(this, 
			WorkbenchPlugin.getDefault().getViewRegistry());
	}
	return viewFactory;
}
/**
 * See IWorkbenchPage.
 */
public IViewPart [] getViews() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getViews();
	else
		return new IViewPart[0];
}
/**
 * See IWorkbenchPage.
 */
public IWorkbenchWindow getWorkbenchWindow() {
	return window;
}
/**
 * Implements IWorkbenchPage
 * 
 * @see org.eclipse.ui.IWorkbenchPage#getWorkingSet()
 * @since 2.0
 */
public IWorkingSet getWorkingSet() {
	return workingSet;
}

/**
 * @see IWorkbenchPage
 */
public void hideActionSet(String actionSetID) {
	Perspective persp = getActivePerspective();
	if (persp != null) {
		persp.hideActionSet(actionSetID);
		window.updateActionSets();
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_ACTION_SET_HIDE);
	}
}
/**
 * See IPerspective
 */
public void hideView(IViewPart view) {
	// Sanity check.	
	Perspective persp = getActivePerspective();
	if (persp == null || !certifyPart(view))
		return;
		
	// If part is added / removed always unzoom.
	if (isZoomed() && !isFastView(view))
		zoomOut();
		
	// Confirm.
	if (!persp.canCloseView(view))
		return;
		
	// Activate new part.
	if (view == activePart) {
		IWorkbenchPart prevActive = activationList.getPreviouslyActive();
		if (prevActive != null)
			activate(prevActive);
		else
			setActivePart(null);
	}
		
	// Hide the part.  
	persp.hideView(view);
	

	// If the part is no longer reference then dispose it.
	boolean exists = viewFactory.hasView(view.getSite().getId());
	if (!exists) {
		firePartClosed(view);
		view.dispose();
		activationList.remove(view);		
	}
	
	// Notify interested listeners
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_VIEW_HIDE);
	
	// Just in case view was fast.
	window.getShortcutBar().update(true);
}
/**
 * Initialize the page.
 *
 * @param w the parent window
 * @param layoutID may be <code>null</code> if restoring from file
 * @param input the page input
 */
private void init(WorkbenchWindow w, String layoutID, IAdaptable input) 
	throws WorkbenchException
{
	// Save args.
	this.window = w;
	this.input = input;

	// Mouse down listener to hide fast view when
	// user clicks on empty editor area or sashes.
	mouseDownListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.MouseDown)
				toggleFastView(null);
		}
	};
	
	// Create presentation.
	createClientComposite();
	editorPresentation = new EditorPresentation(this, mouseDownListener) ;
	editorMgr = new EditorManager(window, this, editorPresentation);
	
	// Get perspective descriptor.
	if(layoutID != null) {
		PerspectiveDescriptor desc = (PerspectiveDescriptor)WorkbenchPlugin
			.getDefault().getPerspectiveRegistry().findPerspectiveWithId(layoutID);
		if (desc == null)
			throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchPage.ErrorRecreatingPerspective")); //$NON-NLS-1$
		Perspective persp = createPerspective(desc);
		perspList.setActive(persp);
		window.firePerspectiveActivated(this, desc);
		
		// Update MRU list.
		Workbench wb = (Workbench)window.getWorkbench();
		wb.getPerspectiveHistory().add(desc);
	}
}
/**
 * Save the init parameters and defer initialization.  
 *
 * @param w the parent window
 * @param memento the persistent memento.
 * @param input the page input
 */
private void deferInit(WorkbenchWindow w, IMemento memento, IAdaptable input) 
	throws WorkbenchException
{
	// Save state.
	window = w;
	this.input = input;
	deferredMemento = memento;

	// Get active perspective.
	IMemento childMem = memento.getChild(IWorkbenchConstants.TAG_PERSPECTIVES);
	String activePerspectiveID = childMem.getString(IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE);
	PerspectiveDescriptor desc = (PerspectiveDescriptor)WorkbenchPlugin
		.getDefault().getPerspectiveRegistry().findPerspectiveWithId(activePerspectiveID);
	if (desc == null)
		throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchPage.ErrorRecreatingPerspective")); //$NON-NLS-1$
	deferredActivePersp = desc;
}
/**
 * Finish initialization if we have been deferred.
 */
private void finishInit() {
	if (deferredMemento == null) 
		return;
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			try {
				init(window, null, input);
			} catch (WorkbenchException e) {
			}
			restoreState(deferredMemento);
			deferredMemento = null;
			deferredActivePersp = null;
		}
	});
}
/**
 * See IWorkbenchPage.
 */
public boolean isEditorAreaVisible() {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return false;
	return persp.isEditorAreaVisible();
}
/**
 * Returns whether the view is fast.
 */
public boolean isFastView(IViewPart part) {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.isFastView(part);
	else
		return false;
}
/**
 * Return the active fast view or null if there are no
 * fast views or if there are all minimized.
 */
public IViewPart getActiveFastView() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getActiveFastView();
	else
		return null;
}
/**
 * Return true if the perspective has a dirty editor.
 */
protected boolean isSaveNeeded() {
	return getEditorManager().isSaveAllNeeded();
}
/**
 * Returns whether the page is zoomed.
 */
public boolean isZoomed() {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return false;
	return persp.getPresentation().isZoomed();
}
/**
 * Returns <code>true</code> if the window needs to unzoom for the given
 * IWorkbenchPart to be seen by the user. Returns false otherwise.
 * 
 * @param part the part whose visibility is to be determined
 * @return <code>true</code> if the window needs to unzoom for the given
 * 		IWorkbenchPart to be seen by the user, <code>false</code> otherwise.
 */
private boolean needToZoomOut(IWorkbenchPart part) {
	// part is an editor
	if (part instanceof IEditorPart) {
		if(getActivePart() instanceof IViewPart) {
			if(!isFastView((IViewPart)getActivePart()))
				return true;
		}
		EditorSite site = (EditorSite)part.getSite();
		EditorPane pane = (EditorPane)site.getPane();
		EditorWorkbook book = pane.getWorkbook();
		return !book.equals(book.getEditorArea().getActiveWorkbook());
	}
	// part is a view
	if(part instanceof IViewPart) {
		if(isFastView((IViewPart)part) || part.equals(getActivePart()))
			return false;
		else
			return true;
	}

	return true;
}
/**
 * This method is called when the page is activated.  
 */
protected void onActivate() {
	if (deferredMemento != null)
		finishInit();
	Iterator enum = perspList.iterator();
	while (enum.hasNext()) {
		Perspective perspective = (Perspective) enum.next();
		window.addPerspectiveShortcut(perspective, this);
	}
	composite.setVisible(true);
	Perspective persp = getActivePerspective();
	if (persp != null) {
		window.selectPerspectiveShortcut(persp, this, true);
		persp.onActivate();
	}
	if (activePart != null) {
		activationList.setActive(activePart);
		
		activatePart(activePart);
		actionSwitcher.updateActivePart(activePart);
		if (activePart instanceof IEditorPart) {
			lastActiveEditor = (IEditorPart) activePart;
			actionSwitcher.updateTopEditor((IEditorPart) activePart);
		}
		firePartActivated(activePart);
	} else {
		composite.setFocus();
	}
}
/**
 * This method is called when the page is deactivated.  
 */
protected void onDeactivate() {
	if (activePart != null) {
		deactivatePart(activePart);
		actionSwitcher.updateActivePart(null);
		firePartDeactivated(activePart);
	}
	actionSwitcher.updateTopEditor(null);
	lastActiveEditor = null;
	if (getActivePerspective() != null)
		getActivePerspective().onDeactivate();
	composite.setVisible(false);
	Iterator enum = perspList.iterator();
	while (enum.hasNext()) {
		Perspective perspective = (Perspective) enum.next();
		window.removePerspectiveShortcut(perspective, this);
	}
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IFile file) 
	throws PartInitException
{
	return openEditor(new FileEditorInput(file),null,true,false,null);
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IFile file, String editorID)
	throws PartInitException 
{
	return openEditor(new FileEditorInput(file),editorID,true,true,file);
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IFile file, String editorID,boolean activate)
	throws PartInitException 
{
	return openEditor(new FileEditorInput(file),editorID,activate,editorID != null,file);
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IMarker marker)
	throws PartInitException
{
	return openEditor(marker, true);
}
/**
 * @see IWorkbenchPage
 */
public IEditorPart openEditor(IMarker marker, boolean activate) 
	throws PartInitException 
{
	// Get the resource.
	IFile file = (IFile)marker.getResource();

	// Get the preferred editor id.
	String editorID = null;
	try {
		editorID = (String)marker.getAttribute(EDITOR_ID_ATTR);
	}
	catch (CoreException e) {
		WorkbenchPlugin.log(WorkbenchMessages.getString("WorkbenchPage.ErrorExtractingEditorIDFromMarker"), e.getStatus()); //$NON-NLS-1$
		return null;
	}
	
	// Create a new editor.
	IEditorPart editor = null;
	if (editorID == null)
		editor = openEditor(new FileEditorInput(file),null,activate,false,null);
	else 
		editor = openEditor(new FileEditorInput(file),editorID,activate,true,file);

	// Goto the bookmark.
	if (editor != null)
		editor.gotoMarker(marker);
	return editor;
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IEditorInput input, String editorID) 
	throws PartInitException
{
	return openEditor(input, editorID, true);
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IEditorInput input, String editorID, boolean activate) 
	throws PartInitException
{
	return openEditor(input,editorID,activate,true,null);
}
/**
 * See IWorkbenchPage.
 */
private IEditorPart openEditor(IEditorInput input, String editorID, boolean activate,boolean useEditorID,IFile file) 
	throws PartInitException
{	
	if(file != null) {
		// Update the default editor for this file.
		WorkbenchPlugin.getDefault().getEditorRegistry().setDefaultEditor(
			file, editorID);
	}
		
	// If an editor already exists for the input use it.
	IEditorPart editor = getEditorManager().findEditor(input);
	if (editor != null) {
		zoomOutIfNecessary(editor);
		setEditorAreaVisible(true);
		if (activate)
			activate(editor);
		else
			bringToTop(editor);
		return editor;
	}

// Disabled turning redraw off, because it causes setFocus
// in activate(editor) to fail.
// getClientComposite().setRedraw(false);

	// Remember the old visible editor 
	IEditorPart oldVisibleEditor = getEditorManager().getVisibleEditor();
	
	// Otherwise, create a new one. This may cause the new editor to
	// become the visible (i.e top) editor.
	if(useEditorID)
		editor = getEditorManager().openEditor(editorID, input);
	else
		editor = getEditorManager().openEditor((IFileEditorInput)input,true);
				
	if (editor != null) {
		//firePartOpened(editor);
		zoomOutIfNecessary(editor);
		setEditorAreaVisible(true);
		if (activate) {
			if(editor instanceof MultiEditor)
				activate(((MultiEditor)editor).getActiveEditor());
			else
				activate(editor);
		} else {
			activationList.setActive(editor);
			if (activePart != null)
				// ensure the activation list is in a valid state
				activationList.setActive(activePart);
			// The previous openEditor call may create a new editor
			// and make it visible, so send the notification.
			IEditorPart visibleEditor = getEditorManager().getVisibleEditor();
			if ((visibleEditor == editor) && (oldVisibleEditor != editor))
				firePartBroughtToTop(editor);
			else
				bringToTop(editor);
		}
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_OPEN);
	}
	
//	getClientComposite().setRedraw(true);

	return editor;
}
/**
 * See IWorkbenchPage.
 */
public boolean isEditorPinned(IEditorPart editor) {
	return !((EditorSite)editor.getEditorSite()).getReuseEditor();
}
/**
 * See IWorkbenchPage.
 */
public void openSystemEditor(IFile input) 
	throws PartInitException
{
	getEditorManager().openSystemEditor(input);
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
private boolean partChangeAffectsZoom(IWorkbenchPart part) {
	PartPane pane = ((PartSite)part.getSite()).getPane();
	return getActivePerspective().getPresentation().partChangeAffectsZoom(pane);
}
/**
 * Removes a fast view.
 */
public void removeFastView(IViewPart view) {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;

	// If parts change always update zoom.
	if (isZoomed())
		zoomOut();

	// Do real work.	
	persp.removeFastView(view);

	// Notify listeners.
	window.getShortcutBar().update(true);
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_FAST_VIEW_REMOVE);
}
/**
 * Removes an IPartListener from the part service.
 */
public void removePartListener(IPartListener l) {
	partListeners.removePartListener(l);
}

/**
 * Implements IWorkbenchPage
 * 
 * @see org.eclipse.ui.IWorkbenchPage#removePropertyChangeListener(IPropertyChangeListener)
 * @since 2.0
 */
public void removePropertyChangeListener(IPropertyChangeListener listener) {
	propertyChangeListeners.remove(listener);
}

/*
 * (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void removeSelectionListener(ISelectionListener listener) {
	selectionService.removeSelectionListener(listener);
}

/*
 * (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void removeSelectionListener(String partId, ISelectionListener listener) {
	selectionService.removeSelectionListener(partId, listener);
}

/**
 * This method is called when a part is activated by clicking within it.
 * In response, the part, the pane, and all of its actions will be activated.
 *
 * In the current design this method is invoked by the part pane
 * when the pane, the part, or any children gain focus.
 */
public void requestActivation(IWorkbenchPart part) {
	// Sanity check.
	if (!certifyPart(part))
		return;

	// Real work.
	setActivePart(part);
}
/**
 * Resets the layout for the perspective.  The active part in the old layout is activated
 * in the new layout for consistent user context.
 */
public void resetPerspective() {
	// Run op in busy cursor.
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			busyResetPerspective();
		}
	});
}
/**
 * @see IPersistable.
 */
private void restoreState(IMemento memento) {
	// Restore working set
	String workingSetName = memento.getString(IWorkbenchConstants.TAG_WORKING_SET);
	if (workingSetName != null) {
		WorkingSetManager workingSetManager = (WorkingSetManager) getWorkbenchWindow().getWorkbench().getWorkingSetManager();
		setWorkingSet(workingSetManager.getWorkingSet(workingSetName));
	}
	
	// Restore editor manager.
	IMemento childMem = memento.getChild(IWorkbenchConstants.TAG_EDITORS);
	getEditorManager().restoreState(childMem);

	// Get persp block.
	childMem = memento.getChild(IWorkbenchConstants.TAG_PERSPECTIVES);
	String activePartID = childMem.getString(IWorkbenchConstants.TAG_ACTIVE_PART);
	String activePerspectiveID = childMem.getString(IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE);
	
	// Restore perspectives.
	IMemento perspMems[]  = childMem.getChildren(IWorkbenchConstants.TAG_PERSPECTIVE);
	Perspective activePerspective = null;
	for (int i = 0; i < perspMems.length; i++) {
		try {
			Perspective persp = new Perspective(null, this);
			persp.restoreState(perspMems[i]);
			if (persp.getDesc().getId().equals(activePerspectiveID))
				activePerspective = persp;
			perspList.add(persp);
		} catch (WorkbenchException e) {
		}
	}
	perspList.setActive(activePerspective);
	
	// Make sure we have a valid perspective to work with,
	// otherwise return.
	activePerspective = perspList.getActive();
	if (activePerspective == null) {
		activePerspective = perspList.getNextActive();
		perspList.setActive(activePerspective);
	}
	if (activePerspective == null)
		return;

	window.firePerspectiveActivated(this, activePerspective.getDesc());

	// Restore active part.
	if (activePartID != null) {
		IViewPart view = activePerspective.findView(activePartID);
		if (view != null)
			activePart = view;
	}
}
/**
 * See IWorkbenchPage
 */
public boolean saveAllEditors(boolean confirm) {
	if (deferredMemento != null)
		return true;
	return getEditorManager().saveAll(confirm, false);
}
/**
 * Saves an editors in the workbench.  
 * If <code>confirm</code> is <code>true</code> the user is prompted to
 * confirm the command.
 *
 * @param confirm if user confirmation should be sought
 * @return <code>true</code> if the command succeeded, or 
 *   <code>false</code> if the user cancels the command
 */
public boolean saveEditor(org.eclipse.ui.IEditorPart editor, boolean confirm) {
	// Sanity check.
	if (!certifyPart(editor))
		return false;

	// Real work.
	return getEditorManager().saveEditor(editor, confirm);
}
/**
 * Saves the current perspective.
 */
public void savePerspective() {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;
		
	// Always unzoom.
	if (isZoomed())
		zoomOut();

	persp.saveDesc();
}
/**
 * Saves the perspective.
 */
public void savePerspectiveAs(IPerspectiveDescriptor desc) {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;
		
	// Always unzoom.
	if (isZoomed())
		zoomOut();

	persp.saveDescAs(desc);
	
	window.updatePerspectiveShortcut(persp, this);
	
	// Update MRU list.
	Workbench wb = (Workbench)window.getWorkbench();
	wb.getPerspectiveHistory().add(desc);
}
/**
 * Save the state of the page.
 */
public void saveState(IMemento memento) {
	// If we were never initialized ..
	if (deferredMemento != null) {
		XMLMemento realMemento = (XMLMemento) memento;
		IMemento child = deferredMemento.getChild(IWorkbenchConstants.TAG_EDITORS);
		realMemento.copyChild(child);
		child = deferredMemento.getChild(IWorkbenchConstants.TAG_PERSPECTIVES);
		realMemento.copyChild(child);
		return;
	}
	
	// We must unzoom to get correct layout.
	if (isZoomed())
		zoomOut();
		
	// Save editor manager.
	IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_EDITORS);
	editorMgr.saveState(childMem);

	// Create persp block.
	childMem = memento.createChild(IWorkbenchConstants.TAG_PERSPECTIVES);
	if (getPerspective() != null)
		childMem.putString(IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE, getPerspective().getId());
	if (getActivePart() != null)
	 	childMem.putString(IWorkbenchConstants.TAG_ACTIVE_PART,getActivePart().getSite().getId());

	// Save each perspective in opened order
	Iterator enum = perspList.iterator();
	while (enum.hasNext()) {
		Perspective persp = (Perspective)enum.next();
		IMemento gChildMem = childMem.createChild(IWorkbenchConstants.TAG_PERSPECTIVE);
		persp.saveState(gChildMem);
	}
	// Save working set if set
	if (workingSet != null) {
		memento.putString(IWorkbenchConstants.TAG_WORKING_SET, workingSet.getName());
	}
}
/**
 * Sets the active part.
 */
private void setActivePart(IWorkbenchPart newPart) {
	// Optimize it.
	if (activePart == newPart)
		return;

	// Notify perspective.  It may deactivate fast view.
	Perspective persp = getActivePerspective();
	if (persp != null)
		persp.partActivated(newPart);
	
	// Deactivate old part
	IWorkbenchPart oldPart = activePart;
	if (oldPart != null) {
		deactivatePart(oldPart);
	}
	
	// Set active part.
	activePart = newPart;
	if (newPart != null) {	
		activationList.setActive(newPart);
		if (newPart instanceof IEditorPart) {
			lastActiveEditor = (IEditorPart)newPart;
			editorMgr.setVisibleEditor(lastActiveEditor,true);
		}
	}
	activatePart(activePart);
	

	// Update actions
	actionSwitcher.updateActivePart(newPart);	

	// Fire notifications
	if (oldPart != null)
		firePartDeactivated(oldPart);
	if (newPart != null)
		firePartActivated(newPart);

	
}
/**
 * See IWorkbenchPage.
 */
public void setEditorAreaVisible(boolean showEditorArea) {	
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;
	if(showEditorArea == persp.isEditorAreaVisible())
		return;
	// If parts change always update zoom.
	if (isZoomed())
		zoomOut();
	// Update editor area visibility.
	if (showEditorArea) {
		persp.showEditorArea();
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_AREA_SHOW);
	} else {
		persp.hideEditorArea();
		if (activePart instanceof IEditorPart)
			setActivePart(null);
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_AREA_HIDE);
	}
}
/**
 * Sets the layout of the page. Assumes the new perspective
 * is not null. Keeps the active part if possible. Updates
 * the window menubar and toolbar if necessary.
 */
private void setPerspective(Perspective newPersp) {
	// Don't do anything if already active layout
	Perspective oldPersp = getActivePerspective();
	if (oldPersp == newPersp)
		return;
	// Save the toolbar layout for the perspective before the
	// active part is closed, so that any editor-related tool
	// items are saved as part of the layout.
	IToolBarManager toolsMgr = window.getToolsManager();
	if (toolsMgr instanceof CoolBarManager) {
		CoolBarManager coolBarMgr = (CoolBarManager)toolsMgr;
		if (oldPersp != null) {
			oldPersp.setToolBarLayout(coolBarMgr.getLayout());
		} 
	}
	
	// Deactivate active part.
	IWorkbenchPart oldActivePart = activePart;
	setActivePart(null);

	// Deactivate the old layout
	if (oldPersp != null) {
		oldPersp.onDeactivate();
		window.selectPerspectiveShortcut(oldPersp, this, false);
	}
	
	// Activate the new layout
	perspList.setActive(newPersp);
	if (newPersp != null) {
		newPersp.onActivate();

		// Notify listeners of activation
		window.firePerspectiveActivated(this, newPersp.getDesc());
	
		// Update MRU list.
		Workbench wb = (Workbench)window.getWorkbench();
		wb.getPerspectiveHistory().add(newPersp.getDesc());
	
		// Update the shortcut	
		window.selectPerspectiveShortcut(newPersp, this, true);
	} else {
		// No need to remember old active part since there
		// is no new active perspective to activate it in.
		oldActivePart = null;
	}
	
	// Update the window
	window.updateActionSets();
	window.updateTitle();
	window.getShortcutBar().update(true);
	
	// Reactivate active part.
	if (oldActivePart != null) {
		if (oldActivePart instanceof IEditorPart && isEditorAreaVisible()) {
			activate(oldActivePart);
		} else if (oldActivePart instanceof IViewPart) {
			String id = oldActivePart.getSite().getId();
			if (findView(id) != null)
				activate(oldActivePart);
		}
	}
	
	// Update the Coolbar layout.  Do this after the part is activated,
	// since the layout may contain items associated to the part.
	IToolBarManager mgr = window.getToolsManager();
	if (mgr instanceof CoolBarManager) {
		CoolBarManager cBarMgr = (CoolBarManager)mgr;
		if (newPersp != null) {
			CoolBarLayout layout = newPersp.getToolBarLayout();
			cBarMgr.setLayout(newPersp.getToolBarLayout());
		}
	}
}
/**
 * Sets the perspective.  
 * 
 * @param persp identifies the new perspective.
 */
public void setPerspective(final IPerspectiveDescriptor desc) {
	// Run op in busy cursor.
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			busySetPerspective(desc);
		}
	});
}
/**
 * Sets the active working set for the workbench page.
 * Notifies property change listener about the change.
 * 
 * @param newWorkingSet the active working set for the page.
 * 	May be null.
 * @since 2.0
 */
public void setWorkingSet(IWorkingSet newWorkingSet) {
	IWorkingSet oldWorkingSet = workingSet;

	workingSet = newWorkingSet;
	if (oldWorkingSet != newWorkingSet) {
		firePropertyChange(CHANGE_WORKING_SET_REPLACE, oldWorkingSet, newWorkingSet);
	}
	if (newWorkingSet != null) {
		WorkbenchPlugin.getDefault().getWorkingSetManager().addPropertyChangeListener(propertyChangeListener);
	}
	else {
		WorkbenchPlugin.getDefault().getWorkingSetManager().removePropertyChangeListener(propertyChangeListener);
	}
}
/**
 * @see IWorkbenchPage
 */
public void showActionSet(String actionSetID) {
	Perspective persp = getActivePerspective();
	if (persp != null) {
		persp.showActionSet(actionSetID);
		window.updateActionSets();
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_ACTION_SET_SHOW);
	}
}
/**
 * See IWorkbenchPage.
 */
public IViewPart showView(final String viewID) 
	throws PartInitException
{
	return showView(viewID, true);
}
/**
 * See IWorkbenchPage.
 */
private IViewPart showView(final String viewID, final boolean activate) 
	throws PartInitException
{
	// Run op in busy cursor.
	final Object [] result = new Object[1];
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			try {
				result[0] = busyShowView(viewID, activate);
			} catch (PartInitException e) {
				result[0] = e;
			}
		}
	});
	if (result[0] instanceof IViewPart)
		return (IViewPart)result[0];
	else if (result[0] instanceof PartInitException)
		throw (PartInitException)result[0];
	else
		throw new PartInitException(WorkbenchMessages.getString("WorkbenchPage.AbnormalWorkbenchCondition")); //$NON-NLS-1$
}
/**
 * Toggles the visibility of a fast view.  If the view is active it
 * is deactivated.  Otherwise, it is activated.
 */
public void toggleFastView(IViewPart part) {
	Perspective persp = getActivePerspective();
	if (persp != null) {
		persp.toggleFastView(part);
		// if the fast view has been deactivated
		if (part != persp.getActiveFastView()) {
			setActivePart(activationList.getPreviouslyActive());
		}
	}
}
/**
 * Zoom in on a part.  
 * If the part is already in zoom then zoom out.
 */
public void toggleZoom(IWorkbenchPart part) {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;

	 PartPane pane = ((PartSite)(part.getSite())).getPane();
	 
	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * // If target part is detached ignore.
	 * if (pane.getWindow() instanceof DetachedWindow) 
	 * 	return;
	 */
	 
	if (part instanceof IViewPart && isFastView((IViewPart)part))
		return;
		
	// Update zoom status.
	if (isZoomed()) {
		zoomOut();
		return;
	} else {
		persp.getPresentation().zoomIn(pane);
		activate(part);
	}
}
/**
 * updateActionBars method comment.
 */
public void updateActionBars() {
	window.updateActionBars();
}

/**
 * Sets the tab list of this page's composite appropriately
 * when a part is activated.
 */
private void updateTabList(IWorkbenchPart part) {
	PartSite site = (PartSite)part.getSite();
	PartPane pane = site.getPane();
	if (pane instanceof ViewPane) {
		ViewPane viewPane = (ViewPane) pane;
		Control[] tabList = viewPane.getTabList();
		/*
		 * Detached window no longer supported - remove when confirmed
		 *
		 * if (pane.getWindow() instanceof DetachedWindow) {
		 * 	viewPane.getControl().getShell().setTabList(tabList);
		 * }
		 * else {
		 */
		getClientComposite().setTabList(tabList);
		/*}*/
	}
	else if (pane instanceof EditorPane) {
		EditorArea ea = ((EditorPane) pane).getWorkbook().getEditorArea();
		ea.updateTabList();
		getClientComposite().setTabList(new Control[] { ea.getParent() });
	}
}

/**
 * The title of the given part has changed.
 * For views, updates the fast view button if necessary.
 */
public void updateTitle(IWorkbenchPart part) {
	if (part instanceof IViewPart) {
		if (isFastView((IViewPart) part)) {
			// Would be more efficient to just update label of single tool item
			// but we don't have access to it from here.
			window.getShortcutBar().update(true);
		}
	}
}
/**
 * Zooms out a zoomed in part.
 */
/*package*/ void zoomOut() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		persp.getPresentation().zoomOut();
}
/**
 * Zooms out a zoomed in part if it is necessary to do so for the user
 * to view the IWorkbenchPart that is the argument. Otherwise, does nothing.
 * 
 * @param part the part to be made viewable
 */
private void zoomOutIfNecessary(IWorkbenchPart part) {
	if (isZoomed() && needToZoomOut(part))
		zoomOut();	
}
/**
 * @see IPageLayout.
 */
public int getEditorReuseThreshold() {
	Perspective persp = getActivePerspective();
	if (persp != null) {
		int result = persp.getEditorReuseThreshold();
		if(result > 0)
			return result;
	}
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();		
	return store.getInt(IPreferenceConstants.REUSE_EDITORS);
}
/**
 * @see IPageLayout.
 */
public void setEditorReuseThreshold(int openEditors) {
	Perspective persp = getActivePerspective();
	if (persp != null)
		persp.setEditorReuseThreshold(openEditors);
}
/*
 * Returns the editors in activation order (oldest first).
 */
public IEditorPart[] getSortedEditors() {
	ArrayList editors = activationList.getEditors();
	IEditorPart[] result = new IEditorPart[editors.size()];
	return (IEditorPart[])editors.toArray(result);
}
/*
 * Returns the parts in activation order (oldest first).
 */
public IWorkbenchPart[] getSortedParts() {
	return activationList.getParts();
}

class ActivationList {
	//List of parts in the activation order (oldest first)
	List parts = new ArrayList();
	/*
	 * Add/Move the active part to end of the list;
	 */
	void setActive(IWorkbenchPart part) {
		if(parts.size() > 0 && part == parts.get(parts.size() - 1))
			return;
		PartPane pane = ((PartSite)part.getSite()).getPane();
		if(pane instanceof MultiEditorInnerPane) {
			MultiEditorInnerPane innerPane = (MultiEditorInnerPane)pane;
			setActive(innerPane.getParentPane().getPart());
		} else {
			parts.remove(part);
			parts.add(part);
		}
	}
	/*
	 * Add the active part to the beginning of the list.
	 */
	void add(IWorkbenchPart part) {
		if(parts.indexOf(part) >= 0)
			return;
		PartPane pane = ((PartSite)part.getSite()).getPane();
		if(pane instanceof MultiEditorInnerPane) {
			MultiEditorInnerPane innerPane = (MultiEditorInnerPane)pane;
			add(innerPane.getParentPane().getPart());
		} else {
			parts.add(0,part);
		}
	}
	/*
	 * Return the active part. Filter fast views.
	 */
	IWorkbenchPart getActive() {
		if(parts.isEmpty())
			return null;
		return getActive(parts.size() - 1);	
	}
	/*
	 * Return the previously active part. Filter fast views.
	 */
	IWorkbenchPart getPreviouslyActive() {
		if(parts.size() < 2) 
			return null;
		return getActive(parts.size() - 2);	
	} 
	/*
	 * Find a part in the list starting from the end and
	 * filter fast views.
	 */	
	IWorkbenchPart getActive(int start) {
		IViewPart[] views = getViews();
		for (int i = start; i >= 0; i--) {
			IWorkbenchPart part = (IWorkbenchPart)parts.get(i);
			if(part instanceof IViewPart) {
				if(!isFastView((IViewPart)part)) {
					for (int j = 0; j < views.length; j++) {
						if(views[j] == part) {
							return part;
						}
					}
				}
			} else {
				return part;
			}
		}
		return null;
	}
	/*
	 * Retuns the index of the part within the activation
	 * list. The higher the index, the more recent it
	 * was used.
	 */
	int indexOf(IWorkbenchPart part) {
		return parts.indexOf(part);
	}
	/*
	 * Remove a part from the list
	 */
	boolean remove(Object part) {
		return parts.remove(part);
	}

	/*
	 * Returns the editors in activation order (oldest first).
	 */
	private ArrayList getEditors() {
		ArrayList editors = new ArrayList(parts.size());
		for (Iterator i = parts.iterator(); i.hasNext();) {
			IWorkbenchPart part = (IWorkbenchPart) i.next();
			if (part instanceof IEditorPart) {
				editors.add(part);
			}
		}
		return editors;
	}
	/*
	 * Return a list with all parts (editors and views).
	 */
	private IWorkbenchPart[] getParts() {
		IViewPart[] views = getViews();
		ArrayList resultList = new ArrayList(parts.size());
		for (Iterator iterator = parts.iterator(); iterator.hasNext();) {
			IWorkbenchPart part = (IWorkbenchPart)iterator.next();
			if(part instanceof IViewPart) {
				for (int i = 0; i < views.length; i++) {
					if(views[i] == part) {
						resultList.add(part);
						break;
					}
				}
			} else {
				resultList.add(part);	
			}	
		}
		IWorkbenchPart[] result = new IWorkbenchPart[resultList.size()];
		return (IWorkbenchPart[])resultList.toArray(result);
	}
	/*
	 * Cycles the editors forward or backward, returning the editor to activate,
	 * or null if none.
	 * 
	 * @param forward true to cycle forward, false to cycle backward
	 */
	IEditorPart cycleEditors(boolean forward) {
		ArrayList editors = getEditors();
		if (editors.size() >= 2) {
			if (forward) {
				// move the topmost editor to the bottom
				IEditorPart top = (IEditorPart) editors.get(editors.size()-1);
				parts.remove(top);
				parts.add(0, top);
				// get the next editor and move it on top of any views
				IEditorPart next = (IEditorPart) editors.get(editors.size()-2);
				setActive(next);
				return next;
			} else {
				// move the bottom-most editor to the top
				IEditorPart prev = (IEditorPart) editors.get(0);
				setActive(prev);
				return prev;
			}
		}
		return null;
	}
	
	/*
	 * Returns the topmost editor on the stack, or null if none.
	 */
	IEditorPart getTopEditor() {
		ArrayList editors = getEditors();
		if (editors.size() > 0) {
			return (IEditorPart) editors.get(editors.size()-1);
		}
		return null;
	}
}

	/**
	 * Helper class to keep track of all opened perspective.
	 * Both the opened and used order is kept.
	 */
	class PerspectiveList {
		/**
		 * List of perspectives in the order they were opened;
		 */
		private List openedList;
		
		/**
		 * List of perspectives in the order they were used.
		 * Last element is the most recently used, and first element
		 * is the least recently used.
		 */
	 	private List usedList;
	 	
	 	/**
	 	 * The perspective explicitly set as being the active one
	 	 */
	 	private Perspective active;
	 	
	 	/**
	 	 * Creates an empty instance of the perspective list
	 	 */
		public PerspectiveList() {
			openedList = new ArrayList(15);
	 		usedList = new ArrayList(15);
		}
		
		/**
		 * Adds a perspective to the list. No check is done
		 * for a duplicate when adding.
		 */
		public boolean add(Perspective perspective) {
			openedList.add(perspective);
			usedList.add(0, perspective); //It will be moved to top only when activated.
			return true;
		}
		
		/**
		 * Returns an iterator on the perspective list
		 * in the order they were opened.
		 */
		public Iterator iterator() {
			return openedList.iterator();
		}
		
		/**
		 * Checks if the specified perspective exists in the
		 * list already.
		 */
		public boolean contains(Perspective perspective) {
			return openedList.contains(perspective);
		}
		
		/**
		 * Removes a perspective from the list.
		 */
		public boolean remove(Perspective perspective) {
			if (active == perspective)
				active = null;
			usedList.remove(perspective);
			return openedList.remove(perspective);
		}

		/**
		 * Swap the opened order of old perspective with the
		 * new perspective.
		 */
		public void swap(Perspective oldPerspective, Perspective newPerspective) {
			int oldIndex = openedList.indexOf(oldPerspective);
			int newIndex = openedList.indexOf(newPerspective);
			
			if (oldIndex < 0 || newIndex < 0) 
				return;
				
			openedList.set(oldIndex, newPerspective);
			openedList.set(newIndex, oldPerspective);
		}
				
		/**
		 * Returns whether the list contains any perspectives
		 */
		public boolean isEmpty() {
			return openedList.isEmpty();
		}
		
		/**
		 * Returns the most recently used perspective in
		 * the list.
		 */
		public Perspective getActive() {
			return active;
		}
		
		/**
		 * Returns the next most recently used perspective in
		 * the list.
		 */
		public Perspective getNextActive() {
			if (active == null) {
				if (usedList.isEmpty())
					return null;
				else
					return (Perspective)usedList.get(usedList.size() - 1);
			} else {
				if (usedList.size() < 2)
					return null;
				else
					return (Perspective)usedList.get(usedList.size() - 2);
			}
		}
		
		/**
		 * Returns the number of perspectives opened
		 */ 
		public int size() {
			return openedList.size();
		}
		
		/**
		 * Marks the specified perspective as the most
		 * recently used one in the list.
		 */
		public void setActive(Perspective perspective) {
			if (perspective == active)
				return;
				
			active = perspective;
			
			if (perspective != null) {
				usedList.remove(perspective);
				usedList.add(perspective);
			}
		}
	}
}
