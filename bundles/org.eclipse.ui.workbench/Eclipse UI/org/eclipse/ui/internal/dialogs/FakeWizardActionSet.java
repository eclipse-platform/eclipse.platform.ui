package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * A fake view action set.
 */
public class FakeWizardActionSet extends FakeActionSetDescriptor {
/**
 * Constructs a new action set.
 */
public FakeWizardActionSet(WorkbenchWizardElement desc) {
	super(desc.getID(), desc);
}
/**
 * Returns the action image descriptor.
 */
protected ImageDescriptor getActionImageDescriptor() {
	return getWizard().getImageDescriptor();
}
/**
 * Returns the action text.
 */
protected String getActionLabel() {
	WorkbenchWizardElement wizard = getWizard();
	return wizard.getLabel(wizard);
}
/**
 * Returns the descriptor
 */
public WorkbenchWizardElement getWizard() {
	return (WorkbenchWizardElement)getData();
}
}
