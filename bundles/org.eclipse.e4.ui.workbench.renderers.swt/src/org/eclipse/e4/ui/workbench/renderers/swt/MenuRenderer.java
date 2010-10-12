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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Create a contribute part.
 */
public class MenuRenderer extends SWTPartRenderer {
	private static final String NO_LABEL = "UnLabled"; //$NON-NLS-1$

	private Map<MMenu, MenuManager> modelToManager = new HashMap<MMenu, MenuManager>();
	private Map<MenuManager, MMenu> managerToModel = new HashMap<MenuManager, MMenu>();

	private Map<MMenuItem, IContributionItem> modelToContribution = new HashMap<MMenuItem, IContributionItem>();

	private Map<MMenuElement, ContributionRecord> modelContributionToRecord = new HashMap<MMenuElement, ContributionRecord>();

	@Inject
	private Logger logger;

	@Inject
	private MApplication application;

	@Inject
	IEventBroker eventBroker;
	private EventHandler itemUpdater = new EventHandler() {
		public void handleEvent(Event event) {
			// Ensure that this event is for a MMenuItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuItem))
				return;

			MMenuItem itemModel = (MMenuItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);

			IContributionItem ici = getContribution(itemModel);
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
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			String attName = (String) event
					.getProperty(UIEvents.EventTags.ATTNAME);
			if (element instanceof MMenuItem) {
				MMenuItem itemModel = (MMenuItem) element;
				if (UIEvents.UIElement.TOBERENDERED.equals(attName)) {
					Object obj = itemModel.getParent();
					if (!(obj instanceof MMenu)) {
						return;
					}
					MenuManager parent = getManager((MMenu) obj);
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
			if (UIEvents.UIElement.VISIBLE.equals(attName)) {
				if (element instanceof MMenu) {
					MMenu menuModel = (MMenu) element;
					MenuManager manager = getManager(menuModel);
					if (manager == null) {
						return;
					}
					manager.setVisible(menuModel.isVisible());
				} else if (element instanceof MMenuElement) {
					MMenuElement itemModel = (MMenuElement) element;
					IContributionItem ici = getContribution(itemModel);
					if (ici == null) {
						return;
					}
					ici.setVisible(itemModel.isVisible());
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

	private IMenuListener visibilityCalculationListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			MenuManager menuManager = (MenuManager) manager;
			if (menuManager.getMenu() == null) {
				return;
			}
			MMenu menuModel = getMenuModel(menuManager);
			if (menuModel == null) {
				Menu menu = menuManager.getMenu();
				Object obj = menu.getData(AbstractPartRenderer.OWNING_ME);
				if (obj == null && menu.getParentItem() != null) {
					obj = menu.getParentItem().getData(
							AbstractPartRenderer.OWNING_ME);
				}
				if (!(obj instanceof MMenu)) {
					return;
				}
				menuModel = (MMenu) obj;
			} else if (menuModel.getWidget() == null) {
				bindWidget(menuModel, menuManager.getMenu());
			}
			final IEclipseContext evalContext;
			if (menuModel instanceof MContext) {
				evalContext = ((MContext) menuModel).getContext();
			} else {
				evalContext = modelService.getContainingContext(menuModel);
			}
			HashSet<ContributionRecord> records = new HashSet<ContributionRecord>();
			for (MMenuElement element : menuModel.getChildren()) {
				ContributionRecord record = modelContributionToRecord
						.get(element);
				if (record != null && records.add(record)) {
					record.updateVisibility(evalContext);
				}
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
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC),
				toBeRenderedUpdater);

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
		boolean menuBar = false;

		if (parent instanceof Decorations) {
			MUIElement container = (MUIElement) ((EObject) element)
					.eContainer();
			if (container instanceof MWindow) {
				MenuManager menuBarManager = getManager(menuModel);
				if (menuBarManager == null) {
					menuBarManager = new MenuManager(NO_LABEL,
							menuModel.getElementId());
					linkModelToManager(menuModel, menuBarManager);
				}
				newMenu = menuBarManager.createMenuBar((Decorations) parent);
				((Decorations) parent).setMenuBar(newMenu);
				newMenu.setData(menuBarManager);
				menuBar = true;
			} else {
				MenuManager popupManager = getManager(menuModel);
				if (popupManager == null) {
					popupManager = new MenuManager(NO_LABEL,
							menuModel.getElementId());
					linkModelToManager(menuModel, popupManager);
				}
				newMenu = popupManager.createContextMenu((Control) parent);
				((Control) parent).setMenu(newMenu);
				newMenu.setData(popupManager);
			}
		} else if (parent instanceof Menu) {
			// Object data = ((Menu) parent).getData();
			logger.debug(new Exception(), "Trying to render a sub menu " //$NON-NLS-1$
					+ menuModel + "\n\t" + parent); //$NON-NLS-1$

		} else if (parent instanceof Control) {
			MenuManager popupManager = getManager(menuModel);
			if (popupManager == null) {
				popupManager = new MenuManager(NO_LABEL,
						menuModel.getElementId());
				linkModelToManager(menuModel, popupManager);
			}
			newMenu = popupManager.createContextMenu((Control) parent);
			((Control) parent).setMenu(newMenu);
			newMenu.setData(popupManager);
		}
		processContributions(menuModel, menuBar);
		if (newMenu != null) {
			newMenu.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					cleanUp(menuModel);
				}
			});
		}
		return newMenu;
	}

	/**
	 * @param menuModel
	 */
	protected void cleanUp(MMenu menuModel) {
		Collection<ContributionRecord> vals = modelContributionToRecord
				.values();
		for (ContributionRecord record : vals
				.toArray(new ContributionRecord[vals.size()])) {
			if (record.menuModel == menuModel) {
				record.dispose();
				for (MMenuElement copy : record.generatedElements) {
					modelContributionToRecord.remove(copy);
					if (copy instanceof MMenu) {
						MMenu menuCopy = (MMenu) copy;
						cleanUp(menuCopy);
						MenuManager copyManager = getManager(menuCopy);
						clearModelToManager(menuCopy, copyManager);
						if (copyManager != null) {
							copyManager.dispose();
						}
					} else {
						IContributionItem ici = modelToContribution
								.remove(copy);
						if (ici != null) {
							record.manager.remove(ici);
						}
					}
				}
				record.generatedElements.clear();
			}
		}
	}

	/**
	 * @param menuModel
	 * @param menuBar
	 */
	private void processContributions(MMenu menuModel, boolean menuBar) {
		final ArrayList<MMenuContribution> toContribute = new ArrayList<MMenuContribution>();
		ContributionsAnalyzer.XXXgatherMenuContributions(menuModel,
				application.getMenuContributions(), menuModel.getElementId(),
				toContribute, null, menuModel instanceof MPopupMenu);
		generateContributions(menuModel, toContribute, menuBar);
	}

	/**
	 * @param menuModel
	 * @param toContribute
	 */
	private void generateContributions(MMenu menuModel,
			ArrayList<MMenuContribution> toContribute, boolean menuBar) {
		HashSet<String> existingMenuIds = new HashSet<String>();
		HashSet<String> existingSeparatorNames = new HashSet<String>();
		for (MMenuElement child : menuModel.getChildren()) {
			String elementId = child.getElementId();
			if (child instanceof MMenu && elementId != null) {
				existingMenuIds.add(elementId);
			} else if (child instanceof MMenuSeparator && elementId != null) {
				existingSeparatorNames.add(elementId);
			}
		}

		MenuManager manager = getManager(menuModel);
		boolean done = toContribute.size() == 0;
		while (!done) {
			ArrayList<MMenuContribution> curList = new ArrayList<MMenuContribution>(
					toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (MMenuContribution menuContribution : curList) {
				if (!processAddition(menuModel, manager, menuContribution,
						existingMenuIds, existingSeparatorNames, menuBar)) {
					toContribute.add(menuContribution);
				}
			}

			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = (toContribute.size() == 0)
					|| (toContribute.size() == retryCount);
		}
	}

	/**
	 * @param menuModel
	 * @param manager
	 * @param menuContribution
	 * @return true if the menuContribution was processed
	 */
	private boolean processAddition(MMenu menuModel, final MenuManager manager,
			MMenuContribution menuContribution,
			final HashSet<String> existingMenuIds,
			HashSet<String> existingSeparatorNames, boolean menuBar) {
		int idx = getIndex(menuModel, menuContribution.getPositionInParent());
		if (idx == -1) {
			return false;
		}
		final ContributionRecord record = new ContributionRecord(menuModel,
				menuContribution, manager);
		record.generate();
		for (MMenuElement copy : record.generatedElements) {
			modelContributionToRecord.put(copy, record);
			if (copy instanceof MMenu
					&& existingMenuIds.contains(copy.getElementId())) {
				// skip this, it's already there
				continue;
			} else if (copy instanceof MMenuSeparator
					&& existingSeparatorNames.contains(copy.getElementId())) {
				// skip this, it's already there
				continue;
			}
			menuModel.getChildren().add(idx++, copy);
			if (copy instanceof MMenu && copy.getElementId() != null) {
				existingMenuIds.add(copy.getElementId());
			} else if (copy instanceof MMenuSeparator
					&& copy.getElementId() != null) {
				existingSeparatorNames.add(copy.getElementId());
			}
		}
		if (menuBar) {
			if (menuContribution.getVisibleWhen() != null) {
				final IEclipseContext parentContext = modelService
						.getContainingContext(menuModel);
				parentContext.runAndTrack(new RunAndTrack() {
					@Override
					public boolean changed(IEclipseContext context) {
						record.updateVisibility(parentContext.getActiveLeaf());
						manager.update(true);
						return true;
					}
				});
			}
		}
		return true;
	}

	private static int getIndex(MElementContainer<?> menuModel,
			String positionInParent) {
		String id = null;
		String modifier = null;
		if (positionInParent != null && positionInParent.length() > 0) {
			String[] array = positionInParent.split("="); //$NON-NLS-1$
			modifier = array[0];
			id = array[1];
		}
		if (id == null) {
			return menuModel.getChildren().size();
		}

		int idx = 0;
		int size = menuModel.getChildren().size();
		while (idx < size) {
			if (id.equals(menuModel.getChildren().get(idx).getElementId())) {
				if ("after".equals(modifier)) { //$NON-NLS-1$
					idx++;
				}
				return idx;
			}
			idx++;
		}
		return id.equals("additions") ? menuModel.getChildren().size() : -1; //$NON-NLS-1$
	}

	static class ContributionRecord {
		MMenu menuModel;
		MMenuContribution menuContribution;
		ArrayList<MMenuElement> generatedElements = new ArrayList<MMenuElement>();
		MenuManager manager;

		public ContributionRecord(MMenu menuModel,
				MMenuContribution contribution, MenuManager manager) {
			this.menuModel = menuModel;
			this.menuContribution = contribution;
			this.manager = manager;
		}

		/**
		 * @param context
		 */
		public void updateVisibility(IEclipseContext context) {
			ExpressionContext exprContext = new ExpressionContext(context);
			boolean isVisible = ContributionsAnalyzer.isVisible(
					menuContribution, exprContext);
			for (MMenuElement item : generatedElements) {
				item.setVisible(isVisible);
			}
			manager.markDirty();
		}

		public void generate() {
			for (MMenuElement item : menuContribution.getChildren()) {
				MMenuElement copy = (MMenuElement) EcoreUtil
						.copy((EObject) item);
				generatedElements.add(copy);
			}
		}

		public void dispose() {
			for (MMenuElement copy : generatedElements) {
				menuModel.getChildren().remove(copy);
			}
		}
	}

	void removeMenuContributions(final MMenu menuModel,
			final ArrayList<MMenuElement> menuContributionsToRemove) {
		for (MMenuElement item : menuContributionsToRemove) {
			menuModel.getChildren().remove(item);
		}
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

		// this is in direct violation of good programming
		MenuManager parentManager = getManager((MMenu) ((Object) container));
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
		linkModelToManager(menuModel, menuManager);
		menuManager.setVisible(menuModel.isVisible());
		parentManager.add(menuManager);
		processContributions(menuModel, false);
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
		if (!childME.isToBeRendered()) {
			return;
		}
		if (childME instanceof MHandledMenuItem) {
			MHandledMenuItem itemModel = (MHandledMenuItem) childME;
			processHandledItem(menuManager, itemModel);
		} else if (childME instanceof MDirectMenuItem) {
			MDirectMenuItem itemModel = (MDirectMenuItem) childME;
			processDirectItem(menuManager, itemModel, null);
		} else if (childME instanceof MMenuSeparator) {
			MMenuSeparator sep = (MMenuSeparator) childME;
			processSeparator(menuManager, sep);
		} else if (childME instanceof MMenu) {
			MMenu itemModel = (MMenu) childME;
			processMenu(menuManager, itemModel);
		}
	}

	/**
	 * @param menuManager
	 * @param itemModel
	 */
	private void processSeparator(MenuManager menuManager,
			MMenuSeparator itemModel) {
		AbstractGroupMarker marker = null;
		if (itemModel.isVisible()) {
			marker = new Separator();
			marker.setId(itemModel.getElementId());
		} else {
			if (itemModel.getElementId() != null) {
				marker = new GroupMarker(itemModel.getElementId());
			}
		}
		menuManager.add(marker);
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
		ci.setVisible(itemModel.isVisible());
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
		ci.setVisible(itemModel.isVisible());
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

	public MenuManager getManager(MMenu model) {
		return modelToManager.get(model);
	}

	public MMenu getMenuModel(MenuManager manager) {
		return managerToModel.get(manager);
	}

	public void linkModelToManager(MMenu model, MenuManager manager) {
		modelToManager.put(model, manager);
		managerToModel.put(manager, model);
		manager.addMenuListener(visibilityCalculationListener);
	}

	public void clearModelToManager(MMenu model, MenuManager manager) {
		modelToManager.remove(model);
		managerToModel.remove(manager);
		if (manager != null) {
			manager.removeMenuListener(visibilityCalculationListener);
		}
	}

	public IContributionItem getContribution(MMenuElement model) {
		return modelToContribution.get(model);
	}
}
