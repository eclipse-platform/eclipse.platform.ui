package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.UIHackFinder;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import java.util.*;

/**
 *	Instances of this class provide a simple API to the workbench for
 *	accessing of the core registry.  It accepts a registry at creation
 *	time and extracts workbench-related information from it as requested.
 */
public class NewWizardsRegistryReader extends WizardsRegistryReader {
	
	private boolean projectsOnly;
	
	// constants
	public final static String		BASE_CATEGORY = "Base";//$NON-NLS-1$
	private final static String		TAG_CATEGORY = "category";	//$NON-NLS-1$
	private final static String		UNCATEGORIZED_WIZARD_CATEGORY = "org.eclipse.ui.Other";//$NON-NLS-1$
	private final static String		UNCATEGORIZED_WIZARD_CATEGORY_LABEL = "Other";//$NON-NLS-1$
	private final static String		CATEGORY_SEPARATOR = "/";//$NON-NLS-1$
	private final static String		ATT_CATEGORY = "category";//$NON-NLS-1$
	private final static String ATT_PROJECT = "project";//$NON-NLS-1$
	private final static String STR_TRUE = "true";//$NON-NLS-1$
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
/**
 *	Insert the passed wizard element into the wizard collection appropriately
 *	based upon its defining extension's CATEGORY tag value
 *
 *	@param element WorkbenchWizardElement
 *	@param extension 
 *	@param currentResult WizardCollectionElement
 */
protected void addNewElementToResult(WorkbenchWizardElement element, IConfigurationElement config, AdaptableList result) {
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
 * Save new category definition.
 */
private void processCategory(IConfigurationElement config) {
	WizardCollectionElement currentResult = (WizardCollectionElement) wizards;
	Category category = null;
	
	try {
		category = new Category(config);
	} catch (CoreException e) {
		WorkbenchPlugin.log("Cannot create category: ", e.getStatus());//$NON-NLS-1$
		return;
	}
	
	String[] categoryPath = category.getParentCategoryPath();
	WizardCollectionElement element = currentResult; 		// ie.- root
	
	if (categoryPath != null) {
		for (int i = 0; i < categoryPath.length; i++) {
			WizardCollectionElement tempElement = getChildWithID(element,categoryPath[i]);
			if (tempElement == null) {
				element = null;
				break;
			} else
				element = tempElement;
		}
	}
	
	if (element != null)
		createCollectionElement(element, category.getID(), category.getLabel());
}
/**
 * Removes the empty categories from a wizard collection. 
 */
private void pruneEmptyCategories(WizardCollectionElement parent) {
	Object [] children = parent.getChildren();
	for (int nX = 0; nX < children.length; nX ++) {
		WizardCollectionElement child = (WizardCollectionElement)children[nX];
		pruneEmptyCategories(child);
		if (child.isEmpty())
			parent.remove(child);
	}
}
/**
 * Implement this method to read element attributes.
 */
protected boolean readElement(IConfigurationElement element) {
	if (element.getName().equals(TAG_CATEGORY)) {
		processCategory(element);
		return true;
	} else {
		return super.readElement(element);
	}
}
/**
 * Reads the wizards in a registry.  
 */
protected void readWizards() {
	super.readWizards();
	if (projectsOnly && wizards != null) {
		WizardCollectionElement parent = (WizardCollectionElement)wizards;
		pruneEmptyCategories(parent);
	}
}
}
