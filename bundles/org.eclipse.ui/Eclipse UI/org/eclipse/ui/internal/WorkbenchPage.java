package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
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
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;

/**
 * A collection of views and editors in a workbench.
 */
public class WorkbenchPage implements IWorkbenchPage
{
	private WorkbenchWindow window;
	private IAdaptable input;
	private Composite composite;
	private ControlListener resizeListener;
	private IWorkbenchPart activePart;
	private IEditorPart lastActiveEditor;
	private EditorManager editorMgr;
	private EditorPresentation editorPresentation;
	private PartListenerList partListeners = new PartListenerList();
	private SelectionService selectionService = new SelectionService();
	private IActionBars actionBars;
	private Perspective activePersp;
	private ViewFactory viewFactory;
	private ArrayList perspList = new ArrayList(1);

	private Listener mouseDownListener;
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
	init(w, null, input);
	restoreState(memento);
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
		
	// Activate part.
	if(window.getActivePage() == this) {
		bringToTop(part);
		setActivePart(part);
	} else {
		activePart = part;
	}
}
/**
 * Activates a part.  The part is given focus, the pane is hilighted and the action bars are shown.
 */
static private void activatePart(final IWorkbenchPart part, final boolean switchActions, final boolean switchActionsForced) {
	Platform.run(new SafeRunnableAdapter(WorkbenchMessages.getString("WorkbenchPage.ErrorActivatingView")) { //$NON-NLS-1$
		public void run() {
			if (part != null) {
				part.setFocus();
				PartSite site = (PartSite)part.getSite();
				site.getPane().showFocus(true);
				SubActionBars bars = (SubActionBars)site.getActionBars();
				bars.partChanged(part);
				if (switchActions)
					bars.activate(switchActionsForced);
			}
		}
	});
}
/**
 * Add a fast view.
 */
public void addFastView(IViewPart view) {
	// If view is zoomed unzoom.
	if (isZoomed() && partChangeAffectsZoom(view))
		zoomOut();

	// Do real work.	
	getPersp().addFastView(view);

	// The view is now invisible.
	// If it is active then deactivate it.
	if (view == activePart)
		setActivePart(null);
		
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
/*
 * Adds an ISelectionListener to the service.
 */
public void addSelectionListener(ISelectionListener l) {
	selectionService.addSelectionListener(l);
}
/**
 * Moves a part forward in the Z order of a perspective so it is visible.
 *
 * @param part the part to bring to move forward
 */
public void bringToTop(IWorkbenchPart part) {
	// Sanity check.
	if (!certifyPart(part))
		return;
		
	// If zoomed unzoom.
	if (isZoomed() && partChangeAffectsZoom(part))
		zoomOut();

	// Move part.
	boolean broughtToTop = false;
	if (part instanceof IEditorPart) {
		broughtToTop = getEditorManager().setVisibleEditor((IEditorPart)part, false);
		if (lastActiveEditor != null && broughtToTop) {
			String newID = part.getSite().getId();
			String oldID = lastActiveEditor.getSite().getId();
			if (newID != oldID) {
				deactivateLastEditor();
				lastActiveEditor = null;
				updateActionBars();
			}
		}
	} else if (part instanceof IViewPart) {
		broughtToTop = getPersp().bringToTop((IViewPart)part);
	}
	if (broughtToTop)
		firePartBroughtToTop(part);
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
	Perspective oldPersp = getPersp();

	// Map the current perspective to the original template.
	// If the original template cannot be found then it has been deleted.  In
	// that case just return. (PR#1GDSABU).
	PerspectiveDescriptor desc = (PerspectiveDescriptor)WorkbenchPlugin
		.getDefault().getPerspectiveRegistry().findPerspectiveWithId(oldPersp.getDesc().getId());
	if (desc == null)
		return;

	// Create new persp from original template.
	Perspective newPersp = createPerspective(desc);
	if (newPersp == null)
		return;

	// Deactivate active part.
	IWorkbenchPart oldActivePart = activePart;
	setActivePart(null);
	
	// Install new persp.
	setPerspective(newPersp);

	// Notify listeners.
	window.getShortcutBar().update(true);
	window.firePerspectiveReset(this, desc);
	window.firePerspectiveChanged(this, desc, CHANGE_RESET);

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
		if (newPersp == null)
			return;
	}

	// Deactivate active part.
	IWorkbenchPart oldActivePart = activePart;
	setActivePart(null);

	// Change layout.
	setPerspective(newPersp);
	window.firePerspectiveActivated(this, desc);
	
	// Update shortcut
	window.updateShortcut(this);
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
}
/**
 * Opens a view.
 *
 * Assumes that a busy cursor is active.
 */
