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
package org.eclipse.core.internal.indexing;

import org.eclipse.core.internal.utils.Messages;


public class IndexedStoreException extends StoreException {

	public static final int GenericError = 0;
	public static final int EntryKeyLengthError = 1;
	public static final int EntryNotRemoved = 2;
	public static final int EntryValueLengthError = 3;
	public static final int EntryValueNotUpdated = 4;
	public static final int IndexNodeNotRetrieved = 5;
	public static final int IndexNodeNotStored = 6;
	public static final int IndexNodeNotSplit = 7;
	public static final int IndexNodeNotCreated = 8;
	public static final int IndexExists = 9;
	public static final int IndexNotCreated = 10;
	public static final int IndexNotFound = 11;
	public static final int IndexNotRemoved = 12;
	public static final int ObjectExists = 13;
	public static final int ObjectNotAcquired = 14;
	public static final int ObjectNotCreated = 15;
	public static final int ObjectNotFound = 16;
	public static final int ObjectNotReleased = 17;
	public static final int ObjectNotRemoved = 18;
	public static final int ObjectNotUpdated = 19;
	public static final int ObjectNotStored = 20;
	public static final int ObjectTypeError = 21;
	public static final int StoreEmpty = 22;
	public static final int StoreFormatError = 23;
	public static final int StoreNotCreated = 24;
	public static final int StoreNotOpen = 25;
	public static final int StoreNotClosed = 26;
	public static final int StoreNotFlushed = 27;
	public static final int StoreNotOpened = 28;
	public static final int StoreNotReadWrite = 29;
	public static final int ContextNotAvailable = 30;
	public static final int ObjectIDInvalid = 31;
	public static final int MetadataRequestError = 32;
	public static final int EntryRemoved = 33;
	public static final int StoreNotConverted = 34;
	public static final int StoreIsOpen = 35;
	public static final int StoreNotCommitted = 36;
	public static final int StoreNotRolledBack = 37;

	public static String[] messages = new String[40];

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	static {
		initializeMessages();
	}

	public int id = GenericError;

	/**
	 * IndexedStoreException constructor comment.
	 */
	public IndexedStoreException(int id) {
		super(messages[id]);
		this.id = id;
	}

	/**
	 * IndexedStoreException constructor comment.
	 */
	public IndexedStoreException(int id, Throwable e) {
		super(messages[id], e);
		this.id = id;
	}

	/**
	 * IndexedStoreException constructor comment.
	 */
	public IndexedStoreException(String s) {
		super(s);
		id = GenericError;
	}

	/**
	 * Initializes the messages at class load time.
	 */
	private static void initializeMessages() {
		messages[GenericError] = Messages.indexedStore_genericError;
		messages[EntryKeyLengthError] = Messages.indexedStore_entryKeyLengthError;
		messages[EntryNotRemoved] = Messages.indexedStore_entryNotRemoved;
		messages[EntryValueLengthError] = Messages.indexedStore_entryValueLengthError;
		messages[EntryValueNotUpdated] = Messages.indexedStore_entryValueNotUpdated;
		messages[IndexNodeNotRetrieved] = Messages.indexedStore_indexNodeNotRetrieved;
		messages[IndexNodeNotStored] = Messages.indexedStore_indexNodeNotStored;
		messages[IndexNodeNotSplit] = Messages.indexedStore_indexNodeNotSplit;
		messages[IndexNodeNotCreated] = Messages.indexedStore_indexNodeNotCreated;
		messages[IndexExists] = Messages.indexedStore_indexExists;
		messages[IndexNotCreated] = Messages.indexedStore_indexNotCreated;
		messages[IndexNotFound] = Messages.indexedStore_indexNotFound;
		messages[IndexNotRemoved] = Messages.indexedStore_indexNotRemoved;
		messages[ObjectExists] = Messages.indexedStore_objectExists;
		messages[ObjectNotAcquired] = Messages.indexedStore_objectNotAcquired;
		messages[ObjectNotCreated] = Messages.indexedStore_objectNotCreated;
		messages[ObjectNotFound] = Messages.indexedStore_objectNotFound;
		messages[ObjectNotReleased] = Messages.indexedStore_objectNotReleased;
		messages[ObjectNotRemoved] = Messages.indexedStore_objectNotRemoved;
		messages[ObjectNotUpdated] = Messages.indexedStore_objectNotUpdated;
		messages[ObjectNotStored] = Messages.indexedStore_objectNotStored;
		messages[ObjectTypeError] = Messages.indexedStore_objectTypeError;
		messages[StoreEmpty] = Messages.indexedStore_storeEmpty;
		messages[StoreFormatError] = Messages.indexedStore_storeFormatError;
		messages[StoreNotCreated] = Messages.indexedStore_storeNotCreated;
		messages[StoreNotOpen] = Messages.indexedStore_storeNotOpen;
		messages[StoreNotClosed] = Messages.indexedStore_storeNotClosed;
		messages[StoreNotFlushed] = Messages.indexedStore_storeNotFlushed;
		messages[StoreNotOpened] = Messages.indexedStore_storeNotOpened;
		messages[StoreNotReadWrite] = Messages.indexedStore_storeNotReadWrite;
		messages[ContextNotAvailable] = Messages.indexedStore_contextNotAvailable;
		messages[ObjectIDInvalid] = Messages.indexedStore_objectIDInvalid;
		messages[MetadataRequestError] = Messages.indexedStore_metadataRequestError;
		messages[EntryRemoved] = Messages.indexedStore_entryRemoved;
		messages[StoreNotConverted] = Messages.indexedStore_storeNotConverted;
		messages[StoreIsOpen] = Messages.indexedStore_storeIsOpen;
		messages[StoreNotCommitted] = Messages.indexedStore_storeNotCommitted;
		messages[StoreNotRolledBack] = Messages.indexedStore_storeNotRolledBack;
	}

	/**
	 * Creates a printable representation of this exception.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(50);
		buffer.append("IndexedStoreException:"); //$NON-NLS-1$
		buffer.append(getMessage());
		if (wrappedException != null) {
			buffer.append("\n"); //$NON-NLS-1$
			buffer.append(wrappedException.toString());
		}
		return buffer.toString();
	}
}