package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

public class IndexedStoreException extends Exception {

	public static final int GenericError                =  0;
	public static final int EntryKeyLengthError         =  1;
	public static final int EntryNotRemoved             =  2;
	public static final int EntryValueLengthError       =  3;
	public static final int EntryValueNotUpdated        =  4;
	public static final int IndexNodeNotRetrieved       =  5;
	public static final int IndexNodeNotStored          =  6;
	public static final int IndexNodeNotSplit           =  7;
	public static final int IndexNodeNotCreated         =  8;
	public static final int IndexExists                 =  9;
	public static final int IndexNotCreated             = 10;
	public static final int IndexNotFound               = 11;
	public static final int IndexNotRemoved             = 12;
	public static final int ObjectExists                = 13;
	public static final int ObjectNotAcquired           = 14;
	public static final int ObjectNotCreated            = 15;
	public static final int ObjectNotFound              = 16;
	public static final int ObjectNotReleased           = 17;
	public static final int ObjectNotRemoved            = 18;
	public static final int ObjectNotUpdated            = 19;
	public static final int ObjectNotStored             = 20;
	public static final int ObjectTypeError             = 21;
	public static final int StoreEmpty                  = 22;
	public static final int StoreFormatError            = 23;
	public static final int StoreNotCreated             = 24;
	public static final int StoreNotOpen                = 25;
	public static final int StoreNotClosed              = 26;
	public static final int StoreNotFlushed             = 27;
	public static final int StoreNotOpened              = 28;
	public static final int StoreNotReadWrite           = 29;
	public static final int ContextNotAvailable         = 30;
	public static final int ObjectIDInvalid             = 31;
	public static final int MetadataRequestError        = 32;
	public static final int EntryRemoved                = 33;
	public static final int StoreNotConverted           = 34;
	public static final int StoreIsOpen					= 35;
	public static final int StoreNotCommitted           = 36;
	public static final int StoreNotRolledBack          = 37;
	

	public static String[] messages = new String[40];

	static {
		initializeMessages();
	}

	public int id = GenericError;
	public Throwable previousError = null;


	
	/**
	 * IndexedStoreException constructor comment.
	 */
	public IndexedStoreException(int id) {
		super(messages[id]);
		this.id = id;
		previousError = null;
	}
	/**
	 * IndexedStoreException constructor comment.
	 */
	public IndexedStoreException(int id, Throwable e) {
		super(messages[id]);
		this.id = id;
		previousError = e;
	}
	/**
	 * IndexedStoreException constructor comment.
	 */
	public IndexedStoreException(String s) {
		super(s);
		id = GenericError;
		previousError = null;
	}
/**
 * Initializes the messages at class load time.
 */
private static void initializeMessages() {
	messages[GenericError] = "An error occurred during an indexed store operation.";
	messages[EntryKeyLengthError] = "Key length > 1024.";
	messages[EntryNotRemoved] = "Cannot remove an index entry referenced by other cursors.";
	messages[EntryValueLengthError] = "Value length > 2048.";
	messages[EntryValueNotUpdated] = "Error occurred updating value in leaf node.";
	messages[IndexNodeNotRetrieved] = "Error occurred getting index node.";
	messages[IndexNodeNotStored] = "Error occurred storing index node.";
	messages[IndexNodeNotSplit] = "Error occurred spliting an index node.";
	messages[IndexNodeNotCreated] = "Index node not created.";
	messages[IndexExists] = "Index already exists.";
	messages[IndexNotCreated] = "Index was not created.";
	messages[IndexNotFound] = "Index not found.";
	messages[IndexNotRemoved] = "Error occurred removing index from the store.";
	messages[ObjectNotCreated] = "Error occurred creating a new object.";
	messages[ObjectExists] = "Object already exists.";
	messages[ObjectNotFound] = "Object not found.";
	messages[ObjectNotAcquired] = "Object has not been acquired for the intent needed for this operation.";
	messages[ObjectNotReleased] = "Cannot release an object that has not been acquired.";
	messages[ObjectNotRemoved] = "Error occurred removing an object from the store.";
	messages[ObjectTypeError] = "Unexpected object type found.";
	messages[ObjectNotUpdated] = "Error occurred updating an object.";
	messages[ObjectNotStored] = "Object was not stored.";
	messages[StoreNotCreated] = "Store does not exist and cannot be created.";
	messages[StoreEmpty] = "Could not find root context because store is empty";
	messages[StoreFormatError] = "Could not find root context because store has invalid format";
	messages[StoreNotOpen] = "Store has not been opened.";
	messages[StoreNotReadWrite] = "Attempted to set a Modify intent for an object in a read-only store.";
	messages[StoreNotOpened] = "Error occurred opening indexed store.";
	messages[StoreNotClosed] = "Error closing indexed store.";
	messages[StoreNotFlushed] = "Error flushing indexed store.";
	messages[ContextNotAvailable] = "Error occurred accessing the indexed store context";
	messages[ObjectIDInvalid] = "ObjectID format is invalid.";
	messages[EntryRemoved] = "Element at this cursor has been removed.";
	messages[StoreNotConverted] = "The indexed store opened is not the current version and no conversion routine exists.";
}
	/**
	 * Creates a printable representation of this exception.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(50);
		buffer.append("IndexedStoreException:");
		buffer.append(getMessage());
		if (previousError != null) {
			buffer.append("\n");
			buffer.append(previousError.toString());
		}
		return buffer.toString();
	}
}
