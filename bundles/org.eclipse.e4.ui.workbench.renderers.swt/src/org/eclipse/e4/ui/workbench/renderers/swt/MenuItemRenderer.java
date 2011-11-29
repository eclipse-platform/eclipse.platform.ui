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
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.internal.expressions.ReferenceExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Create a contribute part.
 */
public abstract class MenuItemRenderer extends SWTPartRenderer {
	static class VisibleRAT extends RunAndTrack {
		Expression exp;
		MMenuItem item;
		ExpressionContext ec;
		boolean participating = true;

		public VisibleRAT(MMenuItem i, Expression e, IEclipseContext c) {
			exp = e;
			item = i;
			ec = new ExpressionContext(c);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.e4.core.contexts.RunAndTrack#changed(org.eclipse.e4.core
		 * .contexts.IEclipseContext)
		 */
		@Override
		public boolean changed(IEclipseContext context) {
			try {
				item.setVisible(EvaluationResult.FALSE != exp.evaluate(ec));
			} catch (CoreException e) {
				item.setVisible(false);
				WorkbenchSWTActivator.trace(
						"/trace/menus", "VisibleRAT failed", e); //$NON-NLS-1$//$NON-NLS-2$
			}
			return participating;
		}
	}

	private HashMap<MMenuItem, Expression> menuItemToExpression = new HashMap<MMenuItem, Expression>();
	private HashMap<MMenuItem, VisibleRAT> menuItemToRAT = new HashMap<MMenuItem, VisibleRAT>();

	@Inject
	Logger logger;
	@Inject
	IEventBroker eventBroker;
	private EventHandler itemUpdater = new EventHandler() {
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

	private EventHandler selectionUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MToolItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuItem))
				return;

			MMenuItem itemModel = (MMenuItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			MenuItem menuItem = (MenuItem) itemModel.getWidget();
			if (menuItem != null) {
				menuItem.setSelection(itemModel.isSelected());
			}
		}
	};

	private EventHandler enabledUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MMenuItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuItem))
				return;

			MMenuItem itemModel = (MMenuItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			MenuItem menuItem = (MenuItem) itemModel.getWidget();
			if (menuItem != null) {
				menuItem.setEnabled(itemModel.isEnabled());
			}
		}
	};

	@PostConstruct
	public void init() {
		eventBroker.subscribe(UIEvents.UILabel.TOPIC_ALL, itemUpdater);
		eventBroker.subscribe(UIEvents.Item.TOPIC_SELECTED, selectionUpdater);
		eventBroker.subscribe(UIEvents.Item.TOPIC_ENABLED, enabledUpdater);
	}

	@PreDestroy
	public void contextDisposed() {
		eventBroker.unsubscribe(itemUpdater);
		eventBroker.unsubscribe(selectionUpdater);
		eventBroker.unsubscribe(enabledUpdater);
	}

	protected void processVisible(MMenuItem item) {
		if (menuItemToExpression.get(item) != null) {
			return;
		}
		MCoreExpression exp = (MCoreExpression) item.getVisibleWhen();
		Expression ref = null;
		if (exp.getCoreExpression() instanceof Expression) {
			ref = (Expression) exp.getCoreExpression();
		} else {
			ref = new ReferenceExpression(exp.getCoreExpressionId());
			exp.setCoreExpression(ref);
		}
		menuItemToExpression.put(item, ref);
		IEclipseContext itemContext = getContext(item);
		VisibleRAT rat = new VisibleRAT(item, ref, itemContext);
		menuItemToRAT.put(item, rat);
		itemContext.runAndTrack(rat);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#hideChild
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
		menuItemToExpression.remove(child);
		VisibleRAT rat = menuItemToRAT.remove(child);
		if (rat != null) {
			rat.participating = false;
		}
	}

	protected void setItemText(MMenuItem model, MenuItem item) {
		String text = model.getLocalizedLabel();
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		item.setText(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#hookControllerLogic
	 * (org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void hookControllerLogic(MUIElement me) {
		// If the item is a CHECK or RADIO update the model's state to match
		if (me instanceof MItem) {
			final MItem item = (MItem) me;
			if (item.getType() == ItemType.CHECK
					|| item.getType() == ItemType.RADIO) {
				MenuItem ti = (MenuItem) me.getWidget();
				ti.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						item.setSelected(((MenuItem) e.widget).getSelection());
					}

					public void widgetDefaultSelected(SelectionEvent e) {
						item.setSelected(((MenuItem) e.widget).getSelection());
					}
				});
			}
		}
	}
}
