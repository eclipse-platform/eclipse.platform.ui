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
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.internal.expressions.ReferenceExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.workbench.modeling.ExpressionContext;
import org.eclipse.e4.workbench.ui.UIEvents;
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
				item.setVisible(EvaluationResult.TRUE == exp.evaluate(ec));
				//System.err.println("" + item.isVisible() + ": " + item); //$NON-NLS-1$//$NON-NLS-2$
			} catch (CoreException e) {
				item.setVisible(false);
				e.printStackTrace();
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

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MMenuItem) || !(parent instanceof Menu))
			return null;

		MMenuItem itemModel = (MMenuItem) element;
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

	private void setEnabled(MMenuItem itemModel, final MenuItem newItem) {
		if (itemModel instanceof MHandledItem) {
			final MHandledItem item = (MHandledItem) itemModel;
			final IEclipseContext lclContext = getContext(itemModel);
			lclContext.runAndTrack(new RunAndTrack() {
				@Override
				public boolean changed(IEclipseContext context) {
					if (newItem.isDisposed()) {
						return false;
					}
					EHandlerService service = lclContext
							.get(EHandlerService.class);
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
	}

	private void processVisible(MMenuItem item) {
		if (menuItemToExpression.get(item) != null) {
			return;
		}
		MCoreExpression exp = (MCoreExpression) item.getVisibleWhen();
		ReferenceExpression ref = new ReferenceExpression(
				exp.getCoreExpressionId());
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
		menuItemToExpression.remove(child);
		VisibleRAT rat = menuItemToRAT.remove(child);
		if (rat != null) {
			rat.participating = false;
		}
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

		// 'Execute' the operation if possible
		if (me instanceof MContribution
				&& ((MContribution) me).getContributionURI() != null) {
			final MMenuItem item = (MMenuItem) me;
			final MContribution contrib = (MContribution) me;
			final IEclipseContext lclContext = getContext(me);
			MenuItem mi = (MenuItem) me.getWidget();
			mi.addSelectionListener(new SelectionListener() {
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
