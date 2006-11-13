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

import org.eclipse.help.Node;
import org.eclipse.help.internal.NodeAdapter;

/*
 * Adapts a "context" Node and provides convenience methods for operating
 * on it. All methods operate on the underlying adapted Node.
 */
public class ContentExtension extends NodeAdapter {
	
	private static final String NAME_CONTRIBUTION = "contribution"; //$NON-NLS-1$
	private static final String NAME_REPLACEMENT = "replacement"; //$NON-NLS-1$
	private static final String NAME_CONTRIBUTION_LEGACY = "topicExtension"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CONTENT = "content"; //$NON-NLS-1$
	private static final String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
	
	// type for contribution into anchor
	public static final int CONTRIBUTION = 0;
	
	// type for element replacement
	public static final int REPLACEMENT = 1;
	
	/*
	 * Constructs a new content extension adapter for an empty extension node.
	 * (contribution type by default).
	 */
	public ContentExtension() {
		super();
		setNodeName(NAME_CONTRIBUTION);
	}
	
	/*
	 * Constructs a new adapter for the given content extension node.
	 */
	public ContentExtension(Node node) {
		super(node);
	}
	
	/*
	 * Returns the extension's content.
	 */
	public String getContent() {
		return getAttribute(ATTRIBUTE_CONTENT);
	}
	
	/*
	 * Returns the extensions target path.
	 */
	public String getPath() {
		return getAttribute(ATTRIBUTE_PATH);
	}
	
	/*
	 * Returns the extension type (either a contribution or replace).
	 */
	public int getType() {
		String name = getNodeName();
		return (NAME_CONTRIBUTION.equals(name) || NAME_CONTRIBUTION_LEGACY.equals(name)) ? CONTRIBUTION : REPLACEMENT;
	}
	
	/*
	 * Sets the extension's content.
	 */
	public void setContent(String content) {
		setAttribute(ATTRIBUTE_CONTENT, content);
	}
	
	/*
	 * Sets the extension's target path.
	 */
	public void setPath(String path) {
		setAttribute(ATTRIBUTE_PATH, path);
	}
	
	/*
	 * Sets the extension type.
	 */
	public void setType(int type) {
		setNodeName(type == CONTRIBUTION ? NAME_CONTRIBUTION : NAME_REPLACEMENT);
	}
}