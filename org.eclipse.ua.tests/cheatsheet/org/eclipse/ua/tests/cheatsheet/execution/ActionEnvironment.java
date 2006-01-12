/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.execution;

/**
 * Utility class used to save the result of executing an action and store 
 * environment including the cheatsheet manager
 */

public class ActionEnvironment {
	
	public static void reset() {
		params = null;
		timesCompleted = 0;
		throwException = false;
	}
	
	private static String[] params;
	private static int timesCompleted;
	private static boolean throwException;
	
	public static void setParams(String[] actualParams) {
		params = actualParams;
	}
	
	public static String[] getParams() {
		return params;
	}
	
	public static void actionCompleted() {
		timesCompleted++;
	}
	
	public static int getTimesCompleted() {
		return timesCompleted;
	}
	
	public static void setThrowException(boolean doThrowException) {
		throwException = doThrowException;
	}
	
	public static boolean shouldThrowException() {
		return throwException;
	}

}
