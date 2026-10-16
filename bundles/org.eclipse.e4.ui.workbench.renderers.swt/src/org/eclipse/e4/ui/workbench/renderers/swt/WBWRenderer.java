/*******************************************************************************
 * Copyright (c) 2008, 2026 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 429728, 441150, 444410, 472654
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 429729, 506306
 *     Mike Leneweit <mike-le@web.de> - Bug 444410
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import static java.util.Collections.singletonList;
import static org.eclipse.jface.viewers.LabelProvider.createTextProvider;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.resources.IResourcesRegistry;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.css.swt.resources.ResourceByDefinitionKey;
import org.eclipse.e4.ui.css.swt.resources.SWTResourcesRegistry;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.PartServiceSaveHandler;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.SWTRenderersMessages;
import org.eclipse.e4.ui.internal.workbench.swt.CSSConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;

/**
 * Default SWT renderer responsible for an instance of MWindow. See
 * {@link WorkbenchRendererFactory}
 */
public class WBWRenderer extends SWTPartRenderer {

	private static String ShellMinimizedTag = "shellMinimized"; //$NON-NLS-1$
	private static String ShellMaximizedTag = "shellMaximized"; //$NON-NLS-1$

	/**
	 * A named preference of the
	 * {@link CTabRendering#PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT}
	 * node: when <code>true</code>, the detached windows are rendered as top level
	 * shells instead of children of the shell of their window, as if they had the
	 * {@link IPresentationEngine#WINDOW_TOP_LEVEL} tag.
	 * <p>
	 * The window manager keeps a child shell with its parent: activating a
	 * detached window brings the workbench window to the front too, over the
	 * other applications. A top level detached window is activated alone.
	 * </p>
	 * <p>
	 * Such a shell has its own task bar entry: it can be minimized and, when the
	 * window has no label, its title is the label of the part it shows.
	 * </p>
	 * <p>
	 * The preference is read when the shell of a detached window is created.
	 * </p>
	 */
	public static final String DETACHED_WINDOWS_TOP_LEVEL = "DETACHED_WINDOWS_TOP_LEVEL"; //$NON-NLS-1$

	/**
	 * Default value of the {@link #DETACHED_WINDOWS_TOP_LEVEL} preference: the
	 * detached windows are children of the shell of their window.
	 */
	public static final boolean DETACHED_WINDOWS_TOP_LEVEL_DEFAULT = false;

	/**
	 * Marks the shell of a detached window made top level by the
	 * {@link #DETACHED_WINDOWS_TOP_LEVEL} preference: its title follows the part
	 * it shows.
	 */
	private static final String DETACHED_TOP_LEVEL_SHELL = "detachedTopLevelShell"; //$NON-NLS-1$

	private class WindowSizeUpdateJob implements Runnable {
		public List<MWindow> windowsToUpdate = new ArrayList<>();

		@Override
		public void run() {
			boundsJob = null;
			while (!windowsToUpdate.isEmpty()) {
				MWindow window = windowsToUpdate.remove(0);
				Shell shell = (Shell) window.getWidget();
				if (shell == null || shell.isDisposed()) {
					continue;
				}
				shell.setBounds(window.getX(), window.getY(),
						window.getWidth(), window.getHeight());
			}
		}
	}

	WindowSizeUpdateJob boundsJob;

	boolean ignoreSizeChanges = false;

	@Inject
	Logger logger;

	@SuppressWarnings("hiding")
	@Inject
	private IEclipseContext context;

	@Inject
	private IPresentationEngine engine;

	private ThemeDefinitionChangedHandler themeDefinitionChanged;

	@SuppressWarnings("hiding")
	@Inject
	private EModelService modelService;

	@Inject
	private Display display;

