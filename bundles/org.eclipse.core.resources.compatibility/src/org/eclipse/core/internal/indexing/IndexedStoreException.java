/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.indexing;

import org.eclipse.core.internal.resources.CompatibilityMessages;


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
	 * Initializes the messages at class load time.
	 */
	private static void initializeMessages() {
		messages[GenericError] = CompatibilityMessages.indexedStore_genericError;
		messages[EntryKeyLengthError] = CompatibilityMessages.indexedStore_entryKeyLengthError;
		messages[EntryNotRemoved] = CompatibilityMessages.indexedStore_entryNotRemoved;
		messages[EntryValueLengthError] = CompatibilityMessages.indexedStore_entryValueLengthError;
		messages[EntryValueNotUpdated] = CompatibilityMessages.indexedStore_entryValueNotUpdated;
		messages[IndexNodeNotRetrieved] = CompatibilityMessages.indexedStore_indexNodeNotRetrieved;
		messages[IndexNodeNotStored] = CompatibilityMessages.indexedStore_indexNodeNotStored;
		messages[IndexNodeNotSplit] = CompatibilityMessages.indexedStore_indexNodeNotSplit;
		messages[IndexNodeNotCreated] = CompatibilityMessages.indexedStore_indexNodeNotCreated;
		messages[IndexExists] = CompatibilityMessages.indexedStore_indexExists;
		messages[IndexNotCreated] = CompatibilityMessages.indexedStore_indexNotCreated;
		messages[IndexNotFound] = CompatibilityMessages.indexedStore_indexNotFound;
		messages[IndexNotRemoved] = CompatibilityMessages.indexedStore_indexNotRemoved;
		messages[ObjectExists] = CompatibilityMessages.indexedStore_objectExists;
		messages[ObjectNotAcquired] = CompatibilityMessages.indexedStore_objectNotAcquired;
		messages[ObjectNotCreated] = CompatibilityMessages.indexedStore_objectNotCreated;
		messages[ObjectNotFound] = CompatibilityMessages.indexedStore_objectNotFound;
		messages[ObjectNotReleased] = CompatibilityMessages.indexedStore_objectNotReleased;
		messages[ObjectNotRemoved] = CompatibilityMessages.indexedStore_objectNotRemoved;
		messages[ObjectNotUpdated] = CompatibilityMessages.indexedStore_objectNotUpdated;
		messages[ObjectNotStored] = CompatibilityMessages.indexedStore_objectNotStored;
		messages[ObjectTypeError] = CompatibilityMessages.indexedStore_objectTypeError;
		messages[StoreEmpty] = CompatibilityMessages.indexedStore_storeEmpty;
		messages[StoreFormatError] = CompatibilityMessages.indexedStore_storeFormatError;
		messages[StoreNotCreated] = CompatibilityMessages.indexedStore_storeNotCreated;
		messages[StoreNotOpen] = CompatibilityMessages.indexedStore_storeNotOpen;
		messages[StoreNotClosed] = CompatibilityMessages.indexedStore_storeNotClosed;
		messages[StoreNotFlushed] = CompatibilityMessages.indexedStore_storeNotFlushed;
		messages[StoreNotOpened] = CompatibilityMessages.indexedStore_storeNotOpened;
		messages[StoreNotReadWrite] = CompatibilityMessages.indexedStore_storeNotReadWrite;
		messages[ContextNotAvailable] = CompatibilityMessages.indexedStore_contextNotAvailable;
		messages[ObjectIDInvalid] = CompatibilityMessages.indexedStore_objectIDInvalid;
		messages[MetadataRequestError] = CompatibilityMessages.indexedStore_metadataRequestError;
		messages[EntryRemoved] = CompatibilityMessages.indexedStore_entryRemoved;
		messages[StoreNotConverted] = CompatibilityMessages.indexedStore_storeNotConverted;
		messages[StoreIsOpen] = CompatibilityMessages.indexedStore_storeIsOpen;
		messages[StoreNotCommitted] = CompatibilityMessages.indexedStore_storeNotCommitted;
		messages[StoreNotRolledBack] = CompatibilityMessages.indexedStore_storeNotRolledBack;
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
