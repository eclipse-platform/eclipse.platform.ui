/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *******************************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor.text;

import org.eclipse.swt.graphics.RGB;

/**
 * The color constants used by the Ant Editor.
 */
public interface IAntEditorColorConstants {
	RGB XML_COMMENT =       new RGB(128,   0,   0);
	RGB PROC_INSTR =        new RGB(128, 128, 128);
	RGB STRING=             new RGB(  0, 128,   0);
	RGB DEFAULT=            new RGB(  0,   0,   0);
	RGB TAG=                new RGB(  0,   0, 128);
	
	
	String P_XML_COMMENT = "antEditor.color.xml_comment"; //$NON-NLS-1$
	String P_PROC_INSTR = "antEditor.color.instr"; //$NON-NLS-1$
	String P_STRING = "antEditor.color.string"; //$NON-NLS-1$
	String P_DEFAULT = "antEditor.color.default"; //$NON-NLS-1$
	String P_TAG = "antEditor.color.tag"; //$NON-NLS-1$
}
