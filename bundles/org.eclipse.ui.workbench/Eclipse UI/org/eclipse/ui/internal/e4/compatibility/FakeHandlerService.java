/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.Collection;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * @since 3.5
 *
 */
public class FakeHandlerService implements IHandlerService {

	public IHandlerActivation activateHandler(IHandlerActivation activation) {
		E4Util.unsupported("activateHandler"); //$NON-NLS-1$
		return null;
	}

	public IHandlerActivation activateHandler(String commandId, IHandler handler) {
		E4Util.unsupported("activateHandler"); //$NON-NLS-1$
		return null;
	}

	public IHandlerActivation activateHandler(String commandId, IHandler handler,
			Expression expression) {
		E4Util.unsupported("activateHandler"); //$NON-NLS-1$
		return null;
	}

	public IHandlerActivation activateHandler(String commandId, IHandler handler,
			Expression expression, boolean global) {
		E4Util.unsupported("activateHandler"); //$NON-NLS-1$
		return null;
	}

	public IHandlerActivation activateHandler(String commandId, IHandler handler,
			Expression expression, int sourcePriorities) {
		E4Util.unsupported("activateHandler"); //$NON-NLS-1$
		return null;
	}

	public ExecutionEvent createExecutionEvent(Command command, Event event) {
		E4Util.unsupported("createExecutionEvent"); //$NON-NLS-1$
		return null;
	}

	public ExecutionEvent createExecutionEvent(ParameterizedCommand command, Event event) {
		E4Util.unsupported("createExecutionEvent"); //$NON-NLS-1$
		return null;
	}

	public void deactivateHandler(IHandlerActivation activation) {
		E4Util.unsupported("deactivateHandler"); //$NON-NLS-1$

	}

	public void deactivateHandlers(Collection activations) {
		E4Util.unsupported("deactivateHandlers"); //$NON-NLS-1$

	}

	public Object executeCommand(String commandId, Event event) throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		E4Util.unsupported("executeCommand"); //$NON-NLS-1$
		return null;
	}

	public Object executeCommand(ParameterizedCommand command, Event event)
			throws ExecutionException, NotDefinedException, NotEnabledException,
			NotHandledException {
		E4Util.unsupported("executeCommand"); //$NON-NLS-1$
		return null;
	}

	public Object executeCommandInContext(ParameterizedCommand command, Event event,
			IEvaluationContext context) throws ExecutionException, NotDefinedException,
			NotEnabledException, NotHandledException {
		E4Util.unsupported("executeCommandInContext"); //$NON-NLS-1$
		return null;
	}

	public IEvaluationContext createContextSnapshot(boolean includeSelection) {
		E4Util.unsupported("createContextSnapshot"); //$NON-NLS-1$
		return null;
	}

	public IEvaluationContext getCurrentState() {
		E4Util.unsupported("getCurrentState"); //$NON-NLS-1$
		return null;
	}

	public void readRegistry() {
		E4Util.unsupported("readRegistry"); //$NON-NLS-1$

	}

	public void setHelpContextId(IHandler handler, String helpContextId) {
		E4Util.unsupported("setHelpContextId"); //$NON-NLS-1$

	}

	public void addSourceProvider(ISourceProvider provider) {
		E4Util.unsupported("addSourceProvider"); //$NON-NLS-1$

	}

	public void removeSourceProvider(ISourceProvider provider) {
		E4Util.unsupported("removeSourceProvider"); //$NON-NLS-1$

	}

	public void dispose() {
		E4Util.unsupported("dispose"); //$NON-NLS-1$

	}

}
