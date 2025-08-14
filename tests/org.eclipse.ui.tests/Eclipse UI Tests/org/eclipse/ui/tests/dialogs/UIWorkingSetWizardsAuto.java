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

import static org.eclipse.ui.PlatformUI.getWorkbench;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Abstract test class for the working set wizard tests.
 */
public abstract class UIWorkingSetWizardsAuto<W extends IWizard> extends UITestCase {
	protected static final String WORKING_SET_NAME_1 = "ws1";

	protected static final String WORKING_SET_NAME_2 = "ws2";

	private static final int SIZING_WIZARD_WIDTH_2 = 500;

	private static final int SIZING_WIZARD_HEIGHT_2 = 500;

	private WizardDialog wizardDialog;

	private W wizardToTest;

	private IProject project1;

	private IProject project2;

	private IFile fileInProject2;

	public UIWorkingSetWizardsAuto(String name) {
		super(name);
	}

	protected WizardDialog getWizardDialog() {
		return wizardDialog;
	}

	protected W getWizard() {
		return wizardToTest;
	}

	protected IProject getProject1() {
		return project1;
	}

	protected IProject getProject2() {
		return project2;
	}

	protected IFile getFileInProject2() {
		return fileInProject2;
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		wizardToTest = createWizardToTest();
		wizardDialog = createWizardDialog();
		initializeTestResources();
	}

	protected abstract W createWizardToTest();

	private WizardDialog createWizardDialog() {
		Shell parentShell = DialogCheck.getShell();
		WizardDialog wizardDialog = new WizardDialog(parentShell, getWizard());
		wizardDialog.create();
		Shell dialogShell = wizardDialog.getShell();
		dialogShell.setSize(Math.max(SIZING_WIZARD_WIDTH_2, dialogShell.getSize().x), SIZING_WIZARD_HEIGHT_2);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialogShell, IWorkbenchHelpContextIds.WORKING_SET_NEW_WIZARD);
		return wizardDialog;
	}

	private void initializeTestResources() throws CoreException {
		project1 = FileUtil.createProject("TP1");
		project2 = FileUtil.createProject("TP2");
		FileUtil.createFile("f1.txt", project1);
		fileInProject2 = FileUtil.createFile("f2.txt", project2);
	}

	@Override
	protected void doTearDown() throws Exception {
		removeAllWorkingSets();
		cleanupWorkspace();
		disposeWizardAndDialog();
		super.doTearDown();
	}

	private void removeAllWorkingSets() {
		IWorkingSetManager workingSetManager = getWorkbench().getWorkingSetManager();
		IWorkingSet[] workingSets = workingSetManager.getWorkingSets();
		for (IWorkingSet workingSet : workingSets) {
			workingSetManager.removeWorkingSet(workingSet);
		}
	}

	private void cleanupWorkspace() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			root.delete(true, null);
		} catch (CoreException e) {
			// give it some more time
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e1) {
			}
			try {
				root.refreshLocal(IResource.DEPTH_INFINITE, null);
				if (root.exists()) {
					root.delete(true, null);
				}
			} catch (CoreException e1) {
				TestPlugin.getDefault().getLog().log(e.getStatus());
				throw createAssertionError(e);
			}
		} finally {
			project1 = null;
			project2 = null;
			fileInProject2 = null;
		}
	}

	private AssertionError createAssertionError(CoreException originalException) {
		Throwable cause = originalException.getCause();
		if (cause == null) {
			IStatus mostSevere = findMostSevere(originalException.getStatus());
			cause = mostSevere.getException();
		}

		return new AssertionError(originalException.getMessage(), cause);
	}

	private IStatus findMostSevere(IStatus status) {
		if (!status.isMultiStatus()) {
			return status;
		}

		IStatus mostSevere = null;

		for (IStatus childStatus : status.getChildren()) {
			IStatus mostSevereChild = findMostSevere(childStatus);
			if (mostSevere == null || mostSevereChild.getSeverity() > mostSevere.getSeverity()) {
				mostSevere = mostSevereChild;
			}
		}

		if (mostSevere == null) {
			mostSevere = status;
		}

		return mostSevere;
	}

	private void disposeWizardAndDialog() {
		wizardDialog = null;
		wizardToTest.dispose();
		wizardToTest = null;
	}

	protected void checkTreeItems() {
		List<Widget> widgets = getWidgets((Composite) getWizardDialog().getCurrentPage().getControl(), Tree.class);
		Tree tree = (Tree) widgets.get(0);
		TreeItem[] treeItems = tree.getItems();
		for (TreeItem treeItem : treeItems) {
			treeItem.setChecked(true);
			Event event = new Event();
			event.detail = SWT.CHECK;
			event.item = treeItem;
			tree.notifyListeners(SWT.Selection, event);
		}
	}

	protected List<Widget> getWidgets(Composite composite, Class<?> clazz) {
		Widget[] children = composite.getChildren();
		List<Widget> selectedChildren = new ArrayList<>();

		for (Widget child : children) {
			if (child.getClass() == clazz) {
				selectedChildren.add(child);
			}
			if (child instanceof Composite) {
				selectedChildren.addAll(getWidgets((Composite) child, clazz));
			}
		}
		return selectedChildren;
	}

	protected void setTextWidgetText(String text, IWizardPage page) {
		List<Widget> widgets = getWidgets((Composite) page.getControl(), Text.class);
		Text textWidget = (Text) widgets.get(0);
		textWidget.setText(text);
		textWidget.notifyListeners(SWT.Modify, new Event());
	}

	protected WorkingSetDescriptor[] getEditableWorkingSetDescriptors() {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		WorkingSetDescriptor[] all = registry.getWorkingSetDescriptors();
		ArrayList<WorkingSetDescriptor> editable = new ArrayList<>(all.length);
		for (WorkingSetDescriptor descriptor : all) {
			if (descriptor.isEditable()) {
				editable.add(descriptor);
			}
		}
		return editable.toArray(new WorkingSetDescriptor[editable.size()]);
	}

}
