/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.undo.views;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.examples.undo.AddBoxOperation;
import org.eclipse.ui.examples.undo.Box;
import org.eclipse.ui.examples.undo.Boxes;
import org.eclipse.ui.examples.undo.ClearBoxesOperation;
import org.eclipse.ui.examples.undo.MoveBoxOperation;
import org.eclipse.ui.examples.undo.PromptingUserApprover;
import org.eclipse.ui.examples.undo.UndoExampleMessages;
import org.eclipse.ui.examples.undo.UndoPlugin;
import org.eclipse.ui.examples.undo.preferences.PreferenceConstants;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

public class BoxView extends ViewPart {
	/*
	 * The canvas to paint the boxes on.
	 */
	private Canvas paintCanvas;

	/*
	 * The gc used for drawing the rubber band.
	 */
	private GC gc;

	/*
	 * The model, a group of boxes
	 */
	private Boxes boxes = new Boxes();

	/*
	 * Undo and redo actions
	 */
	private UndoActionHandler undoAction;

	private RedoActionHandler redoAction;

	private IAction clearBoxesAction;

	/*
	 * Private undo context for box operations
	 */
	private IUndoContext undoContext;

	/*
	 * Operation approver for approving undo and redo
	 */
	private IOperationApprover operationApprover;

	/*
	 * Property change listener for the undo limit preference.
	 */
	private IPropertyChangeListener propertyChangeListener;

	/*
	 * True if a click-drag is in progress
	 */
	private boolean dragInProgress = false;

	/*
	 * True if a click-move is in progress
	 */
	private boolean moveInProgress = false;

	/*
	 * The box that is being moved.
	 */
	private Box movingBox;

	/*
	 * The diff between a moving box and the track position.
	 */
	int diffX, diffY;

	/*
	 * The position of the first click in a click-drag
	 */
	private Point anchorPosition = new Point(-1, -1);

	/*
	 * A temporary point in a drag or move operation
	 */
	private Point tempPosition = new Point(-1, -1);

	/*
	 * The rubber band position (the last recorded temp position)
	 */
	private Point rubberbandPosition = new Point(-1, -1);

	/*
	 * Construct a BoxView.
	 */
	public BoxView() {
		super();
		initializeOperationHistory();
	}

	/*
	 * Create the canvas on which boxes are drawn and hook up all actions and
	 * listeners.
	 */
	public void createPartControl(Composite parent) {
		paintCanvas = new Canvas(parent, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.NO_REDRAW_RESIZE);

		// set up a GC for drawing the tracking rectangle
		gc = new GC(paintCanvas);
		gc.setForeground(paintCanvas.getForeground());
		gc.setLineStyle(SWT.LINE_SOLID);

		// add listeners
		addListeners();

		// create actions and hook them up to the menus and toolbar
		makeActions();
		hookContextMenu();
		createGlobalActionHandlers();
		contributeToActionBars();
	}

	/*
	 * Add listeners to the canvas.
	 */
	private void addListeners() {
		paintCanvas.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				if (event.button != 1)
					return;
				if (dragInProgress || moveInProgress)
					return; // spurious event

				tempPosition.x = event.x;
				tempPosition.y = event.y;
				Box box = boxes.getBox(event.x, event.y);
				if (box != null) {
					moveInProgress = true;
					movingBox = box;
					anchorPosition.x = box.x1;
					anchorPosition.y = box.y1;
					diffX = event.x - box.x1;
					diffY = event.y - box.y1;
				} else {
					dragInProgress = true;
					anchorPosition.x = event.x;
					anchorPosition.y = event.y;
				}
			}

			public void mouseUp(MouseEvent event) {
				if (event.button != 1) {
					resetDrag(true); // abort if right or middle mouse button
					// pressed
					return;
				}
				if (anchorPosition.x == -1)
					return; // spurious event

				if (dragInProgress) {
					Box box = new Box(anchorPosition.x, anchorPosition.y,
							tempPosition.x, tempPosition.y);
					if (box.getWidth() > 0 && box.getHeight() > 0) {
						try {
							getOperationHistory().execute(
									new AddBoxOperation(
											UndoExampleMessages.BoxView_Add,
											undoContext, boxes, box, paintCanvas),
									null, null);
						} catch (ExecutionException e) {
						}
						dragInProgress = false;
					}
				} else if (moveInProgress) {
					try {
						getOperationHistory().execute(
								new MoveBoxOperation(
										UndoExampleMessages.BoxView_Move,
										undoContext, movingBox, paintCanvas,
										anchorPosition), null, null);
					} catch (ExecutionException e) {
					}
					moveInProgress = false;
					movingBox = null;
				}
				resetDrag(false);

				// redraw everything to clean up the tracking rectangle
				paintCanvas.redraw();
			}

