/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.UAContentFilter;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.remote.RemoteHelp;

/**
 * Helper class for tocView.jsp initialization
 */
public class TocData extends ActivitiesData {
	// maximum number of topics in a book for generating all topics at once
	private static int loadBookAtOnceLimit;
	// suggested number of topic levels for large books
	private static int dynamicLoadDepths;
	// maximum number of topics generated when loading levels dynamically
	// above which dynamicLoadDepths is ignored, the rest of branches will be 1
	// deep
	private static int honorLevelsLimit;

	// Request parameters
	private String tocHref;
	private String topicHref;

	// help form of selected topic href
	private String topicHelpHref;
	// Selected TOC
	private int selectedToc;
	// path from TOC to the root topic of the TOC fragment
	private int[] rootPath = null;
	// path from TOC to the selected topic, excluding TOC;
	private ITopic[] topicPath = null;
	// Number of topics generated so far
	private int topicsGenerated = 0;

	// List of TOC's, unfiltered
	private IToc[] tocs;
	// List of TOC's, filtered by roles
	//private IToc[] filteredTocs;

	// images directory
	private String imagesDirectory;

	/**
	 * Constructs the xml data for the contents page.
	 * 
	 * @param context
	 * @param request
	 */
	public TocData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
		if (dynamicLoadDepths < 1) {
			WebappPreferences pref = new WebappPreferences();
			loadBookAtOnceLimit = pref.getBookAtOnceLimit();
			dynamicLoadDepths = pref.getLoadDepth();
			honorLevelsLimit = loadBookAtOnceLimit / 4;
		}

		this.tocHref = request.getParameter("toc"); //$NON-NLS-1$
		this.topicHref = request.getParameter("topic"); //$NON-NLS-1$
		if (tocHref != null && tocHref.length() == 0)
			tocHref = null;
		if (topicHref != null && topicHref.length() == 0)
			topicHref = null;
		
		String anchor = request.getParameter("anchor"); //$NON-NLS-1$
		if (topicHref != null && anchor != null) {
			topicHref = topicHref + '#' + anchor;
		}
		// initialize rootPath
		String pathStr = request.getParameter("path"); //$NON-NLS-1$
		if (pathStr != null && pathStr.length() > 0) {
			String[] paths = pathStr.split("_", -1); //$NON-NLS-1$
			int[] indexes = new int[paths.length];
			boolean indexesOK = true;
			for (int i = 0; i < paths.length; i++) {
				try {
					indexes[i] = Integer.parseInt(paths[i]);
				} catch (NumberFormatException nfe) {
					indexesOK = false;
					break;
				}
				if (indexesOK) {
					rootPath = indexes;
				}
			}
		}

		imagesDirectory = preferences.getImagesDirectory();

