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

import java.util.*;
import java.util.ArrayList;

import org.eclipse.help.*;
import org.eclipse.help.internal.appserver.WebappManager;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.federated.IndexerJob;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.*;

public class ReusableHelpPart implements IHelpUIConstants {
	public static final int ALL_TOPICS = 1 << 1;
	public static final int CONTEXT_HELP = 1 << 2;
	public static final int SEARCH = 1 << 3;
	private ManagedForm mform;
	private int verticalSpacing = 15;
	private int bmargin = 5;
	private String defaultContextHelpText;

	private ArrayList pages;
	private Action backAction;
	private Action nextAction;
	private CopyAction copyAction;
	private Action openInfoCenterAction;
	private OpenHrefAction openAction;
	private OpenHrefAction openInHelpAction;
	private ReusableHelpPartHistory history;
	private HelpPartPage currentPage;
	private int style;
	private boolean showDocumentsInPlace=true;
	private int numberOfInPlaceHits =8;

	private IRunnableContext runnableContext;

	private IToolBarManager toolBarManager;
	private IStatusLineManager statusLineManager;
	private IActionBars actionBars;
	
	private abstract class BusyRunAction extends Action {
		public BusyRunAction(String name) {
			super(name);
		}
		public void run() {
			BusyIndicator.showWhile(getControl().getDisplay(), 
					new Runnable() {
				public void run() {
					busyRun();
				}
			});
		}
		protected abstract void busyRun();
	}
	private abstract class OpenHrefAction extends BusyRunAction {
		private Object target;
		public OpenHrefAction(String name) {
			super(name);
		}
		public void setTarget(Object target) {
			this.target = target;
		}
		public Object getTarget() {
			return target;
		}
	}
	private class CopyAction extends Action {
		private FormText target;
		public CopyAction() {
			super("copy"); //$NON-NLS-1$
		}
		public void setTarget(FormText target) {
			this.target = target;
			setEnabled(target.canCopy());
		}
		public void run() {
			if (target!=null)
				target.copy();
		}
	}
	private static class PartRec {
		String id;
		boolean flexible;
		boolean grabVertical;
		IHelpPart part;

		PartRec(String id, boolean flexible, boolean grabVertical) {
			this.id = id;
			this.flexible = flexible;
			this.grabVertical = grabVertical;
		}
	}
	private class HelpPartPage {
		private String id;
		private int vspacing = verticalSpacing;
		private int horizontalMargin = 0;

		private String text;
		private SubActionBars bars;
		private IToolBarManager toolBarManager;
		protected ArrayList partRecs;
		private int nflexible;
		private Control focusControl;

		public HelpPartPage(String id, String text) {
			this.id = id;
			this.text = text;
			partRecs = new ArrayList();
			if (ReusableHelpPart.this.actionBars!=null) {
				bars = new SubActionBars(ReusableHelpPart.this.actionBars);
				toolBarManager = bars.getToolBarManager();
			}
			else
				toolBarManager = new SubToolBarManager(ReusableHelpPart.this.toolBarManager);
		}
		public void dispose() {
			if (bars!=null) {
				bars.dispose();
				bars = null;
				toolBarManager = null;
			}
			else
				((SubToolBarManager)toolBarManager).disposeManager();
			partRecs = null;
		}
		public void setVerticalSpacing(int value) {
			this.vspacing = value;
		}
		public int getVerticalSpacing() {
			return vspacing;
		}
		public void setHorizontalMargin(int value) {
			this.horizontalMargin = value;
		}
		public int getHorizontalMargin() {
			return horizontalMargin;
		}
		public IToolBarManager getToolBarManager() {
			return toolBarManager;
		}

		public String getId() {
			return id;
		}
		public String getText() {
			return text;
		}
		
		public void addPart(String id, boolean flexible) {
			addPart(id, flexible, false);
		}

		public void addPart(String id, boolean flexible, boolean grabVertical) {
			partRecs.add(new PartRec(id, flexible, grabVertical));
			if (flexible)
				nflexible++;
		}

		public PartRec[] getParts() {
			return (PartRec[]) partRecs.toArray(new PartRec[partRecs.size()]);
		}
		
		public int getNumberOfFlexibleParts() {
			return nflexible;
		}
		
