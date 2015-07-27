/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.sac;

/**
 * Factory interface to get instance of {@link ExtendedDocumentHandler}.
 */
public interface IDocumentHandlerFactory {

	/**
	 * Return default instance of {@link ExtendedDocumentHandler}.
	 *
	 * @return
	 */
	public ExtendedDocumentHandler makeDocumentHandler();

}
