/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marco Descher <marco@descher.at> - Bug 389063, Bug 398865, Bug 398866, Bug 405471
 *     Sopot Cela <sopotcela@gmail.com>
 *     Steven Spungin <steven@spungin.tv> - Bug 437747
 *     Alan Staves <alan.staves@microfocus.com> - Bug 435274
 *     Patrick Naish <patrick.naish@microfocus.com> - Bug 435274
 *     René Brandstetter <Rene.Brandstetter@gmx.net> - Bug 378849
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 378849
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460556
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 391430
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.internal.workbench.RenderedElementUtil;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MDynamicMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.ElementContainer;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.internal.MenuManagerEventHelper;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Create a contribute part.
 */
public class MenuManagerRenderer extends SWTPartRenderer {
	public static final String VISIBILITY_IDENTIFIER = "IIdentifier"; //$NON-NLS-1$
	private static final String NO_LABEL = "UnLabled"; //$NON-NLS-1$
	public static final String GROUP_MARKER = "org.eclipse.jface.action.GroupMarker.GroupMarker(String)"; //$NON-NLS-1$

	private Map<MMenu, MenuManager> modelToManager = new HashMap<MMenu, MenuManager>();
	private Map<MenuManager, MMenu> managerToModel = new HashMap<MenuManager, MMenu>();

	private Map<MMenuElement, IContributionItem> modelToContribution = new HashMap<MMenuElement, IContributionItem>();
	private Map<IContributionItem, MMenuElement> contributionToModel = new HashMap<IContributionItem, MMenuElement>();

	private Map<MMenuElement, ContributionRecord> modelContributionToRecord = new HashMap<MMenuElement, ContributionRecord>();
	private Map<MMenuElement, ArrayList<ContributionRecord>> sharedElementToRecord = new HashMap<MMenuElement, ArrayList<ContributionRecord>>();

	@Inject
	private Logger logger;

	@Inject
	private MApplication application;

