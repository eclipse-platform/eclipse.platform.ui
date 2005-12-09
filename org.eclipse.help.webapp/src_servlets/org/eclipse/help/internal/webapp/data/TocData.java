/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;
import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.model.*;
import org.eclipse.help.internal.toc.*;

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
	private ITocElement[] tocs;
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

	// Accessor methods to avoid exposing help classes directly to JSP.
	// Note: this seems ok for now, but maybe we need to reconsider this
	//       and allow help classes in JSP's.

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
	 * @return Element[]
	 */
	public ITocElement[] getTocs() {
		return tocs;
	}

	/**
	 * Check if given TOC is visible (belongs to an enabled activity)
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
	 * Check if given TOC is visible (belongs to an enabled activity)
	 * 
	 * @param toc
	 * @return true if TOC should be visible
	 */
	private boolean isEnabled(ITocElement toc) {
		if(!isAdvancedUI()){
			// activities never filtered for basic browsers
			return true;
		}
		return HelpBasePlugin.getActivitySupport().isEnabled(toc.getHref());
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
			if (topic != null
					&& topic instanceof org.eclipse.help.internal.toc.Topic) {
				topicPath = ((org.eclipse.help.internal.toc.Topic) topic)
						.getPathInToc(tocs[selectedToc]);
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
		ITopicElement[] topics = getEnabledSubtopics(tocs[toc]);
		if (topics.length <= 0) {
			// do not generate toc when there are no leaf topics
			return;
		}

		int maxLevels = dynamicLoadDepths;
		if (tocs[toc] instanceof Toc
				&& ((Toc) tocs[toc]).size() <= loadBookAtOnceLimit) {
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
	private void generateTopic(ITopicElement topic, Writer out, String id,
			int maxLevels, int currentLevel) throws IOException {
		if (maxLevels == 0) {
			return;
		}

		topicsGenerated++;
		if (maxLevels > 1 && topicsGenerated > honorLevelsLimit) {
			maxLevels = 1;
		}

		ITopicElement[] topics = getEnabledSubtopics(topic);
		boolean hasNodes = topics.length > 0;

		if (hasNodes) {
			out.write("<li>"); //$NON-NLS-1$
			out.write("<img src='"); //$NON-NLS-1$
			out.write(imagesDirectory);
			out
					.write("/plus.gif' class='collapsed' alt=\"" + ServletResources.getString("topicClosed", request) + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		ITopicElement[] topics = getEnabledSubtopics(tocs[toc]);
		for (int i = 0; i < topics.length; i++) {
			generateBasicTopic(topics[i], out);
		}

	}

	private void generateBasicTopic(ITopicElement topic, Writer out)
			throws IOException {

		out.write("<li>"); //$NON-NLS-1$
		ITopicElement[] topics = getEnabledSubtopics(topic);
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
	 * @param navigationElement
	 * @return ITopic[]
	 */
	private ITopicElement[] getEnabledSubtopics(
			INavigationElement navigationElement) {
		List topics = getEnabledSubtopicList(navigationElement);
		return (ITopicElement[]) topics
				.toArray(new ITopicElement[topics.size()]);
	}
	/**
	 * Obtains children topics for a given navigation element. Topics from TOCs
	 * not matching enabled activities are filtered out.
	 * 
	 * @param navigationElement
	 * @return List of ITopicElement
	 */
	private List getEnabledSubtopicList(INavigationElement navigationElement) {
		if (navigationElement instanceof ITocElement
				&& !isEnabled((ITocElement) navigationElement))
			return Collections.EMPTY_LIST;
		List children = navigationElement.getChildren();
		List childTopics = new ArrayList(children.size());
		for (Iterator childrenIt = children.iterator(); childrenIt.hasNext();) {
			INavigationElement c = (INavigationElement) childrenIt.next();
			if ((c instanceof ITopicElement)) {
				// add topic only if it will not end up being an empty
				// container
				if ((((ITopicElement) c).getHref() != null && ((ITopicElement) c)
						.getHref().length() > 0)
						|| getEnabledSubtopicList(c).size() > 0) {
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
