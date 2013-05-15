/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
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
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Create a contribute part.
 */
public class ToolItemRenderer extends SWTPartRenderer {

	private static final String TIR_STATIC_CONTEXT = "HCI-staticContext"; //$NON-NLS-1$

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
				toolItem.setToolTipText(getToolTipText(itemModel));
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
		String text = model.getLocalizedLabel();
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		item.setText(text);
	}

	private String getToolTipText(MItem item) {
		String text = item.getLocalizedTooltip();
		if (item instanceof MHandledItem) {
			MHandledItem handledItem = (MHandledItem) item;
			IEclipseContext context = getContext(item);
			EBindingService bs = (EBindingService) context
					.get(EBindingService.class.getName());
			ParameterizedCommand cmd = handledItem.getWbCommand();
			if (cmd == null) {
				cmd = generateParameterizedCommand(handledItem, context);
			}
			TriggerSequence sequence = bs.getBestSequenceFor(handledItem
					.getWbCommand());
			if (sequence != null) {
				if (text == null) {
					try {
						text = cmd.getName();
					} catch (NotDefinedException e) {
						return null;
					}
				}
				text = text + " (" + sequence.format() + ')'; //$NON-NLS-1$
			}
			return text;
		}
		return text;
	}

	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MToolItem) || !(parent instanceof ToolBar))
			return null;

		MToolItem itemModel = (MToolItem) element;

		// determine the index at which we should create the new item
		int addIndex = calcVisibleIndex(element);

		// OK, it's a real menu item, what kind?
		MMenu menu = itemModel.getMenu();
		int flags = 0;
		if (menu != null) {
			flags |= SWT.DROP_DOWN;
		} else if (itemModel.getType() == ItemType.PUSH)
			flags |= SWT.PUSH;
		else if (itemModel.getType() == ItemType.CHECK)
			flags |= SWT.CHECK;
		else if (itemModel.getType() == ItemType.RADIO)
			flags |= SWT.RADIO;

		ToolItem newItem = new ToolItem((ToolBar) parent, flags, addIndex);
		if (itemModel.getLabel() != null)
			newItem.setText(itemModel.getLocalizedLabel());

		newItem.setToolTipText(getToolTipText(itemModel));

		newItem.setImage(getImage((MUILabel) element));

		newItem.setEnabled(itemModel.isEnabled());

		newItem.setSelection(itemModel.isSelected());

		return newItem;
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
				ToolItem ti = (ToolItem) me.getWidget();
				ti.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e) {
						item.setSelected(((ToolItem) e.widget).getSelection());
					}

					public void widgetDefaultSelected(SelectionEvent e) {
						item.setSelected(((ToolItem) e.widget).getSelection());
					}
				});
			} else if (me instanceof MToolItem) {
				final MMenu mmenu = ((MToolItem) me).getMenu();
				if (mmenu != null) {
					final ToolItem ti = (ToolItem) me.getWidget();
					ti.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							if (e.detail == SWT.ARROW) {
								Menu menu = getMenu(mmenu, ti);

								Rectangle itemBounds = ti.getBounds();
								Point displayAt = ti.getParent().toDisplay(
										itemBounds.x,
										itemBounds.y + itemBounds.height);
								menu.setLocation(displayAt);
								menu.setVisible(true);

								Display display = menu.getDisplay();
								while (menu.isVisible()) {
									if (!display.readAndDispatch()) {
										display.sleep();
									}
								}
							}
						}
					});
				}
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
			final ToolItem ti = (ToolItem) me.getWidget();
			final Display display = ti.getDisplay();
			display.timerExec(500, new Runnable() {
				boolean logged = false;

				public void run() {
					if (ti.isDisposed()) {
						return;
					}
					SafeRunner.run(new ISafeRunnable() {
						public void run() throws Exception {
							EHandlerService service = lclContext
									.get(EHandlerService.class);
							if (service == null) {
								// no localized handler service
								return;
							}
							ParameterizedCommand cmd = item.getWbCommand();
							if (cmd == null) {
								cmd = generateParameterizedCommand(item,
										lclContext);
							}
							if (cmd == null) {
								return;
							}
							final IEclipseContext staticContext = EclipseContextFactory
									.create(TIR_STATIC_CONTEXT);
							ContributionsAnalyzer.populateModelInterfaces(item,
									staticContext, item.getClass()
											.getInterfaces());
							try {
								item.setEnabled(service.canExecute(cmd,
										staticContext));
							} finally {
								staticContext.dispose();
							}
						}

						public void handleException(Throwable exception) {
							if (!logged) {
								logged = true;
								logger.error(
										exception,
										"Internal error during tool item enablement updating, this is only logged once per tool item."); //$NON-NLS-1$
							}
						}
					});
					// repeat until disposed
					display.timerExec(500, this);
				}
			});
			ti.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if (e.detail != SWT.ARROW) {
						EHandlerService service = (EHandlerService) lclContext
								.get(EHandlerService.class.getName());
						ParameterizedCommand cmd = item.getWbCommand();
						if (cmd == null) {
							cmd = generateParameterizedCommand(item, lclContext);
						}
						if (cmd == null) {
							Activator.trace(Policy.DEBUG_MENUS,
									"Failed to execute: " + item.getCommand(), //$NON-NLS-1$
									null);
							return;
						}
						final IEclipseContext staticContext = EclipseContextFactory
								.create(TIR_STATIC_CONTEXT);
						ContributionsAnalyzer.populateModelInterfaces(item,
								staticContext, item.getClass().getInterfaces());
						service.executeHandler(cmd, staticContext);
					}
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
	}

	/**
	 * @param mmenu
	 * @param shell
	 * @return
	 */
	protected Menu getMenu(final MMenu mmenu, ToolItem toolItem) {
		Object obj = mmenu.getWidget();
		if (obj instanceof Menu) {
			return (Menu) obj;
		}
		// this is a temporary passthrough of the IMenuCreator
		if (mmenu instanceof MRenderedMenu) {
			obj = ((MRenderedMenu) mmenu).getContributionManager();
			if (obj instanceof IContextFunction) {
				final IEclipseContext lclContext = getContext(mmenu);
				obj = ((IContextFunction) obj).compute(lclContext, null);
				((MRenderedMenu) mmenu).setContributionManager(obj);
			}
			if (obj instanceof IMenuCreator) {
				final IMenuCreator creator = (IMenuCreator) obj;
				final Menu menu = creator.getMenu(toolItem.getParent()
						.getShell());
				if (menu != null) {
					toolItem.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							if (menu != null && !menu.isDisposed()) {
								creator.dispose();
								((MRenderedMenu) mmenu).setWidget(null);
							}
						}
					});
					mmenu.setWidget(menu);
					menu.setData(AbstractPartRenderer.OWNING_ME, menu);
					return menu;
				}
			}
		}
		return null;
	}
}
