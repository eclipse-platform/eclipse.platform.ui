/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.registry;

import java.text.Collator;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.cheatsheets.AbstractItemExtensionElement;
import org.eclipse.ui.internal.registry.Category;

import org.eclipse.ui.internal.cheatsheets.*;

/**
 *  Instances access the registry that is provided at creation time
 *  in order to determine the contained CheatSheet Contents
 */
public class CheatSheetRegistryReader extends RegistryReader {

	private class CategoryNode {
		private Category category;
		private String path;
		public CategoryNode(Category cat) {
			category = cat;
			path = ""; //$NON-NLS-1$
			String[] categoryPath = category.getParentPath();
			if (categoryPath != null) {
				for (int nX = 0; nX < categoryPath.length; nX++) {
					path += categoryPath[nX] + '/'; //$NON-NLS-1$
				}
			}
			path += cat.getId();
		}
		public Category getCategory() {
			return category;
		}
		public String getPath() {
			return path;
		}
	}
	private final static String ATT_CATEGORY = "category"; //$NON-NLS-1$
	public final static String ATT_CONTENTFILE = "contentFile"; //$NON-NLS-1$
	protected final static String ATT_ICON = "icon"; //$NON-NLS-1$
	protected final static String ATT_ID = "id"; //$NON-NLS-1$
	protected final static String ATT_LISTENERCLASS = "class"; //$NON-NLS-1$	
	protected final static String ATT_NAME = "name"; //$NON-NLS-1$
	protected final static String ATT_PLUGINCLASSID = "classPluginId"; //$NON-NLS-1$	
	protected final static String ATT_CLASS = "class"; //$NON-NLS-1$
	protected final static String ATT_TARGETCHEATSHEETID = "targetCheatSheetId"; //$NON-NLS-1$	
	private final static String CATEGORY_SEPARATOR = "/"; //$NON-NLS-1$
	private final static String ATT_ITEM_ATTRIBUTE = "itemAttribute"; //$NON-NLS-1$
	private static CheatSheetRegistryReader instance;
	private final static String TAG_CATEGORY = "category"; //$NON-NLS-1$

	// constants
	protected final static String TAG_CHEATSHEET = "cheatSheet"; //$NON-NLS-1$
	protected final static String TAG_CHEATSHEET_LISTENER = "cheatSheetListener"; //$NON-NLS-1$
	protected final static String TAG_ITEM_EXTENSION = "itemExtension"; //$NON-NLS-1$
	protected final static String trueString = "TRUE"; //$NON-NLS-1$
	private final static String UNCATEGORIZED_CHEATSHEET_CATEGORY = "org.eclipse.ui.Other"; //$NON-NLS-1$
	private final static String UNCATEGORIZED_CHEATSHEET_CATEGORY_LABEL = CheatSheetPlugin.getResourceString(ICheatSheetResource.CHEAT_SHEET_OTHER_CATEGORY);

	/**
	 * Returns a list of cheatsheets, project and not.
	 *
	 * The return value for this method is cached since computing its value
	 * requires non-trivial work.  
	 */
	public static CheatSheetRegistryReader getInstance() {
		if (instance == null)
			instance = new CheatSheetRegistryReader();

		return instance;
	}
	protected ArrayList cheatsheetListeners;
	protected ArrayList cheatsheetItemExtensions;

	protected AdaptableList cheatsheets;
	private ArrayList deferCategories = null;

	//	private boolean shouldPrune = true;
	private ArrayList deferCheatSheets = null;
	private final String pluginListenerPoint = "cheatSheetListener"; //$NON-NLS-1$
	private final String pluginPoint = "cheatSheetContent"; //$NON-NLS-1$
	private final String csItemExtension = "cheatSheetItemExtension"; //$NON-NLS-1$

	/**
	 *	Create an instance of this class.
	 */
	private CheatSheetRegistryReader() {
	}

	/**
	 * Adds new cheatsheet to the provided collection. Override to
	 * provide more logic.
	 * <p>
	 * This implementation uses a defering strategy.  For more info see
	 * <code>readCheatSheets</code>.
	 * </p>
	 */
	protected void addNewElementToResult(CheatSheetElement element, IConfigurationElement config, AdaptableList result) {
		deferCheatSheet(element);
	}

