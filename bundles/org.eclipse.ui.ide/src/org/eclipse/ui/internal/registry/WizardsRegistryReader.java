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

import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.model.AdaptableList;

/**
 *  Instances access the registry that is provided at creation time
 *  in order to determine the contained Wizards
 */
public class WizardsRegistryReader extends RegistryReader {
	protected AdaptableList wizards;
	private String pluginPoint;

	protected final static String TAG_WIZARD = "wizard";//$NON-NLS-1$

	protected final static String ATT_NAME = "name";//$NON-NLS-1$
	public final static String ATT_CLASS = "class";//$NON-NLS-1$
	protected final static String ATT_ICON = "icon";//$NON-NLS-1$
	protected final static String ATT_ID = "id";//$NON-NLS-1$
	protected final static String trueString = "TRUE";//$NON-NLS-1$
/**
 *	Create an instance of this class.
 *
 *	@param pluginPointId java.lang.String
 */
public WizardsRegistryReader(String pluginPointId) {
	pluginPoint = pluginPointId;
}
/**
 * Adds new wizard to the provided collection. Override to
 * provide more logic.
 */
protected void addNewElementToResult(WorkbenchWizardElement wizard, IConfigurationElement config, AdaptableList result) {
	result.add(wizard);
}
/**
 * Creates empty element collection. Overrider to fill
 * initial elements, if needed.
 */
protected AdaptableList createEmptyWizardCollection() {
	return new AdaptableList();
}
/**
 * Returns a new WorkbenchWizardElement configured according to the parameters
 * contained in the passed Registry.  
 *
 * May answer null if there was not enough information in the Extension to create 
 * an adequate wizard
 */
protected WorkbenchWizardElement createWizardElement(IConfigurationElement element) {
	// WizardElements must have a name attribute
	String nameString = element.getAttribute(ATT_NAME);
	if (nameString == null) {
		logMissingAttribute(element, ATT_NAME);
		return null;
	}
	WorkbenchWizardElement result = new WorkbenchWizardElement(nameString);
	if (initializeWizard(result, element))
		return result;	// ie.- initialization was successful

	return null;
}
/**
 *	Returns the first wizard with a given id.
 */
public WorkbenchWizardElement findWizard(String id) {
	Object [] wizards = getWizards().getChildren();
	for (int nX = 0; nX < wizards.length; nX ++) {
		WizardCollectionElement collection = (WizardCollectionElement)wizards[nX];
		WorkbenchWizardElement element = collection.findWizard(id,true);
		if (element != null)
			return element;
	}
	return null;
}
/**
 * Returns a list of wizards, project and not.
 *
 * The return value for this method is cached since computing its value
 * requires non-trivial work.  
 */
public AdaptableList getWizards() {
	if (wizards == null)
		readWizards();
	return wizards;
}
/**
 *	Initialize the passed element's properties based on the contents of
 *	the passed registry.  Answer a boolean indicating whether the element
 *	was able to be adequately initialized.
 *
 *	@return boolean
 *	@param element WorkbenchWizardElement
 *	@param extension Extension
 */
protected boolean initializeWizard(WorkbenchWizardElement element, IConfigurationElement config) {
	element.setID(config.getAttribute(ATT_ID));
	element.setDescription(getDescription(config));

	// apply CLASS and ICON properties	
	element.setConfigurationElement(config);
	String iconName = config.getAttribute(ATT_ICON);
	if (iconName != null) {
		IExtension extension = config.getDeclaringExtension();
		element.setImageDescriptor(WorkbenchImages.getImageDescriptorFromExtension(extension, iconName));
	}
	// ensure that a class was specified
	if (element.getConfigurationElement() == null) {
		logMissingAttribute(config, ATT_CLASS);
		return false;
	}
	return true;	
}
/**
 * Implement this method to read element attributes.
 */
protected boolean readElement(IConfigurationElement element) {
	if (!element.getName().equals(TAG_WIZARD))
		return false;
	WorkbenchWizardElement wizard = createWizardElement(element);
	if (wizard != null)
	   addNewElementToResult(wizard, element, wizards);
	return true;
}
/**
 * Reads the wizards in a registry.  
 */
protected void readWizards() {
	if (wizards == null) {
		wizards = createEmptyWizardCollection();
		IPluginRegistry pregistry = Platform.getPluginRegistry();
		readRegistry(pregistry, PlatformUI.PLUGIN_ID, pluginPoint);
	}
}
}
