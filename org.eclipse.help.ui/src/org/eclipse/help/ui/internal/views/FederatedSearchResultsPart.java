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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.internal.search.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;

public class FederatedSearchResultsPart extends AbstractFormPart implements IHelpPart, ISearchEngineResultCollector { 
	private ReusableHelpPart parent;
	private FormText searchResults;
	private String id;

	private String phrase;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public FederatedSearchResultsPart(Composite parent, FormToolkit toolkit) {
		searchResults = toolkit.createFormText(parent, true);
		searchResults.marginWidth = 10;
		searchResults.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		//searchResults.setImage(topicKey, HelpUIResources.getImage(topicKey));
		//searchResults.setImage(nwKey, HelpUIResources.getImage(nwKey));
		//searchResults.setImage(searchKey, HelpUIResources.getImage(searchKey));
		searchResults.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				/*
				Object href = e.getHref();
				if (href.toString().equals("_more")) //$NON-NLS-1$
					doMoreResults(phrase);
				else if (href.toString().equals("_cancel")) { //$NON-NLS-1$
					if (runningJob!=null) {
						runningJob.cancel();
						runningJob=null;
					}
				}
				else
					doOpenLink(e.getHref());
					*/
			}
		});
		searchResults.setText("", false, false); //$NON-NLS-1$
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return searchResults;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		searchResults.setVisible(visible);
	}

	void clearResults() {
		searchResults.setText("", false, false); //$NON-NLS-1$
		parent.reflow();
	}
	
	void startNewSearch(Job job) {
		/*
		StringBuffer buff = new StringBuffer();
		buff.append("<form>"); //$NON-NLS-1$
		buff.append("<p><span color=\""); //$NON-NLS-1$
		buff.append(FormColors.TITLE);
		buff.append("\">"); //$NON-NLS-1$
		buff.append(HelpUIResources.getString("SearchResultsPart.4")); //$NON-NLS-1$
		buff.append("</span>"); //$NON-NLS-1$
		buff.append("<a href=\"cancel\">"); //$NON-NLS-1$
		buff.append(HelpUIResources.getString("SearchResultsPart.7")); //$NON-NLS-1$
		buff.append("</a></p>"); //$NON-NLS-1$
		buff.append("</form>"); //$NON-NLS-1$
		searchResults.setText(buff.toString(), true, false);
		parent.reflow();
		runningJob = job;
		job.schedule();
		*/
	}
/*
	void updateResults(String phrase, StringBuffer buff,
			HelpSearchResult hresult) {
		if (runningJob!=null) {
			runningJob.cancel();
		}
		this.phrase = phrase;
		buff.delete(0, buff.length());
		if (hresult.getMatchCount() > 0) {
			buff.append("<form>"); //$NON-NLS-1$
			buff.append("<p><span color=\""); //$NON-NLS-1$
			buff.append(FormColors.TITLE);
			buff.append("\">"); //$NON-NLS-1$
			buff.append(HelpUIResources.getString("SearchResultsPart.label")); //$NON-NLS-1$
			buff.append("</span></p>"); //$NON-NLS-1$
			Object[] elements = hresult.getElements();
			resultSorter.sort(null, elements);

			for (int i = 0; i < elements.length; i++) {
				SearchHit hit = (SearchHit) elements[i];
				buff.append("<li indent=\"21\" style=\"image\" value=\""); //$NON-NLS-1$
				buff.append(IHelpUIConstants.IMAGE_FILE_F1TOPIC);
				buff.append("\">"); //$NON-NLS-1$
				buff.append("<a href=\""); //$NON-NLS-1$
				buff.append(hit.getHref());
				buff.append("\">"); //$NON-NLS-1$
				buff.append(hit.getLabel());
				buff.append("</a>"); //$NON-NLS-1$
				buff.append(" <a href=\""); //$NON-NLS-1$
				buff.append("nw:"); //$NON-NLS-1$
				buff.append(hit.getHref());
				buff.append("\"><img href=\""); //$NON-NLS-1$
				buff.append(IHelpUIConstants.IMAGE_NW);
				buff.append("\" alt=\""); //$NON-NLS-1$
				buff.append(HelpUIResources.getString("SearchResultsPart.nwtooltip")); //$NON-NLS-1$
				buff.append("\""); //$NON-NLS-1$
				buff.append("/>"); //$NON-NLS-1$
				buff.append("</a>"); //$NON-NLS-1$
				buff.append("</li>"); //$NON-NLS-1$
			}
			if (elements.length > 0) {
				buff.append("<p><img href=\"");
				buff.append(IHelpUIConstants.IMAGE_HELP_SEARCH);
				buff.append("\"/>");
				buff.append(" <a href=\"_more\">"); //$NON-NLS-1$
				buff.append(HelpUIResources.getString("SearchResultsPart.moreResults")); //$NON-NLS-1$
				buff.append("</a></p>"); //$NON-NLS-1$
			}
			buff.append("</form>"); //$NON-NLS-1$
			searchResults.setText(buff.toString(), true, false);
		} else
			searchResults.setText("", false, false); //$NON-NLS-1$
		parent.reflow();
	}
*/

	private void doOpenLink(Object href) {
		String url = (String) href;

		if (url.startsWith("nw:")) { //$NON-NLS-1$
			WorkbenchHelp.displayHelpResource(url.substring(3));
		} else
			parent.showURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return parent.fillFormContextMenu(searchResults, manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return searchResults.equals(control);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.search.ISearchEngineResultCollector#accept(org.eclipse.help.internal.search.ISearchEngineResult)
	 */
	public void accept(ISearchEngineResult match) {
		// TODO Auto-generated method stub
		
	}
}