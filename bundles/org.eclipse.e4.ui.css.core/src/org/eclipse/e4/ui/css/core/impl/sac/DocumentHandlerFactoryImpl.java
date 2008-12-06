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
package org.eclipse.e4.ui.css.core.impl.sac;

import org.eclipse.e4.ui.css.core.sac.DocumentHandlerFactory;
import org.eclipse.e4.ui.css.core.sac.ExtendedDocumentHandler;

/**
 * This class implements the
 * {@link org.eclipse.e4.ui.css.core.sac.IDocumentHandlerFactory} interface.
 */
public class DocumentHandlerFactoryImpl extends DocumentHandlerFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.sac.IDocumentHandlerFactory#makeDocumentHandler()
	 */
	public ExtendedDocumentHandler makeDocumentHandler() {
		return new CSSDocumentHandlerImpl();
	}

}
