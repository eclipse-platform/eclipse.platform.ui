package org.eclipse.core.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * User data that can be attached to the element tree itself.
 */
public interface IElementTreeData extends Cloneable {
/**
 * ElementTreeData must define a publicly accessible clone method.
 * This method can simply invoke Object's clone method.
 */
public Object clone();
}
