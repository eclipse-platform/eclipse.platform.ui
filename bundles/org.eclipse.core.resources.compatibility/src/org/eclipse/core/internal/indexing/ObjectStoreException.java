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

public class ObjectStoreException extends StoreException {

	public final static int GenericFailure = 0;
	public final static int InternalFailure = 1;
	public final static int StoreCreateFailure = 10;
	public final static int StoreConversionFailure = 11;
	public final static int StoreOpenFailure = 12;
	public final static int StoreCloseFailure = 13;
	public final static int PageReadFailure = 20;
	public final static int PageWriteFailure = 21;
	public final static int PageVacancyFailure = 22;
	public final static int ObjectTypeFailure = 23;
	public final static int ObjectSizeFailure = 24;
	public final static int ObjectExistenceFailure = 25;
	public final static int ObjectHeaderFailure = 26;
	public final static int ObjectInsertFailure = 27;
	public final static int ObjectRemoveFailure = 28;
	public final static int ObjectUpdateFailure = 29;
	public final static int ObjectIsLocked = 30;
	public final static int MetadataRequestFailure = 40;

	public final static String[] message = new String[50];

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	static {
		initializeMessages();
	}

	public int id = 0; // exception id

	public ObjectStoreException(int id) {
		this(id, null);
	}

	public ObjectStoreException(int id, Throwable exception) {
		super(message[id], exception);
		this.id = id;
	}

	/**
	 * Initializes the messages at class load time.
	 */
	private static void initializeMessages() {
		message[GenericFailure] = CompatibilityMessages.objectStore_genericFailure;
		message[InternalFailure] = CompatibilityMessages.objectStore_internalFailure;
		message[StoreCreateFailure] = CompatibilityMessages.objectStore_storeCreateFailure;
		message[StoreConversionFailure] = CompatibilityMessages.objectStore_storeConversionFailure;
		message[StoreOpenFailure] = CompatibilityMessages.objectStore_storeOpenFailure;
		message[StoreCloseFailure] = CompatibilityMessages.objectStore_storeCloseFailure;
		message[PageReadFailure] = CompatibilityMessages.objectStore_pageReadFailure;
		message[PageWriteFailure] = CompatibilityMessages.objectStore_pageWriteFailure;
		message[PageVacancyFailure] = CompatibilityMessages.objectStore_pageVacancyFailure;
		message[ObjectTypeFailure] = CompatibilityMessages.objectStore_objectTypeFailure;
		message[ObjectSizeFailure] = CompatibilityMessages.objectStore_objectSizeFailure;
		message[ObjectExistenceFailure] = CompatibilityMessages.objectStore_objectExistenceFailure;
		message[ObjectHeaderFailure] = CompatibilityMessages.objectStore_objectHeaderFailure;
		message[ObjectInsertFailure] = CompatibilityMessages.objectStore_objectInsertFailure;
		message[ObjectRemoveFailure] = CompatibilityMessages.objectStore_objectRemoveFailure;
		message[ObjectUpdateFailure] = CompatibilityMessages.objectStore_objectUpdateFailure;
		message[ObjectIsLocked] = CompatibilityMessages.objectStore_objectIsLocked;
		message[MetadataRequestFailure] = CompatibilityMessages.objectStore_metadataRequestFailure;
	}

}
