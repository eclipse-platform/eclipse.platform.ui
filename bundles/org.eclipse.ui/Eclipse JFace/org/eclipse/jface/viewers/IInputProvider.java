package org.eclipse.jface.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
/**
 * Interface common to all objects that provide an input.
 */
public interface IInputProvider {
/**
 * Returns the input.
 *
 * @return the input object
 */
public Object getInput();
}
