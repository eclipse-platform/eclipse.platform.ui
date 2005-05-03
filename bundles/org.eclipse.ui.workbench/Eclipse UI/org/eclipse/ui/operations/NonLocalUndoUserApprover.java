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
import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IAdvancedUndoableOperation;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * An operation approver that prompts the user to see if a non-local undo should
 * proceed inside an editor. A non-local undo is detected when an operation
 * being undone or redone affects elements other than those described by the
 * editor itself. Clients can optionally specify a class, the preferred
 * comparison class, that should be used when comparing objects affected by the
 * editor with objects affected by an undo or redo operation. If any of the
 * compared objects are not instances of the preferred comparison class but are
 * instances of {@link org.eclipse.core.runtime.IAdaptable}, then the operation
 * approver will attempt to retrieve an adapter of the preferred comparison
 * class to be used for the comparison.
 * 
 * The operation approver also rechecks the validity of the operation (using
 * {@link IAdvancedUndoableOperation#computeUndoableStatus(IProgressMonitor)} or
 * {@link IAdvancedUndoableOperation#computeRedoableStatus(IProgressMonitor)}
 * before any prompting occurs, so that the user is not prompted if the
 * operation will fail anyway.
 * </p>
 * 
 * @since 3.1
 */
public class NonLocalUndoUserApprover implements IOperationApprover {

	private IUndoContext context;

	private IEditorPart part;

	private Object[] elements;

	private Class affectedObjectsClass;

	private ArrayList elementsAndAdapters;

	/**
	 * Create a NonLocalUndoUserApprover associated with the specified editor
	 * and undo context
	 * 
	 * @param context -
	 *            the undo context of operations in question.
	 * @param part -
	 *            the editor part that is displaying the element
	 * @param affectedObjects -
	 *            the objects that are affected by the editor and considered to
	 *            be objects local to the editor. The objects are typically
	 *            instances of the preferredComparison or else provide adapters
	 *            for the preferredComparisonClass, although this is not
	 *            required.
	 * @param preferredComparisonClass -
	 *            the preferred class to be used when comparing the editor's
	 *            affectedObjects with those provided by the undoable operation
	 *            using
	 *            {@link org.eclipse.core.commands.operations.IAdvancedUndoableOperation#getAffectedObjects()}.
	 *            If the operation's affected objects are not instances of the
	 *            specified class, but are instances of
	 *            {@link org.eclipse.core.runtime.IAdaptable}, then an adapter
	 *            for this class will be requested. The preferredComparisonClass
	 *            may be <code>null</code>, which indicates that there is no
	 *            expected class or adapter necessary for the comparison.
	 */
	public NonLocalUndoUserApprover(IUndoContext context, IEditorPart part,
			Object[] affectedObjects, Class preferredComparisonClass) {
		super();
		this.context = context;
		this.part = part;
		this.affectedObjectsClass = preferredComparisonClass;
		this.elements = affectedObjects;
	}

	/**
	 * Create a NonLocalUndoUserApprover associated with the specified editor
	 * and undo context
	 * 
	 * @param context -
	 *            the undo context of operations in question.
	 * @param part -
	 *            the editor part that is displaying the element
	 * @deprecated see https://bugs.eclipse.org/bugs/show_bug.cgi?id=89197
	 */
	public NonLocalUndoUserApprover(IUndoContext context, IEditorPart part) {
		this(context, part, new Object[] {}, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedRedoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus proceedRedoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo) {

		// return immediately if the operation is not relevant
		if (!requiresApproval(operation, uiInfo))
			return Status.OK_STATUS;

		String message = MessageFormat
				.format(
						"Redoing {0} affects elements outside of {1}.  Continue with redoing {0}?", new Object[] { operation.getLabel(), part.getEditorInput().getName() }); //$NON-NLS-1$
		return proceedWithOperation(operation, history, uiInfo, message, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedUndoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus proceedUndoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo) {

		// return immediately if the operation is not relevant
		if (!requiresApproval(operation, uiInfo))
			return Status.OK_STATUS;

		String message = MessageFormat
				.format(
						"Undoing {0} affects elements outside of {1}.  Continue with undoing {0}?", new Object[] { operation.getLabel(), part.getEditorInput().getName() }); //$NON-NLS-1$
		return proceedWithOperation(operation, history, uiInfo, message, true);

	}

	/*
	 * Determine whether the operation in question affects elements outside of
	 * the editor. If this can be determined and it does affect other elements,
	 * prompt the user as to whether the operation should proceed.
	 */
	private IStatus proceedWithOperation(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo, String message,
			boolean undoing) {

		// if the operation cannot tell us about its modified elements, there's
		// nothing we can do.
		if (!(operation instanceof IAdvancedUndoableOperation))
			return Status.OK_STATUS;

		// see if the operation is still valid. Some advanced model operations
		// cannot compute their validity in canUndo() or canRedo() and true
		// validity cannot be determined until it is time to perform the
		// operation.
		IStatus status;
		try {
			if (undoing)
				status = ((IAdvancedUndoableOperation) operation)
						.computeUndoableStatus(getProgressMonitor());
			else
				status = ((IAdvancedUndoableOperation) operation)
						.computeRedoableStatus(getProgressMonitor());
		} catch (OperationCanceledException e) {
			status = Status.CANCEL_STATUS;
		} catch (ExecutionException e) {
			status = IOperationHistory.OPERATION_INVALID_STATUS;
			reportException(e);
		}

		if (status.getSeverity() == IStatus.ERROR) {
			// report failure to the user.
			reportErrorStatus(status);
		}

		if (!status.isOK()) {
			// inform listeners of the change in status of the operation since
			// it was previously
			// believed to be valid. We rely here on the ability of an
			// implementer of IAdvancedUndoableOperation
			// to correctly answer canUndo() and canRedo() once the undoable and
			// redoable status have
			// been computed.
			history.operationChanged(operation);
			return status;
		}

		// Obtain the operation's affected objects.
		Object[] modifiedElements = ((IAdvancedUndoableOperation) operation)
				.getAffectedObjects();

		// Since the operation participates in describing its affected objects,
		// we assume for the rest of this method that an inability to
		// determine a match implies that a non-local operation is occurring.
		// This is a conservative assumption that provides more user prompting.

		boolean local;
		if (modifiedElements == null) {
			// The operation could not determine which elements are affected.
			// Consider the operation non-local.
			local = false;
		} else {
			// The operation answered some array of affected objects.  Consider
			// the operation local until a non-match is found.  Note that an empty
			// array of affected objects is considered a local change.
			local = true;
			for (int i = 0; i < modifiedElements.length; i++) {
				Object modifiedElement = modifiedElements[i];
				if (!elementsContains(modifiedElement)) {
					// the modified element is not known by the editor
					local = false;
					// one last try - try to adapt the modified element if a preferred 
					// comparison class has been provided.
					if (affectedObjectsClass != null
							&& modifiedElement instanceof IAdaptable) {
						if (elementsContains(((IAdaptable) modifiedElement)
								.getAdapter(affectedObjectsClass))) {
							local = true;
						}
					} 
					// if the element did not match the affected objects, no need to check any others.
					if (!local)
						break;
				}
			}
		}
		if (local)
			return Status.OK_STATUS;

		// Now we know the operation affects more than just our element, so warn
		// the user.
		boolean proceed = MessageDialog.openQuestion(part.getSite().getShell(),
				part.getEditorInput().getName(), message);

		if (proceed)
			return Status.OK_STATUS;

		return Status.CANCEL_STATUS;
	}

	/*
	 * Answer whether this operation is relevant enough to this operation
	 * approver that it should be examined in detail.
	 */
	private boolean requiresApproval(IUndoableOperation operation,
			IAdaptable uiInfo) {
		// no approval is required if the operation doesn't have our undo
		// context
		if (!(operation.hasContext(context)))
			return false;

		// no approval is required if the operation only has our context
		if (operation.getContexts().length == 1)
			return false;

		// no approval is required if we can ascertain that the operation did
		// not originate
		// in our context.
		if (uiInfo != null) {
			IUndoContext originatingContext = (IUndoContext) uiInfo
					.getAdapter(IUndoContext.class);
			if (originatingContext != null
					&& !(originatingContext.matches(context)))
				return false;
		}

		return true;
	}

	/*
	 * Return the progress monitor that should be used for computing validity
	 * checks for undo and redo.
	 */
	private IProgressMonitor getProgressMonitor() {
		// temporary implementation
		return null;
	}

	/*
	 * Report the specified execution exception to the log and to the user.
	 */
	private void reportException(ExecutionException e) {
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
		ErrorDialog
				.openError(part.getSite().getShell(), title, message, status);
	}

	/*
	 * Report a failed status to the user
	 */
	private void reportErrorStatus(IStatus status) {
		ErrorDialog.openError(part.getSite().getShell(),
				WorkbenchMessages.Error, null, status);
	}

	/*
	 * Return whether or not the collection of editor elements plus any of their
	 * adapters contains the specified object.
	 */
	private boolean elementsContains(Object someObject) {
		if (elements == null)
			return false;
		if (elementsAndAdapters == null) {
			// Compute a list of not just the elements, but any adapters they
			// may provide on the preferred class if they are not instances of
			// the preferred class. This is done only once.
			elementsAndAdapters = new ArrayList(elements.length);
			for (int i = 0; i < elements.length; i++) {
				Object element = elements[i];
				elementsAndAdapters.add(element);
				if (affectedObjectsClass != null
						&& !affectedObjectsClass.isInstance(element)
						&& element instanceof IAdaptable) {
					Object adapter = ((IAdaptable) element)
							.getAdapter(affectedObjectsClass);
					if (adapter != null)
						elementsAndAdapters.add(adapter);
				}
			}
		}
		for (int i = 0; i < elementsAndAdapters.size(); i++) {
			if (elementsAndAdapters.get(i).equals(someObject))
				return true;
		}
		return false;
	}
}
