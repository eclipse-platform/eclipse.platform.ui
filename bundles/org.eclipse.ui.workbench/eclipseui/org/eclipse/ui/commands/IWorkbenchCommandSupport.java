/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.commands;

import java.util.Collection;

/**
 * An instance of this interface provides support for managing commands at the
 * <code>IWorkbench</code> level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 *
 * @since 3.0
 * @deprecated Please use <code>ICommandService</code> and
 *             <code>IHandlerService</code> instead. This API is scheduled for
 *             deletion, see Bug 431177 for details
 * @see org.eclipse.ui.commands.ICommandService
 * @see org.eclipse.ui.handlers.IHandlerService
 * @noimplement This interface is not intended to be implemented by clients.
 * @noreference This interface is scheduled for deletion.
 * @noextend This interface is not intended to be extended by clients.
 */
@Deprecated
@SuppressWarnings("all")
public interface IWorkbenchCommandSupport {

	/**
	 * Adds a single handler submissions for consideration by the workbench. The
	 * submission indicates to the workbench a set of conditions under which the
	 * handler should become active. The workbench, however, ultimately decides
	 * which handler becomes active (in the event of conflicts or changes in state).
	 * This could cause the handlers for one or more commands to change.
	 *
	 * @param handlerSubmission The submission to be added; must not be
	 *                          <code>null</code>.
	 */
	@Deprecated
	void addHandlerSubmission(HandlerSubmission handlerSubmission);

	/**
	 * Adds a collection of handler submissions for consideration by the workbench.
	 * The submission indicates to the workbench a set of conditions under which the
	 * handler should become active. The workbench, however, ultimately decides
	 * which handler becomes active (in the event of conflicts or changes in state).
	 * This could cause the handlers for one or more commands to change.
	 *
	 * @param handlerSubmissions The submissions to be added; must not be
	 *                           <code>null</code>, and must contain zero or more
	 *                           instances of <code>HandlerSubmission</code>.
	 */
	@Deprecated
	void addHandlerSubmissions(Collection handlerSubmissions);

	/**
	 * Returns the command manager for the workbench.
	 *
	 * @return the command manager for the workbench. Guaranteed not to be
	 *         <code>null</code>.
	 */
	@Deprecated
	ICommandManager getCommandManager();

	/**
	 * Removes a single handler submission from consideration by the workbench. The
	 * handler submission must be the same as the one added (not just equivalent).
	 * This could cause the handlers for one or more commands to change.
	 *
	 * @param handlerSubmission The submission to be removed; must not be
	 *                          <code>null</code>.
	 */
	@Deprecated
	void removeHandlerSubmission(HandlerSubmission handlerSubmission);

	/**
	 * Removes a single handler submission from consideration by the workbench. The
	 * handler submission must be the same as the one added (not just equivalent).
	 * This could cause the handlers for one or more commands to change.
	 *
	 * @param handlerSubmissions The submissions to be removed; must not be
	 *                           <code>null</code>, and must contain instances of
	 *                           <code>HandlerSubmission</code> only.
	 */
	@Deprecated
	void removeHandlerSubmissions(Collection handlerSubmissions);
}
