package org.eclipse.help.internal.navigation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributions.xml.*;
import org.eclipse.help.internal.util.*;

/**
 * Builder for the contribution hierarchy using the insert contributions.
 */
public class InfosetBuilder {
	// Contributions data, before indexing by id
	private Iterator infosets;
	private Iterator topics;
	private Iterator actions;

	// Contributions indexed by id for fast lookup
	
	// <topic> by id
	private Map topicNodeMap = new HashMap();
	// <topics> by id
	private Map topicSetNodeMap = new HashMap();
	// <infoset> by id
	private Map infoSetMap = new HashMap();
	// <infoview> by id
	private Map viewNodeMap = new HashMap();
	// <actions> by infoview
	private Map actionNodeMap = new HashMap();
	// <actions standalone=true> by infoview
	private Map standaloneActionNodeMap = new HashMap();
	
	// plugins that contributed integrated topics, by plugin id
	private Set integratedPlugins = new HashSet(/* of String */);
	
	// current view being built
	private HelpInfoView view = null;
	// collection of topics that were insert sources in current view
	private Map sources = new HashMap();
	// collection of topics that were insert targets in current view
	private Map targets = new HashMap();

	/*******************************************************/
	/*****  Inserter ***********************************/
	/*******************************************************/
	// Action command to execute the insert scripts
	class Inserter
	{
		private String from;
		private String to;
		private String label;
		private int    mode;

		public Inserter(String from, String to, String label, int mode) {
			this.from = from;
			this.to = to;
			this.label = label;
			this.mode = mode;
		}
		
		public Inserter(Insert insertNode) {
			this.from = insertNode.getSource();
			this.to = insertNode.getTarget();
			this.label = insertNode.getRawLabel();
			this.mode = insertNode.getMode();
		}

		public boolean hasValidData()
		{
			return (isValidTopic(from) || isValidTopicSet(from)) &&
				   (isValidView(to) || isValidTopic(to)); 
		}
		
		/**
		 * Inserts the topic and creates the HelpTopic objects
		 */
		public boolean insert() {
			if ((mode == Contribution.PREV) || (mode == Contribution.NEXT))
				return insertAsSibling();
			else
				return insertAsChild();
		}
		
		/**
	     * Inserts the topic and creates the HelpTopic objects
	     */
		private boolean insertAsChild() {
			// do a simple insert
			if (isValidTopic(from)) {
				Contribution parent = getTargetTopic(to,true);
				Contribution child = getSourceTopic(from);
				if (child == null)
					return false;//topic already inserted
					
				if(label != null && (child instanceof HelpTopicRef))
					((HelpTopicRef) child).setRawLabel(label);
			
				((HelpContribution) parent).insertChild(child, mode);

				// keep track of this insertion for handling standalone actions
				trackTopic(from);
			} else	if (isValidTopicSet(from)) {
				// insert all the child topics nodes
				Contribution topicSet = (Contribution) topicSetNodeMap.get(from);
				List topics = topicSet.getChildrenList();
				if (mode == Contribution.LAST) {
					for (int i = topics.size() - 1; i >= 0; i--) {
						Contribution topic = (Contribution) topics.get(i);
						Inserter newInserter = new Inserter(topic.getID(), to, null, mode);
						newInserter.insert();
					}
				}else{
					for (int i = 0; i < topics.size(); i++) {
						Contribution topic = (Contribution) topics.get(i);
						Inserter newInserter = new Inserter(topic.getID(), to, null, mode);
						newInserter.insert();
					}
				}
			}
			return true; //success
		}
		
