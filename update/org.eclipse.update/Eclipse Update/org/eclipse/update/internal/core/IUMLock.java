package org.eclipse.update.internal.core;

/**
 * Insert the type's description here.
 * Creation date: (4/20/01 12:44:17 PM)
 * @author: Linda Chui
 */
public interface IUMLock {
/**
 * Checks to see if the UMLock file is there.  If yes, returns true
 */
public boolean exists();
/**
 * Insert the method's description here.
 * Creation date: (4/20/01 12:45:16 PM)
 */
void remove();
/**
 * Insert the method's description here.
 * Creation date: (4/20/01 12:45:01 PM)
 */
void set();
/**
 * Insert the method's description here.
 * Creation date: (4/20/01 12:45:01 PM)
 */
void set(String msg);
}
