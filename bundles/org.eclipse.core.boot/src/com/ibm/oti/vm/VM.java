/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package com.ibm.oti.vm;

/*
 * Dummy class to fill in for the real J9 VM support class.
 * This class will be overridden when running the platform on J9
 * because the real class is part of the boot class path.
 */

abstract public class VM {

public static void enableClassHotSwap(Class clazz) {
}
public static void setClassPathImpl(ClassLoader cl, String path) {
}

}
