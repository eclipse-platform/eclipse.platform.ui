package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.internal.utils.Policy;

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
		message[GenericFailure] = bind("objectStore.genericFailure");
		message[InternalFailure] = bind("objectStore.internalFailure");
		message[ObjectSizeFailure] = bind("objectStore.objectSizeFailure");
		message[PageVacancyFailure] = bind("objectStore.pageVacancyFailure");
		message[ObjectExistenceFailure] = bind("objectStore.objectExistenceFailure");
		message[ObjectHeaderFailure] = bind("objectStore.objectHeaderFailure");
		message[StoreCloseFailure] = bind("objectStore.storeCloseFailure");
		message[StoreCreateFailure] = bind("objectStore.storeCreateFailure");
		message[PageReadFailure] = bind("objectStore.pageReadFailure");
		message[StoreOpenFailure] = bind("objectStore.storeOpenFailure");
		message[ObjectInsertFailure] = bind("objectStore.objectInsertFailure");
		message[PageWriteFailure] = bind("objectStore.pageWriteFailure");
		message[ObjectRemoveFailure] = bind("objectStore.objectRemoveFailure");
		message[ObjectUpdateFailure] = bind("objectStore.objectUpdateFailure");
		message[StoreConversionFailure] = bind("objectStore.storeConversionFailure");
		message[MetadataRequestFailure] = bind("objectStore.metadataRequestFailure");
		message[ObjectTypeFailure] = bind("objectStore.objectTypeFailure");
		message[ObjectIsLocked] = bind("objectStore.objectIsLocked");
	}

	private static String bind(String name) {
		return Policy.bind(name);
	}
	
}
