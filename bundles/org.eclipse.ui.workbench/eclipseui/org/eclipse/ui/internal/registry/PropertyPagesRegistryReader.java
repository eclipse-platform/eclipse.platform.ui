/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - Bug 294628 multiple selection
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.RegistryPageContributor;

/**
 * This class loads property pages from the registry.
 */
public class PropertyPagesRegistryReader extends CategorizedPageRegistryReader {

	/**
	 * Value "<code>nameFilter</code>".
	 */
	public static final String ATT_NAME_FILTER = "nameFilter";//$NON-NLS-1$

	/**
	 * Value "<code>name</code>".
	 */
	public static final String ATT_FILTER_NAME = "name";//$NON-NLS-1$

	/**
	 * Value "<code>value</code>".
	 */
	public static final String ATT_FILTER_VALUE = "value";//$NON-NLS-1$

	/**
	 * Value "<code>selectionFilter</code>". Is an enum allowing propertyPages to
	 * support multiple selection when enum value is
	 * <code>ATT_SELECTION_FILTER_MULTI</code>
	 *
	 * @since 3.7
	 */
	public static final String ATT_SELECTION_FILTER = "selectionFilter";//$NON-NLS-1$

	/**
	 * Selection filter attribute value indicating support for multiple selection.
	 *
	 * @since 3.7
	 */
	public static final String ATT_SELECTION_FILTER_MULTI = "multi";//$NON-NLS-1$

	private static final String TAG_PAGE = "page";//$NON-NLS-1$

	/**
	 * Value "<code>filter</code>".
	 */
	public static final String TAG_FILTER = "filter";//$NON-NLS-1$

	/**
	 * Value "<code>keywordReference</code>".
	 */
	public static final String TAG_KEYWORD_REFERENCE = "keywordReference";//$NON-NLS-1$

	/**
	 * Value "<code>objectClass</code>".
	 */
	public static final String ATT_OBJECTCLASS = "objectClass";//$NON-NLS-1$

	/**
	 * Value "<code>adaptable</code>".
	 */
	public static final String ATT_ADAPTABLE = "adaptable";//$NON-NLS-1$

	private static final String CHILD_ENABLED_WHEN = "enabledWhen"; //$NON-NLS-1$ ;

	private Collection<RegistryPageContributor> pages = new ArrayList<>();

	private PropertyPageContributorManager manager;

	static class PropertyCategoryNode extends CategoryNode {

		RegistryPageContributor page;

		/**
		 * Create a new category node on the given reader for the property page.
		 */
		PropertyCategoryNode(CategorizedPageRegistryReader reader, RegistryPageContributor propertyPage) {
			super(reader);
			page = propertyPage;
		}

		@Override
		String getLabelText() {
			return page.getPageName();
		}

		@Override
		String getLabelText(Object element) {
			return ((RegistryPageContributor) element).getPageName();
		}

		@Override
		Object getNode() {
			return page;
		}
	}

	/**
	 * The constructor.
	 *
	 * @param manager the manager
	 */
	public PropertyPagesRegistryReader(PropertyPageContributorManager manager) {
		this.manager = manager;
	}

	/**
	 * Reads static property page specification.
	 */
	private void processPageElement(IConfigurationElement element) {
		String pageId = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

		if (pageId == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_ID);
			return;
		}

		RegistryPageContributor contributor = new RegistryPageContributor(pageId, element);

		String pageClassName = getClassValue(element, IWorkbenchRegistryConstants.ATT_CLASS);
		if (pageClassName == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_CLASS);
			return;
		}
		if (element.getAttribute(ATT_OBJECTCLASS) == null) {
			pages.add(contributor);
			manager.registerContributor(contributor, Object.class.getName());
		} else {
			List<String> objectClassNames = new ArrayList<>();
			objectClassNames.add(element.getAttribute(ATT_OBJECTCLASS));
			registerContributors(contributor, objectClassNames);
		}
	}

	/**
	 * Register the contributor for all of the relevant classes.
	 */
	private void registerContributors(RegistryPageContributor contributor, List<String> objectClassNames) {

		pages.add(contributor);
		for (String className : objectClassNames) {
			manager.registerContributor(contributor, className);
		}

	}

	/**
	 * Reads the next contribution element.
	 *
	 * public for dynamic UI
	 */
	@Override
	public boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(TAG_PAGE)) {
			processPageElement(element);
			readElementChildren(element);
			return true;
		}
		if (element.getName().equals(TAG_FILTER)) {
			return true;
		}

		if (element.getName().equals(CHILD_ENABLED_WHEN)) {
			return true;
		}

		if (element.getName().equals(TAG_KEYWORD_REFERENCE)) {
			return true;
		}

		return false;
	}

	/**
	 * Reads all occurances of propertyPages extension in the registry.
	 *
	 * @param registry the registry
	 */
	public void registerPropertyPages(IExtensionRegistry registry) {
		readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_PROPERTY_PAGES);
		processNodes();
	}

	@Override
	void add(Object parent, Object node) {
		((RegistryPageContributor) parent).addSubPage((RegistryPageContributor) node);

	}

	@Override
	CategoryNode createCategoryNode(CategorizedPageRegistryReader reader, Object object) {
		return new PropertyCategoryNode(reader, (RegistryPageContributor) object);
	}

	@Override
	Object findNode(Object parent, String currentToken) {
		return ((RegistryPageContributor) parent).getChild(currentToken);
	}

	@Override
	Object findNode(String id) {
		Iterator<RegistryPageContributor> iterator = pages.iterator();
		while (iterator.hasNext()) {
			RegistryPageContributor next = iterator.next();
			if (next.getPageId().equals(id))
				return next;
		}
		return null;
	}

	@Override
	String getCategory(Object node) {
		return ((RegistryPageContributor) node).getCategory();
	}

	@Override
	protected String invalidCategoryNodeMessage(CategoryNode categoryNode) {
		RegistryPageContributor rpc = (RegistryPageContributor) categoryNode.getNode();
		return "Invalid property category path: " + rpc.getCategory() + " (bundle: " + rpc.getPluginId() //$NON-NLS-1$ //$NON-NLS-2$
				+ ", propertyPage: " + rpc.getLocalId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	Collection<?> getNodes() {
		return pages;
	}
}
