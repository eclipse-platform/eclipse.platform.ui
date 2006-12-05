package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @since 3.3
 * 
 */
final class WidgetContributionItem extends ContributionItem {
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

	public void fill(ToolBar parent, int index) {
		AbstractWorkbenchWidget widget = MenuAdditionCacheEntry
				.getWidget(widgetAddition);
		if (widget != null) {
			Composite widgetContainer = new Composite(parent, SWT.NONE);
			widget.fill(widgetContainer);
			Point prefSize = widget.getPreferredSize();

			ToolItem sepItem = new ToolItem(parent, SWT.SEPARATOR, index);
			sepItem.setControl(widgetContainer);
			sepItem.setWidth(prefSize.x);
		}
	}
}