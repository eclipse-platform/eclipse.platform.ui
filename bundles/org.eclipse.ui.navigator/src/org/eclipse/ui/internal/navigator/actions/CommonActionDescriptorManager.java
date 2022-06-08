/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentRegistryReader;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.Priority;

/**
 * Manages descriptors consumed from the 'actionProvider' elements of the
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extension point.
 *
 * @since 3.2
 *
 */
public class CommonActionDescriptorManager {

	private static final CommonActionProviderDescriptor[] NO_DESCRIPTORS = new CommonActionProviderDescriptor[0];

	private static final CommonActionDescriptorManager INSTANCE = new CommonActionDescriptorManager();

	private CommonActionDescriptorManager() {

		new ActionProviderRegistry().readRegistry();
	}

	/**
	 * @return the singleton instance of the registry
	 */
	public static CommonActionDescriptorManager getInstance() {
		return INSTANCE;
	}

	/* Provides a map of (ids, CommonActionProviderDescriptor)-pairs. */
	private final Map<String, CommonActionProviderDescriptor> dependentDescriptors = new LinkedHashMap<>();

	/* Provides a map of (ids, CommonActionProviderDescriptor)-pairs. */
	private final Map<String, CommonActionProviderDescriptor> rootDescriptors = new LinkedHashMap<>();

	/* Provides a map of (ids, CommonActionProviderDescriptor)-pairs. */
	private final Set<CommonActionProviderDescriptor> overridingDescriptors = new LinkedHashSet<>();


	private final LinkedList<CommonActionProviderDescriptor> rootDescriptorsList = new LinkedList<>();
	private final LinkedList<CommonActionProviderDescriptor> dependentDescriptorsList = new LinkedList<>();


	/**
	 *
	 * @param aDescriptor
	 *            A valid descriptor to begin managing.
	 */
	protected void addActionDescriptor(
			CommonActionProviderDescriptor aDescriptor) {

		if (aDescriptor.getDependsOnId() == null) {
			rootDescriptorsList.add(aDescriptor);
		} else {
			dependentDescriptorsList.add(aDescriptor);
		}

		if (aDescriptor.getOverridesId() != null) {
			overridingDescriptors.add(aDescriptor);
		}
	}

	private int findId(List<CommonActionProviderDescriptor> list, String id) {
		for (int i= 0, len = list.size(); i< len; i++) {
			CommonActionProviderDescriptor desc = list.get(i);
			if (desc.getId().equals(id))
				return i;
		}
		return -1;
	}



	/**
	 * Sorts the descriptors according to the appearsBefore property
	 */
	private void sortDescriptors(LinkedList<CommonActionProviderDescriptor> list, Map<String, CommonActionProviderDescriptor> outMap) {
		boolean changed = true;
		while (changed) {
			changed = false;
			for (int i = 0, len = list.size(); i < len; i++) {
				CommonActionProviderDescriptor desc = list.get(i);
				if (desc.getAppearsBeforeId() != null) {
					int beforeInd = findId(list, desc.getAppearsBeforeId());
					if (beforeInd < i) {
						list.add(beforeInd, desc);
						list.remove(i + 1);
						changed = true;
					}
				}
			}
		}
		for (CommonActionProviderDescriptor desc : list) {
			outMap.put(desc.getDefinedId(), desc);
		}
	}


