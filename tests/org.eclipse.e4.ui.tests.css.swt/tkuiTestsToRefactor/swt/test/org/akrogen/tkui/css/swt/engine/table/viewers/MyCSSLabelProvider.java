/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.akrogen.tkui.css.swt.engine.table.viewers;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.jface.viewers.CSSLabelProvider;
import org.eclipse.jface.viewers.TableViewer;

public class MyCSSLabelProvider extends CSSLabelProvider {

	public MyCSSLabelProvider(CSSEngine engine, TableViewer tableViewer) {
		super(engine, tableViewer);
	}
	
	public String getText(Object element) {		
		return element.toString();
	} 
	
}
