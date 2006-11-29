package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @since 3.3
 * 
 */
public final class CommandContributionItem extends AuthorityContributionItem {
	/**
	 * 
	 */
	private IConfigurationElement itemAddition;
	private LocalResourceManager localResourceManager;
	private Listener menuItemListener;

	private Listener getMenuItemListener() {
		if (menuItemListener == null) {
			menuItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						// to be determined
						break;
					}
				}

			};
		}
		return menuItemListener;
	}

	private void handleWidgetDispose(Event event) {
		dispose();
	}

	/**
	 * @param id
	 * @param itemAddition
	 */
	CommandContributionItem(String id, IConfigurationElement itemAddition) {
		super(id);
		this.itemAddition = itemAddition;
	}

	public void fill(Menu parent, int index) {
		MenuItem newItem = new MenuItem(parent, SWT.PUSH, index);
		newItem.setText(MenuAdditionCacheEntry.getLabel(itemAddition));

		ImageDescriptor iconDescriptor = MenuAdditionCacheEntry
				.getIconDescriptor(itemAddition);
		if (iconDescriptor != null) {
			LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			newItem.setImage(m.createImage(iconDescriptor));
			disposeOldImages();
			localResourceManager = m;
		}

		newItem.addListener(SWT.Dispose, getMenuItemListener());
		newItem.addListener(SWT.Selection, getMenuItemListener());
	}

	public void fill(ToolBar parent, int index) {
		ToolItem newItem = new ToolItem(parent, SWT.PUSH, index);

		ImageDescriptor iconDescriptor = MenuAdditionCacheEntry
				.getIconDescriptor(itemAddition);
		if (iconDescriptor != null) {
			LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			newItem.setImage(m.createImage(iconDescriptor));
			disposeOldImages();
			localResourceManager = m;
		} else if (MenuAdditionCacheEntry.getLabel(itemAddition) != null) {
			newItem.setText(MenuAdditionCacheEntry.getLabel(itemAddition));
		}

		if (MenuAdditionCacheEntry.getTooltip(itemAddition) != null)
			newItem.setToolTipText(MenuAdditionCacheEntry
					.getTooltip(itemAddition));
		else
			newItem.setToolTipText(MenuAdditionCacheEntry
					.getLabel(itemAddition));
		// TBD...Listener support
		// newItem.addListener(SWT.Selection, getToolItemListener());
		// newItem.addListener(SWT.Dispose, getToolItemListener());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#update()
	 */
	public void update() {
		update(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#update(java.lang.String)
	 */
	public void update(String id) {
		if (getParent() != null) {
			getParent().update(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AuthorityContributionItem#dispose()
	 */
	public void dispose() {
		disposeOldImages();
		itemAddition = null;
		super.dispose();
	}

	/**
	 * 
	 */
	private void disposeOldImages() {
		if (localResourceManager != null) {
			localResourceManager.dispose();
			localResourceManager = null;
		}
	}
}