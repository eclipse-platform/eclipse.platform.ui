package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.menus.AbstractWorkbenchTrimWidget;

/**
 * @since 3.3
 * 
 */
final class WidgetContributionItem extends WidgetDataContributionItem {
	/**
	 * 
	 */
	private final IConfigurationElement widgetAddition;

	/**
	 * @param id
	 * @param widgetAddition
	 */
	WidgetContributionItem(String id, IConfigurationElement widgetAddition) {
		super(id);
		this.widgetAddition = widgetAddition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.WidgetDataContributionItem#createWidget()
	 */
	public AbstractWorkbenchTrimWidget createWidget() {
		return MenuAdditionCacheEntry.getWidget(widgetAddition);
	}
}