package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * Standard action for closing the currently selected project(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CloseResourceAction extends WorkspaceAction implements IResourceChangeListener {
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CloseResourceAction"; //$NON-NLS-1$
	/**
	 * Creates a new action.
	 *
	 * @param shell the shell for any dialogs
	 */
	public CloseResourceAction(Shell shell) {
		super(shell, WorkbenchMessages.getString("CloseResourceAction.text")); //$NON-NLS-1$
		setId(ID);
		setToolTipText(WorkbenchMessages.getString("CloseResourceAction.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_RESOURCE_ACTION);
	}
	/**
	 * Return a list of dirty editors associated with the given projects.  Return
	 * editors from all perspectives.
	 * 
	 * @return List the dirty editors
	 */
	List getDirtyEditors(List projects) {
		List dirtyEditors = new ArrayList(0);
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				IWorkbenchPage page = pages[j];
				IEditorPart[] editors = page.getDirtyEditors();
				for (int k = 0; k < editors.length; k++) {
					IEditorPart editor = editors[k];
					IEditorInput input = editor.getEditorInput();
					if (input instanceof IFileEditorInput) {
						IFile inputFile = ((IFileEditorInput) input).getFile();
						if (projects.contains(inputFile.getProject())) {
							if (editor.isDirty()) {
								dirtyEditors.add(editor);
							}
						}
					}
				}
			}
		}
		return dirtyEditors;
	}
	/**
	 * Open a dialog that can be used to select which of the given
	 * editors to save. Return the list of editors to save.  A value of 
	 * null implies that the operation was cancelled.
	 * 
	 * @return List the editors to save
	 */
	List getEditorsToSave(List dirtyEditors) {
		if (dirtyEditors.isEmpty())
			return new ArrayList(0);

		// The list may have multiple editors opened for the same input,
		// so process the list for duplicates.
		List saveEditors = new ArrayList(0);
		List dirtyInputs = new ArrayList(0);
		Iterator iter = dirtyEditors.iterator();
		while (iter.hasNext()) {
			IEditorPart editor = (IEditorPart) iter.next();
			IEditorInput input = editor.getEditorInput();
			IFile inputFile = ((IFileEditorInput) input).getFile();
			// if the same file is open in multiple perspectives,
			// we don't want to count it as dirty multiple times
			if (!dirtyInputs.contains(inputFile)) {
				dirtyInputs.add(inputFile);
				saveEditors.add(editor);
			} 
		}
		AdaptableList input = new AdaptableList();
		input.add(saveEditors.iterator());
		ListSelectionDialog dlg =
			new ListSelectionDialog(getShell(), input, new WorkbenchContentProvider(), new WorkbenchPartLabelProvider(), WorkbenchMessages.getString("EditorManager.saveResourcesMessage")); //$NON-NLS-1$

		dlg.setInitialSelections(saveEditors.toArray(new Object[saveEditors.size()]));
		dlg.setTitle(WorkbenchMessages.getString("EditorManager.saveResourcesTitle")); //$NON-NLS-1$
		int result = dlg.open();

		if (result == IDialogConstants.CANCEL_ID)
			return null;
		return Arrays.asList(dlg.getResult());
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	String getOperationMessage() {
		return ""; //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	String getProblemsMessage() {
		return WorkbenchMessages.getString("CloseResourceAction.problemMessage"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	String getProblemsTitle() {
		return WorkbenchMessages.getString("CloseResourceAction.title"); //$NON-NLS-1$
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
		((IProject) resource).close(monitor);
	}
	/** 
	 * The implementation of this <code>WorkspaceAction</code> method
	 * method saves and closes the resource's dirty editors before closing 
	 * it.
	 */
	public void run() {
		if (!saveDirtyEditors())
			return;
		super.run();
	}
	/**
	 * Causes all dirty editors associated to the resource(s) to be saved, if so
	 * specified by the user, and closed.
	 */
	boolean saveDirtyEditors() {
		// Get the items to close.
		List projects = getSelectedResources();
		if (projects == null || projects.isEmpty())
			// no action needs to be taken since no projects are selected
			return false;

		// Collect all the dirty editors that are associated to the projects that are
		// to be closed.
		final List dirtyEditors = getDirtyEditors(projects);

		// See which editors should be saved.
		final List saveEditors = getEditorsToSave(dirtyEditors);
		if (saveEditors == null)
			// the operation was cancelled
			return false;

		// Save and close the dirty editors.
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				Iterator iter = dirtyEditors.iterator();
				while (iter.hasNext()) {
					IEditorPart editor = (IEditorPart) iter.next();
					IWorkbenchPage page = editor.getEditorSite().getPage();
					if (saveEditors.contains(editor)) {
						// do a direct save vs. using page.saveEditor, so that 
						// progress dialogs do not flash up on the screen multiple 
						// times
						editor.doSave(new NullProgressMonitor());
					}
					page.closeEditor(editor, false);
				}
			}
		});

		return true;
	}
	/* (non-Javadoc)
	 * Method declared on WorkspaceAction.
	 */
	boolean shouldPerformResourcePruning() {
		return false;
	}
	/**
	 * The <code>CloseResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * enabled only if one of the selections is an open project.
	 */
	protected boolean updateSelection(IStructuredSelection s) {
		// don't call super since we want to enable if open project is selected.
		if (!selectionIsOfType(IResource.PROJECT))
			return false;

		Iterator resources = getSelectedResources().iterator();
		while (resources.hasNext()) {
			IProject currentResource = (IProject) resources.next();
			if (currentResource.isOpen()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Handles a resource changed event by updating the enablement
	 * if one of the selected projects is opened or closed.
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		// Warning: code duplicated in OpenResourceAction
		List sel = getSelectedResources();
		// don't bother looking at delta if selection not applicable
		if (selectionIsOfType(IResource.PROJECT)) {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				IResourceDelta[] projDeltas = delta.getAffectedChildren(IResourceDelta.CHANGED);
				for (int i = 0; i < projDeltas.length; ++i) {
					IResourceDelta projDelta = projDeltas[i];
					if ((projDelta.getFlags() & IResourceDelta.OPEN) != 0) {
						if (sel.contains(projDelta.getResource())) {
							selectionChanged(getStructuredSelection());
							return;
						}
					}
				}
			}
		}
	}
}