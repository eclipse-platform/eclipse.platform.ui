/*******************************************************************************
 * Copyright (c) 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.internal.theme;

import org.eclipse.e4.ui.css.swt.theme.ITheme;

public class Theme implements ITheme {
	private String id;
	private String label;
	
	public Theme(String id, String label) {
		this.id = id;
		this.label = label;
	}
	
	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
}