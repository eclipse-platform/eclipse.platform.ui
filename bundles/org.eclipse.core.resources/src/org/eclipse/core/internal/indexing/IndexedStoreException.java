package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.internal.utils.Policy;

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
		messages[GenericError] = bind("indexedStore.genericError");
		messages[EntryKeyLengthError] = bind("indexedStore.entryKeyLengthError");
		messages[EntryNotRemoved] = bind("indexedStore.entryNotRemoved");
		messages[EntryValueLengthError] = bind("indexedStore.entryValueLengthError");
		messages[EntryValueNotUpdated] = bind("indexedStore.entryValueNotUpdated");
		messages[IndexNodeNotRetrieved] = bind("indexedStore.indexNodeNotRetrieved");
		messages[IndexNodeNotStored] = bind("indexedStore.indexNodeNotStored");
		messages[IndexNodeNotSplit] = bind("indexedStore.indexNodeNotSplit");
		messages[IndexNodeNotCreated] = bind("indexedStore.indexNodeNotCreated");
		messages[IndexExists] = bind("indexedStore.indexExists");
		messages[IndexNotCreated] = bind("indexedStore.indexNotCreated");
		messages[IndexNotFound] = bind("indexedStore.indexNotFound");
		messages[IndexNotRemoved] = bind("indexedStore.indexNotRemoved");
		messages[ObjectNotCreated] = bind("indexedStore.objectNotCreated");
		messages[ObjectExists] = bind("indexedStore.objectExists");
		messages[ObjectNotFound] = bind("indexedStore.objectNotFound");
		messages[ObjectNotAcquired] = bind("indexedStore.objectNotAcquired");
		messages[ObjectNotReleased] = bind("indexedStore.objectNotReleased");
		messages[ObjectNotRemoved] = bind("indexedStore.objectNotRemoved");
		messages[ObjectTypeError] = bind("indexedStore.objectTypeError");
		messages[ObjectNotUpdated] = bind("indexedStore.objectNotUpdated");
		messages[ObjectNotStored] = bind("indexedStore.objectNotStored");
		messages[StoreNotCreated] = bind("indexedStore.storeNotCreated");
		messages[StoreEmpty] = bind("indexedStore.storeEmpty");
		messages[StoreFormatError] = bind("indexedStore.storeFormatError");
		messages[StoreNotOpen] = bind("indexedStore.storeNotOpen");
		messages[StoreNotReadWrite] = bind("indexedStore.storeNotReadWrite");
		messages[StoreNotOpened] = bind("indexedStore.storeNotOpened");
		messages[StoreNotClosed] = bind("indexedStore.storeNotClosed");
		messages[StoreNotFlushed] = bind("indexedStore.storeNotFlushed");
		messages[ContextNotAvailable] = bind("indexedStore.contextNotAvailable");
		messages[ObjectIDInvalid] = bind("indexedStore.objectIDInvalid");
		messages[EntryRemoved] = bind("indexedStore.entryRemoved");
		messages[StoreNotConverted] = bind("indexedStore.storeNotConverted");
	}
	
	private static String bind(String name) {
		return Policy.bind(name);
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
