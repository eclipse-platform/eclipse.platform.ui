/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 427961
 *******************************************************************************/
package org.eclipse.ui.examples.undo.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.examples.undo.UndoExampleMessages;
import org.eclipse.ui.examples.undo.UndoPlugin;
import org.eclipse.ui.examples.undo.preferences.PreferenceConstants;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.ViewPart;

/**
 * This view shows what operations are being added to the operations history for
 * undo. The view can be filtered by any operation context. A null operation
 * context indicates that the view should not be filtered.
 *
 * <p>
 * Selecting undo or redo from the context menu or the edit menu will perform a
 * linear undo in the current context of the view. Selecting "Undo selected"
 * allows experimentation with direct undo. Operations that are undoable may be
 * undone independently of their order in the history.
 */

public class UndoHistoryView extends ViewPart implements
		ISelectionChangedListener {
	private TableViewer viewer;

	private Action filterAction;

	private Action doubleClickAction;

	private Action selectiveUndoAction;

	private Action refreshListAction;

	private IOperationHistory history = OperationHistoryFactory
			.getOperationHistory();

	private IUndoContext fContext = IOperationHistory.GLOBAL_UNDO_CONTEXT;

	private UndoActionHandler undoAction;

	private RedoActionHandler redoAction;

	private boolean showDebug = UndoPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_SHOWDEBUG);
	private IPropertyChangeListener propertyChangeListener;

	/*
	 * The content provider shows the operations in the undo portion of the
	 * operation history.  The histor is filtered by the currently selected
	 * undo context.
	 */

	class ViewContentProvider implements IStructuredContentProvider, IOperationHistoryListener {

		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// we never change inputs, so we just use this as a place to add our
			// listener.
			history.addOperationHistoryListener(this);
		}

		@Override
		public void dispose() {
			history.removeOperationHistoryListener(this);
		}

		@Override
		public Object[] getElements(Object input) {
			// show the items in the operations history.
			return history.getUndoHistory(fContext);
		}

		@Override
		public void historyNotification(OperationHistoryEvent event) {
			if (viewer.getTable().isDisposed()) {
				return;
			}
			Display display = viewer.getTable().getDisplay();
			switch (event.getEventType()) {
			case OperationHistoryEvent.OPERATION_ADDED:
			case OperationHistoryEvent.OPERATION_REMOVED:
			case OperationHistoryEvent.UNDONE:
			case OperationHistoryEvent.REDONE:
				if (event.getOperation().hasContext(fContext)
						&& display != null) {
					display.syncExec(() -> {
						// refresh all labels in case any operation has
						// changed dynamically
						// without notifying the operation history.
						if (!viewer.getTable().isDisposed()) {
							viewer.refresh(true);
						}
					});
				}
				break;
			}
		}
	}

	/*
	 * A simple label provider that uses a preference to determine
	 * whether the simple label or the debugging label (toString())
	 * for an operation is shown.
	 */
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public String getText(Object obj) {
			if (!showDebug && obj instanceof IUndoableOperation operation) {
				return operation.getLabel();
			}
			return obj.toString();
		}
	}

	/*
	 * Create a table viewer to show the list of operations.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		addListeners();
		createGlobalActionHandlers();
	}

	/*
	 * Add any listeners needed by this view.
	 */
	private void addListeners() {
		propertyChangeListener = event -> {
			if (event.getProperty() == PreferenceConstants.PREF_SHOWDEBUG) {
				showDebug = UndoPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_SHOWDEBUG);
				viewer.refresh();
			}
		};
		UndoPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
		viewer.getControl().addDisposeListener(event -> removeListeners());
	}

	/*
	 * Remove listeners that were added to this view.
	 */
	private void removeListeners() {
		UndoPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
	}


	/*
	 * Create global action handlers to control undo and redo. We use the action
	 * handlers rather than the UndoRedoActionGroup because this view
	 * dynamically sets the undo context of the handlers. Most views that simply
	 * desire an undo and redo menu action for their undo context can use
	 * UndoRedoActionGroup.
	 */
	private void createGlobalActionHandlers() {
		// set up action handlers that operate on the current context
		undoAction = new UndoActionHandler(this.getSite(), fContext);
		redoAction = new RedoActionHandler(this.getSite(), fContext);
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
				undoAction);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
				redoAction);
	}

	/*
	 * Put up a dialog that shows all of the available undo contexts and allow
	 * the user to select one.
	 */
	private IUndoContext selectContext() {
		// This would be better implemented as a view filter, but for now, we
		// will use a dialog that collects the available undo contexts.
		List<IUndoContext> input = new ArrayList<>();
		IUndoableOperation[] operations = history
				.getUndoHistory(IOperationHistory.GLOBAL_UNDO_CONTEXT);
		for (IUndoableOperation operation : operations) {
			IUndoContext[] contexts = operation.getContexts();
			for (IUndoContext context : contexts) {
				if (!input.contains(context)) {
					input.add(context);
				}
			}
		}
		input.add(IOperationHistory.GLOBAL_UNDO_CONTEXT);

		ILabelProvider labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IUndoContext) element).getLabel();
			}
		};

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getSite().getShell(), labelProvider);
		dialog.setMultipleSelection(false);
		dialog.setTitle(UndoExampleMessages.UndoHistoryView_ContextFilterDialog);
		dialog.setMessage(UndoExampleMessages.UndoHistoryView_ChooseContextMessage);
		dialog.setElements(input.toArray());
		dialog.setInitialSelections(fContext);
		if (dialog.open() == Window.OK) {
			Object[] contexts = dialog.getResult();
			if (contexts[0] instanceof IUndoContext) {
				return (IUndoContext) contexts[0];
			}
			return null;
		}
		return null;
	}

	/*
	 * Reset the undo context on which the history is filtered.
	 */
	public void setContext(IUndoContext context) {
		fContext = context;
		// setting the context into the actions updates the menu labels, etc.
		redoAction.setContext(context);
		undoAction.setContext(context);
		// need to refresh the viewer
		viewer.refresh(false);
	}

	/*
	 * Hook the context menu for the view
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(UndoHistoryView.this::fillContextMenu);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/*
	 * Fill the context menu for the view.
	 */
	private void fillContextMenu(IMenuManager manager) {
		// First add the global undo/redo actions
		undoAction.update();
		redoAction.update();
		manager.add(undoAction);
		manager.add(redoAction);
		manager.add(new Separator());

		// Now add our specialized actions
		manager.add(selectiveUndoAction);
		manager.add(filterAction);
		manager.add(refreshListAction);

		IStructuredSelection selection = viewer.getStructuredSelection();
		if (!selection.isEmpty()) {
			IUndoableOperation operation = (IUndoableOperation) selection.getFirstElement();
			selectiveUndoAction.setEnabled(operation.canUndo());
		} else {
			selectiveUndoAction.setEnabled(false);
		}

		// Other plug-ins can contribute actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * Create the actions for the view.
	 */
	private void makeActions() {
		filterAction = new Action() {
			@Override
			public void run() {
				IUndoContext context = selectContext();
				if (fContext != context && context != null) {
					setContext(context);
				}
			}
		};
		filterAction.setText(UndoExampleMessages.UndoHistoryView_FilterText);
		filterAction.setToolTipText(UndoExampleMessages.UndoHistoryView_FilterToolTipText);
		filterAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_OBJS_INFO_TSK));

		selectiveUndoAction = new Action() {
			@Override
			public void run() {
				IUndoableOperation operation = (IUndoableOperation) viewer.getStructuredSelection().getFirstElement();
				if (operation.canUndo()) {
					try {
						history.undoOperation(operation, null, undoAction);
					} catch (ExecutionException e) {
						showMessage(UndoExampleMessages.UndoHistoryView_OperationException);
					}
				} else {
					showMessage(UndoExampleMessages.UndoHistoryView_OperationInvalid);
				}
			}
		};
		selectiveUndoAction.setText(UndoExampleMessages.UndoHistoryView_UndoSelected);
		selectiveUndoAction.setToolTipText(UndoExampleMessages.UndoHistoryView_UndoSelectedToolTipText);
		selectiveUndoAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_UNDO));

		refreshListAction = new Action() {
			@Override
			public void run() {
				if (!viewer.getTable().isDisposed()) {
					viewer.refresh(true);
				}
			}
		};
		refreshListAction.setText(UndoExampleMessages.UndoHistoryView_RefreshList);
		refreshListAction.setToolTipText(UndoExampleMessages.UndoHistoryView_RefreshListToolTipText);

		doubleClickAction = new Action() {
			@Override
			public void run() {
				IUndoableOperation operation = (IUndoableOperation) viewer.getStructuredSelection().getFirstElement();
				StringBuilder buf = new StringBuilder(operation.getLabel());
				buf.append("\n");
				buf.append("Enabled=");	//$NON-NLS-1$
				buf.append(Boolean.valueOf(operation.canUndo()).toString());
				buf.append("\n");
				buf.append(operation.getClass().toString());
				showMessage(buf.toString());

			}
		};
	}

	/*
	 * Register a double click action with the double click event.
	 */
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(event -> doubleClickAction.run());
	}

	/*
	 * Show an info message
	 */
	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				UndoExampleMessages.UndoHistoryView_InfoDialogTitle, message);
	}

	/*
	 * The selection has changed.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = viewer.getStructuredSelection();
		boolean enabled = !selection.isEmpty();
		selectiveUndoAction.setEnabled(enabled);
	}

	/*
	 * Pass the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();

	}
}
