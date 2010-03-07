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

import org.eclipse.e4.core.services.events.IEventBroker;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.annotations.PreDestroy;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MParameter;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Create a contribute part.
 */
public class MenuItemRenderer extends SWTPartRenderer {

	@Inject
	Logger logger;
	@Inject
	IEventBroker eventBroker;
	private EventHandler itemUpdater;

	@PostConstruct
	public void init() {
		itemUpdater = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MMenuItem
				if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuItem))
					return;

				MMenuItem itemModel = (MMenuItem) event
						.getProperty(UIEvents.EventTags.ELEMENT);
				MenuItem menuItem = (MenuItem) itemModel.getWidget();

				// No widget == nothing to update
				if (menuItem == null)
					return;

				String attName = (String) event
						.getProperty(UIEvents.EventTags.ATTNAME);
				if (UIEvents.UILabel.LABEL.equals(attName)) {
					setItemText(itemModel, menuItem);
				} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
					menuItem.setImage(getImage(itemModel));
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UILabel.TOPIC),
				itemUpdater);
	}

	@PreDestroy
	public void contextDisposed() {
		eventBroker.unsubscribe(itemUpdater);
	}

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MMenuItem) || !(parent instanceof Menu))
			return null;

		MMenuItem itemModel = (MMenuItem) element;
		Menu parentMenu = (Menu) parent;

		// determine the index at which we should create the new item
		int addIndex = calcVisibleIndex(element);

		if (itemModel.getType() == ItemType.SEPARATOR) {
			return new MenuItem(parentMenu, SWT.SEPARATOR, addIndex);
		}

		// OK, it's a real menu item, what kind?
		int flags = 0;
		if (itemModel.getChildren().size() > 0)
			flags = SWT.CASCADE;
		else if (itemModel.getType() == ItemType.PUSH)
			flags = SWT.PUSH;
		else if (itemModel.getType() == ItemType.CHECK)
			flags = SWT.CHECK;
		else if (itemModel.getType() == ItemType.RADIO)
			flags = SWT.RADIO;

		MenuItem newItem = new MenuItem((Menu) parent, flags, addIndex);
		setItemText(itemModel, newItem);
		newItem.setEnabled(itemModel.isEnabled());

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

	private void setItemText(MMenuItem model, MenuItem item) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.SWTPartRenderer#hookControllerLogic
	 * (org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void hookControllerLogic(MUIElement me) {
		if (me instanceof MContribution
				&& ((MContribution) me).getURI() != null) {
			final MContribution contrib = (MContribution) me;
			final IEclipseContext lclContext = getContext(me);
			MenuItem mi = (MenuItem) me.getWidget();
			mi.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if (contrib.getObject() == null) {
						IContributionFactory cf = (IContributionFactory) lclContext
								.get(IContributionFactory.class.getName());
						contrib.setObject(cf.create(contrib.getURI(),
								lclContext));
					}
					try {
						ContextInjectionFactory.invoke(contrib.getObject(),
								"execute", lclContext); //$NON-NLS-1$
					} catch (InvocationTargetException e1) {
						if (logger != null)
							logger.error(e1);
					} catch (CoreException e1) {
						if (logger != null)
							logger.error(e1);
					}
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		} else if (me instanceof MHandledItem) {
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
					service.executeHandler(cmd);
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.AbstractPartRenderer#getUIContainer
	 * (org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public Object getUIContainer(MUIElement element) {
		if (!(element instanceof MMenuItem))
			return null;

		if (!(element.getParent().getWidget() instanceof MenuItem))
			return null;

		MenuItem mi = (MenuItem) element.getParent().getWidget();
		if (mi.getMenu() == null) {
			mi.setMenu(new Menu(mi));
		}

		return mi.getMenu();
	}

	private ParameterizedCommand generateParameterizedCommand(
			final MHandledItem item, final IEclipseContext lclContext) {
		ECommandService cmdService = (ECommandService) lclContext
				.get(ECommandService.class.getName());
		Map<String, Object> parameters = null;
		EList<MParameter> modelParms = item.getParameters();
		if (modelParms != null && !modelParms.isEmpty()) {
			parameters = new HashMap<String, Object>();
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getTag(), mParm.getValue());
			}
		}
		ParameterizedCommand cmd = cmdService.createCommand(item.getCommand()
				.getId(), parameters);
		item.setWbCommand(cmd);
		return cmd;
	}
}
