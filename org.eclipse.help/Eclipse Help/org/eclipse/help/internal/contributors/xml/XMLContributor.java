package org.eclipse.help.internal.contributors.xml;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import java.io.*;
import java.net.*;
import org.xml.sax.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.server.PluginURL;
import org.eclipse.help.internal.contributions.xml.HelpContribution;

/**
 * Basic contributor
 */
public abstract class XMLContributor implements Contributor {
	protected IPluginDescriptor plugin = null;
	protected IConfigurationElement configuration = null;
	protected Contribution contribution = null;
	// always call the getContributionParser() to use this....
	protected ContributionParser contributionParser = null;

	/**
	 * XMLContributor constructor comment.
	 */
	public XMLContributor(
		IPluginDescriptor plugin,
		IConfigurationElement configuration) {
		super();

		this.plugin = plugin;
		this.configuration = configuration;
	}
	/**
	 * @return java.lang.String
	 * @param plugin com.ibm.itp.core.api.plugins.IPluginDescriptor
	 */
	protected String createUniqueID(IPluginDescriptor plugin) {
		return plugin.getUniqueIdentifier() + "." + SequenceGenerator.next();
	}
	/**
	 * @return org.w3c.dom.Document
	 */
	public abstract Contribution getContribution();
	/**
	 * @return org.w3c.dom.Document
	 * @param contributionType java.lang.String
	 * @param idAttributeName java.lang.String
	 */
	protected Contribution getContribution(String contributionNameAttribute) {
		if (contribution == null) {
			contribution = load(contributionNameAttribute);
			if (contribution != null) {
				preprocess(contribution);
			}
		}
		return contribution;
	}
	/**
	 * Returns the contribution factory.
	 * Use the default contribution factory, but certain contributors can
	 * provide specialized subclasses.
	 */
	protected ContributionParser getContributionParser() {
		if (contributionParser == null)
			contributionParser = new ContributionParser();
		return contributionParser;
	}
	/**
	 * getPluginID method comment.
	 */
	public IPluginDescriptor getPlugin() {
		return plugin;
	}
	/**
	 * @return boolean
	 * @param shortID java.lang.String
	 */
	protected boolean isValidShortID(String shortID) {
		return shortID.indexOf(".") == -1;
	}
	/**
	 * @param elementType java.lang.String
	 * @param documentList java.util.Vector
	 */
	protected Contribution load(String nameAttribute) {
		Contribution contribution = null;
		String file = plugin.getUniqueIdentifier()+"/"+ configuration.getAttribute(nameAttribute);

		try {
			PluginURL xmlURL = new PluginURL(file, "");
			InputStream stream = xmlURL.openFileFromPlugin();
			if (stream == null)
				return null;

			InputSource source = new InputSource(stream);
			// set id info for parser exceptions.
			// use toString method to capture protocol...etc
			////////source.setSystemId(xmlURL.toString());
			source.setSystemId(file);

			ContributionParser parser = getContributionParser();
			parser.parse(source);
			stream.close();
			contribution = parser.getContribution();
		} catch (MalformedURLException ue) {
			Logger.logError("", ue);
		} catch (SAXException se) {
			Logger.logError("", se);
		} catch (IOException ioe) {
			String msg = Resources.getString("E009", file);
			Logger.logError(msg, ioe);
			// now pass it to the RuntimeHelpStatus object explicitly because we
			// still need to display errors even if Logging is turned off.
			RuntimeHelpStatus.getInstance().addParseError(msg, file);
		}

		return contribution;
	}
	/**
	 * @param doc org.w3c.dom.Document
	 */
	protected abstract void preprocess(Contribution contribution);
	/**
	 * Utility method that scans the Document for all id attributes and update them
	 * to include the plugin id (i.e. create a fully qualified
	 * id).
	 * @param doc org.w3c.dom.Document
	 */
	protected void updateIDs(Contribution contrib) {
		// set the id on the input contribution 
		String id = contrib.getID();
		if (id != null) {
			// is valid node name?
			if (isValidShortID(id)) {
				((HelpContribution) contrib).setID(plugin.getUniqueIdentifier() + "." + id);
			} else {
				// XXX ?  Set the id to nothing?
			}
		} else {
			// Create a unique id for the topic...
			 ((HelpContribution) contrib).setID(createUniqueID(plugin));
		}

		// recurse to children
		for (Iterator contribs = contrib.getChildren(); contribs.hasNext();) {
			HelpContribution c = (HelpContribution) contribs.next();
			updateIDs(c);
		}
	}
}
