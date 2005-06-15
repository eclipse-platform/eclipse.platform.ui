/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.undo.views;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.*;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

public class BoxView extends ViewPart {
	private Canvas paintCanvas;
	private Action action1;
	private Action action2;
    private UndoActionHandler undoAction;
    private RedoActionHandler redoAction;
    private IUndoContext undoContext;
	/**
	 * The constructor.
	 */
	public BoxView() {
		super();
		// create a unique undo context to represent this view's undo history
		undoContext = new ObjectUndoContext(this);
	}

	/**
	 * Create the canvas on which boxes are drawn.
	 */
	public void createPartControl(Composite parent) {
		paintCanvas = new Canvas(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL |
				SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);
        makeActions();
        hookContextMenu();
        createGlobalActionHandlers();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BoxView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(paintCanvas);
		paintCanvas.setMenu(menu);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			paintCanvas.getShell(),
			"Box View",
			message);
	}
	
    private void createGlobalActionHandlers() {
        // set up action handlers that operate on the current context
        undoAction = new UndoActionHandler(this.getSite(), undoContext);
        redoAction = new RedoActionHandler(this.getSite(), undoContext);
        IActionBars actionBars = getViewSite().getActionBars();
        actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
                undoAction);
        actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
                redoAction);
    }

	/**
	 * Passing the focus request to the canvas.
	 */
	public void setFocus() {
		paintCanvas.setFocus();
	}
}