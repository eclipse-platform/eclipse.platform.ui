package org.eclipse.help.internal.contributions.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;
import org.xml.sax.*;
import org.eclipse.help.internal.contributions.Contribution;
import org.eclipse.help.internal.contributions.Visitor;
import org.eclipse.help.internal.util.*;

/**
 * Common class for contribution data.
 */
public class HelpContribution implements Contribution {

	protected HelpContribution parent;
	protected List children = new ArrayList(/* of HelpContribution */);

	protected String id;

	// counter to track children with FIRST or LAST position preferences
	protected int firstChildrenNo = 0;
	protected String label;
	protected int lastChildrenNo = 0;
	protected String translatedLabel;

	/**
	 */
	public HelpContribution(Attributes attrs) {
		if (attrs == null)
			return;

		// set the label
		this.label = attrs.getValue("label");
		if (this.label == null)
			this.label = attrs.getValue("id");
		if (this.label == null)
			this.label = getClass().toString();

		// set the id
		id = attrs.getValue("id");
	}
	/**
	 * Implements the method for the Visitor pattern
	 * @param visitor com.ibm.itp.contributions.Visitor
	 */
	public void accept(Visitor visitor) {
		// this should execute
		System.out.println(Resources.getString("contribution_accept"));
	}
	/**
	 * Adds a child and returns it
	 * @returns com.ibm.itp.contributions.HelpContribution
	 * @param parentNode com.ibm.itp.contributions.HelpContribution
	 */
	public Contribution addChild(Contribution child) {
		return insertChild(child, Contribution.NORMAL);
	}
	/**
	 */
	public Iterator getChildren() {
		return children.iterator();
	}
	/**
	 */
	public List getChildrenList() {
		return children;
	}
	/**
	 */
	public String getID() {
		return id;
	}
	/**
	 * Returns the translated label
	 */
	public String getLabel() {
		if (translatedLabel == null) {
			translatedLabel = label;
			if (translatedLabel.indexOf('%') == 0) {
				int lastPeriod = id.lastIndexOf('.');
				String pluginID = id.substring(0, lastPeriod);
				translatedLabel =
					DocResources.getPluginString(pluginID, translatedLabel.substring(1));
			}
		}
		return translatedLabel;
	}
	/**
	 */
	public Contribution getParent() {
		return parent;
	}
	/**
	 * Returns the label without translation, as it appears in the xml files
	 */
	public String getRawLabel() {
		return label;
	}
	/**
	 * Adds a child and returns it
	 * @returns com.ibm.itp.contributions.HelpContribution
	 * @param parentNode com.ibm.itp.contributions.HelpContribution
	 */
	public Contribution insertChild(Contribution child, int positionPreference) {
		if (positionPreference == FIRST)
			children.add(firstChildrenNo++, child);
		else
			if (positionPreference == LAST)
				children.add(children.size() - lastChildrenNo++, child);
			else // NO PREFERENCE
				children.add(children.size() - lastChildrenNo, child);
		// detach from old parent, if any
		//if (((HelpContribution) child).parent != null)
		// ((HelpContribution) child).parent.children.removeElement(child);
		// set the new parent
		 ((HelpContribution) child).parent = this;
		return child;
	}
	/**
	 * Adds a child and returns it
	 * @returns com.ibm.itp.contributions.HelpContribution
	 * @param parentNode com.ibm.itp.contributions.HelpContribution
	 */
	public Contribution insertNeighbouringChild(
		Contribution child,
		Contribution newchild,
		int positionPreference) {
		// find position of existing child
		int pos = -1;
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) == child) {
				pos = i;
				break;
			}
		}
		if (pos == -1) {
			//child not found
			return null;
		}
		if (positionPreference == PREV)
			children.add(pos, newchild);
		else
			children.add(pos + 1, newchild);
		// detach from old parent, if any
		//if (((HelpContribution) child).parent != null)
		// ((HelpContribution) child).parent.children.removeElement(child);
		// set the new parent
		 ((HelpContribution) newchild).parent = this;
		return child;
	}
	/**
	 */
	public void setID(String id) {
		this.id = id;
	}
	/**
	 * Sets the label without translation, as it would appear if this was created from xml navigation file
	 * @param label - raw label, which needs to appear in the property fille, or untranslatable label
	 */
	public void setRawLabel(String rawLabel) {
		this.label = rawLabel;
	}
	/**
	 */
	public String toString() {
		return getID() + " " + getClass();
	}
}
