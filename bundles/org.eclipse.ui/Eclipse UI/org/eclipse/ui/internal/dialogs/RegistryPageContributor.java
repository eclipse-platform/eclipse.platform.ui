package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.jface.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.ImageDescriptor;
import java.util.*;

/**
 * This property page contributor is created from page entry
 * in the registry. Since instances of this class are created
 * by the workbench, there is no danger of premature loading
 * of plugins.
 */

public class RegistryPageContributor implements IPropertyPageContributor {
	private String pageName;
	private String iconName;
	private String pageId;
	private IConfigurationElement pageElement;
	private HashMap filterProperties;
/**
 * PropertyPageContributor constructor.
 */
public RegistryPageContributor(String pageId, String pageName, String iconName, HashMap filterProperties, IConfigurationElement pageElement) {
	this.pageId = pageId;
	this.pageName = pageName;
	this.iconName = iconName;
	this.filterProperties = filterProperties;
	this.pageElement = pageElement;
}
/**
 * Implements the interface by creating property page specified with
 * the configuration element.
 */
public boolean contributePropertyPages(PropertyPageManager mng, IAdaptable element) {
	PropertyPageNode node = new PropertyPageNode(this, element);
	mng.addToRoot(node);
	return true;
}
/**
 * Creates the page based on the information in the configuration element.
 */
public IWorkbenchPropertyPage createPage(IAdaptable element) throws CoreException {
	IWorkbenchPropertyPage ppage = null;
	ppage = (IWorkbenchPropertyPage)WorkbenchPlugin.createExtension(
		pageElement, PropertyPagesRegistryReader.ATT_CLASS);
	ppage.setElement(element);
	ppage.setTitle(pageName);
	return ppage;
}
/**
 * Returns page icon as defined in the registry.
 */
public ImageDescriptor getPageIcon() {
	if (iconName==null) return null;
	return WorkbenchImages.getImageDescriptorFromExtension(pageElement.getDeclaringExtension(), iconName);
}
/**
 * Returns page ID as defined in the registry.
 */

public String getPageId() {
	return pageId;
}
/**
 * Returns page name as defined in the registry.
 */

public String getPageName() {
	return pageName;
}
/**
 * Return true if name filter is not defined in the registry for this page,
 * or if name of the selected object matches the name filter.
 */
public boolean isApplicableTo(Object object) {
	// Test name filter
	String nameFilter = pageElement.getAttribute(PropertyPagesRegistryReader.ATT_NAME_FILTER);
	if (nameFilter != null) {
		String objectName = object.toString();
		if (object instanceof IAdaptable) {
			IWorkbenchAdapter adapter = (IWorkbenchAdapter)((IAdaptable)object).getAdapter(IWorkbenchAdapter.class);
			if (adapter != null) {
				String elementName = adapter.getLabel(object);
				if (elementName != null) {
					objectName = elementName;
				}
			}
		}
		if (!SelectionEnabler.verifyNameMatch(objectName, nameFilter))
			return false;
	}

	// Test custom filter	
	if (filterProperties == null)
		return true;
	IActionFilter filter = null;
	if (object instanceof IActionFilter)
		filter = (IActionFilter)object;
	else if (object instanceof IAdaptable)
		filter = (IActionFilter)((IAdaptable)object).getAdapter(IActionFilter.class);
	if (filter != null)
		return testCustom(object, filter);
	else
		return true;
}
/**
 * Returns whether the object passes a custom key value filter
 * implemented by a matcher.
 */
private boolean testCustom(Object object, IActionFilter filter) {
	if (filterProperties == null)
		return false;
	Iterator iter = filterProperties.keySet().iterator();
	while (iter.hasNext()) {
		String key = (String)iter.next();
		String value = (String)filterProperties.get(key);
		if (!filter.testAttribute(object, key, value))
			return false;
	}
	return true;
}
}
