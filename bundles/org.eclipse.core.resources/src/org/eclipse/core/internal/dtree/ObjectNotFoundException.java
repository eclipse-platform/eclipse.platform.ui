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
package org.eclipse.core.internal.dtree;

/**
 * This exception is thrown when an attempt is made to reference a source tree
 * element that does not exist in the given SourceTree
 */
public class ObjectNotFoundException extends RuntimeException {
/**
 * ObjectNotFoundException constructor comment.
 */
public ObjectNotFoundException() {
	super();
}
/**
 * ObjectNotFoundException constructor comment.
 * @param s java.lang.String
 */
public ObjectNotFoundException(String s) {
	super(s);
}
}
