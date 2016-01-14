/*******************************************************************************
 * Copyright (c) 2009, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Maxime Porhel <maxime.porhel@obeo.fr> Obeo - Bug 410426
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 426535, 433234, 431868, 472654, 485852
 *     Maxime Porhel <maxime.porhel@obeo.fr> Obeo - Bug 431778
 *     Andrey Loskutov <loskutov@gmx.de> - Bugs 383569, 457198
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 431990
 *     Sopot Cela <scela@redhat.com> - Bug 472761
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 473184
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.ElementContainer;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;

/**
 * Create a contribute part.
 */
public class ToolBarManagerRenderer extends SWTPartRenderer {

	private static final Selector ALL_SELECTOR = new Selector() {

		@Override
		public boolean select(MApplicationElement element) {
			return true;
		}
	};

	/**	 */
	public static final String POST_PROCESSING_FUNCTION = "ToolBarManagerRenderer.postProcess.func"; //$NON-NLS-1$
	/**	 */
	public static final String POST_PROCESSING_DISPOSE = "ToolBarManagerRenderer.postProcess.dispose"; //$NON-NLS-1$
	/**	 */
	public static final String UPDATE_VARS = "ToolBarManagerRenderer.updateVars"; //$NON-NLS-1$
	private static final String DISPOSE_ADDED = "ToolBarManagerRenderer.disposeAdded"; //$NON-NLS-1$

	private Map<MToolBar, ToolBarManager> modelToManager = new HashMap<>();
	private Map<ToolBarManager, MToolBar> managerToModel = new HashMap<>();

	private Map<MToolBarElement, IContributionItem> modelToContribution = new HashMap<>();
	private Map<IContributionItem, MToolBarElement> contributionToModel = new HashMap<>();

	private Map<MToolBarElement, ToolBarContributionRecord> modelContributionToRecord = new HashMap<>();

	private Map<MToolBarElement, ArrayList<ToolBarContributionRecord>> sharedElementToRecord = new HashMap<>();

	private ToolItemUpdater enablementUpdater = new ToolItemUpdater();

	@Inject
	private MApplication application;

	@Inject
	EModelService modelService;

