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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

/**
 * ColorSchemeService is the service that sets the colors on widgets as
 * appropriate.
 */
public class ColorSchemeService {

	static void setSchemeColors(Control control) {
		
		if(control instanceof CTabFolder){
			setTabColors((CTabFolder) control);
			return;
		}
		
		if(control instanceof Composite){
			setCompositeColors((Composite) control);
			return;
		}
		
		if(control instanceof List){
			return;
		}
		
		if(control instanceof Tree){
			return;
		}
		
		if(control instanceof StyledText){
			return;
		}
		
		if(control instanceof Table){
			return;
		}
		
		control.setBackground(
			JFaceColors.getSchemeBackground(control.getDisplay()));
		control.setForeground(
			JFaceColors.getSchemeForeground(control.getDisplay()));

	}
	
	static void setTabColors(CTabFolder control) {
		
		control.setSelectionBackground(
				JFaceColors.getSchemeSelectionBackground(control.getDisplay()));
		control.setSelectionForeground(
				JFaceColors.getSchemeSelectionForeground(control.getDisplay()));
		Control[] children = control.getChildren();
		for (int i = 0; i < children.length; i++) {
			setSchemeColors(children[i]);
		}
	}


	static void setCompositeColors(Composite control) {
		
		
		control.setBackground(
				JFaceColors.getSchemeBackground(control.getDisplay()));
		control.setForeground(
				JFaceColors.getSchemeForeground(control.getDisplay()));
		
		Control[] children = control.getChildren();
		for (int i = 0; i < children.length; i++) {
			setSchemeColors(children[i]);
		}
	}
}
