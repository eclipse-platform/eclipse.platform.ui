/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.properties;

import java.util.*;
import org.eclipse.core.internal.indexing.IndexCursor;
import org.eclipse.core.internal.indexing.ObjectID;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;

/**
 *
 */
public class PropertyStore {

	// The indexed store will maintain the properties
	protected IndexedStoreWrapper store = null;

	// Add directives
	public static final int CREATE = 0; // must not exist
	public static final int UPDATE = 1; // must exist
	public static final int SET_UPDATE = 2; // create if doesn't exist, update if exists
	public static final int SET_SKIP = 3; // create if doesn't exist, don't update if exists

	// Remove directives
	public static final int IGNORE_MISSING = 0;
	public static final int FAIL_MISSING = 1;

	public PropertyStore(IPath location) {
		store = new IndexedStoreWrapper(location);
	}

	protected boolean basicExists(StoreKey searchKey) throws CoreException {
		byte[] searchBytes = searchKey.toBytes();
		IndexCursor cursor = store.getCursor();
		try {
			cursor.find(searchBytes);
			boolean exists = cursor.keyEquals(searchBytes);
			cursor.close();
			return exists;
		} catch (Exception e) {
			String message = Policy.bind("properties.couldNotReadProp", searchKey.getQualifier(), searchKey.getLocalName()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, searchKey.getResourceName().getPath(), message, e);
		}
	}

	/**
	 * The caller is responsible for ensuring that this will not produce
	 * duplicate keys in the index.
	 */
	protected void basicInsert(StoreKey key, String value) throws CoreException {
		try {
			ObjectID valueID = store.createObject(value);
			store.getIndex().insert(key.toBytes(), valueID);
		} catch (Exception e) {
			String message = Policy.bind("properties.couldNotWriteProp", key.getQualifier(), key.getLocalName()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, key.getResourceName().getPath(), message, e);
		}
	}

	protected boolean basicRemove(ResourceName resourceName, QualifiedName propertyName) throws CoreException {
		StoreKey key = new StoreKey(resourceName, propertyName);
		byte[] keyBytes = key.toBytes();
		boolean wasFound = false;
		IndexCursor cursor = store.getCursor();
		try {
			cursor.find(keyBytes);
			if (cursor.keyEquals(keyBytes)) {
				wasFound = true;
				ObjectID valueID = cursor.getValueAsObjectID();
				store.removeObject(valueID);
				cursor.remove();
			}
			cursor.close();
		} catch (Exception e) {
			String message = Policy.bind("properties.couldNotDeleteProp", key.getQualifier(), key.getLocalName()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_DELETE_LOCAL, resourceName.getPath(), message, e);
		}
		return wasFound;
	}

	protected void basicUpdate(StoreKey key, String value) throws CoreException {
		byte[] keyBytes = key.toBytes();
		IndexCursor cursor = store.getCursor();
		try {
			cursor.find(keyBytes);
			if (cursor.keyEquals(keyBytes)) {
				ObjectID oldID = cursor.getValueAsObjectID();
				store.removeObject(oldID);
				ObjectID newValueId = store.createObject(value);
				cursor.updateValue(newValueId);
			}
			cursor.close();
		} catch (Exception e) {
			String message = Policy.bind("properties.couldNotWriteProp", key.getQualifier(), key.getLocalName()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, key.getResourceName().getPath(), message, e);
		}
	}

	protected synchronized void commonSet(ResourceName resourceName, StoredProperty[] properties, int depth, int setMode, QueryResults failures) throws CoreException {
		if (depth == IResource.DEPTH_ZERO) {
			for (int i = 0; i < properties.length; i++) {
				StoredProperty property = properties[i];
				StoreKey key = new StoreKey(resourceName, property.getName());
				boolean exists = basicExists(key);
				if ((exists && (setMode == CREATE)) || (!exists && (setMode == UPDATE)))
					failures.add(resourceName, property);
				else if (exists && (setMode != SET_SKIP))
					basicUpdate(key, property.getStringValue());
				else
					basicInsert(key, property.getStringValue());
			}
		} else {
			Enumeration resourceNamesEnum = deepResourceNames(resourceName);
			while (resourceNamesEnum.hasMoreElements())
				commonSet((ResourceName) resourceNamesEnum.nextElement(), properties, IResource.DEPTH_ZERO, setMode, failures);
		}
	}