	/**
	 * Orders the set of available descriptors based on the order defined by the
	 * <i>dependsOn</i> attribute from the &lt;actionProvider /&gt; element in
	 * <b>org.eclipse.ui.navigator.navigatorContent</b>
	 *
	 */
	protected void computeOrdering() {
		sortDescriptors(rootDescriptorsList, rootDescriptors);
		sortDescriptors(dependentDescriptorsList, dependentDescriptors);

		CommonActionProviderDescriptor dependentDescriptor;
		CommonActionProviderDescriptor requiredDescriptor;

		CommonActionProviderDescriptor descriptor;
		CommonActionProviderDescriptor overriddenDescriptor;
		for (Iterator<CommonActionProviderDescriptor> iter = overridingDescriptors.iterator(); iter.hasNext();) {
			descriptor = iter.next();
			if (rootDescriptors.containsKey(descriptor.getOverridesId())) {
				overriddenDescriptor = rootDescriptors
						.get(descriptor.getOverridesId());
				overriddenDescriptor.addOverridingDescriptor(descriptor);
			} else if (dependentDescriptors.containsKey(descriptor
					.getOverridesId())) {
				overriddenDescriptor = dependentDescriptors
						.get(descriptor.getOverridesId());
				overriddenDescriptor.addOverridingDescriptor(descriptor);
			}

		}

		Collection<CommonActionProviderDescriptor> unresolvedDependentDescriptors = new ArrayList<>(
				dependentDescriptors.values());

		for (Iterator<CommonActionProviderDescriptor> iter = dependentDescriptors.values().iterator(); iter
				.hasNext();) {
			dependentDescriptor = iter.next();
			requiredDescriptor = rootDescriptors
					.get(dependentDescriptor.getDependsOnId());
			if (requiredDescriptor == null) {
				requiredDescriptor = dependentDescriptors
						.get(dependentDescriptor.getDependsOnId());
			}
			if (requiredDescriptor != null) {
				requiredDescriptor.addDependentDescriptor(dependentDescriptor);
				unresolvedDependentDescriptors.remove(dependentDescriptor);
			}

		}

		dependentDescriptors.clear();

		if (!unresolvedDependentDescriptors.isEmpty()) {
			StringBuilder errorMessage = new StringBuilder(
					"There were unresolved dependencies for action provider extensions to a Common Navigator.\n" + //$NON-NLS-1$
							"Verify that the \"dependsOn\" attribute for each <actionProvider /> element is valid."); //$NON-NLS-1$

			CommonActionProviderDescriptor[] unresolvedDescriptors = unresolvedDependentDescriptors
					.toArray(new CommonActionProviderDescriptor[unresolvedDependentDescriptors
							.size()]);
			for (CommonActionProviderDescriptor unresolvedDescriptor : unresolvedDescriptors) {
				errorMessage.append("\nUnresolved dependency specified for actionProvider: ") //$NON-NLS-1$
						.append(unresolvedDescriptor.getDefinedId());
			}

			NavigatorPlugin.log(IStatus.WARNING, 0, errorMessage.toString(),
					null);

		}
		unresolvedDependentDescriptors.clear();

	}

	/**
	 *
	 * @param aContentService
	 *            The content service to use when filtering action providers;
	 *            only action providers bound directly or indirectly will be
	 *            returned.
	 * @param aContext
	 *            The action context that contains a valid selection.
	 * @return An array of visible, active, and enabled CommonActionProviders.
	 *         See <b>org.eclipse.ui.navigator.navigatorContent</b> for the
	 *         details of what each of these adjectives implies.
	 */
	public CommonActionProviderDescriptor[] findRelevantActionDescriptors(
			INavigatorContentService aContentService, ActionContext aContext) {
		Assert.isNotNull(aContext);
		IStructuredSelection structuredSelection = null;
		if (aContext.getSelection() instanceof IStructuredSelection) {
			structuredSelection = (IStructuredSelection) aContext
					.getSelection();
		} else {
			structuredSelection = StructuredSelection.EMPTY;
		}

		Set blockedProviders = new HashSet();
		CommonActionProviderDescriptor actionDescriptor = null;
		Set<CommonActionProviderDescriptor> providers = new LinkedHashSet<>();
		for (Iterator<CommonActionProviderDescriptor> providerItr = rootDescriptors.values().iterator(); providerItr
				.hasNext();) {
			actionDescriptor = providerItr
					.next();
			addProviderIfRelevant(aContentService, structuredSelection,
					actionDescriptor, providers, blockedProviders);
		}
		if (providers.size() > 0) {
			providers.removeAll(blockedProviders);
			return providers
					.toArray(new CommonActionProviderDescriptor[providers
							.size()]);
		}
		return NO_DESCRIPTORS;
	}

