package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.BusyIndicator;
import java.util.*;

/**
 * A wizard node represents a "potential" wizard. Wizard nodes
 * are used by wizard selection pages to allow the user to pick
 * from several available nested wizards.
 * <p>
 * <b>Subclasses</b> simply need to overide method <code>createWizard()</code>,
 * which is responsible for creating an instance of the wizard it represents
 * AND ensuring that this wizard is the "right" type of wizard (eg.-
 * New, Import, etc.).</p>
 */
public abstract class WorkbenchWizardNode implements IWizardNode {
	protected WorkbenchWizardElement wizardElement;
	protected IWizard wizard;
	protected WorkbenchWizardSelectionPage parentWizardPage;
/**
 * Creates a <code>WorkbenchWizardNode</code> that holds onto a wizard
 * element.  The wizard element provides information on how to create
 * the wizard supplied by the ISV's extension.
 */
public WorkbenchWizardNode(WorkbenchWizardSelectionPage aWizardPage, WorkbenchWizardElement element) {
	super(); 
	this.parentWizardPage = aWizardPage;
	this.wizardElement = element;
}
/**
 * Attempt to convert the elements in the passed selection into resources
 * by asking each for its IResource property (iff it isn't already a resource).
 * If all elements in the initial selection can be converted to resources then
 * answer a new selection containing these resources; otherwise answer a new
 * empty selection
 *
 * @param originalSelection IStructuredSelection
 * @return IStructuredSelection
 */
protected IStructuredSelection convertToResources(IStructuredSelection originalSelection) {
	List result = new ArrayList();
	Iterator elements = originalSelection.iterator();
	
	while (elements.hasNext()) {
		Object currentElement = elements.next();
		if (currentElement instanceof IResource) {				// already a resource
			result.add(currentElement);
		} else if (!(currentElement instanceof IAdaptable))	{	// cannot be converted to resource
			return new StructuredSelection();					// so fail
		} else {
			Object adapter = ((IAdaptable)currentElement).getAdapter(IResource.class);
			if (!(adapter instanceof IResource))				// chose not to be converted to resource
				return new StructuredSelection();				// so fail
			result.add(adapter);							// add the converted resource
		}
	}
	
	return new StructuredSelection(result.toArray());						// all converted fine, answer new selection
}
/**
 * Returns the wizard represented by this wizard node.  <b>Subclasses</b>
 * must override this method.
 */
public abstract IWorkbenchWizard createWizard() throws CoreException;
/**
 * @see org.eclipse.jface.wizard.IWizardNode#dispose()
 */
public void dispose() {
	// Do nothing since the wizard wasn't created via reflection.
}
/**
 * Returns the current resource selection that is being given to the wizard.
 */
protected IStructuredSelection getCurrentResourceSelection() {
	return parentWizardPage.getCurrentResourceSelection();
}
/**
 * @see org.eclipse.jface.wizard.IWizardNode#getExtent()
 */
public Point getExtent() {
	return new Point(-1,-1);
}
/**
 * @see org.eclipse.jface.wizards.IWizardNode#getWizard()
 */
public IWizard getWizard() {
	if (wizard != null)
		return wizard;						// we've already created it

	final IWorkbenchWizard[] workbenchWizard = new IWorkbenchWizard[1];
	final CoreException ex[] = new CoreException[1];
	// Start busy indicator.
	BusyIndicator.showWhile(parentWizardPage.getShell().getDisplay(), new Runnable() {
		public void run() {
			try {
				workbenchWizard[0] = createWizard();		// create instance of target wizard
			} catch (CoreException e) {
				ex[0] = e;
			}
		}
	});

	if (ex[0] != null) {
		parentWizardPage.setErrorMessage(WorkbenchMessages.getString("WorkbenchWizard.errorMessage")); //$NON-NLS-1$
		ErrorDialog.openError(
			parentWizardPage.getShell(),
			WorkbenchMessages.getString("WorkbenchWizard.errorTitle"), //$NON-NLS-1$
			WorkbenchMessages.getString("WorkbenchWizard.errorMessage"), //$NON-NLS-1$
			ex[0].getStatus());
		return null;
	}

	IStructuredSelection currentSelection = getCurrentResourceSelection();
	
	if (!wizardElement.canHandleSelection(currentSelection)) {
		currentSelection = convertToResources(currentSelection);
		if (!currentSelection.isEmpty() && !wizardElement.canHandleSelection(currentSelection))
			currentSelection = new StructuredSelection();
	}
		
	workbenchWizard[0].init(getWorkbench(),currentSelection);

	wizard = workbenchWizard[0];
	return wizard;
}
/**
 * Returns the wizard element.
 */
public WorkbenchWizardElement getWizardElement() {
	return wizardElement;
}
/**
 * Returns the current workbench.
 */
protected IWorkbench getWorkbench() {
	return parentWizardPage.getWorkbench();
}
/**
 * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
 */
public boolean isContentCreated() {
	return wizard != null;
}
}
