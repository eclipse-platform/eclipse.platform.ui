package org.eclipse.ui.externaltools.internal.ant.editor.text;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
// 

/*
 * This file originates from an internal package of Eclipse's 
 * Manifest Editor. It has been copied by GEBIT to here in order to
 * permanently use those features. It has been renamed and edited by GEBIT 
 * after copying.
 */

import org.eclipse.swt.graphics.RGB;

/**
 * The color constants used by Planty.
 */
public interface PlantyColorConstants {
	RGB XML_COMMENT =       new RGB(128,   0,   0);
	RGB PROC_INSTR =        new RGB(128, 128, 128);
	RGB STRING=             new RGB(  0, 128,   0);
	RGB DEFAULT=            new RGB(  0,   0,   0);
	RGB TAG=                new RGB(  0,   0, 128);
	
	
	String P_XML_COMMENT = "planty.color.xml_comment";
	String P_PROC_INSTR = "planty.color.instr";
	String P_STRING = "planty.color.string";
	String P_DEFAULT = "planty.color.default";
	String P_TAG = "planty.color.tag";
}
