package org.eclipse.help.internal.navigation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.util.*;
import java.net.*;
import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributors.xml.*;
import org.eclipse.help.internal.util.*;

/**
 * Manages the navigation model. It generates it and it reas it back
 * and instantiates the model for future rendering.
 * There is a model (notifier) for each <views> node.
 */
public class HelpNavigationManager {
	public static final String NAV_XML_FILENAME = "_nav.xml";

	private NavigationModel currentModel;
	private Map navigationModels = new HashMap(/* of NavigationModel */);
	
	// Map that keeps track of all the infosets available
	private InfosetsMap infosetsMap = null;

	private String currentInfosetId = null;

	// suggested ordered list of infosets
	private String baseInfosets = null;
	/**
	 * HelpNavigationManager constructor.
	 */
	public HelpNavigationManager() {
		super();

		try {	
			// read the product.ini file with base infoset ordering
			readProductInfo();
				
			ContributionManager cmgr = HelpSystem.getContributionManager();
			if (cmgr.hasNewContributions()) {
				// build all the info sets: build the structure and generate the xml's
				// Note: it is cheaper to do all the info sets now, since we've taken the hit to
				//       to some extra processing in parsing actions, etc.
				//       Also, in most cases there is only one info set.
				
				// suggest memory cleanup, as we're going to use a bit of it
				System.gc();

				createNavigationModels(); // it will initialize infosetsIds
				cmgr.versionContributions();

				// attemp to cleanup all the memory no longer needed
				//System.gc();
			}else{
				// initialize infosetsIds
				getInfoSetIds();
			}
		} catch (Exception e) {
			Logger.logError("", e);
		}
	}
	private void createNavigationModels() {
		try {
			// Keep track of all the infosets available
			infosetsMap = new InfosetsMap();
			InfosetBuilder builder = new InfosetBuilder(HelpSystem.getContributionManager());
			// merges all topics into views 
			Map infosets = builder.buildInformationSets();
			for (Iterator it = infosets.values().iterator(); it.hasNext();) {
				InfoSet infoset = (InfoSet) it.next();
				NavigationModel m = new NavigationModel(infoset);
				navigationModels.put(infoset.getID(), m);

				// generate navigation file for each infoset
				File navOutDir = 					
					HelpPlugin
						.getDefault()
						.getStateLocation()
						.addTrailingSeparator()
						.append(infoset.getID()).toFile();
				if (!navOutDir.exists()) {
					navOutDir.mkdirs();
				}
				File navOutFile = new File(navOutDir, NAV_XML_FILENAME);
				new XMLNavGenerator(infoset, navOutFile).generate();

				infosetsMap.put(infoset.getID(), infoset.getRawLabel());
			}
			// Save a file with all the infosets ids and labels
			infosetsMap.save();
		} catch (Exception e) {
			Logger.logError("", e);
		}
	}
	/**
	 * Returns the current navigation model
	 */
	public InfoSet getCurrentInfoSet() {
		NavigationModel navModel = getCurrentNavigationModel();
		if (navModel == null)
			return null;
		else
			return (InfoSet) navModel.getRootElement();
	}
	/**
	 * Returns the current navigation model
	 */
	public NavigationModel getCurrentNavigationModel() {
		if (currentModel == null) {
			// no previous InfoSet loaded, use default InfoSet
			if (infosetsMap.size() > 0) {
				InfoSet defaultInfoset = getDefaultInfoSet();
				// this also sets the current NavigationModel
				setCurrentInfoSet(defaultInfoset.getID());
			}
		}
		return currentModel;
	}
	
	/**
	 * Returns the default infoset. 
	 */
	public InfoSet getDefaultInfoSet() {
		NavigationModel navModel = getDefaultNavigationModel();
		if (navModel == null)
			return null;
		else
			return (InfoSet) navModel.getRootElement();
	}
	
