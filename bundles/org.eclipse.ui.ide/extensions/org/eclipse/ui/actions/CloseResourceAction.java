/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 41431, 462760, 461786
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
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
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Standard action for closing the currently selected project(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CloseResourceAction extends WorkspaceAction implements IResourceChangeListener {
    /**
     * The id of this action.
     */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CloseResourceAction"; //$NON-NLS-1$

	private String[] modelProviderIds;

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @deprecated See {@link #CloseResourceAction(IShellProvider)}
     */
    @Deprecated
	public CloseResourceAction(Shell shell) {
        super(shell, IDEWorkbenchMessages.CloseResourceAction_text);
        initAction();
    }

    /**
	 * Override super constructor to allow subclass to
	 * override with unique text.
	 * @deprecated See {@link #CloseResourceAction(IShellProvider, String)}
	 */
    @Deprecated
	protected CloseResourceAction(Shell shell, String text) {
    	super(shell, text);
    }

    /**
	 * Create the new action.
	 *
	 * @param provider
	 *            the shell provider for any dialogs
	 * @since 3.4
	 */
    public CloseResourceAction(IShellProvider provider) {
    	super(provider, IDEWorkbenchMessages.CloseResourceAction_text);
        initAction();
    }

    /**
	 * Provide text to the action.
	 *
	 * @param provider
	 *            the shell provider for any dialogs
	 * @param text
	 *            label
	 * @since 3.4
	 */
    protected CloseResourceAction(IShellProvider provider, String text) {
    	super(provider, text);
    }

	private void initAction() {
		setId(ID);
		setToolTipText(IDEWorkbenchMessages.CloseResourceAction_toolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.CLOSE_RESOURCE_ACTION);
	}

    @Override
	protected String getOperationMessage() {
        return IDEWorkbenchMessages.CloseResourceAction_operationMessage;
    }

    @Override
	protected String getProblemsMessage() {
        return IDEWorkbenchMessages.CloseResourceAction_problemMessage;
    }

    @Override
	protected String getProblemsTitle() {
        return IDEWorkbenchMessages.CloseResourceAction_title;
    }

    @Override
	protected void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
		((IProject) resource).close(monitor);
	}

    /**
     * The implementation of this <code>WorkspaceAction</code> method
     * method saves and closes the resource's dirty editors before closing
     * it.
     */
    @Override
	public void run() {
        // Get the items to close.
		List<? extends IResource> projects = getSelectedResources();
        if (projects == null || projects.isEmpty()) {
			// no action needs to be taken since no projects are selected
            return;
		}

		final IResource[] projectArray = projects.toArray(new IResource[projects.size()]);

		if (!IDE.saveAllEditors(projectArray, true)) {
			return;
		}
        if (!validateClose()) {
        	return;
        }

		closeMatchingEditors(projects, false);

        //be conservative and include all projects in the selection - projects
        //can change state between now and when the job starts
    	ISchedulingRule rule = null;
    	IResourceRuleFactory factory = ResourcesPlugin.getWorkspace().getRuleFactory();
		for (IResource element : projectArray) {
			IProject project = (IProject) element;
       		rule = MultiRule.combine(rule, factory.modifyRule(project));
        }
        runInBackground(rule);
    }

    @Override
	protected boolean shouldPerformResourcePruning() {
        return false;
    }

    /**
     * The <code>CloseResourceAction</code> implementation of this
     * <code>SelectionListenerAction</code> method ensures that this action is
     * enabled only if one of the selections is an open project.
     */
    @Override
	protected boolean updateSelection(IStructuredSelection s) {
        // don't call super since we want to enable if open project is selected.
        if (!selectionIsOfType(IResource.PROJECT)) {
			return false;
		}

		Iterator<? extends IResource> resources = getSelectedResources().iterator();
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
    @Override
	public synchronized void resourceChanged(IResourceChangeEvent event) {
        // Warning: code duplicated in OpenResourceAction
		List<? extends IResource> sel = getSelectedResources();
        // don't bother looking at delta if selection not applicable
        if (selectionIsOfType(IResource.PROJECT)) {
            IResourceDelta delta = event.getDelta();
            if (delta != null) {
				IResourceDelta[] projDeltas = delta.getAffectedChildren(IResourceDelta.CHANGED);
				for (IResourceDelta projDelta : projDeltas) {
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


    @Override
	protected synchronized List<? extends IResource> getSelectedResources() {
    	return super.getSelectedResources();
    }

    @Override
	protected synchronized List<?> getSelectedNonResources() {
    	return super.getSelectedNonResources();
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
		List<? extends IResource> resources = getActionResources();
		for (IResource resource : resources) {
			if (resource instanceof IProject) {
				IProject project = (IProject) resource;
				factory.close(project);
			}
		}
    	String message;
    	if (resources.size() == 1) {
    		message = NLS.bind(IDEWorkbenchMessages.CloseResourceAction_warningForOne, resources.get(0).getName());
    	} else {
    		message = IDEWorkbenchMessages.CloseResourceAction_warningForMultiple;
    	}
		return IDE.promptToConfirm(getShell(), IDEWorkbenchMessages.CloseResourceAction_confirm, message, factory.getDelta(), getModelProviderIds(), false /* no need to syncExec */);
	}

	/**
	 * Tries to find opened editors matching given resource roots. The editors
	 * will be closed without confirmation and only if the editor resource does
	 * not exists anymore.
	 *
	 * @param resourceRoots
	 *            non null array with deleted resource tree roots
	 * @param deletedOnly
	 *            true to close only editors on resources which do not exist
	 */
	static void closeMatchingEditors(final List<? extends IResource> resourceRoots, final boolean deletedOnly) {
		if (resourceRoots.isEmpty()) {
			return;
		}
		Runnable runnable = () -> SafeRunner.run(new SafeRunnable(IDEWorkbenchMessages.ErrorOnCloseEditors) {
			@Override
			public void run() {
				IWorkbenchWindow w = getActiveWindow();
				if (w != null) {
					List<IEditorReference> toClose = getMatchingEditors(resourceRoots, w, deletedOnly);
					if (toClose.isEmpty()) {
						return;
					}
					closeEditors(toClose, w);
				}
			}
		});
		BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), runnable);
	}

	private static IWorkbenchWindow getActiveWindow() {
		IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (w == null) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 0) {
				w = windows[0];
			}
		}
		return w;
	}

	private static List<IEditorReference> getMatchingEditors(final List<? extends IResource> resourceRoots,
			IWorkbenchWindow w, boolean deletedOnly) {
		List<IEditorReference> toClose = new ArrayList<>();
		IEditorReference[] editors = getEditors(w);
		for (IEditorReference ref : editors) {
			IResource resource = getAdapter(ref);
			// only collect editors for non existing resources
			if (resource != null && belongsTo(resourceRoots, resource)) {
				if (deletedOnly && resource.exists()) {
					continue;
				}
				toClose.add(ref);
			}
		}
		return toClose;
	}

	private static IEditorReference[] getEditors(IWorkbenchWindow w) {
		if (w != null) {
			IWorkbenchPage page = w.getActivePage();
			if (page != null) {
				return page.getEditorReferences();
			}
		}
		return new IEditorReference[0];
	}

	private static IResource getAdapter(IEditorReference ref) {
		IEditorInput input;
		try {
			input = ref.getEditorInput();
		} catch (PartInitException e) {
			// ignore if factory can't restore input, see bug 461786
			return null;
		}
		// here we can only guess how the input might be related to a resource
		IFile adapter = Adapters.getAdapter(input, IFile.class, true);
		if (adapter != null) {
			return adapter;
		}
		return Adapters.getAdapter(input, IResource.class, true);
	}

	private static boolean belongsTo(List<? extends IResource> roots, IResource leaf) {
		for (IResource resource : roots) {
			if (resource.contains(leaf)) {
				return true;
			}
		}
		return false;
	}

	private static void closeEditors(List<IEditorReference> toClose, IWorkbenchWindow w) {
		IWorkbenchPage page = w.getActivePage();
		if (page == null) {
			return;
		}
		page.closeEditors(toClose.toArray(new IEditorReference[toClose.size()]), false);
	}
}
