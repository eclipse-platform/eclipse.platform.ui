package org.eclipse.help.internal.contributors.xml1_0;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */



import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.contributors1_0.*;
import org.eclipse.help.internal.context.*;


/**
 * Contributor factory: 
 */
public class ContributorFactory {
	protected static final ContributorFactory instance = new ContributorFactory();


	protected Hashtable creators; // replace with HashMap later
	/**
	 * ContributorFactory constructor.
	 */
	protected ContributorFactory() {
		super();


		creators = new Hashtable();
		creators.put(TopicContributor.TOPICS_ELEM, new XMLTopicContributorCreator());
		creators.put(ViewContributor.INFOSET_ELEM, new XMLViewContributorCreator());
		creators.put(ActionContributor.ACTIONS_ELEM, new XMLActionContributorCreator());
		creators.put(
			IContextContributor.CONTEXTS_ELEM,
			new XMLContextContributorCreator());
	}
	/**
	 * @param id java.lang.String
	 * @param creator com.ibm.itp.ua.contribution.ContributorCreator
	 */
	public void addContributorCreator(String id, ContributorCreator creator) {
		creators.put(id, creator);
	}
	/**
	 * @return com.ibm.itp.ua.contribution.Contributor
	 * @param type java.lang.String
	 */
	public Contributor createContributor(
		IPluginDescriptor plugin,
		IConfigurationElement contribution) {
		ContributorCreator creator =
			(ContributorCreator) creators.get(contribution.getName());


		if ((creator == null) || (plugin == null) || (contribution == null)) {
			// unknown contribution type, or invalid plugin or contribution
			// returning null is properly handled by calling class.
			return null;
		} else {
			return creator.create(plugin, contribution);
		}
	}
	/**
	 * @return com.ibm.itp.help.contribution.ContributorFactory
	 */
	public static ContributorFactory getFactory() {
		return instance;
	}
}
