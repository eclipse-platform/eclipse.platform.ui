/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.webapp.WebappResources;
import org.eclipse.help.internal.webapp.data.EnabledTopicUtils;
import org.eclipse.help.internal.webapp.data.TocData;
import org.eclipse.help.internal.webapp.data.UrlUtil;

/*
 * Creates xml representing selected parts of one or more TOCs  depending on the parameters
 * With no parameters the head of each toc is included
 * With parameter "href" the node and all its ancestors and siblings is included, corresponds to show in toc 
 * With parameter "toc" and optionally "path" the node, its ancestors and children are included
 */
public class TocFragmentServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static Map locale2Response = new WeakHashMap();

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
	    resp.setHeader("Cache-Control","no-cache");   //$NON-NLS-1$//$NON-NLS-2$
	    resp.setHeader("Pragma","no-cache");  //$NON-NLS-1$ //$NON-NLS-2$
	    resp.setDateHeader ("Expires", 0); 	 //$NON-NLS-1$
		TocData data = new TocData(this.getServletContext(), req, resp);	
		Serializer serializer = new Serializer(data, req.getLocale());
		String response = serializer.generateTreeXml();	
		locale2Response.put(locale, response);
		resp.getWriter().write(response);
	}
	
	/*
	 * Class which creates the xml file based upon the request parameters
	 */
	private class Serializer {
		
		private TocData tocData;
		private StringBuffer buf;
		private int requestKind;
		private Locale locale;
		private static final int REQUEST_SHOW_IN_TOC = 1;      // Show an element based on its href
		private static final int REQUEST_SHOW_TOCS = 2;        // Show all the tocs but not their children
		private static final int REQUEST_SHOW_CHILDREN = 3;    // Show the children of a node

		public Serializer(TocData data, Locale locale) {
			tocData = data;
			buf = new StringBuffer();
			this.locale = locale;
			if (tocData.getTopicHref() != null) {
				requestKind = REQUEST_SHOW_IN_TOC;
			} else if (tocData.getSelectedToc() == -1) {
				requestKind = REQUEST_SHOW_TOCS;
			} else {
				requestKind = REQUEST_SHOW_CHILDREN;
			}
		}
			
		public String generateTreeXml() {
			buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
			buf.append("<tree_data>\n"); //$NON-NLS-1$
			

		    if (tocData.isRemoteHelpError()) {
		    	addError(WebappResources.getString("remoteHelpErrorMessage", locale)); //$NON-NLS-1$			
		    }
					
			// Return an error for show in toc if topic was not found in toc
			if (requestKind == REQUEST_SHOW_IN_TOC && tocData.getTopicPath() == null) {
				addError(WebappResources.getString("CannotSync", locale)); //$NON-NLS-1$
			} else {
			    serializeTocs();
			}
			buf.append("</tree_data>\n"); //$NON-NLS-1$
			return buf.toString();
		}

		private void addError(String message) {
			buf.append("<error>"); //$NON-NLS-1$
			buf.append(XMLGenerator.xmlEscape(message));
			buf.append("</error>"); //$NON-NLS-1$			
		}

		private void serializeTocs() {
			ITopic[] topicPath = tocData.getTopicPath();
	
			int selectedToc = tocData.getSelectedToc();
			// Iterate over all tocs - if there is a selected toc only generate that
			// toc, otherwise generate the root of every toc.
			for (int toc=0; toc< tocData.getTocCount(); toc++) {
				boolean shouldLoad = requestKind == REQUEST_SHOW_TOCS || toc == selectedToc;
				if(!tocData.isEnabled(toc)){
					shouldLoad = false;
				} 
	            if (shouldLoad) {
	            	boolean isSelected = false; // Should this node be selected in the tree
	            	if (requestKind == REQUEST_SHOW_TOCS) {
	            		isSelected = toc == 0;
	            	} else if (requestKind == REQUEST_SHOW_CHILDREN) {
	            		isSelected = tocData.getRootPath() == null;
	            	}
					serializeToc(tocData.getTocs()[toc], toc, topicPath, isSelected);
				}
			}
		}
	
		private void serializeToc(IToc toc, int tocIndex, ITopic[] topicPath, boolean isSelected) {
			ITopic[] topics = EnabledTopicUtils.getEnabled(toc.getTopics());
			if (topics.length <= 0) {
				// do not generate toc when there are no leaf topics
				return;
			}
			
			if (requestKind == REQUEST_SHOW_CHILDREN) {
				topicPath = getTopicPathFromRootPath(toc);
			}
			
			buf.append("<node"); //$NON-NLS-1$
			if (toc.getLabel() != null) { 
				buf.append('\n' + "      title=\"" + XMLGenerator.xmlEscape(toc.getLabel()) + '"'); //$NON-NLS-1$
			}
			buf.append('\n' + "      id=\"" + XMLGenerator.xmlEscape(toc.getHref()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$

			String href = toc.getTopic(null).getHref();
			if (href == null) {
				href = "/../nav/" + tocIndex; //$NON-NLS-1$
			}
			buf.append('\n' + "      href=\"" + XMLGenerator.xmlEscape(UrlUtil.getHelpURL(href)) + "\""); //$NON-NLS-1$ //$NON-NLS-2$

			buf.append('\n' + "      image=\"toc_closed\""); //$NON-NLS-1$
				
			boolean serializeChildren = true;
			if (requestKind == REQUEST_SHOW_TOCS) {
				serializeChildren = false;
			}
			if (requestKind == REQUEST_SHOW_IN_TOC && topicPath.length == 0) {
				serializeChildren = false;
				buf.append('\n' + "      is_selected=\"true\"" ); //$NON-NLS-1$
				buf.append('\n' + "      is_highlighted=\"true\"" ); //$NON-NLS-1$	
			}
			buf.append(">\n"); //$NON-NLS-1$
			if (serializeChildren) { 
				serializeChildTopics(topics, topicPath, "", isSelected); //$NON-NLS-1$
			}
			buf.append("</node>\n"); //$NON-NLS-1$
			
		}
		
		private ITopic[] getTopicPathFromRootPath(IToc toc) {
			ITopic[] topicPath;
			// Determine the topicPath from the path passed in as a parameter
			int[] rootPath = tocData.getRootPath();
			if (rootPath == null) {
				return null;
			}
			int pathLength = rootPath.length;
			topicPath = new ITopic[pathLength];
			ITopic[] children = EnabledTopicUtils.getEnabled(toc.getTopics());
			for (int i = 0; i < pathLength; i++) {
				int index = rootPath[i];
				if (index < children.length) {
					topicPath[i] = children[index];
					children = EnabledTopicUtils.getEnabled(topicPath[i].getSubtopics());
				} else {
					return null;  // Mismatch between expected and actual children
				}
			}
			return topicPath;
		}
	
		private void serializeTopic(ITopic topic, ITopic[] topicPath, boolean isSelected, String parentPath)  {
		    ITopic[] subtopics = EnabledTopicUtils.getEnabled(topic.getSubtopics());
		    buf.append("<node"); //$NON-NLS-1$
			if (topic.getLabel() != null) { 
				buf.append('\n'	+ "      title=\"" + XMLGenerator.xmlEscape(topic.getLabel()) + '"'); //$NON-NLS-1$
			}
	
			buf.append('\n' + "      id=\"" + parentPath + "\""); //$NON-NLS-1$ //$NON-NLS-2$

			String href = topic.getHref();
			if (href == null) {
				href = "/../nav/" + tocData.getSelectedToc() + '_' + parentPath; //$NON-NLS-1$
			}
			buf.append('\n' + "      href=\"" + XMLGenerator.xmlEscape( //$NON-NLS-1$
					UrlUtil.getHelpURL(href)) + '"');
			if (subtopics.length == 0 ) {
				buf.append('\n' + "      is_leaf=\"true\"" ); //$NON-NLS-1$
			}
			if (isSelected && requestKind == REQUEST_SHOW_IN_TOC) {
				buf.append('\n' + "      is_selected=\"true\"" ); //$NON-NLS-1$
				buf.append('\n' + "      is_highlighted=\"true\"" ); //$NON-NLS-1$	
			}
			String icon; 
			if (subtopics.length == 0) {
				icon = "topic"; //$NON-NLS-1$
			} else if (topic.getHref() == null) {
				icon = "container_obj"; //$NON-NLS-1$
			} else {
				icon = "container_topic"; //$NON-NLS-1$
			}
			buf.append('\n' + "      image=\"" + icon + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			
			buf.append(">\n"); //$NON-NLS-1$
			serializeChildTopics(subtopics, topicPath, parentPath, isSelected);
			buf.append("</node>\n"); //$NON-NLS-1$	
		}
	
		private void serializeChildTopics(ITopic[] childTopics, ITopic[] topicPath, String parentPath, boolean parentIsSelected) {
			if (parentIsSelected && requestKind == REQUEST_SHOW_CHILDREN) {
				// Show the children of this node
				for (int subtopic = 0; subtopic < childTopics.length; subtopic++) {
				    serializeTopic(childTopics[subtopic], null, false, addSuffix(parentPath, subtopic));
				}
			} else if (topicPath != null) {
				for (int subtopic = 0; subtopic < childTopics.length; subtopic++) {
					if (topicPath[0].getLabel().equals(childTopics[subtopic].getLabel())) {
						ITopic[] newPath = null;
						if (topicPath.length > 1) {
							newPath = new ITopic[topicPath.length - 1];
							System.arraycopy(topicPath, 1, newPath, 0, topicPath.length - 1);
						}
				        serializeTopic(childTopics[subtopic], newPath, topicPath.length == 1, addSuffix(parentPath, subtopic));
					} else {
						serializeTopic(childTopics[subtopic], null, false, addSuffix(parentPath, subtopic));
					}
				}
			} 
		}

		private String addSuffix(String parentPath, int subtopic) {
			if (parentPath.length() == 0) {
				return parentPath + subtopic;
			} 
			return parentPath + '_' + subtopic;
		}
	}

}
