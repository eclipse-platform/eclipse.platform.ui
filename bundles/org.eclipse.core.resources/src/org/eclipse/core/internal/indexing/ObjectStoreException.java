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

import org.eclipse.core.internal.utils.Policy;

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

	public ObjectStoreException(String s) {
		this(s, null);
	}

	public ObjectStoreException(String s, Throwable exception) {
		super(s, exception);
		this.id = GenericFailure;
	}

	/**
	 * Initializes the messages at class load time.
	 */
	private static void initializeMessages() {
		message[GenericFailure] = bind("objectStore.genericFailure"); //$NON-NLS-1$
		message[InternalFailure] = bind("objectStore.internalFailure"); //$NON-NLS-1$
		message[StoreCreateFailure] = bind("objectStore.storeCreateFailure"); //$NON-NLS-1$
		message[StoreConversionFailure] = bind("objectStore.storeConversionFailure"); //$NON-NLS-1$
		message[StoreOpenFailure] = bind("objectStore.storeOpenFailure"); //$NON-NLS-1$
		message[StoreCloseFailure] = bind("objectStore.storeCloseFailure"); //$NON-NLS-1$
		message[PageReadFailure] = bind("objectStore.pageReadFailure"); //$NON-NLS-1$
		message[PageWriteFailure] = bind("objectStore.pageWriteFailure"); //$NON-NLS-1$
		message[PageVacancyFailure] = bind("objectStore.pageVacancyFailure"); //$NON-NLS-1$
		message[ObjectTypeFailure] = bind("objectStore.objectTypeFailure"); //$NON-NLS-1$
		message[ObjectSizeFailure] = bind("objectStore.objectSizeFailure"); //$NON-NLS-1$
		message[ObjectExistenceFailure] = bind("objectStore.objectExistenceFailure"); //$NON-NLS-1$
		message[ObjectHeaderFailure] = bind("objectStore.objectHeaderFailure"); //$NON-NLS-1$
		message[ObjectInsertFailure] = bind("objectStore.objectInsertFailure"); //$NON-NLS-1$
		message[ObjectRemoveFailure] = bind("objectStore.objectRemoveFailure"); //$NON-NLS-1$
		message[ObjectUpdateFailure] = bind("objectStore.objectUpdateFailure"); //$NON-NLS-1$
		message[ObjectIsLocked] = bind("objectStore.objectIsLocked"); //$NON-NLS-1$
		message[MetadataRequestFailure] = bind("objectStore.metadataRequestFailure"); //$NON-NLS-1$
	}

	private static String bind(String name) {
		return Policy.bind(name);
	}

}