	/**
	 * Returns a new CheatSheetElement configured according to the parameters
	 * contained in the passed Registry.  
	 *
	 * May answer null if there was not enough information in the Extension to create 
	 * an adequate cheatsheet
	 */
	protected CheatSheetElement createCheatSheetElement(IConfigurationElement element) {
		// CheatSheetElements must have a name attribute
		String nameString = element.getAttribute(ATT_NAME);
		if (nameString == null) {
			logMissingAttribute(element, ATT_NAME);
			return null;
		}
		CheatSheetElement result = new CheatSheetElement(nameString);
		if (initializeCheatSheet(result, element))
			return result; // ie.- initialization was successful

		return null;
	}

	/**
	 *	Create and answer a new CheatSheetCollectionElement, configured as a
	 *	child of <code>parent</code>
	 *
	 *	@return org.eclipse.ui.internal.model.CheatSheetCollectionElement
	 *	@param parent org.eclipse.ui.internal.model.CheatSheetCollectionElement
	 *	@param childName java.lang.String
	 */
	protected CheatSheetCollectionElement createCollectionElement(CheatSheetCollectionElement parent, String id, String label) {
		CheatSheetCollectionElement newElement = new CheatSheetCollectionElement(id, label, parent);

		parent.add(newElement);
		return newElement;
	}

	/**
	 * Creates empty element collection. Overrider to fill
	 * initial elements, if needed.
	 */
	protected AdaptableList createEmptyCheatSheetCollection() {
		return new CheatSheetCollectionElement("root", "root", null); //$NON-NLS-2$//$NON-NLS-1$
	}

	private void createListenerElement(IConfigurationElement element) {
		String listenerID = element.getAttribute(ATT_ID);
		String targetCheatsheetID = element.getAttribute(ATT_TARGETCHEATSHEETID);
		String className = element.getAttribute(ATT_LISTENERCLASS);
		String listenerClassPluginID = element.getAttribute(ATT_PLUGINCLASSID);
		if(listenerClassPluginID==null)
			listenerClassPluginID = element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
		if (listenerID == null || targetCheatsheetID == null || className == null || listenerClassPluginID == null)
			return;
		CheatSheetListenerElement listenerElement = new CheatSheetListenerElement(listenerID, className, listenerClassPluginID, targetCheatsheetID);
		if (cheatsheetListeners == null)
			cheatsheetListeners = new ArrayList(20);
		cheatsheetListeners.add(listenerElement);
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
			CheatSheetPlugin.getPlugin().getLog().log(e.getStatus());
			return;
		}

