/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 *
 */
public class NewMenuRenderer extends SWTPartRenderer {
	private static final String NO_LABEL = "UnLabled"; //$NON-NLS-1$

	private Map<MMenu, MenuManager> modelToManager = new HashMap<MMenu, MenuManager>();

	private Map<MMenuItem, IContributionItem> modelToContribution = new HashMap<MMenuItem, IContributionItem>();

	@Inject
	private Logger logger;

	@Inject
	IEventBroker eventBroker;
	private EventHandler itemUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MMenuItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuItem))
				return;

			MMenuItem itemModel = (MMenuItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);

			IContributionItem ici = modelToContribution.get(itemModel);
			if (ici == null) {
				return;
			}

			String attName = (String) event
					.getProperty(UIEvents.EventTags.ATTNAME);
			if (UIEvents.UILabel.LABEL.equals(attName)) {
				ici.update();
			} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
				ici.update();
			}
		}
	};

	private EventHandler toBeRenderedUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MMenuItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuItem))
				return;

			MMenuItem itemModel = (MMenuItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			String attName = (String) event
					.getProperty(UIEvents.EventTags.ATTNAME);
			if (UIEvents.UIElement.TOBERENDERED.equals(attName)) {
				Object obj = itemModel.getParent();
				if (!(obj instanceof MMenu)) {
					return;
				}
				MenuManager parent = modelToManager.get(obj);
				if (itemModel.isToBeRendered()) {
					if (parent != null) {
						modelProcessSwitch(parent, itemModel);
					}
				} else {
					IContributionItem ici = modelToContribution
							.remove(itemModel);
					if (ici != null && parent != null) {
						parent.remove(ici);
					}
					if (ici != null) {
						ici.dispose();
					}
				}
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
			IContributionItem ici = modelToContribution.get(itemModel);
			if (ici != null) {
				ici.update();
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
			IContributionItem ici = modelToContribution.get(itemModel);
			if (ici != null) {
				ici.update();
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
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC,
				UIEvents.UIElement.TOBERENDERED), toBeRenderedUpdater);

	}

	@PreDestroy
	public void contextDisposed() {
		eventBroker.unsubscribe(itemUpdater);
		eventBroker.unsubscribe(selectionUpdater);
		eventBroker.unsubscribe(enabledUpdater);
		eventBroker.unsubscribe(toBeRenderedUpdater);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#createWidget
	 * (org.eclipse.e4.ui.model.application.ui.MUIElement, java.lang.Object)
	 */
	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MMenu))
			return null;

		final MMenu menuModel = (MMenu) element;
		Menu newMenu = null;

		if (parent instanceof Decorations) {
			MUIElement container = (MUIElement) ((EObject) element)
					.eContainer();
			if (container instanceof MWindow) {
				MenuManager menuBarManager = new MenuManager(
						"MainMenu", menuModel.getElementId()); //$NON-NLS-1$
				modelToManager.put(menuModel, menuBarManager);
				newMenu = menuBarManager.createMenuBar((Decorations) parent);
				newMenu.setData(menuBarManager);
			} else {
				MenuManager popupManager = new MenuManager(
						"PopupMenu", menuModel.getElementId()); //$NON-NLS-1$
				modelToManager.put(menuModel, popupManager);
				newMenu = popupManager.createContextMenu((Control) parent);
				newMenu.setData(popupManager);
			}
		} else if (parent instanceof Menu) {
			// Object data = ((Menu) parent).getData();
			logger.debug(new Exception(), "Trying to render a sub menu " //$NON-NLS-1$
					+ menuModel + "\n\t" + parent); //$NON-NLS-1$

		} else if (parent instanceof Control) {
			MenuManager popupManager = new MenuManager("PopupMenu", //$NON-NLS-1$
					menuModel.getElementId());
			modelToManager.put(menuModel, popupManager);
			newMenu = popupManager.createContextMenu((Control) parent);
			newMenu.setData(popupManager);
		}
		return newMenu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#processContents
	 * (org.eclipse.e4.ui.model.application.ui.MElementContainer)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> container) {
		// I can either simply stop processing, or we can walk the model
		// ourselves like the "old" days
		// EMF gives us null lists if empty
		if (container == null)
			return;

		MenuManager parentManager = null;
		if (container.getWidget() != null
				&& (((Widget) container.getWidget()).getData() instanceof MenuManager)) {
			parentManager = (MenuManager) ((Widget) container.getWidget())
					.getData();
		}
		if (parentManager == null) {
			return;
		}
		// Process any contents of the newly created ME
		List<MUIElement> parts = container.getChildren();
		if (parts != null) {
			MUIElement[] plist = parts.toArray(new MUIElement[parts.size()]);
			for (int i = 0; i < plist.length; i++) {
				MUIElement childME = plist[i];
				modelProcessSwitch(parentManager, (MMenuElement) childME);
			}
		}
		parentManager.update(false);
	}

	/**
	 * @param parentManager
	 * @param menuModel
	 */
	private void processMenu(MenuManager parentManager, MMenu menuModel) {
		String menuText = getText(menuModel);
		ImageDescriptor desc = getImageDescriptor(menuModel);
		MenuManager menuManager = new MenuManager(menuText, desc,
				menuModel.getElementId());
		modelToManager.put(menuModel, menuManager);
		parentManager.add(menuManager);
		List<MMenuElement> parts = menuModel.getChildren();
		if (parts != null) {
			MMenuElement[] plist = parts
					.toArray(new MMenuElement[parts.size()]);
			for (int i = 0; i < plist.length; i++) {
				MMenuElement childME = plist[i];
				modelProcessSwitch(menuManager, childME);
			}
		}
	}

	/**
	 * @param menuManager
	 * @param childME
	 */
	void modelProcessSwitch(MenuManager menuManager, MMenuElement childME) {
		if (childME instanceof MHandledMenuItem) {
			MHandledMenuItem itemModel = (MHandledMenuItem) childME;
			processHandledItem(menuManager, itemModel);
		} else if (childME instanceof MDirectMenuItem) {
			MDirectMenuItem itemModel = (MDirectMenuItem) childME;
			processDirectItem(menuManager, itemModel, null);
		} else if (childME instanceof MMenu) {
			MMenu itemModel = (MMenu) childME;
			processMenu(menuManager, itemModel);
		}
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 * @param id
	 *            TODO
	 */
	void processDirectItem(MenuManager parentManager,
			MDirectMenuItem itemModel, String id) {
		final IEclipseContext lclContext = getContext(itemModel);
		DirectContributionItem ci = ContextInjectionFactory.make(
				DirectContributionItem.class, lclContext);
		ci.setModel(itemModel);
		parentManager.add(ci);
		modelToContribution.put(itemModel, ci);
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 */
	void processHandledItem(MenuManager parentManager,
			MHandledMenuItem itemModel) {
		final IEclipseContext lclContext = getContext(itemModel);
		HandledContributionItem ci = ContextInjectionFactory.make(
				HandledContributionItem.class, lclContext);
		ci.setModel(itemModel);
		parentManager.add(ci);
		modelToContribution.put(itemModel, ci);
	}

	private String getText(MMenu menuModel) {
		String text = menuModel.getLabel();
		if (text == null || text.length() == 0) {
			return NO_LABEL;
		}
		return text;
	}

	private ImageDescriptor getImageDescriptor(MUILabel element) {
		IEclipseContext localContext = context;
		String iconURI = element.getIconURI();
		if (iconURI != null && iconURI.length() > 0) {
			ISWTResourceUtilities resUtils = (ISWTResourceUtilities) localContext
					.get(IResourceUtilities.class.getName());
			return resUtils.imageDescriptorFromURI(URI.createURI(iconURI));
		}
		return null;
	}
}
