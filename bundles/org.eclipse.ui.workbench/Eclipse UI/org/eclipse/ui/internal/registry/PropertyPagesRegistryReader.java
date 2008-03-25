/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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

	private static final String CHILD_ENABLED_WHEN = "enabledWhen"; //$NON-NLS-1$;

	private Collection pages = new ArrayList();

	private PropertyPageContributorManager manager;

	class PropertyCategoryNode extends CategoryNode {

		RegistryPageContributor page;

		/**
		 * Create a new category node on the given reader for the property page.
		 * 
		 * @param reader
		 * @param propertyPage
		 */
		PropertyCategoryNode(CategorizedPageRegistryReader reader,
				RegistryPageContributor propertyPage) {
			super(reader);
			page = propertyPage;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText()
		 */
		String getLabelText() {
			return page.getPageName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText(java.lang.Object)
		 */
		String getLabelText(Object element) {
			return ((RegistryPageContributor) element).getPageName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getNode()
		 */
		Object getNode() {
			return page;
		}
	}

	/**
	 * The constructor.
	 * 
	 * @param manager
	 *            the manager
	 */
	public PropertyPagesRegistryReader(PropertyPageContributorManager manager) {
		this.manager = manager;
	}

	/**
	 * Reads static property page specification.
	 */
	private void processPageElement(IConfigurationElement element) {
		String pageId = element
				.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

		if (pageId == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_ID);
			return;
		}

		RegistryPageContributor contributor = new RegistryPageContributor(
				pageId, element);

		String pageClassName = getClassValue(element,
				IWorkbenchRegistryConstants.ATT_CLASS);
		if (pageClassName == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_CLASS);
			return;
		}
		if (element.getAttribute(ATT_OBJECTCLASS) == null) {
			pages.add(contributor);
			manager.registerContributor(contributor, Object.class.getName());
		} else {
			List objectClassNames = new ArrayList();
			objectClassNames.add(element.getAttribute(ATT_OBJECTCLASS));
			registerContributors(contributor, objectClassNames);
		}
	}

	/**
	 * Register the contributor for all of the relevant classes.
	 * 
	 * @param contributor
	 * @param objectClassNames
	 */
	private void registerContributors(RegistryPageContributor contributor,
			List objectClassNames) {

		pages.add(contributor);
		for (Iterator iter = objectClassNames.iterator(); iter.hasNext();) {
			manager.registerContributor(contributor, (String) iter.next());
		}

	}


	/**
	 * Reads the next contribution element.
	 * 
	 * public for dynamic UI
	 */
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
	 * @param registry
	 *            the registry
	 */
	public void registerPropertyPages(IExtensionRegistry registry) {
		readRegistry(registry, PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_PROPERTY_PAGES);
		processNodes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#add(java.lang.Object,
	 *      java.lang.Object)
	 */
	void add(Object parent, Object node) {
		((RegistryPageContributor) parent)
				.addSubPage((RegistryPageContributor) node);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#createCategoryNode(org.eclipse.ui.internal.registry.CategorizedPageRegistryReader,
	 *      java.lang.Object)
	 */
	CategoryNode createCategoryNode(CategorizedPageRegistryReader reader,
			Object object) {
		return new PropertyCategoryNode(reader,
				(RegistryPageContributor) object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.Object,
	 *      java.lang.String)
	 */
	Object findNode(Object parent, String currentToken) {
		return ((RegistryPageContributor) parent).getChild(currentToken);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.String)
	 */
	Object findNode(String id) {
		Iterator iterator = pages.iterator();
		while (iterator.hasNext()) {
			RegistryPageContributor next = (RegistryPageContributor) iterator
					.next();
			if (next.getPageId().equals(id))
				return next;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getCategory(java.lang.Object)
	 */
	String getCategory(Object node) {
		return ((RegistryPageContributor) node).getCategory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getNodes()
	 */
	Collection getNodes() {
		return pages;
	}
}
