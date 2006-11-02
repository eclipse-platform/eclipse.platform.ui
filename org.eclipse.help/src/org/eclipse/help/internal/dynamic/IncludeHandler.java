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
package org.eclipse.help.internal.dynamic;

import org.eclipse.help.Node;

/*
 * The handler responsible for processing includes, where a node is pulled
 * in from another document.
 */
public class IncludeHandler extends NodeHandler {

	private static final String ELEMENT_INCLUDE = "include"; //$NON-NLS-1$
	private static final String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
	
	private IncludeResolver resolver;
	private String locale;
	
	/*
	 * Creates the handler. It needs to know which locale the current document
	 * is in in order to pull content from the correct locale.
	 */
	public IncludeHandler(String locale) {
		this.locale = locale;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.dynamic.DocumentProcessorHandler#handle(org.eclipse.help.Node, java.lang.String)
	 */
	public short handle(Node node, String id) {
		if (ELEMENT_INCLUDE.equals(node.getName())) {
			String path = node.getAttribute(ATTRIBUTE_PATH);
			if (path != null && path.length() > 0) {
				String bundleId = getBundleId(path);
				String relativePath = getRelativePath(path);
				String elementId = getElementId(path);
				if (bundleId != null && relativePath != null && elementId != null) {
					resolveInclude(bundleId, relativePath, elementId, node, locale);
				}
			}
			else {
				// remove invalid includes
				node.getParent().removeChild(node);
			}
			return HANDLED_SKIP;
		}
		return UNHANDLED;
	}
	
	/*
	 * Processes the include; replaces the node with the one described by
	 * the parameters.
	 */
	private void resolveInclude(String bundleId, String relativePath, String elementId, Node node, String locale) {
		if (resolver == null) {
			resolver = new IncludeResolver(getProcessor(), locale);
		}
		Node parent = node.getParent();
		if (parent != null) {
			try {
				Node nodeToInclude = resolver.resolve(bundleId, relativePath, elementId);
				parent.insertBefore(nodeToInclude, node);
				parent.removeChild(node);
			}
			catch (Throwable t) {
				// remove invalid includes
				parent.removeChild(node);
			}
		}
	}
	
	/*
	 * Extracts the bundle ID from the given path.
	 */
	private String getBundleId(String path) {
		if (path.charAt(0) == '/') {
			int index = path.indexOf('/', 1);
			if (index > 1) {
				return path.substring(1, index);
			}
		}
		else {
			// legacy - handle no slash at beginning
			int index = path.indexOf('/');
			if (index != -1) {
				return path.substring(0, index);
			}
		}
		return null;
	}
	
	/*
	 * Extracts the bundle-relative path from the given full path.
	 */
	private String getRelativePath(String path) {
		int startIndex = path.indexOf('/', 1);
		int endIndex = path.indexOf('#');
		if (endIndex == -1) {
			// legacy - can use slash in place of '#'
			endIndex = path.lastIndexOf('/');
		}
		if (startIndex != -1 && endIndex > startIndex + 1) {
			return path.substring(startIndex + 1, endIndex);
		}
		return null;
	}
	
	/*
	 * Extracts the element id from the given path.
	 */
	private String getElementId(String path) {
		int index = path.indexOf('#');
		if (index == -1) {
			// legacy - can use slash in place of '#'
			index = path.lastIndexOf('/');
		}
		if (index != -1 && index < path.length() - 1) {
			return path.substring(index + 1);
		}
		return null;
	}
}
