/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.provisional;

import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * A command that can be enabled or disabled and executed.
 * 
 * @since 3.3
 */
public interface IDebugCommand {
	
	/**
	 * Determines whether this command can be executed on the specified element.
	 * 
	 * @param element element to operate on
	 * @param monitor accepts result
	 */
	public void canExecute(Object element, IBooleanRequestMonitor monitor);
	
	/**
	 * Executes this command on the specified element reporting any status
	 * to the given monitor and returns whether this command should
	 * remain enabled or become disabled until the request completes.
	 * 
	 * @param element element to perform capability on
	 * @param monitor status monitor
	 * @return whether the command remains enabled
	 */
	public boolean execute(Object element, IAsynchronousRequestMonitor monitor);

}