	/**
	 * Returns the names of all resources that are rooted at the given resource.
	 * <p>
	 * Answers an <code>Enumeration</code> of <code>IResourceName</code>.
	 * The enumerator will include (at least) <code>resourceName</code> if it
	 * exists.  If <code>resourceName</code> does not exist returns an empty
	 * enumerator.
	 *
	 * @see ResourceName
	 * @param resourceName the name of the top most resource to match.
	 * @return an enumeration of matching resource names.
	 */
	public Enumeration deepResourceNames(ResourceName resourceName) throws CoreException {
		final Set resultHolder = new HashSet(10);
		IVisitor visitor = new IVisitor() {
			public void visit(ResourceName resourceName, StoredProperty property, IndexCursor cursor) {
				resultHolder.add(resourceName);
			}

			public boolean requiresValue(ResourceName resourceName, QualifiedName propertyName) {
				return false;
			}
		};
		recordsDeepMatching(resourceName, visitor);
		return Collections.enumeration(resultHolder);
	}

	/**
	 * Returns the named property for the given resource.
	 * <p>
	 * The retieval is performed to depth zero.  Returns <code>null</code>
	 * if there is no such property defined on the resource.
	 *
	 * @param resourceName the resource name to match.
	 * @param propertyName the property name to match.
	 * @return the matching property, or <code>null</code> if no such property.
	 */
	public StoredProperty get(ResourceName resourceName, final QualifiedName propertyName) throws CoreException {
		final Object[] resultHolder = new Object[1];
		IVisitor simpleVisitor = new IVisitor() {
			public void visit(ResourceName resourceName, StoredProperty property, IndexCursor cursor) {
				resultHolder[0] = property;
			}

			public boolean requiresValue(ResourceName resourceName, QualifiedName propertyName) {
				return true;
			}
		};
		recordsMatching(resourceName, propertyName, simpleVisitor);
		return (StoredProperty) resultHolder[0];
	}

	/**
	 * Returns all the properties for a given resource.
	 * <p>
	 * Answer a <code>QueryResults</code> containing <code>StoredProperty</code>.
	 * If there are no matches returns an empty <code>QueryResults</code></p>
	 * <p>
	 * The depth parameter allows searching based on resource name path prefix.</p>
	 *
	 * @see QueryResults
	 * @param resourceName the resource name to match.
	 * @param depth the scope of the query
	 * @return a <code>QueryResults</code> with the matching properties.
	 */
	public QueryResults getAll(ResourceName resourceName, int depth) throws CoreException {
		final QueryResults result = new QueryResults();
		IVisitor visitor = new IVisitor() {
			public void visit(ResourceName resourceName, StoredProperty property, IndexCursor cursor) {
				result.add(resourceName, property);
			}

			public boolean requiresValue(ResourceName resourceName, QualifiedName propertyName) {
				return true;
			}
		};
		if (depth == IResource.DEPTH_ZERO)
			recordsMatching(resourceName, visitor);
		else
			recordsDeepMatching(resourceName, visitor);
		return result;
	}

	/**
	 * Returns all the property names for a given resource.
	 * <p>
	 * The result is a <code>QueryResults</code> containing <code>QualifiedName</code>.  
	 * If the resource has no defined properties, the method returns
	 * an empty <code>QueryResults</code>.</p>
	 * <p>
	 * The depth parameter allows searching based on resource name path prefix.</p>
	 *
	 * @param resourceName the resource name to match.
	 * @param depth the depth to which the query runs.
	 * @return a <code>QueryResults</code> containing the property names.
	 */
	public QueryResults getNames(ResourceName resourceName, int depth) throws CoreException {
		QueryResults results = new QueryResults();
		if (depth == IResource.DEPTH_ZERO)
			recordsMatching(resourceName, propertyNameVisitor(results));
		else
			recordsDeepMatching(resourceName, propertyNameVisitor(results));
		return results;
	}

	/**
	 * Returns true if the property store is up and running.  Returns false if
	 * the store has been shutdown.
	 */
	public boolean isRunning() {
		return store != null;
	}

	protected IVisitor propertyNameVisitor(final QueryResults results) {
		return new IVisitor() {
			public void visit(ResourceName resourceName, StoredProperty property, IndexCursor cursor) {
				results.add(resourceName, property.getName());
			}

			public boolean requiresValue(ResourceName resourceName, QualifiedName propertyName) {
				return false;
			}
		};
	}

