package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashMap;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.*;

/**
 * This class loads property pages from the registry.
 */
public class PropertyPagesRegistryReader extends RegistryReader {
	public static final String ATT_NAME_FILTER = "nameFilter";//$NON-NLS-1$
	public static final String ATT_FILTER_NAME = "name";//$NON-NLS-1$
	public static final String ATT_FILTER_VALUE = "value";//$NON-NLS-1$
	public static final String ATT_CLASS = "class";//$NON-NLS-1$

	private static final String TAG_PAGE = "page";//$NON-NLS-1$
	private static final String TAG_FILTER="filter";//$NON-NLS-1$
	private static final String ATT_NAME = "name";//$NON-NLS-1$
	private static final String ATT_ID = "id";//$NON-NLS-1$
	private static final String ATT_ICON = "icon";//$NON-NLS-1$
	private static final String ATT_OBJECTCLASS = "objectClass";//$NON-NLS-1$
	private static final String ATT_ADAPTABLE = "adaptable";//$NON-NLS-1$
	
	private static final String P_TRUE = "true";//$NON-NLS-1$
	
	private HashMap filterProperties; 
	private PropertyPageContributorManager manager;
/**
 * The constructor.
 */
public PropertyPagesRegistryReader(PropertyPageContributorManager manager) {
	this.manager = manager;
}
/**
 * Parses child element and processes it 
 */
private void processChildElement(IConfigurationElement element) {
	String tag = element.getName();
	if (tag.equals(TAG_FILTER)) {
		String key = element.getAttribute(ATT_FILTER_NAME);
		String value = element.getAttribute(ATT_FILTER_VALUE);
		if (key == null || value == null)
			return;
		if (filterProperties==null) 
			filterProperties = new HashMap();
		filterProperties.put(key, value);
	}
}

/**
 * Reads static property page specification.
 */
private void processPageElement(IConfigurationElement element) {
	String pageId = element.getAttribute(ATT_ID);
	String pageName = element.getAttribute(ATT_NAME);
	String iconName = element.getAttribute(ATT_ICON);
	String pageClassName = element.getAttribute(ATT_CLASS);
	String objectClassName = element.getAttribute(ATT_OBJECTCLASS);
	String adaptable = element.getAttribute(ATT_ADAPTABLE);

	if (pageId==null) {
		logMissingAttribute(element, ATT_ID);
		return;
	}
	if (objectClassName == null) {
		logMissingAttribute(element, ATT_OBJECTCLASS);
		return;
	}
	if (pageClassName == null) {
		logMissingAttribute(element, ATT_CLASS);
		return;
	}

	filterProperties = null;
	IConfigurationElement[] children = element.getChildren();
	for (int i=0; i<children.length; i++) {
		processChildElement(children[i]);
	}

	IPropertyPageContributor contributor = 
		new RegistryPageContributor(
			pageId, 
			pageName, 
			iconName, 
			filterProperties, 
			objectClassName, 
			P_TRUE.equalsIgnoreCase(adaptable),
			element);
	registerContributor(objectClassName, contributor);
}
/**
 * Reads the next contribution element.
 */
protected boolean readElement(IConfigurationElement element) {
	if (element.getName().equals(TAG_PAGE)) {
		processPageElement(element);
		readElementChildren(element);
		return true;
	}
	if (element.getName().equals(TAG_FILTER)) {
		return true;
	}

	return false;
}
/**
 * Creates object class instance and registers the contributor with the
 * property page manager.
 */
private void registerContributor(String objectClassName, IPropertyPageContributor contributor) {
	manager.registerContributor(contributor, objectClassName);
}
/**
 *	Reads all occurances of propertyPages extension in the registry.
 */
public void registerPropertyPages(IPluginRegistry registry) {
	readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_PROPERTY_PAGES);
}
}
