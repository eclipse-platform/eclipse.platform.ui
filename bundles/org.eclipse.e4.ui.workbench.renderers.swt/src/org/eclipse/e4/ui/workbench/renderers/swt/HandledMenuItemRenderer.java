/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Create a contribute part.
 */
public class HandledMenuItemRenderer extends MenuItemRenderer {

	private static final String HMI_STATIC_CONTEXT = "HMIR-staticContext"; //$NON-NLS-1$

	@Override
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

		ParameterizedCommand cmd = itemModel.getWbCommand();
		if (cmd == null) {
			IEclipseContext lclContext = getContext(itemModel);
			cmd = generateParameterizedCommand(itemModel, lclContext);
		}
		MenuItem newItem = new MenuItem((Menu) parent, flags, addIndex);
		setItemText(itemModel, newItem);
		setEnabled(itemModel, newItem);
		newItem.setImage(getImage(itemModel));
		newItem.setSelection(itemModel.isSelected());

		return newItem;
	}

	private void setEnabled(MHandledMenuItem itemModel, MenuItem newItem) {
		ParameterizedCommand cmd = itemModel.getWbCommand();
		if (cmd == null) {
			return;
		}
		final IEclipseContext lclContext = getContext(itemModel);
		EHandlerService service = lclContext.get(EHandlerService.class);
		final IEclipseContext staticContext = EclipseContextFactory
				.create(HMI_STATIC_CONTEXT);
		ContributionsAnalyzer.populateModelInterfaces(itemModel, staticContext,
				itemModel.getClass().getInterfaces());

		try {
			itemModel.setEnabled(service.canExecute(cmd, staticContext));
		} finally {
			staticContext.dispose();
		}
		newItem.setEnabled(itemModel.isEnabled());
	}

	@Override
	protected void setItemText(MMenuItem model, MenuItem item) {
		String text = model.getLocalizedLabel();
		if (model instanceof MHandledItem) {
			MHandledItem handledItem = (MHandledItem) model;
			IEclipseContext context = getContext(model);
			EBindingService bs = (EBindingService) context
					.get(EBindingService.class.getName());
			ParameterizedCommand cmd = handledItem.getWbCommand();
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
			item.setText(text == null ? handledItem.getCommand().getElementId()
					: text);
		} else {
			super.setItemText(model, item);
		}
	}

	@Override
	public void hookControllerLogic(MUIElement me) {
		// If the item is a CHECK or RADIO update the model's state to match
		super.hookControllerLogic(me);

		// 'Execute' the operation if possible
		if (me instanceof MHandledItem) {
			final MHandledItem item = (MHandledItem) me;
			final IEclipseContext lclContext = getContext(me);
			MenuItem mi = (MenuItem) me.getWidget();
			mi.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					EHandlerService service = (EHandlerService) lclContext
							.get(EHandlerService.class.getName());
					ParameterizedCommand cmd = item.getWbCommand();
					if (cmd == null) {
						return;
					}
					final IEclipseContext staticContext = EclipseContextFactory
							.create(HMI_STATIC_CONTEXT);
					if (e != null) {
						staticContext.set(Event.class, e);
					}
					ContributionsAnalyzer.populateModelInterfaces(item,
							staticContext, item.getClass().getInterfaces());
					try {
						service.executeHandler(cmd, staticContext);
					} finally {
						staticContext.dispose();
					}
				}
			});
		}
	}

	public static ParameterizedCommand generateParameterizedCommand(
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
