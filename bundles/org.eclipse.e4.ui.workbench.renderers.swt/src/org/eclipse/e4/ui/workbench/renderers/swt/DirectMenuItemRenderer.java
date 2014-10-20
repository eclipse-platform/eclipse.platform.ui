/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Create a contribute part.
 */
public class DirectMenuItemRenderer extends MenuItemRenderer {

	@Inject
	Logger logger;

	@Override
	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MDirectMenuItem) || !(parent instanceof Menu))
			return null;

		MDirectMenuItem itemModel = (MDirectMenuItem) element;
		if (itemModel.getVisibleWhen() != null) {
			processVisible(itemModel);
		}

		if (!itemModel.isVisible()) {
			return null;
		}

		// determine the index at which we should create the new item
		int addIndex = calcVisibleIndex(element);

		// OK, it's a real menu item, what kind?
		int flags = 0;
		if (itemModel.getType() == ItemType.PUSH)
			flags = SWT.PUSH;
		else if (itemModel.getType() == ItemType.CHECK)
			flags = SWT.CHECK;
		else if (itemModel.getType() == ItemType.RADIO)
			flags = SWT.RADIO;

		MenuItem newItem = new MenuItem((Menu) parent, flags, addIndex);
		setItemText(itemModel, newItem);
		newItem.setImage(getImage(itemModel));
		setEnabled(itemModel, newItem);
		newItem.setEnabled(itemModel.isEnabled());
		newItem.setSelection(itemModel.isSelected());

		return newItem;
	}

	private void setEnabled(MDirectMenuItem itemModel, final MenuItem newItem) {
		// TODO direct query to @CanExecute goes here
	}

	@Override
	public void hookControllerLogic(MUIElement me) {
		super.hookControllerLogic(me);

		// 'Execute' the operation if possible
		if (me instanceof MDirectMenuItem
				&& ((MDirectMenuItem) me).getContributionURI() != null) {
			final MMenuItem item = (MMenuItem) me;
			final MDirectMenuItem contrib = (MDirectMenuItem) me;
			final IEclipseContext lclContext = getContext(me);
			MenuItem mi = (MenuItem) me.getWidget();
			mi.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					lclContext.set(MItem.class.getName(), item);
					ContextInjectionFactory.invoke(contrib.getObject(),
							Execute.class, lclContext);
					lclContext.remove(MItem.class.getName());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
	}
}
