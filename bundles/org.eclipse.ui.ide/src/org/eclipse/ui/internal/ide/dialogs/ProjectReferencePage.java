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
package org.eclipse.ui.internal.ide.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A property page for viewing and modifying the set
 * of projects referenced by a given project.
 */
public class ProjectReferencePage extends PropertyPage {
    private IProject project;

    private boolean modified = false;

    //widgets
    private CheckboxTableViewer listViewer;

    private static final int PROJECT_LIST_MULTIPLIER = 30;

    /**
     * Creates a new ProjectReferencePage.
     */
    public ProjectReferencePage() {
        //Do nothing on creation
    }

    /**
     * @see PreferencePage#createContents
     */
    protected Control createContents(Composite parent) {

    	PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
                IIDEHelpContextIds.PROJECT_REFERENCE_PROPERTY_PAGE);
        Font font = parent.getFont();

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setFont(font);

        initialize();

        Label description = createDescriptionLabel(composite);
        description.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        listViewer = CheckboxTableViewer.newCheckList(composite, SWT.TOP
                | SWT.BORDER);
        listViewer.getTable().setFont(font);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.grabExcessHorizontalSpace = true;

        //Only set a height hint if it will not result in a cut off dialog
        if (DialogUtil.inRegularFontMode(parent))
            data.heightHint = getDefaultFontHeight(listViewer.getTable(),
                    PROJECT_LIST_MULTIPLIER);
        listViewer.getTable().setLayoutData(data);
        listViewer.getTable().setFont(font);

        listViewer.setLabelProvider(WorkbenchLabelProvider
                .getDecoratingWorkbenchLabelProvider());
        listViewer.setContentProvider(getContentProvider(project));
        listViewer.setSorter(new ViewerSorter());
        listViewer.setInput(project.getWorkspace());
        try {
            listViewer.setCheckedElements(project.getDescription()
                    .getReferencedProjects());
        } catch (CoreException e) {
            //don't initial-check anything
        }

        //check for initial modification to avoid work if no changes are made
        listViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                modified = true;
            }
        });

        return composite;
    }

    /**
     * Returns a content provider for the list dialog. It
     * will return all projects in the workspace except
     * the given project, plus any projects referenced
     * by the given project which do no exist in the
     * workspace.
     * @param project the project to provide content for
     * @return the content provider that shows the project content
     */
    protected IStructuredContentProvider getContentProvider(
            final IProject project) {
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object o) {
                if (!(o instanceof IWorkspace)) {
                    return new Object[0];
                }

                // Collect all the projects in the workspace except the given project
                IProject[] projects = ((IWorkspace) o).getRoot().getProjects();
                ArrayList referenced = new ArrayList(projects.length);
                boolean found = false;
                for (int i = 0; i < projects.length; i++) {
                    if (!found && projects[i].equals(project)) {
                        found = true;
                        continue;
                    }
                    referenced.add(projects[i]);
                }

                // Add any referenced that do not exist in the workspace currently
                try {
                    projects = project.getDescription().getReferencedProjects();
                    for (int i = 0; i < projects.length; i++) {
                        if (!referenced.contains(projects[i]))
                            referenced.add(projects[i]);
                    }
                } catch (CoreException e) {
                    //Ignore core exceptions
                }

                return referenced.toArray();
            }
        };
    }

    /**
     * Get the defualt widget height for the supplied control.
     * @return int
     * @param control - the control being queried about fonts
     * @param lines - the number of lines to be shown on the table.
     */
    private static int getDefaultFontHeight(Control control, int lines) {
        FontData[] viewerFontData = control.getFont().getFontData();
        int fontHeight = 10;

        //If we have no font data use our guess
        if (viewerFontData.length > 0)
            fontHeight = viewerFontData[0].getHeight();
        return lines * fontHeight;

    }

    /**
     * Handle the exception thrown when saving.
     * @param e the exception
     */
    protected void handle(InvocationTargetException e) {
        IStatus error;
        Throwable target = e.getTargetException();
        if (target instanceof CoreException) {
            error = ((CoreException) target).getStatus();
        } else {
            String msg = target.getMessage();
            if (msg == null)
                msg = IDEWorkbenchMessages.Internal_error;
            error = new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
                    1, msg, target);
        }
        ErrorDialog.openError(getControl().getShell(), null, null, error);
    }

    /**
     * Initializes a ProjectReferencePage.
     */
    private void initialize() {
        project = (IProject) getElement().getAdapter(IResource.class);
        noDefaultAndApplyButton();
        setDescription(NLS.bind(IDEWorkbenchMessages.ProjectReferencesPage_label, project.getName()));
    }

    /**
     * @see PreferencePage#performOk
     */
    public boolean performOk() {
        if (!modified)
            return true;
        Object[] checked = listViewer.getCheckedElements();
        final IProject[] refs = new IProject[checked.length];
        System.arraycopy(checked, 0, refs, 0, checked.length);
        IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException {

                try {
                    IProjectDescription description = project.getDescription();
                    description.setReferencedProjects(refs);
                    project.setDescription(description, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }
            }
        };
        try {
            new ProgressMonitorJobsDialog(getControl().getShell()).run(true,
                    true, runnable);
        } catch (InterruptedException e) {
            //Ignore interrupted exceptions
        } catch (InvocationTargetException e) {
            handle(e);
            return false;
        }
        return true;
    }
}
