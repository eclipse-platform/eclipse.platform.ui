package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 */
public interface IUMLock {
/**
 * Checks to see if the UMLock file is there.  If yes, returns true
 */
public boolean exists();
/**
 */
void remove();
/**
 */
void set();
/**
 */
void set(String msg);
}
