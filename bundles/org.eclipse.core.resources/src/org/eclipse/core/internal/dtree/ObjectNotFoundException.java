package org.eclipse.core.internal.dtree;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
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