private IViewPart busyShowView(String viewID, boolean activate) 
	throws PartInitException
{
	// If this view is already visible just return.
	IViewPart view = getPersp().findView(viewID);
	if (view != null) {
		if (activate)
			activate(view);
		else
			bringToTop(view);
		return view;
	}

	// If part is added / removed always unzoom.
	if (isZoomed())
		zoomOut();
		
	// Show the view.  
	boolean exists = viewFactory.hasView(viewID);
	view = getPersp().showView(viewID);
	if (view != null) {
		// If it view is new then fire an open event.		
		if (!exists)
			firePartOpened(view);
		if (activate)
			activate(view);
		else
			bringToTop(view);
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_VIEW_SHOW);
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
		return getPersp().containsView((IViewPart)part);
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
	if (activePart instanceof IEditorPart)
		setActivePart(null);
	if (lastActiveEditor != null) {
		deactivateLastEditor();
		updateActionBars();
		lastActiveEditor = null;
	}
			
	// Close all editors.
	IEditorPart [] editors = getEditorManager().getEditors();
	getEditorManager().closeAll();
	for (int nX = 0; nX < editors.length; nX ++) {
		IEditorPart editor = editors[nX];
		firePartClosed(editor);
		editor.dispose();
	}

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
		
	// If part is added / removed always unzoom.
	if (isZoomed())
		zoomOut();

	// Save part.
	if (save && editor.isSaveOnCloseNeeded() 
		&& !getEditorManager().saveEditor(editor, true))
		return false;

	// Deactivate part.
	boolean partWasActive = (editor == activePart);
	if (partWasActive)
		setActivePart(null);
	if (lastActiveEditor == editor) {
		deactivateLastEditor();
		updateActionBars();
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
		IEditorPart newEditor = getEditorManager().getVisibleEditor();
		setActivePart(newEditor); // null is OK.  It just deactivates editor.
	}

	// Return true on success.
	return true;
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
		return persp;
	} catch (WorkbenchException e) {
		return null;
	}
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
 * Deactivates a part.  The pane is unhilighted and the action bars are hidden.
 */
static private void deactivatePart(IWorkbenchPart part, boolean switchActions, boolean switchActionsForced) {
	if (part != null) {
		PartSite site = (PartSite)part.getSite();
		site.getPane().showFocus(false);
		if (switchActions) {
			SubActionBars bars = (SubActionBars)site.getActionBars();
			bars.deactivate(switchActionsForced);
		}
	}
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
	IViewPart [] views = viewFactory.getViews();
	
	// Get rid of perspectives.  This will close the views.
	Iterator enum = perspList.iterator();
	while (enum.hasNext()) {
		Perspective mgr = (Perspective)enum.next();
		mgr.dispose();
	}
	activePersp = null;

	// Dispose views.
	for (int nX = 0; nX < views.length; nX ++) {
		IViewPart view = views[nX];
		firePartClosed(view);
		view.dispose();
	}
	activePart = null;

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
	persp.dispose();

	// Loop through the views.
	for (int nX = 0; nX < views.length; nX ++) {
		IViewPart view = views[nX];
		
		// If the part is no longer reference then dispose it.
		boolean exists = viewFactory.hasView(view.getSite().getId());
		if (!exists) {
			firePartClosed(view);
			view.dispose();
		}
	}
}
/**
 * Edits the action sets.
 */
public boolean editActionSets() {
	// Create list dialog.
	ActionSetSelectionDialog dlg =
		new ActionSetSelectionDialog(
			window.getShell(),
			getPersp());

	// Open.
	boolean ret = (dlg.open() == Window.OK);
	if (ret) {
		window.updateActionSets();
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_ACTION_SET_SHOW);
		window.firePerspectiveReset(this, getPerspective());
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
	return getPersp().findView(id);
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
private void firePartOpened(IWorkbenchPart part) {
	partListeners.firePartOpened(part);
	selectionService.partOpened(part);
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
	return getPersp().getActionSets();
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
	return getPersp().getFastViews();
}
/**
 * @see IWorkbenchPage
 */
public IAdaptable getInput() {
	return input;
}
/**
 * Returns the page label.  This is a permutation of the page number
 * and active perspective.
 */
public String getLabel() {
	String label = WorkbenchMessages.getString("WorkbenchPage.UnknownLabel"); //$NON-NLS-1$
	if (input != null) {
		IWorkbenchAdapter adapter = (IWorkbenchAdapter)input.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null)
			label = adapter.getLabel(input);
	}
	if(activePersp != null)
		label = WorkbenchMessages.format("WorkbenchPage.PerspectiveFormat", new Object[] { label, activePersp.getDesc().getLabel() }); //$NON-NLS-1$
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
	return getPersp().getNewWizardActions();
}
/**
 * Answer the current prespective for this window.
 */
