package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.*;
import org.eclipse.ui.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.model.AdaptableList;
import java.util.*;

/**
 * This class collates the input for an action set dialog.  There
 * are a number of sources:
 *		- the action sets
 *		- the views
 *		- the perspectives
 *		- the new wizards
 */
public class ActionSetDialogInput {
	private ArrayList categories = new ArrayList(10);
	public final static String STR_OTHER = "Other";
	private final static String STR_VIEW = "Views";
	private final static String STR_WIZARD = "New Wizards";
	private final static String STR_PERSP = "Perspectives";
	private FakeActionSetCategory viewCat;
	private FakeActionSetCategory perspCat;
	private FakeActionSetCategory wizardCat;
/**
 * ActionSetContent constructor comment.
 */
public ActionSetDialogInput() {
	super();
	initActionSets();
	initViews();
	initNewWizards();
	initPerspectives();
}
/**
 * Returns the category with a given id.
 */
public ActionSetCategory findCategory(String id) {
	if (id == null)
		id = "Other";
	Iterator iter = categories.iterator();
	while (iter.hasNext()) {
		ActionSetCategory cat = (ActionSetCategory)iter.next();
		if (cat.getLabel().equals(id))
			return cat;
	}
	return null;
}
/**
 * Returns the categories.
 */
public Object [] getCategories() {
	return categories.toArray();
}
/**
 * Returns the fake persp action for a particular id.
 */
public FakePerspectiveActionSet getPerspectiveActionSet(String id) {
	return (FakePerspectiveActionSet)perspCat.findActionSet(id);
}
/**
 * Returns the fake view action for a particular id.
 */
public FakeViewActionSet getViewActionSet(String id) {
	return (FakeViewActionSet)viewCat.findActionSet(id);
}
/**
 * Returns the fake wizard action for a particular id.
 */
public FakeWizardActionSet getWizardActionSet(String id) {
	return (FakeWizardActionSet)wizardCat.findActionSet(id);
}
/**
 * Initialize the registered action sets.
 */
private void initActionSets() {
	ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
	ActionSetCategory [] cats = reg.getCategories();
	for (int nX = 0; nX < cats.length; nX ++) {
		categories.add(cats[nX]);
	}
}
/**
 * Initialize the new wizard action sets.
 */
private void initNewWizards() {
	// Create fake category.
	wizardCat = new FakeActionSetCategory(STR_WIZARD);
	categories.add(wizardCat);

	// Get wizards categories.
	NewWizardsRegistryReader rdr = new NewWizardsRegistryReader();
	WizardCollectionElement wizardCollection = (WizardCollectionElement)rdr.getWizards();
	Object [] cats = wizardCollection.getChildren();
	for (int nX = 0; nX < cats.length; nX ++) {
		WizardCollectionElement cat = (WizardCollectionElement)cats[nX];
		Object [] wizards = cat.getWizards();
		for (int nY = 0; nY < wizards.length; nY ++) {
			WorkbenchWizardElement wiz = (WorkbenchWizardElement)wizards[nY];
			FakeWizardActionSet actionSet = new FakeWizardActionSet(wiz);
			actionSet.setCategory(STR_WIZARD);
			wizardCat.addActionSet(actionSet);
		}
	}
}
/**
 * Initialize the perspective action sets.
 */
private void initPerspectives() {
	// Create fake category.
	perspCat = new FakeActionSetCategory(STR_PERSP);
	categories.add(perspCat);

	// Add views.
	IPerspectiveRegistry perspReg = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	IPerspectiveDescriptor [] persps = perspReg.getPerspectives();
	for (int nX = 0; nX < persps.length; nX ++) {
		FakePerspectiveActionSet actionSet = new FakePerspectiveActionSet(persps[nX]);
		actionSet.setCategory(STR_PERSP);
		perspCat.addActionSet(actionSet);
	}
}
/**
 * Initialize the view action sets.
 */
private void initViews() {
	// Create fake category.
	viewCat = new FakeActionSetCategory(STR_VIEW);
	categories.add(viewCat);

	// Add views.
	IViewRegistry viewReg = WorkbenchPlugin.getDefault().getViewRegistry();
	IViewDescriptor [] views = viewReg.getViews();
	for (int nX = 0; nX < views.length; nX ++) {
		FakeViewActionSet actionSet = new FakeViewActionSet(views[nX]);
		actionSet.setCategory(STR_VIEW);
		viewCat.addActionSet(actionSet);
	}
}
}
