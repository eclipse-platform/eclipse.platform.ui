package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	message[GenericFailure] = Policy.bind("objectStore.genericFailure");
	message[InternalFailure] = Policy.bind("objectStore.internalFailure");
	message[ObjectSizeFailure] = Policy.bind("objectStore.objectSizeFailure");
	message[PageVacancyFailure] = Policy.bind("objectStore.pageVacancyFailure");
	message[ObjectExistenceFailure] = Policy.bind("objectStore.objectExistenceFailure");
	message[ObjectHeaderFailure] = Policy.bind("objectStore.objectHeaderFailure");
	message[StoreCloseFailure] = Policy.bind("objectStore.storeCloseFailure");
	message[StoreCreateFailure] = Policy.bind("objectStore.storeCreateFailure");
	message[PageReadFailure] = Policy.bind("objectStore.pageReadFailure");
	message[StoreOpenFailure] = Policy.bind("objectStore.storeOpenFailure");
	message[ObjectInsertFailure] = Policy.bind("objectStore.objectInsertFailure");
	message[PageWriteFailure] = Policy.bind("objectStore.pageWriteFailure");
	message[ObjectRemoveFailure] = Policy.bind("objectStore.objectRemoveFailure");
	message[ObjectUpdateFailure] = Policy.bind("objectStore.objectUpdateFailure");
	message[StoreConversionFailure] = Policy.bind("objectStore.storeConversionFailure");
	message[MetadataRequestFailure] = Policy.bind("objectStore.metadataRequestFailure");
	message[ObjectTypeFailure] = Policy.bind("objectStore.objectTypeFailure");
	message[ObjectIsLocked] = Policy.bind("objectStore.objectIsLocked");
}
}
