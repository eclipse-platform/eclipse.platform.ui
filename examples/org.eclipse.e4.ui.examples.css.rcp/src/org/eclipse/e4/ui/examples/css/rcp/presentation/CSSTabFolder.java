/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.examples.css.rcp.presentation;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.presentations.defaultpresentation.DefaultTabFolder;

/**
 * @since 3.1
 */
public class CSSTabFolder extends DefaultTabFolder {

	public CSSTabFolder(Composite parent, int flags, boolean allowMin,
			boolean allowMax) {
		super(parent, flags, allowMin, allowMax);
		// TODO Auto-generated constructor stub
	}
    public void updateColors() {
    	//do nothing, CSS will handle
    }
}
