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
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Create a contribute part.
 */
public class ToolItemRenderer extends SWTPartRenderer {

	@Inject
	Logger logger;
	@Inject
	IEventBroker eventBroker;

	private EventHandler itemUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MToolItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolItem))
				return;

			MToolItem itemModel = (MToolItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			ToolItem toolItem = (ToolItem) itemModel.getWidget();

			// No widget == nothing to update
			if (toolItem == null)
				return;

			String attName = (String) event
					.getProperty(UIEvents.EventTags.ATTNAME);
			if (UIEvents.UILabel.LABEL.equals(attName)) {
				setItemText(itemModel, toolItem);
			} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
				toolItem.setImage(getImage(itemModel));
			} else if (UIEvents.UILabel.TOOLTIP.equals(attName)) {
				toolItem.setToolTipText(itemModel.getTooltip());
				toolItem.setImage(getImage(itemModel));
			}
		}
	};

	private EventHandler selectionUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MToolItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolItem))
				return;

			MToolItem itemModel = (MToolItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			ToolItem toolItem = (ToolItem) itemModel.getWidget();
			if (toolItem != null) {
				toolItem.setSelection(itemModel.isSelected());
			}
		}
	};

	private EventHandler enabledUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MToolItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolItem))
				return;

			MToolItem itemModel = (MToolItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			ToolItem toolItem = (ToolItem) itemModel.getWidget();
			if (toolItem != null) {
				toolItem.setEnabled(itemModel.isEnabled());
			}
		}
	};

	@PostConstruct
	public void init() {
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UILabel.TOPIC),
				itemUpdater);
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.Item.TOPIC,
				UIEvents.Item.SELECTED), selectionUpdater);
		eventBroker
				.subscribe(UIEvents.buildTopic(UIEvents.Item.TOPIC,
						UIEvents.Item.ENABLED), enabledUpdater);
	}

	@PreDestroy
	public void contextDisposed() {
		eventBroker.unsubscribe(itemUpdater);
		eventBroker.unsubscribe(selectionUpdater);
		eventBroker.unsubscribe(enabledUpdater);
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

	private void setItemText(MToolItem model, ToolItem item) {
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
			TriggerSequence sequence = bs.getBestSequenceFor(handledItem
					.getWbCommand());
			if (sequence != null) {
				text = text + '\t' + sequence.format();
			}
			item.setText(text);
		} else {
			if (text == null) {
				text = ""; //$NON-NLS-1$
			}
			item.setText(text);
		}
	}

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MToolItem) || !(parent instanceof ToolBar))
			return null;

		MToolItem itemModel = (MToolItem) element;

		// determine the index at which we should create the new item
		int addIndex = calcVisibleIndex(element);

		// OK, it's a real menu item, what kind?
		int flags = itemModel.getChildren().size() > 0 ? SWT.DROP_DOWN : 0;
		if (itemModel.getType() == ItemType.PUSH)
			flags |= SWT.PUSH;
		else if (itemModel.getType() == ItemType.CHECK)
			flags |= SWT.CHECK;
		else if (itemModel.getType() == ItemType.RADIO)
			flags |= SWT.RADIO;

		ToolItem newItem = new ToolItem((ToolBar) parent, flags, addIndex);
		if (itemModel.getLabel() != null)
			newItem.setText(itemModel.getLabel());

		if (itemModel.getTooltip() != null)
			newItem.setToolTipText(itemModel.getTooltip());

		newItem.setImage(getImage((MUILabel) element));

		newItem.setEnabled(itemModel.isEnabled());
		newItem.setSelection(itemModel.isSelected());

		return newItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer#hideChild
	 * (org.eclipse.e4.ui.model.application.MElementContainer,
	 * org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void hideChild(MElementContainer<MUIElement> parentElement,
			MUIElement child) {
		super.hideChild(parentElement, child);

		// Since there's no place to 'store' a child that's not in a menu
		// we'll blow it away and re-create on an add
		Widget widget = (Widget) child.getWidget();
		if (widget != null && !widget.isDisposed())
			widget.dispose();
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
		if (me instanceof MItem) {
			final MItem item = (MItem) me;
			if (item.getType() == ItemType.CHECK
					|| item.getType() == ItemType.RADIO) {
				ToolItem ti = (ToolItem) me.getWidget();
				ti.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						item.setSelected(((ToolItem) e.widget).getSelection());
					}

					public void widgetDefaultSelected(SelectionEvent e) {
						item.setSelected(((ToolItem) e.widget).getSelection());
					}
				});
			}
		}

		// 'Execute' the operation if possible
		if (me instanceof MContribution
				&& ((MContribution) me).getContributionURI() != null) {
			final MToolItem item = (MToolItem) me;
			final MContribution contrib = (MContribution) me;
			final IEclipseContext lclContext = getContext(me);
			ToolItem ti = (ToolItem) me.getWidget();
			ti.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if (contrib.getObject() == null) {
						IContributionFactory cf = (IContributionFactory) lclContext
								.get(IContributionFactory.class.getName());
						contrib.setObject(cf.create(
								contrib.getContributionURI(), lclContext));
					}
					lclContext.set(MItem.class.getName(), item);
					ContextInjectionFactory.invoke(contrib.getObject(),
							Execute.class, lclContext);
					lclContext.remove(MItem.class.getName());
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		} else if (me instanceof MHandledItem) {
			final MHandledItem item = (MHandledItem) me;
			final IEclipseContext lclContext = getContext(me);
			ToolItem ti = (ToolItem) me.getWidget();
			ti.addSelectionListener(new SelectionListener() {
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
}
