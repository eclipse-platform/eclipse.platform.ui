package com.ibm.oti.vm;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
