/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.io.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.util.*;

public class NavigationBuilder {
	// list of all topics files
	protected Collection contributedTopicsFiles;

	// list of unprocessed topics files
	protected Collection unprocessedTopicsFiles;
	
	// list of unprocessed topics (the target of attach_to was not available at the time)
	protected List unprocessedTopics;

	/**
	 * Constructor.
	 */
	public NavigationBuilder() {
		unprocessedTopicsFiles = new ArrayList();
		unprocessedTopics = new ArrayList();
	}

	public Collection getBuiltTopics() {
		// returns the list of root Topics trees
		Collection topicsCol = new ArrayList(contributedTopicsFiles.size());
		for (Iterator it = contributedTopicsFiles.iterator(); it.hasNext();) {
			TopicsFile topicsFile = (TopicsFile) it.next();
			Topics topics = topicsFile.getTopics();
			if (topics != null && topics.getParents().isEmpty())
				topicsCol.add((topics));
		}
		return topicsCol;
	}

	/**
	 * @return Topics Collection
	 */
	public void build(Collection contributedTopicsFiles) {
		this.contributedTopicsFiles = contributedTopicsFiles;
		unprocessedTopicsFiles.addAll(contributedTopicsFiles);

		// process all the topics files.
		// A side-effect is that linked files are also processed
		while (!unprocessedTopicsFiles.isEmpty()) {
			TopicsFile topicsFile =
				(TopicsFile) unprocessedTopicsFiles.iterator().next();
			topicsFile.build(this);
		}
		
		// try processing as many topics (attach_to) as possible
		// All these topics could not be attached because the 
		// target node was not parsed at that time
		int remaining = unprocessedTopics.size();
		for (int i=0; i<remaining; i++ )
		{
			Topics topics = (Topics)unprocessedTopics.get(i);
			buildTopics(topics);
		}
	}

	public void buildTopicsFile(TopicsFile topicsFile) {
		unprocessedTopicsFiles.remove(topicsFile);
		
		//topicsFile.build(this);
		TopicsFileParser parser = new TopicsFileParser(this);
		parser.parse(topicsFile);
	}

	public void buildAnchor(Anchor anchor) {
		// cache the anchor in the topics file
		anchor.getTopicsFile().addAnchor(anchor);
	}

	public void buildInclude(Include include) {
		// parse the included file

		String includeHref = include.getHref();
		TopicsFile includedTopicsFile = getTopicsFile(includeHref);
		if (includedTopicsFile == null)
			return;

		Topics topics = includedTopicsFile.getTopics();
		if (topics == null)
			return;

		// link the two Topics objects
		include.addChild(topics);
	}

	public void buildTopic(Topic topic) {
		// nothing to do
	}

	public void buildTopics(Topics topics) {
		// attach them if so specified
		String href = topics.getAttach_to();
		if (href == null || href.equals(""))
			return;

		TopicsFile targetTopicsFile = getTopicsFile(href);
		if (targetTopicsFile == null)
			return;

		Anchor anchor = targetTopicsFile.getAnchor(href);
		if (anchor == null)
		{
			unprocessedTopics.add(topics);
			return; 
		}

		// link the two topics objects
		anchor.addChild(topics);
	}

	private TopicsFile getTopicsFile(String href) {
		String plugin = HrefUtil.getPluginIDFromHref(href);
		if (plugin == null)
			return null;
		String path = HrefUtil.getResourcePathFromHref(href);
		if (path == null)
			return null;

		TopicsFile topicsFile = null;
		for (Iterator it = contributedTopicsFiles.iterator(); it.hasNext();) {
			topicsFile = (TopicsFile) it.next();
			if (topicsFile.getPluginID().equals(plugin)
				&& topicsFile.getHref().equals(path))
				break;
			else
				topicsFile = null;
		}

		if (topicsFile == null)
			return null;

		if (unprocessedTopicsFiles.contains(topicsFile))
			buildTopicsFile(topicsFile);

		return topicsFile;
	}

}