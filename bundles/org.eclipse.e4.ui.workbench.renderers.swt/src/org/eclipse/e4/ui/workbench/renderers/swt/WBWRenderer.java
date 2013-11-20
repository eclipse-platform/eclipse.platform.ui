/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.PartServiceSaveHandler;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Render a Window or Workbench Window.
 */
public class WBWRenderer extends SWTPartRenderer {

	private static String ShellMinimizedTag = "shellMinimized"; //$NON-NLS-1$
	private static String ShellMaximizedTag = "shellMaximized"; //$NON-NLS-1$

	private class WindowSizeUpdateJob implements Runnable {
		public List<MWindow> windowsToUpdate = new ArrayList<MWindow>();

		public void run() {
			clearSizeUpdate();
			while (!windowsToUpdate.isEmpty()) {
				MWindow window = windowsToUpdate.remove(0);
				Shell shell = (Shell) window.getWidget();
				if (shell == null || shell.isDisposed())
					continue;

				shell.setBounds(window.getX(), window.getY(),
						window.getWidth(), window.getHeight());
			}
		}
	}

	WindowSizeUpdateJob boundsJob;

	void clearSizeUpdate() {
		boundsJob = null;
	}

	boolean ignoreSizeChanges = false;

	@Inject
	Logger logger;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private IPresentationEngine engine;

	private EventHandler topWindowHandler;
	private EventHandler shellUpdater;
	private EventHandler visibilityHandler;
	private EventHandler sizeHandler;

