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

/*
 * A handler that is notified when the DOM processor visits an element,
 * allowing it to process the element and return a result.
 */
public abstract class DOMProcessorHandler {
	
	public static final short UNHANDLED = 0;
	public static final short HANDLED_CONTINUE = 1;
	public static final short HANDLED_SKIP = 2;
	
	private DOMProcessor processor;
	
	/*
	 * Will be called for every element visited by the processor,
	 * except those requested to be skipped.
	 */
	public abstract short handle(Element elem, String id);

	/*
	 * Returns the processor that is calling this handler.
	 */
	public DOMProcessor getProcessor() {
		return processor;
	}

	/*
	 * Sets the processor that is calling this handler. This should only
	 * be called by the processor.
	 */
	public void setProcessor(DOMProcessor processor) {
		this.processor = processor;
	}	
}
