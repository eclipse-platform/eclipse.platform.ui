/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;

/**
 * Standard action for closing the currently selected project(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CloseResourceAction extends WorkspaceAction implements
        IResourceChangeListener {
    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID
            + ".CloseResourceAction"; //$NON-NLS-1$
	private String[] modelProviderIds;

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     */
    public CloseResourceAction(Shell shell) {
        super(shell, IDEWorkbenchMessages.CloseResourceAction_text);
        setId(ID);
        setToolTipText(IDEWorkbenchMessages.CloseResourceAction_toolTip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.CLOSE_RESOURCE_ACTION);
    }

    /**
     * Return a list of dirty editors associated with the given projects.  Return
     * editors from all perspectives.
     * 
     * @return List the dirty editors
     */
    List getDirtyEditors(List projects) {
        List dirtyEditors = new ArrayList(0);
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
                .getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            IWorkbenchPage[] pages = windows[i].getPages();
            for (int j = 0; j < pages.length; j++) {
                IWorkbenchPage page = pages[j];
                IEditorPart[] editors = page.getDirtyEditors();
                for (int k = 0; k < editors.length; k++) {
                    IEditorPart editor = editors[k];
                    IFile inputFile = ResourceUtil.getFile(editor.getEditorInput());
                    if (inputFile != null) {
                        if (projects.contains(inputFile.getProject())) {
                            dirtyEditors.add(editor);
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
            IFile inputFile = ResourceUtil.getFile(editor.getEditorInput());
            if (inputFile != null) {
                // if the same file is open in multiple perspectives,
                // we don't want to count it as dirty multiple times
                if (!dirtyInputs.contains(inputFile)) {
                    dirtyInputs.add(inputFile);
                    saveEditors.add(editor);
                }
            }
        }
        AdaptableList input = new AdaptableList(saveEditors);
        ListSelectionDialog dlg = new ListSelectionDialog(getShell(), input,
                new WorkbenchContentProvider(),
                new WorkbenchPartLabelProvider(), IDEWorkbenchMessages.EditorManager_saveResourcesMessage);

        dlg.setInitialSelections(saveEditors.toArray(new Object[saveEditors
                .size()]));
        dlg.setTitle(IDEWorkbenchMessages.EditorManager_saveResourcesTitle);
        int result = dlg.open();

        if (result == IDialogConstants.CANCEL_ID)
            return null;
        return Arrays.asList(dlg.getResult());
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected String getOperationMessage() {
        return IDEWorkbenchMessages.CloseResourceAction_operationMessage;
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected String getProblemsMessage() {
        return IDEWorkbenchMessages.CloseResourceAction_problemMessage;
    }

    /* (non-Javadoc)
     * Method declared on WorkspaceAction.
     */
    protected String getProblemsTitle() {
        return IDEWorkbenchMessages.CloseResourceAction_title;
    }

    protected void invokeOperation(IResource resource, IProgressMonitor monitor)
	        throws CoreException {
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
        if (!validateClose()) {
        	return;
        }
        //be conservative and include all projects in the selection - projects
        //can change state between now and when the job starts
    	ISchedulingRule rule = null;
    	IResourceRuleFactory factory = ResourcesPlugin.getWorkspace().getRuleFactory();
        Iterator resources = getSelectedResources().iterator();
        while (resources.hasNext()) {
            IProject project = (IProject) resources.next();
       		rule = MultiRule.combine(rule, factory.modifyRule(project));
        }
        runInBackground(rule);
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
    protected boolean shouldPerformResourcePruning() {
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
                IResourceDelta[] projDeltas = delta
                        .getAffectedChildren(IResourceDelta.CHANGED);
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
    
    /**
     * Returns the model provider ids that are known to the client
     * that instantiated this operation.
     * 
     * @return the model provider ids that are known to the client
     * that instantiated this operation.
     * @since 3.2
     */
	public String[] getModelProviderIds() {
		return modelProviderIds;
	}

	/**
     * Sets the model provider ids that are known to the client
     * that instantiated this operation. Any potential side effects
     * reported by these models during validation will be ignored.
     * 
	 * @param modelProviderIds the model providers known to the client
	 * who is using this operation.
	 * @since 3.2
	 */
	public void setModelProviderIds(String[] modelProviderIds) {
		this.modelProviderIds = modelProviderIds;
	}
	
	/**
	 * Validates the operation against the model providers.
	 * 
	 * @return whether the operation should proceed
	 */
    private boolean validateClose() {
    	IResourceChangeDescriptionFactory factory = ResourceChangeValidator.getValidator().createDeltaFactory();
    	List resources = getActionResources();
    	for (Iterator iter = resources.iterator(); iter.hasNext();) {
			IResource resource = (IResource) iter.next();
			if (resource instanceof IProject) {
				IProject project = (IProject) resource;
				factory.close(project);
			}
		}
    	String message;
    	if (resources.size() == 1) {
    		message = NLS.bind(IDEWorkbenchMessages.CloseResourceAction_warningForOne, ((IResource)resources.get(0)).getName());
    	} else {
    		message = IDEWorkbenchMessages.CloseResourceAction_warningForMultiple;
    	}
		return IDE.promptToConfirm(getShell(), IDEWorkbenchMessages.CloseResourceAction_confirm, message, factory.getDelta(), getModelProviderIds(), false /* no need to syncExec */);
	}
}
