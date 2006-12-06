package org.eclipse.ui.internal.menus;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.menus.AbstractWorkbenchTrimWidget;

/**
 * @since 3.3
 * 
 */
public abstract class WidgetDataContributionItem extends ContributionItem {

	/**
	 * @param id
	 */
	public WidgetDataContributionItem(String id) {
		super(id);
	}

	public void fill(ToolBar parent, int index) {
		AbstractWorkbenchTrimWidget widget = createWidget();
		if (widget != null) {
			Composite widgetContainer = new Composite(parent, SWT.NONE);
			widget.fill(widgetContainer);
			Point prefSize = widget.getPreferredSize();

			ToolItem sepItem = new ToolItem(parent, SWT.SEPARATOR, index);
			sepItem.setControl(widgetContainer);
			sepItem.setWidth(prefSize.x);
		}
	}

	public abstract AbstractWorkbenchTrimWidget createWidget();
}
