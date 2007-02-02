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
package org.eclipse.help.internal.toc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.help.ITocContribution;
import org.eclipse.help.IUAElement;
import org.eclipse.help.internal.Anchor;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.Topic;
import org.eclipse.help.internal.UAElement;
import org.eclipse.help.internal.dynamic.DocumentProcessor;
import org.eclipse.help.internal.dynamic.DocumentReader;
import org.eclipse.help.internal.dynamic.ExtensionHandler;
import org.eclipse.help.internal.dynamic.IncludeHandler;
import org.eclipse.help.internal.dynamic.ProcessorHandler;
import org.eclipse.help.internal.dynamic.ValidationHandler;

/*
 * Assembles toc contributions (toc fragments) into complete, linked, and
 * assembled books.
 */
public class TocAssembler {

	private DocumentProcessor processor;
	private ProcessorHandler[] handlers;
	
	private List contributions;
	private Map contributionsById;
	private Map contributionsByLinkTo;
	private Set processedContributions;
	private Map requiredAttributes;
	
	/*
	 * Assembles the given toc contributions into complete, linked
	 * books. The originals are not modified.
	 */
	public List assemble(List contributions) {
		this.contributions = contributions;
		contributionsById = null;
		contributionsByLinkTo = null;
		processedContributions = null;
		
		List books = getBooks();
		Iterator iter = books.iterator();
		while (iter.hasNext()) {
			TocContribution book = (TocContribution)iter.next();
			process(book);
		}
		return books;
	}
	
	/*
	 * Returns the list of contributions that should appear as root TOCs
	 * (books). Contributions are books if:
	 * 
	 * 1. isPrimary() returns true.
	 * 2. The toc has no "link_to" attribute defined (does not link into
	 *    another toc).
	 * 3. No other toc has a link to this contribution (via "link" element).
	 */
	private List getBooks() {
		Set linkedContributionIds = getLinkedContributionIds(contributions);
		List books = new ArrayList();
		Iterator iter = contributions.iterator();
		while (iter.hasNext()) {
			TocContribution contrib = (TocContribution)iter.next();
			if (contrib.isPrimary() && contrib.getToc().getLinkTo() == null && !linkedContributionIds.contains(contrib.getId())) {
				books.add(contrib);
			}
		}
		return books;
	}
	
	/*
	 * Returns the set of ids of contributions that are linked to by other
	 * contributions, i.e. at least one other contribution has a link element
	 * pointing to it.
	 */
	private Set getLinkedContributionIds(List contributions) {
		if (processor == null) {
			processor = new DocumentProcessor();
		}
		final Set linkedContributionIds = new HashSet();
		ProcessorHandler[] linkFinder = new ProcessorHandler[] {
			new ValidationHandler(getRequiredAttributes()),
			new ProcessorHandler() {
				public short handle(UAElement element, String id) {
					if (element instanceof Link) {
						Link link = (Link)element;
						String toc = link.getToc();
						if (toc != null) {
							TocContribution srcContribution = getContribution(id);
							linkedContributionIds.add(HrefUtil.normalizeHref(srcContribution.getContributorId(), toc));
						}
					}
					return UNHANDLED;
				}
			}
		};
		processor.setHandlers(linkFinder);
		ListIterator iter = contributions.listIterator();
		while (iter.hasNext()) {
			TocContribution contrib = (TocContribution)iter.next();
			try {
				processor.process((Toc)contrib.getToc(), contrib.getId());
			}
			catch (Throwable t) {
				iter.remove();
				String msg = "Error processing help table of contents: " + contrib.getId() + " (skipping)"; //$NON-NLS-1$ //$NON-NLS-2$
				HelpPlugin.logError(msg, t);
			}
		}
		return linkedContributionIds;
	}
	
