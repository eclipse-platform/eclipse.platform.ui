/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.update.internal.ui.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public abstract class BaseView extends ViewPart {
	private Action showDetailsAction;
	private StructuredViewer viewer;
	
	private static final String KEY_SHOW_DETAILS =
		"BaseView.Popup.ShowDetails";

	private static final String KEY_CONFIRM_DELETE = "ConfirmDelete.title";

	private static final String KEY_CONFIRM_DELETE_MULTIPLE =
		"ConfirmDelete.multiple";

	private static final String KEY_CONFIRM_DELETE_SINGLE =
		"ConfirmDelete.single";
	/**
	 * The constructor.
	 */
	public BaseView() {
	}

	public abstract void initProviders();
	
	protected abstract StructuredViewer createViewer(Composite parent, int styles);
	
	public StructuredViewer getViewer() {
		return viewer;
	}
	
	public Control getControl() {
		return viewer!=null?viewer.getControl():null;
	}

	public void createPartControl(Composite parent) {
		viewer = createViewer(parent, SWT.NULL);
		viewer.setUseHashlookup(true);
		initProviders();
		initDragAndDrop();
		//initRefreshKey();
		//initRenameKey();
		//updateTitle();

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new GroupMarker("additions"));
				BaseView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
		makeActions();

		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					deleteKeyPressed(event.widget);
				}
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});

		fillActionBars(getViewSite().getActionBars());

		getSite().setSelectionProvider(viewer);
		partControlCreated();
	}

	protected void partControlCreated() {
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(showDetailsAction);
	}

	protected void makeActions() {
		showDetailsAction = new Action() {
			public void run() {
				IWorkbenchPage page = UpdateUI.getActivePage();
				try {
					IViewPart part =
						page.showView(UpdatePerspective.ID_DETAILS);
					((DetailsView) part).selectionChanged(
						BaseView.this,
						viewer.getSelection());
				} catch (PartInitException e) {
					UpdateUI.logException(e);
				}
			}
		};
		WorkbenchHelp.setHelp(
			showDetailsAction,
			"org.eclipse.update.ui.BaseViewer_showDetailsAction");
		showDetailsAction.setText(
			UpdateUI.getString(KEY_SHOW_DETAILS));
	}

	protected void initDragAndDrop() {
	}
	
	protected void deleteKeyPressed(Widget widget) {
	}

	protected void handleSelectionChanged(SelectionChangedEvent e) {
	}

	protected void handleDoubleClick(DoubleClickEvent e) {
		showDetailsAction.run();
	}

	protected void handleKeyPressed(KeyEvent e) {
	}

	protected void fillActionBars(IActionBars bars) {
	}

	protected boolean confirmDeletion() {
		IStructuredSelection ssel =
			(IStructuredSelection) viewer.getSelection();
		String title = UpdateUI.getString(KEY_CONFIRM_DELETE);
		String message;

		if (ssel.size() > 1) {
			message =
				UpdateUI.getFormattedMessage(
					KEY_CONFIRM_DELETE_MULTIPLE,
					"" + ssel.size());
		} else {
			Object obj = ssel.getFirstElement().toString();
			message =
				UpdateUI.getFormattedMessage(
					KEY_CONFIRM_DELETE_SINGLE,
					obj.toString());
		}
		return MessageDialog.openConfirm(
			viewer.getControl().getShell(),
			title,
			message);
	}
}
