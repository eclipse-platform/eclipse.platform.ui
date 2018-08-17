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
import org.akrogen.tkui.css.jface.viewers.CSSTableLabelProvider;
import org.akrogen.tkui.css.swt.engine.table.CSSSWTTableViewerEngineTest2;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;

public class MyCSSTableLabelProvider extends CSSTableLabelProvider {

	public MyCSSTableLabelProvider(CSSEngine engine, TableViewer tableViewer) {
		super(engine, tableViewer);
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		CSSSWTTableViewerEngineTest2.Item item = (CSSSWTTableViewerEngineTest2.Item) element;
		switch (columnIndex) {
		case 0:
			return item.getItem0();
		case 1:
			return item.getItem1();
		}
		return null;
	}
}
