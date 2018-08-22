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
package org.eclipse.e4.ui.css.core.impl.sac;

import org.eclipse.e4.ui.css.core.sac.DocumentHandlerFactory;
import org.eclipse.e4.ui.css.core.sac.ExtendedDocumentHandler;

/**
 * This class implements the
 * {@link org.eclipse.e4.ui.css.core.sac.IDocumentHandlerFactory} interface.
 */
public class DocumentHandlerFactoryImpl extends DocumentHandlerFactory {

	@Override
	public ExtendedDocumentHandler makeDocumentHandler() {
		return new CSSDocumentHandlerImpl();
	}

}
