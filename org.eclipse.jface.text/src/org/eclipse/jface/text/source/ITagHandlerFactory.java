/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.source;

/**
 * A tag handler factory provides access to tag
 * handlers.
 * 
 * @since 3.0
 */
public interface ITagHandlerFactory {
	
	/**
	 * Returns a handler that can handle the
	 * given tag.
	 * <p>
	 * Depending on the used handler the factory might
	 * return a new or a shared instance.</p> 
	 *
	 * @param tag the tag for which to return the handler 
	 * @return a tag handler or <code>null</code> if no handler
	 * 			is available
	 */
	public ITagHandler getHandler(String tag);
	
	/**
	 * Finds and returns a handler that can handle
	 * the given text snippet.
	 * <p>
	 * Depending on the used handler the factory might
	 * return a new or a shared instance.</p> 
	 * 
	 * @param text the text for which to find a handler
	 * @return a tag handler or <code>null</code> if no handler
	 * 			is available
	 */
	public ITagHandler findHandler(String text);
}
