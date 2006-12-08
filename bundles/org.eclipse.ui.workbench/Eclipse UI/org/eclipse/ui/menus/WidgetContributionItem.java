package org.eclipse.ui.menus;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * An {@link IContributionItem} abstraction that allows the contribution
 * of widgets into a toolbar.
 *  
 * @since 3.3
 * 
 */
public abstract class WidgetContributionItem extends ContributionItem {

	/**
	 * @param id
	 */
	public WidgetContributionItem(String id) {
		super(id);
	}

	public void fill(ToolBar parent, int index) {
		IWorkbenchWidget widget = createWidget();
		if (widget != null) {
			Composite widgetContainer = new Composite(parent, SWT.NONE);
			widget.fill(widgetContainer);
			
			Point prefSize;
			if (widget instanceof AbstractWorkbenchTrimWidget)
				prefSize = ((AbstractWorkbenchTrimWidget)widget).getPreferredSize();
			else
				prefSize = widgetContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT);

			ToolItem sepItem = new ToolItem(parent, SWT.SEPARATOR, index);
			sepItem.setControl(widgetContainer);
			sepItem.setWidth(prefSize.x);
		}
	}

	public abstract IWorkbenchWidget createWidget();
}
