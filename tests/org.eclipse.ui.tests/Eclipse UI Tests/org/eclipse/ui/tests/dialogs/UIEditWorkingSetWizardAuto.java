package org.eclipse.ui.tests.dialogs;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.ui.tests.util.*;
import org.eclipse.ui.tests.util.ArrayUtil;
import org.eclipse.ui.tests.util.DialogCheck;
import org.eclipse.ui.wizards.newresource.*;

/**
 * Tests the WorkingSetEditWizard
 * Tests input validation, presence of correct edit page and 
 * wizard page texts.
 */
public class UIEditWorkingSetWizardAuto extends UIWorkingSetWizardsAuto {
	IWorkingSetPage fDefaultEditPage;
		
	public UIEditWorkingSetWizardAuto(String name) {
		super(name);
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		fDefaultEditPage = registry.getDefaultWorkingSetPage();
		fWizard = new WorkingSetEditWizard(fDefaultEditPage);
		super.setUp();
	}
	public void testEditPage() throws Throwable {
		IWizardPage page = fWizardDialog.getCurrentPage();
		assertTrue(page instanceof IWorkingSetPage);

		/*
		 * Verify that correct working set edit page is displayed
		 */
		assertTrue(page.getClass() == fDefaultEditPage.getClass());
		/*
		 * Test initial page state
		 */
		assertTrue(page.canFlipToNextPage() == false);
		assertTrue(fWizard.canFinish() == false);						
		assertNull(page.getErrorMessage());
		/*
		 * Test page state with preset page input
		 */
		IWorkingSetManager workingSetManager = fWorkbench.getWorkingSetManager();
		IWorkingSet workingSet = workingSetManager.createWorkingSet(WORKING_SET_NAME_1, new IAdaptable[] {p1, f2});
		((WorkingSetEditWizard) fWizard).setSelection(workingSet);
						 
		List widgets = getWidgets(fWizardDialog.getShell(), Text.class);
		Text text = (Text) widgets.get(0);
		assertEquals(WORKING_SET_NAME_1, text.getText());
		assertTrue(page.canFlipToNextPage() == false);
		assertTrue(fWizard.canFinish() == false);
		assertNull(page.getErrorMessage());		
		widgets = getWidgets(fWizardDialog.getShell(), Tree.class);
		Tree tree = (Tree) widgets.get(0);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();		
		assertEquals(workspace.getRoot().getProjects().length, tree.getItemCount());
		setTextWidgetText(WORKING_SET_NAME_2);
		assertTrue(fWizard.canFinish());
		
		/*
		 * Test page state with partial page input
		 */
 		setTextWidgetText("");
		assertTrue(page.canFlipToNextPage() == false);
		assertTrue(fWizard.canFinish() == false);		
		assertNotNull(page.getErrorMessage());		

		/*
		 * Test page state with complete page input
		 */
		setTextWidgetText(WORKING_SET_NAME_2);
		checkTreeItems();
		assertTrue(page.canFlipToNextPage() == false);
		assertTrue(fWizard.canFinish());
		assertNull(page.getErrorMessage());
		
		fWizard.performFinish();
		workingSet = ((WorkingSetEditWizard) fWizard).getSelection();
		IAdaptable[] workingSetItems = workingSet.getElements();
		assertEquals(WORKING_SET_NAME_2, workingSet.getName());
		assertTrue(ArrayUtil.contains(workingSetItems, p1));
		assertTrue(ArrayUtil.contains(workingSetItems, p2));

		DialogCheck.assertDialogTexts(fWizardDialog, this);
	}
}

