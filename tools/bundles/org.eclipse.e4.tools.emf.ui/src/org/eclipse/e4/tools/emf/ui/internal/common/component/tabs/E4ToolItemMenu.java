/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A tool item that displays commands in a drop down menu. The enabled state is
 * set by querying the handler when the menu is opened.
 *
 * @author Steven Spungin
 */
public class E4ToolItemMenu {

	public static final String SEPARATOR = "~separator~"; //$NON-NLS-1$
	private final IEclipseContext context;
	private final ArrayList<String> commandIds = new ArrayList<>();
	private final Menu menu;
	private final ECommandService commandService;
	private final EHandlerService handlerService;
	private final ToolItem toolItem;

	public E4ToolItemMenu(final ToolBar parent, IEclipseContext context) {

		toolItem = new ToolItem(parent, SWT.DROP_DOWN);
		this.context = context;
		commandService = this.context.get(ECommandService.class);
		handlerService = this.context.get(EHandlerService.class);

		menu = new Menu(parent.getShell(), SWT.POP_UP);

		toolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// if (event.detail == SWT.ARROW) {
				final Rectangle rect = toolItem.getBounds();
				Point pt = new Point(rect.x, rect.y + rect.height);
				pt = parent.toDisplay(pt);
				menu.setLocation(pt.x, pt.y);
				menu.setVisible(true);
				for (final MenuItem mi : menu.getItems()) {
					if (mi.getData() instanceof ParameterizedCommand) {
						final ParameterizedCommand cmd = (ParameterizedCommand) mi.getData();
						mi.setEnabled(handlerService.canExecute(cmd));
					}
				}
				// }
			}
		});
	}

	public void addCommands(Collection<String> commandIds) {
		this.commandIds.addAll(commandIds);

		for (final String id : commandIds) {
			if (id.equals(SEPARATOR)) {
				new MenuItem(menu, SWT.SEPARATOR);
			} else {
				final ParameterizedCommand myCommand = commandService.createCommand(id, null);
				if (myCommand != null) {
					final MenuItem item = new MenuItem(menu, SWT.PUSH);
					try {
						item.setText(myCommand.getName());
					} catch (final NotDefinedException e1) {
						item.setText(id);
						e1.printStackTrace();
					}
					item.setData(myCommand);
					item.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							final ParameterizedCommand cmd = (ParameterizedCommand) item.getData();
							handlerService.executeHandler(cmd);
						}

					});
				}
			}
		}
	}

	public ToolItem getToolItem() {
		return toolItem;
	}
}
