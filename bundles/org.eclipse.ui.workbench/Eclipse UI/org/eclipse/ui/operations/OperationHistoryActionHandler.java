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
package org.eclipse.ui.operations;

import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * OperationHistoryActionHandler provides common behavior for the undo and redo
 * actions. It supports filtering of undo or redo on a particular context. A
 * null context means that there is no undo or redo supported. When a part is
 * activated, the action looks for an adapter for the undo context. If one is
 * supplied, then the action will filter the operations history on that context.
 * </p>
 * <p>
 * OperationHistoryActionHandler provides an adapter for its 
 * org.eclipse.ui.IWorkbenchWindow and its 
 * org.eclipse.swt.widgets.Shell in the info parameter of the IOperationHistory
 * undo and redo methods.
 * </p>
 * <p>
 * OperationHistoryActionHandler assumes a linear undo/redo model. When the
 * handler is run, the operation history is asked to perform the most recent
 * undo for the handler's context. The handler can be configured (using
 * #setPruneHistory(true) to flush the operation undo or redo history for its
 * context when there is no valid operation, to avoid keeping a stale history of
 * invalid operations. By default, pruning does not occur and it is assumed that
 * clients of the particular undo context are pruning the history when
 * necessary.
 * </p>
 * <p>
 * Note: This class/interface is part of a new API under development. It has
 * been added to builds so that clients can start using the new features.
 * However, it may change significantly before reaching stability. It is being
 * made available at this early stage to solicit feedback with the understanding
 * that any code that uses this API may be broken as the API evolves.
 * </p>
 * 
 * @since 3.1
 * @experimental
 */
public abstract class OperationHistoryActionHandler extends Action implements
		ActionFactory.IWorkbenchAction, IAdaptable, IOperationHistoryListener {

	protected IUndoContext undoContext = null;

	private boolean pruning = false;

	protected IWorkbenchWindow workbenchWindow;

	/**
	 * Construct an operation history action for the specified workbench window
	 * with the specified undo context.
	 * 
	 * @param window -
	 *            the workbench window for the action.
	 * @param context -
	 *            the undo context to be used
	 */
	public OperationHistoryActionHandler(IWorkbenchWindow window,
			IUndoContext context) {
		// string will be reset inside action
		super(""); //$NON-NLS-1$
		workbenchWindow = window;
		undoContext = context;
		getHistory().addOperationHistoryListener(this);
	}

	/**
	 * Dispose of any resources allocated by this action.
	 */
	public void dispose() {
		// nothing to dispose
	}

	/*
	 * Flush the history associated with this action.
	 */
	protected abstract void flush();

	/*
	 * Return the string describing the command.
	 */
	protected abstract String getCommandString();

	/*
	 * Return the operation history we are using.
	 */
	protected IOperationHistory getHistory() {
		return workbenchWindow.getWorkbench().getOperationSupport()
				.getOperationHistory();
	}

	/*
	 * Return the current operation.
	 */
	protected abstract IUndoableOperation getOperation();

	/**
	 * Run the action. Implemented by subclasses.
	 */
	public abstract void run();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Shell.class)) {
			return workbenchWindow.getShell();
		}
		if (adapter.equals(IWorkbenchWindow.class)) {
			return workbenchWindow;
		}
		return null;
	}

	/**
	 * The undo and redo subclasses should implement this.
	 * 
	 * @return - a boolean indicating enablement state
	 */
	protected abstract boolean shouldBeEnabled();

	/**
	 * Set the context shown by the handler. Normally the context is set as
	 * parts activate and deactivate, but a part may wish to set the context
	 * manually.
	 * 
	 * @param context -
	 *            the context to be used for the undo history
	 */
	public void setContext(IUndoContext context) {
		undoContext = context;
		update();
	}

	/**
	 * Specify whether the action handler should actively prune the operation
	 * history when invalid operations are encountered. The default value is
	 * <code>false</code>.
	 * 
	 * @param prune -
	 *            <code>true</code> if the history should be pruned by the
	 *            handler, and <code>false</code> if it should not.
	 * 
	 */
	public void setPruneHistory(boolean prune) {
		pruning = prune;
	}

	/**
	 * Something has changed in the operation history. Check to see if it
	 * involves this context.
	 * 
	 * @param event -
	 *            the event that has occurred.
	 */
	public void historyNotification(OperationHistoryEvent event) {
		switch (event.getEventType()) {
		case OperationHistoryEvent.OPERATION_ADDED:
		case OperationHistoryEvent.OPERATION_REMOVED:
		case OperationHistoryEvent.UNDONE:
		case OperationHistoryEvent.REDONE:
			if (event.getOperation().hasContext(undoContext))
				update();
			break;
		case OperationHistoryEvent.OPERATION_CHANGED:
			if (event.getOperation() == getOperation()) {
				update();
			}
		}
	}

	/**
	 * Update enabling and labels according to the current status of the
	 * history.
	 */
	public void update() {
		boolean enabled = shouldBeEnabled();
		String text = getCommandString();
		if (enabled) {
			text = MessageFormat.format(
					"{0} {1}", new Object[] { text, getOperation().getLabel() }); //$NON-NLS-1$
		} else {
			/*
			 * if there is nothing to do and we are pruning the history, flush
			 * the history of this context.
			 */
			if (undoContext != null && pruning)
				flush();
		}
		setText(text.toString());
		setEnabled(enabled);
	}
	
	/*
	 * Report the specified execution exception to the log and to the user.
	 */
	final void reportException(ExecutionException e) {
			Throwable nestedException = e.getCause();
			Throwable exception = (nestedException == null) ? e : nestedException;
			String title = WorkbenchMessages.Error;
			String message = WorkbenchMessages.WorkbenchWindow_exceptionMessage;
			String exceptionMessage = exception.getMessage();
			if (exceptionMessage == null) {
				exceptionMessage = message;
			}
			IStatus status = new Status(IStatus.ERROR,
					WorkbenchPlugin.PI_WORKBENCH, 0, exceptionMessage, exception);
			WorkbenchPlugin.log(message, status);
			ErrorDialog.openError(workbenchWindow.getShell(), title, message, status);
		}

}
