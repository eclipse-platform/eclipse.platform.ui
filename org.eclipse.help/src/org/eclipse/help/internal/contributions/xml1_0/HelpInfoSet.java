package org.eclipse.help.internal.contributions.xml1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.help.internal.contributions1_0.*;
import org.eclipse.help.internal.contributors1_0.*;
import org.xml.sax.*;
/* 1.0 nav support */	
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpSystem;
/* eo 1.0 nav support */

/**
 * Views contribution implementation 
 */
public class HelpInfoSet extends HelpContribution implements InfoSet {

	protected String href;
	protected boolean isStandalone;
	/**
	 * @return com.ibm.itp.contributions.ViewSet
	 * @param id java.lang.String
	 */
	public HelpInfoSet(Attributes attrs) {
		super(attrs);
		if (attrs != null) {
			href = attrs.getValue(ViewContributor.INFOSET_HREF_ATTR);
			isStandalone =
				Boolean
					.valueOf(attrs.getValue(ActionContributor.ACTIONS_STANDALONE_ATTR))
					.booleanValue();
		}

	}
	/**
	 * Implements the method for the Visitor pattern
	 * @param visitor com.ibm.itp.contributions.Visitor
	 */
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	/**
	 * @return java.lang.String
	 */
	public String getHref() {
		return href;
	}
	/**
	 * getView method comment.
	 */
	public InfoView getView(String name) {
		for (Iterator it = getChildren(); it.hasNext();) {
			InfoView view = (InfoView) it.next();
			if (view.getID().equals(name))
				return view;
		}
		return null;
	}
	/**
	 * getViewNames method comment.
	 */
	public String[] getViewNames() {
		InfoView[] views = getViews();
		String[] names = new String[views.length];
		for (int i = 0; i < names.length; i++)
			names[i] = views[i].getID();
		return names;
	}
	/**
	 * getViews method comment.
	 */
	public InfoView[] getViews() {
		InfoView[] views = new InfoView[children.size()];
		Iterator it = getChildren();
		for (int i = 0; it.hasNext(); i++) {
			views[i] = (InfoView) it.next();
		}
		return views;
	}
	public boolean isStandalone() {
		return isStandalone;
	}
	public void setHref(String href) {
		this.href = href;
	}
	/**
	 * @return java.lang.String
	 */
	public String toString() {
		return getID();
	}
	
/* 1.0 nav support */	
	public ITopic getTopic(String href)
	{
		Topic[] topics =
			(Topic[]) HelpSystem
				.getNavigationManager()
				.getNavigationModel(getID())
				.getTopicsWithURL(href);
				
		if (topics == null || topics.length == 0)
			return null;
		else
			return topics[0];
	}
/* eo 1.0 nav support */
}
