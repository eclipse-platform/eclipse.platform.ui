package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.graphics.Color;

/**
 * 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 2.1
 */

public interface IConsoleDocumentContentProvider {

	public boolean isReadOnly();
	
	public boolean isTerminated();
	
	public Color getColor(String streamIdentifer);
	
	public void connect(IProcess process, IConsoleDocument partitioner);
	
	public void disconnect();
}
