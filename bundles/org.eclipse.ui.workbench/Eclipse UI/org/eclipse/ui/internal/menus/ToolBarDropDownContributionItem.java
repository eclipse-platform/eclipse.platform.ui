/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.menus;

import java.util.Map;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;

/**
 * A contribution item which provides drop down menu support specifically for
 * ToolBar drop down menus. It can be used in
 * {@link AbstractContributionFactory#createContributionItems(IMenuService, java.util.List)}.
 * <p>
 * It currently supports placement only in toolbars.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.3
 */
public final class ToolBarDropDownContributionItem extends ContributionItem {
	/**
	 * 
	 */
	private LocalResourceManager localResourceManager;

	private Listener menuItemListener;

	private Widget widget;

	//private String commandId;
	
	private ImageDescriptor icon;

	private String label;

	private String tooltip;

	private IMenuService menuService;

	private MenuManager menuManager;
	
	private Menu menu = null;
	
	/**
	 * Create a ToolBarDropDownConributionItem to place in a ContributionManager.
	 * <p>
	 * <b>NOTE:</b> The comand part of this contribution is not currently
	 * implemented.
	 * </p>
	 * 
	 * @param id
	 *            The id for this item. May be <code>null</code>. Items
	 *            without an id cannot be referenced later.
	 * @param commandId
	 *            A command id for a defined command. Must not be
	 *            <code>null</code>.
	 * @param parameters
	 *            A map of strings to strings which represent parameter names to
	 *            values. The parameter names must match those in the command
	 *            definition.
	 * @param icon
	 *            An icon for this item. May be <code>null</code>.
	 * @param label
	 *            A label for this item. May be <code>null</code>.
	 * @param tooltip
	 *            A tooltip for this item. May be <code>null</code>. Tooltips
	 *            are currently only valid for toolbar contributions.
	 */
	public ToolBarDropDownContributionItem(String id, String commandId, Map parameters,
			ImageDescriptor icon, String label, String tooltip) {
		super(id);
		//this.commandId = commandId;
		this.icon = icon;
		this.label = label;
		this.tooltip = tooltip;

		// Access the menu service
		menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);

		// Construct a MenuManager and its menu
		menuManager = new MenuManager(label, getId());
		menuManager.setRemoveAllWhenShown(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.ToolBar,
	 *      int)
	 */
	public void fill(ToolBar parent, int index) {
		ToolItem newItem = new ToolItem(parent, SWT.DROP_DOWN, index);
		newItem.setData(this);
		newItem.setEnabled(isEnabled());

		if (icon != null) {
			LocalResourceManager m = new LocalResourceManager(JFaceResources
					.getResources());
			newItem.setImage(m.createImage(icon));
			disposeOldImages();
			localResourceManager = m;
		} else if (label != null) {
			newItem.setText(label);
		}

		if (tooltip != null)
			newItem.setToolTipText(tooltip);
		else if (label != null)
			newItem.setToolTipText(label);

		newItem.addListener(SWT.Selection, getItemListener());
		newItem.addListener(SWT.Dispose, getItemListener());

		widget = newItem;
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
		if (widget != null) {
			if (widget instanceof MenuItem) {
				MenuItem item = (MenuItem) widget;
				if (item.isEnabled() != isEnabled()) {
					item.setEnabled(isEnabled());
				}
			} else if (widget instanceof ToolItem) {
				ToolItem item = (ToolItem) widget;
				if (item.isEnabled() != isEnabled()) {
					item.setEnabled(isEnabled());
				}
			}
		}
		
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

	private Listener getItemListener() {
		if (menuItemListener == null) {
			menuItemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Dispose:
						handleWidgetDispose(event);
						break;
					case SWT.Selection:
						if (event.widget != null) {
							handleWidgetSelection(event);
						}
						break;
					}
				}
			};
		}
		return menuItemListener;
	}

	private void handleWidgetDispose(Event event) {
		if (event.widget == widget) {
			widget.removeListener(SWT.Selection, getItemListener());
			widget.removeListener(SWT.Dispose, getItemListener());
			widget = null;
			dispose();
		}
	}

	private void handleWidgetSelection(Event event) {
		Widget item = event.widget;
		if (item != null) {
			int style = item.getStyle();
			if ((style & SWT.DROP_DOWN) != 0) {
				if (event.detail == 4) { // on drop-down button
					ToolItem ti = (ToolItem) item;
					
					// Defer menu creation until the first 'open'
					if (menu == null) {
						menu = menuManager.createContextMenu(ti.getParent());
						menuManager.addMenuListener(new IMenuListener() {
							public void menuAboutToShow(IMenuManager manager) {
								populateMenu();
							}
						});
					}
					
					// position the menu below the drop down item
					Rectangle b = ti.getBounds();
					Point p = ti.getParent().toDisplay(
							new Point(b.x, b.y + b.height));
					menu.setLocation(p.x, p.y); // waiting for SWT
												// 0.42
					menu.setVisible(true);
					return; // we don't fire the action
				}
			}
			// TODO: 'Comand' code goes here...
		}
	}

	/**
	 * Use the menu service to add any additons into the MenuManager
	 */
	private void populateMenu() {		
		menuService.populateContributionManager(menuManager, "menu:" + getId()); //$NON-NLS-1$
	}

	/**
	 * Update the icon on this command contribution item.
	 * 
	 * @param desc
	 *            The descriptor for the new icon to display.
	 */
	public void setIcon(ImageDescriptor desc) {
		icon = desc;
		if (widget instanceof MenuItem) {
			MenuItem item = (MenuItem) widget;
			disposeOldImages();
			if (desc != null) {
				LocalResourceManager m = new LocalResourceManager(
						JFaceResources.getResources());
				item.setImage(m.createImage(desc));
				localResourceManager = m;
			}
		} else if (widget instanceof ToolItem) {
			ToolItem item = (ToolItem) widget;
			disposeOldImages();
			if (desc != null) {
				LocalResourceManager m = new LocalResourceManager(
						JFaceResources.getResources());
				item.setImage(m.createImage(desc));
				localResourceManager = m;
			}
		}
	}

	/**
	 * Update the label on this command contribution item.
	 * 
	 * @param text
	 *            The new label to display.
	 */
	public void setLabel(String text) {
		label = text;
		if (widget instanceof MenuItem) {
			((MenuItem) widget).setText(text);
		} else if (widget instanceof ToolItem) {
			((ToolItem) widget).setText(text);
		}
	}

	/**
	 * Update the tooltip on this command contribution item. Tooltips are
	 * currently only valid for toolbar contributions.
	 * 
	 * @param text
	 *            The new tooltip to display.
	 */
	public void setTooltip(String text) {
		tooltip = text;
		if (widget instanceof ToolItem) {
			((ToolItem) widget).setToolTipText(text);
		}
	}
}
