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
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.In;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.model.workbench.MWorkbenchWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * Render a Window or Workbench Window.
 */
public class WBPartFactory extends SWTPartFactory {
	@In
	Logger logger;

	public WBPartFactory() {
		super();
	}

	public Object createWidget(MPart<?> part) {
		final Widget newWidget;

		if (part instanceof MWindow<?>) {
			IEclipseContext parentContext = getContextForParent(part);
			Shell wbwShell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
			wbwShell.setLayout(new FillLayout());
			newWidget = wbwShell;
			bindWidget(part, newWidget);

			// set up context
			IEclipseContext localContext = part.getContext();
			localContext
					.set(IContextConstants.DEBUG_STRING, "MWorkbenchWindow"); //$NON-NLS-1$
			parentContext.set(IServiceConstants.ACTIVE_CHILD, localContext);

			// Add the shell into the WBW's context
			localContext.set(Shell.class.getName(), wbwShell);
			localContext.set(Workbench.LOCAL_ACTIVE_SHELL, wbwShell);

			if (part instanceof MWorkbenchWindow) {
				// TrimmedLayout tl = new TrimmedLayout(wbwShell);
				// wbwShell.setLayout(tl);
				// localContext.set(MWorkbenchWindow.class.getName(), part);
			} else {
				wbwShell.setLayout(new FillLayout());
			}
			if (((MWindow<?>) part).getName() != null)
				wbwShell.setText(((MWindow<?>) part).getName());
			String uri = ((MWindow<?>) part).getIconURI();
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
		} else {
			newWidget = null;
		}

		return newWidget;
	}

	@Override
	public void hookControllerLogic(MPart<?> me) {
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

		// Set up the text binding...perhaps should catch exceptions?
		IObservableValue emfTextObs = EMFObservables.observeValue(me,
				ApplicationPackage.Literals.MITEM__NAME);
		if (widget instanceof Control && !(widget instanceof Composite)) {
			ISWTObservableValue uiTextObs = SWTObservables
					.observeText((Control) widget);
			dbc.bindValue(uiTextObs, emfTextObs, null, null);
		} else if (widget instanceof org.eclipse.swt.widgets.Item) {
			ISWTObservableValue uiTextObs = SWTObservables.observeText(widget);
			dbc.bindValue(uiTextObs, emfTextObs, null, null);
		}

		// Set up the tool tip binding...perhaps should catch exceptions?
		IObservableValue emfTTipObs = EMFObservables.observeValue(me,
				ApplicationPackage.Literals.MITEM__TOOLTIP);
		if (widget instanceof Control) {
			ISWTObservableValue uiTTipObs = SWTObservables
					.observeTooltipText((Control) widget);
			dbc.bindValue(uiTTipObs, emfTTipObs, null, null);
		} else if (widget instanceof org.eclipse.swt.widgets.Item
				&& !(widget instanceof MenuItem)) {
			ISWTObservableValue uiTTipObs = SWTObservables
					.observeTooltipText(widget);
			dbc.bindValue(uiTTipObs, emfTTipObs, null, null);
		}

		// Handle generic image changes
		((EObject) me).eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				MPart<?> sm = (MPart<?>) msg.getNotifier();
				if (ApplicationPackage.Literals.MITEM__ICON_URI.equals(msg
						.getFeature())) {
					Widget widget = (Widget) sm.getWidget();
					if (widget instanceof org.eclipse.swt.widgets.Item) {
						org.eclipse.swt.widgets.Item item = (org.eclipse.swt.widgets.Item) widget;
						Image image = getImage(sm);
						if (image != null)
							item.setImage(image);
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.SWTPartFactory#processContents
	 * (org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public <P extends MPart<?>> void processContents(MPart<P> me) {
		super.processContents(me);

		// Populate the main menu
		if (me.getMenu() != null) {
			createMenu(me, me.getWidget(), me.getMenu());
		}
	}
}