	@Inject
	@Optional
	private void subscribeTopicUpdateItems(@UIEventTopic(UIEvents.UILabel.TOPIC_ALL) Event event) {
		// Ensure that this event is for a MToolBarElement
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolBarElement)) {
			return;
		}

		MToolBarElement itemModel = (MToolBarElement) event.getProperty(UIEvents.EventTags.ELEMENT);

		IContributionItem ici = getContribution(itemModel);
		if (ici == null) {
			return;
		}

		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
		if (UIEvents.UILabel.LABEL.equals(attName) || UIEvents.UILabel.LOCALIZED_LABEL.equals(attName)) {
			ici.update();
		} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
			ici.update();
		} else if (UIEvents.UILabel.TOOLTIP.equals(attName) || UIEvents.UILabel.LOCALIZED_TOOLTIP.equals(attName)) {
			ici.update();
		}
	}

	@Inject
	@Optional
	private void subscribeTopicUpdateToBeRendered(@UIEventTopic(UIEvents.UIElement.TOPIC_ALL) Event event) {
		// Ensure that this event is for a MMenuItem
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolBarElement)) {
			return;
		}

		MToolBarElement itemModel = (MToolBarElement) event.getProperty(UIEvents.EventTags.ELEMENT);
		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
		if (UIEvents.UIElement.TOBERENDERED.equals(attName)) {
			Object obj = itemModel.getParent();
			if (!(obj instanceof MToolBar)) {
				return;
			}
			ToolBarManager parent = getManager((MToolBar) obj);
			if (itemModel.isToBeRendered()) {
				if (parent != null) {
					modelProcessSwitch(parent, itemModel);
					parent.update(true);
					ToolBar toolbar = parent.getControl();
					if (toolbar != null && !toolbar.isDisposed()) {
						toolbar.requestLayout();
					}
				}
			} else {
				IContributionItem ici = modelToContribution.remove(itemModel);
				if (ici != null && parent != null) {
					parent.remove(ici);
				}
				if (ici != null) {
					ici.dispose();
				}
			}
		} else if (UIEvents.UIElement.VISIBLE.equals(attName)) {
			IContributionItem ici = getContribution(itemModel);
			if (ici == null) {
				return;
			}

			ToolBarManager parent = null;
			if (ici instanceof MenuManager) {
				parent = (ToolBarManager) ((MenuManager) ici).getParent();
			} else if (ici instanceof ContributionItem) {
				parent = (ToolBarManager) ((ContributionItem) ici).getParent();
			}

			if (parent == null) {
				ici.setVisible(itemModel.isVisible());
				return;
			}

			IContributionManagerOverrides ov = parent.getOverrides();
			// partial fix for bug 383569: only change state if there are no
			// extra override mechanics controlling element visibility
			if (ov == null) {
				ici.setVisible(itemModel.isVisible());
			} else {
				Boolean visible = ov.getVisible(ici);
				if (visible == null) {
					// same as above: only change state if there are no extra
					// override mechanics controlling element visibility
					ici.setVisible(itemModel.isVisible());
				}
			}

			parent.markDirty();
			parent.update(true);
			ToolBar toolbar = parent.getControl();
			if (toolbar != null && !toolbar.isDisposed()) {
				toolbar.requestLayout();
			}
		}
	}

	@Inject
	@Optional
	private void subscribeTopicUpdateSelection(@UIEventTopic(UIEvents.Item.TOPIC_SELECTED) Event event) {
		// Ensure that this event is for a MToolBarElement
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolBarElement)) {
			return;
		}

		MToolBarElement itemModel = (MToolBarElement) event.getProperty(UIEvents.EventTags.ELEMENT);
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			ici.update();
		}
	}

	@Inject
	@Optional
	private void subscribeTopicUpdateEnablement(@UIEventTopic(UIEvents.Item.TOPIC_ENABLED) Event event) {
		// Ensure that this event is for a MMenuItem
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolBarElement)) {
			return;
		}

		MToolBarElement itemModel = (MToolBarElement) event.getProperty(UIEvents.EventTags.ELEMENT);
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			ici.update();
		}
	}

	@SuppressWarnings("unchecked")
	@Inject
	@Optional
	private void subscribeTopicChildAdded(@UIEventTopic(ElementContainer.TOPIC_CHILDREN) Event event) {
		// Ensure that this event is for a MMenuItem
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MToolBar)) {
			return;
		}
		MToolBar toolbarModel = (MToolBar) event.getProperty(UIEvents.EventTags.ELEMENT);
		if (UIEvents.isADD(event)) {
			Object obj = toolbarModel;
			processContents((MElementContainer<MUIElement>) obj);
		}
	}

	private HashSet<String> updateVariables = new HashSet<>();

	@Inject
	@Optional
	private void subscribeTopicDirtyChanged(@UIEventTopic(UIEvents.Dirtyable.TOPIC_DIRTY) Event eventData) {
		getUpdater().updateContributionItems(ALL_SELECTOR);
	}

	@Inject
	@Optional
	private void subscribeTopicUpdateToolbarEnablement(
			@UIEventTopic(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC) Event eventData) {
		final Object v = eventData != null ? eventData.getProperty(IEventBroker.DATA) : UIEvents.ALL_ELEMENT_ID;
		Selector s;
		if (v instanceof Selector) {
			s = (Selector) v;
		} else {
			if (v == null || UIEvents.ALL_ELEMENT_ID.equals(v)) {
				s = ALL_SELECTOR;
			} else {
				s = new Selector() {
					@Override
					public boolean select(MApplicationElement element) {
						return v.equals(element.getElementId());
					}
				};
			}
		}

		getUpdater().updateContributionItems(s);
	}

	@Inject
	@Optional
	private void subscribeTopicTagsChanged(@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {

		Object changedObj = event.getProperty(EventTags.ELEMENT);

		if (!(changedObj instanceof MToolBar)) {
			return;
		}

		final MUIElement changedElement = (MUIElement) changedObj;

		if (UIEvents.isADD(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE, IPresentationEngine.HIDDEN_EXPLICITLY)) {
				changedElement.setVisible(false);
				changedElement.setToBeRendered(false);
			}
		} else if (UIEvents.isREMOVE(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE, IPresentationEngine.HIDDEN_EXPLICITLY)) {
				changedElement.setVisible(true);
				changedElement.setToBeRendered(true);
			}
		}
	}

	@Inject
	@Optional
	private void subscribeTopicAppStartup(@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {
		List<MToolBar> toolBars = modelService.findElements(application, null, MToolBar.class, null);
		for (MToolBar mToolBar : toolBars) {
			if (mToolBar.getTags().contains(IPresentationEngine.HIDDEN_EXPLICITLY)) {
				mToolBar.setVisible(false);
				mToolBar.setToBeRendered(false);
			}
		}
	}

	/**
	 *
	 */
	@PostConstruct
	public void init() {
		context.set(ToolBarManagerRenderer.class, this);

		String[] vars = {
				"org.eclipse.ui.internal.services.EvaluationService.evaluate", //$NON-NLS-1$
				IServiceConstants.ACTIVE_CONTEXTS,
				IServiceConstants.ACTIVE_PART,
				IServiceConstants.ACTIVE_SELECTION,
				IServiceConstants.ACTIVE_SHELL };
		updateVariables.addAll(Arrays.asList(vars));
		context.set(UPDATE_VARS, updateVariables);
		RunAndTrack enablementUpdater = new RunAndTrack() {

			@Override
			public boolean changed(IEclipseContext context) {
				for (String var : updateVariables) {
					context.get(var);
				}
				getUpdater().updateContributionItems(ALL_SELECTOR);
				return true;
			}
		};
		context.runAndTrack(enablementUpdater);
	}

	@Override
	public Object createWidget(final MUIElement element, Object parent) {
		if (!(element instanceof MToolBar) || !(parent instanceof Composite)) {
			return null;
		}

		final MToolBar toolbarModel = (MToolBar) element;
		ToolBar newTB = createToolbar(toolbarModel, (Composite) parent);
		bindWidget(element, newTB);
		processContribution(toolbarModel, toolbarModel.getElementId());

		Control renderedCtrl = newTB;
		MUIElement parentElement = element.getParent();
		if (parentElement instanceof MTrimBar) {
			if (!element.getTags().contains(IPresentationEngine.NO_MOVE)) {
				element.getTags().add(IPresentationEngine.DRAGGABLE);
			}

			setCSSInfo(element, newTB);

			boolean vertical = false;
			MTrimBar bar = (MTrimBar) parentElement;
			vertical = bar.getSide() == SideValue.LEFT || bar.getSide() == SideValue.RIGHT;
			IEclipseContext parentContext = getContextForParent(element);
			CSSRenderingUtils cssUtils = parentContext.get(CSSRenderingUtils.class);
			if (cssUtils != null) {
				MUIElement modelElement = (MUIElement) newTB.getData(AbstractPartRenderer.OWNING_ME);
				boolean draggable = ((modelElement != null) && (modelElement.getTags().contains(IPresentationEngine.DRAGGABLE)));
				renderedCtrl = cssUtils.frameMeIfPossible(newTB, null, vertical, draggable);
			}
		}

		return renderedCtrl;
	}

	/**
	 * @param toolbarModel
	 * @param elementId
	 */
	public void processContribution(MToolBar toolbarModel, String elementId) {

		ToolBarManager manager = getManager(toolbarModel);
		if (manager != null && manager.getControl() != null) {
			addCleanupDisposeListener(toolbarModel, manager.getControl());
		}

		final ArrayList<MToolBarContribution> toContribute = new ArrayList<>();
		ContributionsAnalyzer.XXXgatherToolBarContributions(toolbarModel,
				application.getToolBarContributions(), elementId, toContribute);
		generateContributions(toolbarModel, toContribute);
	}

	private void addCleanupDisposeListener(final MToolBar toolbarModel, ToolBar control) {

		final Map<String, Object> transientData = toolbarModel.getTransientData();
		if (!transientData.containsKey(DISPOSE_ADDED)) {
			transientData.put(DISPOSE_ADDED, Boolean.TRUE);
			control.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					cleanUp(toolbarModel);
					Object dispose = transientData.get(POST_PROCESSING_DISPOSE);
					if (dispose instanceof Runnable) {
						((Runnable) dispose).run();
					}
					transientData.remove(POST_PROCESSING_DISPOSE);
					transientData.remove(DISPOSE_ADDED);
				}
			});
		}

	}

	private void generateContributions(MToolBar toolbarModel, List<MToolBarContribution> toContribute) {

		ToolBarManager manager = getManager(toolbarModel);
		boolean done = toContribute.size() == 0;
		while (!done) {
			ArrayList<MToolBarContribution> curList = new ArrayList<>(toContribute);
			int retryCount = toContribute.size();
			toContribute.clear();

			for (final MToolBarContribution contribution : curList) {
				if (!processAddition(toolbarModel, manager, contribution)) {
					toContribute.add(contribution);
				}
			}
			// We're done if the retryList is now empty (everything done) or
			// if the list hasn't changed at all (no hope)
			done = toContribute.size() == 0 || toContribute.size() == retryCount;
		}
	}

	/**
	 * @return <code>true</code> if the contribution was successfully processed
	 */
	private boolean processAddition(final MToolBar toolbarModel, final ToolBarManager manager,
			MToolBarContribution contribution) {
		final ToolBarContributionRecord record = new ToolBarContributionRecord(toolbarModel, contribution, this);
		if (!record.mergeIntoModel()) {
			return false;
		}
		if (record.anyVisibleWhen()) {
			ExpressionInfo info = new ExpressionInfo();
			record.collectInfo(info);
			updateVariables.addAll(Arrays.asList(info.getAccessedVariableNames()));
			final IEclipseContext parentContext = getContext(toolbarModel);
			parentContext.runAndTrack(new RunAndTrack() {
				@Override
				public boolean changed(IEclipseContext context) {
					if (getManager(toolbarModel) == null) {
						// tool bar no longer being managed, ignore it
						return false;
					}

					record.updateVisibility(parentContext.getActiveLeaf());
					runExternalCode(new Runnable() {

						@Override
						public void run() {
							manager.update(false);
							getUpdater().updateContributionItems(ALL_SELECTOR);
						}
					});
					return true;
				}
			});
		}

		return true;
	}

	private ToolBar createToolbar(final MUIElement element, Composite parent) {
		int orientation = getOrientation(element);
		int style = orientation | SWT.WRAP | SWT.FLAT | SWT.RIGHT;
		ToolBarManager manager = getManager((MToolBar) element);
		if (manager == null) {
			manager = new ToolBarManager(style);
			IContributionManagerOverrides overrides = null;
			MApplicationElement parentElement = element.getParent();
			if (parentElement == null) {
				parentElement = (MApplicationElement) ((EObject) element).eContainer();
			}

			if (parentElement != null) {
				overrides = (IContributionManagerOverrides) parentElement.getTransientData().get(
						IContributionManagerOverrides.class.getName());
			}

			manager.setOverrides(overrides);
			linkModelToManager((MToolBar) element, manager);
		} else {
			ToolBar toolBar = manager.getControl();
			if (toolBar != null && !toolBar.isDisposed() && (toolBar.getStyle() & orientation) == 0) {
				toolBar.dispose();
			}
			manager.setStyle(style);
		}
		ToolBar btoolbar = manager.createControl(parent);
		btoolbar.setData(manager);
		btoolbar.setData(AbstractPartRenderer.OWNING_ME, element);
		btoolbar.requestLayout();
		return btoolbar;
	}

	protected void cleanUp(MToolBar toolbarModel) {
		Collection<ToolBarContributionRecord> vals = modelContributionToRecord.values();
		for (ToolBarContributionRecord record : vals.toArray(new ToolBarContributionRecord[vals.size()])) {
			if (record.toolbarModel == toolbarModel) {
				record.dispose();
				for (MToolBarElement copy : record.generatedElements) {
					cleanUpCopy(record, copy);
				}
				for (MToolBarElement copy : record.sharedElements) {
					cleanUpCopy(record, copy);
				}
				record.generatedElements.clear();
				record.sharedElements.clear();
			}
		}
	}

	/**
	 * @param record
	 * @param copy
	 */
	public void cleanUpCopy(ToolBarContributionRecord record, MToolBarElement copy) {
		modelContributionToRecord.remove(copy);
		IContributionItem ici = getContribution(copy);
		clearModelToContribution(copy, ici);
		if (ici != null) {
			record.getManagerForModel().remove(ici);
		}
	}

	int getOrientation(final MUIElement element) {
		MUIElement theParent = element.getParent();
		if (theParent instanceof MTrimBar) {
			MTrimBar trimContainer = (MTrimBar) theParent;
			SideValue side = trimContainer.getSide();
			if (side.getValue() == SideValue.LEFT_VALUE || side.getValue() == SideValue.RIGHT_VALUE) {
				return SWT.VERTICAL;
			}
		}
		return SWT.HORIZONTAL;
	}

	@Override
	public void processContents(MElementContainer<MUIElement> container) {
		// I can either simply stop processing, or we can walk the model
		// ourselves like the "old" days
		// EMF gives us null lists if empty
		if (container == null) {
			return;
		}

		Object obj = container;
		ToolBarManager parentManager = getManager((MToolBar) obj);
		if (parentManager == null) {
			return;
		}
		// Process any contents of the newly created ME
		List<MUIElement> parts = container.getChildren();
		if (parts != null) {
			MUIElement[] plist = parts.toArray(new MUIElement[parts.size()]);
			for (int i = 0; i < plist.length; i++) {
				MUIElement childME = plist[i];
				modelProcessSwitch(parentManager, (MToolBarElement) childME);
			}
		}
		parentManager.update(true);

		ToolBar toolbar = getToolbarFrom(container.getWidget());
		if (toolbar != null) {
			toolbar.requestLayout();
		}
	}

	private ToolBar getToolbarFrom(Object widget) {
		if (widget instanceof ToolBar) {
			return (ToolBar) widget;
		}
		if (widget instanceof Composite) {
			Composite intermediate = (Composite) widget;
			if (!intermediate.isDisposed()) {
				Control[] children = intermediate.getChildren();
				for (Control control : children) {
					if (control.getData() instanceof ToolBarManager) {
						return (ToolBar) control;
					}
				}
			}
		}
		return null;
	}

	boolean hasOnlySeparators(ToolBar toolbar) {
		ToolItem[] children = toolbar.getItems();
		for (ToolItem toolItem : children) {
			if ((toolItem.getStyle() & SWT.SEPARATOR) == 0) {
				return false;
			} else if (toolItem.getControl() != null
					&& toolItem.getControl().getData(OWNING_ME) instanceof MToolControl) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void hideChild(MElementContainer<MUIElement> parentElement, MUIElement child) {
		super.hideChild(parentElement, child);
		// only handle the disposal of this element if it was actually rendered
		// by the engine
		if (child.getRenderer() != null) {
			// Since there's no place to 'store' a child that's not in a menu
			// we'll blow it away and re-create on an add
			Widget widget = (Widget) child.getWidget();
			if (widget != null && !widget.isDisposed()) {
				widget.dispose();
			}
			ToolBar toolbar = (ToolBar) getUIContainer(child);
			if (toolbar != null && !toolbar.isDisposed()) {
				toolbar.requestLayout();
			}
		}
	}

	@Override
	public void childRendered(MElementContainer<MUIElement> parentElement, MUIElement element) {
		super.childRendered(parentElement, element);
		processContents(parentElement);
		ToolBar toolbar = (ToolBar) getUIContainer(element);
		if (toolbar != null && !toolbar.isDisposed()) {
			toolbar.requestLayout();
		}
	}

	@Override
	public Object getUIContainer(MUIElement childElement) {
		Composite intermediate = (Composite) super.getUIContainer(childElement);
		if (intermediate == null || intermediate.isDisposed()) {
			return null;
		}
		if (intermediate instanceof ToolBar) {
			return intermediate;
		}
		ToolBar toolbar = findToolbar(intermediate);
		if (toolbar == null) {
			toolbar = createToolbar(childElement.getParent(), intermediate);
		}
		return toolbar;
	}

	private ToolBar findToolbar(Composite intermediate) {
		for (Control child : intermediate.getChildren()) {
			if (child.getData() instanceof ToolBarManager) {
				return (ToolBar) child;
			}
		}
		return null;
	}

	private void modelProcessSwitch(ToolBarManager parentManager,
			MToolBarElement childME) {
		if (OpaqueElementUtil.isOpaqueToolItem(childME)) {
			MToolItem itemModel = (MToolItem) childME;
			processOpaqueItem(parentManager, itemModel);
		} else if (childME instanceof MHandledToolItem) {
			MHandledToolItem itemModel = (MHandledToolItem) childME;
			processHandledItem(parentManager, itemModel);
		} else if (childME instanceof MDirectToolItem) {
			MDirectToolItem itemModel = (MDirectToolItem) childME;
			processDirectItem(parentManager, itemModel);
		} else if (childME instanceof MToolBarSeparator) {
			MToolBarSeparator itemModel = (MToolBarSeparator) childME;
			processSeparator(parentManager, itemModel);
		} else if (childME instanceof MToolControl) {
			MToolControl itemModel = (MToolControl) childME;
			processToolControl(parentManager, itemModel);
		}
	}

	private void processSeparator(ToolBarManager parentManager, MToolBarSeparator itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		AbstractGroupMarker marker = null;
		if (itemModel.isVisible() && !itemModel.getTags().contains(MenuManagerRenderer.GROUP_MARKER)) {
			marker = new Separator();
			marker.setId(itemModel.getElementId());
		} else {
			if (itemModel.getElementId() != null) {
				marker = new GroupMarker(itemModel.getElementId());
			}
		}
		if (marker != null) {
			addToManager(parentManager, itemModel, marker);
			linkModelToContribution(itemModel, marker);
		}
	}

	private void processToolControl(ToolBarManager parentManager, MToolControl itemModel) {
		IContributionItem ici = getContribution(itemModel);
		if (ici != null) {
			return;
		}
		itemModel.setRenderer(this);
		final IEclipseContext lclContext = getContext(itemModel);
		ToolControlContribution ci = ContextInjectionFactory.make(ToolControlContribution.class, lclContext);
		ci.setModel(itemModel);
		ci.setVisible(itemModel.isVisible());
		addToManager(parentManager, itemModel, ci);
		linkModelToContribution(itemModel, ci);
	}

	private void processDirectItem(ToolBarManager parentManager, MDirectToolItem itemModel) {
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

	private void processHandledItem(ToolBarManager parentManager, MHandledToolItem itemModel) {
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

	void processOpaqueItem(ToolBarManager parentManager, MToolItem itemModel) {
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

	private void addToManager(ToolBarManager parentManager,
			MToolBarElement model, IContributionItem ci) {
		MElementContainer<MUIElement> parent = model.getParent();
		// technically this shouldn't happen
		if (parent == null) {
			parentManager.add(ci);
		} else {
			int index = parent.getChildren().indexOf(model);
			// shouldn't be -1, but better safe than sorry
			if (index > parentManager.getSize() || index == -1) {
				parentManager.add(ci);
			} else {
				parentManager.insert(index, ci);
			}
		}
	}

	/**
	 * @param model
	 * @return mapped manager, if any
	 */
	public ToolBarManager getManager(MToolBar model) {
		return modelToManager.get(model);
	}

	/**
	 * @param manager
	 * @return mapped model, if any
	 */
	public MToolBar getToolBarModel(ToolBarManager manager) {
		return managerToModel.get(manager);
	}

	/**
	 * @param model
	 * @param manager
	 */
	public void linkModelToManager(MToolBar model, ToolBarManager manager) {
		modelToManager.put(model, manager);
		managerToModel.put(manager, model);
	}

	/**
	 * @param model
	 * @param manager
	 */
	public void clearModelToManager(MToolBar model, ToolBarManager manager) {
		modelToManager.remove(model);
		managerToModel.remove(manager);
	}

	/**
	 * @param element
	 * @return mapped contribution, if any
	 */
	public IContributionItem getContribution(MToolBarElement element) {
		return modelToContribution.get(element);
	}

	/**
	 * @param item
	 * @return mapped toolbar element, if any
	 */
	public MToolBarElement getToolElement(IContributionItem item) {
		return contributionToModel.get(item);
	}

	/**
	 * @param model
	 * @param item
	 */
	public void linkModelToContribution(MToolBarElement model, IContributionItem item) {
		modelToContribution.put(model, item);
		contributionToModel.put(item, model);
	}

	/**
	 * @param model
	 * @param item
	 */
	public void clearModelToContribution(MToolBarElement model, IContributionItem item) {
		modelToContribution.remove(model);
		contributionToModel.remove(item);
	}

	/**
	 * @param item
	 * @return non null records list
	 */
	public ArrayList<ToolBarContributionRecord> getList(MToolBarElement item) {
		ArrayList<ToolBarContributionRecord> tmp = sharedElementToRecord.get(item);
		if (tmp == null) {
			tmp = new ArrayList<>();
			sharedElementToRecord.put(item, tmp);
		}
		return tmp;
	}

	/**
	 * @param element
	 * @param record
	 */
	public void linkElementToContributionRecord(MToolBarElement element, ToolBarContributionRecord record) {
		modelContributionToRecord.put(element, record);
	}

	/**
	 * @param element
	 * @return mapped record, if any
	 */
	public ToolBarContributionRecord getContributionRecord(MToolBarElement element) {
		return modelContributionToRecord.get(element);
	}

	/**
	 * @param menuManager
	 * @param toolBar
	 */
	public void reconcileManagerToModel(IToolBarManager menuManager, MToolBar toolBar) {
		List<MToolBarElement> modelChildren = toolBar.getChildren();
		HashSet<MToolItem> oldModelItems = new HashSet<>();
		for (MToolBarElement itemModel : modelChildren) {
			if (OpaqueElementUtil.isOpaqueToolItem(itemModel)) {
				oldModelItems.add((MToolItem) itemModel);
			}
		}

		IContributionItem[] items = menuManager.getItems();
		for (int src = 0, dest = 0; src < items.length; src++, dest++) {
			IContributionItem item = items[src];
			MToolBarElement element = getToolElement(item);
			if (element == null) {
				MToolItem legacyItem = OpaqueElementUtil.createOpaqueToolItem();
				legacyItem.setElementId(item.getId());
				legacyItem.setVisible(item.isVisible());
				OpaqueElementUtil.setOpaqueItem(legacyItem, item);
				linkModelToContribution(legacyItem, item);
				modelChildren.add(dest, legacyItem);
			} else if (OpaqueElementUtil.isOpaqueToolItem(element)) {
				MToolItem legacyItem = (MToolItem) element;
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

		if (!oldModelItems.isEmpty()) {
			modelChildren.removeAll(oldModelItems);
			for (MToolItem model : oldModelItems) {
				Object obj = OpaqueElementUtil.getOpaqueItem(model);
				clearModelToContribution(model, (IContributionItem) obj);
			}
		}
	}

	@Override
	public void postProcess(MUIElement element) {
		if (element instanceof MToolBar) {
			MToolBar toolbarModel = (MToolBar) element;
			if (toolbarModel.getTransientData().containsKey(POST_PROCESSING_FUNCTION)) {
				Object obj = toolbarModel.getTransientData().get(POST_PROCESSING_FUNCTION);
				if (obj instanceof IContextFunction) {
					IContextFunction func = (IContextFunction) obj;
					final IEclipseContext ctx = getContext(toolbarModel);
					toolbarModel.getTransientData().put(POST_PROCESSING_DISPOSE, func.compute(ctx, null));
				}
			}
		}
	}

	@Override
	public IEclipseContext getContext(MUIElement el) {
		return super.getContext(el);
	}

	ToolItemUpdater getUpdater() {
		return enablementUpdater;
	}

}
