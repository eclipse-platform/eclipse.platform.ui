package org.eclipse.help.internal.contributions.xml1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.contributions1_0.*;
import org.eclipse.help.internal.util.Resources;
import org.xml.sax.Attributes;
import java.net.URLEncoder;
import org.eclipse.help.internal.util.TString;
import org.eclipse.help.*;

/**
 * View contribution implementation.
 */
public class HelpInfoView extends HelpContribution implements InfoView {
	private final static String defaultSplash =
		"/org.eclipse.help/" + Resources.getString("splash_location");
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
	/**
	 *  1.0 nav support
	 */
	public String getHref() {
		return
			defaultSplash
				+ "?title="
				+ URLEncoder.encode(TString.getUnicodeNumbers(getLabel()));
	}
	/**
	 *  1.0 nav support
	 */
	public String getTopicsID() {
		return this.getParent().getID() + ".." + getID();
	}
	/**
	 *  1.0 nav support
	 */
	public String getLabel() {
		// if infoset has one view, use infoset label
		if (getParent().getChildrenList().size() == 1)
			return getParent().getLabel();
		// if only one infoset, use infoview label
		else if (HelpSystem.getNavigationManager().getInfoSetIds().size() == 1)
			return super.getLabel();
		// otherwise join infoset label and infoview label
		else return Resources.getString(
			"infosetLabel-infoviewLabel",
			getParent().getLabel(),
			super.getLabel());
	}

/* 1.0 nav support */	
	public ITopic getTopic(String href)
	{
		Topic[] topics =
			(Topic[]) HelpSystem
				.getNavigationManager()
				.getNavigationModel(getParent().getID())
				.getTopicsWithURL(href);
				
		if (topics == null || topics.length == 0)
			return null;
		else
			return topics[0];
	}
/* eo 1.0 nav support */
}