			public void mouseDoubleClick(MouseEvent event) {
			}
		});
		paintCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent event) {
				if (dragInProgress) {
					clearRubberBandSelection();
					tempPosition.x = event.x;
					tempPosition.y = event.y;
					addRubberBandSelection();
				} else if (moveInProgress) {
					clearRubberBandSelection();
					anchorPosition.x = event.x - diffX;
					anchorPosition.y = event.y - diffY;
					tempPosition.x = anchorPosition.x + movingBox.getWidth();
					tempPosition.y = anchorPosition.y + movingBox.getHeight();
					addRubberBandSelection();
				}
			}
		});
		paintCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				event.gc.setForeground(paintCanvas.getForeground());
				boxes.draw(event.gc);
			}
		});

		paintCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				// dispose the gc
				gc.dispose();
				// dispose listeners
				removeListeners();
			}
		});

		// listen for a change in the undo limit
		propertyChangeListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == PreferenceConstants.PREF_UNDOLIMIT) {
					int limit = UndoPlugin.getDefault().getPreferenceStore()
							.getInt(PreferenceConstants.PREF_UNDOLIMIT);
					getOperationHistory().setLimit(undoContext, limit);
				}
			}
		};
		UndoPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(
				propertyChangeListener);

	}

	/*
	 * Remove listeners that were registered. Since the control is being
	 * disposed, we are only removing non-control listeners.
	 */
	private void removeListeners() {
		UndoPlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(propertyChangeListener);
		getOperationHistory().removeOperationApprover(operationApprover);
	}

	/*
	 * Hook a listener on the menu showing so we can fill the context menu with
	 * our actions.
	 */
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

	/*
	 * Add our actions to the action bars.
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/*
	 * Add our undo and redo actions to the local pulldown.
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(undoAction);
		manager.add(redoAction);
		manager.add(new Separator());
		manager.add(clearBoxesAction);
	}

	/*
	 * Add our undo and redo actions to the context menu.
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(undoAction);
		manager.add(redoAction);
		manager.add(new Separator());
		manager.add(clearBoxesAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Add actions to the local toolbar.
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(clearBoxesAction);
	}

	/*
	 * Make any local actions used in the view.
	 */
	private void makeActions() {
		clearBoxesAction = new Action() {
			public void run() {
				try {
					getOperationHistory().execute(
							new ClearBoxesOperation(
									UndoExampleMessages.BoxView_ClearBoxes,
									undoContext, boxes, paintCanvas), null,
							null);
				} catch (ExecutionException e) {
				}
			}
		};
		clearBoxesAction.setText(UndoExampleMessages.BoxView_ClearBoxes);
		clearBoxesAction
				.setToolTipText(UndoExampleMessages.BoxView_ClearBoxesToolTipText);
		clearBoxesAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));
	}

	/*
	 * Create the global undo and redo action handlers.
	 */
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

	/*
	 * Set focus to the canvas.
	 */
	public void setFocus() {
		paintCanvas.setFocus();
	}

	/*
	 * Reset the drag operation.
	 */
	private void resetDrag(boolean clearRubberband) {
		if (clearRubberband) {
			clearRubberBandSelection();
		}
		dragInProgress = false;
		moveInProgress = false;
		movingBox = null;
		anchorPosition.x = anchorPosition.y = tempPosition.x = tempPosition.y = rubberbandPosition.x = rubberbandPosition.y = -1;
	}

	/*
	 * Clear the existing rubber band selection.
	 */
	private void clearRubberBandSelection() {
		gc.setForeground(paintCanvas.getBackground());
		gc.drawRectangle(anchorPosition.x, anchorPosition.y,
				rubberbandPosition.x - anchorPosition.x, rubberbandPosition.y
						- anchorPosition.y);
		paintCanvas.redraw(anchorPosition.x, anchorPosition.y,
				rubberbandPosition.x - anchorPosition.x, rubberbandPosition.y
						- anchorPosition.y, false);
		paintCanvas.update();
		rubberbandPosition = new Point(-1, -1);
		gc.setForeground(paintCanvas.getForeground());
	}

	/*
	 * Show a rubber band selection.
	 */
	private void addRubberBandSelection() {
		rubberbandPosition = tempPosition;
		gc.drawRectangle(anchorPosition.x, anchorPosition.y,
				rubberbandPosition.x - anchorPosition.x, rubberbandPosition.y
						- anchorPosition.y);
	}

	/*
	 * Initialize the workbench operation history for our undo context.
	 */
	private void initializeOperationHistory() {
		// create a unique undo context to 
		// represent this view's undo history
		undoContext = new ObjectUndoContext(this);

		// set the undo limit for this context based on the preference
		int limit = UndoPlugin.getDefault().getPreferenceStore().getInt(
				PreferenceConstants.PREF_UNDOLIMIT);
		getOperationHistory().setLimit(undoContext, limit);

		// Install an operation approver for this undo context that prompts
		// according to a user preference.
		operationApprover = new PromptingUserApprover(undoContext);
		getOperationHistory().addOperationApprover(operationApprover);
	}

	/*
	 * Get the operation history from the workbench.
	 */
	private IOperationHistory getOperationHistory() {
		return PlatformUI.getWorkbench().getOperationSupport()
				.getOperationHistory();
	}

}
