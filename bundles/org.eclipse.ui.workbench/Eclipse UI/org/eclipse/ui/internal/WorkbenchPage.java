/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.dialogs.ActionSetSelectionDialog;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditor;
 
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
	private PartListenerList2 partListeners2 = new PartListenerList2();
	private ListenerList propertyChangeListeners = new ListenerList();
	private PageSelectionService selectionService = new PageSelectionService(this);
	private IActionBars actionBars;
	private ViewFactory viewFactory;
	private PerspectiveList perspList = new PerspectiveList();
	private PerspectiveDescriptor deferredActivePersp;
	private NavigationHistory navigationHistory = new NavigationHistory(this);	
	private IPropertyChangeListener propertyChangeListener= new IPropertyChangeListener() {
		/*
		 * Remove the working set from the page if the working set is deleted.
		 */
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();
			if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property) && 
				event.getOldValue().equals(workingSet)) {
				setWorkingSet(null);
			} else if(LayoutPart.PROP_VISIBILITY.equals(property)) {
				WorkbenchPartReference ref = (WorkbenchPartReference)((PartPane)event.getSource()).getPartReference();
				//Make sure the new visible part is restored.
				ref.getPart(Boolean.TRUE.equals(event.getNewValue()));
				if(ref == null)
					return;
				if(Boolean.TRUE.equals(event.getNewValue())) {
					String label = "visible::" + ref.getTitle(); //$NON-NLS-1$
					try {
						UIStats.start(UIStats.NOTIFY_PART_LISTENERS,label);
						partListeners2.firePartVisible(ref);
					} finally {
						UIStats.end(UIStats.NOTIFY_PART_LISTENERS,label);
					}
				} else {
					String label = "hidden::" + ref.getTitle(); //$NON-NLS-1$
					try {
						UIStats.start(UIStats.NOTIFY_PART_LISTENERS,label);
						partListeners2.firePartHidden(ref);
					} finally {
						UIStats.end(UIStats.NOTIFY_PART_LISTENERS,label);
					}
				}			
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
			if (isNewPartAnEditor || (activePart == topEditor && newPart == null)) 
				 newActionSets = calculateActionSets(newPart, null);
			else
				 newActionSets = calculateActionSets(newPart, topEditor);
				 
			if (!updateActionSets(newActionSets))
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
			if (!updateActionSets(newActionSets))
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
 * Constructs a page.
 * <code>restoreState(IMemento)</code>should be called to restore this page
 * from data stored in a persistance file.
 *
 * @param w the parent window
 * @param input the page input
 */
public WorkbenchPage(WorkbenchWindow w, IAdaptable input) 
	throws WorkbenchException
{
	super();
	init(w, null, input);
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
	
	if(window.isClosing())
		return;
		
	// If zoomed, unzoom.
	if (isZoomed() && partChangeAffectsZoom(getReference(part)))
		zoomOut();
	
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
	Platform.run(new SafeRunnable(WorkbenchMessages.getString("WorkbenchPage.ErrorActivatingView")) { //$NON-NLS-1$
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
public void addFastView(IViewReference ref) {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;
		
	// If view is zoomed unzoom.
	if (isZoomed() && partChangeAffectsZoom(ref))
		zoomOut();

	// Do real work.	
	persp.addFastView(ref);

	// The view is now invisible.
	// If it is active then deactivate it.
	if (ref.getPart(false) == activePart) {
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
 * Adds an IPartListener to the part service.
 */
public void addPartListener(IPartListener2 l) {
	partListeners2.addPartListener(l);
}
/**
 * Implements IWorkbenchPage
 * 
 * @see org.eclipse.ui.IWorkbenchPage#addPropertyChangeListener(IPropertyChangeListener)
 * @since 2.0
 * @deprecated individual views should store a working set if needed and
 * 	register a property change listener directly with the working set manager
 * 	to receive notification when the view working set is removed.
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
/*
 * (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void addPostSelectionListener(ISelectionListener listener) {
	selectionService.addPostSelectionListener(listener);
}

/*
 * (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void addPostSelectionListener(String partId, ISelectionListener listener) {
	selectionService.addPostSelectionListener(partId, listener);
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
	if (isZoomed() && partChangeAffectsZoom(getReference(part)))
		return;
	
	String label = part != null ? part.getTitle() : "none"; //$NON-NLS-1$
	boolean broughtToTop = false;
	try {
		UIStats.start(UIStats.BRING_PART_TO_TOP,label);
		// Move part.
		if (part instanceof IEditorPart) {
			IEditorReference ref = (IEditorReference)getReference(part);
			broughtToTop = getEditorManager().setVisibleEditor(ref, false);
			actionSwitcher.updateTopEditor((IEditorPart)part);
			if (broughtToTop) {
				lastActiveEditor = null;
			}
		} else if (part instanceof IViewPart) {
			IViewReference ref = (IViewReference)getReference(part);
			broughtToTop = persp.bringToTop(ref);
		}

		if (broughtToTop) {
			firePartBroughtToTop(part);
		}
	} finally {
		UIStats.end(UIStats.BRING_PART_TO_TOP,label);
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

	IContributionItem item = window.findPerspectiveShortcut(oldPersp.getDesc(), this);
	if(item == null)
		return;
		
	// Create new persp from original template.
	Perspective newPersp = createPerspective(desc);
	if (newPersp == null)
		return;
	
	// Update the perspective list and shortcut
	perspList.swap(oldPersp, newPersp);
	
	SetPagePerspectiveAction action = (SetPagePerspectiveAction) ((ActionContributionItem)item).getAction();
	action.setPerspective(newPersp.getDesc());

	// Install new persp.
	setPerspective(newPersp);

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
	// Create new layout.
	String label = desc.getId();
	try {
		UIStats.start(UIStats.SWITCH_PERSPECTIVE,label);
		PerspectiveDescriptor realDesc = (PerspectiveDescriptor)desc;
		Perspective newPersp = findPerspective(realDesc);
		if (newPersp == null) {
			newPersp = createPerspective(realDesc);
			if (newPersp == null)
				return;
			window.addPerspectiveShortcut(realDesc, this);			
		}
	
		// Change layout.
		setPerspective(newPersp);
	} finally {
		UIStats.end(UIStats.SWITCH_PERSPECTIVE,label);		
	}
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
	IViewReference ref = persp.findView(viewID);
	IViewPart view = null;
	if(ref != null)
		view = ref.getView(true);
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
	//Workaround for bug 22325
	if (part != null && !(part.getSite() instanceof PartSite))
		return false;
		
	if (part instanceof IEditorPart) {
		IEditorReference ref = (IEditorReference)getReference(part);
		return getEditorManager().containsEditor(ref);
	}
	if (part instanceof IViewPart) {
		Perspective persp = getActivePerspective();
		return persp != null && persp.containsView((IViewPart)part);
	}
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
public boolean closeAllSavedEditors() {
	// If part is added / removed always unzoom.
	if (isZoomed())
		zoomOut();
		
	boolean deactivated = false;
			
	// Close all editors.
	IEditorReference editors[] = getEditorReferences();
	for (int i = 0; i < editors.length; i ++) {
		IEditorReference editor = editors[i];
		IWorkbenchPart part = editor.getPart(false);
		if(!editor.isDirty()) {
			if (part == activePart) {
				deactivated = true;
				setActivePart(null);
			} else if (lastActiveEditor == part) {
				lastActiveEditor = null;
				actionSwitcher.updateTopEditor(null);
			}
			getEditorManager().closeEditor(editor);
			activationList.remove(editor);
			firePartClosed(editor);
			disposePart(editor);
		}
	}
	if (deactivated)
		activate(activationList.getActive());
		
	// Notify interested listeners
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_CLOSE);

	//if it was the last part, close the perspective
	lastPartClosePerspective();

	// Return true on success.
	return true;
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
	IEditorReference[] editors = getEditorManager().getEditors();
	getEditorManager().closeAll();
	for (int i = 0; i < editors.length; i ++) {
		IEditorReference editor = editors[i]; 
		activationList.remove(editor);
		firePartClosed(editor);
		disposePart(editor);		
	}
	
	
	if (!window.isClosing() && deactivate)
		activate(activationList.getActive());
		
	// Notify interested listeners
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_CLOSE);

	//if it was the last part, close the perspective
	lastPartClosePerspective();

	// Return true on success.
	return true;
}
/**
 * See IWorkbenchPage#closeEditor
 */
public boolean closeEditor(IEditorReference editorRef,boolean save) {
	IEditorPart editor = editorRef.getEditor(false);
	if(editor != null)
		return closeEditor(editor,save);
	getEditorManager().closeEditor(editorRef);
	activationList.remove(editorRef);
	firePartClosed(editorRef);
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
	IEditorReference ref = (IEditorReference)getReference(editor);
	activationList.remove(ref);
	boolean partWasActive = (editor == activePart);

// Removing following lines to fix:
// http://dev.eclipse.org/bugs/show_bug.cgi?id=28031
//	// Deactivate part.
//	if (partWasActive)
//		setActivePart(null);
//	if (lastActiveEditor == editor) {
//		actionSwitcher.updateTopEditor(null);
//		lastActiveEditor = null;
//	}

	// Close the part.
	getEditorManager().closeEditor(ref);
	firePartClosed(ref);
	disposePart(ref);
	// Notify interested listeners
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_CLOSE);
	
	// Activate new part.
	if (partWasActive) {
		IWorkbenchPart top = activationList.getTopEditor();
		zoomOutIfNecessary(top);
		if (top == null) {
			// Fix for bug #31122 (side effect from fix 28031 above)
			actionSwitcher.updateTopEditor(null);
			if (lastActiveEditor == editor)
				lastActiveEditor = null;
			// End - Fix for bug #31122
			top = activationList.getActive();
		}
		if (top != null)
			activate(top);
		else
			setActivePart(null);
	} else if(partWasVisible) {
		IEditorPart top = activationList.getTopEditor();
		zoomOutIfNecessary(top);

		// The editor we are bringing to top may already the visible
		// editor (due to editor manager behavior when it closes and editor).
		// If this is the case, bringToTop will not call firePartBroughtToTop.
		// We must fire it from here.
		if (top != null) {
			boolean isTop = editorMgr.getVisibleEditor() == top;
			bringToTop(top);
			if (isTop)
				firePartBroughtToTop(top);
		}
		else
			actionSwitcher.updateTopEditor(top);
	}
	
	//if it was the last part, close the perspective
	lastPartClosePerspective();
	
	// Return true on success.
	return true;
}
/**
 * Closes the specified perspective. If last perspective, then
 * entire page is closed.
 * 
 * @param desc the descriptor of the perspective to be closed
 * @param save whether the page's editors should be save if last perspective
 */
/* package */ void closePerspective(IPerspectiveDescriptor desc, boolean save) {
	Perspective persp = findPerspective(desc);
	if(persp != null)
		closePerspective(persp,save,true);
}

/**
 * Closes the specified perspective. If last perspective, then
 * entire page is closed.
 * 
 * @param persp the perspective to be closed
 * @param save whether the page's editors should be save if last perspective
 */
/* package */ void closePerspective(Perspective persp, boolean save, boolean closePage) {

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
	window.removePerspectiveShortcut(persp.getDesc(), this);
	if (isActive)
		setPerspective(perspList.getNextActive());
	disposePerspective(persp);
	if (closePage && perspList.size() == 0)
		close();
}

/**
 * Closes all perspectives in the page. The page is kept so as
 * not to lose the input.
 * 
 * @param save whether the page's editors should be saved
 */
/* package */ void closeAllPerspectives() {
	
	if (perspList.isEmpty())
		return;
		
	// Always unzoom
	if (isZoomed())
		zoomOut();
		
	// Close all editors
	if (!closeAllEditors(true))
		return;

	// Deactivate the active perspective and part
	setPerspective((Perspective)null);
	
	// Close each perspective in turn
	PerspectiveList oldList = perspList;
	perspList = new PerspectiveList();
	Iterator enum = oldList.iterator();
	while (enum.hasNext())
		closePerspective((Perspective)enum.next(), false,false);
	close();
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
	String label = desc.getId();
	try {
		UIStats.start(UIStats.CREATE_PERSPECTIVE,label);
		Perspective persp = new Perspective(desc, this);
		perspList.add(persp);
		window.firePerspectiveOpened(this, desc);
		IViewReference refs[] = persp.getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			IViewReference ref = refs[i];
			if(ref != null)
				addPart(ref);			
		}
		return persp;
	} catch (WorkbenchException e) {
		if(!((Workbench)window.getWorkbench()).isStarting()) {
			MessageDialog.openError(
				window.getShell(), 
				WorkbenchMessages.getString("Error"), //$NON-NLS-1$
				WorkbenchMessages.format("Workbench.showPerspectiveError",new String[]{desc.getId()})); //$NON-NLS-1$
		}
		return null;
	} finally {
		UIStats.end(UIStats.CREATE_PERSPECTIVE,label);
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
 * Add a part to the activation list.
 */
protected void addPart(IWorkbenchPartReference ref) {
	activationList.add(ref);
}
/**
 * Remove a part from the activation list.
 */
protected void removePart(IWorkbenchPartReference ref) {
	activationList.remove(ref);
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
private void disposePart(IWorkbenchPartReference ref) {
	final WorkbenchPartReference ref0 = (WorkbenchPartReference)ref;
	Platform.run(new SafeRunnable() {
		public void run() {
			ref0.dispose();
		}
		public void handleException(Throwable e) {
			//Exception has already being logged by Core. Do nothing.
		}
	});
}
/**
 * Cleanup.
 */
public void dispose() {
		
	// Always unzoom
	if (isZoomed())
		zoomOut();
		
	// Close and dispose the editors.
	closeAllEditors(false);

	// Capture views.
	IViewReference refs[] = viewFactory.getViews();
	
	// Get rid of perspectives.  This will close the views.
	Iterator enum = perspList.iterator();
	while (enum.hasNext()) {
		Perspective perspective = (Perspective) enum.next();
		window.removePerspectiveShortcut(perspective.getDesc(), this);
		window.firePerspectiveClosed(this, perspective.getDesc());
		perspective.dispose();
	}
	perspList = new PerspectiveList();

	// Dispose views.
	final int errors[] = {0};
	for (int i = 0; i < refs.length; i ++) {
		final WorkbenchPartReference ref = (WorkbenchPartReference)refs[i];
		firePartClosed(refs[i]);
		Platform.run(new SafeRunnable() {
			public void run() {
				ref.dispose();
			}
			public void handleException(Throwable e) {
				errors[0]++;
			}
		});
	}
	if (errors[0] > 0) {
		String message;
		if (errors[0] == 1)
			message = WorkbenchMessages.getString("WorkbenchPage.oneErrorClosingPage"); //$NON-NLS-1$
		else
			message = WorkbenchMessages.getString("WorkbenchPage.multipleErrorsRestoring"); //$NON-NLS-1$
		MessageDialog.openError(null, WorkbenchMessages.getString("Error"), message); //$NON-NLS-1$
	}
	activePart = null;
	activationList = new ActivationList();;

	// Get rid of editor presentation.
	editorPresentation.dispose();

	// Get rid of composite.
	window.getClientComposite().removeControlListener(resizeListener);
	composite.dispose();
	
	navigationHistory.dispose();
}
/**
 * Dispose a perspective.
 */
private void disposePerspective(Perspective persp) {
	// Get views.
	IViewReference refs[] = persp.getViewReferences();
	
	// Get rid of perspective.
	perspList.remove(persp);
	window.firePerspectiveClosed(this, persp.getDesc());
	persp.dispose();

	// Loop through the views.
	for (int i = 0; i < refs.length; i ++) {
		IViewReference ref = refs[i];
		
		//If the part is no longer reference then dispose it.
		boolean exists = viewFactory.hasView(ref.getId());
		if (!exists) {
			firePartClosed(ref);
			activationList.remove(ref);
			disposePart(ref);
		}
	}
}
/**
 *
 * @return NavigationHistory
 */
public INavigationHistory getNavigationHistory() {
	return navigationHistory;
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
public Perspective findPerspective(IPerspectiveDescriptor desc) {
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
	if (persp == null)
		return null;
	IViewReference ref = persp.findView(id);
	if(ref == null)
		return null;
			
	// Create the control first - needed for fast views only
	IViewPart view = ref.getView(true);
	ViewPane pane = (ViewPane)((WorkbenchPartReference)ref).getPane();	
	Control ctrl = pane.getControl();
	if(ctrl == null)
		pane.createControl(getClientComposite());
	return view;
}
/**
 * Fire part activation out.
 */
private void firePartActivated(IWorkbenchPart part) {
	String label = "activate::" + (part != null ? part.getTitle() : "none"); //$NON-NLS-1$ //$NON-NLS-2$
	try {
		UIStats.start(UIStats.NOTIFY_PART_LISTENERS,label);
		partListeners.firePartActivated(part);
		partListeners2.firePartActivated(getReference(part));
		selectionService.partActivated(part);
	} finally {
		UIStats.end(UIStats.NOTIFY_PART_LISTENERS,label);
	}
}
/**
 * Fire part brought to top out.
 */
private void firePartBroughtToTop(IWorkbenchPart part) {
	String label = "bringToTop::" + (part != null ? part.getTitle() : "none"); //$NON-NLS-1$ //$NON-NLS-2$
	try {
		UIStats.start(UIStats.NOTIFY_PART_LISTENERS,label);
		partListeners.firePartBroughtToTop(part);
		partListeners2.firePartBroughtToTop(getReference(part));
		selectionService.partBroughtToTop(part);
	} finally {
		UIStats.end(UIStats.NOTIFY_PART_LISTENERS,label);
	}		
}
/**
 * Fire part close out.
 */
private void firePartClosed(IWorkbenchPartReference ref) {
	String label = "close" + ref.getTitle(); //$NON-NLS-1$
	try {
		UIStats.start(UIStats.NOTIFY_PART_LISTENERS,label);		
		IWorkbenchPart part = ref.getPart(false);
		if(part != null) {
			partListeners.firePartClosed(part);
			selectionService.partClosed(part);
		}
		partListeners2.firePartClosed(ref);
	} finally {
		UIStats.end(UIStats.NOTIFY_PART_LISTENERS,label);
	}		
}
/**
 * Fire part deactivation out.
 */
private void firePartDeactivated(IWorkbenchPart part) {
	String label = "deactivate" + (part != null ? part.getTitle() : "none"); //$NON-NLS-1$ //$NON-NLS-2$
	try {
		UIStats.start(UIStats.NOTIFY_PART_LISTENERS,label);		
		partListeners.firePartDeactivated(part);
		partListeners2.firePartDeactivated(getReference(part));
		selectionService.partDeactivated(part);
	} finally {
		UIStats.end(UIStats.NOTIFY_PART_LISTENERS,label);
	}		
}
/**
 * Fire part open out.
 */
public void firePartOpened(IWorkbenchPart part) {
	String label = "deactivate" + (part != null ? part.getTitle() : "none"); //$NON-NLS-1$ //$NON-NLS-2$
	try {
		UIStats.start(UIStats.NOTIFY_PART_LISTENERS,label);		
		partListeners.firePartOpened(part);
		partListeners2.firePartOpened(getReference(part));
		selectionService.partOpened(part);
	} finally {
		UIStats.end(UIStats.NOTIFY_PART_LISTENERS,label);
	}		
}
/**
 * Fire part input changed out.
 */
private void firePartInputChanged(IWorkbenchPart part) {
	String label = "inputChanged" + (part != null ? part.getTitle() : "none"); //$NON-NLS-1$ //$NON-NLS-2$
	try {
		UIStats.start(UIStats.NOTIFY_PART_LISTENERS,label);			
		partListeners2.firePartInputChanged(getReference(part));
		selectionService.partInputChanged(part);
	} finally {
		UIStats.end(UIStats.NOTIFY_PART_LISTENERS,label);
	}		
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
 * @see IWorkbenchPage
 */
public IEditorPart getActiveEditor() {
	return getEditorManager().getVisibleEditor();
}
/*
 * (non-Javadoc)
 * Method declared on IPartService
 */
public IWorkbenchPart getActivePart() {
	return activePart;
}
/*
 * (non-Javadoc)
 * Method declared on IPartService
 */
public IWorkbenchPartReference getActivePartReference() {
	return getReference(activePart);
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
 * Answer the perspective presentation.
 */
public PerspectivePresentation getPerspectivePresentation() {
	if(getActivePerspective() != null)
		return getActivePerspective().getPresentation();
	return null;
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
	final IEditorReference refs[] = getEditorReferences();
	final ArrayList result = new ArrayList(refs.length);
	Display d = getWorkbenchWindow().getShell().getDisplay();
	//Must be backward compatible.
	d.syncExec(new Runnable() {
		public void run() {
			for (int i = 0; i < refs.length; i++) {
				IWorkbenchPart part = refs[i].getPart(true);
				if(part != null)
					result.add(part);
			}
		}
	});
	final IEditorPart editors[] = new IEditorPart[result.size()];	
	return (IEditorPart[])result.toArray(editors);
}

public IEditorPart[] getDirtyEditors() {
	return getEditorManager().getDirtyEditors();
}
public IEditorPart findEditor(IEditorInput input) {
	return getEditorManager().findEditor(input);
}
/**
 * See IWorkbenchPage.
 */
public IEditorReference[] getEditorReferences() {
	return getEditorManager().getEditors();
}
/**
 * Returns the docked views.
 */
public IViewReference [] getFastViews() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getFastViews();
	else
		return new IViewReference[0];
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
 * Returns the new wizard actions the page.
 * This is List of Strings.
 */
public ArrayList getNewWizardActionIds() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getNewWizardActionIds();
	else
		return new ArrayList();
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
public ArrayList getPerspectiveActionIds() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getPerspectiveActionIds();
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
 * Returns the ids of the parts to list in the Show In... prompter.
 * This is a List of Strings.
 */
public ArrayList getShowInPartIds() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getShowInPartIds();
	else
		return new ArrayList();
}

/**
 * The user successfully performed a Show In... action on the specified part.
 * Update the list of Show In items accordingly.
 */
public void performedShowIn(String partId) {
	Perspective persp = getActivePerspective();
	if (persp != null) {
		persp.performedShowIn(partId);
	}
}

/**
 * Returns the show view actions the page.
 * This is a List of Strings.
 */
public ArrayList getShowViewActionIds() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getShowViewActionIds();
	else
		return new ArrayList();
}
/*
 * Returns the toolbar layout for the active perspective.
 */
public CoolBarLayout getToolBarLayout() {
	Perspective persp = getActivePerspective();
	if (persp != null) return persp.getToolBarLayout();
	return null;
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
public IViewReference[] getViewReferences() {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.getViewReferences();
	else
		return new IViewReference[0];
}
/**
 * See IWorkbenchPage.
 */
public IViewPart [] getViews() {
	Perspective persp = getActivePerspective();
	if (persp != null) {
		IViewReference refs[] = persp.getViewReferences();
		ArrayList parts = new ArrayList(refs.length);
		for (int i = 0; i < refs.length; i++) {
			IWorkbenchPart part = refs[i].getPart(true);
			if(part != null)
				parts.add(part);
		}
		IViewPart[] result = new IViewPart[parts.size()];	
		return (IViewPart[])parts.toArray(result);
	}
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
 * @deprecated individual views should store a working set if needed
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

public void hideView(IViewReference ref) {
	IWorkbenchPart part = ref.getPart(false);
	if(part != null) {
		hideView((IViewPart)part);
	} else if(isFastView(ref)){
		hideView(getActivePerspective(),ref);		
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
	IViewReference ref = (IViewReference)getReference(view);
	if (isZoomed() && !isFastView(ref))
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
		
	hideView(persp, ref);
	
}
private void hideView(Perspective persp, IViewReference ref) {
	// Hide the part.  
	persp.hideView(ref);
	
	
	// If the part is no longer reference then dispose it.
	boolean exists = viewFactory.hasView(ref.getId());
	if (!exists) {
		firePartClosed(ref);
		disposePart(ref);
		activationList.remove(ref);		
	}
	
	// Notify interested listeners
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_VIEW_HIDE);
	
	// Just in case view was fast.
	window.getShortcutBar().update(true);
	
	//if it was the last part, close the perspective
	lastPartClosePerspective();
}

/*
 * Closes the perspective when there are no fast views 
 * or active parts. Bug 7743.
 */
private void lastPartClosePerspective() {
	Perspective persp = getActivePerspective();
	if (persp != null && getActivePart() == null)
		if(persp.getViewReferences().length == 0 && getEditorReferences().length == 0)
			closePerspective(persp, false,true);
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
	
	// Create presentation.
	createClientComposite();
	editorPresentation = new EditorPresentation(this) ;
	editorMgr = new EditorManager(window, this, editorPresentation);
	
	// Get perspective descriptor.
	if(layoutID != null) {
		PerspectiveDescriptor desc = (PerspectiveDescriptor)WorkbenchPlugin
			.getDefault().getPerspectiveRegistry().findPerspectiveWithId(layoutID);
		if (desc == null)
			throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchPage.ErrorRecreatingPerspective")); //$NON-NLS-1$
		Perspective persp = createPerspective(desc);
		if(persp == null)
			return;
		perspList.setActive(persp);
		window.firePerspectiveActivated(this, desc);
		
		// Update MRU list.
		Workbench wb = (Workbench)window.getWorkbench();
		wb.getPerspectiveHistory().add(desc);
	}
}
/**
 * See IWorkbenchPage.
 */
public boolean isPartVisible(IWorkbenchPart part) {
	return ((PartSite)part.getSite()).getPane().isVisible();
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
public boolean isFastView(IViewReference ref) {
	Perspective persp = getActivePerspective();
	if (persp != null)
		return persp.isFastView(ref);
	else
		return false;
}
/**
 * Return the active fast view or null if there are no
 * fast views or if there are all minimized.
 */
public IViewReference getActiveFastView() {
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
	if(persp.getPresentation() == null)
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
			return true;
		}
		EditorSite site = (EditorSite)part.getSite();
		EditorPane pane = (EditorPane)site.getPane();
		EditorWorkbook book = pane.getWorkbook();
		return !book.equals(book.getEditorArea().getActiveWorkbook());
	}
	// part is a view
	if(part instanceof IViewPart) {
		if(isFastView((IViewReference)getReference(part)) || part.equals(getActivePart()))
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
	Iterator enum = perspList.iterator();
	while (enum.hasNext()) {
		Perspective perspective = (Perspective) enum.next();
		window.addPerspectiveShortcut(perspective.getDesc(), this);
	}
	composite.setVisible(true);
	Perspective persp = getActivePerspective();
	
	if (persp != null) {
		window.selectPerspectiveShortcut(persp.getDesc(), this, true);
		persp.onActivate();
		updateVisibility(null,persp);
	}
	if (activePart == null) {
		IViewReference refs[] = persp.getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			IViewReference ref = refs[i];
			if(ref != null) {
				activePart = ref.getPart(false);
				if(activePart != null)
					break;
			}			
		}
	}
	if (activePart != null) {
		activationList.setActive(activePart);
		
		activatePart(activePart);
		actionSwitcher.updateActivePart(activePart);
		if (activePart instanceof IEditorPart) {
			lastActiveEditor = (IEditorPart) activePart;
			actionSwitcher.updateTopEditor((IEditorPart) activePart);
		} else {
			IEditorPart editor = editorMgr.getVisibleEditor();
			if (editor != null) {
				actionSwitcher.updateTopEditor(editor);
				
				// inform the site's action bars of the current editor 
				// (important that this occur during page opening).
				PartSite site = (PartSite)editor.getSite();
				SubActionBars bars = (SubActionBars)site.getActionBars();
				bars.partChanged(editor);
			}
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
		window.removePerspectiveShortcut(perspective.getDesc(), this);
	}
}
/**
 * See IWorkbenchPage.
 */
public void reuseEditor(IReusableEditor editor,IEditorInput input) {
	editor.setInput(input);
	firePartInputChanged(editor);
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IFile file) 
	throws PartInitException
{
	return openEditor(new FileEditorInput(file),null,true,false,null, false);
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IFile file, String editorID)
	throws PartInitException 
{
	return openEditor(new FileEditorInput(file),editorID,true,true,file, false);
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IFile file, String editorID,boolean activate)
	throws PartInitException 
{
	return openEditor(new FileEditorInput(file),editorID,activate,editorID != null,file, false);
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
	return openMarker(marker, activate, false);
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
	return openEditor(input,editorID,activate,true,null, false);
}
/**
 * Method openInternalEditor.
 * @param input
 * @param editorId
 */
public IEditorPart openInternalEditor(IEditorInput input, String editorId)
	throws PartInitException {
	return openEditor(input,editorId,true,true,null, true);

}
/**
 * Method openInternalEditor.
 * @param iMarker
 */
public IEditorPart openInternalEditor(IMarker marker)
	throws PartInitException {
		
	return openMarker(marker, true, true);
}

private IEditorPart openMarker(IMarker marker, boolean activate, boolean forceInternal)
	throws PartInitException {
	// Get the resource.
	Object resource = marker.getResource();
	if(!(resource instanceof IFile))
		return null;
		
	IFile file = (IFile)resource;

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
		editor = openEditor(new FileEditorInput(file),null,activate,false,null, forceInternal);
	else
		editor = openEditor(new FileEditorInput(file),editorID,activate,true,file, forceInternal);

	// Goto the bookmark.
	if (editor != null)
		editor.gotoMarker(marker);
	return editor;
}
/**
 * Method openInternalEditor.
 * @param iFile
 */
public IEditorPart openInternalEditor(IFile file)
	throws PartInitException {
	return openEditor(new FileEditorInput(file),null,true,false,null, true);
}
/**
 * See IWorkbenchPage.
 */
private IEditorPart openEditor(final IEditorInput input,final String editorID,final boolean activate,final boolean useEditorID,final IFile file,final boolean forceInternal) throws PartInitException {
	final IEditorPart result[] = new IEditorPart[1];
	final PartInitException ex[] = new PartInitException[1];
	BusyIndicator.showWhile(
		window.getClientComposite().getDisplay(),
		new Runnable() {
			public void run() {
				try {
					result[0] = busyOpenEditor(input,editorID,activate,useEditorID,file,forceInternal);
				} catch (PartInitException e) {
					ex[0] = e;
				}
			}
		});
	if(ex[0] != null)
		throw ex[0];
	return result[0];
}
/**
 * See IWorkbenchPage.openEditor
 */
private IEditorPart busyOpenEditor(IEditorInput input, String editorID, boolean activate,boolean useEditorID,IFile file, boolean forceInternal) throws PartInitException {			
	// If an editor already exists for the input use it.
	IEditorPart editor = getEditorManager().findEditor(input);
	if (editor != null) {
		if(IWorkbenchConstants.SYSTEM_EDITOR_ID.equals(editorID)) {
			if(editor.isDirty()) {
				MessageDialog dialog = new MessageDialog(
					getWorkbenchWindow().getShell(),
					WorkbenchMessages.getString("Save"),  //$NON-NLS-1$
					null,	// accept the default window icon
					WorkbenchMessages.format("WorkbenchPage.editorAlreadyOpenedMsg",new String[]{input.getName()}),  //$NON-NLS-1$
					MessageDialog.QUESTION, 
					new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL}, 
					0);	
				int saveFile = dialog.open();
				if(saveFile == 0) {
					try {
						final IEditorPart editorToSave = editor;
						getWorkbenchWindow().run(false,false,new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								editorToSave.doSave(monitor);
							}
						});
					} catch (InvocationTargetException e) {	
						throw (RuntimeException)e.getTargetException();
					} catch (InterruptedException e) {
						return null;
					}
				} else if(saveFile == 2) {
					return null;
				}
			}
		} else {
			showEditor(activate, editor);
			return editor;
		}
	}

// Disabled turning redraw off, because it causes setFocus
// in activate(editor) to fail.
// getClientComposite().setRedraw(false);

	// Remember the old visible editor 
	IEditorPart oldVisibleEditor = getEditorManager().getVisibleEditor();
	
	// Otherwise, create a new one. This may cause the new editor to
	// become the visible (i.e top) editor.
	IEditorReference ref = null;

	if(useEditorID)
		ref = getEditorManager().openEditor(editorID, input,true, forceInternal);
	else
		ref = getEditorManager().openEditor(null,input,true, forceInternal);
		
	if(ref != null) {
		editor = ref.getEditor(true);
		addPart(ref);
	}
	
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
			if ((visibleEditor == editor) && (oldVisibleEditor != editor)) {
				actionSwitcher.updateTopEditor(editor);
				firePartBroughtToTop(editor);
			} else
				bringToTop(editor);
		}
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_OPEN);
	}
	
//	getClientComposite().setRedraw(true);

	return editor;
}
private void showEditor(boolean activate, IEditorPart editor) {
	zoomOutIfNecessary(editor);
	setEditorAreaVisible(true);
	if (activate)
		activate(editor);
	else
		bringToTop(editor);
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
private boolean partChangeAffectsZoom(IWorkbenchPartReference ref) {
	PartPane pane = ((WorkbenchPartReference)ref).getPane();
	if (pane instanceof MultiEditorInnerPane)
		pane = ((MultiEditorInnerPane)pane).getParentPane();
	return getActivePerspective().getPresentation().partChangeAffectsZoom(pane);
}
/**
 * Removes a fast view.
 */
public void removeFastView(IViewReference ref) {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;

	// If parts change always update zoom.
	if (isZoomed())
		zoomOut();

	// Do real work.	
	persp.removeFastView(ref);

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
 * Removes an IPartListener from the part service.
 */
public void removePartListener(IPartListener2 l) {
	partListeners2.removePartListener(l);
}
/**
 * Implements IWorkbenchPage
 * 
 * @see org.eclipse.ui.IWorkbenchPage#removePropertyChangeListener(IPropertyChangeListener)
 * @since 2.0
 * @deprecated individual views should store a working set if needed and
 * 	register a property change listener directly with the working set manager
 * 	to receive notification when the view working set is removed.
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
/*
 * (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void removePostSelectionListener(ISelectionListener listener) {
	selectionService.removePostSelectionListener(listener);
}

/*
 * (non-Javadoc)
 * Method declared on ISelectionListener.
 */
public void removePostSelectionListener(String partId, ISelectionListener listener) {
	selectionService.removePostSelectionListener(partId, listener);
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
	// Use set redraw to eliminate the "flash" that occurs when the
	// coolbar is reset.
	CoolBarManager mgr = window.getCoolBarManager();
	mgr.getControl().setRedraw(false);
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			busyResetPerspective();
		}
	});
	mgr.getControl().setRedraw(true);
}
/**
 * Restore this page from the memento and ensure that
 * the active perspective is equals the active descriptor otherwise
 * create a new perspective for that descriptor.
 * If activeDescriptor is null active the old perspective.
 */
public IStatus restoreState(IMemento memento,IPerspectiveDescriptor activeDescritor) {
	// Restore working set
	String pageName = memento.getString(IWorkbenchConstants.TAG_LABEL);
	String label = pageName == null ? "" : "::" + pageName; //$NON-NLS-1$ //$NON-NLS-2$
	
	try {
		UIStats.start(UIStats.RESTORE_WORKBENCH,"WorkbenchPage" + label); //$NON-NLS-1$
		if(pageName == null) pageName = ""; //$NON-NLS-1$
		MultiStatus result = new MultiStatus(
			PlatformUI.PLUGIN_ID,IStatus.OK,
			WorkbenchMessages.format("WorkbenchPage.unableToRestorePerspective",new String[]{pageName}), //$NON-NLS-1$
			null);
	
		String workingSetName = memento.getString(IWorkbenchConstants.TAG_WORKING_SET);
		if (workingSetName != null) {
			WorkingSetManager workingSetManager = (WorkingSetManager) getWorkbenchWindow().getWorkbench().getWorkingSetManager();
			setWorkingSet(workingSetManager.getWorkingSet(workingSetName));
		}
		
		// Restore editor manager.
		IMemento childMem = memento.getChild(IWorkbenchConstants.TAG_EDITORS);
		result.merge(getEditorManager().restoreState(childMem));
		
		childMem = memento.getChild(IWorkbenchConstants.TAG_VIEWS);
		if(childMem != null)
			result.merge(getViewFactory().restoreState(childMem));
	
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
				result.merge(persp.restoreState(perspMems[i]));
				IPerspectiveDescriptor desc = persp.getDesc();
				if (desc.equals(activeDescritor))
					activePerspective = persp;
				else if((activePerspective == null) && desc.getId().equals(activePerspectiveID))
					activePerspective = persp;
				perspList.add(persp);
			} catch (WorkbenchException e) {
			}
		}
		boolean restoreActivePerspective = false;
		if(activeDescritor == null)
			restoreActivePerspective = true;
		else if (activePerspective != null && activePerspective.getDesc().equals(activeDescritor)) {
			restoreActivePerspective = true;
		} else {
			restoreActivePerspective = false;
			activePerspective = createPerspective((PerspectiveDescriptor)activeDescritor);
			if(activePerspective == null) {
				result.merge(new Status(IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
					WorkbenchMessages.format("Workbench.showPerspectiveError",new String[]{activeDescritor.getId()}), //$NON-NLS-1$
					null));			
			}
		}
				
		perspList.setActive(activePerspective);
		
		// Make sure we have a valid perspective to work with,
		// otherwise return.
		activePerspective = perspList.getActive();
		if (activePerspective == null) {
			activePerspective = perspList.getNextActive();
			perspList.setActive(activePerspective);
			result.merge(activePerspective.restoreState());
		}
		if (activePerspective != null && restoreActivePerspective)
			result.merge(activePerspective.restoreState());
		
		if (activePerspective != null) {	
			window.firePerspectiveActivated(this, activePerspective.getDesc());
		
			// Restore active part.
			if (activePartID != null) {
				IViewReference ref = activePerspective.findView(activePartID);
				IViewPart view = null;
				if(ref != null)
					view = ref.getView(true);
				if (view != null)
					activePart = view;
			}
		}
			
		childMem = memento.getChild(IWorkbenchConstants.TAG_NAVIGATION_HISTORY);
		if(childMem != null)
			navigationHistory.restoreState(childMem);
		else if(getActiveEditor() != null)
			navigationHistory.markEditor(getActiveEditor());
		return result;
	} finally {
		UIStats.end(UIStats.RESTORE_WORKBENCH,"WorkbenchPage" + label); //$NON-NLS-1$
	}
}
/**
 * See IWorkbenchPage
 */
public boolean saveAllEditors(boolean confirm) {
	return getEditorManager().saveAll(confirm, false);
}
/*
 * Saves the workbench part.
 */
protected boolean savePart(ISaveablePart saveable, IWorkbenchPart part, boolean confirm) {
	// Do not certify part do allow editors inside a multipageeditor to call this.
	return getEditorManager().savePart(saveable, part, confirm);
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
public boolean saveEditor(IEditorPart editor, boolean confirm) {
	return savePart(editor, editor, confirm);
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
public void savePerspectiveAs(IPerspectiveDescriptor newDesc) {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;
	IPerspectiveDescriptor oldDesc = persp.getDesc();
	
	// Always unzoom.
	if (isZoomed())
		zoomOut();

	saveToolBarLayout();
	persp.saveDescAs(newDesc);
	window.updatePerspectiveShortcut(oldDesc, newDesc, this);
	
	// Update MRU list.
	Workbench wb = (Workbench)window.getWorkbench();
	wb.getPerspectiveHistory().add(newDesc);
}
/**
 * Save the toolbar layout for the given perspective.
 */
protected void saveToolBarLayout() {
	Perspective persp = getActivePerspective(); 
	if (persp == null) return;
	window.getCoolBarManager().saveLayoutFor(persp);
}
/**
 * Save the state of the page.
 */
public IStatus saveState(IMemento memento) {
	// We must unzoom to get correct layout.
	if (isZoomed())
		zoomOut();

	MultiStatus result = new MultiStatus(
		PlatformUI.PLUGIN_ID,IStatus.OK,
		WorkbenchMessages.format("WorkbenchPage.unableToSavePerspective",new String[]{getLabel()}), //$NON-NLS-1$
		null);
				
	// Save editor manager.
	IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_EDITORS);
	result.merge(editorMgr.saveState(childMem));

	childMem = memento.createChild(IWorkbenchConstants.TAG_VIEWS);
	result.merge(getViewFactory().saveState(childMem));
	
	// Create persp block.
	childMem = memento.createChild(IWorkbenchConstants.TAG_PERSPECTIVES);
	if (getPerspective() != null)
		childMem.putString(IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE, getPerspective().getId());
	if (getActivePart() != null)
	 	childMem.putString(IWorkbenchConstants.TAG_ACTIVE_PART,getActivePart().getSite().getId());

	// Save the toolbar layout for the current perspective.
	saveToolBarLayout();
	
	// Save each perspective in opened order
	Iterator enum = perspList.iterator();
	while (enum.hasNext()) {
		Perspective persp = (Perspective)enum.next();
		IMemento gChildMem = childMem.createChild(IWorkbenchConstants.TAG_PERSPECTIVE);
		result.merge(persp.saveState(gChildMem));
	}
	// Save working set if set
	if (workingSet != null) {
		memento.putString(IWorkbenchConstants.TAG_WORKING_SET, workingSet.getName());
	}
	
	navigationHistory.saveState(memento.createChild(IWorkbenchConstants.TAG_NAVIGATION_HISTORY));
	return result;
}
/**
 * Sets the active part.
 */
private void setActivePart(IWorkbenchPart newPart) {
	// Optimize it.
	if (activePart == newPart)
		return;

	//No need to change the history if the active editor is becoming the active part
	boolean markLocation = newPart != lastActiveEditor;
	String label = newPart != null ? newPart.getTitle() : "none"; //$NON-NLS-1$
	try {
		UIStats.start(UIStats.ACTIVATE_PART,label);
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
				IEditorReference ref = (IEditorReference)getReference(lastActiveEditor);
				editorMgr.setVisibleEditor(ref,true);
			}
		}
		activatePart(activePart);
		
		if(markLocation && activePart != null && activePart instanceof IEditorPart)
			navigationHistory.markEditor((IEditorPart)activePart);
	
		// Fire notifications
		if (oldPart != null)
			firePartDeactivated(oldPart);
	
		// Update actions now so old actions have heard part deactivated and 
		// new actions can hear part activated.
		actionSwitcher.updateActivePart(newPart);	
	
		if (newPart != null)
			firePartActivated(newPart);
	} finally {
		UIStats.end(UIStats.ACTIVATE_PART,label);
	}
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
		if (activePart instanceof IEditorPart) {
			IEditorPart e = (IEditorPart)activePart;
			setActivePart(null);
			// preserve editor contributions
			actionSwitcher.updateTopEditor(e);
		}
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
	saveToolBarLayout();

	if(newPersp != null) {
		IStatus status = newPersp.restoreState();	
		if(status.getSeverity() != IStatus.OK) {
			String title = WorkbenchMessages.getString("WorkbenchPage.problemRestoringTitle");  //$NON-NLS-1$
			String msg = WorkbenchMessages.getString("WorkbenchPage.errorReadingState"); //$NON-NLS-1$
			ErrorDialog.openError(getWorkbenchWindow().getShell(),title,msg,status); 			
		}
	}
	
	// Deactivate active part.
	
	// ensure the switcher is not showing any action sets
	// so it will reshow them in the new perspective
	actionSwitcher.updateTopEditor(null); 
	
	IWorkbenchPart oldActivePart = activePart;
	setActivePart(null);

	// Deactivate the old layout
	if (oldPersp != null) {
		oldPersp.onDeactivate();
		window.selectPerspectiveShortcut(oldPersp.getDesc(), this, false);
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
		window.selectPerspectiveShortcut(newPersp.getDesc(), this, true);
	} else {
		// No need to remember old active part since there
		// is no new active perspective to activate it in.
		oldActivePart = null;
	}
	
	// Update the window
	window.updateActionSets();
	window.updateTitle();
	window.getShortcutBar().update(true);
	
	updateVisibility(oldPersp, newPersp);
	
	// Reactivate active part.
	if (oldActivePart != null) {
		String id = oldActivePart.getSite().getId();
		oldPersp.setOldPartID(id);
		if (oldActivePart instanceof IEditorPart && isEditorAreaVisible()) {
			activate(oldActivePart);
		} else if (oldActivePart instanceof IViewPart) {
			IEditorPart ed = editorMgr.getVisibleEditor();
			if (ed != null)
				actionSwitcher.updateTopEditor(ed);
			if (findView(id) != null) {
				activate(oldActivePart);
			} else {
				activateOldPart(newPersp);
			}
		} else {
			activateOldPart(newPersp);
		}
	} else { //no active part
		IEditorPart ed = editorMgr.getVisibleEditor();
		if (ed != null) {
			actionSwitcher.updateTopEditor(ed);
		}  else  {
			activateOldPart(newPersp);
		}
	}
	if(getActivePart() == null && activationList.getActive() != null) {
		activate(activationList.getActive());
	}
	if(editorPresentation != null)
		editorPresentation.showVisibleEditor();
		
	// Update the Coolbar layout.  Do this after the part is activated,
	// since the layout may contain items associated to the part.
	setToolBarLayout();
}
/*
 * Update visibility state of all views.
 */
