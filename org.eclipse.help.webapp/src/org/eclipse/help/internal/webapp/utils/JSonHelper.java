/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.utils;

public final class JSonHelper {
	
	//JSON Characters
	public static final String BEGIN_BRACE = "{"; //$NON-NLS-1$
	public static final String END_BRACE  = "}"; //$NON-NLS-1$
	public static final String QUOTE  = "\'"; //$NON-NLS-1$
	public static final String COLON  = ":"; //$NON-NLS-1$
	public static final String BEGIN_BRACKET  = "["; //$NON-NLS-1$
	public static final String END_BRACKET  =   "]"; //$NON-NLS-1$
	public static final String COMMA  =   ","; //$NON-NLS-1$
	public static final String NEWLINE  =   "\n"; //$NON-NLS-1$
	public static final String SPACE  =   "   "; //$NON-NLS-1$
	
	//JSON items
	public static final String LABEL			= "label"; //$NON-NLS-1$
	public static final String IDENTIFIER		= "identifier"; //$NON-NLS-1$
	public static final String URL				= "url"; //$NON-NLS-1$
	public static final String PROVIDER			= "provider"; //$NON-NLS-1$
	public static final String ITEMS			= "items"; //$NON-NLS-1$
	public static final String NAME				= "name"; //$NON-NLS-1$
	public static final String TITLE			= "title"; //$NON-NLS-1$
	public static final String ID				= "id"; //$NON-NLS-1$
	public static final String PLUGIN_ID		= "pluginId"; //$NON-NLS-1$
	public static final String HREF				= "href"; //$NON-NLS-1$
	public static final String TYPE				= "type"; //$NON-NLS-1$
	public static final String CHECKED			= "checked"; //$NON-NLS-1$
	public static final String CHILDREN			= "children"; //$NON-NLS-1$
	public static final String REFERENCE		= "_reference"; //$NON-NLS-1$
	public static final String IS_LEAF			= "is_leaf"; //$NON-NLS-1$
	public static final String IS_SELECTED		= "is_selected"; //$NON-NLS-1$
	public static final String IS_HIGHLIGHTED	= "is_highlighted"; //$NON-NLS-1$
	public static final String TOC				= "toc"; //$NON-NLS-1$
	public static final String PATH				= "path"; //$NON-NLS-1$
	public static final String CATEGORY			= "category"; //$NON-NLS-1$
	public static final String DESCRIPTION		= "description"; //$NON-NLS-1$
	public static final String CATEGORY_HREF	= CATEGORY+"_"+HREF; //$NON-NLS-1$
	public static final String PROPERTY_NAME	= "tagName"; //$NON-NLS-1$
	public static final String PROPERTY_VALUE	= "value"; //$NON-NLS-1$
	public static final String INDEX			= "Index"; //$NON-NLS-1$
	public static final String TOPIC			= "Topic"; //$NON-NLS-1$
	public static final String NUMERIC_PATH		= "NumericPath"; //$NON-NLS-1$
	
	public static String getQuotes(String str){
		return ((str != null)?QUOTE + str + QUOTE:""); //$NON-NLS-1$
	}

}