	/*
	 * Processes the given contribution, if it hasn't been processed yet. This
	 * performs the following operations:
	 * 
	 * 1. Topic hrefs are normalized, e.g. "path/doc.html" ->
	 *    "/my.plugin/path/doc.html"
	 * 2. Links are resolved, link is replaced with target content, extra docs
	 *    are merged.
	 * 3. Anchor contributions are resolved, tocs with link_to's are inserted
	 *    at anchors and extra docs merged.
	 */
	private void process(ITocContribution contribution) {
		if (processedContributions == null) {
			processedContributions = new HashSet();
		}
		// don't process the same one twice
		if (!processedContributions.contains(contribution)) {
			if (processor == null) {
				processor = new DocumentProcessor();
			}
			if (handlers == null) {
				DocumentReader reader = new DocumentReader();
				handlers = new ProcessorHandler[] {
					new NormalizeHandler(),
					new LinkHandler(),
					new AnchorHandler(),
					new IncludeHandler(reader, contribution.getLocale()),
					new ExtensionHandler(reader, contribution.getLocale()),
				};
			}
			processor.setHandlers(handlers);
			processor.process((Toc)contribution.getToc(), contribution.getId());
			processedContributions.add(contribution);
		}
	}
	
	/*
	 * Returns the contribution with the given id.
	 */
	private TocContribution getContribution(String id) {
		if (contributionsById == null) {
			contributionsById = new HashMap();
			Iterator iter = contributions.iterator();
			while (iter.hasNext()) {
				TocContribution contribution = (TocContribution)iter.next();
				contributionsById.put(contribution.getId(), contribution);
			}
		}
		return (TocContribution)contributionsById.get(id);
	}
	
	/*
	 * Returns all contributions that define a link_to attribute pointing to
	 * the given anchor path. The path has the form "<contributionId>#<anchorId>",
	 * e.g. "/my.plugin/toc.xml#myAnchor".
	 */
	private TocContribution[] getAnchorContributions(String anchorPath) {
		if (contributionsByLinkTo == null) {
			contributionsByLinkTo = new HashMap();
			Iterator iter = contributions.iterator();
			while (iter.hasNext()) {
				TocContribution srcContribution = (TocContribution)iter.next();
				String linkTo = srcContribution.getToc().getLinkTo();
				if (linkTo != null) {
					String destAnchorPath = HrefUtil.normalizeHref(srcContribution.getContributorId(), linkTo);
					ITocContribution[] array = (ITocContribution[])contributionsByLinkTo.get(destAnchorPath);
					if (array == null) {
						array = new TocContribution[] { srcContribution };
					}
					else {
						TocContribution[] temp = new TocContribution[array.length + 1];
						System.arraycopy(array, 0, temp, 0, array.length);
						temp[array.length] = srcContribution;
						array = temp;
					}
					contributionsByLinkTo.put(destAnchorPath, array);
				}
			}
		}
		TocContribution[] contributions = (TocContribution[])contributionsByLinkTo.get(anchorPath);
		if (contributions == null) {
			contributions = new TocContribution[0];
		}
		return contributions;
	}
	
