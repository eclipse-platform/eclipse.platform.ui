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
package org.eclipse.ui.internal.registry;

import java.text.Collator;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.misc.Sorter;
import org.eclipse.ui.internal.model.AdaptableList;

/**
 *	Instances of this class provide a simple API to the workbench for
 *	accessing of the core registry.  It accepts a registry at creation
 *	time and extracts workbench-related information from it as requested.
 */
public class NewWizardsRegistryReader extends WizardsRegistryReader {
	
	private boolean projectsOnly;
	private ArrayList deferWizards = null;
	private ArrayList deferCategories = null;
	
	// constants
	public final static String		BASE_CATEGORY = "Base";//$NON-NLS-1$
	public final static String		EXAMPLES_WIZARD_CATEGORY = "Examples";//$NON-NLS-1$
	public final static String		FULL_EXAMPLES_WIZARD_CATEGORY = "org.eclipse.ui.Examples";//$NON-NLS-1$
	private final static String		TAG_CATEGORY = "category";	//$NON-NLS-1$
	private final static String		UNCATEGORIZED_WIZARD_CATEGORY = "org.eclipse.ui.Other";//$NON-NLS-1$
	private final static String		UNCATEGORIZED_WIZARD_CATEGORY_LABEL = WorkbenchMessages.getString("NewWizardsRegistryReader.otherCategory");//$NON-NLS-1$
	private final static String		CATEGORY_SEPARATOR = "/";//$NON-NLS-1$
	private final static String		ATT_CATEGORY = "category";//$NON-NLS-1$
	private final static String ATT_PROJECT = "project";//$NON-NLS-1$
	private final static String STR_TRUE = "true";//$NON-NLS-1$

