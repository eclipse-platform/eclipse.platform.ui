package org.eclipse.core.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A DeltaFilter is used to allow the user of an ElementTreeDelta
 * to navigate through the delta in a selective manner.
 */
public interface IDeltaFilter {
/**
 * Returns true if the delta element with the given flag should be
 * included in the response to an ElementTreeDelta query, and false 
 * otherwise.  The flag is the integer set by the IElementComparator.
 *
 * @see IComparator.compare(Object, Object)
 */
public boolean includeElement(int flag);
}
