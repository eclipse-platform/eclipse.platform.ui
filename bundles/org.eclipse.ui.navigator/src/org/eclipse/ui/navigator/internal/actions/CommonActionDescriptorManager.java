package org.eclipse.ui.navigator.internal.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.extensions.NavigatorContentRegistryReader;

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
	private final Map dependentDescriptors = new HashMap();

	/* Provides a map of (ids, CommonActionProviderDescriptor)-pairs. */
	private final Map rootDescriptors = new HashMap();

	/**
	 * 
	 * @param aDescriptor
	 *            A valid descriptor to begin managing.
	 */
	protected void addActionDescriptor(
			CommonActionProviderDescriptor aDescriptor) {

		if (aDescriptor.getDependsOnId() == null)
			rootDescriptors.put(aDescriptor.getId(), aDescriptor);
		else
			dependentDescriptors.put(aDescriptor.getId(), aDescriptor);
	}

	/**
	 * Orders the set of availabe descriptors based on the order defined by the
	 * <i>dependsOn</i> attribute from the <actionProvider /> element in
	 * <b>org.eclipse.ui.navigator.navigatorContent</b>
	 * 
	 */
	protected void computeOrdering() {
		CommonActionProviderDescriptor dependentDescriptor;
		CommonActionProviderDescriptor requiredDescriptor;

		Collection unresolvedDependentDescriptors = new ArrayList(
				dependentDescriptors.values());

		for (Iterator iter = dependentDescriptors.values().iterator(); iter
				.hasNext();) {
			dependentDescriptor = (CommonActionProviderDescriptor) iter.next();
			requiredDescriptor = (CommonActionProviderDescriptor) rootDescriptors
					.get(dependentDescriptor.getDependsOnId());
			if (requiredDescriptor == null)
				requiredDescriptor = (CommonActionProviderDescriptor) dependentDescriptors
						.get(dependentDescriptor.getDependsOnId());
			if (requiredDescriptor != null) {
				requiredDescriptor.addDependentDescriptor(dependentDescriptor);
				unresolvedDependentDescriptors.remove(dependentDescriptor);
			}

		}

		dependentDescriptors.clear();

		if (!unresolvedDependentDescriptors.isEmpty()) {
			StringBuffer errorMessage = new StringBuffer("There were unresolved dependencies for action provider extensions to a Common Navigator.\n" + //$NON-NLS-1$
					"Verify that the \"dependsOn\" attribute for each <actionProvider /> element is valid."); //$NON-NLS-1$

			CommonActionProviderDescriptor[] unresolvedDescriptors = (CommonActionProviderDescriptor[]) unresolvedDependentDescriptors
					.toArray(new CommonActionProviderDescriptor[unresolvedDependentDescriptors
							.size()]);
			for (int i = 0; i < unresolvedDescriptors.length; i++) 
				errorMessage.append("\nUnresolved dependency specified for actionProvider: ").append(unresolvedDescriptors[i].getId()); //$NON-NLS-1$
			
			NavigatorPlugin
			.log(
					IStatus.WARNING,
					0,
					errorMessage.toString(),
					null); 

		}
		unresolvedDependentDescriptors.clear();

	}

	/**
	 * 
	 * @param aViewerDescriptor
	 *            The viewer descriptor to use when filtering action providers;
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
		IStructuredSelection structuredSelection = null;
		if (aContext.getSelection() instanceof IStructuredSelection)
			structuredSelection = (IStructuredSelection) aContext
					.getSelection();
		else
			structuredSelection = StructuredSelection.EMPTY;

		CommonActionProviderDescriptor actionDescriptor = null;
		List providers = new ArrayList();
		for (Iterator providerItr = rootDescriptors.values().iterator(); providerItr
				.hasNext();) {
			actionDescriptor = (CommonActionProviderDescriptor) providerItr
					.next();
			addProviderIfRelevant(aContentService, structuredSelection,
					actionDescriptor, providers);
		}
		if (providers.size() > 0)
			return (CommonActionProviderDescriptor[]) providers
					.toArray(new CommonActionProviderDescriptor[providers
							.size()]);
		return NO_DESCRIPTORS;
	}

	/**
	 * @param aViewerDescriptor
	 * @param structuredSelection
	 * @param actionDescriptor
	 * @param providers
	 */
	private void addProviderIfRelevant(
			INavigatorContentService aContentService,
			IStructuredSelection structuredSelection,
			CommonActionProviderDescriptor actionDescriptor, List providers) {
		if (isVisible(aContentService, actionDescriptor)
				&& actionDescriptor.isEnabledFor(structuredSelection)) {
			providers.add(actionDescriptor);
			if (actionDescriptor.hasDependentDescriptors())
				for (Iterator iter = actionDescriptor.dependentDescriptors(); iter
						.hasNext();)
					addProviderIfRelevant(aContentService,
							structuredSelection,
							(CommonActionProviderDescriptor) iter.next(),
							providers);
		}
	}

	private boolean isVisible(INavigatorContentService aContentService,
			CommonActionProviderDescriptor descriptor) {
		if (descriptor.isNested()) {
			return aContentService.isActive(descriptor.getId())
					&& aContentService.isVisible(descriptor.getId());
		}
		return aContentService.getViewerDescriptor().isVisibleActionExtension(descriptor.getId());
	}

	private class ActionProviderRegistry extends NavigatorContentRegistryReader {

		public void readRegistry() {
			super.readRegistry();
			computeOrdering();
		}

		protected boolean readElement(IConfigurationElement anElement) {
			if (TAG_ACTION_PROVIDER.equals(anElement.getName())) {
				addActionDescriptor(new CommonActionProviderDescriptor(
						anElement));
				return true;
			} else if (TAG_NAVIGATOR_CONTENT.equals(anElement.getName())) {
				IConfigurationElement[] actionProviders = anElement
						.getChildren(TAG_ACTION_PROVIDER);
				if (actionProviders.length == 0)
					return true;
				IConfigurationElement defaultEnablement = null;
				IConfigurationElement[] enablement = anElement
						.getChildren(TAG_ENABLEMENT);
				if (enablement.length == 0)
					enablement = anElement.getChildren(TAG_TRIGGER_POINTS);
				if (enablement.length == 1)
					defaultEnablement = enablement[0];
				for (int i = 0; i < actionProviders.length; i++)
					addActionDescriptor(new CommonActionProviderDescriptor(
							actionProviders[i], defaultEnablement, anElement
									.getAttribute(ATT_ID), true));
				return true;
			}
			return super.readElement(anElement);
		}
	}
}
