/*
 * Created on Oct 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import java.util.ArrayList;

import org.eclipse.help.ITopic;
import org.eclipse.help.internal.appserver.WebappManager;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ILayoutExtension;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ReusableHelpPart implements IHelpViewConstants {
	private ManagedForm mform;
	private int verticalSpacing = 15;
	private int hmargin = 5;
	private int bmargin = 5;
	private String defaultContextHelpText;

	private ArrayList pages;
	private Action backAction;
	private Action nextAction;
	private Action openInfoCenterAction;
	private OpenHrefAction openAction;
	private OpenHrefAction openInHelpAction;
	private ReusableHelpPartHistory history;
	private HelpPartPage currentPage;

	private IRunnableContext runnableContext;

	private IToolBarManager toolBarManager;
	private IStatusLineManager statusLineManager;
	
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

	private static class PartRec {
		String id;
		boolean flexible;
		IHelpPart part;

		PartRec(String id, boolean flexible) {
			this.id = id;
			this.flexible = flexible;
		}
	}

	private class HelpPartPage {
		private String id;
		private int vspacing = verticalSpacing;
		private int horizontalMargin = 0;

		private String text;
		private SubToolBarManager toolBarManager;
		private ArrayList partRecs;
		private int nflexible;

		public HelpPartPage(String id, String text) {
			this.id = id;
			this.text = text;
			partRecs = new ArrayList();
			toolBarManager = new SubToolBarManager(ReusableHelpPart.this.toolBarManager);
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
			partRecs.add(new PartRec(id, flexible));
			if (flexible)
				nflexible++;
		}

		public PartRec[] getParts() {
			return (PartRec[]) partRecs.toArray(new PartRec[partRecs.size()]);
		}
		
		public int getNumberOfFlexibleParts() {
			return nflexible;
			
		}

		public void setVisible(boolean visible) {
			for (int i = 0; i < partRecs.size(); i++) {
				PartRec rec = (PartRec) partRecs.get(i);
				if (visible) {
					if (rec.part == null)
						rec.part = createPart(rec.id);
				}
				rec.part.setVisible(visible);
				toolBarManager.setVisible(visible);
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
					c.setBounds(hmargin, y, innerWidth, fixedSize.y);
				}
				if (i<parts.length-1)
					y += c.getSize().y + currentPage.getVerticalSpacing();
			}
		}
	}

	public ReusableHelpPart(IRunnableContext runnableContext) {
		this.runnableContext = runnableContext;
		history = new ReusableHelpPartHistory();
	}

	private void definePages() {
		pages = new ArrayList();
		// search page
		HelpPartPage page = new HelpPartPage(SEARCH_PAGE, "Find Help");
		page.addPart(SEARCH, false);
		page.addPart(SEARCH_RESULT, true);
		page.addPart(SEE_ALSO, false);
		pages.add(page);
		// all topics page
		page = new HelpPartPage(ALL_TOPICS_PAGE, "All Topics");
		page.setVerticalSpacing(0);
		page.setHorizontalMargin(0);		
		page.addPart(TOPIC_TREE, true);
		page.addPart(SEE_ALSO, false);
		pages.add(page);
		// browser page
		page = new HelpPartPage(BROWSER_PAGE, null);
		page.setVerticalSpacing(0);
		page.addPart(BROWSER, true);
		page.addPart(SEE_ALSO, false);
		pages.add(page);
		// context help page
		page = new HelpPartPage(CONTEXT_HELP_PAGE, "Context Help");
		page.addPart(CONTEXT_HELP, false);
		page.addPart(SEARCH, false);
		page.addPart(SEARCH_RESULT, true);
		page.addPart(SEE_ALSO, false);
		pages.add(page);
	}

	public void init(IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {
		this.toolBarManager = toolBarManager;
		this.statusLineManager = statusLineManager;
		makeActions();
		definePages();
	}
	private void makeActions() {
		backAction = new Action("back") {
			public void run() {
				doBack();
			}
		};
		backAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		backAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
		backAction.setEnabled(false);
		backAction.setText("&Back");
		backAction.setToolTipText("Back");
		
		nextAction = new Action("next") {
			public void run() {
				doNext();
			}
		};
		nextAction.setText("&Forward");
		nextAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		nextAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));
		nextAction.setEnabled(false);
		nextAction.setToolTipText("Forward");
		toolBarManager.add(backAction);
		toolBarManager.add(nextAction);
		
		openInfoCenterAction = new BusyRunAction("openInfoCenter") {
			protected void busyRun() {
				WorkbenchHelp.displayHelp();
			}
		};
		openInfoCenterAction.setText("&Help Contents");
		openAction = new OpenHrefAction("open") {
			protected void busyRun() {
				doOpen(getTarget());
			}
		};
		openAction.setText("&Open");
		openInHelpAction = new OpenHrefAction("openInHelp") {
			protected void busyRun() {
				doOpenInHelp(getTarget());
			}
		};
		openInHelpAction.setText("Open in Help &Contents");
	}
	
	private void doBack() {
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
		if (entry.getType()==HistoryEntry.PAGE)
			showPage(entry.getData());
		else if (entry.getType()==HistoryEntry.URL)
			showURL(entry.getData());
		updateNavigation();
	}

	public void createControl(Composite parent, FormToolkit toolkit) {
		ScrolledForm form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new HelpPartLayout());
		mform = new ManagedForm(toolkit, form);
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
				flipPages(currentPage, page);
				return page;
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

	private void flipPages(HelpPartPage oldPage, HelpPartPage newPage) {
		if (oldPage!=null)
			oldPage.setVisible(false);
		newPage.setVisible(true);
		currentPage = newPage;		
		mform.getForm().setText(newPage.getText());	
		mform.getForm().getBody().layout(true);
		mform.reflow(true);
		if (newPage.getId().equals(IHelpViewConstants.BROWSER_PAGE)==false) {
			if (!history.isBlocked()) {
				history.addEntry(new HistoryEntry(HistoryEntry.PAGE, newPage.getId()));
				updateNavigation();
				history.setBlocked(false);
			}
		}
	}

	void browserChanged(String url) {
		if (!history.isBlocked()) {
			history.addEntry(new HistoryEntry(HistoryEntry.URL, url));
			updateNavigation();
		}
		history.setBlocked(false);
	}
	
	private void updateNavigation() {
		backAction.setEnabled(history.hasPrev());
		nextAction.setEnabled(history.hasNext());
	}

	public boolean isMonitoringContextHelp() {
		return currentPage!=null && currentPage.getId().equals(CONTEXT_HELP_PAGE);
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

	public void update(Control control) {
		mform.setInput(control);
	}
	
	public void update(IContextHelpProvider provider, Control control) {
		mform.setInput(new ContextHelpProviderInput(provider, control));
	}

	private IHelpPart createPart(String id) {
		IHelpPart part = null;
		Composite parent = mform.getForm().getBody();
		
		part = findPart(id);
		if (part!=null)
			return part;

		if (id.equals(SEARCH)) {
			part = new SearchPart(parent, mform.getToolkit());
		} else if (id.equals(TOPIC_TREE)) {
			part = new AllTopicsPart(parent, mform.getToolkit());
		} else if (id.equals(CONTEXT_HELP)) {
			part = new ContextHelpPart(parent, mform.getToolkit());
			((ContextHelpPart)part).setDefaultText(getDefaultContextHelpText());
		} else if (id.equals(BROWSER)) {
			part = new BrowserPart(parent, mform.getToolkit());
		} else if (id.equals(SEARCH_RESULT)) {
			part = new SearchResultsPart(parent, mform.getToolkit());
		} else if (id.equals(SEE_ALSO)) {
			part = new SeeAlsoPart(parent, mform.getToolkit());
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
	public void showURL(String url) {
		if (url==null) return;
		showPage(IHelpViewConstants.BROWSER_PAGE);
		BrowserPart bpart = (BrowserPart)findPart(IHelpViewConstants.BROWSER);
		if (bpart!=null) {
			bpart.showURL(toAbsoluteURL(url));
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
	private String toAbsoluteURL(String url) {
		if (url==null || url.indexOf("://")!= -1)
			return url;
		//TODO need to expose API in help that does exactly what this method does
		BaseHelpSystem.ensureWebappRunning();
		String base = "http://" //$NON-NLS-1$
				+ WebappManager.getHost() + ":" //$NON-NLS-1$
				+ WebappManager.getPort() + "/help/topic"; //$NON-NLS-1$
		char sep = url.lastIndexOf('?')!= -1 ? '&':'?';
		return base + url+sep+"noframes=true";
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
	private void fillOpenActions(Object target, IMenuManager manager) {
		openAction.setTarget(target);
		openInHelpAction.setTarget(target);
		manager.add(openAction);
		manager.add(openInHelpAction);
	}
	boolean fillFormContextMenu(FormText text, IMenuManager manager) {
		fillOpenActions(text, manager);
		return true;
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
			showURL(href);
	}

	private void doOpenInHelp(Object target) {
		String href = getHref(target);
		if (href!=null)
			WorkbenchHelp.displayHelpResource(href);
	}
	/**
	 * @return Returns the statusLineManager.
	 */
	public IStatusLineManager getStatusLineManager() {
		return statusLineManager;
	}
}