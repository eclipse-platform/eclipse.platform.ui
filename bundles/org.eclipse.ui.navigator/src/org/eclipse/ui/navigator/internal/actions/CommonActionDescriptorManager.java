package org.eclipse.ui.navigator.internal.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.Utilities;
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

	private static boolean isInitialized;

	/**
	 * @return the singleton instance of the registry
	 */
	public static CommonActionDescriptorManager getInstance() {
		if (isInitialized)
			return INSTANCE;
		synchronized (INSTANCE) {
			if (!isInitialized) {
				INSTANCE.init();
				isInitialized = true;
			}
		}
		return INSTANCE;
	}

	private void init() {
		new ActionProviderRegistry().readRegistry();
	}

	private Set actionProviderDescriptors = new HashSet();

	/**
	 * 
	 * @param aDescriptor
	 *            A valid descriptor to begin managing.
	 */
	public void addActionDescriptor(CommonActionProviderDescriptor aDescriptor) {
		actionProviderDescriptors.add(aDescriptor);
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
			INavigatorViewerDescriptor aViewerDescriptor, ActionContext aContext) {
		IStructuredSelection structuredSelection = null;
		if (aContext.getSelection() instanceof IStructuredSelection)
			structuredSelection = (IStructuredSelection) aContext
					.getSelection();
		else
			structuredSelection = StructuredSelection.EMPTY;

		CommonActionProviderDescriptor actionDescriptor = null;
		List providers = new ArrayList();
		for (Iterator providerItr = actionProviderDescriptors.iterator(); providerItr
				.hasNext();) {
			actionDescriptor = (CommonActionProviderDescriptor) providerItr
					.next();
			if (isVisible(aViewerDescriptor, actionDescriptor)
					&& actionDescriptor.isEnabledFor(structuredSelection))
				providers.add(actionDescriptor);
		}
		if (providers.size() > 0)
			return (CommonActionProviderDescriptor[]) providers
					.toArray(new CommonActionProviderDescriptor[providers
							.size()]);
		return NO_DESCRIPTORS;
	}

	private boolean isVisible(INavigatorViewerDescriptor aViewerDescriptor,
			CommonActionProviderDescriptor descriptor) {
		if (descriptor.isNested()) {
			return Utilities.isActive(aViewerDescriptor, descriptor.getId())
					&& Utilities.isVisible(aViewerDescriptor, descriptor
							.getId());
		}
		return aViewerDescriptor.isVisibleActionExtension(descriptor.getId());
	}

	private class ActionProviderRegistry extends NavigatorContentRegistryReader {
 

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
