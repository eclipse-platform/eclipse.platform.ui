/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.operations;

import java.util.ArrayList;

import org.eclipse.core.commands.operations.IAdvancedUndoableOperation;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * An operation approver that prompts the user to see if a non-local undo should
 * proceed inside an editor. A non-local undo is detected when an operation
 * being undone or redone affects elements other than those described by the
 * editor itself. Clients can optionally specify a class, the preferred
 * comparison class, that should be used when comparing objects affected by the
 * editor with objects affected by an undo or redo operation. Comparisons
 * between the affected objects inside the editor and those described by the
 * operation will first be done by simply performing an equality check, using
 * {@link java.lang.Object#equals(java.lang.Object)}. If an object described by
 * an operation is not equal to one of the objects affected by the editor, and
 * if it is not an instance of the preferred comparison class, but is an
 * instance of {@link org.eclipse.core.runtime.IAdaptable}, then the operation
 * approver will also attempt to retrieve an adapter on that object for the
 * preferred comparison class and perform a second equality check using the
 * adapter.
 * </p>
 * <p>
 * This class may be instantiated by clients.
 * </p>
 * 
 * 
 * @since 3.1
 */
public final class NonLocalUndoUserApprover implements IOperationApprover {

	private IUndoContext context;

	private IEditorPart part;

	private Object[] elements;

	private Class affectedObjectsClass;

	private ArrayList elementsAndAdapters;

	/**
	 * Create a NonLocalUndoUserApprover associated with the specified editor
	 * and undo context
	 * 
	 * @param context
	 *            the undo context of operations in question.
	 * @param part
	 *            the editor part that is displaying the element
	 * @param affectedObjects
	 *            the objects that are affected by the editor and considered to
	 *            be objects local to the editor. The objects are typically
	 *            instances of the preferredComparisonClass or else provide
	 *            adapters for the preferredComparisonClass, although this is
	 *            not required.
	 * @param preferredComparisonClass
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
		if (!requiresApproval(operation, uiInfo)) {
			return Status.OK_STATUS;
		}

		String message = NLS.bind(
				WorkbenchMessages.Operations_nonLocalRedoWarning, operation
						.getLabel(), part.getEditorInput().getName());
		return proceedWithOperation(operation, message, WorkbenchMessages.Operations_discardRedo, WorkbenchMessages.Workbench_redoToolTip);
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
		if (!requiresApproval(operation, uiInfo)) {
			return Status.OK_STATUS;
		}

		String message = NLS.bind(
				WorkbenchMessages.Operations_nonLocalUndoWarning, operation
						.getLabel(), part.getEditorInput().getName());
		return proceedWithOperation(operation, message, WorkbenchMessages.Operations_discardUndo, WorkbenchMessages.Workbench_undoToolTip);

	}

	/*
	 * Determine whether the operation in question affects elements outside of
	 * the editor. If this can be determined and it does affect other elements,
	 * prompt the user as to whether the operation should proceed.
	 */
	private IStatus proceedWithOperation(IUndoableOperation operation,
			final String message, final String discardButton, final String title) {

		// if the operation cannot tell us about its modified elements, there's
		// nothing we can do.
		if (!(operation instanceof IAdvancedUndoableOperation)) {
			return Status.OK_STATUS;
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
			// The operation answered some array of affected objects. Consider
			// the operation local until a non-match is found. Note that an
			// empty
			// array of affected objects is considered a local change.
			local = true;
			for (int i = 0; i < modifiedElements.length; i++) {
				Object modifiedElement = modifiedElements[i];
				if (!elementsContains(modifiedElement)) {
					// the modified element is not known by the editor
					local = false;
					// one last try - try to adapt the modified element if a
					// preferred
					// comparison class has been provided.
					if (affectedObjectsClass != null) {
						Object adapter = Util.getAdapter(modifiedElement, 
								affectedObjectsClass);
						if (adapter != null && elementsContains(adapter)) {
							local = true;
						}
					}
					// if the element did not match the affected objects, no
					// need to check any others.
					if (!local) {
						break;
					}
				}
			}
		}
		if (local) {
			return Status.OK_STATUS;
		}

		// The operation affects more than just our element.  Find out if
		// we should proceed, cancel, or discard the undo.  Must be done in
		// a syncExec because operation approval notifications may come from
		// a background thread.
		final int[] answer = new int[1];
		Workbench.getInstance().getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog dialog = new MessageDialog(part.getSite().getShell(), title,
						null, message, MessageDialog.QUESTION, new String[] { IDialogConstants.OK_LABEL,
		                        discardButton, IDialogConstants.CANCEL_LABEL }, 0); // yes is the default
		        answer[0] = dialog.open();
		}});
		switch (answer[0]) {
		case 0:
			return Status.OK_STATUS;
		case 1:
			return IOperationHistory.OPERATION_INVALID_STATUS;
		default:
			// Cancel by default to include ESC key and shell close,
			// which return SWT.DEFAULT, and any other unexpected return codes
			return Status.CANCEL_STATUS;
		}
	}

	/*
	 * Answer whether this operation is relevant enough to this operation
	 * approver that it should be examined in detail.
	 */
	private boolean requiresApproval(IUndoableOperation operation,
			IAdaptable uiInfo) {
		// no approval is required if the operation doesn't have our undo
		// context
		if (!(operation.hasContext(context))) {
			return false;
		}

		// no approval is required if the operation only has our context
		if (operation.getContexts().length == 1) {
			return false;
		}

		// no approval is required if we can ascertain that the operation did
		// not originate
		// in our context.
		if (uiInfo != null) {
			IUndoContext originatingContext = (IUndoContext) Util.getAdapter(uiInfo, 
					IUndoContext.class);
			if (originatingContext != null
					&& !(originatingContext.matches(context))) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Return whether or not the collection of editor elements plus any of their
	 * adapters contains the specified object.
	 */
	private boolean elementsContains(Object someObject) {
		if (elements == null) {
			return false;
		}
		if (elementsAndAdapters == null) {
			// Compute a list of not just the elements, but any adapters they
			// may provide on the preferred class if they are not instances of
			// the preferred class. This is done only once.
			elementsAndAdapters = new ArrayList(elements.length);
			for (int i = 0; i < elements.length; i++) {
				Object element = elements[i];
				elementsAndAdapters.add(element);
				if (affectedObjectsClass != null
						&& !affectedObjectsClass.isInstance(element)) {
					Object adapter = Util.getAdapter(element, affectedObjectsClass);
					if (adapter != null) {
						elementsAndAdapters.add(adapter);
					}
				}
			}
		}
		for (int i = 0; i < elementsAndAdapters.size(); i++) {
			if (elementsAndAdapters.get(i).equals(someObject)) {
				return true;
			}
		}
		return false;
	}
}
