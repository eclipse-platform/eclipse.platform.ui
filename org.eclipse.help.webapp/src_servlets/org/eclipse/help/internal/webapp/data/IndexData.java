/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.index.IIndex;
import org.eclipse.help.internal.index.IIndexEntry;
import org.eclipse.help.internal.index.IIndexTopic;

/**
 * Helper class for Index view initialization
 */
public class IndexData extends ActivitiesData {
	private IIndex index;

	// images directory
	private String imagesDirectory;

	// plus/minus image file name
	private String plusMinusImage;

	// name of expand/collapse class for IMG, UL tags
	private String expandedCollapsed;

	// use or not expand/collapse feature
	private boolean usePlusMinus;

	// expand all by default flag
	private boolean expandAll;

	// global writer for private generate...() methods
	private Writer out;

	/**
	 * Constructs the data for the index page.
	 * @param context
	 * @param request
	 */
	public IndexData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);

		imagesDirectory = preferences.getImagesDirectory();
		usePlusMinus = preferences.isIndexPlusMinus();
		expandAll = preferences.isIndexExpandAll();
		plusMinusImage = expandAll ? "/minus.gif" : "/plus.gif"; //$NON-NLS-1$ //$NON-NLS-2$
		expandedCollapsed = expandAll ? "expanded" : "collapsed"; //$NON-NLS-1$ //$NON-NLS-2$

		loadIndex();
	}

	/**
	 * Loads help index
	 */
	private void loadIndex() {
		index = HelpPlugin.getIndexManager().getIndex(Platform.getNL());
	}

	/*
	 * TO DO:
	 * method to be removed
	 */
	public IIndexEntry getIndexEntry(String [] path) {
		Map entries = index.getEntries();
		IIndexEntry result = null;
		for(int i = 0; i < path.length; i++) {
			result = (IIndexEntry) entries.get(path[i]);
			if(result == null)
				return null;
			else
				entries = result.getEntries();
		}
		return result;
	}

	/**
	 * Generates values for array of ids of list items
	 * avaliable to be navigated through typein feature.
	 *
	 * Currently only first level items can be navigated.
	 *
	 * @param out
	 * @throws IOException
	 */
	public void generateIds(Writer out) throws IOException {
		Iterator iter = index.getEntries().values().iterator();
		while (iter.hasNext()) {
			IIndexEntry entry = (IIndexEntry)iter.next();
			out.write("\""); //$NON-NLS-1$
			out.write(entry.getKeyword());
			out.write("\""); //$NON-NLS-1$
			if (iter.hasNext()) {
				out.write(",\n"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Generates the HTML code (a list) for the index.
	 *
	 * @param out
	 * @throws IOException
	 */
	public void generateIndex(Writer out) throws IOException {
		this.out = out;

		Iterator iter = index.getEntries().values().iterator();
		while(iter.hasNext()) {
			IIndexEntry entry = (IIndexEntry) iter.next();
			generateEntry(entry, 0);
		}
	}

	/**
	 * Generates the HTML code for an index entry.
	 *
	 * @param entry
	 * @param level
	 * @throws IOException
	 */
	/*
	 * <li>[ plus_image ]<a ...>...</a>
	 * [<ul>list of topics</ul>]
	 * [<ul>nested entries</ul>]
	 * </li>
	 */
	private void generateEntry(IIndexEntry entry, int level) throws IOException {
		List topics = entry.getTopics();
		int topicCount = topics.size();
		boolean multipleTopics = topicCount > 1;
		boolean singleTopic = topicCount == 1;

		out.write("<li>"); //$NON-NLS-1$
		if (usePlusMinus) generatePlusImage(multipleTopics);
		generateAnchor(singleTopic, entry, level);
		if (multipleTopics) generateTopicList(entry);
		generateSubEntries(entry, level + 1);
		out.write("</li>\n"); //$NON-NLS-1$
	}

	/**
	 * Generates the HTML code for the plus/minus image.
	 *
	 * @param multipleTopics
	 * @throws IOException
	 */
	/*
	 * <img scr="images/plus.gif" class={ "collapsed" | "expanded" | "h" } alt="...">
	 */
	private void generatePlusImage(boolean multipleTopics) throws IOException {
		out.write("<img src=\""); //$NON-NLS-1$
		out.write(imagesDirectory);
		out.write(plusMinusImage);
		out.write("\" class=\""); //$NON-NLS-1$
		if (multipleTopics) {
			out.write(expandedCollapsed);
		} else {
			out.write("h"); //$NON-NLS-1$
		}
		out.write("\" alt=\""); //$NON-NLS-1$
		if (multipleTopics) {
			if (expandAll) {
				out.write(ServletResources.getString("collapseTopicTitles", request)); //$NON-NLS-1$
			} else {
				out.write(ServletResources.getString("expandTopicTitles", request)); //$NON-NLS-1$
			}
		}
		out.write("\">"); //$NON-NLS-1$
	}

	/**
	 * Generates the HTML code for an index entry anchor tag.
	 *
	 * @param singleTopic
	 * @param entry
	 * @param level
	 * @throws IOException
	 */
	/*
	 * <a id="..." [ class="nolink" ] href="...">
	 */
	private void generateAnchor(boolean singleTopic, IIndexEntry entry, int level) throws IOException {
		out.write("<a "); //$NON-NLS-1$
		if (level == 0) {
			out.write("id=\""); //$NON-NLS-1$
			out.write(entry.getKeyword());
			out.write("\" "); //$NON-NLS-1$
		}
		if (singleTopic) {
			out.write("href=\""); //$NON-NLS-1$
			out.write(UrlUtil.getHelpURL(((IIndexTopic)entry.getTopics().get(0)).getHref()));
			out.write("\">"); //$NON-NLS-1$
		} else {
			out.write("class=\"nolink\" href=\"about:blank\">"); //$NON-NLS-1$
		}
		out.write(UrlUtil.htmlEncode(entry.getKeyword()));
		out.write("</a>"); //$NON-NLS-1$
	}

	/**
	 * Generates the HTML code for a list of topics.
	 *
	 * @param entry
	 * @throws IOException
	 */
	/*
	 * <ul class={"collapsed" | "expanded"}>
	 * <li><img class="h" src="images/plus.gif" alt=""><a href="..."><img src="images/topic.gif">...</a></li>
	 * <li>...
	 * </ul>
	 */
	private void generateTopicList(IIndexEntry entry) throws IOException {
		List topics = entry.getTopics();
		int size = topics.size();

		out.write("\n<ul class=\""); //$NON-NLS-1$
		out.write(expandedCollapsed);
		out.write("\">\n"); //$NON-NLS-1$
		for (int i = 0; i < size; ++i) {
			IIndexTopic topic = (IIndexTopic)topics.get(i); 

			out.write("<li>"); //$NON-NLS-1$
			if (usePlusMinus) {
				out.write("<img class=\"h\" src=\""); //$NON-NLS-1$
				out.write(imagesDirectory);
				out.write(plusMinusImage);
				out.write("\" alt=\"\">"); //$NON-NLS-1$
			}
			out.write("<a href=\""); //$NON-NLS-1$
			out.write(UrlUtil.getHelpURL(topic.getHref())); 
			out.write("\"><img src=\""); //$NON-NLS-1$
			out.write(imagesDirectory);
			out.write("/topic.gif\" alt=\"\">"); //$NON-NLS-1$
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</a></li>\n"); //$NON-NLS-1$
		}
		out.write("</ul>\n"); //$NON-NLS-1$
	}

	/**
	 * Generates the HTML for nested index entries.
	 *
	 * @param entry
	 * @param level
	 * @throws IOException
	 */
	/*
	 * <ul class="expanded">
	 * entries...
	 * </ul>
	 */
	private void generateSubEntries(IIndex entry, int level) throws IOException {
		Iterator iter = entry.getEntries().values().iterator();
		if (iter.hasNext()) {
			out.write("<ul class=\"expanded\">\n"); //$NON-NLS-1$
			do {
				IIndexEntry childEntry = (IIndexEntry)iter.next();
				generateEntry(childEntry, level);
			} while (iter.hasNext());
			out.write("</ul>\n"); //$NON-NLS-1$
		}
	}
}
