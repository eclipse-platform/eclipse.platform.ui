package org.eclipse.core.internal.watson;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
