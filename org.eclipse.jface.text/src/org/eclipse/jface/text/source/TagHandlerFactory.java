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

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.Assert;

/**
 * Default implementation of a tag handler factory
 * 
 * @since 3.0
 */
public class TagHandlerFactory implements ITagHandlerFactory {

	private Map fHandlers;

	public void addTagHandler(String tag, ITagHandler handler)  {
		Assert.isNotNull(tag);
		Assert.isNotNull(handler);
		
		fHandlers.put(tag, handler);
	}

	public ITagHandler registerHandler(String tag)  {
		Assert.isNotNull(tag);
		return (ITagHandler)fHandlers.remove(tag);
	}


	/*
	 * @see org.eclipse.jface.text.source.ITagHandlerFactory#getHandler(java.lang.String)
	 */
	public ITagHandler getHandler(String tag) {
		Assert.isNotNull(tag);

		return (ITagHandler)fHandlers.get(tag);
	}

	/*
	 * @see org.eclipse.jface.text.source.ITagHandlerFactory#findHandler(java.lang.String)
	 */
	public ITagHandler findHandler(String text) {
		Assert.isNotNull(text);

		Iterator iter= fHandlers.values().iterator();
		while (iter.hasNext())  {
			ITagHandler handler= (ITagHandler)iter.next();
			if (handler.canHandleText(text))
				return handler;
		}
		return null;
	}
}