private void updateVisibility(Perspective oldPersp, Perspective newPersp) {
	HashSet set = new HashSet();
	IWorkbenchPartReference[] refs;
	if (oldPersp != null) {
		refs = oldPersp.getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			PartPane pane = ((WorkbenchPartReference) refs[i]).getPane();
			if(pane != null)
				set.add(pane);
		}
	}
	if (newPersp != null) {
		refs = newPersp.getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			PartPane pane = ((WorkbenchPartReference) refs[i]).getPane();
			if(pane != null)
				set.add(pane);
		}
		PerspectivePresentation pres = newPersp.getPresentation();
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			PartPane pane = (PartPane) iter.next();
			boolean isVisible = pres.isPartVisible(pane.getID());
			pane.setVisible(isVisible);
		}
	}
	else {
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			PartPane pane = (PartPane) iter.next();
			pane.setVisible(false);
		}
	}
}

private void activateOldPart(Perspective newPersp) {
	if(window.isClosing())
		return;
	if (newPersp != null) {
		String oldID = newPersp.getOldPartID();
		IWorkbenchPart prevOldPart = null;
		if (oldID != null)
			prevOldPart = findView(oldID);
		if (prevOldPart != null)
			activate(prevOldPart);
		else if (isEditorAreaVisible())
			activate(getActiveEditor()); 
	}
}
/**
 * Sets the perspective.  
 * 
 * @param persp identifies the new perspective.
 */