	/**
	 * Matches all properties for a given resource.
	 */
	protected void recordsDeepMatching(ResourceName resourceName, IVisitor visitor) throws CoreException {

		// Build the partial 'search' key
		StoreKey searchKey = new StoreKey(resourceName, true);
		byte[] searchBytes = searchKey.toBytes();
		int probe = searchBytes.length;
		// Position a cursor over the first matching key
		IndexCursor cursor = store.getCursor();
		try {
			cursor.find(searchBytes);

			// While we have a prefix match
			while (cursor.keyMatches(searchBytes)) {
				// Must check that the prefix is up to a valid path segment
				// note that the matching bytes length is > search key length since
				//      properties MUST have a local name.
				byte[] matchingBytes = cursor.getKey();
				if (probe == 1 || //empty path is a valid prefix for all paths
						(matchingBytes[probe] == 0) || // a full path match
						(matchingBytes[probe] == 47 /*IPath.SEPARATOR*/)) {
					// a segment boundary match
					visitPropertyAt(cursor, visitor);
				}
				// else the match is intra-segment and therefore invalid
				cursor.next();
			}
			cursor.close();
		} catch (Exception e) {
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, resourceName.getPath(), Policy.bind("properties.storeProblem"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Matches all properties for a given resource.
	 */
	protected void recordsMatching(ResourceName resourceName, IVisitor visitor) throws CoreException {

		// Build the partial 'search' key
		StoreKey searchKey = new StoreKey(resourceName, false);
		byte[] searchBytes = searchKey.toBytes();
		// Position a cursor over the first matching key
		IndexCursor cursor = store.getCursor();
		try {
			cursor.find(searchBytes);

			// While we have a prefix match, evaluate the visitor
			while (cursor.keyMatches(searchBytes)) {
				visitPropertyAt(cursor, visitor);
				cursor.next();
			}
			cursor.close();
		} catch (Exception e) {
			store.reset();
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, resourceName.getPath(), Policy.bind("properties.storeProblem"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Matches the given property for a given resource.
	 * Note that there should be only one.
	 */
	protected void recordsMatching(ResourceName resourceName, QualifiedName propertyName, IVisitor visitor) throws CoreException {

		// Build the full 'search' key
		StoreKey searchKey = new StoreKey(resourceName, propertyName);
		byte[] searchBytes = searchKey.toBytes();
		// Position a cursor over the first matching key
		IndexCursor cursor = store.getCursor();
		try {
			cursor.find(searchBytes);

			// If we have an exact match, evaluate the visitor
			if (cursor.keyEquals(searchBytes))
				visitPropertyAt(cursor, visitor);
			cursor.close();
		} catch (Exception e) {
			store.reset();
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, resourceName.getPath(), Policy.bind("properties.storeProblem"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Remove the given collection of named properties from the given resource.
	 * <p>
	 * All of the properties being removed must exist already on the
	 * resource based on the removeRule parameter.  If the rule is
	 * MISSING_IGNORE then attempts to remove properties that do not exist
	 * are ignored, if the rule is MISSING_FAIL the method will throw
	 * a <code>PropertyNotFoundException</code> if the property does not exist.
	 * <p>
	 * If an exception is thrown, all properties that did previously
	 * exist will have been removed from the resource.  To determine which
	 * properties caused the exception see the offenders result in the exception.</p>
	 * <p>
	 * The depth parameter allows matching based on resource name path prefix.</p>
	 *
	 * @param resourceName the resource containing the properties.
	 * @param propertyNames the property names to remove.
	 * @param depth the scope for matching the resource name.
	 * @param removeRule the behavior when removing non-existant properties.
	 * @exception CoreException
	 */
	public QueryResults remove(ResourceName resourceName, QualifiedName[] propertyNames, int depth, int removeRule) throws CoreException {
		QueryResults failures = new QueryResults();
		if (depth == IResource.DEPTH_ZERO) {
			for (int i = 0; i < propertyNames.length; i++) {
				boolean found = basicRemove(resourceName, propertyNames[i]);
				if (!found && (removeRule == FAIL_MISSING))
					failures.add(resourceName, propertyNames[i]);
			}
		} else {
			Enumeration resourceNamesEnum = deepResourceNames(resourceName);
			while (resourceNamesEnum.hasMoreElements()) {
				ResourceName resName = (ResourceName) resourceNamesEnum.nextElement();
				for (int i = 0; i < propertyNames.length; i++) {
					boolean found = basicRemove(resName, propertyNames[i]);
					if (!found && (removeRule == FAIL_MISSING))
						failures.add(resName, propertyNames[i]);
				}
			}
		}
		return failures;
	}

	/**
	 * Remove the named property from the given resource.
	 * <p>
	 * If a matching property does not exist on this resource
	 * the method has no affect on the store.  Removal is performed
	 * to depth zero.</p>
	 * <p>
	 * @param resourceName the resource containing the property.
	 * @param propertyName the property to remove.
	 */
	public void remove(ResourceName resourceName, QualifiedName propertyName) throws CoreException {
		remove(resourceName, new QualifiedName[] {propertyName}, IResource.DEPTH_ZERO, IGNORE_MISSING);
	}

	/**
	 * Remove all the properties from a given resource.
	 * <p>
	 * The depth parameter allows matching based on resource name path prefix.</p>
	 *
	 * @param resourceName the resource containing the properties.
	 * @param depth the scope for matching the resource name.
	 */
	public void removeAll(ResourceName resourceName, int depth) throws CoreException {
		QueryResults namesSearch = getNames(resourceName, depth);
		Enumeration resourceNamesEnum = namesSearch.getResourceNames();
		while (resourceNamesEnum.hasMoreElements()) {
			ResourceName resName = (ResourceName) resourceNamesEnum.nextElement();
			Enumeration propertyNamesEnum = Collections.enumeration(namesSearch.getResults(resName));
			while (propertyNamesEnum.hasMoreElements()) {
				QualifiedName propertyName = (QualifiedName) propertyNamesEnum.nextElement();
				basicRemove(resName, propertyName);
			}
		}
	}

	/**
	 * Sets the given collection of properties on the given resource.
	 * <p>
	 * The addRule determines whether the properties must already exist
	 * or not, and if they do whether they are updated by subsequent addition.  
	 * Valid addRule values are defined in <code>IPropertyCollectionConstants</code>.
	 * <p>
	 * The depth parameter allows matching based on resource name path prefix.</p>
	 * <p>
	 * The <code>PropertyExistsException</code> is thrown if the matching resource
	 * already has a property of the same name, and the rule requires that
	 * it must not.  If the exception is thrown, all successfull properties
	 * will have been set, and the failures are listed in the exception.</p>
	 *
	 * @param resourceName the resource to receive the properties.
	 * @param properties the properties to add.
	 * @param depth the depth at which to apply the add opertion.
	 * @param mode the behavior of the add operation.
	 * @exception CoreException
	 */
	public QueryResults set(ResourceName resourceName, StoredProperty[] properties, int depth, int mode) throws CoreException {
		QueryResults failures = new QueryResults();
		commonSet(resourceName, properties, depth, mode, failures);
		return failures;
	}

	/**
	 * Sets the given property to the given resource.
	 * <p>
	 * The property is added to depth zero.  If the resource already has
	 * a proprety with the same name, it's value is updated to the given
	 * value (i.e. SET_UPDATE add rule equivalent.)</p>
	 *
	 * @param resourceName the resource to receive the property.
	 * @param property the property to add.
	 */
	public void set(ResourceName resourceName, StoredProperty property) throws CoreException {
		commonSet(resourceName, new StoredProperty[] {property}, IResource.DEPTH_ZERO, SET_UPDATE, null);
	}

	public void shutdown(IProgressMonitor monitor) {
		if (store == null)
			return;
		try {
			store.close();
		} finally {
			//null the store so other threads with a handle on it cannot use it
			store = null;
		}
	}

	public void startup(IProgressMonitor monitor) {
		//do nothing
	}

	protected void visitPropertyAt(IndexCursor cursor, IVisitor visitor) throws CoreException {
		try {
			StoreKey key = new StoreKey(cursor.getKey());
			ResourceName resourceName = key.getResourceName();
			QualifiedName propertyName = key.getPropertyName();
			String propertyValue = null;
			if (visitor.requiresValue(resourceName, propertyName))
				propertyValue = store.getObjectAsString(cursor.getValueAsObjectID());
			visitor.visit(resourceName, new StoredProperty(propertyName, propertyValue), cursor);
		} catch (Exception e) {
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, null, Policy.bind("properties.storeProblem"), e); //$NON-NLS-1$
		}
	}

	public void commit() throws CoreException {
		store.commit();
	}
}