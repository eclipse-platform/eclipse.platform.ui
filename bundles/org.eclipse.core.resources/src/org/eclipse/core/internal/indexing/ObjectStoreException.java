package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

public class ObjectStoreException extends Exception {

	public final static int GenericFailure              = 0;
	public final static int InternalFailure             = 1;
	public final static int StoreCreateFailure          = 10;
	public final static int StoreConversionFailure      = 11;
	public final static int StoreOpenFailure            = 12;
	public final static int StoreCloseFailure           = 13;
	public final static int PageReadFailure             = 20;
	public final static int PageWriteFailure            = 21;
	public final static int PageVacancyFailure          = 22;
	public final static int ObjectTypeFailure           = 23;
	public final static int ObjectSizeFailure           = 24;
	public final static int ObjectExistenceFailure 	    = 25;
	public final static int ObjectHeaderFailure         = 26;
	public final static int ObjectInsertFailure         = 27;
	public final static int ObjectRemoveFailure         = 28;
	public final static int ObjectUpdateFailure         = 29;
	public final static int ObjectIsLocked              = 30;
	public final static int MetadataRequestFailure 	    = 40;

	public final static String[] message = new String[50];
	static {
		initializeMessages();
	}

	public int id = 0; // exception id

	public ObjectStoreException(int id) {
		super(message[id]);
		this.id = id;
	}
	public ObjectStoreException(String s) {
		super(s);
		this.id = GenericFailure;
	}
/**
 * Initializes the messages at class load time.
 */
private static void initializeMessages() {
	message[GenericFailure] = "Error occurred in object store operation.";
	message[InternalFailure] = "Internal error occurred in object store operation.";
	message[ObjectSizeFailure] = "Object is too large for page.";
	message[PageVacancyFailure] = "Object table on page is full.";
	message[ObjectExistenceFailure] = "Object was not found.";
	message[ObjectHeaderFailure] = "Object header format is invalid.";
	message[StoreCloseFailure] = "Error occurred during close of object store.";
	message[StoreCreateFailure] = "Error creating object store.";
	message[PageReadFailure] = "Error reading page from object store.";
	message[StoreOpenFailure] = "Error opening object store";
	message[ObjectInsertFailure] = "Error occurred writing object into page";
	message[PageWriteFailure] = "Cannot store page in page store";
	message[ObjectRemoveFailure] = "Error occurred removing object from page";
	message[ObjectUpdateFailure] = "Error updating object.";
	message[StoreConversionFailure] = "Error converting object store.";
	message[MetadataRequestFailure] = "Error getting or putting metadata.";
	message[ObjectTypeFailure] = "Type check failed.";
	message[ObjectIsLocked] = "Operation cannot be performed because the object is being used.";
}
}
