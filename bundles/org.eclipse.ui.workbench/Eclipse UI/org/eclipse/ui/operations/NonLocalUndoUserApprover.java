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

import org.eclipse.core.commands.operations.IUndoableAffectedObjects;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;

/**
 * <p>
 * An operation approver that prompts the user to see if a non-local undo should
 * proceed inside an editor. A non-local undo is detected when an operation
 * being undone or redone affects elements other than those described by the
 * editor itself.
 * </p>
 * 
 * @since 3.1
 */
public class NonLocalUndoUserApprover implements IOperationApprover {

	private IUndoContext context;
	private IEditorPart part;
	private ArrayList editorElementsAndResources;

	/**
	 * Create a NonLocalUndoUserApprover associated with the specified editor and
	 * undo context
	 * 
	 * @param context -
	 *            the undo context of operations in question.
	 * @param part -
	 *            the editor part that is displaying the element
	 */
	public NonLocalUndoUserApprover(IUndoContext context,
			IEditorPart part) {
		super();
		this.context = context;
		this.part = part;
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
		return proceedWithOperation(operation, history, uiInfo, message);
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
		return proceedWithOperation(operation, history, uiInfo, message);

	}

	/*
	 * Determine whether the operation in question affects elements outside of
	 * the editor. If this can be determined and it does affect other elements,
	 * prompt the user as to whether the operation should proceed.
	 */
	private IStatus proceedWithOperation(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo, String message) {

		// if the operation cannot tell us about its modified elements, there's
		// nothing we can do.
		if (!(operation instanceof IUndoableAffectedObjects))
			return Status.OK_STATUS;

		// Obtain the operation's affected objects.
		Object[] modifiedElements = ((IUndoableAffectedObjects) operation)
				.getAffectedObjects();

		// Since the operation participates in identifying a single modified
		// element, we assume for the rest of this method that an inability to
		// determine a match implies that a non-local operation is occurring.
		// This is a conservative assumption that provides more user prompting.
		if (modifiedElements != null) {
			boolean local = true;
			// Each modified element must be known by the editor in order to be
			// considered local. First, expand the list of elements known by the
			// editor to include resource adaptations of those elements if
			// supported.
			if (editorElementsAndResources == null)
				computeEditorElementsAndResources();
			for (int i = 0; i < modifiedElements.length; i++) {
				Object modifiedElement = modifiedElements[i];
				if (!editorElementsAndResources.contains(modifiedElement)) {
					if (modifiedElement instanceof IAdaptable) {
						IResource modifiedElementResource = (IResource) ((IAdaptable) modifiedElement)
								.getAdapter(IResource.class);
						if (!editorElementsAndResources
								.contains(modifiedElementResource)) {
							local = false;
							break;
						}
					}
				}
			}
			if (local)
				return Status.OK_STATUS;
		}

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
	 * Compute the full list of affected editor elements and any resource
	 * adapters provided by these elements.
	 */
	private void computeEditorElementsAndResources() {
		Object [] elements = null;
		IUndoableAffectedObjects affectedObjects = (IUndoableAffectedObjects)part.getAdapter(IUndoableAffectedObjects.class);
		if (affectedObjects != null) {
			elements = affectedObjects.getAffectedObjects();
		}
		if (elements == null) {
			editorElementsAndResources = new ArrayList(0);
		} else {
			editorElementsAndResources = new ArrayList(elements.length);
			for (int i = 0; i < elements.length; i++) {
				Object element = elements[i];
				editorElementsAndResources.add(element);
				if (element instanceof IAdaptable) {
					IResource resource = (IResource) ((IAdaptable) element)
							.getAdapter(IResource.class);
					if (resource != null)
						editorElementsAndResources.add(resource);
				}
			}
		}
	}
}
