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
import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.EventUtils;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
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
	public void doFindTarget(IEclipseContext context) {
		MItem item = context.get(MItem.class);
		ToolItem toolItem = (ToolItem) item.getWidget();
		final ToolBar toolBar = toolItem.getParent();
		final Display display = toolItem.getDisplay();

		displayCursor = toolBar.getCursor();
		toolBar.setCursor(targetCursor);
		toolBar.setCapture(true);

		toolBar.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				// nothing
			}

			public void mouseDown(MouseEvent e) {
				// nothing
			}

			public void mouseUp(MouseEvent e) {
				Control control = display.getCursorControl();
				if (toolBar == control) // ignore click on the trigger button
					return;
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
				toolBar.setCapture(false);
				toolBar.removeMouseListener(this);
				toolBar.setCursor(displayCursor);
			}
		});
	}

}
