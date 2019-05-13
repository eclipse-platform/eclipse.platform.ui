/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import java.util.Iterator;

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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.tests.harness.util.DialogCheck;

import junit.framework.TestCase;

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
			for (IProject project : projects) {
				if (project.getName().equals(PROJECT_NAME)) {
					project.delete(true, null);
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
			PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
					IWorkbenchHelpContextIds.PREFERENCE_DIALOG);

			for (Object element : manager.getElements(
					PreferenceManager.PRE_ORDER)) {
				IPreferenceNode node = (IPreferenceNode) element;
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

		IWorkbenchAdapter adapter = element.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) {
			name = adapter.getLabel(element);
		}

		// testing if there are pages in the manager
		Iterator<IPreferenceNode> pages = manager.getElements(PreferenceManager.PRE_ORDER)
				.iterator();
		if (!pages.hasNext()) {
			return null;
		}
			title = NLS.bind(WorkbenchMessages.PropertyDialog_propertyMessage, (new Object[] { name }));
			dialog = new PropertyDialogWrapper(getShell(), manager,
					new StructuredSelection(element));
			dialog.create();
			dialog.getShell().setText(title);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
					IWorkbenchHelpContextIds.PROPERTY_DIALOG);
			for (Object element2 : manager.getElements(
					PreferenceManager.PRE_ORDER)) {
			IPreferenceNode node = (IPreferenceNode) element2;
			if (node.getId().equals(id)) {
			dialog.showPage(node);
			break;
			}
		 }

		return dialog;
	}

	public void testWorkbenchPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.Workbench");
		DialogCheck.assertDialog(dialog);
	}

	public void testAppearancePref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.Views");
		DialogCheck.assertDialog(dialog);
	}

	public void testDefaultTextEditorPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.TextEditor");
		DialogCheck.assertDialog(dialog);
	}

	public void testFileEditorsPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.FileEditors");
		DialogCheck.assertDialog(dialog);
	}

	public void testLocalHistoryPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.FileStates");
		DialogCheck.assertDialog(dialog);
	}

	public void testPerspectivesPref() {
		Dialog dialog = getPreferenceDialog("org.eclipse.ui.preferencePages.Perspectives");
		DialogCheck.assertDialog(dialog);
	}

	public void testInfoProp() {
		Dialog dialog = getPropertyDialog("org.eclipse.ui.propertypages.info.file");
		DialogCheck.assertDialog(dialog);
	}

	public void testProjectReferencesProp() {
		Dialog dialog = getPropertyDialog("org.eclipse.ui.propertypages.project.reference");
		DialogCheck.assertDialog(dialog);
	}
}

