/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.ide.actions;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * This class will perform an incremental build on a working set.
 * 
 * @since 3.0
 */
public class BuildSetAction extends Action {
    public static BuildSetAction lastBuilt;

    private IWorkingSet workingSet;

    private IWorkbenchWindow window;

    private IActionBarConfigurer actionBars;

    /**
     * Convenience method for extracting the selected projects
     * from a selection
     */
    protected static IProject[] extractProjects(Object[] selection) {
        HashSet projects = new HashSet();
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof IResource) {
                projects.add(((IResource) selection[i]).getProject());
            } else if (selection[i] instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selection[i];
                IResource resource = (IResource) adaptable
                        .getAdapter(IResource.class);
                if (resource != null)
                    projects.add(resource.getProject());
            }
        }
        return (IProject[]) projects.toArray(new IProject[projects.size()]);
    }

    /**
     * Creates a new action that builds the provided working set when run
     */
    public BuildSetAction(IWorkingSet set, IWorkbenchWindow window,
            IActionBarConfigurer actionBars) {
        super(set == null ? "" : set.getName(), AS_RADIO_BUTTON); //$NON-NLS-1$
        this.window = window;
        this.actionBars = actionBars;
        this.workingSet = set;
    }

    /**
     * Returns the working set that this instance is building
     */
    public IWorkingSet getWorkingSet() {
        return workingSet;
    }

    public void run() {
        lastBuilt = this;
        //register this action instance as the global handler for the build last action
        setActionDefinitionId("org.eclipse.ui.project.buildLast"); //$NON-NLS-1$
        actionBars.registerGlobalAction(this);

        window.getWorkbench().getWorkingSetManager().addRecentWorkingSet(
                workingSet);
        IProject[] projects = extractProjects(workingSet.getElements());
        if (projects.length == 0)
            return;
        BuildAction build = new BuildAction(window.getShell(),
                IncrementalProjectBuilder.INCREMENTAL_BUILD);
        build.selectionChanged(new StructuredSelection(projects));
        build.run();
    }

    public void runWithEvent(Event event) {
        //radio buttons receive an event when they become unselected,
        //so we must not run the action in this case
        if (event.widget instanceof MenuItem) {
            if (!((MenuItem) event.widget).getSelection())
                return;
        }
        run();
    }

    /* (non-Javadoc)
     * For debugging purposes only.
     */
    public String toString() {
        return "BuildSetAction(" + workingSet.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}