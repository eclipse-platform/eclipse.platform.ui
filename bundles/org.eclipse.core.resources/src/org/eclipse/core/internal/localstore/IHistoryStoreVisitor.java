package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.indexing.IndexedStoreException;

public interface IHistoryStoreVisitor {
/**
 * Performs required behaviour whenever a match is found in the history store query.
 * 
 * @param state State to be visited in IndexedStore.
 */
public boolean visit(HistoryStoreEntry state) throws IndexedStoreException;
}
