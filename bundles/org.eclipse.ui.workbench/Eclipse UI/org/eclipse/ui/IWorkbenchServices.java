/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * <p>
 * The list of service types officially support by the workbench. While
 * third-party plug-ins can contribute additional services, this is the list of
 * service types that the workbench provides initial support for.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public interface IWorkbenchServices {

	/**
	 * The service type that provides access to the current set of bindings.
	 * This service can be used for querying the current state of the binding
	 * architecture.
	 */
	public static final int BINDING = 0;

	/**
	 * The service type that provides access to the current list of commands, as
	 * read from the registry. This service can only be used to query the list
	 * of commands.
	 */
	public static final int COMMAND = 1;

	/**
	 * The service type that provides access to the current list of contexts.
	 * This also allows contexts to be activated or deactivated.
	 */
	public static final int CONTEXT = 2;

	/**
	 * The service type that provides access to the current list of handlers.
	 * This also allows handler to be attached or detached from a command.
	 */
	public static final int HANDLER = 3;

	/**
	 * The service type that provides access to the user interface elements
	 * controlled by the workbench. This service can be used for contributing
	 * additional elements, as well as controlling some aspects of the
	 * presentation.
	 */
	public static final int MENU = 4;

}
