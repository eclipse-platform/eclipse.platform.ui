package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 * Page for selecting a wizard from a group of available wizards.
 */
public abstract class WorkbenchWizardSelectionPage extends WizardSelectionPage {

	// variables
	protected IWorkbench workbench;
	protected AdaptableList wizardElements;
	public TableViewer wizardSelectionViewer;
	protected IStructuredSelection currentResourceSelection;
/**
 *	Create an instance of this class
 */
public WorkbenchWizardSelectionPage(String name, IWorkbench aWorkbench, IStructuredSelection currentSelection, AdaptableList elements) {
	super(name);
	this.wizardElements = elements;
	this.currentResourceSelection = currentSelection;
	this.workbench = aWorkbench;
	setTitle(WorkbenchMessages.getString("Select")); //$NON-NLS-1$
}
/**
 *	Answer the wizard object corresponding to the passed id, or null
 *	if such an object could not be found
 *
 *	@return WizardElement
 *	@param searchPath java.lang.String
 */
protected WorkbenchWizardElement findWizard(String searchId) {
	Object[] children = wizardElements.getChildren();
	for (int i = 0; i < children.length; ++i) {
		WorkbenchWizardElement currentWizard = (WorkbenchWizardElement)children[i];
		if (currentWizard.getID().equals(searchId))
			return currentWizard;
	}
	
	return null;
}
public IStructuredSelection getCurrentResourceSelection() {
	return currentResourceSelection;
}
public IWorkbench getWorkbench() {
	return this.workbench;
}
/**
 *	Specify the passed wizard node as being selected, meaning that if
 *	it's non-null then the wizard to be displayed when the user next
 *	presses the Next button should be determined by asking the passed
 *	node.
 *
 *	@param node org.eclipse.jface.wizards.IWizardNode
 */
public void selectWizardNode(IWizardNode node) {
	setSelectedNode(node);
}
}
