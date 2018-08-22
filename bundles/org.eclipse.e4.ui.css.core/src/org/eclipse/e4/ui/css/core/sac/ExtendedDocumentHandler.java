/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	public void setNodeStack(Stack<Object> statck);
}
