package org.eclipse.ui.navigator.internal.extensions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * Provides a common superclass for all consumers of the
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extension point.
 * 
 * @since 3.2
 * 
 */
public class NavigatorContentRegistryReader extends RegistryReader implements
		INavigatorContentExtPtConstants {

	protected NavigatorContentRegistryReader() {
		super(NavigatorPlugin.PLUGIN_ID, TAG_NAVIGATOR_CONTENT);
	}

	protected boolean readElement(IConfigurationElement element) {
		String elementName = element.getName();

		/* These are all of the valid root tags that exist */
		return TAG_ACTION_PROVIDER.equals(elementName)
				|| TAG_NAVIGATOR_CONTENT.equals(elementName)
				|| TAG_COMMON_WIZARD.equals(elementName)
				|| TAG_COMMON_FILTER.equals(elementName)
				|| TAG_COMMON_SORTER.equals(elementName);
	}
}
