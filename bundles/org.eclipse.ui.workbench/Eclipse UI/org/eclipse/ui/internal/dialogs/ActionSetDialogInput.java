/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.*;

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
	private final static String ID_VIEW = "org.eclipse.ui.views"; //$NON-NLS-1$
	private final static String ID_WIZARD = "org.eclipse.ui.wizards"; //$NON-NLS-1$
	private final static String ID_PERSP = "org.eclipse.ui.perspectives"; //$NON-NLS-1$
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
 * Add the action sets for the given categories
 */
private void addActionSets(Object[] cats) {
	for (int nX = 0; nX < cats.length; nX ++) {
		WizardCollectionElement cat = (WizardCollectionElement)cats[nX];
		Object [] wizards = cat.getWizards();
		for (int nY = 0; nY < wizards.length; nY ++) {
			WorkbenchWizardElement wiz = (WorkbenchWizardElement)wizards[nY];
			FakeWizardActionSet actionSet = new FakeWizardActionSet(wiz);
			wizardCat.addActionSet(actionSet);
		}
		Object[] subCats = cat.getChildren();
		addActionSets(subCats);
	}
}


/**
 * Returns the category with a given id.
 */
public ActionSetCategory findCategory(String id) {
	if (id == null)
		return null;
	Iterator iter = categories.iterator();
	while (iter.hasNext()) {
		ActionSetCategory cat = (ActionSetCategory)iter.next();
		if (cat.getId().equals(id))
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
	wizardCat = new FakeActionSetCategory(ID_WIZARD, 
		WorkbenchMessages.getString("ActionSetDialogInput.wizardCategory")); //$NON-NLS-1$
	categories.add(wizardCat);
	// Get wizards categories.
	NewWizardsRegistryReader rdr = new NewWizardsRegistryReader();
	WizardCollectionElement wizardCollection = (WizardCollectionElement)rdr.getWizards();
	Object [] cats = wizardCollection.getChildren();
	addActionSets(cats);
}
/**
 * Initialize the perspective action sets.
 */
private void initPerspectives() {
	// Create fake category.
	perspCat = new FakeActionSetCategory(ID_PERSP,		
		WorkbenchMessages.getString("ActionSetDialogInput.perspectiveCategory")); //$NON-NLS-1$
	categories.add(perspCat);

	// Add elements.
	IPerspectiveRegistry perspReg = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	IPerspectiveDescriptor [] persps = perspReg.getPerspectives();
	for (int nX = 0; nX < persps.length; nX ++) {
		FakePerspectiveActionSet actionSet = new FakePerspectiveActionSet(persps[nX]);
		perspCat.addActionSet(actionSet);
	}
}
/**
 * Initialize the view action sets.
 */
private void initViews() {
	// Create fake category.
	viewCat = new FakeActionSetCategory(ID_VIEW, 
		WorkbenchMessages.getString("ActionSetDialogInput.viewCategory")); //$NON-NLS-1$
	categories.add(viewCat);

	// Add views.
	IViewRegistry viewReg = WorkbenchPlugin.getDefault().getViewRegistry();
	IViewDescriptor [] views = viewReg.getViews();
	for (int nX = 0; nX < views.length; nX ++) {
		FakeViewActionSet actionSet = new FakeViewActionSet(views[nX]);
		viewCat.addActionSet(actionSet);
	}
}
}
