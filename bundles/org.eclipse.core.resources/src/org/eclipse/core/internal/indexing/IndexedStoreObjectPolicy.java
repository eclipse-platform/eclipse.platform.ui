package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class IndexedStoreObjectPolicy extends AbstractObjectPolicy {
	
	/**
	 * Default constructor
	 */
	public IndexedStoreObjectPolicy() {
		super();
	}

	/**
	 * Creates an IndexedStoreObject from a field.  The contents of the field are 
	 * used to create the internal structure of the object.  The field begins with a 
	 * two byte type code that is used to determine the type of object to create.
	 */
	public StoredObject createObject(Field field, ObjectStore store, ObjectAddress address) throws ObjectStoreException {
		int offset = StoredObject.TYPE_OFFSET;
		int length = StoredObject.TYPE_LENGTH;
		int type = field.subfield(offset, length).getInt();
		StoredObject object = null;
			switch (type) {
				case IndexAnchor.TYPE:
					object = new IndexAnchor(field, store, address);
					break;
				case IndexNode.TYPE:
					object = new IndexNode(field, store, address);
					break;
				case IndexedStoreContext.TYPE:
					object = new IndexedStoreContext(field, store, address);
					break;
				case BinarySmallObject.TYPE:
					object = new BinarySmallObject(field, store, address);
					break;
				default:
					throw new ObjectStoreException(ObjectStoreException.ObjectTypeFailure);
			} 
		return object;
	}

}
