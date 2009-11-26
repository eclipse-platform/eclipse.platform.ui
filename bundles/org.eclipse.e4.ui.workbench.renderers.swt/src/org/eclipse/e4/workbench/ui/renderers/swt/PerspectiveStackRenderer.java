/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui.renderers.swt;

import javax.inject.Inject;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.annotations.PreDestroy;
import org.eclipse.e4.ui.model.application.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.ui.internal.IUIEvents;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 *
 */
public class PerspectiveStackRenderer extends LazyStackRenderer {

	@Inject
	IEventBroker eventBroker;
	private EventHandler showPerspectiveHandler;

	@PostConstruct
	public void init() {
		super.init(eventBroker);

		showPerspectiveHandler = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MMenuItem
				if (!(event.getProperty(IUIEvents.EventTags.Element) instanceof MPerspectiveStack))
					return;

				MPerspectiveStack ps = (MPerspectiveStack) event
						.getProperty(IUIEvents.EventTags.Element);

				String attName = (String) event
						.getProperty(IUIEvents.EventTags.AttName);
				if (IUIEvents.ElementContainer.ActiveChild.equals(attName)) {
					Composite psComp = (Composite) ps.getWidget();
					StackLayout sl = (StackLayout) psComp.getLayout();
					Control ctrl = (Control) ps.getActiveChild().getWidget();
					sl.topControl = ctrl;
					psComp.layout();
				}
			}
		};

		eventBroker.subscribe(IUIEvents.ElementContainer.Topic,
				showPerspectiveHandler);
	}

	@PreDestroy
	public void contextDisposed() {
		eventBroker.unsubscribe(showPerspectiveHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer#createWidget
	 * (org.eclipse.e4.ui.model.application.MUIElement, java.lang.Object)
	 */
	@Override
	public Object createWidget(MUIElement element, Object parent) {
		if (!(element instanceof MPerspectiveStack)
				|| !(parent instanceof Composite))
			return null;

		Composite perspStack = new Composite((Composite) parent, SWT.NONE);
		IStylingEngine stylingEngine = (IStylingEngine) getContext(element)
				.get(IStylingEngine.SERVICE_NAME);
		stylingEngine.setClassname(perspStack, "perspectiveLayout"); //$NON-NLS-1$
		perspStack.setLayout(new StackLayout());

		return perspStack;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.LazyStackRenderer#postProcess
	 * (org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void postProcess(MUIElement element) {
		super.postProcess(element);

		MPerspectiveStack ps = (MPerspectiveStack) element;
		if (ps.getActiveChild() != null
				&& ps.getActiveChild().getWidget() != null) {
			Control ctrl = (Control) ps.getActiveChild().getWidget();
			Composite psComp = (Composite) ps.getWidget();
			StackLayout sl = (StackLayout) psComp.getLayout();
			sl.topControl = ctrl;
			psComp.layout();
		}
	}
}
