/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *     Oakland Software (Francis Upton) <francisu@ieee.org> - bug 223808
 *     James Blackburn (Broadcom Corp.) - Bug 294628 multiple selection
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPageMulti;
import org.eclipse.ui.SelectionEnabler;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.CategorizedPageRegistryReader;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PropertyPagesRegistryReader;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This property page contributor is created from page entry in the registry.
 * Since instances of this class are created by the workbench, there is no
 * danger of premature loading of plugins.
 */

public class RegistryPageContributor implements IPropertyPageContributor, IAdaptable, IPluginContribution {
	private static final String CHILD_ENABLED_WHEN = "enabledWhen"; //$NON-NLS-1$

	private String pageId;

	/**
	 * The list of subpages (immediate children) of this node (element type:
	 * <code>RegistryPageContributor</code>).
	 */
	private Collection<RegistryPageContributor> subPages = new ArrayList<>();

	private boolean adaptable = false;

	/**
	 * Flag which indicates if this property page supports multiple selection
	 */
	private final boolean supportsMultiSelect;

	private IConfigurationElement pageElement;

	private SoftReference<Map<String, String>> filterProperties;

	private Expression enablementExpression;

	/**
	 * PropertyPageContributor constructor.
	 *
	 * @param pageId  the id
	 * @param element the element
	 */
	public RegistryPageContributor(String pageId, IConfigurationElement element) {
		this.pageId = pageId;
		this.pageElement = element;
		adaptable = Boolean.parseBoolean(pageElement.getAttribute(PropertyPagesRegistryReader.ATT_ADAPTABLE));
		supportsMultiSelect = PropertyPagesRegistryReader.ATT_SELECTION_FILTER_MULTI
				.equals(pageElement.getAttribute(PropertyPagesRegistryReader.ATT_SELECTION_FILTER));
		initializeEnablement(element);
	}

	@Override
	public PreferenceNode contributePropertyPage(PropertyPageManager mng, Object element) {
		PropertyPageNode node = new PropertyPageNode(this, element);
		if (IWorkbenchConstants.WORKBENCH_PROPERTIES_PAGE_INFO.equals(node.getId()))
			node.setPriority(-1);
		return node;
	}

	/**
	 * Creates the page based on the information in the configuration element.
	 *
	 * @param element the adaptable element
	 * @return the property page
	 * @throws CoreException thrown if there is a problem creating the apge
	 */
	public IPreferencePage createPage(Object element) throws CoreException {
		IPreferencePage ppage = null;
		ppage = (IPreferencePage) WorkbenchPlugin.createExtension(pageElement, IWorkbenchRegistryConstants.ATT_CLASS);

		ppage.setTitle(getPageName());

		Object[] elements = getObjects(element);
		IAdaptable[] adapt = new IAdaptable[elements.length];

		for (int i = 0; i < elements.length; i++) {
			Object adapted = elements[i];
			if (adaptable) {
				adapted = getAdaptedElement(adapted);
				if (adapted == null) {
					String message = "Error adapting selection to Property page " + pageId + " is being ignored"; //$NON-NLS-1$ //$NON-NLS-2$
					throw new CoreException(
							new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, message, null));
				}
			}
			adapt[i] = (IAdaptable) ((adapted instanceof IAdaptable) ? adapted : new AdaptableForwarder(adapted));
		}

