package org.eclipse.help.internal.contributions1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.*;

/**
 * Visitor to walk a contribution hierarchy
 */
public interface Visitor {
	/**
	 */
	void visit(InfoSet infoSet);
	/**
	 */
	void visit(InfoView view);
	/**
	 */
	void visit(Topic topic);
}
