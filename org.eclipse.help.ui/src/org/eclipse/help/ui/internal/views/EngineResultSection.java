/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.search.ISearchEngineResult;
import org.eclipse.help.ui.internal.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.osgi.framework.Bundle;

public class EngineResultSection {
	private SearchResultsPart part;

	private EngineDescriptor desc;
	
	private IStatus errorStatus;

	private ArrayList hits;

	private Section section;

	private Composite container;

	private FormText searchResults;

	private ImageHyperlink prevLink;

	private ImageHyperlink nextLink;

	private boolean needsUpdating;

	private FederatedSearchSorter sorter;

	private int HITS_PER_PAGE = 10;

	private static final String HREF_PROGRESS = "__progress__"; //$NON-NLS-1$

	private static final String PROGRESS_VIEW = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$

	private int resultOffset = 0;

	public EngineResultSection(SearchResultsPart part, EngineDescriptor desc) {
		this.part = part;
		this.desc = desc;
		hits = new ArrayList();
		sorter = new FederatedSearchSorter();
	}

	public boolean hasControl(Control control) {
		return searchResults.equals(control);
	}

	public boolean matches(EngineDescriptor desc) {
		return this.desc == desc;
	}

	public Control createControl(Composite parent, final FormToolkit toolkit) {
		section = toolkit.createSection(parent, Section.COMPACT
				| Section.TWISTIE | Section.EXPANDED | Section.LEFT_TEXT_CLIENT_ALIGNMENT);
		// section.marginHeight = 10;
		container = toolkit.createComposite(section);
		//container.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);
		createFormText(container, toolkit);
		searchResults.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		//searchResults.setColor("summary", toolkit.getColors().getColor(FormColors.TITLE));
		section.setClient(container);
		updateSectionTitle();
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
				if (needsUpdating)
					asyncUpdateResults(true, false);
			}

