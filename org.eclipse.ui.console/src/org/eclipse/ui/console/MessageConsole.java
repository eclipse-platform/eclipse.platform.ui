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
package org.eclipse.ui.console;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console that displays messages. A message console may have one or
 * more streams connected to it (<code>MessageConsoleStream</code>).
 * Text written to streams is buffered and processed in a Job by the 
 * console's document partitioner.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 3.0
 */
public class MessageConsole extends IOConsole {

	public MessageConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
	}
		
	/**
	 * Returns a new message stream connected to this console.
	 * 
	 * @return a new message stream connected to this console
	 */
	public MessageConsoleStream newMessageStream() {
		return new MessageConsoleStream(this);
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsole#createPage(org.eclipse.ui.console.IConsoleView)
     */
    public IPageBookViewPage createPage(IConsoleView view) {
        IOConsolePage page = (IOConsolePage) super.createPage(view);
        page.setReadOnly();
        return page;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IOConsole#getInputStream()
     */
    public IOConsoleInputStream getInputStream() {
        throw new UnsupportedOperationException("Message Console does not support user input"); //$NON-NLS-1$
    }
}
