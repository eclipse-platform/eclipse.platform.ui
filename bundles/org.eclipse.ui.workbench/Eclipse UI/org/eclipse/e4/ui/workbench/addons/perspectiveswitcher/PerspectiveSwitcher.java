/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sopot Cela <sopotcela@gmail.com> - Bug 391961
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440810, 485840
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 380233
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 485829
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.perspectiveswitcher;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
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

public class PerspectiveSwitcher {
	/**
	 *
	 */
	public static final String PERSPECTIVE_SWITCHER_ID = "org.eclipse.e4.ui.PerspectiveSwitcher"; //$NON-NLS-1$
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
	private Image backgroundImage;
	private Image perspectiveImage;

	Color borderColor, curveColor;
	Control toolParent;
	IPropertyChangeListener propertyChangeListener;

	@Inject
	void handleChildrenEvent(@Optional @UIEventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) Event event) {

		if (event == null)
			return;

		if (perspSwitcherToolbar.isDisposed()) {
			return;
		}

		Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);

		if (perspSwitcherToolControl == null || !(changedObj instanceof MPerspectiveStack))
			return;

		MWindow perspWin = modelService.getTopLevelWindowFor((MUIElement) changedObj);
		MWindow switcherWin = modelService.getTopLevelWindowFor(perspSwitcherToolControl);
		if (perspWin != switcherWin)
			return;

		if (UIEvents.isADD(event)) {
			for (Object o : UIEvents.asIterable(event, UIEvents.EventTags.NEW_VALUE)) {
				MPerspective added = (MPerspective) o;
				// Adding invisible elements is a NO-OP
				if (!added.isToBeRendered())
					continue;

				addPerspectiveItem(added);
			}
		} else if (UIEvents.isREMOVE(event)) {
			for (Object o : UIEvents.asIterable(event, UIEvents.EventTags.OLD_VALUE)) {
				MPerspective removed = (MPerspective) o;
				// Removing invisible elements is a NO-OP
				if (!removed.isToBeRendered())
					continue;

				removePerspectiveItem(removed);
			}
		}

	}

	@Inject
	void handleToBeRenderedEvent(@Optional @UIEventTopic(UIEvents.UIElement.TOPIC_TOBERENDERED) Event event) {
		if (event == null)
			return;

		if (perspSwitcherToolbar.isDisposed()) {
			return;
		}

		MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

		if (perspSwitcherToolControl == null || !(changedElement instanceof MPerspective))
			return;

		MWindow perspWin = modelService.getTopLevelWindowFor(changedElement);
		MWindow switcherWin = modelService.getTopLevelWindowFor(perspSwitcherToolControl);
		if (perspWin != switcherWin)
			return;

		MPerspective persp = (MPerspective) changedElement;
		if (!persp.getParent().isToBeRendered())
			return;

		if (changedElement.isToBeRendered()) {
			addPerspectiveItem(persp);
		} else {
			removePerspectiveItem(persp);
		}

	}

	@Inject
	void handleLabelEvent(@Optional @UIEventTopic(UIEvents.UILabel.TOPIC_ALL) Event event) {
		if (event == null)
			return;
		if (perspSwitcherToolbar.isDisposed()) {
			return;
		}

		MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

		if (perspSwitcherToolControl == null || !(changedElement instanceof MPerspective))
			return;

		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
		Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);

		MWindow perspWin = modelService.getTopLevelWindowFor(changedElement);
		MWindow switcherWin = modelService.getTopLevelWindowFor(perspSwitcherToolControl);
		if (perspWin != switcherWin)
			return;

		MPerspective perspective = (MPerspective) changedElement;
		if (!perspective.isToBeRendered())
			return;

		for (ToolItem ti : perspSwitcherToolbar.getItems()) {
			if (ti.getData() == perspective) {
				updateToolItem(ti, attName, newValue);
			}
		}

		// update the size
		fixSize();
	}

	@Inject
	void handleSelectionEvent(@Optional @UIEventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		if (event == null)
			return;
		if (perspSwitcherToolbar.isDisposed()) {
			return;
		}

		MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);

		if (perspSwitcherToolControl == null || !(changedElement instanceof MPerspectiveStack))
			return;

		MWindow perspWin = modelService.getTopLevelWindowFor(changedElement);
		MWindow switcherWin = modelService.getTopLevelWindowFor(perspSwitcherToolControl);
		if (perspWin != switcherWin)
			return;

		MPerspectiveStack perspStack = (MPerspectiveStack) changedElement;
		if (!perspStack.isToBeRendered())
			return;

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
			if (bar.getSide() == SideValue.RIGHT || bar.getSide() == SideValue.LEFT)
				orientation = SWT.VERTICAL;
		}
		comp = new Composite(parent, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.marginLeft = layout.marginRight = 8;
		layout.marginBottom = 4;
		layout.marginTop = 6;
		comp.setLayout(layout);
		perspSwitcherToolbar = new ToolBar(comp, SWT.FLAT | SWT.WRAP | SWT.RIGHT + orientation);
		comp.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});
		toolParent = ((Control) toolControl.getParent().getWidget());
		toolParent.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				if (borderColor == null || borderColor.isDisposed()) {
					borderColor = e.display.getSystemColor(SWT.COLOR_GRAY);
				}
				e.gc.setForeground(borderColor);
				Rectangle bounds = ((Control) e.widget).getBounds();
				e.gc.drawLine(0, bounds.height - 1, bounds.width, bounds.height - 1);
			}
		});

		comp.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}

		});

		perspSwitcherToolbar.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(MenuDetectEvent e) {
				ToolBar tb = (ToolBar) e.widget;
				Point p = new Point(e.x, e.y);
				p = perspSwitcherToolbar.getDisplay().map(null, perspSwitcherToolbar, p);
				ToolItem item = tb.getItem(p);
				if (item == null)
					E4Util.message("  ToolBar menu"); //$NON-NLS-1$
				else {
					MPerspective persp = (MPerspective) item.getData();
					if (persp == null)
						E4Util.message("  Add button Menu"); //$NON-NLS-1$
					else
						openMenuFor(item, persp);
				}
			}
		});

		perspSwitcherToolbar.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				disposeTBImages();
			}

		});

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
			final ToolItem openPerspectiveItem = new ToolItem(perspSwitcherToolbar, SWT.PUSH);
			openPerspectiveItem.setImage(getOpenPerspectiveImage());
			openPerspectiveItem.setToolTipText(WorkbenchMessages.OpenPerspectiveDialogAction_tooltip);
			openPerspectiveItem.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectPerspective();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					selectPerspective();
				}
			});
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

	protected Point downPos = null;
	protected ToolItem dragItem = null;
	protected boolean dragging = false;
	protected Shell dragShell = null;

	private void track(MouseEvent e) {
		// Create and track the feedback overlay
		if (dragShell == null)
			createFeedback();

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
				if (dragItem == null)
					return;

				ToolBar bar = (ToolBar) e.widget;
				ToolItem curItem = bar.getItem(new Point(e.x, e.y));
				if (curItem != null && curItem.getData() instanceof MPerspective) {
					Rectangle bounds = curItem.getBounds();
					Point center = new Point(bounds.x + (bounds.width / 2), bounds.y
							+ (bounds.height / 2));
					boolean atStart = (perspSwitcherToolbar.getStyle() & SWT.HORIZONTAL) != 0 ? e.x < center.x
							: e.y < center.y;

					// OK, Calculate the correct drop index
					MPerspective dragPersp = (MPerspective) dragItem.getData();
					int dragPerspIndex = dragPersp.getParent().getChildren().indexOf(dragPersp);
					MPerspective dropPersp = (MPerspective) curItem.getData();
					int dropPerspIndex = dropPersp.getParent().getChildren().indexOf(dropPersp);
					if (!atStart)
						dropPerspIndex++; // We're 'after' the item we're over

					if (dropPerspIndex > dragPerspIndex)
						dropPerspIndex--; // Need to account for the removal of
											// the drag item itself

					// If it's not a no-op move the perspective
					if (dropPerspIndex != dragPerspIndex) {
						MElementContainer<MUIElement> parent = dragPersp.getParent();
						boolean selected = dragPersp == parent.getSelectedElement();
						parent.getChildren().remove(dragPersp);
						parent.getChildren().add(dropPerspIndex, dragPersp);
						if (selected)
							parent.setSelectedElement(dragPersp);
					}
				}

				// Reset to the initial state
				dragItem = null;
				downPos = null;
				dragging = false;
				perspSwitcherToolbar.setCursor(null);
				if (dragShell != null && !dragShell.isDisposed())
					dragShell.dispose();
				dragShell = null;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				ToolBar bar = (ToolBar) e.widget;
				downPos = new Point(e.x, e.y);
				ToolItem downItem = bar.getItem(downPos);

				// We're only interested if the button went down over a
				// perspective item
				if (downItem != null && downItem.getData() instanceof MPerspective)
					dragItem = downItem;
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		bar.addDragDetectListener(new DragDetectListener() {
			@Override
			public void dragDetected(DragDetectEvent e) {
				if (dragItem != null) {
					dragging = true;
					track(e);
				}
			}
		});

		bar.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if (dragging) {
					track(e);
				}
			}
		});
	}

	private Image getOpenPerspectiveImage() {
		if (perspectiveImage == null || perspectiveImage.isDisposed()) {
			ImageDescriptor desc = WorkbenchImages
					.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE);
			perspectiveImage = desc.createImage();
		}
		return perspectiveImage;
	}

	MPerspectiveStack getPerspectiveStack() {
		List<MPerspectiveStack> psList = modelService.findElements(window, null,
				MPerspectiveStack.class, null);
		if (psList.size() > 0)
			return psList.get(0);
		return null;
	}

	private ToolItem addPerspectiveItem(MPerspective persp) {
		int perspIndex = persp.getParent().getChildren().indexOf(persp);

		int index = perspIndex + 2; // HACK !! accounts for the 'open' and the
									// separator
		final ToolItem psItem = index < perspSwitcherToolbar.getItemCount() ? new ToolItem(perspSwitcherToolbar, SWT.RADIO, index)
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

					psItem.addListener(SWT.Dispose, new Listener() {
						@Override
						public void handleEvent(org.eclipse.swt.widgets.Event event) {
							Image currentImage = psItem.getImage();
							if (currentImage != null)
								currentImage.dispose();
						}
					});
					foundImage = true;
					psItem.setToolTipText(persp.getLocalizedLabel());
				}
			}
		}
		if (!foundImage
				|| PrefUtil.getAPIPreferenceStore().getBoolean(
						IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR)) {
			psItem.setText(persp.getLocalizedLabel());
			psItem.setToolTipText(persp.getLocalizedTooltip());
		}

		psItem.setSelection(persp == persp.getParent().getSelectedElement());

		psItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MPerspective persp = (MPerspective) e.widget.getData();
				persp.getParent().setSelectedElement(persp);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				MPerspective persp = (MPerspective) e.widget.getData();
				persp.getParent().setSelectedElement(persp);
			}
		});

		psItem.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				MPerspective persp = (MPerspective) event.widget.getData();
				openMenuFor(psItem, persp);
			}
		});

		// update the size
		fixSize();

		return psItem;
	}

	// FIXME see https://bugs.eclipse.org/bugs/show_bug.cgi?id=385547
	private IPerspectiveDescriptor getDescriptorFor(String id) {
		IPerspectiveRegistry perspectiveRegistry = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();
		if (perspectiveRegistry instanceof PerspectiveRegistry) {
			return ((PerspectiveRegistry) perspectiveRegistry).findPerspectiveWithId(id, false);
		}

		return perspectiveRegistry.findPerspectiveWithId(id);
	}

	private void selectPerspective() {
		// let the handler perform the work to consolidate all the code
		ParameterizedCommand command = commandService.createCommand(
				IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE, Collections.EMPTY_MAP);
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
		// addDockOnSubMenu(menu);
		addShowTextItem(menu);

		Rectangle bounds = item.getBounds();
		Point point = perspSwitcherToolbar.toDisplay(bounds.x, bounds.y + bounds.height);
		menu.setLocation(point.x, point.y);
		menu.setVisible(true);
		menu.addMenuListener(new MenuListener() {

			@Override
			public void menuHidden(MenuEvent e) {
				perspSwitcherToolbar.getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						menu.dispose();
					}

				});
			}

			@Override
			public void menuShown(MenuEvent e) {
				// Nothing to do
			}

		});
	}

	private void addCloseItem(final Menu menu) {
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(WorkbenchMessages.WorkbenchWindow_close);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MPerspective persp = (MPerspective) menu.getData();
				if (persp != null)
					closePerspective(persp);
			}
		});
	}

	private void closePerspective(MPerspective persp) {
		WorkbenchPage page = (WorkbenchPage) window.getContext().get(IWorkbenchPage.class);
		String perspectiveId = persp.getElementId();
		IPerspectiveDescriptor desc = getDescriptorFor(perspectiveId);
		page.closePerspective(desc, perspectiveId, true, true);
	}

	private void addSaveAsItem(final Menu menu) {
		final MenuItem saveAsMenuItem = new MenuItem(menu, SWT.Activate);
		saveAsMenuItem.setText(WorkbenchMessages.PerspectiveBar_saveAs);
		final IWorkbenchWindow workbenchWindow = window.getContext().get(IWorkbenchWindow.class);
		workbenchWindow.getWorkbench().getHelpSystem().setHelp(saveAsMenuItem,
				IWorkbenchHelpContextIds.SAVE_PERSPECTIVE_ACTION);
		saveAsMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (perspSwitcherToolbar.isDisposed())
					return;
				IHandlerService handlerService = workbenchWindow.getService(IHandlerService.class);
				IStatus status = Status.OK_STATUS;
				try {
					handlerService.executeCommand(IWorkbenchCommandConstants.WINDOW_SAVE_PERSPECTIVE_AS, null);
				} catch (ExecutionException e) {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
				} catch (NotDefinedException e) {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
				} catch (NotEnabledException e) {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
				} catch (NotHandledException e) {
				}
				if (!status.isOK())
					StatusManager.getManager().handle(status,
							StatusManager.SHOW | StatusManager.LOG);
			}
		});
	}

	private void addResetItem(final Menu menu) {
		final MenuItem resetMenuItem = new MenuItem(menu, SWT.Activate);
		resetMenuItem.setText(WorkbenchMessages.PerspectiveBar_reset);
		final IWorkbenchWindow workbenchWindow = window.getContext().get(IWorkbenchWindow.class);
		workbenchWindow.getWorkbench().getHelpSystem().setHelp(resetMenuItem,
				IWorkbenchHelpContextIds.RESET_PERSPECTIVE_ACTION);
		resetMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (perspSwitcherToolbar.isDisposed())
					return;
				IHandlerService handlerService = workbenchWindow.getService(IHandlerService.class);
				IStatus status = Status.OK_STATUS;
				try {
					handlerService.executeCommand(IWorkbenchCommandConstants.WINDOW_RESET_PERSPECTIVE, null);
				} catch (ExecutionException e) {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
				} catch (NotDefinedException e) {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
				} catch (NotEnabledException e) {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
				} catch (NotHandledException e) {
				}
				if (!status.isOK())
					StatusManager.getManager().handle(status,
							StatusManager.SHOW | StatusManager.LOG);
			}
		});
	}

	private void addCustomizeItem(final Menu menu) {
		final MenuItem customizeMenuItem = new MenuItem(menu, SWT.Activate);
		customizeMenuItem.setText(WorkbenchMessages.PerspectiveBar_customize);
		final IWorkbenchWindow workbenchWindow = window.getContext().get(IWorkbenchWindow.class);
		customizeMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (perspSwitcherToolbar.isDisposed()) {
					return;
				}
				IHandlerService handlerService = workbenchWindow.getService(IHandlerService.class);
				IStatus status = Status.OK_STATUS;
				try {
					handlerService.executeCommand(IWorkbenchCommandConstants.WINDOW_CUSTOMIZE_PERSPECTIVE, null);
				} catch (ExecutionException e) {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
				} catch (NotDefinedException e) {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
				} catch (NotEnabledException e) {
					status = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, e.getMessage(), e);
				} catch (NotHandledException e) {
				}
				if (!status.isOK()) {
					StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
				}
			}
		});
	}

	private void addShowTextItem(final Menu menu) {
		final MenuItem showtextMenuItem = new MenuItem(menu, SWT.CHECK);
		showtextMenuItem.setText(WorkbenchMessages.PerspectiveBar_showText);
		showtextMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean preference = showtextMenuItem.getSelection();
				if (preference != PrefUtil.getAPIPreferenceStore().getDefaultBoolean(
						IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR)) {
					PrefUtil.getInternalPreferenceStore().setValue(
							IPreferenceConstants.OVERRIDE_PRESENTATION, true);
				}
				PrefUtil.getAPIPreferenceStore().setValue(
						IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR, preference);
				changeShowText(preference);
			}
		});
		showtextMenuItem.setSelection(PrefUtil.getAPIPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR));
	}

	private void setPropertyChangeListener() {
		propertyChangeListener = new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
				if (IWorkbenchPreferenceConstants.SHOW_TEXT_ON_PERSPECTIVE_BAR
						.equals(propertyChangeEvent.getProperty())) {
					Object newValue = propertyChangeEvent.getNewValue();
					boolean showText = true; // default
					if (newValue instanceof Boolean)
						showText = ((Boolean) newValue).booleanValue();
					else if ("false".equals(newValue)) //$NON-NLS-1$
						showText = false;
					changeShowText(showText);
				}
			}
		};
		PrefUtil.getAPIPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	private void changeShowText(boolean showText) {
		ToolItem[] items = perspSwitcherToolbar.getItems();
		for (ToolItem item : items) {
			MPerspective persp = (MPerspective) item.getData();
			if (persp != null)
				if (showText) {
					if (persp.getLabel() != null)
						item.setText(persp.getLocalizedLabel());
					item.setToolTipText(persp.getLocalizedTooltip());
				} else {
					Image image = item.getImage();
					if (image != null) {
						item.setText(""); //$NON-NLS-1$
						item.setToolTipText(persp.getLocalizedLabel());
					}
				}
		}

		// update the size
		fixSize();
	}

	private void fixSize() {
		perspSwitcherToolbar.pack();
		perspSwitcherToolbar.getParent().pack();
		perspSwitcherToolbar.getShell().layout(new Control[] { perspSwitcherToolbar }, SWT.DEFER);
	}

	private void removePerspectiveItem(MPerspective toRemove) {
		ToolItem psItem = getItemFor(toRemove);
		if (psItem != null) {
			psItem.dispose();
		}

		// update the size
		fixSize();
	}

	protected ToolItem getItemFor(MPerspective persp) {
		if (perspSwitcherToolbar == null)
			return null;

		for (ToolItem ti : perspSwitcherToolbar.getItems()) {
			if (ti.getData() == persp)
				return ti;
		}

		return null;
	}

	void paint(PaintEvent e) {
		GC gc = e.gc;
		Point size = comp.getSize();
		if (curveColor == null || curveColor.isDisposed()) {
			curveColor = e.display.getSystemColor(SWT.COLOR_GRAY);
		}
		int h = size.y;
		int[] simpleCurve = new int[] { 0, h - 1, 1, h - 1, 2, h - 2, 2, 1, 3, 0 };
		// draw border
		gc.setForeground(curveColor);
		gc.setAdvanced(true);
		if (gc.getAdvanced()) {
			gc.setAntialias(SWT.ON);
		}
		gc.drawPolyline(simpleCurve);

		Rectangle bounds = ((Control) e.widget).getBounds();
		bounds.x = bounds.y = 0;
		Region r = new Region();
		r.add(bounds);
		int[] simpleCurveClose = new int[simpleCurve.length + 4];
		System.arraycopy(simpleCurve, 0, simpleCurveClose, 0, simpleCurve.length);
		int index = simpleCurve.length;
		simpleCurveClose[index++] = bounds.width;
		simpleCurveClose[index++] = 0;
		simpleCurveClose[index++] = bounds.width;
		simpleCurveClose[index++] = bounds.height;
		r.subtract(simpleCurveClose);
		Region clipping = new Region();
		gc.getClipping(clipping);
		r.intersect(clipping);
		gc.setClipping(r);
		Image b = toolParent.getBackgroundImage();
		if (b != null && !b.isDisposed())
			gc.drawImage(b, 0, 0);

		r.dispose();
		clipping.dispose();
	}

	void resize() {
		Point size = comp.getSize();
		Image oldBackgroundImage = backgroundImage;
		backgroundImage = new Image(comp.getDisplay(), size.x, size.y);
		GC gc = new GC(backgroundImage);
		comp.getParent().drawBackground(gc, 0, 0, size.x, size.y, 0, 0);
		Color background = comp.getBackground();
		Color border = comp.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		RGB backgroundRGB = background.getRGB();
		// TODO naive and hard coded, doesn't deal with high contrast, etc.
		Color gradientTop = new Color(comp.getDisplay(), backgroundRGB.red + 12,
				backgroundRGB.green + 10, backgroundRGB.blue + 10);
		int h = size.y;
		int curveStart = 0;
		int curve_width = 5;

		int[] curve = new int[] { 0, h, 1, h, 2, h - 1, 3, h - 2, 3, 2, 4, 1, 5, 0, };
		int[] line1 = new int[curve.length + 4];
		int index = 0;
		int x = curveStart;
		line1[index++] = x + 1;
		line1[index++] = h;
		for (int i = 0; i < curve.length / 2; i++) {
			line1[index++] = x + curve[2 * i];
			line1[index++] = curve[2 * i + 1];
		}
		line1[index++] = x + curve_width;
		line1[index++] = 0;

		int[] line2 = new int[line1.length];
		index = 0;
		for (int i = 0; i < line1.length / 2; i++) {
			line2[index] = line1[index++] - 1;
			line2[index] = line1[index++];
		}

		// custom gradient
		gc.setForeground(gradientTop);
		gc.setBackground(background);
		gc.drawLine(4, 0, size.x, 0);
		gc.drawLine(3, 1, size.x, 1);
		gc.fillGradientRectangle(2, 2, size.x - 2, size.y - 3, true);
		gc.setForeground(background);
		gc.drawLine(2, size.y - 1, size.x, size.y - 1);
		gradientTop.dispose();

		gc.setForeground(border);
		gc.drawPolyline(line2);
		gc.dispose();
		comp.setBackgroundImage(backgroundImage);
		if (oldBackgroundImage != null)
			oldBackgroundImage.dispose();

	}

	void dispose() {
		cleanUp();

		if (backgroundImage != null) {
			comp.setBackgroundImage(null);
			backgroundImage.dispose();
			backgroundImage = null;
		}
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

	public void setKeylineColor(Color borderColor, Color curveColor) {
		this.borderColor = borderColor;
		this.curveColor = curveColor;
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
				} else
					ti.setImage(descriptor.createImage());
			} catch (IOException e) {
				ti.setImage(null);
				logger.warn(e);
			} finally {
				if (currentImage != null)
					currentImage.dispose();
			}
		}
	}
}
