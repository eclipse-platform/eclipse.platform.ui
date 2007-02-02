/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.dynamic;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.UAContentFilter;
import org.eclipse.help.internal.UAElement;

/*
 * The handler responsible for filtering elements. Filters can either be
 * an attribute of the element to filter, or any number of child filter
 * elements.
 */
public class FilterHandler extends ProcessorHandler {

	private IEvaluationContext context;
	
	public FilterHandler(IEvaluationContext context) {
		this.context = context;
	}

	public short handle(UAElement element, String id) {
		if (UAContentFilter.isFiltered(element, context)) {
			UAElement parent = element.getParentElement();
			if (parent != null) {
				parent.removeChild(element);
			}
			return HANDLED_SKIP;
		}
		return UNHANDLED;
	}
}
