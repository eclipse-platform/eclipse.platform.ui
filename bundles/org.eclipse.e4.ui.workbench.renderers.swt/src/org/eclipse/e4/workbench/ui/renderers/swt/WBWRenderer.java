/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MSaveablePart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.IUIEvents;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Render a Window or Workbench Window.
 */
public class WBWRenderer extends SWTPartRenderer {
	@Inject
	Logger logger;

	public WBWRenderer() {
		super();
	}

	@PostConstruct
	public void init(IEventBroker eventBroker) {
		EventHandler shellUpdater = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MMenuItem
				Object objElement = event
						.getProperty(IUIEvents.EventTags.Element);
				if (!(event.getProperty(IUIEvents.EventTags.Element) instanceof MWindow))
					return;

				// Is this listener interested ?
				MWindow windowModel = (MWindow) objElement;
				if (windowModel.getFactory() != this)
					return;

				// No widget == nothing to update
				Shell theShell = (Shell) windowModel.getWidget();
				if (theShell == null)
					return;

				String attName = (String) event
						.getProperty(IUIEvents.EventTags.AttName);

				if (IUIEvents.UIItem.Name.equals(attName)) {
					String newTitle = (String) event
							.getProperty(IUIEvents.EventTags.NewValue);
					theShell.setText(newTitle);
				} else if (IUIEvents.UIItem.IconURI.equals(attName)) {
					theShell.setImage(getImage(windowModel));
				} else if (IUIEvents.UIItem.IconURI.equals(attName)) {
					String newTTip = (String) event
							.getProperty(IUIEvents.EventTags.NewValue);
					theShell.setToolTipText(newTTip);
				}
			}
		};

		eventBroker.subscribe(IUIEvents.UIItem.Topic, shellUpdater);
	}

	public Object createWidget(MUIElement element, Object parent) {
		final Widget newWidget;

		if (!(element instanceof MWindow)
				|| (parent != null && !(parent instanceof Shell)))
			return null;

		MWindow wbwModel = (MWindow) element;

		Shell parentShell = (Shell) parent;

		IEclipseContext parentContext = getContextForParent(element);
		Shell wbwShell;
		if (parentShell == null) {
			wbwShell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
		} else {
			wbwShell = new Shell(parentShell, SWT.SHELL_TRIM);
			wbwShell.setLocation(wbwModel.getX(), wbwModel.getY());
			wbwShell.setSize(wbwModel.getWidth(), wbwModel.getHeight());
		}
		wbwShell.setVisible(true);

		wbwShell.setLayout(new FillLayout());
		newWidget = wbwShell;
		bindWidget(element, newWidget);

		// set up context
		IEclipseContext localContext = getContext(wbwModel);
		localContext.set(IContextConstants.DEBUG_STRING, "MWindow"); //$NON-NLS-1$
		parentContext.set(IContextConstants.ACTIVE_CHILD, localContext);

		// Add the shell into the WBW's context
		localContext.set(Shell.class.getName(), wbwShell);
		localContext.set(Workbench.LOCAL_ACTIVE_SHELL, wbwShell);

		if (element instanceof MWindow) {
			// TrimmedLayout tl = new TrimmedLayout(wbwShell);
			// wbwShell.setLayout(tl);
			// localContext.set(MWindow.class.getName(), part);
		} else {
			wbwShell.setLayout(new FillLayout());
		}
		if (wbwModel.getName() != null)
			wbwShell.setText(wbwModel.getName());
		String uri = wbwModel.getIconURI();
		if (uri != null) {
			try {
				Image image = ImageDescriptor.createFromURL(new URL(uri))
						.createImage();
				wbwShell.setImage(image);
			} catch (MalformedURLException e) {
				// invalid image in model, so don't set an image
				if (logger != null)
					logger.error(e);
			}
		}

		return newWidget;
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
					w.setWidth(shell.getSize().x);
					w.setHeight(shell.getSize().y);
				}

				public void controlMoved(ControlEvent e) {
					w.setX(shell.getLocation().x);
					w.setY(shell.getLocation().y);
				}
			});

			shell.addShellListener(new ShellAdapter() {
				public void shellClosed(ShellEvent e) {
					e.doit = promptForSave(shell, w);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.SWTPartFactory#processContents
	 * (org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		super.processContents(me);

		if (!(((MUIElement) me) instanceof MWindow))
			return;

		MWindow wbwModel = (MWindow) ((MUIElement) me);
		// Populate the main menu
		if (wbwModel.getMainMenu() != null) {
			IPresentationEngine renderer = (IPresentationEngine) context
					.get(IPresentationEngine.class.getName());
			renderer.createGui(wbwModel.getMainMenu(), me.getWidget());
			Shell shell = (Shell) me.getWidget();
			shell.setMenuBar((Menu) wbwModel.getMainMenu().getWidget());
			// createMenu(me, me.getWidget(), wbwModel.getMainMenu());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.PartFactory#postProcess(org.eclipse
	 * .e4.ui.model.application.MPart)
	 */
	@Override
	public void postProcess(MUIElement childME) {
		super.postProcess(childME);

		Shell shell = (Shell) childME.getWidget();
		shell.layout(true);
	}

	private List<MSaveablePart> collectSaveableParts(
			MElementContainer<?> window, List<MSaveablePart> saveableParts) {
		for (Object element : window.getChildren()) {
			if (element instanceof MSaveablePart) {
				MSaveablePart part = (MSaveablePart) element;
				if (part.isDirty()) {
					Object clientObject = part.getObject();
					if (clientObject != null) {
						Boolean saveOnCloseNeeded = (Boolean) ContextInjectionFactory
								.invoke(
										clientObject,
										"isSaveOnCloseNeeded", null, Boolean.TRUE); //$NON-NLS-1$
						if (saveOnCloseNeeded.booleanValue()) {
							saveableParts.add(part);
						}
					}
				}
			}

			if (element instanceof MElementContainer<?>) {
				collectSaveableParts((MElementContainer<?>) element,
						saveableParts);
			}
		}
		return saveableParts;
	}

	private boolean promptForSave(Shell parentShell, MElementContainer<?> window) {
		List<MSaveablePart> saveableParts = collectSaveableParts(window,
				new ArrayList<MSaveablePart>());
		if (!saveableParts.isEmpty()) {
			SaveablePartPromptDialog dialog = new SaveablePartPromptDialog(
					parentShell, saveableParts);
			if (dialog.open() == Window.CANCEL) {
				return false;
			}

			for (Object element : dialog.getCheckedElements()) {
				MSaveablePart part = (MSaveablePart) element;
				Object clientObject = part.getObject();
				ContextInjectionFactory.invoke(clientObject, "doSave", //$NON-NLS-1$
						context, null);
			}
		}

		return true;
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

		private Collection<?> collection;

		private CheckboxTableViewer tableViewer;

		private Object[] checkedElements = new Object[0];

		SaveablePartPromptDialog(Shell shell, Collection<?> collection) {
			super(shell);
			this.collection = collection;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			parent = (Composite) super.createDialogArea(parent);

			Label label = new Label(parent, SWT.LEAD);
			label
					.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
							false));
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
					return ((MSaveablePart) element).getName();
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