	/**
	 * Returns the default navigation model.
	 */
	public NavigationModel getDefaultNavigationModel() {
		// currently this implementation returns the first infoset as 
		// defined by product.ini. This may be revisited if a 
		// "default infoset" attribute is added to product.ini
		Collection orderedInfosets = getInfoSetIds();
		
		// we are guaranteed proper order here because Collection is ArrayList.
		Iterator anIterator = orderedInfosets.iterator();
		if ((anIterator != null) && (anIterator.hasNext()) ) {
			String infosetId = (String)(anIterator.next());
			return getNavigationModel(infosetId);
		}
		return null;
	}
	
	
	
	
	/**
	 * Returns the navigation model for an infoset
	 */
	public InfoSet getInfoSet(String id) {
		NavigationModel navModel = getNavigationModel(id);
		if (navModel == null)
			return null;
		else
			return (InfoSet) navModel.getRootElement();
	}
	/**
	 * @return Collection of infosetIds available, not including
	 * ones that do not have navigation
	 * (i.e. standalone included elsewhere)
	 */
	public Collection getInfoSetIds() {
		if(infosetsMap == null)
		{
			infosetsMap = new InfosetsMap();
			infosetsMap.restore();
		}

		ArrayList orderedInfosets = new ArrayList(infosetsMap.size());
		// first add the infosets from the product ini
		if (baseInfosets != null)
		{
			StringTokenizer suggestdOrderedInfosets = new StringTokenizer(baseInfosets, " ;,");
			while(suggestdOrderedInfosets.hasMoreElements())
			{
				String infoset = (String)suggestdOrderedInfosets.nextElement();
				if (infosetsMap.containsKey(infoset))
					orderedInfosets.add(infoset);
			}
		}
		// add the remaining infosets
		for (Enumeration infosets=infosetsMap.keys();infosets.hasMoreElements(); )
		{
			String infoset = (String)infosets.nextElement();
			if (!orderedInfosets.contains(infoset))
				orderedInfosets.add(infoset);
		}
		return orderedInfosets;
	}
	
	/**
	 * Returns the label for an infoset.
	 * This method uses the label from the infoset map file
	 * so that the navigation file does not need to be
	 * read in memory
	 */
	public String getInfoSetLabel(String infosetId) {
		String label = (String)infosetsMap.get(infosetId);
		if (label.indexOf('%') == 0) {
			int lastPeriod = infosetId.lastIndexOf('.');
			String pluginID = infosetId.substring(0, lastPeriod);
			label =	DocResources.getPluginString(pluginID, label.substring(1));
		}
		return label;
	}
	
	/**
	 * Returns the navigation model for an infoset
	 */
	public NavigationModel getNavigationModel(String id) {
		if (id == null || id.equals(""))
			return null;

		// First check the cache
		NavigationModel m = (NavigationModel) navigationModels.get(id);
		if (m == null && infosetsMap.containsKey(id)) {
			m = new NavigationModel(id);
			navigationModels.put(id, m);
		}
		return m;
	}
	/**
	 * Reads the product info and extracts the ordering of the infosets
	 */
	private void readProductInfo() {
		try
		{
			Properties productInfo = new Properties();
			URL configURL= null;
			IInstallInfo ii= BootLoader.getInstallationInfo();
			String configName= ii.getApplicationConfigurationIdentifier();
			if (configName != null) {
				configURL = ii.getConfigurationInstallURLFor(configName);
			} else {
				Logger.logWarning(Resources.getString("W001"));
				return;
			}
			URL iniURL= null;
			try {
				iniURL = new URL(configURL, "product.ini");
			} catch (MalformedURLException e) {
				Logger.logWarning(Resources.getString("W001"));
			}
	
			InputStream is = iniURL.openStream();
			if (is == null) return;
			productInfo.load(is);
			is.close();

			baseInfosets = productInfo.getProperty("baseInfosets");
		}
		catch(Throwable e)
		{}
	}
	/**
	 * Sets the current model
	 */
	public void setCurrentInfoSet(String infosetId) {
		if (infosetsMap.containsKey(infosetId)) {
			// Set global infoset and navigation model
			currentInfosetId = infosetId;
			// Set the current navigation model.
			// Side effect: the model may be loaded at this time.
			currentModel = getNavigationModel(infosetId);
		}
	}
	/**
	 * Sets the current model
	 */
	public void setCurrentNavigationModel(NavigationModel m) {
		currentModel = m;
	}
}