		public boolean canOpen() {
			for (int i = 0; i < partRecs.size(); i++) {
				PartRec rec = (PartRec) partRecs.get(i);

				if (rec.id.equals(IHelpUIConstants.HV_BROWSER)) {
					//Try to create a browser and watch
					// for 'no-handle' error - it means
					// that the embedded browser is not
					// available
					try {
						createRecPart(rec);
						rec.part.setVisible(false);
					}
					catch (SWTError error) {
						// cannot create a browser
						return false;
					}
				}
			}
			return true;
		}
		
		public void stop() {
			for (int i=0; i<partRecs.size(); i++) {
				PartRec rec =(PartRec)partRecs.get(i);
				rec.part.stop();
			}
		}

		public void setVisible(boolean visible) {
			if (bars!=null) bars.clearGlobalActionHandlers();
			ArrayList tabList = new ArrayList();
			for (int i = 0; i < partRecs.size(); i++) {
				PartRec rec = (PartRec) partRecs.get(i);
				if (visible) {
					createRecPart(rec);
					hookGlobalAction(ActionFactory.PRINT.getId(), rec.part);
					hookGlobalAction(ActionFactory.COPY.getId(), rec.part);
					tabList.add(rec.part.getControl());
				}
				rec.part.setVisible(visible);
			}
			Composite parent = mform.getForm().getBody();
			parent.setTabList((Control[])tabList.toArray(new Control[tabList.size()]));
			
			if (actionBars!=null) {
				actionBars.clearGlobalActionHandlers();
				if (visible) {
					Map handlers = bars.getGlobalActionHandlers();
					if (handlers!=null) {
						Set keys = handlers.keySet();
						for (Iterator iter=keys.iterator(); iter.hasNext();) {
							String key = (String)iter.next();
							actionBars.setGlobalActionHandler(key, (IAction)handlers.get(key));
						}
					}
				}
			}
			if (bars!=null) {
				if (visible)
					bars.activate();
				else
					bars.deactivate();
				bars.updateActionBars();
			}
			else
				((SubToolBarManager)toolBarManager).setVisible(visible);			
		}
		private void hookGlobalAction(String id, IHelpPart part) {
			if (bars==null) return;
			IAction action = part.getGlobalAction(id);
			if (action!=null)
				bars.setGlobalActionHandler(id, action);
		}
		private void createRecPart(PartRec rec) throws SWTError {
			if (rec.part == null) {
				rec.part = createPart(rec.id, toolBarManager);
				rec.part.getControl().addListener(SWT.Activate, new Listener() {
					public void handleEvent(Event e) {
						focusControl = e.widget.getDisplay().getFocusControl();
					}
				});
			}
		}
		public IHelpPart findPart(String id) {
			for (int i = 0; i < partRecs.size(); i++) {
				PartRec rec = (PartRec) partRecs.get(i);
				if (rec.id.equals(id))
					return rec.part;
			}
			return null;
		}
		public void setFocus() {
			//set focus on the control that had
			//focus when this page was active
			if (focusControl!=null)
				focusControl.setFocus();
			if (partRecs.size()==0) return;
			PartRec rec = (PartRec)partRecs.get(0);
			rec.part.setFocus();
		}
	}

	class HelpPartLayout extends Layout implements ILayoutExtension {
		public int computeMaximumWidth(Composite parent, boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
		}

		public int computeMinimumWidth(Composite parent, boolean changed) {
			return computeSize(parent, 0, SWT.DEFAULT, changed).x;
		}

		protected Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			if (currentPage==null)
				return new Point(0, 0);
			PartRec[] parts = currentPage.getParts();
			int hmargin = currentPage.getHorizontalMargin();
			int innerWhint = wHint!=SWT.DEFAULT?wHint-2*hmargin:wHint;
			Point result = new Point(0, 0);
			for (int i=0; i<parts.length; i++) {
				PartRec partRec = parts[i];
				if (!partRec.flexible) {
					Control c = partRec.part.getControl();
					Point size = c.computeSize(innerWhint, SWT.DEFAULT, flushCache);
					result.x = Math.max(result.x, size.x);
					result.y += size.y;
				}
				if (i<parts.length-1)
					result.y += currentPage.getVerticalSpacing();								
			}
			result.x += hmargin * 2;
			result.y += bmargin; 
			return result;
		}