		/**
	     * Inserts the topic and creates the HelpTopic objects
	     */
		private boolean insertAsSibling() 
		{
			// do a simple insert
			if (isValidTopic(from)) {
				Contribution refSib = getTargetTopic(to, false);
				if (refSib == null)
					return false; //sibling must already be inserted
				Contribution parent = refSib.getParent();
				if (parent == null)
					return false; //parent must exist for proper insertion
				Contribution newSib = getSourceTopic(from);
				if (newSib == null)
					return false;//topic already inserted
					
				if(label != null && (newSib instanceof HelpTopicRef))
					((HelpTopicRef) newSib).setRawLabel(label);
		
				((HelpContribution) parent).insertNeighbouringChild(refSib,	newSib,	mode);

				// keep track of this insertion for handling stanalone actions
				trackTopic(from);

			} else if (isValidTopicSet(from)) {
				Contribution refSib = getTargetTopic(to, false);
				if (refSib == null)
					return false; //sibling must exist
				Contribution parent = refSib.getParent();
				if (parent == null)
					return false; //parent must exist for proper insertion
				// insert all the child topics nodes
				Contribution topicSet = (Contribution) topicSetNodeMap.get(from);
				List topics = topicSet.getChildrenList();
				if (mode == Contribution.NEXT) {
					for (int i = topics.size() - 1; i >= 0; i--) {
						Contribution topic = (Contribution) topics.get(i);
						Inserter newInserter = new Inserter(topic.getID(), to, null, mode);
						newInserter.insert();
					}
				} else {
					for (int i = 0; i < topics.size(); i++) {
						Contribution topic = (Contribution) topics.get(i);
						Inserter newInserter = new Inserter(topic.getID(), to, null, mode);
						newInserter.insert();
					}
				}
			}
			return true; //success
		}
	}
	
	
	
	/**
	 * HelpViewManager constructor comment.
	 */
	public InfosetBuilder(ContributionManager cmgr) {
		infosets = cmgr.getContributionsOfType(ViewContributor.INFOSET_ELEM);
		topics = cmgr.getContributionsOfType(TopicContributor.TOPICS_ELEM);
		actions = cmgr.getContributionsOfType(ActionContributor.ACTIONS_ELEM);
	}
	/**
	 * Builds the views objects from the view data
	 */
	public Map buildInformationSets() {
		// Index actions and topics by ID for easier lookup
		sortTopics();
		sortActions();

		// Create the views set objects
		while (infosets.hasNext()) {
			InfoSet infoset = (InfoSet) infosets.next();
			infoSetMap.put(infoset.getID(), infoset);
			// Create the InfoView objects
			for (Iterator views = infoset.getChildren(); views.hasNext();) {
				InfoView view = (InfoView) views.next();
				viewNodeMap.put(view.getID(), view);
			}
			// build the views for this info set
			buildViews(infoset);
		}

		// run the standalone actions to build stand-alone navigation
		buildStandaloneNavigation();

		// do not keep empty infosets that were tagged as standalone
		discardEmptyStandaloneInfosets();

		return infoSetMap;
	}
	/**
	 * Builds the topics contributed by standalone actions
	 */
	private void buildStandaloneNavigation() {
		Iterator infosets = infoSetMap.values().iterator();
		while (infosets.hasNext()) {
			InfoSet infoset = (InfoSet) infosets.next();
			InfoView[] infoviews = infoset.getViews();
			for (int i = 0; i < infoviews.length; i++) {
				// get list of standalone actions for this view
				setCurrentView(infoviews[i]);
				List actions = (List) standaloneActionNodeMap.get(infoviews[i].getID());
				List validStandaloneActions = getValidStandaloneActions(actions);
				executeActions(infoviews[i], validStandaloneActions);
			}
		}
	}
	/**
	 * Builds the view object by wiring up the topics
	 * as specified in the insert actions.
	 * @return 
	 * @param viewName java.lang.String
	 */
	private void buildView(InfoView view) {
		setCurrentView(view);
		// Get build actions for this view
		List actions = (List) actionNodeMap.get(view.getID());
		executeActions(view, actions);
	}
	/**
	 * Builds the views objects for an info set
	 * @return 
	 * @param viewName java.lang.String
	 */
	private void buildViews(InfoSet infoSet) {
		for (Iterator views = infoSet.getChildren(); views.hasNext();) {
			InfoView view = (InfoView) views.next();
			// build the rest of the view (wire up all the topics)
			buildView(view);
		}
	}
	/**
	 * Remove empty standalone infosets
	 */
	private void discardEmptyStandaloneInfosets() {
		Iterator infosets = infoSetMap.values().iterator();
		while (infosets.hasNext()) {
			InfoSet infoset = (InfoSet) infosets.next();
			if (!infoset.isStandalone())
				continue;
			InfoView[] infoviews = infoset.getViews();
			boolean foundNonEmptyView = false;
			for (int i = 0; !foundNonEmptyView && i < infoviews.length; i++)
				if (!infoviews[i].getChildrenList().isEmpty())
					foundNonEmptyView = true;

			if (!foundNonEmptyView)
				infosets.remove();
		}
	}
	/**
	 * Builds the view object by wiring up the topics
	 * as specified in the insert actions.
	 * @return 
	 * @param viewName java.lang.String
	 */
	private void executeActions(InfoView view, List actions) {
		if (actions == null)
			return;
		// keep track of inserts that did not succeed at first
		List deferredInserts = new ArrayList();
		
		for (Iterator e = actions.iterator(); e.hasNext();)
		 {
			Action actionsNode = (Action) e.next();
			// execute each action in part.
			// assume they all inserts
			for (Iterator actionsList=actionsNode.getChildren();actionsList.hasNext();) 
			{
				Insert insertNode = (Insert) actionsList.next();
				if (!execute(insertNode))
					deferredInserts.add(insertNode);			
			}
		}
		// executing delayed inserts
		int noDelayed = 0;
		while (deferredInserts.size() != noDelayed) {
			noDelayed = deferredInserts.size();
			for (int i=0; i<deferredInserts.size(); i++) {
				if (execute((Insert) deferredInserts.get(i)))
					deferredInserts.remove(i--);
			}
		}
		// report unexecuted actions
		for( int i=0; i<deferredInserts.size(); i++){
			Insert insertNode = (Insert)deferredInserts.get(i);
			String from = insertNode.getSource();
			String to = insertNode.getTarget();
			Logger.logWarning(Resources.getString("W002", "from=\""+from+"\" to=\""+to+"\""));
		}
	}
	
	
	/**
	  * Execute the insert action. May need to execute nested action if not successful.
	  */
	private boolean execute(Insert insertNode) {  				
		// check conditions
		Inserter inserter = new Inserter(insertNode);
		if (inserter.hasValidData()) 
			return inserter.insert();
		else
			return executeAlternateActions(insertNode);
	}


