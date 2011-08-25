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
package org.eclipse.e4.ui.css.core.util.resources;

import java.io.InputStream;
import java.io.Reader;

/**
 * Resources locator to get {@link InputStream} or {@link Reader} from an URI.
 */
public interface IResourceLocator extends IURIResolver {

	/**
	 * Return {@link InputStream} from the <code>uri</code>.
	 * 
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public InputStream getInputStream(String uri) throws Exception;

	/**
	 * Return {@link Reader} from the <code>uri</code>.
	 * 
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public Reader getReader(String uri) throws Exception;

}
