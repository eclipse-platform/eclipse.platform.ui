/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands.ws;

import java.util.Map;

import org.eclipse.ui.commands.IHandler;

public interface ICommandService {

    void addHandlerSubmission(String commandId, IHandler handler);

    void addHandlerSubmissions(Map handlersByCommandId);

    void removeHandlerSubmission(String commandId, IHandler handler);

    void removeHandlerSubmissions(Map handlersByCommandId);
}