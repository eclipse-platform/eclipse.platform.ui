/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

/**
 * Default handler for command.  It ensures that the keyboard accelerator works even
 * if the menu action set is not enabled.
 * 
 * @since 3.8
 */
public class SuspendCommandHandler extends DebugCommandHandler {

    protected Class getCommandType() {
        return ISuspendHandler.class;
    }
    
}