	private Map getRequiredAttributes() {
		if (requiredAttributes == null) {
			requiredAttributes = new HashMap();
			requiredAttributes.put(Toc.NAME, new String[] { Toc.ATTRIBUTE_LABEL });
			requiredAttributes.put(Topic.NAME, new String[] { Topic.ATTRIBUTE_LABEL });
			requiredAttributes.put("anchor", new String[] { "id" }); //$NON-NLS-1$ //$NON-NLS-2$
			requiredAttributes.put("include", new String[] { "path" }); //$NON-NLS-1$ //$NON-NLS-2$
			requiredAttributes.put("link", new String[] { "toc" }); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return requiredAttributes;
	}
	
	/*
	 * Adds the given extra documents to the contribution.
	 */
	private void addExtraDocuments(TocContribution contribution, String[] extraDocuments) {
		if (extraDocuments.length > 0) {
			String[] destExtraDocuments = contribution.getExtraDocuments();
			String[] combinedExtraDocuments;
			if (destExtraDocuments.length == 0) {
				combinedExtraDocuments = extraDocuments;
			}
			else {
				Set set = new HashSet();
				set.addAll(Arrays.asList(destExtraDocuments));
				set.addAll(Arrays.asList(extraDocuments));
				combinedExtraDocuments = (String[])set.toArray(new String[set.size()]);
			}
			contribution.setExtraDocuments(combinedExtraDocuments);
		}
	}
	
	/*
	 * Handler that resolves link elements (replaces the link element with
	 * the linked-to toc's children.
	 */
	private class LinkHandler extends ProcessorHandler {
		public short handle(UAElement element, String id) {
			if (element instanceof Link) {
				Link link = (Link)element;
				UAElement parent = link.getParentElement();
				if (parent != null) {
					String toc = link.getToc();
					if (toc != null) {
						TocContribution destContribution = getContribution(id);
						TocContribution srcContribution = getContribution(HrefUtil.normalizeHref(destContribution.getContributorId(), toc));
						if (srcContribution != null) {
							process(srcContribution);
							IUAElement[] children = srcContribution.getToc().getChildren();
							for (int i=0;i<children.length;++i) {
								parent.insertBefore((UAElement)children[i], link);
							}
							addExtraDocuments(destContribution, srcContribution.getExtraDocuments());
						}
						parent.removeChild(link);
					}
				}
				return HANDLED_SKIP;
			}
			return UNHANDLED;
		}
	}

	/*
	 * Handles anchor contributions. If any contribution's toc wants to link
	 * into this one at the current anchor, link it in.
	 */
	private class AnchorHandler extends ProcessorHandler {
		public short handle(UAElement element, String id) {
			if (element instanceof Anchor) {
				Anchor anchor = (Anchor)element;
				UAElement parent = anchor.getParentElement();
				if (parent != null) {
					String anchorId = anchor.getId();
					if (anchorId != null) {
						TocContribution destContribution = getContribution(id);
						if (destContribution != null) {
							TocContribution[] srcContributions = getAnchorContributions(destContribution.getId() + '#' +  anchorId);
							for (int i=0;i<srcContributions.length;++i) {
								process(srcContributions[i]);
								IUAElement[] children = srcContributions[i].getToc().getChildren();
								for (int j=0;j<children.length;++j) {
									parent.insertBefore((UAElement)children[j], anchor);
								}
								addExtraDocuments(destContribution, srcContributions[i].getExtraDocuments());
							}
						}
					}
				}
			}
			// allow the extension handler to act on anchors afterwards
			return UNHANDLED;
		}
	}

	/*
	 * Normalizes topic hrefs, by prepending the plug-in id to form an href.
	 * e.g. "path/myfile.html" -> "/my.plugin/path/myfile.html"
	 */
	private class NormalizeHandler extends ProcessorHandler {
		public short handle(UAElement element, String id) {
			if (element instanceof Topic) {
				Topic topic = (Topic)element;
				String href = topic.getHref();
				if (href != null) {
					topic.setHref(normalize(href, id));
				}
				return HANDLED_CONTINUE;
			}
			else if (element instanceof Toc) {
				Toc toc = (Toc)element;
				toc.setHref(id);
				String topic = toc.getTopic();
				if (topic != null) {
					toc.setTopic(normalize(topic, id));
				}
				return HANDLED_CONTINUE;
			}
			return UNHANDLED;
		}
		
		private String normalize(String href, String id) {
			ITocContribution contribution = getContribution(id);
			if (contribution != null) {
				String pluginId = contribution.getContributorId();
				return HrefUtil.normalizeHref(pluginId, href);
			}
			else {
				int index = id.indexOf('/', 1);
				if (index != -1) {
					String pluginId = id.substring(1, index);
					return HrefUtil.normalizeHref(pluginId, href);
				}
			}
			return href;
		}
	}
}
