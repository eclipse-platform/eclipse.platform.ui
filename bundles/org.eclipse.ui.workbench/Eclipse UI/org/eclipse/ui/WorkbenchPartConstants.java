/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * This class describes the constants used for <link>IWorkbenchPart</link> properties 
 * 
 * @since 3.0
 */
public class WorkbenchPartConstants {
	
	/**
	 * The property id for <code>getTitle</code>, <code>getTitleImage</code>
	 * and <code>getTitleToolTip</code>.
	 */
	public static final int PROP_TITLE = 0x001;
	
	/**
	 * The property id for <code>ISaveablePart.isDirty()</code>.
	 * 
	 * @since 3.0
	 */
	public static final int PROP_DIRTY = 0x101;
	
	/**
	 * The property id for <code>IEditorPart.getEditorInput()</code>.
	 * 
	 * @since 3.0
	 */
	public static final int PROP_INPUT = 0x102;
	
	/**
	 * The property id for <code>IWorkbenchPart2.getPartName</code>
	 * 
	 * @since 3.0
	 */
	public static final int PROP_PART_NAME = 0x104;
	
	/**
	 * The property id for <code>IWorkbenchPart2.getContentDescription()</code>
	 * 
	 * @since 3.0
	 */
	public static final int PROP_CONTENT_DESCRIPTION = 0x105;
	
	
	private WorkbenchPartConstants() {};
}
