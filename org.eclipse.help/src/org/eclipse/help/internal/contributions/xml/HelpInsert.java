package org.eclipse.help.internal.contributions.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	protected int    mode = Contribution.NORMAL;
	/**
	 * Constructor
	 */
	public HelpInsert(Attributes attrs) {
		super(attrs);
		if (attrs != null) {
			fromID = attrs.getValue(ActionContributor.INSERT_FROM_ATTR);
			toID = attrs.getValue(ActionContributor.INSERT_TO_ATTR);
			
			// override the parent behavior for label
			// Note: we may need to change the parent to work with null labels
			label = attrs.getValue("label");
			
			String as = attrs.getValue(ActionContributor.INSERT_AS_ATTR);
			if (as == null || as.equals(""))
				mode = Contribution.NORMAL;
			else if (as.equals(ActionContributor.INSERT_AS_FIRST_CHILD)) 
				mode = Contribution.FIRST;
			else if (as.equals(ActionContributor.INSERT_AS_LAST_CHILD)) 
				mode = Contribution.LAST;
			else if (as.equals(ActionContributor.INSERT_AS_PREV_SIB)) 
				mode = Contribution.PREV;
			else if (as.equals(ActionContributor.INSERT_AS_NEXT_SIB)) 
				mode = Contribution.NEXT;
			else
				mode = Contribution.NORMAL;			
		}
	}
	public int getMode() {
		return mode;
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
