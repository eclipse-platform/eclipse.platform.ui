package org.eclipse.help.internal.contributions.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;
import org.xml.sax.*;

/**
 * View contribution implementation.
 */
public class HelpInfoView extends HelpContribution implements InfoView {
	/**
	 * Constructor
	 */
	public HelpInfoView(Attributes attrs) {
		super(attrs);
	}
	/**
	 */
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	/**
	* Adds a child and returns it
	*/
	public Contribution addChild(Contribution child) {
		children.add(child);
		// detach from old parent, if any
		if (((HelpContribution) child).parent != null)
			 ((HelpContribution) child).parent.children.remove(child);
		// set the new parent
		 ((HelpContribution) child).parent = this;
		return child;
	}

	public Topic getRoot() {
		return (Topic) children.get(0);
	}

	/**
	 * @return java.lang.String
	 */
	public String toString() {
		return id;
	}
}
