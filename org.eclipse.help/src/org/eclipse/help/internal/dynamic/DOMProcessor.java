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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * A DOM processor that traverses every element of a DOM and allows
 * handlers to perform operations on the elements.
 */
public class DOMProcessor {

	private DOMProcessorHandler[] handlers;
	
	/*
	 * Creates a processor with the given handlers.
	 */
	public DOMProcessor(DOMProcessorHandler[] handlers) {
		this.handlers = handlers;
		for (int i=0;i<handlers.length;++i) {
			handlers[i].setProcessor(this);
		}
	}
	
	/*
	 * Processes the given element and all its descendants, which exist
	 * inside a document identified by the given id.
	 */
	public void process(Element element, String id) {
		for (int i=0;i<handlers.length;++i) {
			short result = handlers[i].handle(element, id);
			if (result == DOMProcessorHandler.HANDLED_CONTINUE) {
				// handler wants us to keep processing children
				break;
			}
			if (result == DOMProcessorHandler.HANDLED_SKIP) {
				// handler wants us to skip children
				return;
			}
		}
		// process each child
		Node child = element.getFirstChild();
		while (child != null) {
			Node next = child.getNextSibling();
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				process((Element)child, id);
			}
			child = next;
		}
	}
}