	private class CategoryNode {
		private Category category;
		private String path;
		public CategoryNode(Category cat) {
			category = cat;
			path = ""; //$NON-NLS-1$
			String[] categoryPath = category.getParentPath();
			if (categoryPath != null) {
				for (int nX = 0; nX < categoryPath.length; nX ++) {
					path += categoryPath[nX] + '/'; //$NON-NLS-1$
				}
			}
			path += cat.getId();
		}
		public String getPath() {
			return path;
		}
		public Category getCategory() {
			return category;
		}
	}
/**
 * Constructs a new reader.  All wizards are read, including projects.
 */
public NewWizardsRegistryReader() {
	this(false);
}
/**
 * Constructs a new reader.
 *
 * @param projectsOnly if true, only projects are read.
 */
public NewWizardsRegistryReader(boolean projectsOnly) {
	super(IWorkbenchConstants.PL_NEW);
	this.projectsOnly = projectsOnly;
}
/* (non-Javadoc)
 * Method declared on WizardRegistryReader.  
 * <p>
 * This implementation uses a defering strategy.  For more info see
 * <code>readWizards</code>.
 * </p>
 */
protected void addNewElementToResult(WorkbenchWizardElement element, IConfigurationElement config, AdaptableList result) {
	deferWizard(element);
}
/**
 *	Create and answer a new WizardCollectionElement, configured as a
 *	child of <code>parent</code>
 *
 *	@return org.eclipse.ui.internal.model.WizardCollectionElement
 *	@param parent org.eclipse.ui.internal.model.WizardCollectionElement
 *	@param childName java.lang.String
 */
protected WizardCollectionElement createCollectionElement(WizardCollectionElement parent, String id, String label) {
	WizardCollectionElement newElement = new WizardCollectionElement(id, label, parent);

	parent.add(newElement);
	return newElement;
}
/**
 * Creates empty element collection. Overrider to fill
 * initial elements, if needed.
 */
protected AdaptableList createEmptyWizardCollection() {
	return new WizardCollectionElement("root", "root", null);//$NON-NLS-2$//$NON-NLS-1$
}
/**
 * Returns a new WorkbenchWizardElement configured according to the parameters
 * contained in the passed Registry.  
 *
 * May answer null if there was not enough information in the Extension to create 
 * an adequate wizard
 */
protected WorkbenchWizardElement createWizardElement(IConfigurationElement element) {
	if (projectsOnly) {
		String flag = element.getAttribute(ATT_PROJECT);
		if (flag == null || !flag.equalsIgnoreCase(STR_TRUE))
			return null;
	}
	return super.createWizardElement(element);
}
/**
 * Stores a category element for deferred addition.
 */
private void deferCategory(IConfigurationElement config) {
	// Create category.
	Category category = null;
	try {
		category = new Category(config);
	} catch (CoreException e) {
		WorkbenchPlugin.log("Cannot create category: ", e.getStatus());//$NON-NLS-1$
		return;
	}

	// Defer for later processing.
	if (deferCategories == null)
		deferCategories = new ArrayList(20);
	deferCategories.add(category);
}
/**
 * Stores a wizard element for deferred addition.
 */
private void deferWizard(WorkbenchWizardElement element) {
	if (deferWizards == null)
		deferWizards = new ArrayList(50);
	deferWizards.add(element);
}
/**
 * Finishes the addition of categories.  The categories are sorted and
 * added in a root to depth traversal.
 */
private void finishCategories() {
	// If no categories just return.
	if (deferCategories == null)
		return;

	// Sort categories by flattened name.
	CategoryNode [] flatArray = new CategoryNode[deferCategories.size()];
	for (int i=0; i < deferCategories.size(); i++) {
		flatArray[i] = new CategoryNode((Category)deferCategories.get(i));
	}
	Sorter sorter = new Sorter() {
		private Collator collator = Collator.getInstance();
		
		public boolean compare(Object o1, Object o2) {
			String s1 = ((CategoryNode)o1).getPath();
			String s2 = ((CategoryNode)o2).getPath();
			return collator.compare(s2, s1) > 0;
		}
	};
	Object [] sortedCategories = sorter.sort(flatArray);

	// Add each category.
	for (int nX = 0; nX < sortedCategories.length; nX ++) {
		Category cat = ((CategoryNode)sortedCategories[nX]).getCategory();
		finishCategory(cat);
	}

	// Cleanup.
	deferCategories = null;
}
/**
 * Save new category definition.
 */
private void finishCategory(Category category) {
	WizardCollectionElement currentResult = (WizardCollectionElement) wizards;
	
	String[] categoryPath = category.getParentPath();
	WizardCollectionElement parent = currentResult; 		// ie.- root

	// Traverse down into parent category.	
	if (categoryPath != null) {
		for (int i = 0; i < categoryPath.length; i++) {
			WizardCollectionElement tempElement = getChildWithID(parent,categoryPath[i]);
			if (tempElement == null) {
				// The parent category is invalid.  By returning here the
				// category will be dropped and any wizard within the category
				// will be added to the "Other" category.
				return;
			} else
				parent = tempElement;
		}
	}

	// If another category already exists with the same id ignore this one.
	Object test = getChildWithID(parent, category.getId());
	if (test != null)
		return;
		
	if (parent != null)
		createCollectionElement(parent, category.getId(), category.getLabel());
}
/**
 *	Insert the passed wizard element into the wizard collection appropriately
 *	based upon its defining extension's CATEGORY tag value
 *
 *	@param element WorkbenchWizardElement
 *	@param extension 
 *	@param currentResult WizardCollectionElement
 */
private void finishWizard(WorkbenchWizardElement element, IConfigurationElement config, AdaptableList result) {
	WizardCollectionElement currentResult = (WizardCollectionElement)result;
	StringTokenizer familyTokenizer = new StringTokenizer(getCategoryStringFor(config),CATEGORY_SEPARATOR);

	// use the period-separated sections of the current Wizard's category
	// to traverse through the NamedSolution "tree" that was previously created
	WizardCollectionElement currentCollectionElement = currentResult; // ie.- root
	boolean moveToOther = false;
	
	while (familyTokenizer.hasMoreElements()) {
		WizardCollectionElement tempCollectionElement =
			getChildWithID(currentCollectionElement,familyTokenizer.nextToken());
			
		if (tempCollectionElement == null) {	// can't find the path; bump it to uncategorized
			moveToOther = true;
			break;
		}
		else
			currentCollectionElement = tempCollectionElement;
	}
	
	if (moveToOther)
		moveElementToUncategorizedCategory(currentResult, element);
	else
		currentCollectionElement.add(element);
}
/**
 * Finishes the addition of wizards.  The wizards are processed and categorized.
 */
private void finishWizards() {
	if (deferWizards != null) {
		Iterator iter = deferWizards.iterator();
		while (iter.hasNext()) {
			WorkbenchWizardElement wizard = (WorkbenchWizardElement)iter.next();
			IConfigurationElement config = wizard.getConfigurationElement();
			finishWizard(wizard, config, wizards);
		}
		deferWizards = null;
	}
}
/**
 *	Return the appropriate category (tree location) for this Wizard.
 *	If a category is not specified then return a default one.
 */
protected String getCategoryStringFor(IConfigurationElement config) {
	String result = config.getAttribute(ATT_CATEGORY);
	if (result == null)
		result = UNCATEGORIZED_WIZARD_CATEGORY;

	return result;
}
/**
 *	Go through the children of  the passed parent and answer the child
 *	with the passed name.  If no such child is found then return null.
 *
 *	@return org.eclipse.ui.internal.model.WizardCollectionElement
 *	@param parent org.eclipse.ui.internal.model.WizardCollectionElement
 *	@param childName java.lang.String
 */
protected WizardCollectionElement getChildWithID(WizardCollectionElement parent, String id) {
	Object[] children = parent.getChildren();
	for (int i = 0; i < children.length; ++i) {
		WizardCollectionElement currentChild = (WizardCollectionElement)children[i];
		if (currentChild.getId().equals(id))
			return currentChild;
	}
	return null;
}
/**
 *	Moves given element to "Other" category, previously creating one if missing.
 */
protected void moveElementToUncategorizedCategory(WizardCollectionElement root, WorkbenchWizardElement element) {
	WizardCollectionElement otherCategory = getChildWithID(root, UNCATEGORIZED_WIZARD_CATEGORY);
	
	if (otherCategory == null)
		otherCategory = createCollectionElement(root,UNCATEGORIZED_WIZARD_CATEGORY,UNCATEGORIZED_WIZARD_CATEGORY_LABEL);

	otherCategory.add(element);
}
/**
 * Removes the empty categories from a wizard collection. 
 */
private void pruneEmptyCategories(WizardCollectionElement parent) {
	Object [] children = parent.getChildren();
	for (int nX = 0; nX < children.length; nX ++) {
		WizardCollectionElement child = (WizardCollectionElement)children[nX];
		pruneEmptyCategories(child);
		boolean shouldPrune = projectsOnly || child.getId().equals(FULL_EXAMPLES_WIZARD_CATEGORY);
		if (child.isEmpty() && shouldPrune)
			parent.remove(child);
	}
}
/**
 * Implement this method to read element attributes.
 */
protected boolean readElement(IConfigurationElement element) {
	if (element.getName().equals(TAG_CATEGORY)) {
		deferCategory(element);
		return true;
	} else {
		return super.readElement(element);
	}
}
/**
 * Reads the wizards in a registry.  
 * <p>
 * This implementation uses a defering strategy.  All of the elements 
 * (categories, wizards) are read.  The categories are created as the read occurs. 
 * The wizards are just stored for later addition after the read completes.
 * This ensures that wizard categorization is performed after all categories
 * have been read.
 * </p>
 */
protected void readWizards() {
	super.readWizards();
	finishCategories();
	finishWizards();
	if (wizards != null) {
		WizardCollectionElement parent = (WizardCollectionElement)wizards;
		pruneEmptyCategories(parent);
	}
}
}
