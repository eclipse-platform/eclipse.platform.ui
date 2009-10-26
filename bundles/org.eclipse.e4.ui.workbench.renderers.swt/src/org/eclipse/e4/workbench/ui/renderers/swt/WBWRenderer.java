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
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.In;
import org.eclipse.e4.core.services.annotations.Inject;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.IUIEvents;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Render a Window or Workbench Window.
 */
public class WBWRenderer extends SWTPartRenderer {
	@In
	Logger logger;

	public WBWRenderer() {
		super();
	}

	@Inject
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
			wbwShell.setVisible(true);
		}

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
}
