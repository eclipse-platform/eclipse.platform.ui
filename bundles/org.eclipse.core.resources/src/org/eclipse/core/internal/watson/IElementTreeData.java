package org.eclipse.core.internal.watson;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
