/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ant.internal.ui.toolscripts;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * For use in a tool script wizard.  Prompts for:
 *  - script name, which can be any file in the filesystem or workbench.  
 *  - script parameters, which is an arbitrary string
 * Enables finishing when any script name has been provided.
 */
public class ChooseToolScriptPage extends WizardPage {
	protected Button optionWorkspace, optionFileSystem;
	protected TreeViewer wsTree;
	protected Text fsEntryField;
/**
 * Constructor for ChooseToolScriptPage.
 * @param pageName
 */
protected ChooseToolScriptPage(String pageName) {
	super(pageName);
}

/**
 * Constructor for ChooseToolScriptPage.
 * @param pageName
 * @param title
 * @param titleImage
 */
protected ChooseToolScriptPage(String pageName, String title, ImageDescriptor titleImage) {
	super(pageName, title, titleImage);
}
/*
 * @see IDialogPage#createControl(Composite)
 */
public void createControl(Composite parent) {
	Composite topLevel = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	topLevel.setLayout(layout);

	//option 1 -- use workspace file
	optionWorkspace = new Button(topLevel, SWT.RADIO);
	optionWorkspace.setText("Select From Workspace");
	optionWorkspace.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	Tree tree = new Tree(topLevel, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
	tree.setLayoutData(new GridData(GridData.FILL_BOTH));
	wsTree = new TreeViewer(tree);
	wsTree.setContentProvider(new WorkbenchContentProvider());
	wsTree.setLabelProvider(new WorkbenchLabelProvider());
	wsTree.setInput(ResourcesPlugin.getWorkspace().getRoot());

	//option 2 -- use filesystem
	optionFileSystem = new Button(topLevel, SWT.RADIO);
	optionFileSystem.setText("In file system");
	optionFileSystem.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	Composite fsGroup = new Composite(topLevel, SWT.NONE);
	layout = new GridLayout();
	layout.numColumns = 2;
	fsGroup.setLayout(layout);
	fsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
	fsEntryField = new Text(fsGroup, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
	fsEntryField.setLayoutData(data);
	
	Button browse = new Button(fsGroup, SWT.PUSH);
	data = new GridData();
	data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
	data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	browse.setLayoutData(data);
	
	setControl(topLevel);
}
}
