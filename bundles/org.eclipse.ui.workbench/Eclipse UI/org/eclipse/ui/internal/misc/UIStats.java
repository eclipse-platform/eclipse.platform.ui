/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal.misc;

import java.util.HashMap;

public class UIStats {
	
	private static int SIZE = 10;
	private static boolean debug[] = new boolean[SIZE];
	private static String startStrings[] = new String[SIZE];
	private static String endStrings[] = new String[SIZE];
	private static HashMap operations = new HashMap(); 
	
	public static int CREATE_PART = 0;
	public static int CREATE_PART_CONTROL = 1;
	public static int INIT_PART = 2;
	
	public static int CREATE_PERSPECTIVE = 3;
	public static int RESTORE_WORKBENCH = 4;
	public static int START_WORKBENCH = 5;
	public static int CREATE_PART_INPUT = 6;
	
	static {
		debug[CREATE_PART] = Policy.DEBUG_PART_CREATE;
		debug[CREATE_PART_INPUT] = Policy.DEBUG_PART_CREATE;
		debug[CREATE_PART_CONTROL] = Policy.DEBUG_PART_CREATE;
		debug[INIT_PART] = Policy.DEBUG_PART_CREATE;
		debug[CREATE_PERSPECTIVE] = Policy.DEBUG_PERSPECTIVE_CREATE;
		debug[RESTORE_WORKBENCH] = Policy.DEBUG_RESTORE_WORKBENCH;
		debug[START_WORKBENCH] = Policy.DEBUG_START_WORKBENCH;
		
		startStrings[CREATE_PART] = "Creating part: "; //$NON-NLS-1$
		endStrings[CREATE_PART] = " ms to create: "; //$NON-NLS-1$
		startStrings[CREATE_PART_INPUT] = "Creating part input: "; //$NON-NLS-1$
		endStrings[CREATE_PART_INPUT] = " ms to create input: "; //$NON-NLS-1$
		startStrings[CREATE_PART_CONTROL] = "Creating control: "; //$NON-NLS-1$
		endStrings[CREATE_PART_CONTROL] = " ms to create control: "; //$NON-NLS-1$
		startStrings[INIT_PART] = "Initializing part: "; //$NON-NLS-1$
		endStrings[INIT_PART] = " ms to init part: "; //$NON-NLS-1$	
		startStrings[CREATE_PERSPECTIVE] = "Creating perspective: "; //$NON-NLS-1$
		endStrings[CREATE_PERSPECTIVE] = " ms to create perspective:"; //$NON-NLS-1$
		startStrings[RESTORE_WORKBENCH] = "Restoring: "; //$NON-NLS-1$
		endStrings[RESTORE_WORKBENCH] = " ms to restore:"; //$NON-NLS-1$
		startStrings[START_WORKBENCH] = "Starting:"; //$NON-NLS-1$
		endStrings[START_WORKBENCH] = " ms to start:"; //$NON-NLS-1$

	}
	
	public static void start(int operation,String label) {
		if(debug[operation]) {
			System.out.println(startStrings[operation] + label);
			operations.put(operation + label,new Long(System.currentTimeMillis()));
		}
	}
	public static void end(int operation,String label) {
		if(debug[operation]) {
			Long startTime = (Long)operations.remove(operation + label);
			System.out.println(
				"Time - " + //$NON-NLS-1$
				(System.currentTimeMillis() - startTime.longValue()) + 
				endStrings[operation] +
				label);
		}
	}
}
