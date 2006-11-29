package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @since 3.3
 * 
 */
final public class SeparatorContributionItem extends AuthorityContributionItem {
	/**
	 * 
	 */
	private final IConfigurationElement sepAddition;

	/**
	 * @param id
	 * @param sepAddition
	 */
	SeparatorContributionItem(String id, IConfigurationElement sepAddition) {
		super(id);
		this.sepAddition = sepAddition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AdditionBase#isSeparator()
	 */
	public boolean isSeparator() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AdditionBase#fill(org.eclipse.swt.widgets.Menu,
	 *      int)
	 */
	public void fill(Menu parent, int index) {
		if (MenuAdditionCacheEntry.isSeparatorVisible(sepAddition)) {
			new MenuItem(parent, SWT.SEPARATOR, index);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AdditionBase#fill(org.eclipse.swt.widgets.Menu,
	 *      int)
	 */
	public void fill(ToolBar parent, int index) {
		if (isVisible()) {
			new ToolItem(parent, SWT.SEPARATOR, index);
		}
	}
}