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

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.search.federated.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.forms.events.HyperlinkAdapter;

public class FederatedSearchResultsPart extends AbstractFormPart implements IHelpPart { 
	private ReusableHelpPart parent;
	private Composite separator;
	private Composite container;
	private FormText searchResults;
	private String id;
	private ArrayList results;

	private String phrase;
	
	class EngineResult {
		EngineDescriptor ed;
		ArrayList hits;

		public EngineResult(EngineDescriptor ed) {
			this.ed = ed;
			hits = new ArrayList();
		}
		public void clear() {
			hits.clear();
		}
	}

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public FederatedSearchResultsPart(Composite parent, FormToolkit toolkit) {
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		container = toolkit.createComposite(parent);
		container.setLayout(layout);
		separator = toolkit.createCompositeSeparator(container);
		separator.setVisible(false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 2;
		separator.setLayoutData(gd);		
		ScrolledFormText stext = new ScrolledFormText(container, false);
		stext.setLayoutData(new GridData(GridData.FILL_BOTH));
		toolkit.adapt(stext, false, true);
		searchResults = toolkit.createFormText(stext, true);
		stext.setFormText(searchResults);
		searchResults.marginWidth = 10;
		searchResults.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		String topicKey = IHelpUIConstants.IMAGE_FILE_F1TOPIC;
		String nwKey = IHelpUIConstants.IMAGE_NW;
		String searchKey = IHelpUIConstants.IMAGE_HELP_SEARCH;
		searchResults.setImage(topicKey, HelpUIResources.getImage(topicKey));
		searchResults.setImage(nwKey, HelpUIResources.getImage(nwKey));
		searchResults.setImage(searchKey, HelpUIResources.getImage(searchKey));
		searchResults.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				Object href = e.getHref();
				if (href.toString().equals("__cancel__")) { //$NON-NLS-1$
					Platform.getJobManager().cancel(FederatedSearchJob.FAMILY);
					clearResults();
				}
				else
					doOpenLink(e.getHref());
			}
		});
		searchResults.setText("", false, false); //$NON-NLS-1$
		results = new ArrayList();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return container;
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
		container.setVisible(visible);
	}

	void clearResults() {
		searchResults.setText("", false, false); //$NON-NLS-1$
		parent.reflow();
		separator.setVisible(false);
	}
	
	void startNewSearch(String phrase) {
		this.phrase = phrase;
		StringBuffer buff = new StringBuffer();
		buff.append("<form>"); //$NON-NLS-1$
		buff.append("<p><span color=\""); //$NON-NLS-1$
		buff.append(FormColors.TITLE);
		buff.append("\">"); //$NON-NLS-1$
		buff.append(HelpUIResources.getString("SearchResultsPart.progress")); //$NON-NLS-1$
		buff.append("</span>"); //$NON-NLS-1$
		buff.append("<a href=\"__cancel__\">"); //$NON-NLS-1$
		buff.append(HelpUIResources.getString("SearchResultsPart.cancel")); //$NON-NLS-1$
		buff.append("</a></p>"); //$NON-NLS-1$
		buff.append("</form>"); //$NON-NLS-1$
		searchResults.setText(buff.toString(), true, false);
		results.clear();
		parent.reflow();
		separator.setVisible(true);
	}
	
	private void asyncUpdateResults() {
		searchResults.getDisplay().asyncExec(new Runnable() {
			public void run() {
				updateResults();
			}
		});
	}
	
	private void updateResults() {
		StringBuffer buff= new StringBuffer();
		buff.append("<form>"); //$NON-NLS-1$
		buff.append("<p><span color=\""); //$NON-NLS-1$
		buff.append(FormColors.TITLE);
		buff.append("\">"); //$NON-NLS-1$
		buff.append(HelpUIResources.getString("SearchResultsPart.label")); //$NON-NLS-1$
		buff.append("</span></p>"); //$NON-NLS-1$
		for (int i=0; i<results.size(); i++) {
			EngineResult er = (EngineResult)results.get(i);
			if (er.hits.size()==0)
				continue;
			buff.append("<p>");
			buff.append(er.ed.getLabel());
			buff.append(" ("+er.hits.size()+" hits)");
			buff.append("</p>");
			updateResults(er, buff);
		}
		buff.append("</form>"); //$NON-NLS-1$
		searchResults.setText(buff.toString(), true, false);
		//parent.reflow();
		ScrolledFormText stext = (ScrolledFormText)searchResults.getParent();
		stext.reflow(true);
	}

	private void updateResults(EngineResult er, StringBuffer buff) {
		for (int i = 0; i < er.hits.size(); i++) {
			ISearchEngineResult hit = (ISearchEngineResult)er.hits.get(i);
			buff.append("<li indent=\"31\" bindent=\"10\" style=\"image\" value=\""); //$NON-NLS-1$
			buff.append(IHelpUIConstants.IMAGE_FILE_F1TOPIC);
			buff.append("\">"); //$NON-NLS-1$
			buff.append("<a href=\""); //$NON-NLS-1$
			buff.append(hit.getHref());
			buff.append("\" alt=\"");
			buff.append(hit.getCategory().getLabel());
			buff.append("\">"); //$NON-NLS-1$
			buff.append(hit.getLabel());
			buff.append("</a>"); //$NON-NLS-1$
			/*
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
			*/
			buff.append("</li>"); //$NON-NLS-1$
		}
	}

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
	 * @see org.eclipse.help.internal.search.ISearchEngineResultCollector#add(org.eclipse.help.internal.search.ISearchEngineResult)
	 */
	public synchronized void add(EngineDescriptor ed, ISearchEngineResult match) {
		EngineResult er = findEngineResult(ed);
		er.hits.add(match);
		asyncUpdateResults();
	}
 
    /* (non-Javadoc)
     * @see org.eclipse.help.internal.search.federated.ISearchEngineResultCollector#add(org.eclipse.help.internal.search.federated.ISearchEngineResult[])
     */
    public synchronized void add(EngineDescriptor ed, ISearchEngineResult[] searchResults) {
    	EngineResult er = findEngineResult(ed);
    	for (int i=0; i<searchResults.length; i++) 
    		er.hits.add(searchResults[i]);
    	asyncUpdateResults();
    }
    
    private synchronized EngineResult findEngineResult(EngineDescriptor ed) {
    	for (int i=0; i<results.size(); i++) {
    		EngineResult er = (EngineResult)results.get(i);
    		if (er.ed==ed)
    			return er;
    	}
    	EngineResult er = new EngineResult(ed);
    	results.add(er);
    	return er;
    }
}