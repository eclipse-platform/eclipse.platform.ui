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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.help.WorkbenchHelp;
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
public abstract class UIWorkingSetWizardsAuto extends UITestCase {
    protected static final int SIZING_WIZARD_WIDTH = 470;

    protected static final int SIZING_WIZARD_HEIGHT = 550;

    protected static final int SIZING_WIZARD_WIDTH_2 = 500;

    protected static final int SIZING_WIZARD_HEIGHT_2 = 500;

    protected static final String WORKING_SET_NAME_1 = "ws1";

    protected static final String WORKING_SET_NAME_2 = "ws2";

    protected WizardDialog fWizardDialog;

    protected IWizard fWizard;

    protected IProject p1;

    protected IProject p2;

    protected IFile f1;

    protected IFile f2;

    public UIWorkingSetWizardsAuto(String name) {
        super(name);
    }

    protected void checkTreeItems() {
        List widgets = getWidgets((Composite) fWizardDialog.getCurrentPage()
                .getControl(), Tree.class);
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

    private void deleteResources() throws CoreException {
        try {
            if (p1 != null) {
                FileUtil.deleteProject(p1);
            }
            if (p2 != null) {
                FileUtil.deleteProject(p2);
            }

        } catch (CoreException e) {
            TestPlugin.getDefault().getLog().log(e.getStatus());
            fail();
            throw (e);

        }

    }

    private Shell getShell() {
        return DialogCheck.getShell();
    }

    protected List getWidgets(Composite composite, Class clazz) {
        Widget[] children = composite.getChildren();
        List selectedChildren = new ArrayList();

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

    /**
     * <code>fWizard</code> must be initialized by subclasses prior to
     * calling this.
     */
    @Override
	protected void doSetUp() throws Exception {
        super.doSetUp();

        fWizardDialog = new WizardDialog(getShell(), fWizard);
        fWizardDialog.create();
        Shell dialogShell = fWizardDialog.getShell();
        dialogShell.setSize(Math.max(SIZING_WIZARD_WIDTH_2, dialogShell
                .getSize().x), SIZING_WIZARD_HEIGHT_2);
        WorkbenchHelp.setHelp(fWizardDialog.getShell(),
                IWorkbenchHelpContextIds.WORKING_SET_NEW_WIZARD);

        IWorkingSetManager workingSetManager = fWorkbench
                .getWorkingSetManager();
        IWorkingSet[] workingSets = workingSetManager.getWorkingSets();
        for (IWorkingSet workingSet : workingSets) {
            workingSetManager.removeWorkingSet(workingSet);
        }
        setupResources();
    }

    private void setupResources() throws CoreException {
        p1 = FileUtil.createProject("TP1");
        p2 = FileUtil.createProject("TP2");
        f1 = FileUtil.createFile("f1.txt", p1);
        f2 = FileUtil.createFile("f2.txt", p2);
    }

    protected void setTextWidgetText(String text, IWizardPage page) {
        List widgets = getWidgets((Composite) page.getControl(), Text.class);
        Text textWidget = (Text) widgets.get(0);
        textWidget.setText(text);
        textWidget.notifyListeners(SWT.Modify, new Event());
    }

    @Override
	protected void doTearDown() throws Exception {
        deleteResources();
        super.doTearDown();
    }

    protected WorkingSetDescriptor[] getEditableWorkingSetDescriptors() {
        WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
        WorkingSetDescriptor[] all = registry.getWorkingSetDescriptors();
        ArrayList editable = new ArrayList(all.length);
        for (WorkingSetDescriptor descriptor : all) {
            if (descriptor.isEditable()) {
                editable.add(descriptor);
            }
        }
        return (WorkingSetDescriptor[]) editable.toArray(new WorkingSetDescriptor[editable.size()]);
    }

}
