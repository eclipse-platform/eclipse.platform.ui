/*
 * Created on Nov 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.navigator.internal.filters;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 */
public class ExtensionFilterActivationManager {

	public static final String ACTIVATED_FILTERS = "activatedFilters"; //$NON-NLS-1$

	/* Associates this ExtensionFilterRegistry with a specific Common Navigator instance */
	private final String viewerId;

	/* Maintains a list of the active filters for the viewer with the viewerid */
	private final Set activatedFilters = new HashSet();

	private final ExtensionFilterViewerRegistry parentRegistry;

	public ExtensionFilterActivationManager(String theViewerId, ExtensionFilterViewerRegistry theParentRegistry) {
		this.viewerId = theViewerId;
		this.parentRegistry = theParentRegistry;
	}

	public void activateFilter(ExtensionFilterDescriptor descriptor, boolean enabled) {
		if (enabled)
			getActivatedFilters().add(getFilterActivationPreferenceKey(descriptor));
		else
			getActivatedFilters().remove(getFilterActivationPreferenceKey(descriptor));
	}

	public boolean isFilterActive(ExtensionFilterDescriptor descriptor) {
		return getActivatedFilters().contains(getFilterActivationPreferenceKey(descriptor));
	}

	public void revertFilterActivations(String navigatorExtensionIdKey) {

		try {
			Preferences preferences = NavigatorPlugin.getDefault().getPluginPreferences();

			String activatedFiltersString = preferences.getString(getExtensionPreferenceKeyForFilterActivations(navigatorExtensionIdKey));
			synchronized (activatedFilters) {
				if (activatedFiltersString != null && activatedFiltersString.length() > 0) {
					String activatedExtensionKey = null;
					StringTokenizer tokenizer = new StringTokenizer(activatedFiltersString, ";"); //$NON-NLS-1$
					while (tokenizer.hasMoreTokens()) {
						activatedExtensionKey = tokenizer.nextToken();
						if (activatedExtensionKey.length() > 0)
							activatedFilters.add(activatedExtensionKey);
					}
				} else { // leave the filters off by default
					List availableFilters = getParentRegistry().getExtensionFilterDescriptors(navigatorExtensionIdKey);
					for (int i = 0; i < availableFilters.size(); i++) {
						ExtensionFilterDescriptor filterDescriptor = (ExtensionFilterDescriptor) availableFilters.get(i);
						if (filterDescriptor.isEnabledByDefault())
							activatedFilters.add(getFilterActivationPreferenceKey(filterDescriptor));
					}
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void persistFilterActivations() {

		Preferences preferences = NavigatorPlugin.getDefault().getPluginPreferences();

		synchronized (activatedFilters) {
			/* ensure that the preference will be non-empty */
			StringBuffer activatedFiltersStringBuffer = null;

			Iterator navigatorExtensionIdIterator = getParentRegistry().getNavigatorFilters().keySet().iterator();
			String navigatorExtensionId = null;
			while (navigatorExtensionIdIterator.hasNext()) {

				navigatorExtensionId = (String) navigatorExtensionIdIterator.next();
				activatedFiltersStringBuffer = new StringBuffer(";"); //$NON-NLS-1$

				List availableFilters = getParentRegistry().getExtensionFilterDescriptors(navigatorExtensionId);
				for (int i = 0; i < availableFilters.size(); i++) {
					ExtensionFilterDescriptor filterDescriptor = (ExtensionFilterDescriptor) availableFilters.get(i);
					if (isFilterActive(filterDescriptor))
						activatedFiltersStringBuffer.append(getFilterActivationPreferenceKey(filterDescriptor)).append(";"); //$NON-NLS-1$
				}

				preferences.setValue(getExtensionPreferenceKeyForFilterActivations(navigatorExtensionId), activatedFiltersStringBuffer.toString());
			}


		}
		NavigatorPlugin.getDefault().savePluginPreferences();
	}

	/**
	 * 
	 * @param navigatorExtensionIdKey
	 * @return
	 */
	protected String getExtensionPreferenceKeyForFilterActivations(String navigatorExtensionIdKey) {
		return getViewerId() + "." + navigatorExtensionIdKey + "." + ACTIVATED_FILTERS; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param string
	 * @return
	 */
	private Object getFilterActivationPreferenceKey(ExtensionFilterDescriptor filterDescriptor) {
		return getFilterActivationPreferenceKey(filterDescriptor.getNavigatorExtensionId(), filterDescriptor.getId());
	}

	/**
	 * @param string
	 * @return
	 */
	private Object getFilterActivationPreferenceKey(String navigatorExtensionId, String filterId) {
		return getViewerId() + "." + navigatorExtensionId + "." + filterId + ".filterActivated"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @return Returns the viewerId.
	 */
	protected String getViewerId() {
		return viewerId;
	}

	/**
	 * @return Returns the activatedFilters.
	 */
	protected Set getActivatedFilters() {
		return activatedFilters;
	}

	/**
	 * @return Returns the parentRegistry.
	 */
	protected ExtensionFilterViewerRegistry getParentRegistry() {
		return parentRegistry;
	}
}