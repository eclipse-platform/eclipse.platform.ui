package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public abstract class ObjectStorePolicy {

	/**
	 * Constructs a new instance of an ObjectStorePolicy, invoked only
	 * by subclasses since this class is abstract.
	 */
	public ObjectStorePolicy() {
		super();
	}
	
	/** 
	 * Creates a new instance of an object for this object store.  Uses
	 * the contents of the field to decide what type of object to create.
	 * The object may be a pagemapped object, if so the page is passed
	 * to the creator so that modifications to a page object 
	 * can be noted as the object is modified.  Objects which are not page
	 * mapped may choose to ignore this argument.
	 */
	public abstract StoredObject createObject(Field field, ObjectPage page);
	
}