			public void expansionStateChanged(ExpansionEvent e) {
			}
		});
		return section;
	}

	private void createFormText(Composite parent, FormToolkit toolkit) {
		searchResults = toolkit.createFormText(parent, false);
		searchResults.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		searchResults.marginHeight = 5;
		String topicKey = IHelpUIConstants.IMAGE_FILE_F1TOPIC;
		String nwKey = IHelpUIConstants.IMAGE_NW;
		String searchKey = IHelpUIConstants.IMAGE_HELP_SEARCH;
		searchResults.setImage(topicKey, HelpUIResources.getImage(topicKey));
		searchResults.setImage(nwKey, HelpUIResources.getImage(nwKey));
		searchResults.setImage(searchKey, HelpUIResources.getImage(searchKey));
		searchResults.setColor("summary", parent.getDisplay().getSystemColor( //$NON-NLS-1$
				SWT.COLOR_WIDGET_DARK_SHADOW));
		searchResults.setImage(ISharedImages.IMG_TOOL_FORWARD, PlatformUI
				.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_TOOL_FORWARD));
		searchResults.setImage(ISharedImages.IMG_TOOL_BACK, PlatformUI
				.getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_TOOL_BACK));
		searchResults.setImage(ISharedImages.IMG_OBJS_ERROR_TSK, PlatformUI.
				getWorkbench().getSharedImages().getImage(
						ISharedImages.IMG_OBJS_ERROR_TSK));
		searchResults.setImage(desc.getId(), desc.getIconImage());
		searchResults.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				Object href = e.getHref();
				if ("nw:".equals(href)) { //$NON-NLS-1$
					part.doOpenLink(e.getHref());
				} else if (HREF_PROGRESS.equals(href)) {
					showProgressView();
				} else
					part.doOpenLink(e.getHref());
			}

			public void linkEntered(HyperlinkEvent e) {
				part.parent.handleLinkEntered(e);
			}

			public void linkExited(HyperlinkEvent e) {
				part.parent.handleLinkExited(e);
			}
		});
		initializeText();
		needsUpdating = true;
	}

	private void initializeText() {
		Bundle bundle = Platform.getBundle("org.eclipse.ui.views"); //$NON-NLS-1$
		if (bundle != null) {
			StringBuffer buff = new StringBuffer();
			buff.append("<form>"); //$NON-NLS-1$
			buff.append("<p><a href=\""); //$NON-NLS-1$
			buff.append(HREF_PROGRESS);
			buff.append("\" alt=\""); //$NON-NLS-1$
			buff.append(HelpUIResources.getString("EngineResultSection.progressTooltip")); //$NON-NLS-1$
			buff.append("\">"); //$NON-NLS-1$
			buff.append(HelpUIResources.getString("EngineResultSection.searchInProgress")); //$NON-NLS-1$
			buff.append("</a></p></form>"); //$NON-NLS-1$
			searchResults.setText(buff.toString(), true, false);
		} else {
			searchResults.setText(HelpUIResources.getString("EngineResultSection.progress2"), false, false); //$NON-NLS-1$
		}
	}

	private void showProgressView() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					page.showView(PROGRESS_VIEW);
				} catch (PartInitException e) {
					HelpUIPlugin.logError(HelpUIResources.getString("EngineResultSection.progressError"), e); //$NON-NLS-1$
				}
			}
		}
	}

	public synchronized void add(ISearchEngineResult match) {
		hits.add(match);
		asyncUpdateResults(false, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.search.federated.ISearchEngineResultCollector#add(org.eclipse.help.internal.search.federated.ISearchEngineResult[])
	 */
	public synchronized void add(ISearchEngineResult[] matches) {
		for (int i = 0; i < matches.length; i++)
			hits.add(matches[i]);
		asyncUpdateResults(false, false);
	}

	public synchronized void error(IStatus status) {
		errorStatus = status;
		asyncUpdateResults(false, false);		
	}

	private void asyncUpdateResults(boolean now, final boolean scrollToBeginning) {
		Runnable runnable = new Runnable() {
			public void run() {
				BusyIndicator.showWhile(section.getDisplay(), new Runnable() {
					public void run() {
						updateResults(true);
						if (scrollToBeginning) {
							part.scrollToBeginning();
							searchResults.setFocus();
						}
					}
				});
			}
		};
		if (now)
			section.getDisplay().syncExec(runnable);
		else
			section.getDisplay().asyncExec(runnable);
	}

	void updateResults(boolean reflow) {
		updateSectionTitle();
		ISearchEngineResult[] results = (ISearchEngineResult[]) hits
				.toArray(new ISearchEngineResult[hits.size()]);
		if (part.getShowCategories())
			sorter.sort(null, results);
		StringBuffer buff = new StringBuffer();
		buff.append("<form>"); //$NON-NLS-1$
		IHelpResource oldCat = null;
		boolean earlyExit = false;

		for (int i = resultOffset; i < hits.size(); i++) {
			if (i - resultOffset == HITS_PER_PAGE) {
				break;
			}
			ISearchEngineResult hit = results[i];
			IHelpResource cat = hit.getCategory();
			if (part.getShowCategories()
					&& cat != null
					&& (oldCat == null || !oldCat.getLabel().equals(
							cat.getLabel()))) {
				buff.append("<p>"); //$NON-NLS-1$
				if (cat.getHref() != null) {
					buff.append("<a bold=\"true\" href=\""); //$NON-NLS-1$
					String absoluteHref = hit.toAbsoluteHref(cat.getHref(), true); 
					buff.append(escapeSpecialChars(absoluteHref));
					buff.append("\">"); //$NON-NLS-1$
					buff.append(cat.getLabel());
					buff.append("</a>"); //$NON-NLS-1$
				} else {
					buff.append("<b>"); //$NON-NLS-1$
					buff.append(cat.getLabel());
					buff.append("</b>"); //$NON-NLS-1$
				}
				buff.append("</p>"); //$NON-NLS-1$
				oldCat = cat;
			}
			int indent = part.getShowCategories() && cat != null ? 26 : 21;
			int bindent = part.getShowCategories() && cat != null ? 5 : 0;
			buff
					.append("<li indent=\"" + indent + "\" bindent=\"" + bindent + "\" style=\"image\" value=\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buff.append(desc.getId());
			buff.append("\">"); //$NON-NLS-1$
			buff.append("<a href=\""); //$NON-NLS-1$
			if (hit.getForceExternalWindow())
				buff.append("nw:"); //$NON-NLS-1$
			buff.append(escapeSpecialChars(hit.toAbsoluteHref(hit.getHref(), false)));
			buff.append("\""); //$NON-NLS-1$
			if (hit.getCategory() != null) {
				buff.append(" alt=\""); //$NON-NLS-1$
				buff.append(hit.getCategory().getLabel());
				buff.append("\""); //$NON-NLS-1$
			}
			buff.append(">"); //$NON-NLS-1$
			buff.append(hit.getLabel());
			buff.append("</a>"); //$NON-NLS-1$
			if (!hit.getForceExternalWindow()) {
				buff.append(" <a href=\""); //$NON-NLS-1$ 
				buff.append("nw:");//$NON-NLS-1$ 
				buff.append(escapeSpecialChars(hit.toAbsoluteHref(hit.getHref(), true)));
				buff.append("\"><img href=\""); //$NON-NLS-1$ 
				buff.append(IHelpUIConstants.IMAGE_NW);
				buff.append("\" alt=\""); //$NON-NLS-1$
				buff.append(HelpUIResources
						.getString("SearchResultsPart.nwtooltip"));//$NON-NLS-1$ 
				buff.append("\""); //$NON-NLS-1$ 
				buff.append("/>"); //$NON-NLS-1$ 
				buff.append("</a>"); //$NON-NLS-1$
			}
			if (part.getShowDescription()) {
				String summary = getSummary(hit);
				if (summary != null) {
					buff.append("<br/>"); //$NON-NLS-1$
					//buff.append("<span color=\"summary\">");
					buff.append(summary);
					//buff.append("</span>");
				}
			}
			buff.append("</li>"); //$NON-NLS-1$
		}
		if (errorStatus!=null)
			updateErrorStatus(buff);
		updateNavigation();
		buff.append("</form>"); //$NON-NLS-1$
		searchResults.setText(buff.toString(), true, false);
		section.layout();
		if (reflow)
			part.reflow();
	}

	private void updateErrorStatus(StringBuffer buff) {
		int indent = 21;
		buff.append("<li indent=\"" + indent + "\" style=\"image\" value=\""); //$NON-NLS-1$ //$NON-NLS-2$
		buff.append(ISharedImages.IMG_OBJS_ERROR_TSK);
		buff.append("\">"); //$NON-NLS-1$
		buff.append("<b>"); //$NON-NLS-1$
		buff.append(escapeSpecialChars(errorStatus.getMessage()));
		buff.append("</b>"); //$NON-NLS-1$
		buff.append("<br/>"); //$NON-NLS-1$
		Throwable t = errorStatus.getException();
		if (t!=null)
			buff.append(escapeSpecialChars(t.getMessage()));
		buff.append("</li>"); //$NON-NLS-1$
	}

	private void updateNavigation() {
		if (hits.size() > HITS_PER_PAGE) {
			if (prevLink == null) {
				FormToolkit toolkit = part.getToolkit();
				Composite navContainer = toolkit.createComposite(container);
				TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
				navContainer.setLayoutData(td);
				GridLayout glayout = new GridLayout();
				glayout.numColumns = 2;
				navContainer.setLayout(glayout);
				Label sep = toolkit.createLabel(navContainer, null,
						SWT.SEPARATOR | SWT.HORIZONTAL);
				GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
				gd.horizontalSpan = 2;
				gd.widthHint = 2;
				sep.setLayoutData(gd);
				prevLink = toolkit.createImageHyperlink(navContainer, SWT.NULL);

				prevLink.setText(HelpUIResources.getString("EngineResultSection.previous", ""+HITS_PER_PAGE)); //$NON-NLS-1$ //$NON-NLS-2$
				prevLink.setImage(PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_TOOL_BACK));
				prevLink.addHyperlinkListener(new HyperlinkAdapter() {
					public void linkActivated(HyperlinkEvent e) {
						resultOffset -= HITS_PER_PAGE;
						asyncUpdateResults(false, true);
					}
				});
				nextLink = toolkit
						.createImageHyperlink(navContainer, SWT.RIGHT);
				int remainder = Math.min(hits.size() - resultOffset
						- HITS_PER_PAGE - HITS_PER_PAGE, HITS_PER_PAGE);
				nextLink.setText(HelpUIResources.getString("EngineResultSection.next", ""+remainder)); //$NON-NLS-1$ //$NON-NLS-2$
				nextLink.setImage(PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_TOOL_FORWARD));
				gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
				gd.grabExcessHorizontalSpace = true;
				nextLink.setLayoutData(gd);
				nextLink.addHyperlinkListener(new HyperlinkAdapter() {
					public void linkActivated(HyperlinkEvent e) {
						resultOffset += HITS_PER_PAGE;
						asyncUpdateResults(false, true);
					}
				});
			}
			prevLink.setVisible(resultOffset > 0);
			nextLink.setVisible(hits.size() >= resultOffset + HITS_PER_PAGE);
		} else {
			if (prevLink != null) {
				prevLink.getParent().setMenu(null);
				prevLink.getParent().dispose();
				prevLink = null;
				nextLink = null;
			}
		}
	}

	private String getSummary(ISearchEngineResult hit) {
		String desc = hit.getDescription();
		if (desc != null) {
			String edesc = escapeSpecialChars(desc);
			if (!edesc.equals(hit.getLabel())) {
				String label = hit.getLabel();
				if (edesc.length() > label.length()) {
					String ldesc = edesc.substring(0, label.length());
					if (ldesc.equalsIgnoreCase(label))
						edesc = edesc.substring(label.length() + 1);
				}
				return edesc;
			}
		}
		return null;
	}

	private String escapeSpecialChars(String value) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '&':
				buf.append("&amp;"); //$NON-NLS-1$
				break;
			case '<':
				buf.append("&lt;"); //$NON-NLS-1$
				break;
			case '>':
				buf.append("&gt;"); //$NON-NLS-1$
				break;
			case '\'':
				buf.append("&apos;"); //$NON-NLS-1$
				break;
			case '\"':
				buf.append("&quot;"); //$NON-NLS-1$
				break;
			default:
				buf.append(c);
				break;
			}
		}
		return buf.toString();
	}

	private void updateSectionTitle() {
		if (errorStatus!=null) {
			Label label = part.getToolkit().createLabel(section, null);
			label.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
			section.setTextClient(label);
			section.setText(HelpUIResources.getString(
					"EngineResultSection.sectionTitle.error"));//$NON-NLS-1$
		}
		else {
			section.setTextClient(null);
		}
		if (hits.size() == 1)
			section.setText(HelpUIResources.getString(
					"EngineResultSection.sectionTitle.hit", desc.getLabel(), "" //$NON-NLS-1$ //$NON-NLS-2$
							+ hits.size()));
		else if (hits.size() <= HITS_PER_PAGE)
			section.setText(HelpUIResources.getString(
					"EngineResultSection.sectionTitle.hits", desc.getLabel(), //$NON-NLS-1$
					"" + hits.size())); //$NON-NLS-1$
		else {
			int from = (resultOffset + 1);
			int to = (resultOffset + HITS_PER_PAGE);
			to = Math.min(to, hits.size());
			section.setText(HelpUIResources.getString(
					"EngineResultSection.sectionTitle.hitsRange", desc //$NON-NLS-1$
							.getLabel(), "" + from, "" + to, "" + hits.size())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public void dispose() {
		if (!section.isDisposed()) {
			FormUtil.recursiveSetMenu(section, null);
			section.dispose();
		}
	}
}