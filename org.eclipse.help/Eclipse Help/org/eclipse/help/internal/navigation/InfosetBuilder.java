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
	// Contributions data
	private Iterator infosets;
	private Iterator topics;
	private Iterator actions;

	// Topics data indexed by ID
	private Map topicNodeMap = new HashMap(/* of <topic> Node */);
	private Map topicSetNodeMap = new HashMap(/* of <topics> Node */);

	// Infosets
	private Map infoSetMap = new HashMap(/* of InfoSet */);

	// Views data indexed by ID
	private Map viewNodeMap = new HashMap(/* of <view> Node */);
	// Actions data indexed by view ID
	private Map actionNodeMap = new HashMap(/* of Vector of <action> Node */);
	// Solo (default) actions data indexed by view ID
	private Map soloActionNodeMap = new HashMap(/* of Vector of <action> Node */);

	// Keep track of plugins with inserted topics
	private Set integratedPlugins = new HashSet(/* of String */);

	/*******************************************************/
	/*****  InsertAction ***********************************/
	/*******************************************************/
	// Action command to execute the insert scripts
	class InsertAction //extends HelpContribution
	{
		private InfoView view;
		private Insert insertNode;

		public InsertAction(InfoView view, Insert insertNode) {
			//super(node);
			this.view = view;
			HelpTopicFactory.setView(this.view);
			this.insertNode = insertNode;
		}

		/**
		  * Execute the insert action. May need to execute nested action if not successful.
		  */
		public boolean execute() {
			String fromID = insertNode.getSource();
			String toID = insertNode.getTarget();
   
			String newLabel = insertNode.getRawLabel();
			// check conditions
			if (isKnownView(fromID))
				return false;
			if ((isKnownTopic(fromID) || isKnownTopicSet(fromID))
				&& (isKnownView(toID) || isKnownTopic(toID))) 
			{
				if (insertTopic(fromID, toID, view, insertNode.getMode(), newLabel))
					return true;
			}
			return executeNested();
		}
		/**
		 *  Writes this action to the log
		 */
		public void fail() {
			String fromID = insertNode.getSource();
			String toID = insertNode.getTarget();
			Logger.logWarning(Resources.getString("WS03", "from=\""+fromID+"\" to=\""+toID+"\""));
		}

		private boolean executeNested() {
			// execute each action in part
			for (Iterator nested = insertNode.getChildren(); nested.hasNext();) {
				InsertAction action = new InsertAction(view, (HelpInsert) nested.next());
				return action.execute();
			}
			return false;
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

		// run the solo actions to build stand-alone navigation
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
				// get list of solo actions for this view
				List actions = (List) soloActionNodeMap.get(infoviews[i].getID());
				List validSoloActions = getValidSoloActions(actions);
				executeActions(infoviews[i], validSoloActions);
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
		String viewID = view.getID();
		// Registers the view as a topic, so <insert from=topic to=view> work
		 ((HelpInfoView) view).registerTopic(view);
		topicNodeMap.put(viewID, view);

		// Get build actions for this view
		List actions = (List) actionNodeMap.get(viewID);
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
		List unexecutedActions = new ArrayList();
		for (Iterator e = actions.iterator(); e.hasNext();) {
			Action actionsNode = (Action) e.next();

			// execute each action in part
			for (Iterator actionsList = actionsNode.getChildren();
				actionsList.hasNext();
				) {
				Insert insertNode = (Insert) actionsList.next();
				InsertAction action = new InsertAction(view, insertNode);
				if (!action.execute())
					unexecutedActions.add(action);
			}
		}
		// executing delayed actions
		int noDelayed = 0;
		while (unexecutedActions.size() != noDelayed) {
			noDelayed = unexecutedActions.size();
			for (int i = 0; i < unexecutedActions.size(); i++) {
				InsertAction action = (InsertAction) unexecutedActions.get(i);
				if (action.execute())
					unexecutedActions.remove(i--);
			}
		}
		// report unexecuted actions
		for( int i=0; i<unexecutedActions.size(); i++){
			((InsertAction)unexecutedActions.get(i)).fail();
		}
	}
	/**
	 * Returns the actions are valid as standalone actions.
	 * Valid actions are those whose "from" topics are from non-integrated plugins.
	 */
	private List getValidSoloActions(List actions) {
		if (actions == null)
			return null;
		List validSoloActions = new ArrayList(actions.size());
		for (Iterator e = actions.iterator(); e.hasNext();) {
			Action actionsNode = (Action) e.next();

			// check each action in part
			boolean isSoloAction = true;
			for (Iterator actionsList = actionsNode.getChildren();
				isSoloAction && actionsList.hasNext();
				) {
				Insert insertNode = (Insert) actionsList.next();
				String from = insertNode.getSource();
				int index = from.lastIndexOf('.');
				if (index == -1)
					continue;
				String plugin = from.substring(0, index);
				if (integratedPlugins.contains(plugin))
					isSoloAction = false;
			}
			if (isSoloAction)
				validSoloActions.add(actionsNode);
		}

		return validSoloActions;
	}
	/**
	 * Inserts the topic and creates the HelpTopic objects
	 */
	private boolean insertTopic(
		String fromTopic,
		String toTopic,
		InfoView view,
		int positionPreference,
		String newLabel) {
		if ((positionPreference == HelpContribution.PREV)
			|| (positionPreference == HelpContribution.NEXT))
			return insertTopicAsSib(fromTopic, toTopic, view, positionPreference, newLabel);
		else
			return insertTopicAsChild(fromTopic, toTopic, view, positionPreference, newLabel);
	}
	/**
	 * Inserts the topic and creates the HelpTopic objects
	 */
	private boolean insertTopicAsChild(
		String fromTopic,
		String toTopic,
		InfoView view,
		int positionPreference,
		String newLabel) {
		// do a simple insert
		if (isKnownTopic(fromTopic)) {
			Contribution parent = ((HelpInfoView) view).getContribution(toTopic);
			if (parent == null)
				parent = HelpTopicFactory.createTopic((Topic) topicNodeMap.get(toTopic));
			Contribution child = ((HelpInfoView) view).getContribution(fromTopic);
			if (child == null)
				child = HelpTopicFactory.createTopic((Topic) topicNodeMap.get(fromTopic));
			else
				return false;//topic already inserted
			if(newLabel!=null){
				if(child instanceof HelpTopicRef)
					((HelpTopicRef) child).setRawLabel(newLabel);
			}
			((HelpContribution) parent).insertChild(child, positionPreference);

			// keep track of this insertion for handling solo actions
			trackTopic(fromTopic);

			// now recursively insert all the children
			Contribution topicNode = child;
			if (child instanceof TopicRef)
				topicNode = ((TopicRef) child).getTopic();

			for (Iterator childTopics = topicNode.getChildren(); childTopics.hasNext();) {
				Contribution childNode = (Contribution) childTopics.next();
				String newFromTopic = childNode.getID();
				insertTopic(newFromTopic, fromTopic, view, HelpContribution.NORMAL, null);
			}
		} else
			if (isKnownTopicSet(fromTopic)) {
				// insert all the child topics nodes
				Contribution topicSet = (Contribution) topicSetNodeMap.get(fromTopic);
				for (Iterator topics = topicSet.getChildren(); topics.hasNext();) {
					Contribution topic = (Contribution) topics.next();
					insertTopic(topic.getID(), toTopic, view, positionPreference, null);
				}
			}
		return true; //success
	}
	/**
	 * Inserts the topic and creates the HelpTopic objects
	 */
	private boolean insertTopicAsSib(
		String fromTopic,
		String nearTopic,
		InfoView view,
		int positionPreference,
		String newLabel) {
		// do a simple insert
		if (isKnownTopic(fromTopic)) {
			Contribution refSib = ((HelpInfoView) view).getContribution(nearTopic);
			if (refSib == null)
				return false; //sibling must already be inserted
			Contribution parent = refSib.getParent();
			if (parent == null)
				return false; //parent must exist for proper insertion
			Contribution newSib = ((HelpInfoView) view).getContribution(fromTopic);
			if (newSib == null)
				newSib = HelpTopicFactory.createTopic((Topic) topicNodeMap.get(fromTopic));
			else
				return false;//topic already inserted	
			if(newLabel!=null){
				if(newSib instanceof HelpTopicRef)
					((HelpTopicRef) newSib).setRawLabel(newLabel);
			}
			((HelpContribution) parent).insertNeighbouringChild(
				refSib,
				newSib,
				positionPreference);

			// keep track of this insertion for handling stanalone actions
			trackTopic(fromTopic);

			// now recursively insert all the children
			Contribution topicNode = newSib;
			if (newSib instanceof TopicRef)
				topicNode = ((TopicRef) newSib).getTopic();

			for (Iterator childTopics = topicNode.getChildren(); childTopics.hasNext();) {
				Contribution childNode = (Contribution) childTopics.next();
				String newFromTopic = childNode.getID();
				insertTopic(newFromTopic, fromTopic, view, HelpContribution.NORMAL, null);
			}
		} else
			if (isKnownTopicSet(fromTopic)) {
				Contribution refSib = ((HelpInfoView) view).getContribution(nearTopic);
				if (refSib == null)
					return false; //sibling must exist
				Contribution parent = refSib.getParent();
				if (parent == null)
					return false; //parent must exist for proper insertion
				// insert all the child topics nodes
				Contribution topicSet = (Contribution) topicSetNodeMap.get(fromTopic);
				List topics = topicSet.getChildrenList();
				if (positionPreference == HelpContribution.NEXT) {
					for (int i = topics.size() - 1; i >= 0; i--) {
						Contribution topic = (Contribution) topics.get(i);
						insertTopic(topic.getID(), nearTopic, view, positionPreference, null);
					}
				} else {
					for (int i = 0; i < topics.size(); i++) {
						Contribution topic = (Contribution) topics.get(i);
						insertTopic(topic.getID(), nearTopic, view, positionPreference, null);
					}
				}
			}
		return true; //success
	}
	public boolean isKnownTopic(String topicID) {
		return topicNodeMap.get(topicID) != null;
	}
	public boolean isKnownTopicSet(String topicSetID) {
		return topicSetNodeMap.get(topicSetID) != null;
	}
	private boolean isKnownView(String viewName) {
		return (viewNodeMap.get(viewName) != null);
	}
	/**
	 * Indexes actions by view id
	 * @return java.util.Vector
	 * @param viewName java.lang.String
	 */
	private void sortActions() {
		while (actions.hasNext()) {
			Action actionNode = (Action) actions.next();
			String viewID = actionNode.getView();
			if (viewID.equals("")) {
				// XXX EXCEPTION!!!
			} else {
				// do a CASE SENSITIVE match on the view name.
				// We store a vector of actions under one id
				if (actionNode.isStandalone()) {
					List actions = (List) soloActionNodeMap.get(viewID);
					if (actions == null)
						actions = new ArrayList();
					actions.add(actionNode);
					soloActionNodeMap.put(viewID, actions);
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
}
