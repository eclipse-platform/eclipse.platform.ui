package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
