package org.eclipse.core.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.dtree.IComparator;
import org.eclipse.core.internal.dtree.NodeComparison;
/**
 * This interface allows clients of the element tree to specify
 * how element infos are compared, and thus how element tree deltas
 * are created.
 */
public interface IElementComparator extends IComparator {
	/**
	 * The kinds of changes
	 */
	public int K_NO_CHANGE = 0;
}
