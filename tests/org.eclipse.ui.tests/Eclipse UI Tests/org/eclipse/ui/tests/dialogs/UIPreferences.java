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
package org.eclipse.ui.tests.dialogs;

import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.tests.harness.util.DialogCheck;

public class UIPreferences extends TestCase {
    private IProject _project;

    private static final String PROJECT_NAME = "DummyProject";

    public UIPreferences(String name) {
        super(name);
    }

    private Shell getShell() {
        return DialogCheck.getShell();
    }

    private IProject getDummyProject() {
        try {
            IProject projects[] = ResourcesPlugin.getWorkspace().getRoot()
                    .getProjects();
            for (int i = 0; i < projects.length; i++) {
                if (projects[i].getName().equals(PROJECT_NAME)) {
                    projects[i].delete(true, null);
                    break;
                }
            }
            _project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                    PROJECT_NAME);
            _project.create(null);
        } catch (CoreException e) {
            System.out.println(e);
        }
        return _project;
    }

    private PreferenceDialog getPreferenceDialog(String id) {
        PreferenceDialogWrapper dialog = null;
        PreferenceManager manager = WorkbenchPlugin.getDefault()
                .getPreferenceManager();
        if (manager != null) {
            dialog = new PreferenceDialogWrapper(getShell(), manager);
            dialog.create();
            WorkbenchHelp.setHelp(dialog.getShell(),
                    IWorkbenchHelpContextIds.PREFERENCE_DIALOG);

            for (Iterator iterator = manager.getElements(
                    PreferenceManager.PRE_ORDER).iterator(); iterator.hasNext();) {
                IPreferenceNode node = (IPreferenceNode) iterator.next();
                if (node.getId().equals(id)) {
                    dialog.showPage(node);
                    break;
                }
            }
        }
        return dialog;
    }

    private PropertyDialog getPropertyDialog(String id) {
        PropertyDialogWrapper dialog = null;

        PropertyPageManager manager = new PropertyPageManager();
        String title = "";
        String name = "";

        IProject element = getDummyProject();
        if (element == null) {
            return null;
        }
        // load pages for the selection
        // fill the manager with contributions from the matching contributors
        PropertyPageContributorManager.getManager()
                .contribute(manager, element);

        IWorkbenchAdapter adapter = (IWorkbenchAdapter) element
                .getAdapter(IWorkbenchAdapter.class);
        if (adapter != null) {
            name = adapter.getLabel(element);
        }

        // testing if there are pages in the manager
        Iterator pages = manager.getElements(PreferenceManager.PRE_ORDER)
                .iterator();
        if (!pages.hasNext()) {
            return null;
        } 
            title = NLS.bind(WorkbenchMessages.PropertyDialog_propertyMessage, (new Object[] { name }));
            dialog = new PropertyDialogWrapper(getShell(), manager,
                    new StructuredSelection(element));
            dialog.create();
            dialog.getShell().setText(title);
            WorkbenchHelp.setHelp(dialog.getShell(),
                    IWorkbenchHelpContextIds.PROPERTY_DIALOG);
            for (Iterator iterator = manager.getElements(
                    PreferenceManager.PRE_ORDER).iterator(); iterator.hasNext();) {
                IPreferenceNode node = (IPreferenceNode) iterator.next();
                if (node.getId().equals(id)) {
                    dialog.showPage(node);
                    break;
                }
            }
        
        return dialog;
    }

    public void testWorkbenchPref() {
        Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.Workbench");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testAppearancePref() {
        Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.Views");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testDefaultTextEditorPref() {
        Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.TextEditor");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testFileEditorsPref() {
        Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.FileEditors");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testLocalHistoryPref() {
        Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.FileStates");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testPerspectivesPref() {
        Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.Perspectives");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testInfoProp() {
        Dialog dialog = getPropertyDialog("org.eclipse.ui.propertypages.info.file");
        DialogCheck.assertDialog(dialog, this);
    }

    public void testProjectReferencesProp() {
        Dialog dialog = getPropertyDialog("org.eclipse.ui.propertypages.project.reference");
        DialogCheck.assertDialog(dialog, this);
    }
}

