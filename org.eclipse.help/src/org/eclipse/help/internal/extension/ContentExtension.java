/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.extension;

import org.eclipse.help.IContentExtension;

/*
 * A concrete model element that implements IContentExtension.
 */
public class ContentExtension implements IContentExtension {
	
	private String content;
	private String path;
	private int type;
	
	/*
	 * Creates the extension with the given info.
	 */
	public ContentExtension(String content, String path, int type) {
		this.content = content;
		this.path = path;
		this.type = type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IContentExtension#getContent()
	 */
	public String getContent() {
		return content;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IContentExtension#getPath()
	 */
	public String getPath() {
		return path;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.IContentExtension#getType()
	 */
	public int getType() {
		return type;
	}
}