		protected void layout(Composite composite, boolean flushCache) {
			if (currentPage==null) 
				return;
			
			Rectangle clientArea = composite.getClientArea();

			PartRec[] parts = currentPage.getParts();
			int hmargin = currentPage.getHorizontalMargin();			
			int nfixedParts = parts.length - currentPage.getNumberOfFlexibleParts();
			Point [] fixedSizes = new Point[nfixedParts];
			int fixedHeight = 0;
			int index = 0;
			int innerWidth = clientArea.width-hmargin*2;
			for (int i=0; i<parts.length; i++) {
				PartRec partRec = parts[i];
				if (!partRec.flexible) {
					Control c = partRec.part.getControl();
					Point size = c.computeSize(innerWidth, SWT.DEFAULT, false);
					fixedSizes[index++] = size;
					if (!partRec.grabVertical)
						fixedHeight += size.y;
				}
				if (i<parts.length-1)
					fixedHeight += currentPage.getVerticalSpacing();				
			}
			fixedHeight += bmargin;
			int flexHeight = clientArea.height - fixedHeight;
			int flexPortion = 0;
			if (currentPage.getNumberOfFlexibleParts()>0)
				flexPortion = flexHeight/currentPage.getNumberOfFlexibleParts();

			int usedFlexHeight = 0;
			int y = 0;
			index = 0;
			int nflexParts = 0;
			for (int i=0; i<parts.length; i++) {
				PartRec partRec = parts[i];
				Control c = partRec.part.getControl();
				
				if (partRec.flexible) {
					int height;
					if (++nflexParts == currentPage.getNumberOfFlexibleParts())
						height = flexHeight-usedFlexHeight;
					else {
						height = flexPortion;
						usedFlexHeight += height;
					}
					c.setBounds(0, y, clientArea.width, height);
				}
				else {
					Point fixedSize = fixedSizes[index++];
					if (fixedSize.y<flexHeight && partRec.grabVertical)
						c.setBounds(hmargin, y, innerWidth, flexHeight);
					else
						c.setBounds(hmargin, y, innerWidth, fixedSize.y);
				}
				if (i<parts.length-1)
					y += c.getSize().y + currentPage.getVerticalSpacing();
			}
		}
	}

	public ReusableHelpPart(IRunnableContext runnableContext) {
		this(runnableContext, CONTEXT_HELP|SEARCH|ALL_TOPICS);
	}
	public ReusableHelpPart(IRunnableContext runnableContext, int style) {
		this.runnableContext = runnableContext;
		history = new ReusableHelpPartHistory();
		this.style = style;
		ensureHelpIndexed();
	}
	
	private void ensureHelpIndexed() {
		// make sure we have the index
		IndexerJob indexerJob = new IndexerJob();
		indexerJob.schedule();
	}
	
	private void definePages() {
		pages = new ArrayList();
		// federated search page
		HelpPartPage page = new HelpPartPage(HV_FSEARCH_PAGE, HelpUIResources.getString("ReusableHelpPart.searchPage.name")); //$NON-NLS-1$
		page.setVerticalSpacing(0);		
		page.addPart(HV_FSEARCH, false);
		page.addPart(HV_FSEARCH_RESULT, true);
		page.addPart(HV_SEE_ALSO, false);
		pages.add(page);
		
		// all topics page
		page = new HelpPartPage(HV_ALL_TOPICS_PAGE, HelpUIResources.getString("ReusableHelpPart.allTopicsPage.name")); //$NON-NLS-1$
		page.setVerticalSpacing(0);
		page.setHorizontalMargin(0);		
		page.addPart(HV_TOPIC_TREE, true);
		page.addPart(HV_SEE_ALSO, false);
		pages.add(page);
		// browser page
		page = new HelpPartPage(HV_BROWSER_PAGE, null);
		page.setVerticalSpacing(0);
		page.addPart(HV_BROWSER, true);
		page.addPart(HV_SEE_ALSO, false);
		pages.add(page);
		// context help page
		page = new HelpPartPage(HV_CONTEXT_HELP_PAGE, HelpUIResources.getString("ReusableHelpPart.contextHelpPage.name")); //$NON-NLS-1$
		page.addPart(HV_CONTEXT_HELP, false);
		page.addPart(HV_SEARCH_RESULT, false, true);
		page.addPart(HV_SEE_ALSO, false);
		pages.add(page);
	}

	public void init(IActionBars bars, IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {
		this.actionBars = bars;
		this.toolBarManager = toolBarManager;
		this.statusLineManager = statusLineManager;
		makeActions();
		definePages();
	}
	
	private void makeActions() {
		backAction = new Action("back") { //$NON-NLS-1$
			public void run() {
				doBack();
			}
		};
		backAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		backAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
		backAction.setEnabled(false);
		backAction.setText(HelpUIResources.getString("ReusableHelpPart.back.label")); //$NON-NLS-1$
		backAction.setToolTipText(HelpUIResources.getString("ReusableHelpPart.back.tooltip")); //$NON-NLS-1$
		backAction.setId("back"); //$NON-NLS-1$
		
		nextAction = new Action("next") { //$NON-NLS-1$
			public void run() {
				doNext();
			}
		};
		nextAction.setText(HelpUIResources.getString("ReusableHelpPart.forward.label")); //$NON-NLS-1$
		nextAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		nextAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));
		nextAction.setEnabled(false);
		nextAction.setToolTipText(HelpUIResources.getString("ReusableHelpPart.forward.tooltip")); //$NON-NLS-1$
		nextAction.setId("next"); //$NON-NLS-1$
		toolBarManager.add(backAction);
		toolBarManager.add(nextAction);
		
		openInfoCenterAction = new BusyRunAction("openInfoCenter") { //$NON-NLS-1$
			protected void busyRun() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp();
			}
		};
		openInfoCenterAction.setText(HelpUIResources.getString("ReusableHelpPart.openInfoCenterAction.label")); //$NON-NLS-1$
		openAction = new OpenHrefAction("open") { //$NON-NLS-1$
			protected void busyRun() {
				doOpen(getTarget(), getShowDocumentsInPlace());
			}
		};
		openAction.setText(HelpUIResources.getString("ReusableHelpPart.openAction.label")); //$NON-NLS-1$
		openInHelpAction = new OpenHrefAction(HelpUIResources.getString("ReusableHelpPart.openInHelpAction.label")) { //$NON-NLS-1$
			protected void busyRun() {
				doOpenInHelp(getTarget());
			}
		};
		openInHelpAction.setText(HelpUIResources.getString("ReusableHelpPart.openInHelpContentsAction.label")); //$NON-NLS-1$
		copyAction = new CopyAction();
		copyAction.setText(HelpUIResources.getString("ReusableHelpPart.copyAction.label")); //$NON-NLS-1$
	}

	private void doBack() {
		String id = getCurrentPageId();
		if (id.equals(IHelpUIConstants.HV_BROWSER_PAGE)) {
			// stop the browser
			BrowserPart part = (BrowserPart)findPart(IHelpUIConstants.HV_BROWSER);
			part.stop();
		}
		HistoryEntry entry = history.prev();
		if (entry!=null)
			executeHistoryEntry(entry);
	}
	
	private void doNext() {
		HistoryEntry entry = history.next();
		if (entry!=null)
			executeHistoryEntry(entry);
	}

	private void executeHistoryEntry(HistoryEntry entry) {
		history.setBlocked(true);
		if (entry.getType()==HistoryEntry.PAGE) {
			HelpPartPage page = showPage(entry.getTarget(), true);
			mform.setInput(entry.getData());
		}
		else if (entry.getType()==HistoryEntry.URL) {
			String relativeUrl = (String)entry.getData();
			showURL(relativeUrl!=null?relativeUrl:entry.getTarget(), true);
		}
	}

	public void createControl(Composite parent, FormToolkit toolkit) {
		ScrolledForm form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new HelpPartLayout());
		mform = new ManagedForm(toolkit, form);
		mform.getForm().setDelayedReflow(false);
		MenuManager manager = new MenuManager();
		IMenuListener listener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		};
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(listener);
		Menu contextMenu = manager.createContextMenu(form.getForm());
		form.getForm().setMenu(contextMenu);
	}

	public HelpPartPage showPage(String id) {
		if (currentPage!=null && currentPage.getId().equals(id))
			return currentPage;
		for (int i=0; i<pages.size(); i++) {
			HelpPartPage page = (HelpPartPage)pages.get(i);
			if (page.getId().equals(id)) {
				boolean success = flipPages(currentPage, page);
				return success?page:null;
			}
		}
		return null;
	}
	public HelpPartPage showPage(String id, boolean setFocus) {
		HelpPartPage page = this.showPage(id);
		if (page!=null && setFocus)
			page.setFocus();
		return page;
	}
	
	public void startSearch(String phrase) {
		showPage(IHelpUIConstants.HV_FSEARCH_PAGE, true);
		SearchPart part = (SearchPart)findPart(IHelpUIConstants.HV_FSEARCH);
		if (part!=null && phrase!=null)
			part.startSearch(phrase);
	}

	private boolean flipPages(HelpPartPage oldPage, HelpPartPage newPage) {
		if (newPage.canOpen()==false)
			return false;
		if (oldPage!=null) {
			oldPage.stop();
			oldPage.setVisible(false);
		}
		mform.getForm().setText(newPage.getText());			
		newPage.setVisible(true);
		toolBarManager.update(true);
		currentPage = newPage;		
		mform.getForm().getBody().layout(true);
		mform.reflow(true);
		if (newPage.getId().equals(IHelpUIConstants.HV_BROWSER_PAGE)==false) {
			if (!history.isBlocked()) {
				history.addEntry(new HistoryEntry(HistoryEntry.PAGE, newPage.getId(), null));
			}
			updateNavigation();
		}
		return true;
	}
	/*
	void addPageHistoryEntry(String id, Object data) {
		if (!history.isBlocked()) {
			history.addEntry(new HistoryEntry(HistoryEntry.PAGE, id, data));
		}
		updateNavigation();
	}
	*/
	public HelpPartPage getCurrentPage() {
		return currentPage;
	}
	public String getCurrentPageId() {
		return currentPage.getId();
	}

	void browserChanged(String url) {
		if (!history.isBlocked()) {
			history.addEntry(new HistoryEntry(HistoryEntry.URL, url, toRelativeURL(url)));
		}
		updateNavigation();
	}
	
	private void updateNavigation() {
		backAction.setEnabled(history.hasPrev());
		nextAction.setEnabled(history.hasNext());
		history.setBlocked(false);
	}

	public boolean isMonitoringContextHelp() {
		return currentPage!=null && currentPage.getId().equals(HV_CONTEXT_HELP_PAGE);
	}

	public Control getControl() {
		return mform.getForm();
	}
	
	public ManagedForm getForm() {
		return mform;
	}
	public void reflow() {
		mform.getForm().getBody().layout();
		mform.reflow(true);
	}

	public void dispose() {
		for (int i=0; i<pages.size(); i++) {
			HelpPartPage page = (HelpPartPage)pages.get(i);
			page.dispose();
		}
		pages = null;
		if (mform != null) {
			mform.dispose();
			mform = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#setFocus()
	 */
	public void setFocus() {
		if (currentPage!=null)
			currentPage.setFocus();
		else
			mform.setFocus();
	}

	public void update(IWorkbenchPart part, Control control) {
		mform.setInput(new ContextHelpProviderInput((IContextProvider)null, control, part));
	}
	
	public void update(IContextProvider provider, IWorkbenchPart part, Control control) {
		mform.setInput(new ContextHelpProviderInput(provider, control, part));
	}
	
	public void update(IContext context, IWorkbenchPart part, Control control) {
		mform.setInput(new ContextHelpProviderInput(context, control, part));
	}

	private IHelpPart createPart(String id, IToolBarManager tbm) {
		IHelpPart part = null;
		Composite parent = mform.getForm().getBody();
		
		part = findPart(id);
		if (part!=null)
			return part;

		if (id.equals(HV_TOPIC_TREE)) {
			part = new AllTopicsPart(parent, mform.getToolkit(), tbm);
		} else if (id.equals(HV_CONTEXT_HELP)) {
			part = new ContextHelpPart(parent, mform.getToolkit());
			((ContextHelpPart)part).setDefaultText(getDefaultContextHelpText());
		} else if (id.equals(HV_BROWSER)) {
			part = new BrowserPart(parent, mform.getToolkit(), tbm);
		} else if (id.equals(HV_SEARCH_RESULT)) {
			part = new DynamicHelpPart(parent, mform.getToolkit());
		} else if (id.equals(HV_FSEARCH_RESULT)) {
			part = new SearchResultsPart(parent, mform.getToolkit(), tbm);
		} else if (id.equals(HV_SEE_ALSO)) {
			part = new SeeAlsoPart(parent, mform.getToolkit());
		} else if (id.equals(HV_FSEARCH)) {
			part = new SearchPart(parent, mform.getToolkit());
		}
		if (part != null) {
			part.init(this, id);
			part.initialize(mform);
			mform.addPart(part);
		}
		return part;
	}
	
	/**
	 * @return Returns the runnableContext.
	 */
	public IRunnableContext getRunnableContext() {
		return runnableContext;
	}
	public boolean isInWorkbenchWindow() {
		return runnableContext instanceof IWorkbenchWindow;
	}
	/**
	 * @return Returns the defaultContextHelpText.
	 */
	public String getDefaultContextHelpText() {
		return defaultContextHelpText;
	}
	/**
	 * @param defaultContextHelpText The defaultContextHelpText to set.
	 */
	public void setDefaultContextHelpText(String defaultContextHelpText) {
		this.defaultContextHelpText = defaultContextHelpText;
	}

	public void showURL(final String url) {
		BusyIndicator.showWhile(getControl().getDisplay(),
				new Runnable() {
			public void run() {
				showURL(url, getShowDocumentsInPlace());
			}
		});
	}

	public void showURL(String url, boolean replace) {
		if (url==null) return;
		if (url.startsWith("nw:")) { //$NON-NLS-1$
			replace=false;
			url = url.substring(3);
		}
		if (replace) {
			showPage(IHelpUIConstants.HV_BROWSER_PAGE);
			BrowserPart bpart = (BrowserPart)findPart(IHelpUIConstants.HV_BROWSER);
			if (bpart!=null) {
				bpart.showURL(toAbsoluteURL(url));
				return;
			}
		}
		// fallback - open in help resource
		
		//try {
			//URL fullURL = new URL(toAbsoluteURL(url));
			//WebBrowser.openURL(fullURL, 0, "org.eclipse.help");
			
		//}
		//catch (MalformedURLException e) {
		//}
		showExternalURL(url);
	}
	
	public void showExternalURL(String url) {
		if (isHelpResource(url))
			PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url);
		else {
			try {
				String aurl = toAbsoluteURL(url);
				if (aurl.endsWith("&noframes=true")||aurl.endsWith("?noframes=true")) //$NON-NLS-1$ //$NON-NLS-2$
					aurl = aurl.substring(0, aurl.length()-14);
				BaseHelpSystem.getHelpBrowser(true).displayURL(aurl);
			}
			catch (Exception e) {
				HelpUIPlugin.logError("Error opening browser", e); //$NON-NLS-1$
			}
		}
	}
	
	public IHelpPart findPart(String id) {
		if (mform==null) return null;
		IFormPart [] parts = (IFormPart[])mform.getParts();
		for (int i=0; i<parts.length; i++) {
			IHelpPart part = (IHelpPart)parts[i];
			if (part.getId().equals(id))
				return part;
		}
		return null;
	}
	
	public boolean isHelpResource(String url) {
		if (url==null || url.indexOf("://")== -1) //$NON-NLS-1$
			return true;
		return false;
	}

	String toAbsoluteURL(String url) {
		if (url==null || url.indexOf("://")!= -1) //$NON-NLS-1$
			return url;
		BaseHelpSystem.ensureWebappRunning();
		String base = getBase();
		return base + url;
		//char sep = url.lastIndexOf('?')!= -1 ? '&':'?';
		//return base + url+sep+"noframes=true"; //$NON-NLS-1$
	}

	String toRelativeURL(String url) {
		String base = getBase();
		if (url.startsWith(base))
			return url.substring(base.length());
		return url;
	}

	private String getBase() {
		return "http://" //$NON-NLS-1$
			+ WebappManager.getHost() + ":" //$NON-NLS-1$
			+ WebappManager.getPort() + "/help/nftopic"; //$NON-NLS-1$
	}

	private void contextMenuAboutToShow(IMenuManager manager) {
		IFormPart [] parts = mform.getParts();
		boolean hasContext=false;
		Control focusControl = getControl().getDisplay().getFocusControl();
		for (int i=0; i<parts.length; i++) {
			IHelpPart part = (IHelpPart)parts[i];
			if (part.hasFocusControl(focusControl)) {
				hasContext = part.fillContextMenu(manager);
				break;
			}
		}
		if (hasContext)
			manager.add(new Separator());
		manager.add(backAction);
		manager.add(nextAction);
		manager.add(new Separator());
		manager.add(openInfoCenterAction);
	}
	boolean fillSelectionProviderMenu(ISelectionProvider provider, IMenuManager manager) {
		fillOpenActions(provider, manager);
		return true;
	}
	private boolean fillOpenActions(Object target, IMenuManager manager) {
		String href = getHref(target);
		if (href!=null && !href.startsWith("__")) { //$NON-NLS-1$
			openAction.setTarget(target);
			openInHelpAction.setTarget(target);
			manager.add(openAction);
			if (!href.startsWith("nw:")) //$NON-NLS-1$
				manager.add(openInHelpAction);
			return true;
		}
		return false;
	}
	boolean fillFormContextMenu(FormText text, IMenuManager manager) {
		if (fillOpenActions(text, manager))
			manager.add(new Separator());
		manager.add(copyAction);
		copyAction.setTarget(text);
		return true;
	}
	IAction getCopyAction() {
		return copyAction;
	}
	private String getHref(Object target) {
		if (target instanceof ISelectionProvider) {
			ISelectionProvider provider = (ISelectionProvider)target;
			IStructuredSelection ssel = (IStructuredSelection)provider.getSelection();
			Object obj = ssel.getFirstElement();
			if (obj instanceof ITopic) {
				ITopic topic = (ITopic)obj;
				return topic.getHref();
			}
		}
		else if (target instanceof FormText) {
			FormText text = (FormText)target;
			Object href = text.getSelectedLinkHref();
			if (href!=null)
				return href.toString();
		}
		return null;
	}

	private void doOpen(Object target) {
		String href = getHref(target);
		if (href!=null)
			showURL(href, getShowDocumentsInPlace());		
	}
	
	private void doOpen(Object target, boolean replace) {
		String href = getHref(target);
		if (href!=null)
			showURL(href, replace);
	}

	private void doOpenInHelp(Object target) {
		String href = getHref(target);
		if (href!=null)
			//WorkbenchHelp.displayHelpResource(href);
			showURL(href, false);
	}
	/**
	 * @return Returns the statusLineManager.
	 */
	public IStatusLineManager getStatusLineManager() {
		return statusLineManager;
	}
	/**
	 * @return Returns the showDocumentsInPlace.
	 */
	public boolean getShowDocumentsInPlace() {
		return showDocumentsInPlace;
	}
	/**
	 * @param showDocumentsInPlace The showDocumentsInPlace to set.
	 */
	public void setShowDocumentsInPlace(boolean showDocumentsInPlace) {
		this.showDocumentsInPlace = showDocumentsInPlace;
	}
	/**
	 * @return Returns the style.
	 */
	public int getStyle() {
		return style;
	}
	public int getNumberOfInPlaceHits() {
		return numberOfInPlaceHits;
	}
	public void setNumberOfInPlaceHits(int numberOfInPlaceHits) {
		this.numberOfInPlaceHits = numberOfInPlaceHits;
	}
	void handleLinkEntered(HyperlinkEvent e) {
		IStatusLineManager mng = getRoot(getStatusLineManager());
		if (mng!=null) {
			String label = e.getLabel();
			String href = (String)e.getHref();
			if (href!=null && href.startsWith("__"))
				href = null;
			if (href!=null)
				href = href.replaceAll("&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
			if (label!=null && href!=null) {
				String message = HelpUIResources.getString("ReusableHelpPart.status", label, href); //$NON-NLS-1$
				mng.setMessage(message);
			}
			else if (label!=null)
				mng.setMessage(label);
			else
				mng.setMessage(href);
		}
	}
	
	private IStatusLineManager getRoot(IStatusLineManager mng) {
		while (mng!=null) {
			if (mng instanceof SubStatusLineManager) {
				SubStatusLineManager smng = (SubStatusLineManager)mng;
				IContributionManager parent = smng.getParent();
				if (parent==null)
					return smng;
				if (!(parent instanceof IStatusLineManager))
					return smng;
				mng = (IStatusLineManager)parent;
			}
			else
				break;
		}
		return mng;
	}

	void handleLinkExited(HyperlinkEvent e) {
		IStatusLineManager mng = getRoot(getStatusLineManager());
		if (mng!=null)
			mng.setMessage(null);
	}
	
	String escapeSpecialChars(String value) {
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
			case (int)160:
				buf.append(" ");
				break;
			default:
				buf.append(c);
				break;
			}
		}
		return buf.toString();
	}	
}