private Perspective getPersp() {
	return activePersp;
}
/**
 * Returns the perspective.
 */
public IPerspectiveDescriptor getPerspective() {
	return getPersp().getDesc();
}
/**
 * Returns the perspective actions for this page.
 * This is List of Strings.
 */
public ArrayList getPerspectiveActions() {
	return getPersp().getPerspectiveActions();
}
/*
 * Returns the selection within the <code>IWorkbenchPage</code>
 */
public ISelection getSelection() {
	return selectionService.getSelection();
}
/**
 * Returns the show view actions the page.
 * This is List of Strings.
 */
public ArrayList getShowViewActions() {
	return getPersp().getShowViewActions();
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
	return getPersp().getViews();
}
/**
 * See IWorkbenchPage.
 */
public IWorkbenchWindow getWorkbenchWindow() {
	return window;
}
/**
 * @see IWorkbenchPage
 */
public void hideActionSet(String actionSetID) {
	getPersp().hideActionSet(actionSetID);
	window.updateActionSets();
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_ACTION_SET_HIDE);
}
/**
 * See IPerpsective
 */
public void hideView(IViewPart view) {
	// Sanity check.	
	if (!certifyPart(view))
		return;
		
	// If part is added / removed always unzoom.
	if (isZoomed())
		zoomOut();
		
	// Confirm.
	if (!getPersp().canCloseView(view))
		return;
		
	// Activate new part.
	if (view == activePart)
		setActivePart(null);
		
	// Hide the part.  
	getPersp().hideView(view);

	// If the part is no longer reference then dispose it.
	boolean exists = viewFactory.hasView(view.getSite().getId());
	if (!exists) {
		firePartClosed(view);
		view.dispose();
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
		activePersp = createPerspective(desc);
		window.firePerspectiveActivated(this, desc);
	}
}
/**
 * Determine if the new active part will cause the
 * the actions to change the visibility state or
 * just change the enablement state.
 * 
 * @return boolean true to change the visibility state, or
 *	false to just changed the enablement state.
 */
private boolean isActionSwitchForced(IWorkbenchPart newPart) {
	if (lastActiveEditor == null)
		return true;
		
	if (lastActiveEditor == newPart)
		return false;
		
	if (newPart instanceof IViewPart)
		return false;
		
	return true;
}
/**
 * See IWorkbenchPage.
 */
public boolean isEditorAreaVisible() {
	if (activePersp == null)
		return false;
	return activePersp.isEditorAreaVisible();
}
/**
 * Returns whether the view is fast.
 */
public boolean isFastView(IViewPart part) {
	return getPersp().isFastView(part);
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
	if (activePersp == null)
		return false;
	return activePersp.getPresentation().isZoomed();
}
/**
 * This method is called when the page is activated.  
 * Normally this will be called as a pair of onDeactivate and onActivate, so the caller is
 * expected to update action bars afterwards.
 */
protected void onActivate() {
	composite.setVisible(true);
	getPersp().onActivate();
	if (activePart != null) {
		activatePart(activePart, true, true);
		if (activePart instanceof IEditorPart)
			lastActiveEditor = (IEditorPart) activePart;
		firePartActivated(activePart);
	} else {
		composite.setFocus();
	}
}
/**
 * This method is called when the page is deactivated.  
 * Normally this will be called as a pair of onDeactivate and onActivate, so the caller is
 * expected to update action bars afterwards.
 */
protected void onDeactivate() {
	if (activePart != null) {
		deactivatePart(activePart, true, true);
		firePartDeactivated(activePart);
	}
	deactivateLastEditor();
	lastActiveEditor = null;
	getPersp().onDeactivate();
	composite.setVisible(false);
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IFile file) 
	throws PartInitException
{
	return openEditor(file, true);
}
/**
 * See IWorkbenchPage.
 */
public IEditorPart openEditor(IFile file, String editorID)
	throws PartInitException 
{
	return openEditor(file, editorID, true);
}
/**
 * See IWorkbenchPage.
 */
