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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Wrapper for a ConfigurationElement defining a Menu or Toolbar 'item'
 * addition.
 * 
 * @since 3.3
 * 
 */
public class ItemAddition extends AdditionBase {

	// Icon Support
	private ImageDescriptor imageDesc = null;
	private Image icon = null;

	// Dynamic Item support
	private AbstractDynamicMenuItem filler;

	public ItemAddition(IConfigurationElement element) {
		super(element);
	}

	public String getCommandId() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
	}

	public String getMnemonic() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_MNEMONIC);
	}

	public String getLabel() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
	}

	public String getTooltip() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLTIP);
	}

	public Image getIcon() {
		if (imageDesc == null) {
			String extendingPluginId = element.getDeclaringExtension()
					.getContributor().getName();

			imageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(
					extendingPluginId, getIconPath());
		}

		// Stall loading the icon until first access
		if (icon == null && imageDesc != null) {
			icon = imageDesc.createImage(true, null);
		}
		return icon;
	}

	public int getStyle() {
		// TODO: Check the command type to determine the 'style'
		// (Push, Check, Radio)
		return SWT.PUSH;
	}

	public String getIconPath() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
	}

	public boolean isVisible() {
		// TODO: evaluate the 'visibleWhen' expression
		return true;
	}

	public boolean isEnabled() {
		// TODO: evaluate the 'enabledWhen' expression
		return true;
	}

	public String getClassSpec() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
	}

	public boolean isDynamic() {
		return getClassSpec() != null && getClassSpec().length() > 0;
	}

	public AbstractDynamicMenuItem getFiller() {
		if (filler == null) {
			filler = loadFiller();
		}
		return filler;
	}

	/**
	 * @return
	 */
	private AbstractDynamicMenuItem loadFiller() {
		if (filler == null) {
			try {
				filler = (AbstractDynamicMenuItem) element
						.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return filler;
	}

	public String toString() {
		return getClass().getName()
				+ "(" + getLabel() + ":" + getTooltip() + ") " + getIconPath(); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AdditionBase#getContribution()
	 */
	public IContributionItem getContributionItem(boolean forMenu) {
		return new ContributionItem(getId()) {

			public void fill(Menu parent, int index) {
				MenuItem newItem = new MenuItem(parent, getStyle(), index);
				newItem.setText(getLabel());

				if (getIconPath() != null)
					newItem.setImage(getIcon());

				newItem.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						// Execute through the command service
					}

					public void widgetSelected(SelectionEvent e) {
						// Execute through the command service
					}
				});
			}

			public void fill(ToolBar parent, int index) {
				ToolItem newItem = new ToolItem(parent, getStyle(), index);

				if (getIconPath() != null)
					newItem.setImage(getIcon());
				else if (getLabel() != null)
					newItem.setText(getLabel());

				if (getTooltip() != null)
					newItem.setToolTipText(getTooltip());
				else
					newItem.setToolTipText(getLabel());

				newItem.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
						// Execute through the command service
					}

					public void widgetSelected(SelectionEvent e) {
						// Execute through the command service
					}
				});
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
		};
	}
}
