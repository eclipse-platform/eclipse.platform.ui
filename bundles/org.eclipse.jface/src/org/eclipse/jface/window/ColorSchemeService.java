/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.window;

import org.eclipse.jface.resource.JFaceColors;

import org.eclipse.swt.custom.CTabFolder2;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Sash;

/**
 * ColorSchemeService is the service that sets the colors on widgets as
 * appropriate.
 */
public class ColorSchemeService {

	static void setSchemeColors(Control control) {

		if (control instanceof Composite) {
			setCompositeColors((Composite) control);
			return;
		}
		
		if (control instanceof List) {
			return;
		}

		if (control instanceof Tree) {
			return;
		}

		if (control instanceof StyledText) {
			return;
		}

		if (control instanceof Table) {
			return;
		}

		if (control instanceof CTabFolder2) {
			setTabColors((CTabFolder2) control);
			return;
		}

		if (control instanceof Sash) {
//			control.setBackground(
//			JFaceColors.getSchemeParentBackground(control.getDisplay()));
			return;
		}

//		control.setBackground(
//			JFaceColors.getSchemeBackground(control.getDisplay()));
//		control.setForeground(
//			JFaceColors.getSchemeForeground(control.getDisplay()));

//		if (control instanceof CBanner) {
//			setCBannerColors((CBanner)control);
//		}
	}

//	/**
//	 * @param banner
//	 */
//	public static void setCBannerColors(CBanner control) {
//		Display d = control.getDisplay();
//		control.setBackground(JFaceColors.getSchemeBackground(d));
//		control.setForeground(JFaceColors.getTabFolderSelectionBackground(d));		
//		
//	}

	public static void setTabColors(CTabFolder2 control) {
		Display d = control.getDisplay();
		control.setBackground(JFaceColors.getTabFolderBackground(d));
		control.setForeground(JFaceColors.getTabFolderForeground(d));
		control.setSelectionBackground(JFaceColors.getTabFolderSelectionBackground(d));
		control.setSelectionForeground(JFaceColors.getTabFolderSelectionForeground(d));
	}

	static void setCompositeColors(Composite control) {
		Control[] children = control.getChildren();
		for (int i = 0; i < children.length; i++) {
			setSchemeColors(children[i]);
		}
	}

	/**
	 * @param control
	 */
	public static void setCoolBarColors(Control control) {
		setBasicColors(control);
	}

	private static void setBasicColors(Control control) {
		control.setBackground(
				JFaceColors.getSchemeBackground(control.getDisplay()));
		control.setForeground(
				JFaceColors.getSchemeForeground(control.getDisplay()));
	}

	/**
	 * @param bar
	 */
	public static void setPerspectiveToolBarColors(ToolBar control) {
		setBasicColors(control);
	}
}
