package org.eclipse.ui.tests.dialogs;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.tests.util.DialogCheck;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.wizards.newresource.*;

/**
 * Abstract test class for the working set wizard tests.
 */
public abstract class UIWorkingSetWizardsAuto extends TestCase {
	protected static final int SIZING_WIZARD_WIDTH    = 470;
	protected static final int SIZING_WIZARD_HEIGHT   = 550;
	protected static final int SIZING_WIZARD_WIDTH_2  = 500;
	protected static final int SIZING_WIZARD_HEIGHT_2 = 500;
	protected static final String WORKING_SET_NAME_1 = "ws1";
	protected static final String WORKING_SET_NAME_2 = "ws2";
	
	protected WizardDialog fWizardDialog;
	protected Wizard fWizard;
	protected WorkingSetDescriptor[] fWorkingSetDescriptors;
	protected IProject p1;
	protected IProject p2;
	protected IFile f1;
	protected IFile f2;
	
	public UIWorkingSetWizardsAuto(String name) {
		super(name);
	}
	protected void checkTreeItems() {
		List widgets = getWidgets(fWizardDialog.getShell(), Tree.class);
		Tree tree = (Tree) widgets.get(0);
		TreeItem[] treeItems = tree.getItems();
		for (int i = 0; i < treeItems.length; i++) {
			treeItems[i].setChecked(true);
			Event event = new Event();
			event.detail = SWT.CHECK;
			event.item = treeItems[i];
			tree.notifyListeners(SWT.Selection, event);
		}
	}
	private Shell getShell() {
		return DialogCheck.getShell();
	}
	protected List getWidgets(Composite composite, Class clazz) {
		Widget[] children = composite.getChildren();
		List selectedChildren = new ArrayList();
		
		for (int i = 0; i < children.length; i++) {
			Widget child = children[i];
			if (child.getClass() == clazz) {
				selectedChildren.add(child);
			}
			if (child instanceof Composite) {
				selectedChildren.addAll(getWidgets((Composite) child, clazz));
			}
		}
		return selectedChildren;
	}
	protected IWorkbench getWorkbench() {
		return WorkbenchPlugin.getDefault().getWorkbench();
	}
	/**
	 * <code>fWizard</code> must be initialized by subclasses prior to calling setUp.
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	
		fWizardDialog = new WizardDialog(getShell(), fWizard);
		fWizardDialog.create();
		Shell dialogShell = fWizardDialog.getShell();
		dialogShell.setSize(Math.max(SIZING_WIZARD_WIDTH_2, dialogShell.getSize().x), SIZING_WIZARD_HEIGHT_2);
		WorkbenchHelp.setHelp(fWizardDialog.getShell(), IHelpContextIds.WORKING_SET_NEW_WIZARD);
		
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		fWorkingSetDescriptors = registry.getWorkingSetDescriptors();
	}
	protected void setupResources() throws Throwable {
		if (p1 == null) {
			p1 = FileUtil.createProject("TP1");
			f1 = null;
		}
		if (p2 == null) {
			p2 = FileUtil.createProject("TP2");
			f2 = null;
		}
		if (f1 == null)
			f1 = FileUtil.createFile("f1.txt", p1);
		if (f2 == null)
			f2 = FileUtil.createFile("f2.txt", p2);
	}
	protected void setTextWidgetText(String text) {
		List widgets = getWidgets(fWizardDialog.getShell(), Text.class);
		Text textWidget = (Text) widgets.get(0);
		textWidget.setText(text);
		textWidget.notifyListeners(SWT.Modify, new Event());
	}
}

