/*******************************************************************************
 * Copyright (c) 2010, 2019 IBM Corporation and others.
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
 *     Sopot Cela <sopotcela@gmail.com> - Bug 391961
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440810, 485840, 474320, 497634
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 380233
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 485829
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.perspectiveswitcher;

import static org.eclipse.swt.events.MenuListener.menuHiddenAdapter;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.service.event.Event;

/**
 * Class to handle perspective switching in the UI.
 */
public class PerspectiveSwitcher {

	/**
	 * The ID of the perspective switcher
	 */
	public static final String PERSPECTIVE_SWITCHER_ID = "org.eclipse.e4.ui.PerspectiveSwitcher"; //$NON-NLS-1$
	public static final String NO_MENU = "NoMenu"; //$NON-NLS-1$

	/**
	 * The event {@link IEventBroker}.
	 */
	@Inject
	protected IEventBroker eventBroker;

	@Inject
	EModelService modelService;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private ECommandService commandService;

	@Inject
	private MWindow window;

	@Inject
	private Logger logger;

	private MToolControl perspSwitcherToolControl;
	private ToolBar perspSwitcherToolbar;

	private Composite comp;
	private Image perspectiveImage;
	Control toolParent;
	IPropertyChangeListener propertyChangeListener;

	@Inject
	@Optional
	void handleChildrenEvent(@UIEventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) Event event) {

		Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);

		if (!(changedObj instanceof MPerspectiveStack) || ignoreEvent(changedObj)) {
			return;
		}

		if (UIEvents.isADD(event)) {
			for (Object o : UIEvents.asIterable(event, UIEvents.EventTags.NEW_VALUE)) {
				MPerspective added = (MPerspective) o;
				// Adding invisible elements is a NO-OP
				if (!added.isToBeRendered()) {
					continue;
				}

				addPerspectiveItem(added);
			}
		} else if (UIEvents.isREMOVE(event)) {
			for (Object o : UIEvents.asIterable(event, UIEvents.EventTags.OLD_VALUE)) {
				MPerspective removed = (MPerspective) o;
				// Removing invisible elements is a NO-OP
				if (!removed.isToBeRendered()) {
					continue;
				}

				removePerspectiveItem(removed);
			}
		}
	}

	@Inject
	@Optional
	void handleToBeRenderedEvent(@UIEventTopic(UIEvents.UIElement.TOPIC_TOBERENDERED) Event event) {

		Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(changedObj instanceof MPerspective) || (ignoreEvent(changedObj))) {
			return;
		}

		MPerspective persp = (MPerspective) event.getProperty(UIEvents.EventTags.ELEMENT);

		if (!persp.getParent().isToBeRendered()) {
			return;
		}

		if (persp.isToBeRendered()) {
			addPerspectiveItem(persp);
		} else {
			removePerspectiveItem(persp);
		}

	}

	@Inject
	@Optional
	void handleLabelEvent(@UIEventTopic(UIEvents.UILabel.TOPIC_ALL) Event event) {

		Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(changedObj instanceof MPerspective) || (ignoreEvent(changedObj))) {
			return;
		}

		MPerspective perspective = (MPerspective) changedObj;

		if (!perspective.isToBeRendered())
		{
			return;
		}

		for (ToolItem ti : perspSwitcherToolbar.getItems()) {
			if (ti.getData() == perspective) {
				String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
				Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
				updateToolItem(ti, attName, newValue);
			}
		}

		// update the size
		fixSize();
	}

	@Inject
	@Optional
	void handleSelectionEvent(@UIEventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {

		Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(changedObj instanceof MPerspectiveStack) || (ignoreEvent(changedObj))) {
			return;
		}

		MPerspectiveStack perspStack = (MPerspectiveStack) changedObj;

		if (!perspStack.isToBeRendered()) {
			return;
		}

		MPerspective selElement = perspStack.getSelectedElement();
		for (ToolItem ti : perspSwitcherToolbar.getItems()) {
			ti.setSelection(ti.getData() == selElement);
		}
	}


	@PostConstruct
	void init() {
		setPropertyChangeListener();
	}

	@PreDestroy
	void cleanUp() {
		if (perspectiveImage != null) {
			perspectiveImage.dispose();
			perspectiveImage = null;
		}

		PrefUtil.getAPIPreferenceStore().removePropertyChangeListener(propertyChangeListener);
	}

	@PostConstruct
	void createWidget(Composite parent, MToolControl toolControl) {
		perspSwitcherToolControl = toolControl;
		MUIElement meParent = perspSwitcherToolControl.getParent();
		int orientation = SWT.HORIZONTAL;
		if (meParent instanceof MTrimBar) {
			MTrimBar bar = (MTrimBar) meParent;
			if (bar.getSide() == SideValue.RIGHT || bar.getSide() == SideValue.LEFT) {
				orientation = SWT.VERTICAL;
			}
		}
		comp = new Composite(parent, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.marginLeft = 0;
		layout.marginRight = 4;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		comp.setLayout(layout);
		perspSwitcherToolbar = new ToolBar(comp, SWT.FLAT | SWT.WRAP | SWT.RIGHT + orientation);
		toolParent = ((Control) toolControl.getParent().getWidget());

		comp.addDisposeListener(e -> dispose());

		perspSwitcherToolbar.addMenuDetectListener(e -> {
			if (perspSwitcherToolControl.getTags().contains(NO_MENU)) {
				return;
			}
			ToolBar tb = (ToolBar) e.widget;
			Point p = new Point(e.x, e.y);
			p = perspSwitcherToolbar.getDisplay().map(null, perspSwitcherToolbar, p);
			ToolItem item = tb.getItem(p);
			if (item == null) {
				E4Util.message("  ToolBar menu"); //$NON-NLS-1$
			} else {
				MPerspective persp = (MPerspective) item.getData();
				if (persp == null) {
					E4Util.message("  Add button Menu"); //$NON-NLS-1$
				} else {
					openMenuFor(item, persp);
				}
			}
		});

		perspSwitcherToolbar.addDisposeListener(e -> disposeTBImages());

		perspSwitcherToolbar.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				if (0 <= e.childID && e.childID < perspSwitcherToolbar.getItemCount()) {
					ToolItem item = perspSwitcherToolbar.getItem(e.childID);
					if (item != null) {
						e.result = item.getToolTipText();
					}
				}
			}
		});

		hookupDnD(perspSwitcherToolbar);

		boolean showOpenOnPerspectiveBar = PrefUtil.getAPIPreferenceStore()
				.getBoolean(IWorkbenchPreferenceConstants.SHOW_OPEN_ON_PERSPECTIVE_BAR);
		if (showOpenOnPerspectiveBar) {
			ToolItem openPerspectiveItem = new ToolItem(perspSwitcherToolbar, SWT.PUSH);
			openPerspectiveItem.setImage(getOpenPerspectiveImage());
			openPerspectiveItem.setToolTipText(WorkbenchMessages.OpenPerspectiveDialogAction_tooltip);
			openPerspectiveItem.addSelectionListener(widgetSelectedAdapter(e -> selectPerspective()));
			new ToolItem(perspSwitcherToolbar, SWT.SEPARATOR);
		}

		MPerspectiveStack stack = getPerspectiveStack();
		if (stack != null) {
			// Create an item for each perspective that should show up
			for (MPerspective persp : stack.getChildren()) {
				if (persp.isToBeRendered()) {
					addPerspectiveItem(persp);
				}
			}
		}
	}

	/**
	 * Validates if the event should be processed by this component returns true
	 *
	 * @param perspectiveStack
	 *            Indicates if the event should be evaluated for a perspective
	 *            stack or a perspective
	 *
	 * @return true if the event is relevant, false if it can be ignored
	 */
	private boolean ignoreEvent(Object changedObj) {
		if (perspSwitcherToolControl == null || perspSwitcherToolbar == null || perspSwitcherToolbar.isDisposed()) {
			return true;
		}

		MWindow perspWin = modelService.getTopLevelWindowFor((MUIElement) changedObj);
		MWindow switcherWin = modelService.getTopLevelWindowFor(perspSwitcherToolControl);

		if (perspWin != switcherWin) {
			return true;
		}

		return false;
	}

	protected Point downPos = null;
	protected ToolItem dragItem = null;
	protected boolean dragging = false;
	protected Shell dragShell = null;

	private void track(MouseEvent e) {
		// Create and track the feedback overlay
		if (dragShell == null) {
			createFeedback();
		}

		// Move the drag shell
		Rectangle b = dragItem.getBounds();
		Point p = new Point(e.x, e.y);
		p = dragShell.getDisplay().map(dragItem.getParent(), null, p);
		dragShell.setLocation(p.x - (b.width / 2), p.y - (b.height / 2));

		// Set the cursor feedback
		ToolBar bar = (ToolBar) e.widget;
		ToolItem curItem = bar.getItem(new Point(e.x, e.y));
		if (curItem != null && curItem.getData() instanceof MPerspective) {
			perspSwitcherToolbar.setCursor(perspSwitcherToolbar.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		} else {
			perspSwitcherToolbar.setCursor(perspSwitcherToolbar.getDisplay().getSystemCursor(SWT.CURSOR_NO));
		}
	}

	private void createFeedback() {
		dragShell = new Shell(SWT.NO_TRIM | SWT.NO_BACKGROUND);
		dragShell.setAlpha(175);
		ToolBar dragTB = new ToolBar(dragShell, SWT.RIGHT);
		ToolItem newTI = new ToolItem(dragTB, SWT.RADIO);
		newTI.setText(dragItem.getText());
		newTI.setImage(dragItem.getImage());
		dragTB.pack();
		dragShell.pack();
		dragShell.setVisible(true);
	}

	private void hookupDnD(ToolBar bar) {
		bar.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (dragItem == null) {
					return;
				}

				ToolBar bar = (ToolBar) e.widget;
				ToolItem curItem = bar.getItem(new Point(e.x, e.y));
				if (curItem != null && curItem.getData() instanceof MPerspective) {
					Rectangle bounds = curItem.getBounds();
					Point center = new Point(bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2));
					boolean atStart = (perspSwitcherToolbar.getStyle() & SWT.HORIZONTAL) != 0 ? e.x < center.x
							: e.y < center.y;

					// OK, Calculate the correct drop index
					MPerspective dragPersp = (MPerspective) dragItem.getData();
					int dragPerspIndex = dragPersp.getParent().getChildren().indexOf(dragPersp);
					MPerspective dropPersp = (MPerspective) curItem.getData();
					int dropPerspIndex = dropPersp.getParent().getChildren().indexOf(dropPersp);
					if (!atStart) {
						dropPerspIndex++; // We're 'after' the item we're over
					}

					if (dropPerspIndex > dragPerspIndex) {
						dropPerspIndex--; // Need to account for the removal of
											// the drag item itself
					}

					// If it's not a no-op move the perspective
					if (dropPerspIndex != dragPerspIndex) {
						MElementContainer<MUIElement> parent = dragPersp.getParent();
						boolean selected = dragPersp == parent.getSelectedElement();
						parent.getChildren().remove(dragPersp);
						parent.getChildren().add(dropPerspIndex, dragPersp);
						if (selected) {
							parent.setSelectedElement(dragPersp);
						}
					}
				}

				// Reset to the initial state
				dragItem = null;
				downPos = null;
				dragging = false;
				perspSwitcherToolbar.setCursor(null);
				if (dragShell != null && !dragShell.isDisposed()) {
					dragShell.dispose();
				}
				dragShell = null;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				ToolBar bar = (ToolBar) e.widget;
				downPos = new Point(e.x, e.y);
				ToolItem downItem = bar.getItem(downPos);

				// We're only interested if the button went down over a
				// perspective item
				if (downItem != null && downItem.getData() instanceof MPerspective) {
					dragItem = downItem;
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		bar.addDragDetectListener(e -> {
			if (dragItem != null) {
				dragging = true;
				track(e);
			}
		});

		bar.addMouseMoveListener(e -> {
			if (dragging) {
				track(e);
			}
		});
	}

	private Image getOpenPerspectiveImage() {
		if (perspectiveImage == null || perspectiveImage.isDisposed()) {
			ImageDescriptor desc = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE);
			perspectiveImage = desc.createImage();
		}
		return perspectiveImage;
	}

	MPerspectiveStack getPerspectiveStack() {
		List<MPerspectiveStack> psList = modelService.findElements(window, null, MPerspectiveStack.class);
		if (psList.size() > 0) {
			return psList.get(0);
		}
		return null;
	}

	private ToolItem addPerspectiveItem(MPerspective persp) {
		int perspIndex = persp.getParent().getChildren().indexOf(persp);

		int index = perspIndex + 2; // HACK !! accounts for the 'open' and the
									// separator
		final ToolItem psItem = index < perspSwitcherToolbar.getItemCount()
				? new ToolItem(perspSwitcherToolbar, SWT.RADIO, index)
				: new ToolItem(perspSwitcherToolbar, SWT.RADIO);
		psItem.setData(persp);
		IPerspectiveDescriptor descriptor = getDescriptorFor(persp.getElementId());
		boolean foundImage = false;
		if (descriptor != null) {
			ImageDescriptor desc = descriptor.getImageDescriptor();
			if (desc != null) {
				final Image image = desc.createImage(false);
				if (image != null) {
					psItem.setImage(image);

					psItem.addListener(SWT.Dispose, event -> {
						Image currentImage = psItem.getImage();
						if (currentImage != null) {
							currentImage.dispose();
						}
					});
					foundImage = true;
					psItem.setToolTipText(persp.getLocalizedLabel());
				}
			}
		}
		if (!foundImage || PrefUtil.getAPIPreferenceStore()
				.getBoolean(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR)) {
			psItem.setText(persp.getLocalizedLabel());
			psItem.setToolTipText(persp.getLocalizedTooltip());
		}

		psItem.setSelection(persp == persp.getParent().getSelectedElement());

		psItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BusyIndicator.showWhile(null, () -> {
					MPerspective persp = (MPerspective) e.widget.getData();
					persp.getParent().setSelectedElement(persp);
				});
			}
		});

		psItem.addListener(SWT.MenuDetect, event -> {
			MPerspective persp1 = (MPerspective) event.widget.getData();
			openMenuFor(psItem, persp1);
		});

		// update the size
		fixSize();

		return psItem;
	}

	// FIXME see https://bugs.eclipse.org/bugs/show_bug.cgi?id=385547
	private IPerspectiveDescriptor getDescriptorFor(String id) {
		IPerspectiveRegistry perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		if (perspectiveRegistry instanceof PerspectiveRegistry) {
			return ((PerspectiveRegistry) perspectiveRegistry).findPerspectiveWithId(id, false);
		}

		return perspectiveRegistry.findPerspectiveWithId(id);
	}

	private void selectPerspective() {
		// let the handler perform the work to consolidate all the code
		ParameterizedCommand command = commandService
				.createCommand(IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE, Collections.EMPTY_MAP);
		handlerService.executeHandler(command);
	}

	private void openMenuFor(ToolItem item, MPerspective persp) {
		final Menu menu = new Menu(perspSwitcherToolbar);
		menu.setData(persp);
		if (persp.getParent().getSelectedElement() == persp) {
			addCustomizeItem(menu);
			addSaveAsItem(menu);
			addResetItem(menu);
		}

		if (persp.isVisible()) {
			addCloseItem(menu);
		}

		new MenuItem(menu, SWT.SEPARATOR);
		addShowTextItem(menu);

		Rectangle bounds = item.getBounds();
		Point point = perspSwitcherToolbar.toDisplay(bounds.x, bounds.y + bounds.height);
		menu.setLocation(point.x, point.y);
		menu.setVisible(true);
		menu.addMenuListener(menuHiddenAdapter(e -> perspSwitcherToolbar.getDisplay().asyncExec(menu::dispose)));
	}

	private void addCloseItem(final Menu menu) {
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(WorkbenchMessages.WorkbenchWindow_close);
		menuItem.addSelectionListener(widgetSelectedAdapter(e -> {
			MPerspective persp = (MPerspective) menu.getData();
			if (persp != null) {
				closePerspective(persp);
			}
		}));
	}

	private void closePerspective(MPerspective persp) {
		WorkbenchPage page = (WorkbenchPage) window.getContext().get(IWorkbenchPage.class);
		String perspectiveId = persp.getElementId();
		IPerspectiveDescriptor desc = getDescriptorFor(perspectiveId);
		page.closePerspective(desc, perspectiveId, true, true);
	}

	private void addSaveAsItem(final Menu menu) {
		final MenuItem saveAsMenuItem = new MenuItem(menu, SWT.PUSH);
		saveAsMenuItem.setText(WorkbenchMessages.PerspectiveBar_saveAs);
		final IWorkbenchWindow workbenchWindow = window.getContext().get(IWorkbenchWindow.class);
		workbenchWindow.getWorkbench().getHelpSystem().setHelp(saveAsMenuItem,
				IWorkbenchHelpContextIds.SAVE_PERSPECTIVE_ACTION);
		saveAsMenuItem.addSelectionListener(widgetSelectedAdapter(event -> {
			if (perspSwitcherToolbar.isDisposed()) {
				return;
			}
			IHandlerService handlerService = workbenchWindow.getService(IHandlerService.class);
			IStatus status = Status.OK_STATUS;
			try {
				handlerService.executeCommand(IWorkbenchCommandConstants.WINDOW_SAVE_PERSPECTIVE_AS, null);
			} catch (ExecutionException | NotDefinedException | NotEnabledException e) {
				status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
			} catch (NotHandledException e) {
			}
			if (!status.isOK()) {
				StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
			}
		}));
	}

	private void addResetItem(final Menu menu) {
		final MenuItem resetMenuItem = new MenuItem(menu, SWT.PUSH);
		resetMenuItem.setText(WorkbenchMessages.PerspectiveBar_reset);
		final IWorkbenchWindow workbenchWindow = window.getContext().get(IWorkbenchWindow.class);
		workbenchWindow.getWorkbench().getHelpSystem().setHelp(resetMenuItem,
				IWorkbenchHelpContextIds.RESET_PERSPECTIVE_ACTION);
		resetMenuItem.addSelectionListener(widgetSelectedAdapter(event -> {
			if (perspSwitcherToolbar.isDisposed()) {
				return;
			}
			IHandlerService handlerService = workbenchWindow.getService(IHandlerService.class);
			IStatus status = Status.OK_STATUS;
			try {
				handlerService.executeCommand(IWorkbenchCommandConstants.WINDOW_RESET_PERSPECTIVE, null);
			} catch (ExecutionException | NotDefinedException | NotEnabledException e) {
				status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
			} catch (NotHandledException e) {
			}
			if (!status.isOK()) {
				StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
			}
		}));
	}

	private void addCustomizeItem(final Menu menu) {
		final MenuItem customizeMenuItem = new MenuItem(menu, SWT.PUSH);
		customizeMenuItem.setText(WorkbenchMessages.PerspectiveBar_customize);
		final IWorkbenchWindow workbenchWindow = window.getContext().get(IWorkbenchWindow.class);
		customizeMenuItem.addSelectionListener(widgetSelectedAdapter(event -> {
			if (perspSwitcherToolbar.isDisposed()) {
				return;
			}
			IHandlerService handlerService = workbenchWindow.getService(IHandlerService.class);
			IStatus status = Status.OK_STATUS;
			try {
				handlerService.executeCommand(IWorkbenchCommandConstants.WINDOW_CUSTOMIZE_PERSPECTIVE, null);
			} catch (ExecutionException | NotDefinedException | NotEnabledException e) {
				status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
			} catch (NotHandledException e) {
			}
			if (!status.isOK()) {
				StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
			}
		}));
	}

	private void addShowTextItem(final Menu menu) {
		final MenuItem showtextMenuItem = new MenuItem(menu, SWT.CHECK);
		showtextMenuItem.setText(WorkbenchMessages.PerspectiveBar_showText);
		IPreferenceStore apiPreferenceStore = PrefUtil.getAPIPreferenceStore();
		String showTextOnPerspectiveBarPreference = IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR;
		showtextMenuItem.addSelectionListener(widgetSelectedAdapter(e -> {
			boolean preference = showtextMenuItem.getSelection();
			if (preference != apiPreferenceStore.getDefaultBoolean(showTextOnPerspectiveBarPreference)) {
				PrefUtil.getInternalPreferenceStore().setValue(IPreferenceConstants.OVERRIDE_PRESENTATION, true);
			}
			apiPreferenceStore.setValue(showTextOnPerspectiveBarPreference, preference);
			changeShowText(preference);
		}));
		showtextMenuItem.setSelection(apiPreferenceStore.getBoolean(showTextOnPerspectiveBarPreference));
	}

	private void setPropertyChangeListener() {
		propertyChangeListener = propertyChangeEvent -> {
			if (IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR.equals(propertyChangeEvent.getProperty())) {
				Object newValue = propertyChangeEvent.getNewValue();
				boolean showText = true; // default
				if (newValue instanceof Boolean) {
					showText = ((Boolean) newValue).booleanValue();
				} else if ("false".equals(newValue)) { //$NON-NLS-1$
					showText = false;
				}
				changeShowText(showText);
			}
		};
		PrefUtil.getAPIPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	private void changeShowText(boolean showText) {
		ToolItem[] items = perspSwitcherToolbar.getItems();
		for (ToolItem item : items) {
			MPerspective persp = (MPerspective) item.getData();
			if (persp != null) {
				if (showText) {
					if (persp.getLabel() != null) {
						item.setText(persp.getLocalizedLabel());
					}
					item.setToolTipText(persp.getLocalizedTooltip());
				} else {
					Image image = item.getImage();
					if (image != null) {
						item.setText(""); //$NON-NLS-1$
						item.setToolTipText(persp.getLocalizedLabel());
					}
				}
			}
		}

		// update the size
		fixSize();
	}

	private void fixSize() {
		perspSwitcherToolbar.pack();
		perspSwitcherToolbar.getParent().pack();
		perspSwitcherToolbar.requestLayout();
	}

	private void removePerspectiveItem(MPerspective toRemove) {
		ToolItem psItem = getItemFor(toRemove);
		if (psItem != null) {
			psItem.dispose();
		}

		// update the size
		fixSize();
	}

	/**
	 * @param persp the perspective
	 * @return the tool item
	 */
	protected ToolItem getItemFor(MPerspective persp) {
		if (perspSwitcherToolbar == null) {
			return null;
		}

		for (ToolItem ti : perspSwitcherToolbar.getItems()) {
			if (ti.getData() == persp) {
				return ti;
			}
		}

		return null;
	}


	void dispose() {
		cleanUp();
	}

	void disposeTBImages() {
		ToolItem[] items = perspSwitcherToolbar.getItems();
		for (ToolItem item : items) {
			Image image = item.getImage();
			if (image != null) {
				item.setImage(null);
				image.dispose();
			}
		}
	}

	private void updateToolItem(ToolItem ti, String attName, Object newValue) {
		boolean showText = PrefUtil.getAPIPreferenceStore()
				.getBoolean(IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR);
		if (showText && UIEvents.UILabel.LABEL.equals(attName)) {
			String newName = (String) newValue;
			ti.setText(newName);
		} else if (UIEvents.UILabel.TOOLTIP.equals(attName)) {
			String newTTip = (String) newValue;
			ti.setToolTipText(newTTip);
		} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
			Image currentImage = ti.getImage();
			String uri = (String) newValue;
			URL url = null;
			try {
				url = new URL(uri);
				ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
				if (descriptor == null) {
					ti.setImage(null);
				} else {
					ti.setImage(descriptor.createImage());
				}
			} catch (IOException e) {
				ti.setImage(null);
				logger.warn(e);
			} finally {
				if (currentImage != null) {
					currentImage.dispose();
				}
			}
		}
	}
}