public void setPerspective(final IPerspectiveDescriptor desc) {
	// Going from multiple to single rows can make the coolbar
	// and its adjacent views appear jumpy as perspectives are
	// switched.  Turn off redraw to help with this.
	CoolBarManager mgr = window.getCoolBarManager();
	try {
		mgr.getControl().setRedraw(false);
		getClientComposite().setRedraw(false);
		// Run op in busy cursor.
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				busySetPerspective(desc);
			}
		});
	} finally {
		getClientComposite().setRedraw(true);
		mgr.getControl().setRedraw(true);
		IWorkbenchPart part = getActivePart();
		if(part != null)
			part.setFocus();
	}
}
/**
 * Restore the toolbar layout for the active perspective.
 */
protected void setToolBarLayout() {
	Perspective persp = getActivePerspective(); 
	if (persp == null) return;
	window.getCoolBarManager().setLayoutFor(persp);
}
/**
 * Sets the active working set for the workbench page.
 * Notifies property change listener about the change.
 * 
 * @param newWorkingSet the active working set for the page.
 * 	May be null.
 * @since 2.0
 * @deprecated individual views should store a working set if needed
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
public void toggleFastView(IViewReference ref) {
	Perspective persp = getActivePerspective();
	if (persp != null) {
		persp.toggleFastView(ref);
		// if the fast view has been deactivated
		if (ref != persp.getActiveFastView()) {
			IWorkbenchPart previouslyActive = activationList.getPreviouslyActive();
			IEditorPart activeEditor = getActiveEditor();
			if(activeEditor != null && previouslyActive instanceof IEditorPart)
				setActivePart(activeEditor);
			else
				setActivePart(previouslyActive);
		}
	}
}
/**
 * Zoom in on a part.  
 * If the part is already in zoom then zoom out.
 */
