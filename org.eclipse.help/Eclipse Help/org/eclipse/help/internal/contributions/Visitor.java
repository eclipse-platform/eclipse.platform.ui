package org.eclipse.help.internal.contributions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
