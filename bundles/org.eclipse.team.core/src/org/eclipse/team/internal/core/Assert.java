/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core;

import org.eclipse.osgi.util.NLS;

 

public final class Assert {
	public static class AssertionFailedException extends RuntimeException {
	    // Field required to avoid compiler warning
		private static final long serialVersionUID = -3361573629971779153L;
        public AssertionFailedException() {
		}
		public AssertionFailedException(String detail) {
			super(NLS.bind(Messages.Assert_assertionFailed, new String[] { detail })); 
		}
	}
/* This class is not intended to be instantiated. */
private Assert() {
}
/** Asserts that an argument is legal. If the given boolean is
 * not <code>true</code>, an <code>IllegalArgumentException</code>
 * is thrown.
 *
 * @param expression the outcode of the check
 * @return <code>true</code> if the check passes (does not return
 *    if the check fails)
 * @exception IllegalArgumentException if the legality test failed
 */
public static boolean isLegal(boolean expression) {
	return isLegal(expression, ""); //$NON-NLS-1$
}
/** Asserts that an argument is legal. If the given boolean is
 * not <code>true</code>, an <code>IllegalArgumentException</code>
 * is thrown.
 * The given message is included in that exception, to aid debugging.
 *
 * @param expression the outcode of the check
 * @param message the message to include in the exception
 * @return <code>true</code> if the check passes (does not return
 *    if the check fails)
 * @exception IllegalArgumentException if the legality test failed
 */
public static boolean isLegal(boolean expression, String message) {
	if (!expression)
		throw new IllegalArgumentException(message);
	return expression;
}
/** Asserts that the given object is not <code>null</code>. If this
 * is not the case, some kind of unchecked exception is thrown.
 * 
 * @param object the value to test
 * @exception IllegalArgumentException if the object is <code>null</code>
 */
public static void isNotNull(Object object) {
	if (object == null)
		throw new AssertionFailedException("null argument"); //$NON-NLS-1$
}
/** Asserts that the given object is not <code>null</code>. If this
 * is not the case, some kind of unchecked exception is thrown.
 * The given message is included in that exception, to aid debugging.
 *
 * @param object the value to test
 * @param message the message to include in the exception
 * @exception IllegalArgumentException if the object is <code>null</code>
 */
public static void isNotNull(Object object, String message) {
	if (object == null)
		throw new AssertionFailedException("null argument:" /*non NLS*/ + message); //$NON-NLS-1$
}
/** Asserts that the given boolean is <code>true</code>. If this
 * is not the case, some kind of unchecked exception is thrown.
 *
 * @param expression the outcode of the check
 * @return <code>true</code> if the check passes (does not return
 *    if the check fails)
 */
public static boolean isTrue(boolean expression) {
	return isTrue(expression, ""/*nonNLS*/); //$NON-NLS-1$
}
/** Asserts that the given boolean is <code>true</code>. If this
 * is not the case, some kind of unchecked exception is thrown.
 * The given message is included in that exception, to aid debugging.
 *
 * @param expression the outcode of the check
 * @param message the message to include in the exception
 * @return <code>true</code> if the check passes (does not return
 *    if the check fails)
 */
public static boolean isTrue(boolean expression, String message) {
	if (!expression)
		throw new AssertionFailedException("assert failed:" /*non NLS*/ + message); //$NON-NLS-1$
	return expression;
}
/**
 * Indicates that the caller has not implemented the method.
 * Usually this is a temporary condition.
 */
public static void notYetImplemented() {
}
}