public void toggleZoom(IWorkbenchPartReference ref) {
	Perspective persp = getActivePerspective();
	if (persp == null)
		return;

	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * PartPane pane = ((PartSite)(part.getSite())).getPane();
	 * // If target part is detached ignore.
	 * if (pane.getWindow() instanceof DetachedWindow) 
	 * 	return;
	 */
	 
	// Update zoom status.
	if (isZoomed()) {
		zoomOut();
		return;
	} else {
		persp.getPresentation().zoomIn(ref);
		activate(ref.getPart(true));
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
public void updateTitle(IViewReference ref) {
	if (isFastView(ref)) {
		// Would be more efficient to just update label of single tool item
		// but we don't have access to it from here.
		window.getShortcutBar().update(true);
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
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();		
	return store.getInt(IPreferenceConstants.REUSE_EDITORS);
}
/**
 * @see IPageLayout.
 */
public void setEditorReuseThreshold(int openEditors) {
}
/*
 * Returns the editors in activation order (oldest first).
 */
public IEditorReference[] getSortedEditors() {
	return activationList.getEditors();
}
/**
 * Returns an iterator over the opened perspectives
 */
protected IPerspectiveDescriptor[] getOpenedPerspectives() {
	Perspective opened[] = perspList.getSortedPerspectives();
	IPerspectiveDescriptor[] result = new IPerspectiveDescriptor[opened.length];
	for (int i = 0; i < result.length; i++) {
		result[i] = opened[i].getDesc();
	}
	return result;
}
/*
 * Returns the perspectives in activation order (oldest first).
 */
protected IPerspectiveDescriptor[] getSortedPerspectives() {
	Perspective sortedArray[] = perspList.getSortedPerspectives();
	IPerspectiveDescriptor[] result = new IPerspectiveDescriptor[sortedArray.length];
	for (int i = 0; i < result.length; i++) {
		result[i] = sortedArray[i].getDesc();
	}
	return result;
}
/*
 * Returns the parts in activation order (oldest first).
 */
public IWorkbenchPartReference[] getSortedParts() {
	return activationList.getParts();
}

public IWorkbenchPartReference getReference(IWorkbenchPart part) {
	PartPane pane = ((PartSite)part.getSite()).getPane();
	if(pane instanceof MultiEditorInnerPane) {
		MultiEditorInnerPane innerPane = (MultiEditorInnerPane)pane;
		return innerPane.getParentPane().getPartReference();
	}
	if(pane == null) { 
		/* An error has occurred while creating the view.
		   Reference is needed during clean up. */
		IViewReference refs[] = getViewReferences();
		for (int i = 0; i < refs.length; i++) {
			if(refs[i].getPart(false) == part)
				return refs[i];
		}
		return null;
	}
	return pane.getPartReference();
}

private class ActivationList {
	//List of parts in the activation order (oldest first)
	List parts = new ArrayList();
	
	/*
	 * Add/Move the active part to end of the list;
	 */
	void setActive(IWorkbenchPart part) {
		if(parts.size() <= 0)
			return;
		PartPane pane = ((PartSite)part.getSite()).getPane();
		if(pane instanceof MultiEditorInnerPane) {
			MultiEditorInnerPane innerPane = (MultiEditorInnerPane)pane;
			setActive(innerPane.getParentPane().getPartReference().getPart(true));
		} else {
			IWorkbenchPartReference ref = getReference(part);
			if(ref == parts.get(parts.size() - 1))
				return;
			parts.remove(ref);
			parts.add(ref);
		}
		pane.addPropertyChangeListener(propertyChangeListener);	
	}
	/*
	 * Add/Move the active part to end of the list;
	 */
	void setActive(IWorkbenchPartReference ref) {
		setActive(ref.getPart(true));
	}	
	/*
	 * Add the active part to the beginning of the list.
	 */
	void add(IWorkbenchPartReference ref) {
		if(parts.indexOf(ref) >= 0)
			return;
		
		IWorkbenchPart part = ref.getPart(false);
		if(part != null) {
			PartPane pane = ((PartSite)part.getSite()).getPane();
			if(pane instanceof MultiEditorInnerPane) {
				MultiEditorInnerPane innerPane = (MultiEditorInnerPane)pane;
				add(innerPane.getParentPane().getPartReference());
				return;
			}
		}
		PartPane pane = ((WorkbenchPartReference)ref).getPane();
		if(pane != null)
			pane.addPropertyChangeListener(propertyChangeListener);	
		parts.add(0,ref);
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
	 * filter fast views and views from other perspectives.
	 */	
	private IWorkbenchPart getActive(int start) {
		IWorkbenchPartReference[] views = getViewReferences();
		for (int i = start; i >= 0; i--) {
			IWorkbenchPartReference ref = (IWorkbenchPartReference)parts.get(i);
			if(ref instanceof IViewReference) {
				if(!((IViewReference)ref).isFastView()) {
					for (int j = 0; j < views.length; j++) {
						if(views[j] == ref) {
							return ref.getPart(true);
						}
					}
				}
			} else {
				return ref.getPart(true);
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
		return parts.indexOf(getReference(part));
	}
	/*
	 * Remove a part from the list
	 */
	boolean remove(IWorkbenchPartReference ref) {
		PartPane pane = ((WorkbenchPartReference)ref).getPane();
		if(pane != null) 
			pane.removePropertyChangeListener(propertyChangeListener);
		return parts.remove(ref);
	}
	/*
	 * Returns the editors in activation order (oldest first).
	 */
	private IEditorReference[] getEditors() {
		ArrayList editors = new ArrayList(parts.size());
		for (Iterator i = parts.iterator(); i.hasNext();) {
			IWorkbenchPartReference part = (IWorkbenchPartReference) i.next();
			if (part instanceof IEditorReference) {
				editors.add(part);
			}
		}
		return (IEditorReference[])editors.toArray(new IEditorReference[editors.size()]);
	}
	/*
	 * Return a list with all parts (editors and views).
	 */
	private IWorkbenchPartReference[] getParts() {
		IWorkbenchPartReference[] views = getViewReferences();
		ArrayList resultList = new ArrayList(parts.size());
		for (Iterator iterator = parts.iterator(); iterator.hasNext();) {
			IWorkbenchPartReference ref = (IWorkbenchPartReference)iterator.next();
			if(ref instanceof IViewReference) {
				//Filter views from other perspectives
				for (int i = 0; i < views.length; i++) {
					if(views[i] == ref) {
						resultList.add(ref);
						break;
					}
				}
			} else {
				resultList.add(ref);	
			}	
		}
		IWorkbenchPartReference[] result = new IWorkbenchPartReference[resultList.size()];
		return (IWorkbenchPartReference[])resultList.toArray(result);
	}
	/*
	 * Returns the topmost editor on the stack, or null if none.
	 */
	IEditorPart getTopEditor() {
		IEditorReference editors[] = getEditors();
		if (editors.length > 0) {
			return editors[editors.length - 1].getEditor(true);
		}
		return null;
	}
}

	/**
	 * Helper class to keep track of all opened perspective.
	 * Both the opened and used order is kept.
	 */
	private class PerspectiveList {
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
		 * Return all perspectives in the order they were activated.
		 */
		public Perspective[] getSortedPerspectives() {
			Perspective[] result = new Perspective[usedList.size()];
			return (Perspective[])usedList.toArray(result);
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
		 * Returns an array with all opened perspectives
		 */
		public Perspective[] getOpenedPerspectives() {
			Perspective[] result = new Perspective[openedList.size()];
			return (Perspective[])openedList.toArray(result);
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
