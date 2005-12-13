/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.protocols.HelpURLConnection;
import org.eclipse.help.internal.search.federated.IndexerJob;
import org.eclipse.help.search.ISearchEngine2;
import org.eclipse.help.ui.internal.HelpUIPlugin;
import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class ReusableHelpPart implements IHelpUIConstants,
		IActivityManagerListener {
	public static final int ALL_TOPICS = 1 << 1;

	public static final int CONTEXT_HELP = 1 << 2;

	public static final int SEARCH = 1 << 3;

	public static final int BOOKMARKS = 1 << 4;

	public static final Collator SHARED_COLLATOR = Collator.getInstance();

	private static final String PROMPT_KEY = "askShowAll"; //$NON-NLS-1$

	private static final int STATE_START = 1;

	private static final int STATE_LT = 2;

	private static final int STATE_LT_B = 3;

	private static final int STATE_LT_BR = 4;

	private RoleFilter roleFilter;

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

	private OpenHrefAction bookmarkAction;

	private Action showAllAction;

	private ReusableHelpPartHistory history;

	private HelpPartPage currentPage;

	private int style;

	private IMemento memento;

	private boolean showDocumentsInPlace = true;

	private int numberOfInPlaceHits = 8;

	private IRunnableContext runnableContext;

	private IToolBarManager toolBarManager;

	private IStatusLineManager statusLineManager;

	private IActionBars actionBars;
	
	private EngineDescriptorManager engineManager;

	private abstract class BusyRunAction extends Action {
		public BusyRunAction(String name) {
			super(name);
		}

		public void run() {
			BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
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

	private class CopyAction extends Action implements FocusListener,
			SelectionListener {
		private FormText target;

		public CopyAction() {
			super("copy"); //$NON-NLS-1$
		}

		public void hook(final FormText text) {
			text.addFocusListener(this);
		}

		public void unhook(FormText text) {
			text.removeFocusListener(this);
			if (target == text)
				setTarget(null);
		}

		public void focusGained(FocusEvent e) {
			FormText text = (FormText) e.widget;
			text.addSelectionListener(this);
			setTarget(text);
		}

		public void focusLost(FocusEvent e) {
			FormText text = (FormText) e.widget;
			text.removeSelectionListener(this);
			setTarget(null);
		}

		public void setTarget(FormText target) {
			this.target = target;
			updateState();
		}

		private void updateState() {
			setEnabled(target != null && target.canCopy());
		}

		public void run() {
			if (target != null)
				target.copy();
		}

		public void widgetSelected(SelectionEvent e) {
			FormText text = (FormText) e.widget;
			if (text == target) {
				updateState();
			}
		}

		public void widgetDefaultSelected(SelectionEvent e) {
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

		private String iconId;

		Action pageAction;

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
			if (ReusableHelpPart.this.actionBars != null) {
				bars = new SubActionBars(ReusableHelpPart.this.actionBars);
				toolBarManager = bars.getToolBarManager();
			} else
				toolBarManager = new SubToolBarManager(
						ReusableHelpPart.this.toolBarManager);
		}

		public HelpPartPage(String id, String text, String iconId) {
			this(id, text);
			this.iconId = iconId;
		}

		public void dispose() {
			if (bars != null) {
				bars.dispose();
				bars = null;
				toolBarManager = null;
			} else
				((SubToolBarManager) toolBarManager).disposeManager();
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

		public String getIconId() {
			return iconId;
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
					// Try to create a browser and watch
					// for 'no-handle' error - it means
					// that the embedded browser is not
					// available
					try {
						createRecPart(rec);
						rec.part.setVisible(false);
					} catch (SWTError error) {
						// cannot create a browser
						return false;
					}
				}
			}
			return true;
		}

		public void stop() {
			for (int i = 0; i < partRecs.size(); i++) {
				PartRec rec = (PartRec) partRecs.get(i);
				if (rec.part!=null)
					rec.part.stop();
			}
		}
		
		public void saveState(IMemento memento) {
			for (int i = 0; i < partRecs.size(); i++) {
				PartRec rec = (PartRec) partRecs.get(i);
				if (rec.part!=null)
					rec.part.saveState(memento);
			}
		}

		public void toggleRoleFilter() {
			for (int i = 0; i < partRecs.size(); i++) {
				PartRec rec = (PartRec) partRecs.get(i);
				if (rec.part != null)
					rec.part.toggleRoleFilter();
			}
		}

		public void refilter() {
			for (int i = 0; i < partRecs.size(); i++) {
				PartRec rec = (PartRec) partRecs.get(i);
				if (rec.part != null)
					rec.part.refilter();
			}
		}

		public void setVisible(boolean visible) {
			if (bars != null)
				bars.clearGlobalActionHandlers();
			ArrayList tabList = new ArrayList();
			for (int i = 0; i < partRecs.size(); i++) {
				PartRec rec = (PartRec) partRecs.get(i);
				if (visible) {
					createRecPart(rec);
					hookGlobalAction(ActionFactory.PRINT.getId(), rec.part);
					hookGlobalAction(ActionFactory.COPY.getId(), rec.part);
					hookGlobalAction(ActionFactory.DELETE.getId(), rec.part);
					tabList.add(rec.part.getControl());
				}
				rec.part.setVisible(visible);
			}
			Composite parent = mform.getForm().getBody();
			parent.setTabList((Control[]) tabList.toArray(new Control[tabList
					.size()]));

			if (actionBars != null) {
				actionBars.clearGlobalActionHandlers();
				if (visible) {
					Map handlers = bars.getGlobalActionHandlers();
					if (handlers != null) {
						Set keys = handlers.keySet();
						for (Iterator iter = keys.iterator(); iter.hasNext();) {
							String key = (String) iter.next();
							actionBars.setGlobalActionHandler(key,
									(IAction) handlers.get(key));
						}
					}
				}
				if (pageAction != null)
					pageAction.setChecked(visible);
			}
			if (bars != null) {
				if (visible)
					bars.activate();
				else
					bars.deactivate();
				bars.updateActionBars();
			} else {
				((SubToolBarManager) toolBarManager).setVisible(visible);
				ReusableHelpPart.this.toolBarManager.update(true);
				getControl().getParent().layout();
			}
		}

		private void hookGlobalAction(String id, IHelpPart part) {
			if (bars == null)
				return;
			IAction action = part.getGlobalAction(id);
			if (action != null)
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
			// set focus on the control that had
			// focus when this page was active
			if (focusControl != null && focusControl.isDisposed())
				focusControl = null;
			if (focusControl!=null && focusControl.setFocus())
				return;
			if (partRecs.size() == 0)
				return;
			PartRec rec = (PartRec) partRecs.get(0);
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
			if (currentPage == null)
				return new Point(0, 0);
			PartRec[] parts = currentPage.getParts();
			int hmargin = currentPage.getHorizontalMargin();
			int innerWhint = wHint != SWT.DEFAULT ? wHint - 2 * hmargin : wHint;
			Point result = new Point(0, 0);
			for (int i = 0; i < parts.length; i++) {
				PartRec partRec = parts[i];
				if (!partRec.flexible) {
					Control c = partRec.part.getControl();
					Point size = c.computeSize(innerWhint, SWT.DEFAULT,
							flushCache);
					result.x = Math.max(result.x, size.x);
					result.y += size.y;
				}
				if (i < parts.length - 1)
					result.y += currentPage.getVerticalSpacing();
			}
			result.x += hmargin * 2;
			result.y += bmargin;
			return result;
		}

		protected void layout(Composite composite, boolean flushCache) {
			if (currentPage == null)
				return;

			Rectangle clientArea = composite.getClientArea();

			PartRec[] parts = currentPage.getParts();
			int hmargin = currentPage.getHorizontalMargin();
			int nfixedParts = parts.length
					- currentPage.getNumberOfFlexibleParts();
			Point[] fixedSizes = new Point[nfixedParts];
			int fixedHeight = 0;
			int index = 0;
			int innerWidth = clientArea.width - hmargin * 2;
			for (int i = 0; i < parts.length; i++) {
				PartRec partRec = parts[i];
				if (!partRec.flexible) {
					Control c = partRec.part.getControl();
					Point size = c.computeSize(innerWidth, SWT.DEFAULT, false);
					fixedSizes[index++] = size;
					if (!partRec.grabVertical)
						fixedHeight += size.y;
				}
				if (i < parts.length - 1)
					fixedHeight += currentPage.getVerticalSpacing();
			}
			fixedHeight += bmargin;
			int flexHeight = clientArea.height - fixedHeight;
			int flexPortion = 0;
			if (currentPage.getNumberOfFlexibleParts() > 0)
				flexPortion = flexHeight
						/ currentPage.getNumberOfFlexibleParts();

			int usedFlexHeight = 0;
			int y = 0;
			index = 0;
			int nflexParts = 0;
			for (int i = 0; i < parts.length; i++) {
				PartRec partRec = parts[i];
				Control c = partRec.part.getControl();

				if (partRec.flexible) {
					int height;
					if (++nflexParts == currentPage.getNumberOfFlexibleParts())
						height = flexHeight - usedFlexHeight;
					else {
						height = flexPortion;
						usedFlexHeight += height;
					}
					c.setBounds(0, y, clientArea.width, height);
				} else {
					Point fixedSize = fixedSizes[index++];
					if (fixedSize.y < flexHeight && partRec.grabVertical)
						c.setBounds(hmargin, y, innerWidth, flexHeight);
					else
						c.setBounds(hmargin, y, innerWidth, fixedSize.y);
				}
				if (i < parts.length - 1)
					y += c.getSize().y + currentPage.getVerticalSpacing();
			}
		}
	}

	class RoleFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			IHelpResource res = (IHelpResource) element;
			String href = res.getHref();
			if (href == null)
				return true;
			return HelpBasePlugin.getActivitySupport().isEnabled(href);
		}
	}

	public ReusableHelpPart(IRunnableContext runnableContext) {
		this(runnableContext, CONTEXT_HELP | SEARCH | ALL_TOPICS | BOOKMARKS);
	}

	public ReusableHelpPart(IRunnableContext runnableContext, int style) {
		this.runnableContext = runnableContext;
		history = new ReusableHelpPartHistory();
		this.style = style;
		ensureHelpIndexed();
		PlatformUI.getWorkbench().getActivitySupport().getActivityManager()
				.addActivityManagerListener(this);
	}

	private void ensureHelpIndexed() {
		// make sure we have the index but
		// don't schedule the indexer job if one is
		// already running
		Job[] jobs = Platform.getJobManager().find(IndexerJob.FAMILY);
		if (jobs.length == 0) {
			IndexerJob indexerJob = new IndexerJob();
			indexerJob.schedule();
		}
	}

	private void definePages() {
		pages = new ArrayList();
		// federated search page
		HelpPartPage page = new HelpPartPage(HV_FSEARCH_PAGE,
				Messages.ReusableHelpPart_searchPage_name,
				IHelpUIConstants.IMAGE_HELP_SEARCH);
		page.setVerticalSpacing(0);
		page.addPart(HV_FSEARCH, false);
		page.addPart(HV_FSEARCH_RESULT, true);
		page.addPart(HV_SEE_ALSO, false);
		pages.add(page);

		// all topics page
		page = new HelpPartPage(HV_ALL_TOPICS_PAGE,
				Messages.ReusableHelpPart_allTopicsPage_name,
				IHelpUIConstants.IMAGE_ALL_TOPICS); 
		page.setVerticalSpacing(0);
		page.setHorizontalMargin(0);
		page.addPart(HV_TOPIC_TREE, true);
		page.addPart(HV_SEE_ALSO, false);
		pages.add(page);

		// bookmarks page
		page = new HelpPartPage(HV_BOOKMARKS_PAGE,
				Messages.ReusableHelpPart_bookmarksPage_name, 
				IHelpUIConstants.IMAGE_BOOKMARKS); 
		page.setVerticalSpacing(0);
		page.setHorizontalMargin(0);
		page.addPart(HV_BOOKMARKS_TREE, true);
		page.addPart(HV_SEE_ALSO, false);
		pages.add(page);
		// browser page
		page = new HelpPartPage(HV_BROWSER_PAGE, null);
		page.setVerticalSpacing(0);
		page.addPart(HV_BROWSER, true);
		page.addPart(HV_SEE_ALSO, false);
		pages.add(page);

		// context help page
		page = new HelpPartPage(HV_CONTEXT_HELP_PAGE,
				Messages.ReusableHelpPart_contextHelpPage_name,
				IHelpUIConstants.IMAGE_RELATED_TOPICS); 
		// page.addPart(HV_CONTEXT_HELP, false);
		// page.addPart(HV_SEARCH_RESULT, false, true);
		page.setVerticalSpacing(0);
		page.setHorizontalMargin(0);
		page.addPart(HV_RELATED_TOPICS, true);
		page.addPart(HV_SEE_ALSO, false);
		pages.add(page);
	}

	public void init(IActionBars bars, IToolBarManager toolBarManager,
			IStatusLineManager statusLineManager, IMemento memento) {
		this.memento = memento;
		this.actionBars = bars;
		this.toolBarManager = toolBarManager;
		this.statusLineManager = statusLineManager;
		definePages();
		makeActions();
	}

	private void makeActions() {
		backAction = new Action("back") { //$NON-NLS-1$
			public void run() {
				doBack();
			}
		};
		backAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_BACK));
		backAction.setDisabledImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_BACK_DISABLED));
		backAction.setEnabled(false);
		backAction.setText(Messages.ReusableHelpPart_back_label); 
		backAction.setToolTipText(Messages.ReusableHelpPart_back_tooltip); 
		backAction.setId("back"); //$NON-NLS-1$

		nextAction = new Action("next") { //$NON-NLS-1$
			public void run() {
				doNext();
			}
		};
		nextAction.setText(Messages.ReusableHelpPart_forward_label); 
		nextAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_FORWARD));
		nextAction.setDisabledImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_FORWARD_DISABLED));
		nextAction.setEnabled(false);
		nextAction.setToolTipText(Messages.ReusableHelpPart_forward_tooltip); 
		nextAction.setId("next"); //$NON-NLS-1$
		toolBarManager.add(backAction);
		toolBarManager.add(nextAction);

		openInfoCenterAction = new BusyRunAction("openInfoCenter") { //$NON-NLS-1$
			protected void busyRun() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp();
			}
		};
		openInfoCenterAction
				.setText(Messages.ReusableHelpPart_openInfoCenterAction_label); 
		openAction = new OpenHrefAction("open") { //$NON-NLS-1$
			protected void busyRun() {
				doOpen(getTarget(), getShowDocumentsInPlace());
			}
		};
		openAction.setText(Messages.ReusableHelpPart_openAction_label); 
		openInHelpAction = new OpenHrefAction("") {//$NON-NLS-1$
			protected void busyRun() {
				doOpenInHelp(getTarget());
			}
		};
		openInHelpAction
				.setText(Messages.ReusableHelpPart_openInHelpContentsAction_label); 
		copyAction = new CopyAction();
		copyAction.setText(Messages.ReusableHelpPart_copyAction_label); 
		bookmarkAction = new OpenHrefAction("bookmark") { //$NON-NLS-1$
			protected void busyRun() {
				doBookmark(getTarget());
			}
		};
		bookmarkAction.setText(Messages.ReusableHelpPart_bookmarkAction_label); 
		bookmarkAction.setImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_ADD_BOOKMARK));
		if (actionBars != null && actionBars.getMenuManager() != null)
			contributeToDropDownMenu(actionBars.getMenuManager());
		roleFilter = new RoleFilter();
		if (HelpBasePlugin.getActivitySupport().isUserCanToggleFiltering()) {
			showAllAction = new Action() {
				public void run() {
					BusyIndicator.showWhile(getControl().getDisplay(),
							new Runnable() {
								public void run() {
									toggleShowAll(showAllAction.isChecked());
								}
							});
				}
			};
			showAllAction.setImageDescriptor(HelpUIResources
					.getImageDescriptor(IHelpUIConstants.IMAGE_SHOW_ALL));
			showAllAction
					.setToolTipText(Messages.AllTopicsPart_showAll_tooltip); 
			toolBarManager.insertBefore("back", showAllAction); //$NON-NLS-1$
			toolBarManager.insertBefore("back", new Separator()); //$NON-NLS-1$
			showAllAction.setChecked(!HelpBasePlugin.getActivitySupport()
					.isFilteringEnabled());
		}
	}

	ViewerFilter getRoleFilter() {
		return roleFilter;
	}

	public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
		for (int i = 0; i < pages.size(); i++) {
			HelpPartPage page = (HelpPartPage) pages.get(i);
			page.refilter();
		}
	}

	boolean isFilteredByRoles() {
		return HelpBasePlugin.getActivitySupport().isFilteringEnabled();
	}

	private void doBack() {
		String id = getCurrentPageId();
		if (id.equals(IHelpUIConstants.HV_BROWSER_PAGE)) {
			// stop the browser
			BrowserPart part = (BrowserPart) findPart(IHelpUIConstants.HV_BROWSER);
			part.stop();
		}
		HistoryEntry entry = history.prev();
		if (entry != null)
			executeHistoryEntry(entry);
	}

	private void doNext() {
		HistoryEntry entry = history.next();
		if (entry != null)
			executeHistoryEntry(entry);
	}

	private void executeHistoryEntry(HistoryEntry entry) {
		history.setBlocked(true);
		if (entry.getType() == HistoryEntry.PAGE) {
			showPage(entry.getTarget(), true);
			mform.setInput(entry.getData());
		} else if (entry.getType() == HistoryEntry.URL) {
			String relativeUrl = (String) entry.getData();
			showURL(relativeUrl != null ? relativeUrl : entry.getTarget(), true);
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
		if (currentPage != null && currentPage.getId().equals(id))
			return currentPage;
		HelpPartPage page = findPage(id);
		if (page != null) {
			boolean success = flipPages(currentPage, page);
			return success ? page : null;
		}
		return null;
	}

	public HelpPartPage showPage(String id, boolean setFocus) {
		HelpPartPage page = this.showPage(id);
		if (page != null && setFocus)
			page.setFocus();
		return page;
	}

	public void startSearch(String phrase) {
		showPage(IHelpUIConstants.HV_FSEARCH_PAGE, true);
		SearchPart part = (SearchPart) findPart(IHelpUIConstants.HV_FSEARCH);
		if (part != null && phrase != null)
			part.startSearch(phrase);
	}

	public void showDynamicHelp(IWorkbenchPart wpart, Control c) {
		showPage(IHelpUIConstants.HV_CONTEXT_HELP_PAGE, true);
		RelatedTopicsPart part = (RelatedTopicsPart) findPart(IHelpUIConstants.HV_RELATED_TOPICS);
		if (part != null) {
			part.handleActivation(c, wpart);
		}
	}

	private boolean flipPages(HelpPartPage oldPage, HelpPartPage newPage) {
		if (newPage.canOpen() == false)
			return false;
		if (oldPage != null) {
			oldPage.stop();
			oldPage.setVisible(false);
		}
		mform.getForm().setText(newPage.getText());
		newPage.setVisible(true);
		toolBarManager.update(true);
		currentPage = newPage;
		if (mform.isStale())
			mform.refresh();
		mform.getForm().getBody().layout(true);
		mform.reflow(true);
		if (newPage.getId().equals(IHelpUIConstants.HV_BROWSER_PAGE) == false) {
			if (!history.isBlocked()) {
				history.addEntry(new HistoryEntry(HistoryEntry.PAGE, newPage
						.getId(), null));
			}
			updateNavigation();
		}
		return true;
	}

	/*
	 * void addPageHistoryEntry(String id, Object data) { if
	 * (!history.isBlocked()) { history.addEntry(new
	 * HistoryEntry(HistoryEntry.PAGE, id, data)); } updateNavigation(); }
	 */
	public HelpPartPage getCurrentPage() {
		return currentPage;
	}

	public String getCurrentPageId() {
		return currentPage != null ? currentPage.getId() : null;
	}

	void browserChanged(String url) {
		if (!history.isBlocked()) {
			try {
				history.addEntry(new HistoryEntry(HistoryEntry.URL, url,
						BaseHelpSystem.unresolve(new URL(url))));
			} catch (MalformedURLException e) {
				// TODO handle this
			}
		}
		updateNavigation();
	}

	private void updateNavigation() {
		backAction.setEnabled(history.hasPrev());
		nextAction.setEnabled(history.hasNext());
		history.setBlocked(false);
	}

	public boolean isMonitoringContextHelp() {
		return currentPage != null
				&& currentPage.getId().equals(HV_CONTEXT_HELP_PAGE);
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
		for (int i = 0; i < pages.size(); i++) {
			HelpPartPage page = (HelpPartPage) pages.get(i);
			page.dispose();
		}
		pages = null;
		if (mform != null) {
			mform.dispose();
			mform = null;
		}
		PlatformUI.getWorkbench().getActivitySupport().getActivityManager()
				.removeActivityManagerListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#setFocus()
	 */
	public void setFocus() {
		if (currentPage != null)
			currentPage.setFocus();
		else
			mform.setFocus();
	}

	public void update(IWorkbenchPart part, Control control) {
		mform.setInput(new ContextHelpProviderInput(null, null, control, part));
	}

	public void update(IContextProvider provider, IContext context, IWorkbenchPart part,
			Control control) {
		mform.setInput(new ContextHelpProviderInput(provider, context, control, part));
	}

	public void update(IContext context, IWorkbenchPart part, Control control) {
		mform.setInput(new ContextHelpProviderInput(null, context, control, part));
	}

	private IHelpPart createPart(String id, IToolBarManager tbm) {
		IHelpPart part = null;
		Composite parent = mform.getForm().getBody();

		part = findPart(id);
		if (part != null)
			return part;

		if (id.equals(HV_TOPIC_TREE)) {
			part = new AllTopicsPart(parent, mform.getToolkit(), tbm);
		} else if (id.equals(HV_CONTEXT_HELP)) {
			part = new ContextHelpPart(parent, mform.getToolkit());
			((ContextHelpPart) part)
					.setDefaultText(getDefaultContextHelpText());
		} else if (id.equals(HV_RELATED_TOPICS)) {
			part = new RelatedTopicsPart(parent, mform.getToolkit());
			((RelatedTopicsPart) part)
					.setDefaultText(getDefaultContextHelpText());
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
		} else if (id.equals(HV_BOOKMARKS_TREE)) {
			part = new BookmarksPart(parent, mform.getToolkit(), tbm);
		}
		if (part != null) {
			mform.addPart(part);			
			part.init(this, id, memento);
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
	 * @param defaultContextHelpText
	 *            The defaultContextHelpText to set.
	 */
	public void setDefaultContextHelpText(String defaultContextHelpText) {
		this.defaultContextHelpText = defaultContextHelpText;
	}

	public void showURL(final String url) {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				showURL(url, getShowDocumentsInPlace());
			}
		});
	}

	public void showURL(String url, boolean replace) {
		if (url == null)
			return;
		if (url.startsWith("nw:")) { //$NON-NLS-1$
			replace = false;
			url = url.substring(3);
		}
		else if (url.startsWith("open:")) { //$NON-NLS-1$
			int col = url.indexOf(':');
			int qloc = url.indexOf('?');
			String engineId = url.substring(col+1, qloc);
			EngineDescriptor desc = getEngineManager().findEngine(engineId);
			if (desc==null)
				return;
			HashMap args = new HashMap();
			HelpURLConnection.parseQuery(url.substring(qloc+1), args);
			((ISearchEngine2)desc.getEngine()).open((String)args.get("id")); //$NON-NLS-1$
			return;
		}
		if (replace) {
			if (openInternalBrowser(url))
				return;
		}
		showExternalURL(url);
	}

	private boolean openInternalBrowser(String url) {
		Preferences pref = HelpBasePlugin.getDefault().getPluginPreferences();
		boolean openInEditor = pref
				.getBoolean(IHelpBaseConstants.P_KEY_OPEN_IN_EDITOR);
		if (openInEditor)
			return showInWorkbenchBrowser(url, true);
		showPage(IHelpUIConstants.HV_BROWSER_PAGE);
		BrowserPart bpart = (BrowserPart) findPart(IHelpUIConstants.HV_BROWSER);
		if (bpart != null) {
			bpart.showURL(BaseHelpSystem
					.resolve(url, "/help/ntopic").toString()); //$NON-NLS-1$
			return true;
		}
		return false;
	}

	private boolean showInWorkbenchBrowser(String url, boolean onlyInternal) {
		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
				.getBrowserSupport();
		if (!onlyInternal || support.isInternalWebBrowserAvailable()) {
			try {
				IWebBrowser browser = support
						.createBrowser(
								IWorkbenchBrowserSupport.AS_EDITOR
										| IWorkbenchBrowserSupport.NAVIGATION_BAR
										| IWorkbenchBrowserSupport.STATUS,
								"org.eclipse.help.ui", Messages.ReusableHelpPart_internalBrowserTitle, url); //$NON-NLS-1$
				browser.openURL(BaseHelpSystem.resolve(url, "/help/nftopic")); //$NON-NLS-1$
				return true;
			} catch (PartInitException e) {
				HelpUIPlugin.logError(
						Messages.ReusableHelpPart_internalWebBrowserError, e);
			}
		}
		return false;
	}

	public void showExternalURL(String url) {
		if (isHelpResource(url))
			PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(url);
		else {
			try {
				String aurl = BaseHelpSystem.resolve(url, true).toString();
				if (aurl.endsWith("&noframes=true") || aurl.endsWith("?noframes=true")) //$NON-NLS-1$ //$NON-NLS-2$
					aurl = aurl.substring(0, aurl.length() - 14);
				showInWorkbenchBrowser(aurl, false);
			} catch (Exception e) {
				HelpUIPlugin.logError("Error opening browser", e); //$NON-NLS-1$
			}
		}
	}

	public IHelpPart findPart(String id) {
		if (mform == null)
			return null;
		IFormPart[] parts = mform.getParts();
		for (int i = 0; i < parts.length; i++) {
			IHelpPart part = (IHelpPart) parts[i];
			if (part.getId().equals(id))
				return part;
		}
		return null;
	}

	public boolean isHelpResource(String url) {
		if (url == null || url.indexOf("://") == -1) //$NON-NLS-1$
			return true;
		return false;
	}

	private void contextMenuAboutToShow(IMenuManager manager) {
		IFormPart[] parts = mform.getParts();
		boolean hasContext = false;
		Control focusControl = getControl().getDisplay().getFocusControl();
		for (int i = 0; i < parts.length; i++) {
			IHelpPart part = (IHelpPart) parts[i];
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

	private void contributeToDropDownMenu(IMenuManager manager) {
		addPageAction(manager, IHelpUIConstants.HV_CONTEXT_HELP_PAGE);
		addPageAction(manager, IHelpUIConstants.HV_ALL_TOPICS_PAGE);
		addPageAction(manager, IHelpUIConstants.HV_FSEARCH_PAGE);
		addPageAction(manager, IHelpUIConstants.HV_BOOKMARKS_PAGE);
	}

	private void addPageAction(IMenuManager manager, final String pageId) {
		// String cid = getCurrentPageId();
		HelpPartPage page = findPage(pageId);
		if (page == null)
			return;
		Action action = new Action(pageId, IAction.AS_CHECK_BOX) {
			public void run() {
				BusyIndicator.showWhile(mform.getForm().getDisplay(),
						new Runnable() {
							public void run() {
								showPage(pageId);
							}
						});
			}
		};
		action.setText(page.getText());
		String iconId = page.getIconId();
		if (iconId != null)
			action.setImageDescriptor(HelpUIResources
					.getImageDescriptor(iconId));
		manager.add(action);
		page.pageAction = action;
	}

	private HelpPartPage findPage(String id) {
		for (int i = 0; i < pages.size(); i++) {
			HelpPartPage page = (HelpPartPage) pages.get(i);
			if (page.getId().equals(id)) {
				return page;
			}
		}
		return null;
	}

	boolean fillSelectionProviderMenu(ISelectionProvider provider,
			IMenuManager manager, boolean addBookmarks) {
		boolean value = fillOpenActions(provider, manager);
		if (value && addBookmarks) {
			manager.add(new Separator());
			bookmarkAction.setTarget(provider);
			manager.add(bookmarkAction);
		}
		return true;
	}

	private boolean fillOpenActions(Object target, IMenuManager manager) {
		String href = getHref(target);
		if (href != null && !href.startsWith("__")) { //$NON-NLS-1$
			openAction.setTarget(target);
			openInHelpAction.setTarget(target);
			manager.add(openAction);
			if (!href.startsWith("nw:")) //$NON-NLS-1$
				manager.add(openInHelpAction);
			return true;
		}
		return false;
	}

	void hookFormText(FormText text) {
		copyAction.hook(text);
	}

	void unhookFormText(FormText text) {
		copyAction.unhook(text);
	}

	boolean fillFormContextMenu(FormText text, IMenuManager manager) {
		if (fillOpenActions(text, manager))
			manager.add(new Separator());
		manager.add(copyAction);
		copyAction.setTarget(text);
		if (text.getSelectedLinkHref() != null) {
			manager.add(new Separator());
			manager.add(bookmarkAction);
			bookmarkAction.setTarget(text);
		}
		return true;
	}

	IAction getCopyAction() {
		return copyAction;
	}

	private String getHref(Object target) {
		if (target instanceof ISelectionProvider) {
			ISelectionProvider provider = (ISelectionProvider) target;
			IStructuredSelection ssel = (IStructuredSelection) provider
					.getSelection();
			Object obj = ssel.getFirstElement();
			if (obj instanceof IToc)
				return null;
			if (obj instanceof IHelpResource) {
				IHelpResource res = (IHelpResource) obj;
				return res.getHref();
			}
		} else if (target instanceof FormText) {
			FormText text = (FormText) target;
			Object href = text.getSelectedLinkHref();
			if (href != null)
				return href.toString();
		}
		return null;
	}

	private IHelpResource getResource(Object target) {
		if (target instanceof ISelectionProvider) {
			ISelectionProvider provider = (ISelectionProvider) target;
			IStructuredSelection ssel = (IStructuredSelection) provider
					.getSelection();
			Object obj = ssel.getFirstElement();
			if (obj instanceof ITopic) {
				return (ITopic) obj;
			}
		} else if (target instanceof FormText) {
			FormText text = (FormText) target;
			final Object href = text.getSelectedLinkHref();
			final String label = text.getSelectedLinkText();
			if (href != null) {
				return new IHelpResource() {
					public String getHref() {
						return href.toString();
					}

					public String getLabel() {
						return label;
					}
				};
			}
		}
		return null;
	}

	private void doBookmark(Object target) {
		IHelpResource res = getResource(target);
		if (res != null) {
			BaseHelpSystem.getBookmarkManager().addBookmark(res.getHref(),
					res.getLabel());
		}
	}

	/*
	 * private void doOpen(Object target) { String href = getHref(target); if
	 * (href != null) showURL(href, getShowDocumentsInPlace()); }
	 */

	private void doOpen(Object target, boolean replace) {
		String href = getHref(target);
		if (href != null)
			showURL(href, replace);
	}

	private void doOpenInHelp(Object target) {
		String href = getHref(target);
		if (href != null)
			// WorkbenchHelp.displayHelpResource(href);
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
	 * @param showDocumentsInPlace
	 *            The showDocumentsInPlace to set.
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
		if (mng != null) {
			String label = e.getLabel();
			String href = (String) e.getHref();
			if (href != null && href.startsWith("__")) //$NON-NLS-1$
				href = null;
			if (href != null) {
				try {
					href = URLDecoder.decode(href, "UTF-8"); //$NON-NLS-1$
				} catch (UnsupportedEncodingException ex) {
				}
				href = href.replaceAll("&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (label != null && href != null) {
				String message = NLS.bind(Messages.ReusableHelpPart_status,
						label, href);
				mng.setMessage(message);
			} else if (label != null)
				mng.setMessage(label);
			else
				mng.setMessage(href);
		}
	}

	private IStatusLineManager getRoot(IStatusLineManager mng) {
		while (mng != null) {
			if (mng instanceof SubStatusLineManager) {
				SubStatusLineManager smng = (SubStatusLineManager) mng;
				IContributionManager parent = smng.getParent();
				if (parent == null)
					return smng;
				if (!(parent instanceof IStatusLineManager))
					return smng;
				mng = (IStatusLineManager) parent;
			} else
				break;
		}
		return mng;
	}

	void handleLinkExited(HyperlinkEvent e) {
		IStatusLineManager mng = getRoot(getStatusLineManager());
		if (mng != null)
			mng.setMessage(null);
	}

	String escapeSpecialChars(String value) {
		return escapeSpecialChars(value, false);
	}

	String escapeSpecialChars(String value, boolean leaveBold) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);

			switch (c) {
			case '&':
				buf.append("&amp;"); //$NON-NLS-1$
				break;
			case '<':
				if (leaveBold) {
					int length = value.length();
					if (i +  6 < length) {
						String tag = value.substring(i, i+7);
						if (tag.equalsIgnoreCase("</code>")) { //$NON-NLS-1$
							buf.append("</span>"); //$NON-NLS-1$
							i+= 6;
							continue;
						}
					}
					if (i +  5 < length) {
						String tag = value.substring(i, i+6);
						if (tag.equalsIgnoreCase("<code>")) { //$NON-NLS-1$
							buf.append("<span font=\"code\">"); //$NON-NLS-1$
							i+= 5;
							continue;
						}
					}
					if (i + 3 < length) {
						String tag = value.substring(i, i + 4);
						if (tag.equalsIgnoreCase("</b>")) { //$NON-NLS-1$
							buf.append(tag);
							i += 3;
							continue;
						}
						if (tag.equalsIgnoreCase("<br>")) { //$NON-NLS-1$
							buf.append("<br/>"); //$NON-NLS-1$
							i+= 3;
							continue;
						}					
					}
					if (i + 2 < length) {
						String tag = value.substring(i, i + 3);
						if (tag.equalsIgnoreCase("<b>")) { //$NON-NLS-1$
							buf.append(tag);
							i += 2;
							continue;
						}
					}
				}
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
			case 160:
				buf.append(" "); //$NON-NLS-1$
				break;
			default:
				buf.append(c);
				break;
			}
		}
		return buf.toString();
	}

	private void toggleShowAll(boolean checked) {
		if (checked) {
			IPreferenceStore store = HelpUIPlugin.getDefault()
					.getPreferenceStore();
			String value = store.getString(PROMPT_KEY);
			if (value.length() == 0) {
				MessageDialogWithToggle dialog = MessageDialogWithToggle
						.openOkCancelConfirm(null,
								Messages.AskShowAll_dialogTitle,
								getShowAllMessage(),
								Messages.AskShowAll_toggleMessage, false,
								store, PROMPT_KEY);
				if (dialog.getReturnCode() != MessageDialogWithToggle.OK) {
					showAllAction.setChecked(false);
					return;
				}
			}
		}
		HelpBasePlugin.getActivitySupport().setFilteringEnabled(!checked);
		for (int i = 0; i < pages.size(); i++) {
			HelpPartPage page = (HelpPartPage) pages.get(i);
			page.toggleRoleFilter();
		}
	}
	
	public void saveState(IMemento memento) {
		for (int i = 0; i < pages.size(); i++) {
			HelpPartPage page = (HelpPartPage) pages.get(i);
			page.saveState(memento);
		}
	}

	private String getShowAllMessage() {
		String message = HelpBasePlugin.getActivitySupport()
				.getShowAllMessage();
		if (message == null)
			return Messages.AskShowAll_message;
		StringBuffer buff = new StringBuffer();
		int state = STATE_START;

		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);
			switch (state) {
			case STATE_START:
				if (c == '<')
					state = STATE_LT;
				else
					buff.append(c);
				break;
			case STATE_LT:
				if (c == 'b' || c == 'B')
					state = STATE_LT_B;
				break;
			case STATE_LT_B:
				if (c == 'r' || c == 'R')
					state = STATE_LT_BR;
				break;
			case STATE_LT_BR:
				if (c == '>') {
					buff.append('\n');
				}
				state = STATE_START;
				break;
			default:
				buff.append(c);
			}
		}
		return buff.toString();
	}
	
	EngineDescriptorManager getEngineManager() {
		if (engineManager==null) {
			engineManager = new EngineDescriptorManager();
		}
		return engineManager;
	}
}