	/**
	 * @param aContentService
	 * @param structuredSelection
	 * @param actionDescriptor
	 * @param providers
	 */
	private boolean addProviderIfRelevant(
			INavigatorContentService aContentService,
			IStructuredSelection structuredSelection,
			CommonActionProviderDescriptor actionDescriptor, Set<CommonActionProviderDescriptor> providers, Set blockedProviders) {
		if (isVisible(aContentService, actionDescriptor)
				&& actionDescriptor.isEnabledFor(structuredSelection)) {

			if(actionDescriptor.hasOverridingDescriptors()) {
				for (Iterator iter = actionDescriptor.overridingDescriptors(); iter.hasNext();) {
					CommonActionProviderDescriptor descriptor = (CommonActionProviderDescriptor) iter.next();
					if(addProviderIfRelevant(aContentService, structuredSelection, descriptor, providers, blockedProviders)) {
						while(iter.hasNext())
							blockedProviders.add(iter.next());
						return true;
					}

				}
			}
			providers.add(actionDescriptor);
			if (actionDescriptor.hasDependentDescriptors()) {
				for (Iterator iter = actionDescriptor.dependentDescriptors(); iter
						.hasNext();) {
					addProviderIfRelevant(aContentService, structuredSelection,
							(CommonActionProviderDescriptor) iter.next(),
							providers, blockedProviders);
				}
			}
			return true;
		}
		return false;
	}

	private boolean isVisible(INavigatorContentService aContentService,
			CommonActionProviderDescriptor descriptor) {
		if (descriptor.isNested()) {
			return aContentService.isActive(descriptor.getId())
					&& aContentService.isVisible(descriptor.getId());
		}
		return aContentService.getViewerDescriptor().isVisibleActionExtension(
				descriptor.getId());
	}

	private class ActionProviderRegistry extends NavigatorContentRegistryReader {

		@Override
		public void readRegistry() {
			super.readRegistry();
			computeOrdering();
		}

		@Override
		protected boolean readElement(IConfigurationElement anElement) {
			if (TAG_ACTION_PROVIDER.equals(anElement.getName())) {
				addActionDescriptor(new CommonActionProviderDescriptor(
						anElement));
				return true;
			} else if (TAG_NAVIGATOR_CONTENT.equals(anElement.getName())) {

				IConfigurationElement[] actionProviders = anElement.getChildren(TAG_ACTION_PROVIDER);

				if (actionProviders.length > 0) {

					IConfigurationElement[] inheritedEnablement = anElement.getChildren(TAG_ENABLEMENT);
					if (inheritedEnablement.length == 0) {
						inheritedEnablement = anElement.getChildren(TAG_POSSIBLE_CHILDREN);
					}

					IConfigurationElement defaultEnablement = inheritedEnablement.length == 1 ? inheritedEnablement[0] : null;

					Priority defaultPriority = Priority.get(anElement.getAttribute(ATT_PRIORITY));


					if(defaultEnablement == null) {
						NavigatorPlugin.logError(0,
							"An actionProvider has been defined as the child " + //$NON-NLS-1$
							"of a navigatorContent extension that does not specify " + //$NON-NLS-1$
							"an <enablement/> or <possibleChildren /> expression. Please " + //$NON-NLS-1$
							"review the documentation and correct this error.", null); //$NON-NLS-1$
					}
					for (IConfigurationElement actionProvider : actionProviders) {
						if(defaultEnablement == null) {
							NavigatorPlugin.logError(0,
									"Disabling actionProvider: " + actionProvider.getAttribute(ATT_ID), null); //$NON-NLS-1$
						} else {
							SafeRunner.run(new AddProviderSafeRunner(actionProvider, defaultEnablement, defaultPriority, anElement));
						}
					}
				}
				return true;
			}
			return super.readElement(anElement);
		}

		private class AddProviderSafeRunner implements ISafeRunnable {

			private IConfigurationElement parentElement;
			private IConfigurationElement defaultEnablement;
			private IConfigurationElement actionProvider;
			private Priority defaultPriority;

			protected AddProviderSafeRunner(IConfigurationElement actionProvider,
											 IConfigurationElement defaultEnablement,
											 Priority defaultPriority,
											 IConfigurationElement parentElement) {
				this.actionProvider = actionProvider;
				this.defaultEnablement = defaultEnablement;
				this.defaultPriority = defaultPriority;
				this.parentElement = parentElement;
			}

			@Override
			public void run() throws Exception {
				addActionDescriptor(new CommonActionProviderDescriptor(
							actionProvider, defaultEnablement, defaultPriority, parentElement
									.getAttribute(ATT_ID), true));
			}

			@Override
			public void handleException(Throwable t) {
				NavigatorPlugin.logError(0, "Recovering from error while parsing actionProviders.", t); //$NON-NLS-1$
			}


		}
	}


}
