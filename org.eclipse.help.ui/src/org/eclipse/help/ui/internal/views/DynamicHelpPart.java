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

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.search.federated.IndexerJob;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.*;

public class DynamicHelpPart extends SectionPart implements IHelpPart {
	private static final String CANCEL_HREF = "__cancel__"; //$NON-NLS-1$

	private static final String MORE_HREF = "__more__"; //$NON-NLS-1$

	private ReusableHelpPart parent;

	private FormText searchResults;

	private SorterByScore resultSorter;

	private String id;

	private String phrase;

	private Job runningJob;
	private IContext context;

	private JobListener jobListener;
	public static final int SHORT_COUNT = 8;

	class JobListener implements IJobChangeListener {
		public void aboutToRun(IJobChangeEvent event) {
		}

		public void awake(IJobChangeEvent event) {
		}

		public void done(IJobChangeEvent event) {
			if (event.getJob() == runningJob) {
				runningJob = null;
			}
		}

		public void running(IJobChangeEvent event) {
		}

		public void scheduled(IJobChangeEvent event) {
		}

		public void sleeping(IJobChangeEvent event) {
		}
	}

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public DynamicHelpPart(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.EXPANDED | Section.TWISTIE
				| Section.TITLE_BAR);
		// configure section
		Section section = getSection();
		section.setText(HelpUIResources.getString("SearchPart.title")); //$NON-NLS-1$
		section.marginWidth = 5;
		section.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
			}
			public void expansionStateChanged(ExpansionEvent e) {
				if (e.getState()) {
					if (phrase.length() > 0)
						startInPlaceSearch(phrase, context);
				}
			}
		});
		// create 'clear' hyperlink on the section tool bar
		ImageHyperlink clearLink = new ImageHyperlink(section, SWT.NULL);
		toolkit.adapt(clearLink, true, true);
		clearLink.setToolTipText(HelpUIResources
				.getString("SearchPart.clearResults")); //$NON-NLS-1$
		clearLink.setImage(HelpUIResources
				.getImage(IHelpUIConstants.IMAGE_CLEAR));
		clearLink.setBackground(section.getTitleBarGradientBackground());
		clearLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				clearResults();
			}
		});
		section.setTextClient(clearLink);		
		resultSorter = new SorterByScore();
		searchResults = toolkit.createFormText(section, true);
		//searchResults.marginWidth = 10;
		section.setClient(searchResults);
		// stext.setFormText(searchResults);
		searchResults.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		// searchResults.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		String topicKey = IHelpUIConstants.IMAGE_FILE_F1TOPIC;
		String nwKey = IHelpUIConstants.IMAGE_NW;
		String searchKey = IHelpUIConstants.IMAGE_HELP_SEARCH;
		searchResults.setImage(topicKey, HelpUIResources.getImage(topicKey));
		searchResults.setImage(nwKey, HelpUIResources.getImage(nwKey));
		searchResults.setImage(searchKey, HelpUIResources.getImage(searchKey));
		searchResults.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				Object href = e.getHref();
				if (href.equals(CANCEL_HREF)) { //$NON-NLS-1$
					if (runningJob != null) {
						runningJob.cancel();
						runningJob = null;
					}
					clearResults();
				} else if (href.equals(MORE_HREF)) {
					doMore();
				} else
					doOpenLink(e.getHref());
			}
		});
		searchResults.setText("", false, false); //$NON-NLS-1$
		jobListener = new JobListener();
		Platform.getJobManager().addJobChangeListener(jobListener);
	}

	public void dispose() {
		Platform.getJobManager().removeJobChangeListener(jobListener);
		if (runningJob!=null) {
			runningJob.cancel();
			runningJob=null;
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return getSection();
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
		getSection().setVisible(visible);
	}

	void clearResults() {
		if (runningJob != null) {
			runningJob.cancel();
			runningJob = null;
		}
		searchResults.setText("", false, false); //$NON-NLS-1$
		parent.reflow();
	}
	
	public void startSearch(String newPhrase, IContext excludeContext) {
		if (phrase!=null && phrase.equals(newPhrase))
			return;
		this.phrase = newPhrase;
		this.context = excludeContext;
		if (getSection().isExpanded())
			startInPlaceSearch(phrase, excludeContext);
	}
	
	private void startInPlaceSearch(final String phrase,
			final IContext excludeContext) {
		Job job = new Job(HelpUIResources.getString("SearchPart.dynamicJob")) { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				try {
					try {
						Platform.getJobManager().join(IndexerJob.FAMILY,
								monitor);
					} catch (InterruptedException e) {
						// TODO should we do someting here?
					}
					performSearch(phrase, excludeContext, monitor);
					return Status.OK_STATUS;
				} catch (OperationCanceledException e) {
					// it is ok to cancel the search
					return Status.OK_STATUS;
				}
			}
		};
		scheduleSearch(job);
	}
	
	private void performSearch(String phrase, IContext excludeContext,
			IProgressMonitor monitor) {
		SearchQuery searchQuery = new SearchQuery();
		searchQuery.setSearchWord(phrase);
		SearchResults localResults = new SearchResults(null,
				DynamicHelpPart.SHORT_COUNT * 2, Platform.getNL());
		BaseHelpSystem.getSearchManager().search(searchQuery, localResults,
				monitor);
		SearchHit[] hits = localResults.getSearchHits();
		updateResults(phrase, excludeContext, new StringBuffer(), hits);
	}

	void scheduleSearch(Job job) {
		if (runningJob != null) {
			runningJob.cancel();
		}
		StringBuffer buff = new StringBuffer();
		buff.append("<form>"); //$NON-NLS-1$
		buff.append("<p><span color=\""); //$NON-NLS-1$
		buff.append(FormColors.TITLE);
		buff.append("\">"); //$NON-NLS-1$
		buff.append(HelpUIResources.getString("SearchResultsPart.progress")); //$NON-NLS-1$
		buff.append("</span>"); //$NON-NLS-1$
		buff.append("<a href=\""); //$NON-NLS-1$
		buff.append(CANCEL_HREF);
		buff.append("\">"); //$NON-NLS-1$
		buff.append(HelpUIResources.getString("SearchResultsPart.cancel")); //$NON-NLS-1$
		buff.append("</a></p>"); //$NON-NLS-1$
		buff.append("</form>"); //$NON-NLS-1$
		searchResults.setText(buff.toString(), true, false);
		parent.reflow();
		runningJob = job;
		job.schedule();
	}
	
	private void updateResults(final String phrase,
			final IContext excludeContext, final StringBuffer buffer,
			final SearchHit[] hits) {
		if (getSection().isDisposed())
			return;
		getSection().getDisplay().asyncExec(new Runnable() {
			public void run() {
				doUpdateResults(phrase, excludeContext, buffer, hits);
			}
		});
	}	

	private void doUpdateResults(String phrase, IContext excludeContext, StringBuffer buff, SearchHit[] hits) {
		if (runningJob != null) {
			runningJob.cancel();
		}
		this.phrase = phrase;
		buff.delete(0, buff.length());
		if (hits.length > 0) {
			buff.append("<form>"); //$NON-NLS-1$
			buff.append("<p><span color=\""); //$NON-NLS-1$
			buff.append(FormColors.TITLE);
			buff.append("\">"); //$NON-NLS-1$
			buff.append(HelpUIResources.getString("SearchResultsPart.label")); //$NON-NLS-1$
			buff.append("</span></p>"); //$NON-NLS-1$
			resultSorter.sort(null, hits);
			IHelpResource [] excludedTopics = excludeContext!=null?excludeContext.getRelatedTopics():null;

			for (int i = 0; i < hits.length; i++) {
				SearchHit hit = hits[i];
				if (isExcluded(hit.getHref(), excludedTopics))
					continue;
				if (i==SHORT_COUNT)
					break;
				buff.append("<li indent=\"21\" style=\"image\" value=\""); //$NON-NLS-1$
				buff.append(IHelpUIConstants.IMAGE_FILE_F1TOPIC);
				buff.append("\">"); //$NON-NLS-1$
				buff.append("<a href=\""); //$NON-NLS-1$
				buff.append(hit.getHref());
				buff.append("\" alt=\""); //$NON-NLS-1$
				buff.append(hit.getToc().getLabel());
				buff.append("\">"); //$NON-NLS-1$
				buff.append(hit.getLabel());
				buff.append("</a>"); //$NON-NLS-1$
				/*
				 * buff.append(" <a href=\""); //$NON-NLS-1$ buff.append("nw:");
				 * //$NON-NLS-1$ buff.append(hit.getHref()); buff.append("\">
				 * <img href=\""); //$NON-NLS-1$
				 * buff.append(IHelpUIConstants.IMAGE_NW); buff.append("\"
				 * alt=\""); //$NON-NLS-1$
				 * buff.append(HelpUIResources.getString("SearchResultsPart.nwtooltip"));
				 * //$NON-NLS-1$ buff.append("\""); //$NON-NLS-1$
				 * buff.append("/>"); //$NON-NLS-1$ buff.append(" </a>");
				 * //$NON-NLS-1$
				 */
				buff.append("</li>"); //$NON-NLS-1$
			}
			if (hits.length > 0) {
				buff.append("<p><img href=\""); //$NON-NLS-1$
				buff.append(IHelpUIConstants.IMAGE_HELP_SEARCH);
				buff.append("\"/>"); //$NON-NLS-1$
				buff.append(" <a href=\""); //$NON-NLS-1$
				buff.append(MORE_HREF);
				buff.append("\">"); //$NON-NLS-1$
				buff.append(HelpUIResources
						.getString("SearchResultsPart.moreResults")); //$NON-NLS-1$
				buff.append("</a></p>"); //$NON-NLS-1$
			}
			buff.append("</form>"); //$NON-NLS-1$
			searchResults.setText(buff.toString(), true, false);
		} else
			searchResults.setText("", false, false); //$NON-NLS-1$
		parent.reflow();
	}
	
	private boolean isExcluded(String href, IHelpResource [] excludedTopics) {
		if (excludedTopics==null) return false;
		for (int i=0; i<excludedTopics.length; i++) {
			if (href.startsWith(excludedTopics[i].getHref()))
				return true;
		}
		return false;
	}

	private void doMore() {
		/*
		try {
			String ephrase = URLEncoder.encode(phrase, "UTF-8"); //$NON-NLS-1$
			String query = "tab=search&searchWord=" + ephrase; //$NON-NLS-1$
			WorkbenchHelp.displayHelpResource(query);
		} catch (UnsupportedEncodingException e) {
			System.out.println(e);
		}
		*/
		parent.startSearch(phrase);
	}

	private void doOpenLink(Object href) {
		String url = (String) href;

		if (url.startsWith("nw:")) { //$NON-NLS-1$
			PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url.substring(3));
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

	public IAction getGlobalAction(String id) {
		if (id.equals(ActionFactory.COPY.getId()))
			return parent.getCopyAction();
		return null;
	}
}