	@Inject
	IEventBroker eventBroker;
	private EventHandler itemUpdater = new EventHandler() {
		@Override
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
			if (UIEvents.UILabel.LABEL.equals(attName)
					|| UIEvents.UILabel.LOCALIZED_LABEL.equals(attName)) {
				ici.update();
			} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
				ici.update();
			}
		}
	};

	private EventHandler labelUpdater = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			// Ensure that this event is for a MMenu
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenu))
				return;

			String attName = (String) event
					.getProperty(UIEvents.EventTags.ATTNAME);
			MMenu model = (MMenu) event.getProperty(UIEvents.EventTags.ELEMENT);
			MenuManager manager = getManager(model);
			if ((manager == null))
				return;
			if (UIEvents.UILabel.LABEL.equals(attName)
					|| UIEvents.UILabel.LOCALIZED_LABEL.equals(attName)) {
				manager.setMenuText(getText(model));
				manager.update(IAction.TEXT);
			}
			if (UIEvents.UILabel.ICONURI.equals(attName)) {
				manager.setImageDescriptor(getImageDescriptor(model));
				manager.update(IAction.IMAGE);
			}
		}
	};

	private EventHandler toBeRenderedUpdater = new EventHandler() {
		@Override
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
						IContributionItem ici = getContribution(itemModel);
						clearModelToContribution(itemModel, ici);
						if (ici != null && parent != null) {
							parent.remove(ici);
						}
						if (ici != null) {
							ici.dispose();
						}
					}
				}
			}

			if (element instanceof MPart) {
				MPart part = (MPart) element;
				if (UIEvents.UIElement.TOBERENDERED.equals(attName)) {
					boolean tbr = (Boolean) event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					if (!tbr) {
						List<MMenu> menus = part.getMenus();
						for (MMenu menu : menus) {
							if (menu instanceof MPopupMenu)
								unlinkMenu(menu);
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
					if (manager.getParent() != null) {
						manager.getParent().markDirty();
						manager.getParent().update(false);
					}
				} else if (element instanceof MMenuElement) {
					MMenuElement itemModel = (MMenuElement) element;
					Object obj = getContribution(itemModel);
					if (!(obj instanceof ContributionItem)) {
						return;
					}
					ContributionItem item = (ContributionItem) obj;
					item.setVisible(itemModel.isVisible());
					if (item.getParent() != null) {
						item.getParent().markDirty();
						item.getParent().update(false);
					}
				}
			}
		}
	};

	private EventHandler selectionUpdater = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			// Ensure that this event is for a MToolItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuItem))
				return;

			MMenuItem itemModel = (MMenuItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			IContributionItem ici = getContribution(itemModel);
			if (ici != null) {
				ici.update();
			}
		}
	};

	private EventHandler enabledUpdater = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			// Ensure that this event is for a MMenuItem
			if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuItem))
				return;

			MMenuItem itemModel = (MMenuItem) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			IContributionItem ici = getContribution(itemModel);
			if (ici != null) {
				ici.update();
			}
		}
	};

	private MenuManagerRendererFilter rendererFilter;

	@PostConstruct
	public void init() {
		eventBroker.subscribe(UIEvents.UILabel.TOPIC_ALL, itemUpdater);
		eventBroker.subscribe(UIEvents.UILabel.TOPIC_ALL, labelUpdater);
		eventBroker.subscribe(UIEvents.Item.TOPIC_SELECTED, selectionUpdater);
		eventBroker.subscribe(UIEvents.Item.TOPIC_ENABLED, enabledUpdater);
		eventBroker
				.subscribe(UIEvents.UIElement.TOPIC_ALL, toBeRenderedUpdater);

		context.set(MenuManagerRenderer.class, this);
		Display display = context.get(Display.class);
		rendererFilter = ContextInjectionFactory.make(
				MenuManagerRendererFilter.class, context);
		display.addFilter(SWT.Show, rendererFilter);
		display.addFilter(SWT.Hide, rendererFilter);
		display.addFilter(SWT.Dispose, rendererFilter);
		context.set(MenuManagerRendererFilter.class, rendererFilter);
		MenuManagerEventHelper.getInstance().setShowHelper(
				ContextInjectionFactory.make(MenuManagerShowProcessor.class,
						context));
		MenuManagerEventHelper.getInstance().setHideHelper(
				ContextInjectionFactory.make(MenuManagerHideProcessor.class,
						context));

	}

	@SuppressWarnings("unchecked")
	@Inject
	@Optional
	private void subscribeTopicChildAdded(@UIEventTopic(ElementContainer.TOPIC_CHILDREN) Event event) {
		// Ensure that this event is for a MMenuItem
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenu)) {
			return;
		}
		MMenu menuModel = (MMenu) event.getProperty(UIEvents.EventTags.ELEMENT);
		if (UIEvents.isADD(event)) {
			Object obj = menuModel;
			processContents((MElementContainer<MUIElement>) obj);
		}
	}

	@PreDestroy
	public void contextDisposed() {
		eventBroker.unsubscribe(itemUpdater);
		eventBroker.unsubscribe(labelUpdater);
		eventBroker.unsubscribe(selectionUpdater);
		eventBroker.unsubscribe(enabledUpdater);
		eventBroker.unsubscribe(toBeRenderedUpdater);

		ContextInjectionFactory.uninject(MenuManagerEventHelper.getInstance()
				.getShowHelper(),
				context);
		MenuManagerEventHelper.getInstance().setShowHelper(null);
		ContextInjectionFactory.uninject(
MenuManagerEventHelper.getInstance()
				.getHideHelper(),
				context);
		MenuManagerEventHelper.getInstance().setHideHelper(null);

		context.remove(MenuManagerRendererFilter.class);
		Display display = context.get(Display.class);
		if (display != null && !display.isDisposed() && rendererFilter != null) {
			display.removeFilter(SWT.Show, rendererFilter);
			display.removeFilter(SWT.Hide, rendererFilter);
			display.removeFilter(SWT.Dispose, rendererFilter);
		}
		if (rendererFilter != null) {
			ContextInjectionFactory.uninject(rendererFilter, context);
			rendererFilter = null;
		}
		context.remove(MenuManagerRenderer.class);
	}

	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MMenu))
			return null;

		final MMenu menuModel = (MMenu) element;
		Menu newMenu = null;
		MenuManager menuManager = null;
		boolean menuBar = false;

		if (parent instanceof Decorations) {
			MUIElement container = (MUIElement) ((EObject) element)
					.eContainer();
			if (container instanceof MWindow) {
				menuManager = getManager(menuModel);
				if (menuManager == null) {
					menuManager = new MenuManager(NO_LABEL,
							menuModel.getElementId());
					linkModelToManager(menuModel, menuManager);
				}
				newMenu = menuManager.createMenuBar((Decorations) parent);
				((Decorations) parent).setMenuBar(newMenu);
				newMenu.setData(menuManager);
				menuBar = true;
			} else {
				menuManager = getManager(menuModel);
				if (menuManager == null) {
					menuManager = new MenuManager(NO_LABEL,
							menuModel.getElementId());
					linkModelToManager(menuModel, menuManager);
				}
				newMenu = menuManager.createContextMenu((Control) parent);
				// we can't be sure this is the correct parent.
				// ((Control) parent).setMenu(newMenu);
				newMenu.setData(menuManager);
			}
		} else if (parent instanceof Menu) {
			// Object data = ((Menu) parent).getData();
			logger.debug(new Exception(), "Trying to render a sub menu " //$NON-NLS-1$
					+ menuModel + "\n\t" + parent); //$NON-NLS-1$
			return null;

		} else if (parent instanceof Control) {
			menuManager = getManager(menuModel);
			if (menuManager == null) {
				menuManager = new MenuManager(NO_LABEL,
						menuModel.getElementId());
				linkModelToManager(menuModel, menuManager);
			}
			newMenu = menuManager.createContextMenu((Control) parent);
			// we can't be sure this is the correct parent.
			// ((Control) parent).setMenu(newMenu);
			newMenu.setData(menuManager);
		}
		if (!menuManager.getRemoveAllWhenShown()) {
			processContributions(menuModel, menuModel.getElementId(), menuBar,
					menuModel instanceof MPopupMenu);
		}
		if (newMenu != null) {
			newMenu.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					cleanUp(menuModel);
					MenuManager manager = getManager(menuModel);
					if (manager != null) {
						manager.markDirty();
					}
				}
			});
		}
		return newMenu;
	}

	/**
	 * @param menuModel
	 */
	public void cleanUp(MMenu menuModel) {
		for (MMenuElement childElement : menuModel.getChildren()) {
			if (childElement instanceof MMenu) {
				cleanUp((MMenu) childElement);
			}
		}
		Collection<ContributionRecord> vals = modelContributionToRecord
				.values();
		List<ContributionRecord> disposedRecords = new ArrayList<ContributionRecord>();
		for (ContributionRecord record : vals
				.toArray(new ContributionRecord[vals.size()])) {
			if (record.menuModel == menuModel) {
				record.dispose();
				for (MMenuElement copy : record.getGeneratedElements()) {
					cleanUpCopy(record, copy);
				}
				for (MMenuElement copy : record.getSharedElements()) {
					cleanUpCopy(record, copy);
				}
				record.getGeneratedElements().clear();
				record.getSharedElements().clear();
				disposedRecords.add(record);
			}
		}

		Iterator<Entry<MMenuElement, ContributionRecord>> iterator = modelContributionToRecord
				.entrySet().iterator();
		for (; iterator.hasNext();) {
			Entry<MMenuElement, ContributionRecord> entry = iterator.next();
			ContributionRecord record = entry.getValue();
			if (disposedRecords.contains(record))
				iterator.remove();
		}
	}

	public void cleanUpCopy(ContributionRecord record, MMenuElement copy) {
		modelContributionToRecord.remove(copy);
		if (copy instanceof MMenu) {
			MMenu menuCopy = (MMenu) copy;
			cleanUp(menuCopy);
			MenuManager copyManager = getManager(menuCopy);
			clearModelToManager(menuCopy, copyManager);
			if (copyManager != null) {
				record.getManagerForModel().remove(copyManager);
				copyManager.dispose();
			}
		} else {
			IContributionItem ici = getContribution(copy);
			clearModelToContribution(copy, ici);
			if (ici != null) {
				record.getManagerForModel().remove(ici);
			}
		}
	}

	/**
	 * @param menuModel
	 * @param isMenuBar
	 * @param isPopup
	 */
	public void processContributions(MMenu menuModel, String elementId,
			boolean isMenuBar, boolean isPopup) {
		if (elementId == null) {
			return;
		}
		final ArrayList<MMenuContribution> toContribute = new ArrayList<MMenuContribution>();
		ContributionsAnalyzer.XXXgatherMenuContributions(menuModel,
				application.getMenuContributions(), elementId, toContribute,
				null, isPopup);
		generateContributions(menuModel, toContribute, isMenuBar);
		for (MMenuElement element : menuModel.getChildren()) {
			if (element instanceof MMenu) {
				processContributions((MMenu) element, element.getElementId(),
						false, isPopup);
			}
		}
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
		final ContributionRecord record = new ContributionRecord(menuModel,
				menuContribution, this);
		if (!record.mergeIntoModel()) {
			return false;
		}
		if (menuBar || isPartMenu(menuModel)) {
			final IEclipseContext parentContext = getContext(menuModel);
			parentContext.runAndTrack(new RunAndTrack() {
				@Override
				public boolean changed(IEclipseContext context) {
					record.updateVisibility(parentContext.getActiveLeaf());
					manager.update(false);
					return true;
				}
			});
		}
		return true;
	}

	private boolean isPartMenu(MMenu menuModel) {
		// don't want popup menus as their visibility does not need to be
		// tracked by a separate RunAndTrack
		return !(menuModel instanceof MPopupMenu)
				&& ((EObject) menuModel).eContainer() instanceof MPart;
	}

	private static ArrayList<ContributionRecord> DEFAULT = new ArrayList<ContributionRecord>();

	public ArrayList<ContributionRecord> getList(MMenuElement item) {
		ArrayList<ContributionRecord> tmp = sharedElementToRecord.get(item);
		if (tmp == null) {
			tmp = DEFAULT;
		}
		return tmp;
	}

	public void addRecord(MMenuElement item, ContributionRecord rec) {
		ArrayList<ContributionRecord> tmp = sharedElementToRecord.get(item);
		if (tmp == null) {
			tmp = new ArrayList<ContributionRecord>();
			sharedElementToRecord.put(item, tmp);
		}
		tmp.add(rec);
	}

	public void removeRecord(MMenuElement item, ContributionRecord rec) {
		ArrayList<ContributionRecord> tmp = sharedElementToRecord.get(item);
		if (tmp != null) {
			tmp.remove(rec);
			if (tmp.isEmpty()) {
				sharedElementToRecord.remove(item);
			}
		}
	}

	void removeMenuContributions(final MMenu menuModel,
			final ArrayList<MMenuElement> menuContributionsToRemove) {
		for (MMenuElement item : menuContributionsToRemove) {
			menuModel.getChildren().remove(item);
		}
	}

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

	private void addToManager(MenuManager parentManager, MMenuElement model,
			IContributionItem menuManager) {
		MElementContainer<MUIElement> parent = model.getParent();
		// technically this shouldn't happen
		if (parent == null) {
			parentManager.add(menuManager);
		} else {
			int index = parent.getChildren().indexOf(model);
			// shouldn't be -1, but better safe than sorry
			if (index > parentManager.getSize() || index == -1) {
				parentManager.add(menuManager);
			} else {
				parentManager.insert(index, menuManager);
			}
		}
	}

	/**
	 * @param parentManager
	 * @param menuModel
	 */
	private void processMenu(MenuManager parentManager, MMenu menuModel) {
		MenuManager menuManager = getManager(menuModel);
		if (menuManager == null) {
			menuModel.setRenderer(this);
			String menuText = getText(menuModel);
			ImageDescriptor desc = getImageDescriptor(menuModel);
			menuManager = new MenuManager(menuText, desc,
					menuModel.getElementId());
			linkModelToManager(menuModel, menuManager);
			menuManager.setVisible(menuModel.isVisible());
			addToManager(parentManager, menuModel, menuManager);
		}
		// processContributions(menuModel, false);
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
		if (RenderedElementUtil.isRenderedMenuItem(childME)) {
			MMenuItem itemModel = (MMenuItem) childME;
			processRenderedItem(menuManager, itemModel);
		} else if (OpaqueElementUtil.isOpaqueMenuItem(childME)) {
			MMenuItem itemModel = (MMenuItem) childME;
			processOpaqueItem(menuManager, itemModel);
		} else if (childME instanceof MHandledMenuItem) {
			MHandledMenuItem itemModel = (MHandledMenuItem) childME;
			processHandledItem(menuManager, itemModel);
		} else if (childME instanceof MDirectMenuItem) {
			MDirectMenuItem itemModel = (MDirectMenuItem) childME;
			processDirectItem(menuManager, itemModel, null);
		} else if (childME instanceof MMenuSeparator) {
			MMenuSeparator sep = (MMenuSeparator) childME;
			processSeparator(menuManager, sep);
			// } else if (childME instanceof MOpaqueMenu) {
			// I'm not sure what to do here
			// so I'll just take it out of the running
		} else if (childME instanceof MMenu) {
			MMenu itemModel = (MMenu) childME;
			processMenu(menuManager, itemModel);
		} else if (childME instanceof MDynamicMenuContribution) {
			MDynamicMenuContribution itemModel = (MDynamicMenuContribution) childME;
			processDynamicMenuContribution(menuManager, itemModel);
		}
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 */
	void processRenderedItem(MenuManager parentManager,
 MMenuItem itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		Object obj = RenderedElementUtil.getContributionManager(itemModel);
		if (obj instanceof IContextFunction) {
			final IEclipseContext lclContext = getContext(itemModel);
			ici = (IContributionItem) ((IContextFunction) obj).compute(
					lclContext, null);
			RenderedElementUtil.setContributionManager(itemModel, ici);
		} else if (obj instanceof IContributionItem) {
			ici = (IContributionItem) obj;
		} else {
			// TODO potentially log the state, we've got something we're not
			// happy with
			return;
		}
		ici.setVisible(itemModel.isVisible());
		addToManager(parentManager, itemModel, ici);
		linkModelToContribution(itemModel, ici);
	}

	void processOpaqueItem(MenuManager parentManager, MMenuItem itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		Object obj = OpaqueElementUtil.getOpaqueItem(itemModel);
		if (obj instanceof IContributionItem) {
			ici = (IContributionItem) obj;
		} else {
			return;
		}
		ici.setVisible(itemModel.isVisible());
		addToManager(parentManager, itemModel, ici);
		linkModelToContribution(itemModel, ici);
	}

	/**
	 * @param menuManager
	 * @param itemModel
	 */
	private void processSeparator(MenuManager menuManager,
			MMenuSeparator itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		AbstractGroupMarker marker = null;
		if (itemModel.getTags().contains(GROUP_MARKER)
				|| !itemModel.isVisible()) {
			if (itemModel.getElementId() != null) {
				marker = new GroupMarker(itemModel.getElementId());
			}
		} else {
			marker = new Separator();
			marker.setId(itemModel.getElementId());
		}
		if (marker == null) {
			return;
		}
		addToManager(menuManager, itemModel, marker);
		linkModelToContribution(itemModel, marker);
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 * @param id
	 */
	void processDirectItem(MenuManager parentManager,
			MDirectMenuItem itemModel, String id) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		final IEclipseContext lclContext = getContext(itemModel);
		DirectContributionItem ci = ContextInjectionFactory.make(
				DirectContributionItem.class, lclContext);
		ci.setModel(itemModel);
		ci.setVisible(itemModel.isVisible());
		addToManager(parentManager, itemModel, ci);
		linkModelToContribution(itemModel, ci);
	}

	/**
	 * @param menuManager
	 * @param itemModel
	 */
	private void processDynamicMenuContribution(MenuManager menuManager,
			MDynamicMenuContribution itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		DynamicContributionContributionItem ci = new DynamicContributionContributionItem(
				itemModel);
		addToManager(menuManager, itemModel, ci);
		linkModelToContribution(itemModel, ci);
	}

	/**
	 * @param parentManager
	 * @param itemModel
	 */
	void processHandledItem(MenuManager parentManager,
			MHandledMenuItem itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		final IEclipseContext lclContext = getContext(itemModel);
		HandledContributionItem ci = ContextInjectionFactory.make(
				HandledContributionItem.class, lclContext);
		ci.setModel(itemModel);
		ci.setVisible(itemModel.isVisible());
		addToManager(parentManager, itemModel, ci);
		linkModelToContribution(itemModel, ci);
	}

	private String getText(MMenu menuModel) {
		String text = menuModel.getLocalizedLabel();
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
	}

	public void clearModelToManager(MMenu model, MenuManager manager) {
		for (MMenuElement element : model.getChildren()) {
			IContributionItem ici = getContribution(element);
			clearModelToContribution(element, ici);
		}
		modelToManager.remove(model);
		managerToModel.remove(manager);
	}

	public IContributionItem getContribution(MMenuElement model) {
		return modelToContribution.get(model);
	}

	public MMenuElement getMenuElement(IContributionItem item) {
		return contributionToModel.get(item);
	}

	public void linkModelToContribution(MMenuElement model,
			IContributionItem item) {
		modelToContribution.put(model, item);
		contributionToModel.put(item, model);
	}

	public void clearModelToContribution(MMenuElement model,
			IContributionItem item) {
		modelToContribution.remove(model);
		contributionToModel.remove(item);
	}

	public ContributionRecord getContributionRecord(MMenuElement element) {
		return modelContributionToRecord.get(element);
	}

	public void linkElementToContributionRecord(MMenuElement element,
			ContributionRecord record) {
		modelContributionToRecord.put(element, record);
	}

	/**
	 * Search the records for testing. Look, but don't touch!
	 *
	 * @return the array of active ContributionRecords.
	 */
	public ContributionRecord[] getContributionRecords() {
		HashSet<ContributionRecord> records = new HashSet<ContributionRecord>(
				modelContributionToRecord.values());
		return records.toArray(new ContributionRecord[records.size()]);
	}

	@Override
	public IEclipseContext getContext(MUIElement el) {
		return super.getContext(el);
	}

	/**
	 * @param menuManager
	 * @param menuModel
	 */
	public void reconcileManagerToModel(MenuManager menuManager, MMenu menuModel) {
		List<MMenuElement> modelChildren = menuModel.getChildren();

		HashSet<MMenuItem> oldModelItems = new HashSet<MMenuItem>();
		HashSet<MMenu> oldMenus = new HashSet<MMenu>();
		HashSet<MMenuSeparator> oldSeps = new HashSet<MMenuSeparator>();
		for (MMenuElement itemModel : modelChildren) {
			if (OpaqueElementUtil.isOpaqueMenuSeparator(itemModel)) {
				oldSeps.add((MMenuSeparator) itemModel);
			} else if (OpaqueElementUtil.isOpaqueMenuItem(itemModel)) {
				oldModelItems.add((MMenuItem) itemModel);
			} else if (OpaqueElementUtil.isOpaqueMenu(itemModel)) {
				oldMenus.add((MMenu) itemModel);
			}
		}

		IContributionItem[] items = menuManager.getItems();
		for (int src = 0, dest = 0; src < items.length; src++, dest++) {
			IContributionItem item = items[src];

			if (item instanceof SubContributionItem) {
				// get the wrapped contribution item
				item = ((SubContributionItem) item).getInnerItem();
			}

			if (item instanceof MenuManager) {
				MenuManager childManager = (MenuManager) item;
				MMenu childModel = getMenuModel(childManager);
				if (childModel == null) {
					MMenu legacyModel = OpaqueElementUtil.createOpaqueMenu();
					legacyModel.setElementId(childManager.getId());
					legacyModel.setVisible(childManager.isVisible());
					legacyModel.setLabel(childManager.getMenuText());

					linkModelToManager(legacyModel, childManager);
					OpaqueElementUtil.setOpaqueItem(legacyModel, childManager);
					if (modelChildren.size() > dest) {
						modelChildren.add(dest, legacyModel);
					} else {
						modelChildren.add(legacyModel);
					}
					reconcileManagerToModel(childManager, legacyModel);
				} else {
					if (OpaqueElementUtil.isOpaqueMenu(childModel)) {
						oldMenus.remove(childModel);
					}
					if (modelChildren.size() > dest) {
						if (modelChildren.get(dest) != childModel) {
							modelChildren.remove(childModel);
							modelChildren.add(dest, childModel);
						}
					} else {
						modelChildren.add(childModel);
					}
					if (childModel instanceof MPopupMenu) {
						if (((MPopupMenu) childModel).getContext() == null) {
							IEclipseContext lclContext = getContext(menuModel);
							if (lclContext != null) {
								((MPopupMenu) childModel)
										.setContext(lclContext
												.createChild(childModel
														.getElementId()));
							}
						}
					}

					if (childModel.getChildren().size() != childManager.getSize()) {
						reconcileManagerToModel(childManager, childModel);
					}
				}
			} else if (item.isSeparator() || item.isGroupMarker()) {
				MMenuElement menuElement = getMenuElement(item);
				if (menuElement == null) {
					MMenuSeparator legacySep = OpaqueElementUtil
							.createOpaqueMenuSeparator();
					legacySep.setElementId(item.getId());
					legacySep.setVisible(item.isVisible());
					OpaqueElementUtil.setOpaqueItem(legacySep, item);
					linkModelToContribution(legacySep, item);
					if (modelChildren.size() > dest) {
						modelChildren.add(dest, legacySep);
					} else {
						modelChildren.add(legacySep);
					}
				} else if (OpaqueElementUtil.isOpaqueMenuSeparator(menuElement)) {
					MMenuSeparator legacySep = (MMenuSeparator) menuElement;
					oldSeps.remove(legacySep);
					if (modelChildren.size() > dest) {
						if (modelChildren.get(dest) != legacySep) {
							modelChildren.remove(legacySep);
							modelChildren.add(dest, legacySep);
						}
					} else {
						modelChildren.add(legacySep);
					}
				}
			} else {
				MMenuElement menuElement = getMenuElement(item);
				if (menuElement == null) {
					MMenuItem legacyItem = OpaqueElementUtil
							.createOpaqueMenuItem();
					legacyItem.setElementId(item.getId());
					legacyItem.setVisible(item.isVisible());
					OpaqueElementUtil.setOpaqueItem(legacyItem, item);
					linkModelToContribution(legacyItem, item);
					if (modelChildren.size() > dest) {
						modelChildren.add(dest, legacyItem);
					} else {
						modelChildren.add(legacyItem);
					}
				} else if (OpaqueElementUtil.isOpaqueMenuItem(menuElement)) {
					MMenuItem legacyItem = (MMenuItem) menuElement;
					oldModelItems.remove(legacyItem);
					if (modelChildren.size() > dest) {
						if (modelChildren.get(dest) != legacyItem) {
							modelChildren.remove(legacyItem);
							modelChildren.add(dest, legacyItem);
						}
					} else {
						modelChildren.add(legacyItem);
					}
				}
			}
		}
		if (!oldModelItems.isEmpty()) {
			modelChildren.removeAll(oldModelItems);
			for (MMenuItem model : oldModelItems) {
				IContributionItem ici = (IContributionItem) OpaqueElementUtil
						.getOpaqueItem(model);
				clearModelToContribution(model, ici);
			}
		}
		if (!oldMenus.isEmpty()) {
			modelChildren.removeAll(oldMenus);
			for (MMenu oldMenu : oldMenus) {
				MenuManager oldManager = getManager(oldMenu);
				clearModelToManager(oldMenu, oldManager);
			}
		}
		if (!oldSeps.isEmpty()) {
			modelChildren.removeAll(oldSeps);
			for (MMenuSeparator model : oldSeps) {
				IContributionItem item = (IContributionItem) OpaqueElementUtil
						.getOpaqueItem(model);
				clearModelToContribution(model, item);
			}
		}
	}

	/**
	 * @param menuManager
	 * @param element
	 * @param evalContext
	 */
	public static void updateVisibility(MenuManager menuManager,
			MMenuElement element, ExpressionContext evalContext) {
		boolean current = element.isVisible();
		boolean visible = true;
		boolean evaluated = false;
		if (element.getPersistedState().get(VISIBILITY_IDENTIFIER) != null) {
			evaluated = true;
			String identifier = element.getPersistedState().get(
					VISIBILITY_IDENTIFIER);
			Object rc = evalContext.eclipseContext.get(identifier);
			if (rc instanceof Boolean) {
				visible = ((Boolean) rc).booleanValue();
			}
		}
		if (visible && (element.getVisibleWhen() instanceof MCoreExpression)) {
			evaluated = true;
			visible = ContributionsAnalyzer.isVisible(
					(MCoreExpression) element.getVisibleWhen(), evalContext);
		}
		if (evaluated && visible != current) {
			element.setVisible(visible);
			menuManager.markDirty();
		}
	}

	/**
	 * Clean dynamic menu contributions provided by
	 * {@link MDynamicMenuContribution} application model elements
	 *
	 * @param menuManager
	 * @param menuModel
	 * @param dump
	 */
	public void removeDynamicMenuContributions(MenuManager menuManager,
			MMenu menuModel, ArrayList<MMenuElement> dump) {
		removeMenuContributions(menuModel, dump);
		for (MMenuElement mMenuElement : dump) {
			IContributionItem ici = getContribution(mMenuElement);
			if (ici == null && mMenuElement instanceof MMenu) {
				MMenu menuElement = (MMenu) mMenuElement;
				ici = getManager(menuElement);
				clearModelToManager(menuElement, (MenuManager) ici);
			} else {
				clearModelToContribution(menuModel, ici);
			}
			menuManager.remove(ici);
		}
	}

	private void unlinkMenu(MMenu menu) {

		List<MMenuElement> children = menu.getChildren();
		for (MMenuElement child : children) {
			if (child instanceof MMenu)
				unlinkMenu((MMenu) child);
			else {
				IContributionItem contribution = getContribution(child);
				clearModelToContribution(child, contribution);
			}
		}
		MenuManager mm = getManager(menu);
		clearModelToManager(menu, mm);
	}
}