	@Inject
	@Optional
	private void subscribeTopicSelectedElementChanged(
			@UIEventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		// Ensure that this event is for a MApplication
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MApplication)) {
			return;
		}
		MWindow win = (MWindow) event.getProperty(UIEvents.EventTags.NEW_VALUE);
		if ((win == null) || !win.getTags().contains("topLevel")) { //$NON-NLS-1$
			return;
		}
		win.setToBeRendered(true);
		if (!(win.getRenderer() == WBWRenderer.this)) {
			return;
		}
		Shell shell = (Shell) win.getWidget();
		if (shell.getMinimized()) {
			shell.setMinimized(false);
		}
		shell.setActive();
		shell.moveAbove(null);
	}

	@Inject
	@Optional
	private void subscribeTopicLabelChanged(@UIEventTopic(UIEvents.UILabel.TOPIC_ALL) Event event) {
		Object objElement = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MWindow)) {
			return;
		}

		// Is this listener interested ?
		MWindow windowModel = (MWindow) objElement;
		if (windowModel.getRenderer() != WBWRenderer.this) {
			return;
		}

		// No widget == nothing to update
		Shell theShell = (Shell) windowModel.getWidget();
		if (theShell == null) {
			return;
		}

		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);

		if (UIEvents.UILabel.LABEL.equals(attName) || UIEvents.UILabel.LOCALIZED_LABEL.equals(attName)) {
			String newTitle = (String) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			theShell.setText(newTitle);
		} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
			theShell.setImage(getImage(windowModel));
			// child windows may take their shell icon from the parent
			for (MWindow child : windowModel.getWindows()) {
				if (child.getRenderer() instanceof WBWRenderer) {
					((WBWRenderer) child.getRenderer()).handleParentChange(child);
				}
			}
		} else if (UIEvents.UILabel.TOOLTIP.equals(attName) || UIEvents.UILabel.LOCALIZED_TOOLTIP.equals(attName)) {
			String newTTip = (String) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			theShell.setToolTipText(newTTip);
		}
	}

	/**
	 * A top level detached window has its own task bar entry: when it has no
	 * label, its title follows the label of the part it shows.
	 */
	@Inject
	@Optional
	private void subscribeTopicPartLabelChanged(@UIEventTopic(UIEvents.UILabel.TOPIC_ALL) Event event) {
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MPart part)) {
			return;
		}
		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
		if (UIEvents.UILabel.LABEL.equals(attName) || UIEvents.UILabel.LOCALIZED_LABEL.equals(attName)) {
			updateDetachedWindowTitle(getWindowOf(part.getCurSharedRef() != null ? part.getCurSharedRef() : part));
		}
	}

	@Inject
	@Optional
	private void subscribeTopicDetachedWindowSelectionChanged(
			@UIEventTopic(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MElementContainer<?> container
				&& !(container instanceof MApplication)) {
			updateDetachedWindowTitle(getWindowOf(container));
		}
	}

	@Inject
	@Optional
	private void subscribeTopicWindowChanged(@UIEventTopic(UIEvents.Window.TOPIC_ALL) Event event) {
		if (ignoreSizeChanges) {
			return;
		}

		// Ensure that this event is for a MMenuItem
		Object objElement = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(objElement instanceof MWindow windowModel)) {
			return;
		}

		// Is this listener interested ?
		if (windowModel.getRenderer() != WBWRenderer.this) {
			return;
		}

		// No widget == nothing to update
		Shell theShell = (Shell) windowModel.getWidget();
		if (theShell == null) {
			return;
		}

		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);

		if (UIEvents.Window.X.equals(attName) || UIEvents.Window.Y.equals(attName)
				|| UIEvents.Window.WIDTH.equals(attName) || UIEvents.Window.HEIGHT.equals(attName)) {
			if (boundsJob == null) {
				boundsJob = new WindowSizeUpdateJob();
				boundsJob.windowsToUpdate.add(windowModel);
				theShell.getDisplay().asyncExec(boundsJob);
			} else if (!boundsJob.windowsToUpdate.contains(windowModel)) {
				boundsJob.windowsToUpdate.add(windowModel);
			}
		}
	}

	@Inject
	@Optional
	private void subscribeTopicVisibleChanged(@UIEventTopic(UIEvents.UIElement.TOPIC_VISIBLE) Event event) {
		// Ensure that this event is for a MMenuItem
		Object objElement = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(objElement instanceof MWindow windowModel)) {
			return;
		}

		// Is this listener interested ?
		if (windowModel.getRenderer() != WBWRenderer.this) {
			return;
		}

		// No widget == nothing to update
		Shell theShell = (Shell) windowModel.getWidget();
		if (theShell == null) {
			return;
		}

		String attName = (String) event.getProperty(UIEvents.EventTags.ATTNAME);

		if (UIEvents.UIElement.VISIBLE.equals(attName)) {
			boolean isVisible = (Boolean) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			theShell.setVisible(isVisible);
		}
	}

	@Inject
	@Optional
	private void subscribeThemeDefinitionChanged(
			@UIEventTopic(UIEvents.UILifeCycle.THEME_DEFINITION_CHANGED) Event event) {
		themeDefinitionChanged.handleEvent(event);
	}

	@Inject
	@Optional
	private void subscribeTopicDetachedChanged(@UIEventTopic(UIEvents.Window.TOPIC_WINDOWS) Event event) {
		/*
		 * Handle any changes required for parent changes on detached windows.
		 * This isn't quite straightforward as we don't see TOPIC_PARENT events
		 * parent changes are only described as ADD and REMOVE on the
		 * Window.TOPIC_WINDOWS and Application.TOPIC_CHILDREN.
		 */
		if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MWindow)) {
			return;
		}

		if (UIEvents.isREMOVE(event)) {
			for (Object removed : UIEvents.asIterable(event, UIEvents.EventTags.OLD_VALUE)) {
				if (removed instanceof MWindow window && window.getRenderer() instanceof WBWRenderer) {
					((WBWRenderer) window.getRenderer()).handleParentChange(window);
				}
			}
		} else if (UIEvents.isADD(event)) {
			for (Object removed : UIEvents.asIterable(event, UIEvents.EventTags.NEW_VALUE)) {
				if (removed instanceof MWindow window && window.getRenderer() instanceof WBWRenderer) {
					((WBWRenderer) window.getRenderer()).handleParentChange(window);
				}
			}
		}
	}

	/**
	 * Update this child window with any values that may have been obtained from
	 * the parent.
	 *
	 * @param child
	 *            the child window (may now be orphaned)
	 */
	private void handleParentChange(MWindow child) {
		// No widget == nothing to update
		Shell theShell = (Shell) child.getWidget();
		if (theShell == null) {
			return;
		}

		// Detached windows may take their shell icon from the parent window
		theShell.setImage(getImage(child));
	}

	/**
	 * Closes the provided detached window.
	 *
	 * @param window
	 *            the detached window to close
	 * @return <code>true</code> if the window should be closed,
	 *         <code>false</code> otherwise
	 */
	private boolean closeDetachedWindow(MWindow window) {
		EPartService partService = window.getContext().get(EPartService.class);
		List<MPart> parts = modelService.findElements(window, null, MPart.class);
		// this saves one part at a time, not ideal but better than not saving
		// at all
		for (MPart part : parts) {
			if (!partService.savePart(part, true)) {
				// user cancelled the operation, return false
				return false;
			}
		}

		// hide every part individually, following 3.x behaviour
		for (MPart part : parts) {
			partService.hidePart(part);
		}
		return true;
	}

	@PostConstruct
	protected void init() {
		themeDefinitionChanged = new ThemeDefinitionChangedHandler();
	}

	@Override
	public Object createWidget(MUIElement element, Object parent) {
		final Widget newWidget;

		if (!(element instanceof MWindow wbwModel) || (parent != null && !(parent instanceof Control))) {
			return null;
		}

		MApplication appModel = wbwModel.getContext().get(MApplication.class);
		Boolean rtlMode = (Boolean) appModel.getTransientData().get(E4Workbench.RTL_MODE);
		int rtlStyle = (rtlMode != null && rtlMode.booleanValue()) ? SWT.RIGHT_TO_LEFT : 0;

		Shell parentShell = parent == null ? null : ((Control) parent).getShell();

		final Shell wbwShell;

		int styleOverride = getStyleOverride(wbwModel) | rtlStyle;
		if (parentShell == null) {
			int style = styleOverride == -1 ? SWT.SHELL_TRIM | rtlStyle : styleOverride;
			wbwShell = new Shell(display, style);
			wbwModel.getTags().add("topLevel"); //$NON-NLS-1$
		} else {
			int style = SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.CLOSE | rtlStyle;
			if (wbwModel.getTags().contains(IPresentationEngine.WINDOW_TOP_LEVEL)) {
				style = styleOverride == -1 ? style : styleOverride;
				wbwShell = new Shell(display, style);
			} else if (isDetachedWindowTopLevel()) {
				// A top level window has its own task bar entry: it can be minimized
				style = styleOverride == -1 ? style | SWT.MIN : styleOverride;
				wbwShell = new Shell(display, style);
				wbwShell.setData(DETACHED_TOP_LEVEL_SHELL, Boolean.TRUE);
			} else {
				style = styleOverride == -1 ? style : styleOverride;
				wbwShell = new Shell(parentShell, style);
			}

			// Prevent ESC from closing the DW
			wbwShell.addTraverseListener(e -> {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					e.doit = false;
				}
			});
		}

		wbwShell.setBackgroundMode(SWT.INHERIT_DEFAULT);

		Rectangle modelBounds = wbwShell.getBounds();
		if (wbwModel.isSetX()) {
			modelBounds.x = wbwModel.getX();
		}
		if (wbwModel.isSetY()) {
			modelBounds.y = wbwModel.getY();
		}
		if (wbwModel.isSetHeight()) {
			modelBounds.height = wbwModel.getHeight();
		}
		if (wbwModel.isSetWidth()) {
			modelBounds.width = wbwModel.getWidth();
		}

		// Force the shell onto the display if it would be invisible otherwise
		Display display = Display.getCurrent();
		Monitor closestMonitor = Util.getClosestMonitor(display, Geometry.centerPoint(modelBounds));
		Rectangle displayBounds = closestMonitor.getClientArea();
		if (!modelBounds.intersects(displayBounds)) {
			Geometry.moveInside(modelBounds, displayBounds);
		}
		wbwShell.setBounds(modelBounds);

		setCSSInfo(wbwModel, wbwShell);

		// set up context
		IEclipseContext localContext = getContext(wbwModel);

		// We need to retrieve specific CSS properties for our layout.
		CSSEngineHelper helper = new CSSEngineHelper(localContext, wbwShell);
		TrimmedPartLayout tl = new TrimmedPartLayout(wbwShell);
		tl.gutterTop = helper.getMarginTop(0);
		tl.gutterBottom = helper.getMarginBottom(0);
		tl.gutterLeft = helper.getMarginLeft(0);
		tl.gutterRight = helper.getMarginRight(0);

		wbwShell.setLayout(tl);
		newWidget = wbwShell;
		bindWidget(element, newWidget);

		// Add the shell into the WBW's context
		localContext.set(Shell.class, wbwShell);
		localContext.set(E4Workbench.LOCAL_ACTIVE_SHELL, wbwShell);
		setCloseHandler(wbwModel);
		localContext.set(IShellProvider.class, () -> wbwShell);
		final PartServiceSaveHandler saveHandler = new PartServiceSaveHandler() {
			@Override
			public Save promptToSave(MPart dirtyPart) {
				Shell shell = (Shell) context.get(IServiceConstants.ACTIVE_SHELL);
				Object[] elements = promptForSave(shell, List.of(dirtyPart));
				if (elements == null) {
					return Save.CANCEL;
				}
				return elements.length == 0 ? Save.NO : Save.YES;
			}

			@Override
			public Save[] promptToSave(Collection<MPart> dirtyParts) {
				List<MPart> parts = new ArrayList<>(dirtyParts);
				Shell shell = (Shell) context
						.get(IServiceConstants.ACTIVE_SHELL);
				Save[] response = new Save[dirtyParts.size()];
				Object[] elements = promptForSave(shell, parts);
				if (elements == null) {
					Arrays.fill(response, Save.CANCEL);
				} else {
					Arrays.fill(response, Save.NO);
					for (Object element : elements) {
						response[parts.indexOf(element)] = Save.YES;
					}
				}
				return response;
			}
		};
		saveHandler.logger = logger;
		localContext.set(ISaveHandler.class, saveHandler);

		if (wbwModel.getLabel() != null) {
			wbwShell.setText(wbwModel.getLocalizedLabel());
		} else {
			updateDetachedWindowTitle(wbwModel, wbwShell);
		}

		Image windowImage = getImage(wbwModel);
		if (windowImage != null) {
			wbwShell.setImage(windowImage);
		} else {
			// TODO: This should be added to the model, see bug 308494
			// it allows for a range of icon sizes that the platform gets to
			// choose from
			wbwShell.setImages(Window.getDefaultImages());
		}

		return newWidget;
	}

	private static boolean isDetachedWindowTopLevel() {
		// Use the preferences service so product customization (default scope) is honored
		return Platform.getPreferencesService().getBoolean(
				CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT, DETACHED_WINDOWS_TOP_LEVEL,
				DETACHED_WINDOWS_TOP_LEVEL_DEFAULT, null);
	}

	private static MWindow getWindowOf(MUIElement element) {
		MUIElement current = element;
		while (current != null && !(current instanceof MWindow)) {
			current = current.getParent();
		}
		return (MWindow) current;
	}

	private void updateDetachedWindowTitle(MWindow window) {
		if (window != null && window.getRenderer() == this && window.getLabel() == null
				&& window.getWidget() instanceof Shell shell) {
			updateDetachedWindowTitle(window, shell);
		}
	}

	/**
	 * Sets the title of a detached window made top level by the
	 * {@link #DETACHED_WINDOWS_TOP_LEVEL} preference, when it has no label, to the
	 * label of the part it shows. The detached windows usually have no label: a
	 * child shell has no task bar entry, but a top level shell has one.
	 */
	private static void updateDetachedWindowTitle(MWindow window, Shell shell) {
		if (shell.isDisposed() || shell.getData(DETACHED_TOP_LEVEL_SHELL) == null) {
			return;
		}
		MPart part = getShownPart(window);
		String title = part == null || part.getLocalizedLabel() == null ? "" : part.getLocalizedLabel(); //$NON-NLS-1$
		if (!title.equals(shell.getText())) {
			shell.setText(title);
		}
	}

	/**
	 * @return the part shown by the container: its selected element or, if none,
	 *         its first visible child
	 */
	private static MPart getShownPart(MElementContainer<?> container) {
		MUIElement shown = container.getSelectedElement();
		if (shown == null) {
			for (MUIElement child : container.getChildren()) {
				if (child.isToBeRendered() && child.isVisible()) {
					shown = child;
					break;
				}
			}
		}
		if (shown instanceof MPlaceholder placeholder) {
			shown = placeholder.getRef();
		}
		if (shown instanceof MPart part) {
			return part;
		}
		if (shown instanceof MElementContainer<?> childContainer) {
			return getShownPart(childContainer);
		}
		return null;
	}

	private void setCloseHandler(MWindow window) {
		IEclipseContext context = window.getContext();
		// no direct model parent, must be a detached window
		if (window.getParent() == null) {
			context.set(IWindowCloseHandler.class,
					this::closeDetachedWindow);
		} else {
			context.set(IWindowCloseHandler.class,
					window1 -> {
						EPartService partService = window1.getContext().get(EPartService.class);
						return partService.saveAll(true);
					});
		}
	}

	@Override
	public Image getImage(MUILabel element) {
		Image image = super.getImage(element);
		if (image == null && element instanceof MWindow) {
			// Detached windows should take their image from parent window
			MWindow parent = modelService.getTopLevelWindowFor((MWindow) element);
			if (parent != null && parent != element) {
				image = getImage(parent);
			}
		}
		return image;
	}

	@Override
	public void hookControllerLogic(MUIElement me) {
		super.hookControllerLogic(me);

		Widget widget = (Widget) me.getWidget();

		if (widget instanceof final Shell shell && me instanceof final MWindow w) {
			shell.addControlListener(new ControlListener() {
				@Override
				public void controlResized(ControlEvent e) {
					// Don't store the maximized size in the model
					if (shell.getMaximized()) {
						return;
					}

					try {
						ignoreSizeChanges = true;
						w.setWidth(shell.getSize().x);
						w.setHeight(shell.getSize().y);
					} finally {
						ignoreSizeChanges = false;
					}
				}

				@Override
				public void controlMoved(ControlEvent e) {
					// Don't store the maximized size in the model
					if (shell.getMaximized()) {
						return;
					}

					try {
						ignoreSizeChanges = true;
						w.setX(shell.getLocation().x);
						w.setY(shell.getLocation().y);
					} finally {
						ignoreSizeChanges = false;
					}
				}
			});

			shell.addShellListener(ShellListener.shellClosedAdapter(e -> {
				// override the shell close event
				e.doit = false;
				MWindow window = (MWindow) e.widget.getData(OWNING_ME);
				IWindowCloseHandler closeHandler = window.getContext().get(IWindowCloseHandler.class);
				// if there's no handler or the handler permits the close
				// request, clean-up as necessary
				if (closeHandler == null || closeHandler.close(window)) {
					cleanUp(window);
				}
			}));
			shell.addListener(SWT.Activate, event -> {
				MUIElement parentME = w.getParent();
				if (parentME instanceof MApplication app) {
					app.setSelectedElement(w);
					w.getContext().activate();
				} else if (parentME == null) {
					parentME = modelService.getContainer(w);
					if (parentME instanceof MContext) {
						w.getContext().activate();
					}
				}
				updateNonFocusState(SWT.Activate, w);
			});

			shell.addListener(SWT.Deactivate, event -> updateNonFocusState(SWT.Deactivate, w));
		}
	}

	private void updateNonFocusState(int event, MWindow win) {
		MPerspective perspective = modelService.getActivePerspective(win);
		if (perspective == null) {
			return;
		}

		List<MPartStack> stacks = modelService.findElements(perspective, null, MPartStack.class,
				singletonList(CSSConstants.CSS_ACTIVE_CLASS));
		if (stacks.isEmpty()) {
			return;
		}

		MPartStack stack = stacks.get(0);
		int tagsCount = stack.getTags().size();
		boolean hasNonFocusTag = stack.getTags().contains(
				CSSConstants.CSS_NO_FOCUS_CLASS);

		if (event == SWT.Activate && hasNonFocusTag) {
			stack.getTags().remove(CSSConstants.CSS_NO_FOCUS_CLASS);
		} else if (event == SWT.Deactivate && !hasNonFocusTag) {
			stack.getTags().add(CSSConstants.CSS_NO_FOCUS_CLASS);
		}
		if (tagsCount != stack.getTags().size()) {
			setCSSInfo(stack, stack.getWidget());
		}
	}

	private void cleanUp(MWindow window) {
		MUIElement parent = modelService.getContainer(window);
		if (parent instanceof MApplication application) {
			List<MWindow> children = application.getChildren();
			if (children.size() > 1) {
				// not the last window, destroy and remove
				window.setToBeRendered(false);
				children.remove(window);
			} else {
				// last window, just destroy without changing the model
				engine.removeGui(window);
			}
		} else if (parent != null) {
			window.setToBeRendered(false);
			// this is a detached window, check for parts
			if (modelService.findElements(window, null, MPart.class)
					.isEmpty()) {
				// if no parts, remove it
				if (parent instanceof MWindow) {
					((MWindow) parent).getWindows().remove(window);
				} else if (parent instanceof MPerspective) {
					((MPerspective) parent).getWindows().remove(window);
				}
			}
		}
	}

	/*
	 * Processing the contents of a Workbench window has to take into account
	 * that there may be trim elements contained in its child list. Since the
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		if (!(((MUIElement) me) instanceof MWindow wbwModel)) {
			return;
		}
		super.processContents(me);

		// Populate the main menu
		IPresentationEngine renderer = context.get(IPresentationEngine.class);
		if (wbwModel.getMainMenu() != null) {
			renderer.createGui(wbwModel.getMainMenu(), me.getWidget(), null);
			Shell shell = (Shell) me.getWidget();
			shell.setMenuBar((Menu) wbwModel.getMainMenu().getWidget());
		}

		// create Detached Windows
		for (MWindow dw : wbwModel.getWindows()) {
			renderer.createGui(dw, me.getWidget(), wbwModel.getContext());
		}

		// Populate the trim (if any)
		if (wbwModel instanceof MTrimmedWindow tWindow) {
			Shell shell = (Shell) wbwModel.getWidget();
			List<MTrimBar> trimBars = new ArrayList<>(
					tWindow.getTrimBars());
			for (MTrimBar trimBar : trimBars) {
				renderer.createGui(trimBar, shell, wbwModel.getContext());
				// bug 387161: hack around that createGui(e, parent, context)
				// does not reparent the element widget to the
				// limbo shell wheb visible=false
				if (!trimBar.isVisible()) {
					trimBar.setVisible(true);
					trimBar.setVisible(false);
				}
			}
		}
	}

	@Override
	public Object getUIContainer(MUIElement element) {
		MUIElement parent = element.getParent();
		if (parent == null) {
			// might be a detached window
			parent = modelService.getContainer(element);
			return parent == null ? null : parent.getWidget();
		}

		Composite shellComp = (Composite) element.getParent().getWidget();
		TrimmedPartLayout tpl = (TrimmedPartLayout) shellComp.getLayout();
		return tpl.clientArea;
	}

	@Override
	public void postProcess(MUIElement shellME) {
		super.postProcess(shellME);

		Shell shell = (Shell) shellME.getWidget();

		// Capture the max/min state
		final MUIElement disposeME = shellME;
		shell.addDisposeListener(e -> {
			Shell shell1 = (Shell) e.widget;
			if (disposeME != null) {
				disposeME.getTags().remove(ShellMinimizedTag);
				disposeME.getTags().remove(ShellMaximizedTag);
				if (shell1.getMinimized()) {
					disposeME.getTags().add(ShellMinimizedTag);
				}
				if (shell1.getMaximized()) {
					disposeME.getTags().add(ShellMaximizedTag);
				}
			}
		});

		try {
			// Apply the correct shell state
			if (shellME.getTags().contains(ShellMaximizedTag)) {
				shell.setMaximized(true);
			} else if (shellME.getTags().contains(ShellMinimizedTag)) {
				shell.setMinimized(true);
			}

			forceLayout(shell); // See Bug 375576
		} finally {
			if (shellME.isVisible()) {
				shell.open();
			} else {
				shell.setVisible(false);
			}
		}
	}

	private Object[] promptForSave(Shell parentShell, List<MPart> saveableParts) {
		if (saveableParts.size() == 1) {
			MPart part = saveableParts.get(0);
			String[] buttons;
			buttons = new String[] { SWTRenderersMessages.choosePartsToSave_Button_Save,
					SWTRenderersMessages.choosePartsToSave_Button_Dont_Save,
					SWTRenderersMessages.choosePartsToSave_Button_Cancel };

			String message = NLS.bind(SWTRenderersMessages.saveSingleChangesQuestionTitle, part.getLabel());
			MessageDialog dialog = new MessageDialog(parentShell, SWTRenderersMessages.choosePartsToSaveTitle, null,
					message, MessageDialog.NONE, 0, buttons) {
				@Override
				protected int getShellStyle() {
					return SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.SHEET
							| getDefaultOrientation();
				}
			};
			switch (dialog.open()) {
			case 0:
				return new Object[] { part };
			case 1:
				return new Object[0];
			default:
				return null;
			}
		}
		SaveablePartPromptDialog dialog = new SaveablePartPromptDialog(parentShell, saveableParts);
		if (dialog.open() == Window.CANCEL) {
			return null;
		}
		return dialog.getCheckedElements();
	}

	private void applyDialogStyles(Control control) {
		IStylingEngine engine = context.get(IStylingEngine.class);
		if (engine != null) {
			Shell shell = control.getShell();
			if (shell.getBackgroundMode() == SWT.INHERIT_NONE) {
				shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
			}

			engine.style(shell);
		}
	}

	class SaveablePartPromptDialog extends Dialog {

		private final Collection<MPart> collection;

		private CheckboxTableViewer tableViewer;

		private Object[] checkedElements = new Object[0];

		SaveablePartPromptDialog(Shell shell, Collection<MPart> collection) {
			super(shell);
			this.collection = collection;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(SWTRenderersMessages.choosePartsToSaveTitle);
		}


		@Override
		protected Control createDialogArea(Composite parent) {
			parent = (Composite) super.createDialogArea(parent);

			Label label = new Label(parent, SWT.LEAD);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label.setText(SWTRenderersMessages.choosePartsToSave);

			tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.heightHint = 250;
			data.widthHint = 300;
			tableViewer.getControl().setLayoutData(data);
			tableViewer.setLabelProvider(createTextProvider(element -> ((MPart) element).getLocalizedLabel()));

			tableViewer.setContentProvider(ArrayContentProvider.getInstance());
			tableViewer.setInput(collection);
			tableViewer.setAllChecked(true);

			return parent;
		}

		@Override
		public void create() {
			super.create();
			applyDialogStyles(getShell());
		}

		@Override
		protected void okPressed() {
			checkedElements = tableViewer.getCheckedElements();
			super.okPressed();
		}

		public Object[] getCheckedElements() {
			return checkedElements;
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

	}

	protected static class ThemeDefinitionChangedHandler {
		protected Set<Resource> unusedResources = new HashSet<>();

		public void handleEvent(Event event) {
			Object element = event.getProperty(IEventBroker.DATA);

			if (!(element instanceof MApplication)) {
				return;
			}

			Set<CSSEngine> engines = new HashSet<>();

			// In theory we can have multiple engines since API allows it.
			// It doesn't hurt to be prepared for such case
			for (MWindow window : ((MApplication) element).getChildren()) {
				CSSEngine engine = getEngine(window);
				if (engine != null) {
					engines.add(engine);
				}
			}

			for (CSSEngine engine : engines) {
				for (Object resource : removeResources(engine.getResourcesRegistry())) {
					if (resource instanceof Resource && !((Resource) resource).isDisposed()) {
						unusedResources.add((Resource) resource);
					}
				}
				engine.reapply();
			}
		}

		protected CSSEngine getEngine(MWindow window) {
			return WidgetElement.getEngine((Widget) window.getWidget());
		}

		protected List<Object> removeResources(IResourcesRegistry registry) {
			if (registry instanceof SWTResourcesRegistry) {
				return ((SWTResourcesRegistry) registry)
						.removeResourcesByKeyTypeAndType(
								ResourceByDefinitionKey.class, Font.class);
			}
			return Collections.emptyList();
		}

		public void dispose() {
			for (Resource resource : unusedResources) {
				if (!resource.isDisposed()) {
					resource.dispose();
				}
			}
			unusedResources.clear();
		}
	}

	private void forceLayout(Shell shell) {
		int i = 0;
		while (shell.isLayoutDeferred()) {
			shell.setLayoutDeferred(false);
			i++;
		}
		while (i > 0) {
			shell.setLayoutDeferred(true);
			i--;
		}
	}
}
