/*******************************************************************************
 * Copyright (c) 2009, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 391430, 472654, 460886
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 473779
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 506306
 *     Axel Richard <axel.richard@oebo.fr> - Bug 354538
 *     Christoph Läubrich - issue #1435
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.internal.workbench.RenderedElementUtil;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
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
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.ElementContainer;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.internal.MenuManagerEventHelper;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.osgi.service.event.Event;

/**
 * Create a contribute part.
 */
public class MenuManagerRenderer extends SWTPartRenderer {
	public static final String VISIBILITY_IDENTIFIER = "IIdentifier"; //$NON-NLS-1$
	private static final String NO_LABEL = "UnLabled"; //$NON-NLS-1$
	public static final String GROUP_MARKER = "org.eclipse.jface.action.GroupMarker.GroupMarker(String)"; //$NON-NLS-1$

	private Map<MMenu, MenuManager> modelToManager = new IdentityHashMap<>();
	private Map<MenuManager, MMenu> managerToModel = new IdentityHashMap<>();

	private Map<MMenuElement, IContributionItem> modelToContribution = new IdentityHashMap<>();
	private Map<IContributionItem, MMenuElement> contributionToModel = new IdentityHashMap<>();

	private Map<MMenuElement, ContributionRecord> modelContributionToRecord = new IdentityHashMap<>();
	private Map<MMenuElement, ArrayList<ContributionRecord>> sharedElementToRecord = new IdentityHashMap<>();

	private Collection<IContributionManager> mgrToUpdate = new LinkedHashSet<>();

	@Inject
	private Logger logger;

	@Inject
	private MApplication application;