		if (supportsMultiSelect) {
			if ((ppage instanceof IWorkbenchPropertyPageMulti))
				((IWorkbenchPropertyPageMulti) ppage).setElements(adapt);
			else
				throw new CoreException(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR,
						"Property page must implement IWorkbenchPropertyPageMulti: " + getPageName(), //$NON-NLS-1$
						null));
		} else
			((IWorkbenchPropertyPage) ppage).setElement(adapt[0]);

		return ppage;
	}

	/**
	 * Find an adapted element from the receiver.
	 *
	 * @return the adapted element or <code>null</code> if it could not be found.
	 */
	private Object getAdaptedElement(Object element) {
		Object adapted = LegacyResourceSupport.getAdapter(element, getObjectClass());
		if (adapted != null)
			return adapted;

		return null;
	}

	/**
	 * Return the object class name
	 *
	 * @return the object class name
	 */
	public String getObjectClass() {
		return pageElement.getAttribute(PropertyPagesRegistryReader.ATT_OBJECTCLASS);
	}

	/**
	 * Returns page icon as defined in the registry.
	 *
	 * @return the page icon
	 */
	public ImageDescriptor getPageIcon() {
		String iconName = pageElement.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
		if (iconName == null) {
			return null;
		}
		return ResourceLocator.imageDescriptorFromBundle(pageElement.getNamespaceIdentifier(), iconName).orElse(null);
	}

	/**
	 * Returns page ID as defined in the registry.
	 *
	 * @return the page id
	 */

	public String getPageId() {
		return pageId;
	}

	/**
	 * Returns page name as defined in the registry.
	 *
	 * @return the page name
	 */
	public String getPageName() {
		return pageElement.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
	}

	/**
	 * Calculate whether the Property page is applicable to the current selection.
	 * Checks:
	 * <ul>
	 * <li>multiSelect</li>
	 * <li>enabledWhen enablement expression</li>
	 * <li>nameFilter</li>
	 * <li>custom Filter</li>
	 * <li>checks legacy resource support</li>
	 * </ul>
	 * <p>
	 * For multipleSelection pages, considers all elements in the selection for
	 * enablement.
	 */
	@Override
	public boolean isApplicableTo(Object object) {
		Object[] objs = getObjects(object);

		// If not a multi-select page not applicable to multiple selection
		if (objs.length > 1 && !supportsMultiSelect)
			return false;

		if (failsEnablement(objs))
			return false;

		// Test name filter
		String nameFilter = pageElement.getAttribute(PropertyPagesRegistryReader.ATT_NAME_FILTER);

		for (Object obj : objs) {
			object = obj;
			// Name filter
			if (nameFilter != null) {
				String objectName = object.toString();
				IWorkbenchAdapter adapter = Adapters.adapt(object, IWorkbenchAdapter.class);
				if (adapter != null) {
					String elementName = adapter.getLabel(object);
					if (elementName != null) {
						objectName = elementName;
					}
				}
				if (!SelectionEnabler.verifyNameMatch(objectName, nameFilter))
					return false;
			}

			// Test custom filter
			if (getFilterProperties() == null)
				return true;
			IActionFilter filter = null;

			// Do the free IResource adapting
			Object adaptedObject = LegacyResourceSupport.getAdaptedResource(object);
			if (adaptedObject != null) {
				object = adaptedObject;
			}

			filter = Adapters.adapt(object, IActionFilter.class);

			if (filter != null && !testCustom(object, filter))
				return false;
		}

		return true;
	}

	/**
	 * Return whether or not object fails the enabledWhen enablement criterea. For
	 * multi-select pages, evaluate the enabledWhen expression using the structured
	 * selection as a Collection (which should be iterated over).
	 *
	 * @return boolean <code>true</code> if it fails the enablement test
	 */
	private boolean failsEnablement(Object[] objs) {
		if (enablementExpression == null)
			return false;
		try {
			// If multi-select property page, always pass a collection for iteration
			Object object = (supportsMultiSelect) ? Arrays.asList(objs) : objs[0];
			EvaluationContext context = new EvaluationContext(null, object);
			context.setAllowPluginActivation(true);
			return enablementExpression.evaluate(context).equals(EvaluationResult.FALSE);
		} catch (CoreException e) {
			WorkbenchPlugin.log(e);
			return false;
		}
	}

	/**
	 * Returns an object array for the passed in object. If the object is an
	 * IStructuredSelection, then return its array otherwise return a 1 element
	 * Object[] containing the passed in object
	 *
	 * @return an object array representing the passed in object
	 */
	private Object[] getObjects(Object obj) {
		if (obj instanceof IStructuredSelection)
			return ((IStructuredSelection) obj).toArray();
		return new Object[] { obj };
	}

	/**
	 * Initialize the enablement expression for this decorator
	 */
	protected void initializeEnablement(IConfigurationElement definingElement) {
		IConfigurationElement[] elements = definingElement.getChildren(CHILD_ENABLED_WHEN);

		if (elements.length == 0)
			return;

		try {
			IConfigurationElement[] enablement = elements[0].getChildren();
			if (enablement.length == 0)
				return;
			enablementExpression = ExpressionConverter.getDefault().perform(enablement[0]);
		} catch (CoreException e) {
			WorkbenchPlugin.log(e);
		}

	}

	/**
	 * Returns whether the object passes a custom key value filter implemented by a
	 * matcher.
	 */
	private boolean testCustom(Object object, IActionFilter filter) {
		Map<String, String> filterProperties = getFilterProperties();

		if (filterProperties == null)
			return false;
		for (Entry<String, String> entry : filterProperties.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (!filter.testAttribute(object, key, value))
				return false;
		}
		return true;
	}

	/*
	 * @see IObjectContributor#canAdapt()
	 */
	@Override
	public boolean canAdapt() {
		return adaptable;
	}

	/**
	 * Get the id of the category.
	 *
	 * @return String
	 * @since 3.1
	 */
	public String getCategory() {
		return pageElement.getAttribute(CategorizedPageRegistryReader.ATT_CATEGORY);
	}

	/**
	 * Return the children of the receiver.
	 *
	 * @return Collection
	 */
	public Collection getSubPages() {
		return subPages;
	}

	/**
	 * Add child to the list of children.
	 */
	public void addSubPage(RegistryPageContributor child) {
		subPages.add(child);
	}

	private Map<String, String> getFilterProperties() {
		if (filterProperties == null || filterProperties.get() == null) {
			Map<String, String> map = new HashMap<>();
			filterProperties = new SoftReference<>(map);
			IConfigurationElement[] children = pageElement.getChildren();
			for (IConfigurationElement element : children) {
				processChildElement(map, element);
			}
		}
		return filterProperties.get();
	}

	/**
	 * Get the child with the given id.
	 *
	 * @return RegistryPageContributor
	 */
	public Object getChild(String id) {
		return subPages.stream().filter(c -> c.getPageId().equals(id)).findFirst().orElse(null);
	}

	/**
	 * Parses child element and processes it.
	 *
	 * @since 3.1
	 */
	private void processChildElement(Map<String, String> map, IConfigurationElement element) {
		String tag = element.getName();
		if (tag.equals(PropertyPagesRegistryReader.TAG_FILTER)) {
			String key = element.getAttribute(PropertyPagesRegistryReader.ATT_FILTER_NAME);
			String value = element.getAttribute(PropertyPagesRegistryReader.ATT_FILTER_VALUE);
			if (key == null || value == null)
				return;
			map.put(key, value);
		}
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(IConfigurationElement.class)) {
			return adapter.cast(getConfigurationElement());
		}
		return null;
	}

	/**
	 * @return boolean indicating if this page supports multiple selection
	 * @since 3.7
	 */
	boolean supportsMultipleSelection() {
		return supportsMultiSelect;
	}

	/**
	 * @return the configuration element
	 * @since 3.1
	 */
	IConfigurationElement getConfigurationElement() {
		return pageElement;
	}

	@Override
	public String getLocalId() {
		return pageId;
	}

	@Override
	public String getPluginId() {
		return pageElement.getContributor().getName();
	}
}