private IEditorPart openEditor(IFile file, String editorID, boolean activate)
	throws PartInitException 
{
	// If part is added / removed always unzoom.
	if (isZoomed())
		zoomOut();
		
	// Update the default editor for this file.
	WorkbenchPlugin.getDefault().getEditorRegistry().setDefaultEditor(
		file, editorID);
	
	// If an editor is already open for the input just activate it.
	FileEditorInput input = new FileEditorInput(file);
	IEditorPart editor = getEditorManager().findEditor(input);
	if (editor != null) {
		setEditorAreaVisible(true);
		if (activate)
			activate(editor);
		else
			bringToTop(editor);
		return editor;
	}

	// Otherwise, create a new one.
	editor = getEditorManager().openEditor(editorID, input);
	if (editor != null) {
		firePartOpened(editor);
		setEditorAreaVisible(true);
		if (activate)
			activate(editor);
		else
			bringToTop(editor);
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_OPEN);
	}
	return editor;
}
/**
 * See IWorkbenchPage.
 */
private IEditorPart openEditor(IFile file, boolean activate) 
	throws PartInitException
{
	// If part is added / removed always unzoom.
	if (isZoomed())
		zoomOut();
		
	// If an editor already exists for the input use it.
	FileEditorInput input = new FileEditorInput(file);
	IEditorPart editor = getEditorManager().findEditor(input);
	if (editor != null) {
		setEditorAreaVisible(true);
		if (activate)
			activate(editor);
		else
			bringToTop(editor);
		return editor;
	}

	// Otherwise, create a new one.
	editor = getEditorManager().openEditor(input);
	if (editor != null) {
		firePartOpened(editor);
		setEditorAreaVisible(true);
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_OPEN);
		if (activate)
			activate(editor);
		else
			bringToTop(editor);
	}
	return editor;
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
		editor = openEditor(file, activate);
	else 
		editor = openEditor(file, editorID, activate);

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
	// If part is added / removed always unzoom.
	if (isZoomed())
		zoomOut();
		
	// If an editor already exists for the input use it.
	IEditorPart editor = getEditorManager().findEditor(input);
	if (editor != null) {
		setEditorAreaVisible(true);
		if (activate)
			activate(editor);
		else
			bringToTop(editor);
		return editor;
	}

	// Otherwise, create a new one.
	editor = getEditorManager().openEditor(editorID, input);
	if (editor != null) {
		firePartOpened(editor);
		setEditorAreaVisible(true);
		if (activate)
			activate(editor);
		else
			bringToTop(editor);
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_OPEN);
	}
	return editor;
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
 * Returns whether changes to a parts layout will affect zoom.
 * There are a few conditions for this ..
 *		- we are zoomed.
 *		- the part is contained in the main window.
 *		- the part is not the zoom part
 */
private boolean partChangeAffectsZoom(IWorkbenchPart part) {
	PartPane pane = ((PartSite)part.getSite()).getPane();
	return getPersp().getPresentation().partChangeAffectsZoom(pane);
}
/**
 * Removes a fast view.
 */
public void removeFastView(IViewPart view) {
	// If parts change always update zoom.
	if (isZoomed())
		zoomOut();

	// Do real work.	
	getPersp().removeFastView(view);

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
/*
 * Removes an ISelectionListener from the service.
 */
public void removeSelectionListener(ISelectionListener l) {
	selectionService.removeSelectionListener(l);
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
			Perspective persp = new Perspective(null,this);
			persp.restoreState(perspMems[i]);
			if(persp.getDesc().getId().equals(activePerspectiveID))
				activePerspective = persp;
			perspList.add(persp);
		} catch (WorkbenchException e) {
		}
	}
	activePersp = activePerspective;
	window.firePerspectiveActivated(this, activePersp.getDesc());

	// Restore active part.
	if (activePartID != null) {
		IViewPart view = activePerspective.findView(activePartID);
		if (view != null)
			activePart = view;
	}
	
	IEditorPart editors[] = getEditorManager().getEditors();
	for (int i = 0; i < editors.length; i++){
		firePartOpened(editors[i]);
	}
}
/**
 * See IWorkbenchPage
 */
public boolean saveAllEditors(boolean confirm) {
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
	// Always unzoom.
	if (isZoomed())
		zoomOut();

	getPersp().saveDesc();
}
/**
 * Saves the perspective.
 */
public void savePerspectiveAs(IPerspectiveDescriptor desc) {
	// Always unzoom.
	if (isZoomed())
		zoomOut();

	getPersp().saveDescAs(desc);
	window.updateShortcut(this);
}
/**
 * Save the state of the page.
 */
