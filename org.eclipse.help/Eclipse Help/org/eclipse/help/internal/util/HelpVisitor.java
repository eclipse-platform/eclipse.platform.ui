package org.eclipse.help.internal.util;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;
import org.eclipse.help.internal.contributions.*;

/**
 * Default help contributions visitor
 */
public class HelpVisitor implements Visitor {
	/**
	 * HelpVisitor constructor comment.
	 */
	public HelpVisitor() {
		super();
	}
	/**
	 * @param viewSet com.ibm.itp.ua.view.ViewSet
	 */
	public void visit(InfoSet infoSet) {
		visitChildren(infoSet);
	}
	/**
	 * visit method comment.
	 */
	public void visit(InfoView view) {
		visitChildren(view);
	}
	/**
	 * visit method comment.
	 */
	public void visit(Topic topic) {
		visitChildren(topic);
	}
	public void visitChildren(Contribution con) {
		for (Iterator e = con.getChildren(); e.hasNext();) {
			Contribution c = (Contribution) e.next();
			c.accept(this);
		}
	}
}
