package org.eclipse.help.internal.navigation1_0;


/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */




import java.io.File;
import java.util.*;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.contributions1_0.*;
import org.eclipse.help.internal.contributors1_0.ContributionManager;
import org.eclipse.help.internal.util.*;
/**
 * Manages the navigation model. It generates it and it reads it back
 * and instantiates the model for future rendering.
 * There is a model (notifier) for each <views> node.
 */
public class HelpNavigationManager {
	public static final String NAV_XML_FILENAME = "_nav.xml";


	private NavigationModel currentModel;
	private Map navigationModels = new HashMap(/* of NavigationModel */);
	
	// Map that keeps track of all the infosets available
	private InfosetsMap infosetsMap = null;


	/**
	 * HelpNavigationManager constructor.
	 */
	public HelpNavigationManager() {
		super();


		try {	
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
						.append("nl")
						.addTrailingSeparator()
						.append(Locale.getDefault().toString())
						.addTrailingSeparator()
						.append(infoset.getID()).toFile();
				if (!navOutDir.exists()) {
					navOutDir.mkdirs();
				}
				File navOutFile = new File(navOutDir, NAV_XML_FILENAME);
				new XMLNavGenerator(infoset, navOutFile).generate();


				infosetsMap.put(infoset.getID(), infoset.getLabel());
			}
			// Save a file with all the infosets ids and labels
			infosetsMap.save();
		} catch (Exception e) {
			Logger.logError("", e);
		}
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
		for (Enumeration infosets=infosetsMap.keys();infosets.hasMoreElements(); )
		{
			String infoset = (String)infosets.nextElement();
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
	 *  1.0 nav support
	 */
	public Collection getTopicsIDs() {
		Collection IDs=getInfoSetIds();
		List hrefs = new ArrayList(2*IDs.size());
		for (Iterator isIt=IDs.iterator();isIt.hasNext();) {
			String isID=(String)isIt.next();
			InfoSet is=getInfoSet(isID);
			InfoView views[]=is.getViews();
			for(int i=0; i<views.length;i++){
				String tsID = is.getID()+".."+views[i].getID();
				hrefs.add(tsID);
				
			}
		}
		return hrefs;
	}
	/**
	 *  1.0 nav support
	 */
	public String getTopicsLabel(String href){
		int index = href.indexOf("..");
		if(index<0)
			return "";
		InfoSet is=getInfoSet(href.substring(0, index));
		InfoView iv=is.getView(href.substring(index+"..".length()));
		return iv.getLabel();
	}

	/**
	 *  1.0 nav support
	 */
	public ITopic getTopics(String href) {
		if(href==null)
			return null;
		int index = href.indexOf("..");
		if(index<0 || href.length()<=index+"..".length())
			return null;
		InfoSet is=getInfoSet(href.substring(0, index));
		return is.getView(href.substring(index+"..".length()));
	}

}
