/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;
import java.io.*;
import java.util.Vector;

import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.internal.browser.BrowserManager;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.model.ModelObject;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.forms.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.pages.*;
import org.eclipse.update.internal.ui.preferences.MainPreferencePage;
import org.eclipse.update.internal.ui.search.*;
import org.eclipse.update.ui.forms.internal.engine.FormEngine;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class DetailsView extends MultiPageView {

	// NL keys
	private static final String KEY_HOME = "DetailsView.home.label";
	private static final String KEY_BACKWARD = "DetailsView.backward.label";
	private static final String KEY_FORWARD = "DetailsView.forward.label";
	private static final String KEY_T_HOME = "DetailsView.home.tooltip";
	private static final String KEY_T_BACKWARD = "DetailsView.backward.tooltip";
	private static final String KEY_T_FORWARD = "DetailsView.forward.tooltip";

	public static final String HOME_PAGE = "Home";
	public static final String SITE_PAGE = "Site";
	public static final String CATEGORY_PAGE = "Category";
	public static final String DETAILS_PAGE = "Details";
	public static final String BROWSER_PAGE = "Browser";
	public static final String CONFIG_PAGE = "Config";
	public static final String INSTALL_CONFIGURATION_PAGE =
		"InstallConfiguration";
	public static final String INSTALL_SITE_PAGE = "InstallSite";
	public static final String MY_COMPUTER_PAGE = "MyComputer";
	public static final String EXTENSION_ROOT_PAGE = "ExtensionRoot";
	public static final String SEARCH_PAGE = "Search";
	public static final String DISCOVERY_PAGE = "Discovery";
	public static final String UNKNOWN_PAGE = "Unknown";
	public static final String MULTIPLE_SELECTION_PAGE = "MultipleSelection";

	private Action homeAction;
	private UpdateAction backAction;
	private UpdateAction forwardAction;
	private DetailsHistory history = new DetailsHistory();
	private Vector tmpFiles;

	private boolean inHistory = false;

	abstract class UpdateAction extends Action implements IUpdate {
	}

	/**
	 * The constructor.
	 */
	public DetailsView() {
		history = new DetailsHistory();
	}

	public void createPages() {
		firstPageId = HOME_PAGE;
		formWorkbook.setFirstPageSelected(false);
		MainPage mainPage = new MainPage(this, "Update Home");
		addPage(HOME_PAGE, mainPage);
		DetailsPage detailsPage = new DetailsPage(this, "Details");
		addPage(DETAILS_PAGE, detailsPage);
		SitePage sitePage = new SitePage(this, "Site");
		addPage(SITE_PAGE, sitePage);
		addPage(CATEGORY_PAGE, new CategoryPage(this, "Category"));
		addPage(CONFIG_PAGE, new LocalSitePage(this, "Configuration"));
		addPage(
			INSTALL_CONFIGURATION_PAGE,
			new InstallConfigurationPage(this, "Snapshot"));
		addPage(
			INSTALL_SITE_PAGE,
			new InstallableSitePage(this, "Install Location"));
		addPage(MY_COMPUTER_PAGE, new MyComputerPage(this, "MyComputer"));
		addPage(
			EXTENSION_ROOT_PAGE,
			new ExtensionRootPage(this, "ExtensionRoot"));
		addPage(SEARCH_PAGE, new SearchPage(this, "Search"));
		addPage(
			DISCOVERY_PAGE,
			new DiscoveryFolderPage(this, "Discovery Sites"));
		addPage(MULTIPLE_SELECTION_PAGE, new MultipleSelectionPage(this, "Multiple Selection"));
		addPage(UNKNOWN_PAGE, new UnknownObjectPage(this, "Unknown Object"));
	}
	
	public static boolean showURL(String url) {
		return showURL(url, true);
	}

	public static boolean showURL(String url, boolean considerEmbedded) {
		boolean useEmbedded = false;
		boolean focusGrabbed = false;
		boolean win32 = SWT.getPlatform().equals("win32");
		
		url = WebInstallHandler.getEncodedURLName(url);
		if (win32 && considerEmbedded) {
			useEmbedded = MainPreferencePage.getUseEmbeddedBrowser();
		}
		if (useEmbedded) {
			IWorkbenchPage page = UpdateUI.getActivePage();
			try {
				//IViewPart part = page.findView(UpdatePerspective.ID_BROWSER);
				//if (part == null) {
					IViewPart part = page.showView(UpdatePerspective.ID_BROWSER);
					focusGrabbed = true;
				//} else
					//page.bringToTop(part);
				((IEmbeddedWebBrowser) part).openTo(url);
			} catch (PartInitException e) {
				UpdateUI.logException(e);
			}
		} else {
			if (win32) {
				Program.launch(url);
			} else {
				// defect 11483
				IBrowser browser = BrowserManager.getInstance().createBrowser();
				try {
					browser.displayURL(url);
				}
				catch (Exception e) {
					UpdateUI.logException(e);
				}
			}
		}
		return focusGrabbed;
	}

	public void showText(String text) {
		try {
			File file = File.createTempFile("FeatureLicense", ".txt");
			OutputStream stream = new FileOutputStream(file);
			PrintWriter writer = new PrintWriter(stream);
			writer.println(text);
			writer.flush();
			stream.close();
			if (tmpFiles == null)
				tmpFiles = new Vector();
			tmpFiles.add(file.getAbsolutePath());
			showURL("file:///" + file.getAbsolutePath());
		} catch (IOException e) {
			UpdateUI.logException(e);
		}
	}

	public void dispose() {
		if (tmpFiles != null) {
			for (int i = 0; i < tmpFiles.size(); i++) {
				String fileName = tmpFiles.get(i).toString();
				File file = new File(fileName);
				file.delete();
			}
			tmpFiles = null;
		}
		super.dispose();
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		history.add(HOME_PAGE, null);
		makeActions();
		fillActionBars();
		WorkbenchHelp.setHelp(
			formWorkbook.getControl(),
			"org.eclipse.update.ui.DetailsView");
	}

	public void showPageWithInput(IWorkbenchPart part, String pageId, Object input) {
		if (pageId.equals(HOME_PAGE) == false) {
			if (input!=null && !(input instanceof UIModelObject
				|| input instanceof ModelObject 
				|| input instanceof IStructuredSelection))
				return;
		}
		showPage(pageId, input);
		if (input != null)
			history.add(pageId, input);
		backAction.update();
		forwardAction.update();
		IWorkbenchPage page = UpdateUI.getActivePage();
		if (page == null)
			return;
		IPerspectiveDescriptor desc = page.getPerspective();
		if (desc == null)
			return;
		String pid = desc.getId();
		if (pid.equals("org.eclipse.update.ui.UpdatePerspective")) {
			IViewPart view = page.findView(UpdatePerspective.ID_DETAILS);
			if (view != null)
				page.bringToTop(view);
			else {
				try {
					page.showView(UpdatePerspective.ID_DETAILS);
				}
				catch (PartInitException e) {
					UpdateUI.logException(e);
				}
				// Restore focus to the selection provider
				if (part!=null) part.setFocus();
			}
		}
	}

	private void showWebBookmark(final IWorkbenchPart part, final SiteBookmark bookmark) {
		BusyIndicator
			.showWhile(formWorkbook.getControl().getDisplay(), new Runnable() {
			public void run() {
				boolean focusGrabbed = showURL(bookmark.getURL().toString());
				if (focusGrabbed)
					part.setFocus();
			}
		});
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (part == this)
			return;
		if (part.getSite().getId().equals(UpdatePerspective.ID_BROWSER))
			return;
		if (part instanceof SearchResultView) {
			SearchResultView searchResult = (SearchResultView)part;
			if (searchResult.isSelectionActive()==false)
				return;
		}
		Object el = null;
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) sel;
			if (ssel.size() == 1) {
				el = ssel.getFirstElement();
				if (el instanceof IFeature || el instanceof IFeatureAdapter) {
					showPageWithInput(part, DETAILS_PAGE, el);
					return;
				}
				if (el instanceof SiteBookmark) {
					//SiteBookmark bookmark = (SiteBookmark) el;
					//if (!bookmark.isWebBookmark())
						showPageWithInput(part, SITE_PAGE, el);
					//else
						//showWebBookmark(part, bookmark);
					return;
				}
				if (el instanceof SiteCategory) {
					showPageWithInput(part, CATEGORY_PAGE, el);
					return;
				}
				if (el instanceof ILocalSite) {
					showPageWithInput(part, CONFIG_PAGE, el);
					return;
				}
				if (el instanceof IInstallConfiguration
					|| el instanceof PreservedConfiguration) {
					showPageWithInput(part, INSTALL_CONFIGURATION_PAGE, el);
					return;
				}
				if (el instanceof IConfiguredSiteAdapter) {
					showPageWithInput(part, INSTALL_SITE_PAGE, el);
					return;
				}
				if (el instanceof MyComputer) {
					showPageWithInput(part, MY_COMPUTER_PAGE, el);
					return;
				}
				if (el instanceof ExtensionRoot) {
					showPageWithInput(part, EXTENSION_ROOT_PAGE, el);
					return;
				}
				if (el instanceof DiscoveryFolder) {
					showPageWithInput(part, DISCOVERY_PAGE, el);
					return;
				}
				if (el instanceof SearchObject) {
					showPageWithInput(part, SEARCH_PAGE, el);
					return;
				}
				if (el instanceof SearchResultSite) {
					showPageWithInput(part, SITE_PAGE, el);
					return;
				}
				//fallback - show empty page
				showPageWithInput(part, UNKNOWN_PAGE, el);
			} else if (ssel.size()>1) {
				showPageWithInput(part, MULTIPLE_SELECTION_PAGE, ssel);
			} else
				// defect 14692
				showPageWithInput(part, 
					/*(homeAction != null) ? HOME_PAGE :*/ UNKNOWN_PAGE,
					null);
		}
	}

	private void makeActions() {
		homeAction = new Action() {
			public void run() {
				performHome();
			}
		};
		homeAction.setText(UpdateUI.getString(KEY_HOME));
		homeAction.setToolTipText(UpdateUI.getString(KEY_T_HOME));
		homeAction.setImageDescriptor(UpdateUIImages.DESC_HOME_NAV);
		homeAction.setHoverImageDescriptor(
			UpdateUIImages.DESC_HOME_NAV_H);
		homeAction.setDisabledImageDescriptor(
			UpdateUIImages.DESC_HOME_NAV_D);
		WorkbenchHelp.setHelp(
			homeAction,
			"org.eclipse.update.ui.DetailsView_homeAction");

		backAction = new UpdateAction() {
			public void run() {
				performBackward();
			}
			public void update() {
				setEnabled(canPerformBackward());
			}
		};
		backAction.setText(UpdateUI.getString(KEY_BACKWARD));
		backAction.setToolTipText(
			UpdateUI.getString(KEY_T_BACKWARD));
		backAction.setImageDescriptor(UpdateUIImages.DESC_BACKWARD_NAV);
		backAction.setHoverImageDescriptor(
			UpdateUIImages.DESC_BACKWARD_NAV_H);
		backAction.setDisabledImageDescriptor(
			UpdateUIImages.DESC_BACKWARD_NAV_D);
		backAction.setEnabled(false);
		WorkbenchHelp.setHelp(
			backAction,
			"org.eclipse.update.ui.DetailsView_backAction");

		forwardAction = new UpdateAction() {
			public void run() {
				performForward();
			}
			public void update() {
				setEnabled(canPerformForward());
			}
		};
		forwardAction.setText(UpdateUI.getString(KEY_FORWARD));
		forwardAction.setToolTipText(
			UpdateUI.getString(KEY_T_FORWARD));
		forwardAction.setImageDescriptor(UpdateUIImages.DESC_FORWARD_NAV);
		forwardAction.setHoverImageDescriptor(
			UpdateUIImages.DESC_FORWARD_NAV_H);
		forwardAction.setDisabledImageDescriptor(
			UpdateUIImages.DESC_FORWARD_NAV_D);
		forwardAction.setEnabled(false);
		WorkbenchHelp.setHelp(
			forwardAction,
			"org.eclipse.update.ui.DetailsView_forwardAction");
	}

	private void fillActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager mng = bars.getToolBarManager();
		mng.add(homeAction);
		mng.add(backAction);
		mng.add(forwardAction);
	}

	public void contextMenuAboutToShow(IMenuManager menu) {
		Control control =
			formWorkbook.getControl().getDisplay().getFocusControl();
		if (control instanceof FormEngine) {
			((FormEngine) control).contextMenuAboutToShow(menu);
		}

		menu.add(backAction);
		menu.add(forwardAction);
		menu.add(homeAction);
	}

	private void performHome() {
		showPageWithInput(null, HOME_PAGE, null);
	}

	private boolean canPerformBackward() {
		return history.hasPrevious();
	}

	private void performBackward() {
		DetailsHistoryItem item = history.getPrevious();
		if (item != null) {
			inHistory = true;
			showPage(item.getPageId(), item.getInput());
			//inHistory = false;
			if (item.getPageId() != BROWSER_PAGE) {
				backAction.update();
				forwardAction.update();
			}
		}
	}

	private void performForward() {
		DetailsHistoryItem item = history.getNext();
		if (item != null) {
			inHistory = true;
			showPage(item.getPageId(), item.getInput());
			//inHistory = false;
			if (item.getPageId() != BROWSER_PAGE) {
				backAction.update();
				forwardAction.update();
			}
		}
	}

	private boolean canPerformForward() {
		return history.hasNext();
	}

}
