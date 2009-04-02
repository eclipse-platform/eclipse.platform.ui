/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20070201   154100 pmoogk@ca.ibm.com - Peter Moogk, Port internet code from WTP to Eclipse base.
 *******************************************************************************/
package org.eclipse.ui.internal.net;

import org.eclipse.core.internal.net.ProxySelector;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;

public class NonProxyHostsLabelProvider extends BaseLabelProvider implements
		ITableLabelProvider, IColorProvider {

	public NonProxyHostsLabelProvider() {
		super();
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element == null) {
			return null;
		}
		ProxyBypassData data = (ProxyBypassData) element;
		switch (columnIndex) {
		case 0:
			return null;
		case 1:
			return data.getHost();
		case 2:
			return ProxySelector.localizeProvider(data.getSource());
		default:
			return null;
		}
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void createColumns(TableViewer viewer) {
		String[] titles = {
				"", //$NON-NLS-1$
				NetUIMessages.ProxyPreferencePage_13,
				NetUIMessages.ProxyPreferencePage_14 };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableViewerColumn(viewer, SWT.NONE)
					.getColumn();
			column.setText(titles[i]);
			column.setResizable(true);
		}
	}

	public Color getBackground(Object element) {
		if (element instanceof ProxyBypassData) {
			String provider = ((ProxyBypassData) element).getSource();
			if (!ProxySelector.canSetBypassHosts(provider)) {
				return Display.getCurrent().getSystemColor(
						SWT.COLOR_INFO_BACKGROUND);
			}
		}
		return null;
	}

	public Color getForeground(Object element) {
		return null;
	}

}
