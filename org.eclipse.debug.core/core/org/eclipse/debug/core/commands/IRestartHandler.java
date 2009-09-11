/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.commands;

/**
 * A restart command allows the debugger to quickly restart the current debug 
 * session without terminating and re-launching.  
 * <p>
 * Clients may implement this interface. The debug platform provides a
 * restart action that delegates to this handler interface. Platform does not 
 * provide a default implementation of this handler, so to enable this action
 * the debugger implementation must provide one.
 * </p>
 * 
 * @since 3.6
 */
public interface IRestartHandler extends IDebugCommandHandler {

}