		// Defer for later processing.
		if (deferCategories == null)
			deferCategories = new ArrayList(20);
		deferCategories.add(category);
	}

	/**
	 * Stores a cheatsheet element for deferred addition.
	 */
	private void deferCheatSheet(CheatSheetElement element) {
		if (deferCheatSheets == null)
			deferCheatSheets = new ArrayList(50);
		deferCheatSheets.add(element);
	}

	/**
	 *	Returns the first cheatsheet
	 *  with a given id.
	 */
	public CheatSheetElement findCheatSheet(String id) {
		Object[] cheatsheets = getCheatSheets().getChildren();
		for (int nX = 0; nX < cheatsheets.length; nX++) {
			CheatSheetCollectionElement collection = (CheatSheetCollectionElement) cheatsheets[nX];
			CheatSheetElement element = collection.findCheatSheet(id, true);
			if (element != null)
				return element;
		}
		return null;
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
		CategoryNode[] flatArray = new CategoryNode[deferCategories.size()];
		for (int i = 0; i < deferCategories.size(); i++) {
			flatArray[i] = new CategoryNode((Category) deferCategories.get(i));
		}
		Sorter sorter = new Sorter() {
			private Collator collator = Collator.getInstance();

			public boolean compare(Object o1, Object o2) {
				String s1 = ((CategoryNode) o1).getPath();
				String s2 = ((CategoryNode) o2).getPath();
				return collator.compare(s2, s1) > 0;
			}
		};
		Object[] sortedCategories = sorter.sort(flatArray);

		// Add each category.
		for (int nX = 0; nX < sortedCategories.length; nX++) {
			Category cat = ((CategoryNode) sortedCategories[nX]).getCategory();
			finishCategory(cat);
		}

		// Cleanup.
		deferCategories = null;
	}

	/**
	 * Save new category definition.
	 */
	private void finishCategory(Category category) {
		CheatSheetCollectionElement currentResult = (CheatSheetCollectionElement) cheatsheets;

		String[] categoryPath = category.getParentPath();
		CheatSheetCollectionElement parent = currentResult; // ie.- root

		// Traverse down into parent category.	
		if (categoryPath != null) {
			for (int i = 0; i < categoryPath.length; i++) {
				CheatSheetCollectionElement tempElement = getChildWithID(parent, categoryPath[i]);
				if (tempElement == null) {
					// The parent category is invalid.  By returning here the
					// category will be dropped and any cheatsheet within the category
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
	 *	Insert the passed cheatsheet element into the cheatsheet collection appropriately
	 *	based upon its defining extension's CATEGORY tag value
	 *
	 *	@param element CheatSheetElement
	 *	@param extension 
	 *	@param currentResult CheatSheetCollectionElement
	 */
	private void finishCheatSheet(CheatSheetElement element, IConfigurationElement config, AdaptableList result) {
		CheatSheetCollectionElement currentResult = (CheatSheetCollectionElement) result;
		StringTokenizer familyTokenizer = new StringTokenizer(getCategoryStringFor(config), CATEGORY_SEPARATOR);

		// use the period-separated sections of the current CheatSheet's category
		// to traverse through the NamedSolution "tree" that was previously created
		CheatSheetCollectionElement currentCollectionElement = currentResult; // ie.- root
		boolean moveToOther = false;

		while (familyTokenizer.hasMoreElements()) {
			CheatSheetCollectionElement tempCollectionElement = getChildWithID(currentCollectionElement, familyTokenizer.nextToken());

			if (tempCollectionElement == null) { // can't find the path; bump it to uncategorized
				moveToOther = true;
				break;
			} else
				currentCollectionElement = tempCollectionElement;
		}

		if (moveToOther)
			moveElementToUncategorizedCategory(currentResult, element);
		else
			currentCollectionElement.add(element);
	}

	/**
	 * Finishes the addition of cheatsheets.  The cheatsheets are processed and categorized.
	 */
	private void finishCheatSheets() {
		if (deferCheatSheets != null) {
			Iterator iter = deferCheatSheets.iterator();
			while (iter.hasNext()) {
				CheatSheetElement cheatsheet = (CheatSheetElement) iter.next();
				IConfigurationElement config = cheatsheet.getConfigurationElement();
				finishCheatSheet(cheatsheet, config, cheatsheets);
			}
			deferCheatSheets = null;
		}
	}

	/**
	 *	Return the appropriate category (tree location) for this CheatSheet.
	 *	If a category is not specified then return a default one.
	 */
	protected String getCategoryStringFor(IConfigurationElement config) {
		String result = config.getAttribute(ATT_CATEGORY);
		if (result == null)
			result = UNCATEGORIZED_CHEATSHEET_CATEGORY;

		return result;
	}

	public ArrayList getCheatsheetListenerElements() {
		return cheatsheetListeners;
	}

	/**
	 * Returns a list of cheatsheets, project and not.
	 *
	 * The return value for this method is cached since computing its value
	 * requires non-trivial work.  
	 */
	public AdaptableList getCheatSheets() {
		if (cheatsheets == null)
			readCheatSheets();
		return cheatsheets;
	}

	/**
	 *	Go through the children of  the passed parent and answer the child
	 *	with the passed name.  If no such child is found then return null.
	 *
	 *	@return org.eclipse.ui.internal.model.CheatSheetCollectionElement
	 *	@param parent org.eclipse.ui.internal.model.CheatSheetCollectionElement
	 *	@param childName java.lang.String
	 */
	protected CheatSheetCollectionElement getChildWithID(CheatSheetCollectionElement parent, String id) {
		Object[] children = parent.getChildren();
		for (int i = 0; i < children.length; ++i) {
			CheatSheetCollectionElement currentChild = (CheatSheetCollectionElement) children[i];
			if (currentChild.getId().equals(id))
				return currentChild;
		}
		return null;
	}

	/**
	 *	Initialize the passed element's properties based on the contents of
	 *	the passed registry.  Answer a boolean indicating whether the element
	 *	was able to be adequately initialized.
	 *
	 *	@return boolean
	 *	@param element CheatSheetElement
	 *	@param extension Extension
	 */
	protected boolean initializeCheatSheet(CheatSheetElement element, IConfigurationElement config) {
		element.setID(config.getAttribute(ATT_ID));
		element.setDescription(getDescription(config));

		// apply CONTENTFILE and ICON properties	
		element.setConfigurationElement(config);
//TODO: Port problem, need to fix. 
//		String iconName = config.getAttribute(ATT_ICON);
//		if (iconName != null) {
//			IExtension extension = config.getDeclaringExtension();
//			element.setImageDescriptor(WorkbenchImages.getImageDescriptorFromExtension(extension, iconName));
//		}

		String contentFile = config.getAttribute(ATT_CONTENTFILE);
		if (contentFile != null) {
			element.setContentFile(contentFile);
		}

		// ensure that a contentfile was specified
		if (element.getConfigurationElement() == null) {
			logMissingAttribute(config, ATT_CONTENTFILE);
			return false;
		}
		return true;
	}

	/**
	 *	Moves given element to "Other" category, previously creating one if missing.
	 */
	protected void moveElementToUncategorizedCategory(CheatSheetCollectionElement root, CheatSheetElement element) {
		CheatSheetCollectionElement otherCategory = getChildWithID(root, UNCATEGORIZED_CHEATSHEET_CATEGORY);

		if (otherCategory == null)
			otherCategory = createCollectionElement(root, UNCATEGORIZED_CHEATSHEET_CATEGORY, UNCATEGORIZED_CHEATSHEET_CATEGORY_LABEL);

		otherCategory.add(element);
	}

	/**
	 * Removes the empty categories from a cheatsheet collection. 
	 */
	private void pruneEmptyCategories(CheatSheetCollectionElement parent) {
		Object[] children = parent.getChildren();
		for (int nX = 0; nX < children.length; nX++) {
			CheatSheetCollectionElement child = (CheatSheetCollectionElement) children[nX];
			pruneEmptyCategories(child);
			//			if (child.isEmpty() && shouldPrune)
			//				parent.remove(child);
		}
	}

	/**
	 * Reads the cheatsheets in a registry.  
	 * <p>
	 * This implementation uses a defering strategy.  All of the elements 
	 * (categories, cheatsheets) are read.  The categories are created as the read occurs. 
	 * The cheatsheets are just stored for later addition after the read completes.
	 * This ensures that cheatsheet categorization is performed after all categories
	 * have been read.
	 * </p>
	 */
	protected void readCheatSheets() {
		IPluginRegistry pregistry = Platform.getPluginRegistry();

		if (cheatsheets == null) {
			cheatsheets = createEmptyCheatSheetCollection();
			readRegistry(pregistry, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, pluginPoint);
		}

		finishCategories();
		finishCheatSheets();

		if (cheatsheets != null) {
			CheatSheetCollectionElement parent = (CheatSheetCollectionElement) cheatsheets;
			pruneEmptyCategories(parent);
		}

		//Now read the cheatsheet listeners here from the plugin registry.
		readRegistry(pregistry, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, pluginListenerPoint);

	}

	public ArrayList readItemExtensions() {
		cheatsheetItemExtensions = new ArrayList();

		IPluginRegistry pregistry = Platform.getPluginRegistry();
		//Now read the cheat sheet extensions.
		readRegistry(pregistry, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, csItemExtension);

		return cheatsheetItemExtensions;
	}

	private void createItemExtensionElement(IConfigurationElement element) {
		String className = element.getAttribute(ATT_CLASS);
		String itemAttribute = element.getAttribute(ATT_ITEM_ATTRIBUTE);
		if (className == null || itemAttribute == null)
			return;

		Class extClass = null;
		AbstractItemExtensionElement extElement = null;
		IPluginDescriptor desc = element.getDeclaringExtension().getDeclaringPluginDescriptor();

		try {
			extClass = desc.getPluginClassLoader().loadClass(className);
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_LOADING_CLASS_FOR_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
		}
		try {
			if (extClass != null)
				extElement = (AbstractItemExtensionElement) extClass.newInstance();
		} catch (Exception e) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_CREATING_CLASS_FOR_ACTION), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
		}
		
		if(extElement != null){
			extElement.setAttributeName(itemAttribute);
			cheatsheetItemExtensions.add(extElement);
		}
		
	}

	/**
	 * Implement this method to read element attributes.
	 */
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(TAG_CATEGORY)) {
			deferCategory(element);
			return true;
		} else if (element.getName().equals(TAG_CHEATSHEET_LISTENER)) {
			createListenerElement(element);
			return true;
		} else if (element.getName().equals(TAG_ITEM_EXTENSION)) {
			createItemExtensionElement(element);
			return true;
		} else {
			if (!element.getName().equals(TAG_CHEATSHEET))
				return false;

			CheatSheetElement cheatsheet = createCheatSheetElement(element);
			if (cheatsheet != null)
				addNewElementToResult(cheatsheet, element, cheatsheets);
			return true;
		}
	}
}
