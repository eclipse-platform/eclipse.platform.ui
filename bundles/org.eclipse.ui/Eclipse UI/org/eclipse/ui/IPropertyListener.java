package org.eclipse.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Interface for listening for property changes on an object.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * <p>
 * [Issue: Potential confusion with org.eclipse.jface.IPropertyChangeListener.]
 * </p>
 *
 * @see IWorkbenchPart#addPropertyListener
 */
public interface IPropertyListener {
/**
 * Indicates that a property has changed.
 *
 * @param source the object whose property has changed
 * @param propId the id of the property which has changed; property ids
 *   are generally defined as constants on the source class
 */
public void propertyChanged(Object source, int propId);
}