	private boolean executeAlternateActions(Insert insertNode) {
		// execute each action in part
		Iterator nested = insertNode.getChildren();
		if (nested.hasNext())
			return execute((Insert)nested.next());
		else
			return false;
	}
		
	/**
	 * Returns the actions are valid as standalone actions.
	 * Valid actions are those whose "from" topics are from non-integrated plugins.
	 */
	private List getValidStandaloneActions(List actions) {
		if (actions == null)
			return null;
		List validStandaloneActions = new ArrayList(actions.size());
		for (Iterator e = actions.iterator(); e.hasNext();) {
			Action actionsNode = (Action) e.next();

			// check each action in part
			boolean isStandaloneAction = true;
			for (Iterator actionsList = actionsNode.getChildren();
				isStandaloneAction && actionsList.hasNext(); ) 
			{
				Insert insertNode = (Insert) actionsList.next();
				String from = insertNode.getSource();
				int index = from.lastIndexOf('.');
				if (index == -1)
					continue;
				String plugin = from.substring(0, index);
				if (integratedPlugins.contains(plugin))
					isStandaloneAction = false;
			}
			if (isStandaloneAction)
				validStandaloneActions.add(actionsNode);
		}

		return validStandaloneActions;
	}

	private boolean isValidTopic(String topicID) {
		return topicNodeMap.get(topicID) != null;
	}
	private boolean isValidTopicSet(String topicSetID) {
		return topicSetNodeMap.get(topicSetID) != null;
	}
	private boolean isValidView(String viewName) {
		return (viewNodeMap.get(viewName) != null);
	}
	/**
	 * Indexes actions by view id
	 */
	private void sortActions() {
		while (actions.hasNext()) {
			Action actionNode = (Action) actions.next();
			String viewID = actionNode.getView();
			if (viewID.equals("")) {
				// *** EXCEPTION!!!
			} else {
				// do a CASE SENSITIVE match on the view name.
				// We store a vector of actions under one id
				if (actionNode.isStandalone()) {
					List actions = (List) standaloneActionNodeMap.get(viewID);
					if (actions == null)
						actions = new ArrayList();
					actions.add(actionNode);
					standaloneActionNodeMap.put(viewID, actions);
				} else {
					List actions = (List) actionNodeMap.get(viewID);
					if (actions == null)
						actions = new ArrayList();
					actions.add(actionNode);
					actionNodeMap.put(viewID, actions);
				}
			}
		}
	}
	/**
	 * Indexes topics by their ID for easier lookup
	 */
	private void sortTopics() {
		while (topics.hasNext()) {
			Contribution topicSet = (Contribution) topics.next();
			topicSetNodeMap.put(topicSet.getID(), topicSet);

			// Put all the topics (the whole nested tree) into a hashtable indexed by id
			Stack stack = new Stack();
			stack.push(topicSet.getChildren());
			while (!stack.isEmpty()) {
				Iterator children = (Iterator) stack.pop();
				while (children.hasNext()) {
					Contribution topic = (Contribution) children.next();
					topicNodeMap.put(topic.getID(), topic);
					Iterator subtopics = topic.getChildren();
					if (subtopics.hasNext())
						stack.push(subtopics);
				}
			}
		}
	}
	