public void saveState(IMemento memento) {
	// We must unzoom to get correct layout.
	if (isZoomed())
		zoomOut();
		
	// Save editor manager.
	IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_EDITORS);
	editorMgr.saveState(childMem);

	// Create persp block.
	childMem = memento.createChild(IWorkbenchConstants.TAG_PERSPECTIVES);
	childMem.putString(IWorkbenchConstants.TAG_ACTIVE_PERSPECTIVE,getPerspective().getId());
	if (getActivePart() != null)
	 	childMem.putString(IWorkbenchConstants.TAG_ACTIVE_PART,getActivePart().getSite().getId());

	// Save each perspective (active first, others after).
	Iterator enum = perspList.iterator();
	IMemento gChildMem = childMem.createChild(IWorkbenchConstants.TAG_PERSPECTIVE);
	activePersp.saveState(gChildMem);
	for (int i=0;enum.hasNext();i++) {
		Perspective persp = (Perspective)enum.next();
		if (persp != activePersp) {
			gChildMem = childMem.createChild(IWorkbenchConstants.TAG_PERSPECTIVE);
			persp.saveState(gChildMem);
		}
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
	if(getPersp() != null)
		getPersp().partActivated(newPart);
	
	// We will switch actions only if the part types are different.
	boolean switchActions = true;
	if (activePart != null && newPart != null) {
		String newID = newPart.getSite().getId();
		String oldID = activePart.getSite().getId();
		switchActions = (oldID != newID);
	}
	// Try to get away with only changing the enablement of the
	// tool items if possible - workaround for layout flashing
	// when editors contribute lots of items in the toolbar.
	boolean switchActionsForced = false;
	if (switchActions)
		switchActionsForced = isActionSwitchForced(newPart);

	// Clear active part.
	IWorkbenchPart oldPart = activePart;
	activePart = null;		
	if (oldPart != null) {
		deactivatePart(oldPart, switchActions, switchActionsForced);
		firePartDeactivated(oldPart);
	}

	// Set active part.
	activePart = newPart;
	if (newPart != null) {
		// Upon a new editor being activated, make sure the previously
		// active editor's toolbar contributions are removed.
		if (newPart instanceof IEditorPart) {
			if (lastActiveEditor != null) {
				String newID = newPart.getSite().getId();
				String oldID = lastActiveEditor.getSite().getId();
				if (newID != oldID)
					deactivateLastEditor();
			}
			lastActiveEditor = (IEditorPart)newPart;
		}
		activatePart(newPart, switchActions, switchActionsForced);
		firePartActivated(newPart);
	}

	// Update actions.
	if (switchActions)
		updateActionBars();
}
/**
 * See IWorkbenchPage.
 */
public void setEditorAreaVisible(boolean showEditorArea) {
	// If parts change always update zoom.
	if (isZoomed())
		zoomOut();
		
	if (activePersp == null)
		return;

	// Update editor area visibility.
	if (showEditorArea) {
		activePersp.showEditorArea();
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_AREA_SHOW);
	} else {
		activePersp.hideEditorArea();
		if (activePart instanceof IEditorPart)
			setActivePart(null);
		window.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_AREA_HIDE);
	}
}
/**
 * Sets the layout of the page.  
 */
private void setPerspective(Perspective newMgr) {
	if (activePersp == newMgr)
		return;
	if (activePersp != null)
		activePersp.onDeactivate();
	activePersp = newMgr;
	if (activePersp != null)
		activePersp.onActivate();
	window.updateActionSets();
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
 * @see IWorkbenchPage
 */
public void showActionSet(String actionSetID) {
	getPersp().showActionSet(actionSetID);
	window.updateActionSets();
	window.firePerspectiveChanged(this, getPerspective(), CHANGE_ACTION_SET_SHOW);
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
	Perspective persp = getPersp();
	if (persp != null) 
		persp.toggleFastView(part);
}
/**
 * Zoom in on a part.  
 * If the part is already in zoom then zoom out.
 */
public void toggleZoom(IWorkbenchPart part) {
	// If target part is detached ignore.
	PartPane pane = ((PartSite)(part.getSite())).getPane();
	if (pane.getWindow() instanceof DetachedWindow) 
		return;
	if (part instanceof IViewPart && isFastView((IViewPart)part))
		return;
		
	// Update zoom status.
	if (isZoomed()) {
		zoomOut();
		return;
	} else {
		getPersp().getPresentation().zoomIn(pane);
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
private void zoomOut() {
	getPersp().getPresentation().zoomOut();
}
}
