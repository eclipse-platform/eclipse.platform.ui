package org.eclipse.help.internal.contributions.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.xml.sax.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;

/**
 * Default implementation for a topic contribution
 */
public class HelpInsert extends HelpContribution implements Insert {
	protected String fromID;
	protected String toID;
	protected String asID;
	public HelpInsert(Attributes attrs) {
		super(attrs);
		if (attrs != null) {
			fromID = attrs.getValue(ActionContributor.INSERT_FROM_ATTR);
			toID = attrs.getValue(ActionContributor.INSERT_TO_ATTR);
			asID = attrs.getValue(ActionContributor.INSERT_AS_ATTR);
		}
	}
	public String getMode() {
		return asID;
	}
	public String getSource() {
		return fromID;
	}
	public String getTarget() {
		return toID;
	}
	public String getView() {
		Contribution p = this;
		while (p.getParent() != null)
			p = p.getParent();
		return ((Action) p).getView();
	}
}
