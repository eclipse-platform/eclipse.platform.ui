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
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.console.*;
import org.eclipse.jface.text.Position;

/**
 */
public class HyperlinkPosition extends Position {
	
	public static final String HYPER_LINK_CATEGORY = DebugUIPlugin.getUniqueIdentifier() + ".HYPER_LINK"; //$NON-NLS-1$
	
	private IConsoleHyperlink fLink = null;

	/**
	 * 
	 */
	public HyperlinkPosition(IConsoleHyperlink link, int offset, int length) {
		super(offset, length);
		fLink = link;
	}
	
	public IConsoleHyperlink getHyperLink() {
		return fLink;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg) {
		return arg instanceof HyperlinkPosition && super.equals(arg) && getHyperLink().equals(((HyperlinkPosition)arg).getHyperLink());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() + getHyperLink().hashCode();
	}

}