	/**
	 * Prepares to insert to this view
	 */
	private void setCurrentView (InfoView view)
	{
		this.view = (HelpInfoView)view;
		sources.clear();
		targets.clear();
		// add the view as a possible target
		// so <insert from=topic to=view> works
		targets.put(view.getID(), view);
		// this can likely be removed, as it seems obsolete
		///topicNodeMap.put(view.getID(), view);
	}
	/**
	 * Tracks the topics by its plugin.
	 * All the integrated plugins will be remembered, so the standalone actions
	 * will not be applied to them.
	 */
	private void trackTopic(String topicId) {
		int i = topicId.lastIndexOf('.');
		if (i == -1)
			return;
		String pluginId = topicId.substring(0, i);
		integratedPlugins.add(pluginId);
	}
	
	/**
	 * Returns a clone/reference topic that can be used as 
	 * an insertion source. Create one if necessary.
	 * Returns null if this topic has already been inserted into this infoview.
	 */
	private Contribution getSourceTopic(String id)
	{
		Object topic = sources.get(id);
		// we don't insert the same topic twice in a view
		if (topic != null)	return null; 
			
		// see if this was already a target, so it has a clone
		topic = targets.get(id);
		// need to create the topic clone/ref
		if (topic == null)
		{
			topic = new HelpTopicRef((Topic)topicNodeMap.get(id));
			sources.put(id, topic);
			// recursively "clone" the topic subtree
			insertChildrenTopics((HelpTopicRef)topic);
		}
		return (Contribution)topic;
	}
	
	/**
	 * Returns a clone/reference topic that can be used as
	 * an insertion target. Create one if asked for.
	 * Returns null if the id refers to a topic that cannot be an insert target.
	 */
	private Contribution getTargetTopic(String id, boolean create)
	{
		// check if already a source or target
		Object topic = targets.get(id);
		if (topic == null) topic = sources.get(id);
		if (topic == null && create)
		{
			topic = new HelpTopicRef((Topic)topicNodeMap.get(id));
			targets.put(id, topic);
			// recursively "clone" the topic subtree
			insertChildrenTopics((HelpTopicRef)topic);
		}
		return (Contribution)topic;
	}
	
	/**
	 * Recursively inserts all the original hardcoded children
	 * (Later we may want to do some sort of "clone" operation
	 * as opposed to using topic ref and such.
	 * It is assumed that the parent is a TopicRef.
	 */
	private void insertChildrenTopics(TopicRef parent)
	{
		// now recursively insert all the original hardcoded children
		Topic topic = parent.getTopic();
		for (Iterator childTopics = topic.getChildren(); childTopics.hasNext();) {
			Contribution childTopic = (Contribution) childTopics.next();
			Inserter newInserter = new Inserter(childTopic.getID(), parent.getID(), null, Contribution.NORMAL);					
			newInserter.insert();
		}
	}
}
