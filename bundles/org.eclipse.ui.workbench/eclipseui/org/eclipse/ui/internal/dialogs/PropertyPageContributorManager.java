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
 *     Oakland Software (Francis Upton) <francisu@ieee.org> - bug 219273
 *     James Blackburn (Broadcom Corp.) - Bug 294628 multiple selection
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.ObjectContributorManager;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PropertyPagesRegistryReader;

/**
 * Extends generic object contributor manager by loading property page
 * contributors from the registry.
 */

public class PropertyPageContributorManager extends ObjectContributorManager {
	private static PropertyPageContributorManager sharedInstance = null;

	private static class CategorizedPageNode {
		RegistryPageContributor contributor;

		CategorizedPageNode parent;

		CategorizedPageNode(RegistryPageContributor page) {
			contributor = page;
		}

		void setParent(CategorizedPageNode node) {
			parent = node;
		}

	}

	/**
	 * The constructor.
	 */
	public PropertyPageContributorManager() {
		super();
		// load contributions on startup so that getContributors() returns the
		// proper content
		loadContributors();
	}

	/**
	 * Given the object class, this method will find all the registered matching
	 * contributors and sequentially invoke them to contribute to the property page
	 * manager. Matching algorithm will also check subclasses and implemented
	 * interfaces.
	 *
	 * If object is an IStructuredSelection then attempt to match all the contained
	 * objects.
	 *
	 * @return true if contribution took place, false otherwise.
	 */
	public boolean contribute(PropertyPageManager manager, Object object) {

		Collection result = null;
		if (object instanceof IStructuredSelection) {
			Object[] objs = ((IStructuredSelection) object).toArray();
			for (Object obj : objs) {
				List contribs = getContributors(obj);
				if (result == null)
					result = new LinkedHashSet(contribs);
				else
					result.retainAll(contribs);
			}
		} else
			result = getContributors(object);

		if (result == null || result.isEmpty()) {
			return false;
		}

		// Build the category nodes
		List catNodes = buildNodeList(result);
		Iterator resultIterator = catNodes.iterator();

		// K(CategorizedPageNode) V(PreferenceNode - property page)
		Map catPageNodeToPages = new HashMap();

		// Allow each contributor to add its page to the manager.
		boolean actualContributions = false;
		while (resultIterator.hasNext()) {
			CategorizedPageNode next = (CategorizedPageNode) resultIterator.next();
			IPropertyPageContributor ppcont = next.contributor;
			if (!ppcont.isApplicableTo(object)) {
				continue;
			}
			PreferenceNode page = ppcont.contributePropertyPage(manager, object);
			if (page != null) {
				catPageNodeToPages.put(next, page);
				actualContributions = true;
			}
		}

		// Fixup the parents in each page
		if (actualContributions) {
			resultIterator = catNodes.iterator();
			while (resultIterator.hasNext()) {
				CategorizedPageNode next = (CategorizedPageNode) resultIterator.next();
				PreferenceNode child = (PreferenceNode) catPageNodeToPages.get(next);
				if (child == null)
					continue;
				PreferenceNode parent = null;
				if (next.parent != null)
					parent = (PreferenceNode) catPageNodeToPages.get(next.parent);

				if (parent == null) {
					manager.addToRoot(child);
				} else {
					parent.add(child);
				}
			}
		}
		return actualContributions;
	}

	/**
	 * Build the list of nodes to be sorted.
	 *
	 * @return List of CategorizedPageNode
	 */
	private List buildNodeList(Collection nodes) {
		Hashtable mapping = new Hashtable();

		Iterator nodesIterator = nodes.iterator();
		while (nodesIterator.hasNext()) {
			RegistryPageContributor page = (RegistryPageContributor) nodesIterator.next();
			mapping.put(page.getPageId(), new CategorizedPageNode(page));
		}

		Iterator values = mapping.values().iterator();
		List returnValue = new ArrayList();
		while (values.hasNext()) {
			CategorizedPageNode next = (CategorizedPageNode) values.next();
			returnValue.add(next);
			if (next.contributor.getCategory() == null) {
				continue;
			}
			Object parent = mapping.get(next.contributor.getCategory());
			if (parent != null) {
				next.setParent((CategorizedPageNode) parent);
			}
		}
		return returnValue;
	}

	/**
	 * Ideally, shared instance should not be used and manager should be located in
	 * the workbench class.
	 *
	 * @return PropertyPageContributorManager
	 */
	public static synchronized PropertyPageContributorManager getManager() {
		if (sharedInstance == null) {
			sharedInstance = new PropertyPageContributorManager();
		}
		return sharedInstance;
	}

	/**
	 * Disposes instance if it was created
	 */
	public static synchronized void disposeManager() {
		if (sharedInstance != null) {
			sharedInstance.dispose();
		}
	}

	/**
	 * Loads property page contributors from the registry.
	 */
	private void loadContributors() {
		PropertyPagesRegistryReader reader = new PropertyPagesRegistryReader(this);
		reader.registerPropertyPages(Platform.getExtensionRegistry());
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		for (IConfigurationElement addedElement : extension.getConfigurationElements()) {
			PropertyPagesRegistryReader reader = new PropertyPagesRegistryReader(this);
			reader.readElement(addedElement);
		}
	}

	/**
	 * Return the contributors for element filters on the enablement.
	 *
	 * @return Collection of PropertyPageContribution
	 */
	public Collection getApplicableContributors(Object element) {
		if (element instanceof IStructuredSelection)
			return getApplicableContributors((IStructuredSelection) element);
		Collection contributors = getContributors(element);
		Collection result = new ArrayList();
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor contributor = (RegistryPageContributor) iter.next();
			if (contributor.isApplicableTo(element))
				result.add(contributor);

		}
		return result;
	}

	/**
	 * Get applicable contributors for multiple selection
	 *
	 * @return Collection of applicable property page contributors
	 * @since 3.7
	 */
	public Collection getApplicableContributors(IStructuredSelection selection) {
		boolean isMultiSelection = selection.size() > 1;
		Collection<RegistryPageContributor> result = null;
		for (Object selectionElement : selection) {
			Collection<RegistryPageContributor> collection = getApplicableContributors(selectionElement);
			if (isMultiSelection) {
				// Most pages are not available for multi selection.
				// We can abort early in most cases by checking this during each iteration.
				Iterator<RegistryPageContributor> resIter = collection.iterator();
				while (resIter.hasNext()) {
					RegistryPageContributor contrib = resIter.next();
					if (!contrib.supportsMultipleSelection()) {
						resIter.remove();
					}
				}
			}
			if (result == null) {
				result = new LinkedHashSet<>(collection);
			} else {
				result.retainAll(collection);
			}
			// With each iteration the result can only shrink. We can stop if already empty.
			if (result.isEmpty()) {
				return Collections.emptyList();
			}
		}
		return result;
	}

	@Override
	protected String getExtensionPointFilter() {
		return IWorkbenchRegistryConstants.PL_PROPERTY_PAGES;
	}
}
