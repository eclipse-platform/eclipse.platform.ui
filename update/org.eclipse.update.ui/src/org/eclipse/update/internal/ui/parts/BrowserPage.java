package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.ui.forms.internal.IFormPage;
import org.eclipse.swt.events.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.update.internal.ui.*;
import java.util.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.core.IFeature;
import org.eclipse.jface.action.*;


public class BrowserPage implements IUpdateFormPage {
// NL
private static final String KEY_ADDRESS = "BrowserPage.address";
private static final String KEY_STOP = "BrowserPage.stop";
private static final String KEY_GO = "BrowserPage.go";
private static final String KEY_REFRESH = "BrowserPage.refresh";

	private int ADDRESS_SIZE = 10;
	private WebBrowser browser;
	private Control control;
	private Combo addressCombo;
	private ToolBarManager toolBarManager;
	private Object input;
	private MultiPageView view;
	private IBrowserListener listener;
	
	public void setBrowserListener(IBrowserListener listener) {
		this.listener = listener;
	}
	
	public BrowserPage(MultiPageView view) {
		this.view = view;
	}
	
	public void setInput(Object input) {
		this.input = input;
	}
	
	public Object getInput() {
		return input;
	}
	public WebBrowser getBrowser() {
		return browser;
	}

	/**
	 * @see IUpdateFormPage#contextMenuAboutToShow(IMenuManager)
	 */
	public boolean contextMenuAboutToShow(IMenuManager manager) {
		return false;
	}

	/**
	 * @see IUpdateFormPage#getAction(String)
	 */
	public IAction getAction(String id) {
		return null;
	}

	/**
	 * @see IUpdateFormPage#openTo(Object)
	 */
	public void openTo(Object object) {
		if (object instanceof String) {
			final String url = object.toString();
			addressCombo.setText(url);
			control.getDisplay().asyncExec(new Runnable() {
				public void run() {
					navigate(url);
				}
			});
		}
	}

	/**
	 * @see IUpdateFormPage#performGlobalAction(String)
	 */
	public void performGlobalAction(String id) {
	}

	/**
	 * @see IUpdateFormPage#init(Object)
	 */
	public void init(Object model) {
	}

	/**
	 * @see IUpdateFormPage#update()
	 */
	public void update() {
	}

	/**
	 * @see IFormPage#becomesInvisible(IFormPage)
	 */
	public boolean becomesInvisible(IFormPage newPage) {
		return true;
	}

	/**
	 * @see IFormPage#becomesVisible(IFormPage)
	 */
	public void becomesVisible(IFormPage previousPage) {
	}

	/**
	 * @see IFormPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);
		Composite navContainer = new Composite(container, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 1;
		navContainer.setLayout(layout);
		createNavBar(navContainer);
		navContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		browser = new WebBrowser(container);

		Control c = browser.getControl();
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		Composite statusContainer = new Composite(container, SWT.NONE);
		statusContainer.setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));
		*/
		final BrowserControlSite site = browser.getControlSite();
		IStatusLineManager smng = getView().getViewSite().getActionBars().getStatusLineManager();
		site.setStatusLineManager(smng);

		//site.setStatusContainer(statusContainer);
		site.addEventListener(WebBrowser.DownloadComplete, new OleListener() {
			public void handleEvent(OleEvent event) {
				String url = browser.getLocationURL();
				if (url!=null) {
			   		addressCombo.setText(url);
			   		if (listener!=null)
			   		   listener.downloadComplete(url);
				}
			}
		});
		control = container;
	}
	
	private void createNavBar(Composite parent) {
		Label addressLabel = new Label(parent, SWT.NONE);
		addressLabel.setText(UpdateUIPlugin.getResourceString(KEY_ADDRESS));

		addressCombo = new Combo(parent, SWT.DROP_DOWN | SWT.BORDER);
		addressCombo.addSelectionListener(new SelectionListener () {
			public void widgetSelected(SelectionEvent e) {
				String text = addressCombo.getItem(addressCombo.getSelectionIndex());
				navigate(text);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				navigate(addressCombo.getText());
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);	
		addressCombo.setLayoutData(gd);
		ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.HORIZONTAL);
		toolBarManager = new ToolBarManager(toolbar);
		makeActions();
	}
	
	private void navigate(String url) {
		browser.navigate(url);
		String [] items = addressCombo.getItems();
		int loc = -1;
		String normURL = getNormalizedURL(url);
		for (int i=0; i<items.length; i++) {
			String normItem = getNormalizedURL(items[i]);
			if (normURL.equals(normItem)) {
				// match 
				loc = i;
				break;
			}
		}
		if (loc != -1) {
			addressCombo.remove(loc);
		}
		addressCombo.add(url, 0);
		if (addressCombo.getItemCount()>ADDRESS_SIZE) {
			addressCombo.remove(addressCombo.getItemCount()-1);
		}
	}
	
	private void makeActions() {
		Action goAction = new Action () {
			public void run() {
				navigate(addressCombo.getText());
			}
		};
		goAction.setToolTipText(UpdateUIPlugin.getResourceString(KEY_GO));
		goAction.setImageDescriptor(UpdateUIPluginImages.DESC_GO_NAV);
		goAction.setDisabledImageDescriptor(UpdateUIPluginImages.DESC_GO_NAV_D);
		goAction.setHoverImageDescriptor(UpdateUIPluginImages.DESC_GO_NAV_H);
		
		Action stopAction = new Action () {
			public void run() {
				browser.stop();
			}
		};
		stopAction.setToolTipText(UpdateUIPlugin.getResourceString(KEY_STOP));
		stopAction.setImageDescriptor(UpdateUIPluginImages.DESC_STOP_NAV);
		stopAction.setDisabledImageDescriptor(UpdateUIPluginImages.DESC_STOP_NAV_D);
		stopAction.setHoverImageDescriptor(UpdateUIPluginImages.DESC_STOP_NAV_H);

		Action refreshAction = new Action () {
			public void run() {
				browser.refresh();
			}
		};
		refreshAction.setToolTipText(UpdateUIPlugin.getResourceString(KEY_REFRESH));
		refreshAction.setImageDescriptor(UpdateUIPluginImages.DESC_REFRESH_NAV);
		refreshAction.setDisabledImageDescriptor(UpdateUIPluginImages.DESC_REFRESH_NAV_D);
		refreshAction.setHoverImageDescriptor(UpdateUIPluginImages.DESC_REFRESH_NAV_H);
		toolBarManager.add(goAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(stopAction);
		toolBarManager.add(refreshAction);
		toolBarManager.update(true);
	}
	
	private String getNormalizedURL(String url) {
		url = url.toLowerCase();
		if (url.indexOf("://")== -1) {
			url = "http://"+url;
		}
		return url;
	}
	
	public void dispose() {
		if (browser!=null) browser.dispose();
	}
	
	/**
	 * @see IFormPage#getControl()
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * @see IFormPage#getLabel()
	 */
	public String getLabel() {
		return "Web Browser";
	}

	/**
	 * @see IFormPage#getTitle()
	 */
	public String getTitle() {
		return getLabel();
	}

	/**
	 * @see IFormPage#isSource()
	 */
	public boolean isSource() {
		return false;
	}

	/**
	 * @see IFormPage#isVisible()
	 */
	public boolean isVisible() {
		return true;
	}

	/**
	 * @see IUpdateFormPage#getView()
	 */
	public MultiPageView getView() {
		return view;
	}

}

