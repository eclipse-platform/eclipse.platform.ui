/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.EventUtils;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.osgi.service.event.EventAdmin;

public class FindTargetAction {

	// TBD this is from internal AbstractPartRenderer.OWNING_ME
	// make that API
	private static final String OWNING_ME = "modelElement"; //$NON-NLS-1$

	@Inject
	public EventAdmin eventAdmin;

	private Cursor targetCursor;
	private Cursor displayCursor;

	@Inject
	public FindTargetAction(Display display) {
		targetCursor = new Cursor(display, SWT.CURSOR_CROSS);
	}

	@PreDestroy
	public void dispose() {
		if (targetCursor != null) {
			targetCursor.dispose();
			targetCursor = null;
		}
	}

	@Execute
	public void doFindTarget(MWindow win) {
		final Control windowWidget = (Control) win.getWidget();
		final Display display = windowWidget.getDisplay();

		displayCursor = windowWidget.getCursor();
		windowWidget.setCursor(targetCursor);
		windowWidget.setCapture(true);

		// This filter list is necessary to avoid not-initialized-errors within the
		// actual listener.  The filter approach is required as some platforms (e.g.,
		// MacOS X) don't support setCapture().
		// FIXME: should possible set this up for MouseUp, not MouseDown?
		final LinkedList<Listener> filters = new LinkedList<Listener>();
		filters.add(new Listener() {
			public void handleEvent(Event event) {
				Control control = display.getCursorControl();
				IEclipseContext targetContext = null;
				while (control != null) {
					Object data = control.getData(OWNING_ME);
					if (data instanceof MContext) {
						targetContext = ((MContext) data).getContext();
						if (targetContext != null)
							break;
					}
					control = control.getParent();
				}
				if (targetContext != null) {
					List<WeakContextRef> contexts = new ArrayList<WeakContextRef>();
					while (targetContext != null) {
						contexts.add(new WeakContextRef((EclipseContext) targetContext));
						targetContext = targetContext.getParent();
					}
					Collections.reverse(contexts);
					TreePath path = new TreePath(contexts.toArray());
					EventUtils.send(eventAdmin, ContextsView.SELECT_EVENT, path);
				}
				windowWidget.setCapture(false);
				windowWidget.setCursor(displayCursor);
				for (Listener f : filters) {
					display.removeFilter(SWT.MouseDown, f);
				}
				filters.clear();
			}
		});
		for (Listener f : filters) {
			display.addFilter(SWT.MouseDown, f);
		}
	}

}
