package org.eclipse.core.internal.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.indexing.IndexCursor;

/* package */ interface IVisitor {
/**
 * Answers whether the visitor requires that the <code>IStoredProperty</code>
 * has it's value filled when matched from the store.
 * <p>
 * Retrieving the property value may be expensive in time and memory usage
 * (dependent upon the size of the property value.)</p>
 * <p>
 * If the visitor answers true, the <code>visit()</code> method will be
 * invoked with a complete property; if the visitor answers false the property
 * value will be <code>null</code>.
 *
 * @param resourceName the name of the matching resource
 * @param propertyName the name of the matching property
 * @return whether the visit method requires a value
 */
public boolean requiresValue(ResourceName resourceName, QualifiedName propertyName);
/**
 * Performs whatever actions are appropriate to the visitor when a
 * match is made to the property store query.
 * <p>
 * This method is invoked each time a matching entry is found in the store.</p>
 * <p>
 * Note that the property will have a <code>null</code> value if the <code>
 * requiresValue()</code> method returned <code>false</code> for this property.
 *
 * @see IndexCursor
 * @param resourceName the matching resource.
 * @param property the matching property.
 * @param cursor the cursor positioned at this property in the <code>IndexStore</code>.
 */
public void visit(ResourceName resourceName, StoredProperty property, IndexCursor cursor) throws CoreException;
}
