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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder2;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Sash;

/**
 * ColorSchemeService is the service that sets the colors on widgets as
 * appropriate.
 */
public class ColorSchemeService {

	static void setSchemeColors(Control control) {

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

		if (control instanceof Composite) {
			setCompositeColors((Composite) control);
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

	}

	public static void setTabColors(CTabFolder2 control) {

//		Color back = control.getDisplay().getSystemColor(
//				SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
//		Color fore = control.getDisplay().getSystemColor(
//				SWT.COLOR_WIDGET_FOREGROUND);
//		Color selback = control.getDisplay().getSystemColor(
//				SWT.COLOR_INFO_BACKGROUND);
//		Color selfore = control.getDisplay().getSystemColor(
//				SWT.COLOR_WIDGET_LIGHT_SHADOW);
//		
//		control.setSelectionBackground(selback);
//		control.setSelectionForeground(selfore);
//		control.setBackground(back);
//		control.setForeground(fore);
		
		
		control.setBackground(
			control.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
//			JFaceColors.getSchemeBackground(control.getDisplay()));
		control.setForeground(
			control.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));			
//			JFaceColors.getSchemeForeground(control.getDisplay()));
		control.setSelectionBackground(
			control.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
			//new Color(control.getDisplay(), 255,0,0));
			//control.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			//JFaceColors.getSchemeSelectionBackground(parent.getDisplay()));
		control.setSelectionForeground(
			control.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		control.setBorderColor(
				control.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
//			JFaceColors.getSchemeSelectionForeground(control.getDisplay()));
//		Control[] children = control.getChildren();
//		for (int i = 0; i < children.length; i++) {
//			setSchemeColors(children[i]);
//		}
		
//		Composite parent = control.getParent();
//		while (parent != null) {
//			parent.setBackground(
//				JFaceColors.getSchemeParentBackground(parent.getDisplay()));
//			parent = parent.getParent();
//		}
	}

	static void setCompositeColors(Composite control) {

//		control.setBackground(
//			JFaceColors.getSchemeBackground(control.getDisplay()));
//		control.setForeground(
//			JFaceColors.getSchemeForeground(control.getDisplay()));

		Control[] children = control.getChildren();
		for (int i = 0; i < children.length; i++) {
			setSchemeColors(children[i]);
		}
	}
}
