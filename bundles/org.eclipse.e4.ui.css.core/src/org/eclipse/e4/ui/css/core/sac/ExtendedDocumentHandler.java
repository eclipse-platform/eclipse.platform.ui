/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.sac;

import java.util.Stack;

import org.w3c.css.sac.DocumentHandler;

/**
 * Extends {@link DocumentHandler} to get the root node.
 */
public interface ExtendedDocumentHandler extends DocumentHandler {

	/**
	 * Return root node.
	 *
	 * @return
	 */
	public Object getNodeRoot();

	/**
	 * Set node stack.
	 *
	 * @param statck
	 */
	public void setNodeStack(Stack statck);
}
