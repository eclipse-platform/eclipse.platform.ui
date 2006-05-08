/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Implementation for the action Property on the Project menu.
 */
public class ProjectPropertyDialogAction extends PartEventAction implements
        INullSelectionListener, ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Create a new dialog.
     * 
     * @param window the window
     */
    public ProjectPropertyDialogAction(IWorkbenchWindow window) {
        super(new String());
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setText(IDEWorkbenchMessages.Workbench_projectProperties);
        setToolTipText(IDEWorkbenchMessages.Workbench_projectPropertiesToolTip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                IIDEHelpContextIds.PROJECT_PROPERTY_DIALOG_ACTION);
        workbenchWindow.getSelectionService().addSelectionListener(this);
        workbenchWindow.getPartService().addPartListener(this);
        setActionDefinitionId("org.eclipse.ui.project.properties"); //$NON-NLS-1$
    }

    /**
     * Opens the project properties dialog.
     */
    public void run() {
        IProject project = getProject();
        if (project == null) {
			return;
		}

        SelProvider selProvider = new SelProvider();
        selProvider.projectSelection = new StructuredSelection(project);
        PropertyDialogAction propAction = new PropertyDialogAction(
                workbenchWindow.getShell(), selProvider);
        propAction.run();
    }

    /**
     * Update the enablement state when a the selection changes.
     */
    public void selectionChanged(IWorkbenchPart part, ISelection sel) {
        setEnabled(getProject() != null);
    }

    /**
     * Update the enablement state when a new part is activated.
     */
    public void partActivated(IWorkbenchPart part) {
        super.partActivated(part);
        setEnabled(getProject() != null);
    }

    /**
     * Returns a project from the selection of the active part.
     */
    private IProject getProject() {
        IWorkbenchPart part = getActivePart();
        Object selection = null;
        if (part instanceof IEditorPart) {
            selection = ((IEditorPart) part).getEditorInput();
        } else {
            ISelection sel = workbenchWindow.getSelectionService()
                    .getSelection();
            if ((sel != null) && (sel instanceof IStructuredSelection)) {
				selection = ((IStructuredSelection) sel).getFirstElement();
			}
        }
        if (selection == null) {
			return null;
		}
        if (!(selection instanceof IAdaptable)) {
			return null;
		}
        IResource resource = (IResource) ((IAdaptable) selection)
                .getAdapter(IResource.class);
        if (resource == null) {
			return null;
		}
        return resource.getProject();
    }

    /* (non-javadoc)
     * Method declared on ActionFactory.IWorkbenchAction
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // action has already been disposed
            return;
        }
        workbenchWindow.getSelectionService().removeSelectionListener(this);
        workbenchWindow.getPartService().removePartListener(this);
        workbenchWindow = null;
    }

    /*
     * Helper class to simulate a selection provider
     */
    private static final class SelProvider implements ISelectionProvider {
        protected IStructuredSelection projectSelection = StructuredSelection.EMPTY;

        public void addSelectionChangedListener(
                ISelectionChangedListener listener) {
            // do nothing
        }

        public ISelection getSelection() {
            return projectSelection;
        }

        public void removeSelectionChangedListener(
                ISelectionChangedListener listener) {
            // do nothing
        }

        public void setSelection(ISelection selection) {
            // do nothing
        }
    }
}
