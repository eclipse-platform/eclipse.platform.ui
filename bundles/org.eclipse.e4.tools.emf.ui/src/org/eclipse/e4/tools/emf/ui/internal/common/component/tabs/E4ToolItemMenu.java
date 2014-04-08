/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
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
 *
 */
public class E4ToolItemMenu {

	public static final String SEPARATOR = "~separator~"; //$NON-NLS-1$
	private IEclipseContext context;
	private ArrayList<String> commandIds = new ArrayList<String>();
	private Menu menu;
	private ECommandService commandService;
	private EHandlerService handlerService;
	private ToolItem toolItem;

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
				Rectangle rect = toolItem.getBounds();
				Point pt = new Point(rect.x, rect.y + rect.height);
				pt = parent.toDisplay(pt);
				menu.setLocation(pt.x, pt.y);
				menu.setVisible(true);
				for (MenuItem mi : menu.getItems()) {
					if (mi.getData() instanceof ParameterizedCommand) {
						ParameterizedCommand cmd = (ParameterizedCommand) mi.getData();
						mi.setEnabled(handlerService.canExecute(cmd));
					}
				}
				// }
			};
		});
	}

	public void addCommands(Collection<String> commandIds) {
		this.commandIds.addAll(commandIds);

		for (String id : commandIds) {
			if (id.equals(SEPARATOR)) {
				new MenuItem(menu, SWT.SEPARATOR);
			} else {
				ParameterizedCommand myCommand = commandService.createCommand(id, null);
				if (myCommand != null) {
					final MenuItem item = new MenuItem(menu, SWT.PUSH);
					try {
						item.setText(myCommand.getName());
					} catch (NotDefinedException e1) {
						item.setText(id);
						e1.printStackTrace();
					}
					item.setData(myCommand);
					item.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							ParameterizedCommand cmd = (ParameterizedCommand) item.getData();
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
