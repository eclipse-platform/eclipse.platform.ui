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
 * A document processor that traverses every node of a document and allows
 * handlers to perform operations on the nodes.
 */
public class DocumentProcessor {

	private DocumentProcessorHandler[] handlers;
	
	/*
	 * Creates a processor with the given handlers.
	 */
	public DocumentProcessor(DocumentProcessorHandler[] handlers) {
		this.handlers = handlers;
		for (int i=0;i<handlers.length;++i) {
			handlers[i].setProcessor(this);
		}
	}
	
	/*
	 * Processes the given node and all its descendants, which exist
	 * inside a document identified by the given id.
	 */
	public void process(Node node, String id) {
		for (int i=0;i<handlers.length;++i) {
			short result = handlers[i].handle(node, id);
			if (result == DocumentProcessorHandler.HANDLED_CONTINUE) {
				// handler wants us to keep processing children
				break;
			}
			if (result == DocumentProcessorHandler.HANDLED_SKIP) {
				// handler wants us to skip children
				return;
			}
		}
		// process each child
		Node[] children = node.getChildren();
		for (int i=0;i<children.length;++i) {
			process(children[i], id);
		}
	}
}