		loadTocs();
	}

	/*
	 * Counts and returns the number of topics inside the given TOC (all
	 * descendants, not just subtopics), including the overall book's topic.
	 */
	private static int countTopics(IToc toc) {
		return countTopics(toc.getTopics()) + 1;
	}
	
	/*
	 * Counts and returns the number of topics in the given array and all
	 * subtopics.
	 */
	private static int countTopics(ITopic[] topics) {
		int count = topics.length;
		for (int i=0;i<topics.length;++i) {
			ITopic[] subtopics = topics[i].getSubtopics();
			if (subtopics != null && subtopics.length > 0) {
				count += countTopics(subtopics);
			}
		}
		return count;
	}
	
	// Accessor methods to avoid exposing help classes directly to JSP.
	// Note: this seems ok for now, but maybe we need to reconsider this
	//       and allow help classes in JSP's.

	public boolean isRemoteHelpError() {
		boolean isError = (RemoteHelp.getError() != null);
		if (isError) {
			RemoteHelp.clearError();
		}
		return isError;
	}
	
	public int getTocCount() {
		return tocs.length;
	}

	public String getTocLabel(int i) {
		return tocs[i].getLabel();
	}

	public String getTocHref(int i) {
		return tocs[i].getHref();
	}

	public String getTocDescriptionTopic(int i) {
		return UrlUtil.getHelpURL(tocs[i].getTopic(null).getHref());
	}

	/*
	 * Finds a path of ITopics in the given IToc to the given topic. If the
	 * toc doesn't contain the topic, returns null.
	 */
	private static ITopic[] getTopicPathInToc(ITopic topicToFind, IToc toc) {
		ITopic topics[] = toc.getTopics();
		if (topics != null) {
			for (int i=0;i<topics.length;++i) {
				// returns path in reverse order
				List reversePath = getTopicPathInToc(topicToFind, topics[i]);
				if (reversePath != null) {
					// reverse and return
					ITopic[] path = new ITopic[reversePath.size()];
					for (int j=0;j<path.length;++j) {
						path[j] = (ITopic)reversePath.get((path.length - 1) - j);
					}
					return path;
				}
			}
		}
		return null;
	}
	
	/*
	 * Finds the topic in the given topic sub-tree. Returns a path of ITopics
	 * to that topic in reverse order (from the topic up).
	 */
	private static List getTopicPathInToc(ITopic topicToFind, ITopic topic) {
		if (topic == topicToFind) {
			// found it. start the list to be created recursively
			List path = new ArrayList();
			path.add(topic);
			return path;
		}
		else {
			ITopic[] subtopics = topic.getSubtopics();
			for (int i=0;i<subtopics.length;++i) {
				List path = getTopicPathInToc(topicToFind, subtopics[i]);
				if (path != null) {
					// it was in a subtopic.. add to the path and return
					path.add(topic);
					return path;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the selected TOC
	 * 
	 * @return int
	 */
	public int getSelectedToc() {
		return selectedToc;
	}

	/**
	 * Returns the topic to display. If there is a TOC, return its topic
	 * description. Return null if no topic is specified and there is no toc
	 * description.
	 * 
	 * @return String
	 */
	public String getSelectedTopic() {
		if (topicHref != null && topicHref.length() > 0)
			return UrlUtil.getHelpURL(topicHref);
		else
		if (selectedToc == -1)
			return null;
		IToc toc = tocs[selectedToc];
		ITopic tocDescription = toc.getTopic(null);
		if (tocDescription != null)
			return UrlUtil.getHelpURL(tocDescription.getHref());
		return UrlUtil.getHelpURL(null);
	}

	/**
	 * Returns a list of all the TOC's as xml elements. Individual TOC's are not
	 * loaded yet.
	 * 
	 * @return IToc[]
	 */
	public IToc[] getTocs() {
		return tocs;
	}

	/**
	 * Check if given TOC is visible
	 * 
	 * @param toc
	 * @return true if TOC should be visible
	 */
	public boolean isEnabled(int toc) {
		if (!isEnabled(tocs[toc])) {
			return false;
		}
		// do not generate toc when there are no leaf topics
		return (getEnabledSubtopicList(tocs[toc]).size() > 0);
	}
	/**
	 * Check if given TOC is visible
	 * 
	 * @param toc
	 * @return true if TOC should be visible
	 */
	private boolean isEnabled(IToc toc) {
		if(!isAdvancedUI()){
			// activities never filtered for basic browsers
			return true;
		}
		return HelpBasePlugin.getActivitySupport().isEnabled(toc.getHref()) &&
			!UAContentFilter.isFiltered(toc);
	}

	private void loadTocs() {
		tocs = HelpPlugin.getTocManager().getTocs(getLocale());
		// Find the requested TOC
		selectedToc = -1;
		if (tocHref != null && tocHref.length() > 0) {
			tocs = getTocs();
			for (int i = 0; selectedToc == -1 && i < tocs.length; i++) {
				if (tocHref.equals(tocs[i].getHref())) {
					selectedToc = i;
				}
			}
		} else {
			// try obtaining the TOC from the topic
			selectedToc = findTocContainingTopic(topicHref);

			ITopic topic = findTopic();
			if (topic != null) {
				topicPath = getTopicPathInToc(topic, tocs[selectedToc]);
			}
		}
	}

	/**
	 * Finds a TOC that contains specified topic
	 * 
	 * @param topic
	 *            the topic href
	 */
	private int findTocContainingTopic(String topic) {
		if (topic == null || topic.equals("")) //$NON-NLS-1$
			return -1;

		int index = topic.indexOf("/topic/"); //$NON-NLS-1$
		if (index != -1)
			topic = topic.substring(index + 6);
		index = topic.indexOf('?');
		if (index != -1)
			topic = topic.substring(0, index);

		if (topic == null || topic.equals("")) //$NON-NLS-1$
			return -1;

		tocs = getTocs();
		// try to find in enabled tocs first
		for (int i = 0; i < tocs.length; i++)
			if (isEnabled(i))
				if (tocs[i].getTopic(topic) != null)
					return i;
		// try disabled tocs second
		for (int i = 0; i < tocs.length; i++)
			if (!isEnabled(i))
				if (tocs[i].getTopic(topic) != null)
					return i;

		// nothing found
		return -1;
	}
	/**
	 * Finds topic in a TOC
	 * 
	 * @return ITopic or null
	 */
	private ITopic findTopic() {
		String topic = getSelectedTopic();
		if (topic == null || topic.equals("")) //$NON-NLS-1$
			return null;

		int index = topic.indexOf("/topic/"); //$NON-NLS-1$
		if (index != -1)
			topic = topic.substring(index + 6);
		index = topic.indexOf('?');
		if (index != -1)
			topic = topic.substring(0, index);

		if (topic == null || topic.equals("")) //$NON-NLS-1$
			return null;

		if (getSelectedToc() < 0)
			return null;
		IToc selectedToc = getTocs()[getSelectedToc()];
		if (selectedToc == null)
			return null;
		return selectedToc.getTopic(topic);
	}

	/**
	 * Generates the HTML code (a tree) for a TOC.
	 * 
	 * @param toc
	 * @param out
	 * @throws IOException
	 */
	public void generateToc(int toc, Writer out) throws IOException {
		ITopic[] topics = getEnabledSubtopics(tocs[toc]);
		if (topics.length <= 0) {
			// do not generate toc when there are no leaf topics
			return;
		}

		int maxLevels = dynamicLoadDepths;
		if (countTopics(tocs[toc]) <= loadBookAtOnceLimit) {
			maxLevels = -1;
		}
		// Construct ID of subtree root
		StringBuffer id = new StringBuffer();
		if (rootPath != null) {
			// navigate to root topic, skipping parents
			for (int p = 0; p < rootPath.length; p++) {
				if (id.length() > 0) {
					id.append('_');
				}
				topics = getEnabledSubtopics(topics[rootPath[p]]);
				id.append(rootPath[p]);
			}
			out.write("<ul class='expanded' id=\"" + id.toString() + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		for (int i = 0; i < topics.length; i++) {
			String idPrefix = id.toString();
			if (idPrefix.length() > 0) {
				idPrefix = idPrefix + "_" + Integer.toString(i); //$NON-NLS-1$
			} else {
				idPrefix = Integer.toString(i);
			}

			generateTopic(topics[i], out, idPrefix, maxLevels, rootPath == null
					? 0
					: rootPath.length);
		}

		if (rootPath != null) {
			out.write("</ul>\n"); //$NON-NLS-1$
		}

	}

	/**
	 * @param topic
	 * @param out
	 * @param maxLevels
	 *            relative number of topic levels to generate (pass <0 for
	 *            inifinite), 1 generates this topic as last level topic
	 * @param currentLevel
	 *            current level of topic, 0 is first Level under TOC
	 * @throws IOException
	 */
	private void generateTopic(ITopic topic, Writer out, String id,
			int maxLevels, int currentLevel) throws IOException {
		if (maxLevels == 0) {
			return;
		}

		topicsGenerated++;
		if (maxLevels > 1 && topicsGenerated > honorLevelsLimit) {
			maxLevels = 1;
		}

		ITopic[] topics = getEnabledSubtopics(topic);
		boolean hasNodes = topics.length > 0;

		if (hasNodes) {
			out.write("<li>"); //$NON-NLS-1$
			out.write("<img src='"); //$NON-NLS-1$
			out.write(imagesDirectory);
			out
					.write("/plus.gif' class='collapsed' alt=\"" + ServletResources.getString("topicClosed", request) + "\" title=\"" + ServletResources.getString("topicClosed", request) + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			out.write("<a href=" +"\""+ UrlUtil.getHelpURL(topic.getHref()) + "\""+">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			out.write("<img src='"); //$NON-NLS-1$
			out.write(imagesDirectory);
			out.write("/container_obj.gif' alt=\"\">"); //$NON-NLS-1$
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a>"); //$NON-NLS-1$

			// is it ancestor of topic to reveal
			boolean isAncestor = topicPath != null
					&& topicPath.length > currentLevel + 1
					&& topicPath[currentLevel] == topic;

			if (maxLevels != 1 || isAncestor) {
				out.write("<ul class='collapsed'>\n"); //$NON-NLS-1$
			} else {
				// children will not be generated
				out.write("<ul class='collapsed' id=\"" + id + "\">\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (1 <= maxLevels && maxLevels <= dynamicLoadDepths && isAncestor) {
				// ignore max levels, show children
				for (int i = 0; i < topics.length; i++) {
					generateTopic(topics[i], out, id + "_" + i, //$NON-NLS-1$
							dynamicLoadDepths, currentLevel + 1);
				}
			} else {
				for (int i = 0; i < topics.length; i++) {
					generateTopic(topics[i], out, id + "_" + i, //$NON-NLS-1$
							maxLevels - 1, currentLevel + 1);
				}
			}

			out.write("</ul>\n"); //$NON-NLS-1$
		} else {
			out.write("<li>"); //$NON-NLS-1$
			out.write("<img src='"); //$NON-NLS-1$
			out.write(imagesDirectory);
			out.write("/plus.gif' class='h' alt=\"\">"); //$NON-NLS-1$
			out.write("<a href=" +"\""+ UrlUtil.getHelpURL(topic.getHref()) + "\""+">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
			out.write("<img src='"); //$NON-NLS-1$
			out.write(imagesDirectory);
			out.write("/topic.gif' alt=\"\">"); //$NON-NLS-1$
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a>"); //$NON-NLS-1$
		}

		out.write("</li>\n"); //$NON-NLS-1$
	}

	/**
	 * Generates the HTML code (a tree) for a TOC.
	 * 
	 * @param toc
	 * @param out
	 * @throws IOException
	 */
	public void generateBasicToc(int toc, Writer out) throws IOException {
		ITopic[] topics = getEnabledSubtopics(tocs[toc]);
		for (int i = 0; i < topics.length; i++) {
			generateBasicTopic(topics[i], out);
		}

	}

	private void generateBasicTopic(ITopic topic, Writer out)
			throws IOException {

		out.write("<li>"); //$NON-NLS-1$
		ITopic[] topics = getEnabledSubtopics(topic);
		boolean hasNodes = topics.length > 0;
		if (hasNodes) {
			out.write("<nobr>"); //$NON-NLS-1$
			out.write("<a "); //$NON-NLS-1$
			if (getSelectedTopicHelpHref().equals(topic.getHref())) {
				out.write("name=\"selectedItem\" "); //$NON-NLS-1$
			}
			out.write("href="+"\"" + UrlUtil.getHelpURL(topic.getHref())+"\"" + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
			out.write("<img src='"); //$NON-NLS-1$
			out.write(imagesDirectory);
			out.write("/container_obj.gif' alt=\"\" border=0>&nbsp;"); //$NON-NLS-1$
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a>"); //$NON-NLS-1$
			out.write("</nobr>"); //$NON-NLS-1$

			out.write("<ul>\n"); //$NON-NLS-1$

			for (int i = 0; i < topics.length; i++) {
				generateBasicTopic(topics[i], out);
			}

			out.write("</ul>\n"); //$NON-NLS-1$
		} else {
			out.write("<nobr>"); //$NON-NLS-1$
			out.write("<a "); //$NON-NLS-1$
			if (getSelectedTopicHelpHref().equals(topic.getHref())) {
				out.write("name=\"selectedItem\" "); //$NON-NLS-1$
			}
			out.write("href="+"\"" + UrlUtil.getHelpURL(topic.getHref()) +"\""+ ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
			out.write("<img src='"); //$NON-NLS-1$
			out.write(imagesDirectory);
			out.write("/topic.gif' alt=\"\" border=0>&nbsp;"); //$NON-NLS-1$
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a>"); //$NON-NLS-1$
			out.write("</nobr>"); //$NON-NLS-1$
		}

		out.write("</li>\n"); //$NON-NLS-1$
	}
	/**
	 * @return String - help form of selected topic URL, or ""
	 */
	private String getSelectedTopicHelpHref() {
		if (topicHelpHref == null) {
			String topic = getSelectedTopic();
			if (topic == null || topic.length() == 0) {
				topicHelpHref = ""; //$NON-NLS-1$
				return topicHelpHref;
			}
			int index = topic.indexOf("/topic/"); //$NON-NLS-1$
			if (index != -1)
				topic = topic.substring(index + 6);
			index = topic.indexOf('?');
			if (index != -1)
				topic = topic.substring(0, index);
			topicHelpHref = topic;
			if (topic == null) {
				topicHelpHref = ""; //$NON-NLS-1$
			}
		}
		return topicHelpHref;
	}
	/**
	 * Obtains children topics for a given navigation element. Topics from TOCs
	 * not matching enabled activities are filtered out.
	 * 
	 * @param element ITopic or IToc
	 * @return ITopic[]
	 */
	private ITopic[] getEnabledSubtopics(Object element) {
		List topics = getEnabledSubtopicList(element);
		return (ITopic[])topics.toArray(new ITopic[topics.size()]);
	}
	/**
	 * Obtains children topics for a given navigation element. Topics from TOCs
	 * not matching enabled activities are filtered out.
	 * 
	 * @param navigationElement
	 * @return List of ITopic
	 */
	private List getEnabledSubtopicList(Object element) {
		if (element instanceof IToc && !isEnabled((IToc) element))
			return Collections.EMPTY_LIST;
		List children;
		if (element instanceof IToc) {
			children = Arrays.asList(((IToc)element).getTopics());
		}
		else if (element instanceof ITopic) {
			children = Arrays.asList(((ITopic)element).getSubtopics());
		}
		else {
			// unknown element type
			return Collections.EMPTY_LIST;
		}
		List childTopics = new ArrayList(children.size());
		for (Iterator childrenIt = children.iterator(); childrenIt.hasNext();) {
			Object c = childrenIt.next();
			if ((c instanceof ITopic)) {
				// add topic only if it will not end up being an empty
				// container
				if (((((ITopic) c).getHref() != null && ((ITopic) c)
						.getHref().length() > 0) || getEnabledSubtopicList(c).size() > 0) &&
						!UAContentFilter.isFiltered(c)) {
					childTopics.add(c);
				}
			} else {
				// it is a Toc, Anchor or Link,
				// which may have children attached to it.
				childTopics.addAll(getEnabledSubtopicList(c));
			}
		}
		return childTopics;
	}
	private void generateTopicLinks(ITopic topic, Writer w, int indent) {
        String topicHref = topic.getHref();
        try {
            if (indent == 0)
                w.write("<b>"); //$NON-NLS-1$
            for (int tab = 0; tab < indent; tab++) {
                w.write("&nbsp;&nbsp;"); //$NON-NLS-1$
            }
            if (topicHref != null && topicHref.length() > 0) {
                w.write("<a href=\""); //$NON-NLS-1$
                if ('/' == topicHref.charAt(0)) {
                    w.write("topic"); //$NON-NLS-1$
                }
                w.write(topicHref);
                w.write("\">"); //$NON-NLS-1$
                w.write(UrlUtil.htmlEncode(topic.getLabel()));
                w.write("</a>"); //$NON-NLS-1$
            } else {
                w.write(UrlUtil.htmlEncode(topic.getLabel()));
            }
            w.write("<br>\n"); //$NON-NLS-1$
            if (indent == 0)
                w.write("</b>"); //$NON-NLS-1$
        } catch (IOException ioe) {
        }
        ITopic[] topics = topic.getSubtopics();
        for (int i = 0; i < topics.length; i++) {
            generateTopicLinks(topics[i], w, indent + 1);
        }
    }

    public void generateLinks(Writer out) {
        for (int i = 0; i < tocs.length; i++) {
            IToc toc = tocs[i];
            ITopic tocTopic = toc.getTopic(null);
            generateTopicLinks(tocTopic, out, 0);
            ITopic[] topics = toc.getTopics();
            for (int t = 0; t < topics.length; t++) {
                generateTopicLinks(topics[t], out, 1);
            }
        }

    }
}
