package org.eclipse.help.internal.contributions.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.xml.sax.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;

/**
 * Default implementation for an action contribution
 */
public class HelpAction extends HelpContribution implements Action {
	protected String view;
	protected boolean isStandalone = false;
	public HelpAction(Attributes attrs) {
		super(attrs);
		if (attrs != null) {
			view = attrs.getValue(ViewContributor.VIEW_ELEM);
			isStandalone =
				Boolean
					.valueOf(attrs.getValue(ActionContributor.ACTIONS_STANDALONE_ATTR))
					.booleanValue();
		}
	}
	public String getView() {
		return view;
	}
	public boolean isStandalone() {
		return isStandalone;
	}
}
