/*******************************************************************************
 * Copyright (c) 2006, 2009 Klaus Wenger, Wind River Systems, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Klaus Wenger - initial API and implementation
 * Markus Schorn - cleanup and conversion to inner part of the search plugin.
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.List;

import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.activities.WorkbenchActivityHelper;


public class OpenSearchDialogPageAction implements IWorkbenchWindowPulldownDelegate2 {

	private IWorkbenchWindow fWorkbenchWindow;
	private OpenSearchDialogAction fOpenSearchDialogAction;

	@Override
	public Menu getMenu(Menu parent) {
		Menu menu= new Menu(parent);
		fillMenu(menu);
		return menu;
	}

	@Override
	public Menu getMenu(Control parent) {
		Menu menu= new Menu(parent);
		fillMenu(menu);
		return menu;
	}

	@Override
	public void dispose() {
		if (fOpenSearchDialogAction != null) {
			fOpenSearchDialogAction.dispose();
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
		fWorkbenchWindow= window;
	}

	@Override
	public void run(IAction action) {
		if (fOpenSearchDialogAction == null) {
			fOpenSearchDialogAction= new OpenSearchDialogAction();
		}
		fOpenSearchDialogAction.run(action);
	}

	@Override
	public void selectionChanged(IAction action, ISelection sel) {
		// Empty
	}

	private void fillMenu(final Menu localMenu) {
		List<SearchPageDescriptor> pageDescriptors= SearchPlugin.getDefault().getSearchPageDescriptors();
		int accelerator= 1;
		for (SearchPageDescriptor desc : pageDescriptors) {
			if (!WorkbenchActivityHelper.filterItem(desc) && desc.isEnabled()) {
				SearchPageAction action= new SearchPageAction(fWorkbenchWindow, desc);
				addToMenu(localMenu, action, accelerator++);
			}
		}
		localMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuHidden(MenuEvent e) {
				e.display.asyncExec(new Runnable() {
					@Override
					public void run() {
						localMenu.dispose();
					}
				});
			}
		});

	}

	private void addToMenu(Menu localMenu, IAction action, int accelerator) {
		StringBuilder label= new StringBuilder();
		if (accelerator >= 0 && accelerator < 10) {
			//add the numerical accelerator
			label.append('&');
			label.append(accelerator);
			label.append(' ');
		}
		label.append(action.getText());
		action.setText(label.toString());
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(localMenu, -1);
	}

	private static final class SearchPageAction extends Action {
		private final OpenSearchDialogAction fOpenSearchDialogAction;

		public SearchPageAction(IWorkbenchWindow workbenchWindow, SearchPageDescriptor pageDescriptor) {
			super();
			fOpenSearchDialogAction= new OpenSearchDialogAction(workbenchWindow, pageDescriptor.getId());
			init(pageDescriptor);
		}

		private void init(SearchPageDescriptor pageDesc) {
			setText(pageDesc.getLabel());
			setToolTipText(pageDesc.getLabel());
			ImageDescriptor imageDescriptor= pageDesc.getImage();
			if (imageDescriptor != null) {
				setImageDescriptor(imageDescriptor);
			}
		}

		@Override
		public void run() {
			fOpenSearchDialogAction.run(this);
		}

	}
}