	@Inject
	@Optional
	private void subscribeSelectionUpdated(@UIEventTopic(UIEvents.Item.TOPIC_SELECTED) Event event) {
		Object property = event.getProperty(UIEvents.EventTags.ELEMENT);
		// Ensure that this event is for a MToolItem
		if (!(property instanceof MMenuItem)) {
			return;
		}

		MMenuItem itemModel = (MMenuItem) property;
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			ici.update();
		}
	}

	@Inject
	@Optional
	private void subscribeUIElementTopicToBeRendered(@UIEventTopic(UIEvents.UIElement.TOPIC_TOBERENDERED) Event event) {
		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);

		if (element instanceof MMenuItem) {
			MMenuItem itemModel = (MMenuItem) element;
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
		} else if (element instanceof MPart) {
			MPart part = (MPart) element;
			boolean tbr = (Boolean) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (!tbr) {
				List<MMenu> menus = part.getMenus();
				for (MMenu menu : menus) {
					if (menu instanceof MPopupMenu)
						unlinkMenu(menu);
				}
			}
		}
	}

	@Inject
	@Optional
	private void subscribeUIElementTopicVisible(@UIEventTopic(UIEvents.UIElement.TOPIC_VISIBLE) Event event) {
		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);

		if (element instanceof MMenu) {
			MMenu menuModel = (MMenu) element;
			MenuManager manager = getManager(menuModel);
			if (manager == null) {
				return;
			}
			boolean visible = menuModel.isVisible();
			manager.setVisible(visible);
			if (manager.getParent() != null) {
				manager.getParent().markDirty();
				scheduleManagerUpdate(manager.getParent());
			}
			if (menuModel.getParent() == null) {
				if (menuModel instanceof MPopupMenu) {
					Object data = menuModel.getTransientData().get(IPresentationEngine.RENDERING_PARENT_KEY);
					if (data instanceof Control) {
						Menu menu = (Menu) menuModel.getWidget();
						if (visible && menuModel.isToBeRendered() && menu != null && !menu.isDisposed()) {
							((Control) data).setMenu(menu);
						}
						if (!visible) {
							((Control) data).setMenu(null);
						}
					}
				}
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
				scheduleManagerUpdate(item.getParent());
			}
		}
	}


	@Inject
	@Optional
	private void subscribeUILabelTopicAll(@UIEventTopic(UIEvents.UILabel.TOPIC_ALL) Event event) {
		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);

		if (element instanceof MMenuItem) {
			handleLabelOfMenuItem(event, element);
		}
		else if (element instanceof MMenu) {
			updateLabelOfMenu(event);
		}
		// nothing to do otherwise
	}

	@Inject
	@Optional
	private void subscribeItemEnabledUpdate(@UIEventTopic(UIEvents.Item.TOPIC_ENABLED) Event event) {
		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);

		if (!(element instanceof MMenuItem)) {
			return;
		}

		MMenuItem itemModel = (MMenuItem) element;
		Object widget = itemModel.getWidget();
		if (widget instanceof MenuItem) {
			boolean enabled = (boolean) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			((MenuItem) widget).setEnabled(enabled);
		}
	}


	private void updateLabelOfMenu(Event event) {
		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
		MMenu model = (MMenu) event.getProperty(UIEvents.EventTags.ELEMENT);
		MenuManager manager = getManager(model);
		if ((manager == null)) {
			return;
		}
		if (UIEvents.UILabel.LABEL.equals(attName) || UIEvents.UILabel.LOCALIZED_LABEL.equals(attName)) {
			manager.setMenuText(getText(model));
			manager.update(IAction.TEXT);
		}
		if (UIEvents.UILabel.ICONURI.equals(attName)) {
			manager.setImageDescriptor(getImageDescriptor(model));
			manager.update(IAction.IMAGE);
		}
	}

	private void handleLabelOfMenuItem(Event event, Object element) {
		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);

		MMenuItem itemModel = (MMenuItem) element;

		IContributionItem ici = getContribution(itemModel);
		if (ici == null) {
			return;
		}

		if (UIEvents.UILabel.LABEL.equals(attName) || UIEvents.UILabel.LOCALIZED_LABEL.equals(attName)) {
			ici.update();
		} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
			ici.update();
		} else if (UIEvents.UILabel.TOOLTIP.equals(attName) || UIEvents.UILabel.LOCALIZED_TOOLTIP.equals(attName)) {
			ici.update();
		}
	}


	private MenuManagerRendererFilter rendererFilter;

	@PostConstruct
	public void init() {

		context.set(MenuManagerRenderer.class, this);
		Display display = context.get(Display.class);
		rendererFilter = ContextInjectionFactory.make(MenuManagerRendererFilter.class, context);
		display.addFilter(SWT.Show, rendererFilter);
		display.addFilter(SWT.Hide, rendererFilter);
		display.addFilter(SWT.Dispose, rendererFilter);
		context.set(MenuManagerRendererFilter.class, rendererFilter);
		MenuManagerEventHelper.getInstance()
				.setShowHelper(ContextInjectionFactory.make(MenuManagerShowProcessor.class, context));
		MenuManagerEventHelper.getInstance()
				.setHideHelper(ContextInjectionFactory.make(MenuManagerHideProcessor.class, context));

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

		ContextInjectionFactory.uninject(MenuManagerEventHelper.getInstance().getShowHelper(), context);
		MenuManagerEventHelper.getInstance().setShowHelper(null);
		ContextInjectionFactory.uninject(MenuManagerEventHelper.getInstance().getHideHelper(), context);
		MenuManagerEventHelper.getInstance().setHideHelper(null);

		context.remove(MenuManagerRendererFilter.class);
		Display display = context.get(Display.class);
		if (display != null && !display.isDisposed() && rendererFilter != null) {
			final MenuManagerRendererFilter filter = rendererFilter;
			display.asyncExec(() -> {
				if (!display.isDisposed()) {
					display.removeFilter(SWT.Show, filter);
					display.removeFilter(SWT.Hide, filter);
					display.removeFilter(SWT.Dispose, filter);
				}
			});
		}
		if (rendererFilter != null) {
			ContextInjectionFactory.uninject(rendererFilter, context);
			rendererFilter = null;
		}
		context.remove(MenuManagerRenderer.class);
		if (Policy.DEBUG_RENDERER) {
			logger.debug("\nMMR:dispose: modelToManager size = {0}, managerToModel size = {1}", //$NON-NLS-1$
					modelToManager.size(), managerToModel.size());
		}
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
			MUIElement container = modelService.getContainer(element);
			if (container instanceof MWindow) {
				menuManager = getManager(menuModel);
				if (menuManager == null) {
					menuManager = new MenuManager(NO_LABEL, menuModel.getElementId());
					linkModelToManager(menuModel, menuManager);
				}
				newMenu = menuManager.createMenuBar((Decorations) parent);
				((Decorations) parent).setMenuBar(newMenu);
				newMenu.setData(menuManager);
				menuBar = true;
			} else {
				menuManager = getManager(menuModel);
				if (menuManager == null) {
					menuManager = new MenuManager(NO_LABEL, menuModel.getElementId());
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
				menuManager = new MenuManager(NO_LABEL, menuModel.getElementId());
				linkModelToManager(menuModel, menuManager);
			}
			newMenu = menuManager.createContextMenu((Control) parent);
			// we can't be sure this is the correct parent.
			// ((Control) parent).setMenu(newMenu);
			if (element instanceof MPopupMenu && element.isVisible()) {
				Object data = getUIContainer(element);
				if (data instanceof Control && parent.equals(data)) {
					((Control) parent).setMenu(newMenu);
				}
			}
			newMenu.setData(menuManager);
		}
		if (menuManager != null && !menuManager.getRemoveAllWhenShown()) {
			processContributions(menuModel, menuModel.getElementId(), menuBar, menuModel instanceof MPopupMenu);
		}
		if (newMenu != null) {
			newMenu.addDisposeListener(e -> {
				cleanUp(menuModel);
				MenuManager manager = getManager(menuModel);
				if (manager != null) {
					manager.markDirty();
				}
			});
		}
		return newMenu;
	}

	public void cleanUp(MMenu menuModel) {
		for (MMenuElement childElement : menuModel.getChildren()) {
			if (childElement instanceof MMenu) {
				cleanUp((MMenu) childElement);
			}
		}
		Collection<ContributionRecord> vals = modelContributionToRecord.values();
		List<ContributionRecord> disposedRecords = new ArrayList<>();
		for (ContributionRecord record : vals.toArray(new ContributionRecord[vals.size()])) {
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

		Iterator<Entry<MMenuElement, ContributionRecord>> iterator = modelContributionToRecord.entrySet().iterator();
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
				ici.dispose();
			}
		}
	}

	public void processContributions(MMenu menuModel, String elementId, boolean isMenuBar, boolean isPopup) {
		if (elementId == null) {
			return;
		}
		final ArrayList<MMenuContribution> toContribute = new ArrayList<>();
		ContributionsAnalyzer.XXXgatherMenuContributions(menuModel, application.getMenuContributions(), elementId,
				toContribute, isPopup);
		generateContributions(menuModel, toContribute, isMenuBar);
		for (MMenuElement element : menuModel.getChildren()) {
			if (element instanceof MMenu) {
				processContributions((MMenu) element, element.getElementId(), false, isPopup);
			}
		}
	}

	private void generateContributions(MMenu menuModel, ArrayList<MMenuContribution> toContribute, boolean menuBar) {
		HashSet<String> existingMenuIds = new HashSet<>();
		HashSet<String> existingSeparatorNames = new HashSet<>();
		for (MMenuElement child : menuModel.getChildren()) {
			String elementId = child.getElementId();
			if (child instanceof MMenu && elementId != null) {
				existingMenuIds.add(elementId);
			} else if (child instanceof MMenuSeparator && elementId != null) {
				existingSeparatorNames.add(elementId);
			}
		}

		MenuManager manager = getManager(menuModel);
		boolean done = toContribute.isEmpty();
		while (!done) {
			ArrayList<MMenuContribution> curList = new ArrayList<>(toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (MMenuContribution menuContribution : curList) {
				if (!processAddition(menuModel, manager, menuContribution, menuBar)) {
					toContribute.add(menuContribution);
				}
			}

			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = (toContribute.isEmpty()) || (toContribute.size() == retryCount);
		}
	}

	/**
	 * @return true if the menuContribution was processed
	 */
	private boolean processAddition(MMenu menuModel, final MenuManager manager, MMenuContribution menuContribution,
			boolean menuBar) {
		final ContributionRecord record = new ContributionRecord(menuModel, menuContribution, this);
		if (!record.mergeIntoModel()) {
			return false;
		}
		if (menuBar || isPartMenu(menuModel)) {
			final IEclipseContext parentContext = getContext(menuModel);
			parentContext.runAndTrack(new RunAndTrack() {
				@Override
				public boolean changed(IEclipseContext context) {
					record.updateVisibility(parentContext.getActiveLeaf());
					scheduleManagerUpdate(manager);
					return true;
				}
			});
		}
		return true;
	}

	private boolean isPartMenu(MMenu menuModel) {
		// don't want popup menus as their visibility does not need to be
		// tracked by a separate RunAndTrack
		return !(menuModel instanceof MPopupMenu) && modelService.getContainer(menuModel) instanceof MPart;
	}

	private static ArrayList<ContributionRecord> DEFAULT = new ArrayList<>();

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
			tmp = new ArrayList<>();
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

	private void removeMenuContributions(final MMenu menuModel,
			final List<MMenuElement> menuContributionsToRemove) {
		for (MMenuElement item : menuContributionsToRemove) {
			menuModel.getChildren().remove(item);

			if (item instanceof MMenu) {
				removeMenuContribution((MMenu) item);
			}
		}
	}

	/**
	 * Ensure when a menu contribution is removed, if it contains nested menus,
	 * their contributions are also removed.
	 */
	private void removeMenuContribution(final MMenu menuModel) {
		clearModelToContribution(menuModel, modelToContribution.get(menuModel));

		if (menuModel.getChildren() != null) {
			for (MMenuElement child : menuModel.getChildren()) {
				if (child instanceof MMenu) {
					removeMenuContribution((MMenu) child);
				} else {
					clearModelToContribution(child, modelToContribution.get(child));
				}
			}
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
			for (MUIElement childME : plist) {
				modelProcessSwitch(parentManager, (MMenuElement) childME);
			}
		}
		scheduleManagerUpdate(parentManager);
	}

	private void addToManager(MenuManager parentManager, MMenuElement model, IContributionItem menuManager) {
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

	private void processMenu(MenuManager parentManager, MMenu menuModel) {
		MenuManager menuManager = getManager(menuModel);
		if (menuManager == null) {
			menuModel.setRenderer(this);
			String menuText = getText(menuModel);
			ImageDescriptor desc = getImageDescriptor(menuModel);
			menuManager = new MenuManager(menuText, desc, menuModel.getElementId());
			linkModelToManager(menuModel, menuManager);
			menuManager.setVisible(menuModel.isVisible());
			addToManager(parentManager, menuModel, menuManager);
		}
		// processContributions(menuModel, false);
		List<MMenuElement> parts = menuModel.getChildren();
		if (parts != null) {
			MMenuElement[] plist = parts.toArray(new MMenuElement[parts.size()]);
			for (MMenuElement childME : plist) {
				modelProcessSwitch(menuManager, childME);
			}
		}
	}

	/* package */ void modelProcessSwitch(MenuManager menuManager, MMenuElement childME) {
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
			processDirectItem(menuManager, itemModel);
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

	private void processRenderedItem(MenuManager parentManager, MMenuItem itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		Object obj = RenderedElementUtil.getContributionManager(itemModel);
		if (obj instanceof IContextFunction) {
			final IEclipseContext lclContext = getContext(itemModel);
			ici = (IContributionItem) ((IContextFunction) obj).compute(lclContext, null);
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

	private void processOpaqueItem(MenuManager parentManager, MMenuItem itemModel) {
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

	private void processSeparator(MenuManager menuManager, MMenuSeparator itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		AbstractGroupMarker marker = null;
		if (itemModel.getTags().contains(GROUP_MARKER) || !itemModel.isVisible()) {
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

	private void processDirectItem(MenuManager parentManager, MDirectMenuItem itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		final IEclipseContext lclContext = getContext(itemModel);
		DirectContributionItem ci = ContextInjectionFactory.make(DirectContributionItem.class, lclContext);
		ci.setModel(itemModel);
		ci.setVisible(itemModel.isVisible());
		addToManager(parentManager, itemModel, ci);
		linkModelToContribution(itemModel, ci);
	}

	private void processDynamicMenuContribution(MenuManager menuManager, MDynamicMenuContribution itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		DynamicContributionContributionItem ci = new DynamicContributionContributionItem(itemModel);
		addToManager(menuManager, itemModel, ci);
		linkModelToContribution(itemModel, ci);
	}

	private void processHandledItem(MenuManager parentManager, MHandledMenuItem itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		final IEclipseContext lclContext = getContext(itemModel);
		HandledContributionItem ci = ContextInjectionFactory.make(HandledContributionItem.class, lclContext);
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
					.get(IResourceUtilities.class);
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
		if (Policy.DEBUG_RENDERER) {
			logger.debug("\nMMR:linkModelToManager: modelToManager size = {0}, managerToModel size = {1}", //$NON-NLS-1$
					modelToManager.size(), managerToModel.size());
		}
	}

	public void clearModelToManager(MMenu model, MenuManager manager) {
		for (MMenuElement element : model.getChildren()) {
			if (element instanceof MMenu) {
				clearModelToManager((MMenu) element, getManager((MMenu) element));
			}
			IContributionItem ici = getContribution(element);
			clearModelToContribution(element, ici);
		}
		MenuManager remove = modelToManager.remove(model);
		if (manager == null) {
			managerToModel.remove(remove);
		} else {
			managerToModel.remove(manager);
		}

		if (Policy.DEBUG_RENDERER) {
			logger.debug("\nMMR:clearModelToManager: modelToManager size = {0}, managerToModel size = {1}", //$NON-NLS-1$
					modelToManager.size(), managerToModel.size());
		}
	}

	public IContributionItem getContribution(MMenuElement model) {
		return modelToContribution.get(model);
	}

	public MMenuElement getMenuElement(IContributionItem item) {
		return contributionToModel.get(item);
	}

	public void linkModelToContribution(MMenuElement model, IContributionItem item) {
		modelToContribution.put(model, item);
		contributionToModel.put(item, model);
		if (Policy.DEBUG_RENDERER) {
			logger.debug(
					"\nMMR:linkModelToContribution: modelToContribution size = {0}, contributionToModel size = {1}", //$NON-NLS-1$
					modelToContribution.size(), contributionToModel.size());
		}
	}

	public void clearModelToContribution(MMenuElement model, IContributionItem item) {
		if (model instanceof MMenu) {
			for (MMenuElement element : ((MMenu) model).getChildren()) {
				IContributionItem ici = getContribution(element);
				clearModelToContribution(element, ici);
			}
		}
		modelToContribution.remove(model);
		contributionToModel.remove(item);
		if (Policy.DEBUG_RENDERER) {
			logger.debug(
					"\nMMR:clearModelToContribution: modelToContribution size = {0}, contributionToModel size = {1}", //$NON-NLS-1$
					modelToContribution.size(), contributionToModel.size());
		}
	}

	public ContributionRecord getContributionRecord(MMenuElement element) {
		return modelContributionToRecord.get(element);
	}

	public void linkElementToContributionRecord(MMenuElement element, ContributionRecord record) {
		modelContributionToRecord.put(element, record);
	}

	/**
	 * Search the records for testing. Look, but don't touch!
	 *
	 * @return the array of active ContributionRecords.
	 */
	public ContributionRecord[] getContributionRecords() {
		HashSet<ContributionRecord> records = new HashSet<>(modelContributionToRecord.values());
		return records.toArray(new ContributionRecord[records.size()]);
	}

	@Override
	public IEclipseContext getContext(MUIElement el) {
		return super.getContext(el);
	}

	public void reconcileManagerToModel(MenuManager menuManager, MMenu menuModel) {
		List<MMenuElement> modelChildren = menuModel.getChildren();

		HashSet<MMenuItem> oldModelItems = new HashSet<>();
		HashSet<MMenu> oldMenus = new HashSet<>();
		HashSet<MMenuSeparator> oldSeps = new HashSet<>();
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
								((MPopupMenu) childModel).setContext(lclContext.createChild(childModel.getElementId()));
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
					MMenuSeparator legacySep = OpaqueElementUtil.createOpaqueMenuSeparator();
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
					MMenuItem legacyItem = OpaqueElementUtil.createOpaqueMenuItem();
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
				IContributionItem ici = (IContributionItem) OpaqueElementUtil.getOpaqueItem(model);
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
				IContributionItem item = (IContributionItem) OpaqueElementUtil.getOpaqueItem(model);
				clearModelToContribution(model, item);
			}
		}
	}

	public static void updateVisibility(MenuManager menuManager, MMenuElement element, ExpressionContext evalContext) {
		boolean current = element.isVisible();
		boolean visible = true;
		boolean evaluated = false;
		if (element.getPersistedState().get(VISIBILITY_IDENTIFIER) != null) {
			evaluated = true;
			String identifier = element.getPersistedState().get(VISIBILITY_IDENTIFIER);
			Object rc = evalContext.eclipseContext.get(identifier);
			if (rc instanceof Boolean) {
				visible = ((Boolean) rc).booleanValue();
			}
		}
		if (visible && element.getVisibleWhen() != null) {
			evaluated = true;
			visible = ContributionsAnalyzer.isVisible(element.getVisibleWhen(), evalContext);
		}
		if (evaluated && visible != current) {
			element.setVisible(visible);
			menuManager.markDirty();
		}
	}

	/**
	 * Clean dynamic menu contributions provided by
	 * {@link MDynamicMenuContribution} application model elements
	 */
	public void removeDynamicMenuContributions(MenuManager menuManager, MMenu menuModel, List<MMenuElement> dump) {
		removeMenuContributions(menuModel, dump);
		for (MMenuElement mMenuElement : dump) {
			IContributionItem ici = getContribution(mMenuElement);
			if (ici == null && mMenuElement instanceof MMenu) {
				MMenu menuElement = (MMenu) mMenuElement;
				ici = getManager(menuElement);
				clearModelToManager(menuElement, (MenuManager) ici);
			} else {
				// Bug 518036: the call below removes too much.
				// clearModelToContribution(menuModel, ici);
				contributionToModel.remove(ici);
			}
			menuManager.remove(ici);
			clearModelToContribution(mMenuElement, ici);
		}
	}

	/**
	 * Remove all dynamic contribution items and their model for the MenuManager
	 * specified.
	 */
	@SuppressWarnings("unchecked")
	public void removeDynamicMenuContributions(MenuManager menuManager, MMenu menuModel) {
		List<MMenuElement> toBeRemoved = new ArrayList<>();
		for (Entry<MMenuElement, IContributionItem> entry : modelToContribution.entrySet()) {
			MMenuElement menuElement = entry.getKey();
			if (menuElement instanceof MDynamicMenuContribution) {
				//
				// Find Dynamic MMenuElements for the MenuManager specified.
				//
				final IContributionItem contributionItem = entry.getValue();

				if (contributionItem instanceof DynamicContributionContributionItem) {
					final DynamicContributionContributionItem dynamicContributionItem = (DynamicContributionContributionItem) contributionItem;

					if ((dynamicContributionItem.getParent() instanceof MenuManager)
							&& dynamicContributionItem.getParent().equals(menuManager)) {
						//
						// Remove the dynamically created menu elements.
						//
						final ArrayList<MMenuElement> childElements = (ArrayList<MMenuElement>) menuElement
								.getTransientData().get(MenuManagerShowProcessor.DYNAMIC_ELEMENT_STORAGE_KEY);

						if (childElements != null) {
							toBeRemoved.addAll(childElements);
						}
					}
				}
			}
		}
		removeDynamicMenuContributions(menuManager, menuModel, toBeRemoved);
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

	private void scheduleManagerUpdate(IContributionManager mgr) {
		// Bug 467000: Avoid repeatedly updating menu managers
		// This workaround is opt-in for 4.5
		boolean workaroundEnabled = Boolean.getBoolean("eclipse.workaround.bug467000"); //$NON-NLS-1$
		if (!workaroundEnabled) {
			mgr.update(false);
			return;
		}
		synchronized (mgrToUpdate) {
			if (this.mgrToUpdate.isEmpty()) {
				Display display = context.get(Display.class);
				if (display != null && !display.isDisposed()) {
					display.timerExec(100, () -> {
						Collection<IContributionManager> toUpdate = new LinkedHashSet<>();
						synchronized (mgrToUpdate) {
							toUpdate.addAll(mgrToUpdate);
							mgrToUpdate.clear();
						}
						for (IContributionManager mgr1 : toUpdate) {
							try {
								mgr1.update(false);
							} catch (RuntimeException e) {
								String message = String.format(
										"ContributionManager '%s' threw an exception while performing update!", mgr1); //$NON-NLS-1$
								ILog.get().error(message, e);
								synchronized (mgrToUpdate) {
									mgrToUpdate.add(mgr1);
								}
							}
						}
					});
				}
				this.mgrToUpdate.add(mgr);
			}
		}
	}
}
