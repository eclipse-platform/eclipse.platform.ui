package org.eclipse.ui.views.properties;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
/**
 * Interface used by <code>PropertySheetRoot</code> to obtain an 
 * <code>IPropertySource</code> for a given object.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IPropertySourceProvider {
/**
 * Returns a property source for the given object.
 *
 * @param object the object
 */
public IPropertySource getPropertySource(Object object);
}
