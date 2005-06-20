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

import java.util.List;
import java.util.ArrayList;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
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
	/**
	 * The canvas to paint the boxes on.
	 */
	private Canvas paintCanvas;
	
	/**
	 * The "model," a list of boxes.
	 */
	private List boxes = new ArrayList();
	
	/**
	 * Actions
	 */
	private Action action1;
	private Action action2;
    private UndoActionHandler undoAction;
    private RedoActionHandler redoAction;
    
    /**
     * Private undo context for box operations
     */
    private IUndoContext undoContext;
    
	/**
	 * True if a click-drag is in progress
	 */
	private boolean dragInProgress = false;
	
	/**
	 * The position of the first click in a click-drag
	 */
	private Point anchorPosition = new Point(-1, -1);

	/**
	 * A temporary point
	 */
	private Point tempPosition = new Point(-1, -1);
	
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
				SWT.NO_REDRAW_RESIZE);
        addListeners();
        makeActions();
        hookContextMenu();
        createGlobalActionHandlers();
	}
	
	private void addListeners() {
		paintCanvas.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				if (event.button != 1) return;
				if (dragInProgress) return; // spurious event
				dragInProgress = true;
				
				anchorPosition.x = event.x;
				anchorPosition.y = event.y;

			}
			public void mouseUp(MouseEvent event) {
				if (event.button != 1) {
					resetDrag(); // abort if right or middle mouse button pressed
					return;
				}
				if (! dragInProgress) return; // spurious event
				dragInProgress = false;
				if (anchorPosition.x == -1) return; // spurious event
				
				commitRubberBandSelection();
				boxes.add(new Box(anchorPosition, tempPosition));
				// change to redraw only the box
				paintCanvas.redraw();
			}
			public void mouseDoubleClick(MouseEvent event) {
			}			
		});
		paintCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent event) {
				if (! dragInProgress) {
					return;
				}
				clearRubberBandSelection();
				tempPosition.x = event.x;
				tempPosition.y = event.y;
				addRubberBandSelection(anchorPosition, tempPosition);
			}
		});
		paintCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				for (int i=0; i<boxes.size(); i++) {
					((Box)boxes.get(i)).draw(event.gc);
				}
			}
		});
		
		paintCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				// dispose the gc
			}
		});

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
	
	/**
	 * Resets the drag operation.
	 */
	private void resetDrag() {
		// getPaintSurface().clearRubberbandSelection();
		anchorPosition.x = -1;
		dragInProgress = false;
	}
	
	private void clearRubberBandSelection() {
		
	}
	
	private void addRubberBandSelection(Point origin, Point corner) {
		
	}
	
	private void commitRubberBandSelection() {
		
	}


}