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
package org.eclipse.help.internal.base;

import org.eclipse.help.UAContentFilter;
import org.eclipse.help.internal.FilterableUAElement;
import org.eclipse.help.internal.xhtml.XHTMLSupport;

/*
 * The help system's content filter implementation.
 */
public class HelpContentFilter extends UAContentFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.help.UAContentFilter#isFilteredInternal(java.lang.Object)
	 */
	public boolean isFilteredInternal(Object element) {
		if (element instanceof FilterableUAElement) {
			return !XHTMLSupport.getFilterProcessor().isFilteredIn((FilterableUAElement)element);
		}
		else if (element instanceof String) {
			return !XHTMLSupport.getFilterProcessor().isFilteredIn((String)element);
		}
		return false;
	}
}
