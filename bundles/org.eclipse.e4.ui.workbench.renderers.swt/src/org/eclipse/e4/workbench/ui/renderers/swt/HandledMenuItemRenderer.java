/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Create a contribute part.
 */
public class HandledMenuItemRenderer extends MenuItemRenderer {

	@Inject
	Logger logger;

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MHandledMenuItem) || !(parent instanceof Menu))
			return null;

		MHandledMenuItem itemModel = (MHandledMenuItem) element;
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

		return newItem;
	}

	private void setEnabled(final MHandledItem item, final MenuItem newItem) {
		final IEclipseContext lclContext = getContext(item);
		lclContext.runAndTrack(new RunAndTrack() {
			@Override
			public boolean changed(IEclipseContext context) {
				if (newItem.isDisposed()) {
					return false;
				}
				EHandlerService service = lclContext.get(EHandlerService.class);
				ParameterizedCommand cmd = item.getWbCommand();
				if (cmd == null) {
					cmd = generateParameterizedCommand(item, lclContext);
				}
				if (cmd == null) {
					return false;
				}
				item.setEnabled(service.canExecute(cmd));
				return true;
			}
		});
	}

	protected void setItemText(MMenuItem model, MenuItem item) {
		String text = model.getLabel();
		if (model instanceof MHandledItem) {
			MHandledItem handledItem = (MHandledItem) model;
			IEclipseContext context = getContext(model);
			EBindingService bs = (EBindingService) context
					.get(EBindingService.class.getName());
			ParameterizedCommand cmd = handledItem.getWbCommand();
			if (cmd == null) {
				cmd = generateParameterizedCommand(handledItem, context);
			}
			if (cmd != null && (text == null || text.length() == 0)) {
				try {
					text = cmd.getName();
				} catch (NotDefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			TriggerSequence sequence = bs.getBestSequenceFor(handledItem
					.getWbCommand());
			if (sequence != null) {
				text = text + '\t' + sequence.format();
			}
			item.setText(text);
		} else {
			super.setItemText(model, item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.SWTPartRenderer#hookControllerLogic
	 * (org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void hookControllerLogic(MUIElement me) {
		// If the item is a CHECK or RADIO update the model's state to match
		super.hookControllerLogic(me);

		// 'Execute' the operation if possible
		if (me instanceof MHandledItem) {
			final MHandledItem item = (MHandledItem) me;
			final IEclipseContext lclContext = getContext(me);
			MenuItem mi = (MenuItem) me.getWidget();
			mi.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					EHandlerService service = (EHandlerService) lclContext
							.get(EHandlerService.class.getName());
					ParameterizedCommand cmd = item.getWbCommand();
					if (cmd == null) {
						cmd = generateParameterizedCommand(item, lclContext);
					}
					lclContext.set(MItem.class.getName(), item);
					service.executeHandler(cmd);
					lclContext.remove(MItem.class.getName());
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
	}

	private ParameterizedCommand generateParameterizedCommand(
			final MHandledItem item, final IEclipseContext lclContext) {
		ECommandService cmdService = (ECommandService) lclContext
				.get(ECommandService.class.getName());
		Map<String, Object> parameters = null;
		List<MParameter> modelParms = item.getParameters();
		if (modelParms != null && !modelParms.isEmpty()) {
			parameters = new HashMap<String, Object>();
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getName(), mParm.getValue());
			}
		}
		ParameterizedCommand cmd = cmdService.createCommand(item.getCommand()
				.getElementId(), parameters);
		item.setWbCommand(cmd);
		return cmd;
	}
}