	public WBWRenderer() {
		super();
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
		EPartService partService = (EPartService) window.getContext().get(
				EPartService.class.getName());
		List<MPart> parts = modelService.findElements(window, null,
				MPart.class, null);
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
	public void init() {
		topWindowHandler = new EventHandler() {

			public void handleEvent(Event event) {
				// Ensure that this event is for a MApplication
				if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MApplication))
					return;
				MWindow win = (MWindow) event
						.getProperty(UIEvents.EventTags.NEW_VALUE);
				if ((win == null) || !win.getTags().contains("topLevel")) //$NON-NLS-1$
					return;
				win.setToBeRendered(true);
				if (!(win.getRenderer() == WBWRenderer.this))
					return;
				Shell shell = (Shell) win.getWidget();
				if (shell.getMinimized()) {
					shell.setMinimized(false);
				}
				shell.setActive();
				shell.moveAbove(null);

			}
		};

		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT,
				topWindowHandler);

		shellUpdater = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MMenuItem
				Object objElement = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MWindow))
					return;

				// Is this listener interested ?
				MWindow windowModel = (MWindow) objElement;
				if (windowModel.getRenderer() != WBWRenderer.this)
					return;

				// No widget == nothing to update
				Shell theShell = (Shell) windowModel.getWidget();
				if (theShell == null)
					return;

				String attName = (String) event
						.getProperty(UIEvents.EventTags.ATTNAME);

				if (UIEvents.UILabel.LABEL.equals(attName)) {
					String newTitle = (String) event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					theShell.setText(newTitle);
				} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
					theShell.setImage(getImage(windowModel));
				} else if (UIEvents.UILabel.TOOLTIP.equals(attName)) {
					String newTTip = (String) event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					theShell.setToolTipText(newTTip);
				}
			}
		};

		eventBroker.subscribe(UIEvents.UILabel.TOPIC_ALL, shellUpdater);

		visibilityHandler = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MMenuItem
				Object objElement = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (!(objElement instanceof MWindow))
					return;

				// Is this listener interested ?
				MWindow windowModel = (MWindow) objElement;
				if (windowModel.getRenderer() != WBWRenderer.this)
					return;

				// No widget == nothing to update
				Shell theShell = (Shell) windowModel.getWidget();
				if (theShell == null)
					return;

				String attName = (String) event
						.getProperty(UIEvents.EventTags.ATTNAME);

				if (UIEvents.UIElement.VISIBLE.equals(attName)) {
					boolean isVisible = (Boolean) event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					theShell.setVisible(isVisible);
				}
			}
		};

		eventBroker.subscribe(UIEvents.UIElement.TOPIC_VISIBLE,
				visibilityHandler);

		sizeHandler = new EventHandler() {
			public void handleEvent(Event event) {
				if (ignoreSizeChanges)
					return;

				// Ensure that this event is for a MMenuItem
				Object objElement = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (!(objElement instanceof MWindow)) {
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

				String attName = (String) event
						.getProperty(UIEvents.EventTags.ATTNAME);

				if (UIEvents.Window.X.equals(attName)
						|| UIEvents.Window.Y.equals(attName)
						|| UIEvents.Window.WIDTH.equals(attName)
						|| UIEvents.Window.HEIGHT.equals(attName)) {
					if (boundsJob == null) {
						boundsJob = new WindowSizeUpdateJob();
						boundsJob.windowsToUpdate.add(windowModel);
						theShell.getDisplay().asyncExec(boundsJob);
					} else {
						if (!boundsJob.windowsToUpdate.contains(windowModel))
							boundsJob.windowsToUpdate.add(windowModel);
					}
				}
			}
		};

		eventBroker.subscribe(UIEvents.Window.TOPIC_ALL, sizeHandler);
	}

	@PreDestroy
	public void contextDisposed() {
		eventBroker.unsubscribe(topWindowHandler);
		eventBroker.unsubscribe(shellUpdater);
		eventBroker.unsubscribe(visibilityHandler);
		eventBroker.unsubscribe(sizeHandler);
	}

	public Object createWidget(MUIElement element, Object parent) {
		final Widget newWidget;

		if (!(element instanceof MWindow)
				|| (parent != null && !(parent instanceof Control)))
			return null;

		MWindow wbwModel = (MWindow) element;

		MApplication appModel = wbwModel.getContext().get(MApplication.class);
		Boolean rtlMode = (Boolean) appModel.getTransientData().get(
				E4Workbench.RTL_MODE);
		int rtlStyle = (rtlMode != null && rtlMode.booleanValue()) ? SWT.RIGHT_TO_LEFT
				: 0;

		Shell parentShell = parent == null ? null : ((Control) parent)
				.getShell();

		final Shell wbwShell;
		if (parentShell == null) {
			wbwShell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM
					| rtlStyle);
			wbwModel.getTags().add("topLevel"); //$NON-NLS-1$
		} else if (wbwModel.getTags().contains("dragHost")) { //$NON-NLS-1$
			wbwShell = new Shell(parentShell, SWT.BORDER | rtlStyle);
			wbwShell.setAlpha(110);
		} else {
			wbwShell = new Shell(parentShell, SWT.TITLE | SWT.RESIZE | SWT.MAX
					| SWT.CLOSE | rtlStyle);

			// Prevent ESC from closing the DW
			wbwShell.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_ESCAPE) {
						e.doit = false;
					}
				}
			});
		}

		wbwShell.setBackgroundMode(SWT.INHERIT_DEFAULT);

		Rectangle modelBounds = wbwShell.getBounds();
		if (wbwModel instanceof EObject) {
			EObject wbw = (EObject) wbwModel;
			EClass wbwclass = wbw.eClass();
			// use eIsSet rather than embed sentinel values
			if (wbw.eIsSet(wbwclass.getEStructuralFeature("x"))) { //$NON-NLS-1$
				modelBounds.x = wbwModel.getX();
			}
			if (wbw.eIsSet(wbwclass.getEStructuralFeature("y"))) { //$NON-NLS-1$
				modelBounds.y = wbwModel.getY();
			}
			if (wbw.eIsSet(wbwclass.getEStructuralFeature("height"))) { //$NON-NLS-1$
				modelBounds.height = wbwModel.getHeight();
			}
			if (wbw.eIsSet(wbwclass.getEStructuralFeature("width"))) { //$NON-NLS-1$
				modelBounds.width = wbwModel.getWidth();
			}
		}
		// Force the shell onto the display if it would be invisible otherwise
		Rectangle displayBounds = Display.getCurrent().getBounds();
		if (!modelBounds.intersects(displayBounds)) {
			Rectangle clientArea = Display.getCurrent().getClientArea();
			modelBounds.x = clientArea.x;
			modelBounds.y = clientArea.y;
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
		localContext.set(Shell.class.getName(), wbwShell);
		localContext.set(E4Workbench.LOCAL_ACTIVE_SHELL, wbwShell);
		setCloseHandler(wbwModel);
		localContext.set(IShellProvider.class.getName(), new IShellProvider() {
			public Shell getShell() {
				return wbwShell;
			}
		});
		final PartServiceSaveHandler saveHandler = new PartServiceSaveHandler() {
			public Save promptToSave(MPart dirtyPart) {
				Shell shell = (Shell) context
						.get(IServiceConstants.ACTIVE_SHELL);
				Object[] elements = promptForSave(shell,
						Collections.singleton(dirtyPart));
				if (elements == null) {
					return Save.CANCEL;
				}
				return elements.length == 0 ? Save.NO : Save.YES;
			}

			public Save[] promptToSave(Collection<MPart> dirtyParts) {
				List<MPart> parts = new ArrayList<MPart>(dirtyParts);
				Shell shell = (Shell) context
						.get(IServiceConstants.ACTIVE_SHELL);
				Save[] response = new Save[dirtyParts.size()];
				Object[] elements = promptForSave(shell, parts);
				if (elements == null) {
					Arrays.fill(response, Save.CANCEL);
				} else {
					Arrays.fill(response, Save.NO);
					for (int i = 0; i < elements.length; i++) {
						response[parts.indexOf(elements[i])] = Save.YES;
					}
				}
				return response;
			}
		};
		saveHandler.logger = logger;
		localContext.set(ISaveHandler.class, saveHandler);

		if (wbwModel.getLabel() != null)
			wbwShell.setText(wbwModel.getLocalizedLabel());

		if (wbwModel.getIconURI() != null && wbwModel.getIconURI().length() > 0) {
			wbwShell.setImage(getImage(wbwModel));
		} else {
			// TODO: This should be added to the model, see bug 308494
			// it allows for a range of icon sizes that the platform gets to
			// choose from
			wbwShell.setImages(Window.getDefaultImages());
		}

		return newWidget;
	}

	private void setCloseHandler(MWindow window) {
		IEclipseContext context = window.getContext();
		// no direct model parent, must be a detached window
		if (window.getParent() == null) {
			context.set(IWindowCloseHandler.class.getName(),
					new IWindowCloseHandler() {
						public boolean close(MWindow window) {
							return closeDetachedWindow(window);
						}
					});
		} else {
			context.set(IWindowCloseHandler.class.getName(),
					new IWindowCloseHandler() {
						public boolean close(MWindow window) {
							EPartService partService = (EPartService) window
									.getContext().get(
											EPartService.class.getName());
							return partService.saveAll(true);
						}
					});
		}
	}

	@Override
	public void hookControllerLogic(MUIElement me) {
		super.hookControllerLogic(me);

		Widget widget = (Widget) me.getWidget();

		if (widget instanceof Shell && me instanceof MWindow) {
			final Shell shell = (Shell) widget;
			final MWindow w = (MWindow) me;
			shell.addControlListener(new ControlListener() {
				public void controlResized(ControlEvent e) {
					// Don't store the maximized size in the model
					if (shell.getMaximized())
						return;

					try {
						ignoreSizeChanges = true;
						w.setWidth(shell.getSize().x);
						w.setHeight(shell.getSize().y);
					} finally {
						ignoreSizeChanges = false;
					}
				}

				public void controlMoved(ControlEvent e) {
					// Don't store the maximized size in the model
					if (shell.getMaximized())
						return;

					try {
						ignoreSizeChanges = true;
						w.setX(shell.getLocation().x);
						w.setY(shell.getLocation().y);
					} finally {
						ignoreSizeChanges = false;
					}
				}
			});

			shell.addShellListener(new ShellAdapter() {
				public void shellClosed(ShellEvent e) {
					// override the shell close event
					e.doit = false;
					MWindow window = (MWindow) e.widget.getData(OWNING_ME);
					IWindowCloseHandler closeHandler = (IWindowCloseHandler) window
							.getContext().get(
									IWindowCloseHandler.class.getName());
					// if there's no handler or the handler permits the close
					// request, clean-up as necessary
					if (closeHandler == null || closeHandler.close(window)) {
						cleanUp(window);
					}
				}
			});
			shell.addListener(SWT.Activate, new Listener() {
				public void handleEvent(org.eclipse.swt.widgets.Event event) {
					MUIElement parentME = w.getParent();
					if (parentME instanceof MApplication) {
						MApplication app = (MApplication) parentME;
						app.setSelectedElement(w);
						w.getContext().activate();
					} else if (parentME == null) {
						parentME = (MUIElement) ((EObject) w).eContainer();
						if (parentME instanceof MContext) {
							w.getContext().activate();
						}
					}
				}
			});
		}
	}

	private void cleanUp(MWindow window) {
		Object parent = ((EObject) window).eContainer();
		if (parent instanceof MApplication) {
			MApplication application = (MApplication) parent;
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
			if (modelService.findElements(window, null, MPart.class, null)
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
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartFactory#processContents
	 * (org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		if (!(((MUIElement) me) instanceof MWindow))
			return;
		MWindow wbwModel = (MWindow) ((MUIElement) me);
		super.processContents(me);

		// Populate the main menu
		IPresentationEngine renderer = (IPresentationEngine) context
				.get(IPresentationEngine.class.getName());
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
		if (wbwModel instanceof MTrimmedWindow) {
			Shell shell = (Shell) wbwModel.getWidget();
			MTrimmedWindow tWindow = (MTrimmedWindow) wbwModel;
			List<MTrimBar> trimBars = new ArrayList<MTrimBar>(
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#getUIContainer
	 * (org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public Object getUIContainer(MUIElement element) {
		MUIElement parent = element.getParent();
		if (parent == null) {
			// might be a detached window
			parent = (MUIElement) ((EObject) element).eContainer();
			return parent == null ? null : parent.getWidget();
		}

		Composite shellComp = (Composite) element.getParent().getWidget();
		TrimmedPartLayout tpl = (TrimmedPartLayout) shellComp.getLayout();
		return tpl.clientArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.PartFactory#postProcess(org.eclipse
	 * .e4.ui.model.application.MPart)
	 */
	@Override
	public void postProcess(MUIElement shellME) {
		super.postProcess(shellME);

		Shell shell = (Shell) shellME.getWidget();

		// Capture the max/min state
		final MUIElement disposeME = shellME;
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				Shell shell = (Shell) e.widget;
				if (disposeME != null) {
					disposeME.getTags().remove(ShellMinimizedTag);
					disposeME.getTags().remove(ShellMaximizedTag);
					if (shell.getMinimized())
						disposeME.getTags().add(ShellMinimizedTag);
					if (shell.getMaximized())
						disposeME.getTags().add(ShellMaximizedTag);
				}
			}
		});

		// Apply the correct shell state
		if (shellME.getTags().contains(ShellMaximizedTag))
			shell.setMaximized(true);
		else if (shellME.getTags().contains(ShellMinimizedTag))
			shell.setMinimized(true);

		shell.layout(true);
		if (shellME.isVisible()) {
			shell.open();
		} else {
			shell.setVisible(false);
		}
	}

	private Object[] promptForSave(Shell parentShell,
			Collection<MPart> saveableParts) {
		SaveablePartPromptDialog dialog = new SaveablePartPromptDialog(
				parentShell, saveableParts);
		if (dialog.open() == Window.CANCEL) {
			return null;
		}

		return dialog.getCheckedElements();
	}

	@Inject
	private IEclipseContext context;

	private void applyDialogStyles(Control control) {
		IStylingEngine engine = (IStylingEngine) context
				.get(IStylingEngine.SERVICE_NAME);
		if (engine != null) {
			Shell shell = control.getShell();
			if (shell.getBackgroundMode() == SWT.INHERIT_NONE) {
				shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
			}

			engine.style(shell);
		}
	}

	class SaveablePartPromptDialog extends Dialog {

		private Collection<MPart> collection;

		private CheckboxTableViewer tableViewer;

		private Object[] checkedElements = new Object[0];

		SaveablePartPromptDialog(Shell shell, Collection<MPart> collection) {
			super(shell);
			this.collection = collection;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			parent = (Composite) super.createDialogArea(parent);

			Label label = new Label(parent, SWT.LEAD);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label.setText("Select the parts to save:"); //$NON-NLS-1$

			tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE
					| SWT.BORDER);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.heightHint = 250;
			data.widthHint = 300;
			tableViewer.getControl().setLayoutData(data);
			tableViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((MPart) element).getLocalizedLabel();
				}
			});
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

	}

}
