/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.win32;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.WorkbenchHelpPlugin;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.ui.win32.WebBrowser;
import org.eclipse.help.internal.util.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
public class IEHost {
	public static final String BROWSER_X = "browser.x";
	public static final String BROWSER_Y = "browser.y";
	public static final String BROWSER_WIDTH = "browser.w";
	public static final String BROWSER_HEIGTH = "browser.h";
	private static final String IMAGE_SHELL = "browser.shell";
	private static final String IMAGE_BACK = "browser.back";
	private static final String IMAGE_FORWARD = "browser.forward";
	private static final String IMAGE_PRINT = "browser.print";
	private static ImageRegistry imgRegistry = null;
	Shell shell;
	WebBrowser webBrowser;
	boolean opened = false;
	public IEHost() {
		super();
		if (imgRegistry == null) {
			imgRegistry = WorkbenchHelpPlugin.getDefault().getImageRegistry();
			imgRegistry.put(
				IMAGE_SHELL,
				ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("shellIcon")));
			imgRegistry.put(
				IMAGE_BACK,
				ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("back_icon")));
			imgRegistry.put(
				IMAGE_FORWARD,
				ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("forward_icon")));
			imgRegistry.put(
				IMAGE_PRINT,
				ImageDescriptor.createFromURL(WorkbenchResources.getImagePath("printer_icon")));
		}
		// Create and configure Shell
		shell = new Shell();
		shell.setImage(imgRegistry.get(IMAGE_SHELL));
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				HelpPreferences pref = HelpSystem.getPreferences();
				Point location = shell.getLocation();
				pref.put(IEHost.BROWSER_X, Integer.toString(location.x));
				;
				pref.put(IEHost.BROWSER_Y, Integer.toString(location.y));
				Point size = shell.getSize();
				pref.put(IEHost.BROWSER_WIDTH, Integer.toString(size.x));
				pref.put(IEHost.BROWSER_HEIGTH, Integer.toString(size.y));
				IEHost.this.shell.close();
			}
		});
		shell.setText(WorkbenchResources.getString("browserTitle"));
		GridData data = new GridData(GridData.FILL_BOTH);
		shell.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		shell.setLayout(layout);
		createContents(shell);
	}
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		// Add a toolbar
		ToolBar bar = new ToolBar(composite, SWT.FLAT);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		//gridData.horizontalSpan = 3;
		bar.setLayoutData(gridData);
		ToolItem item;
		// Add a button to navigate back
		item = new ToolItem(bar, SWT.NONE);
		item.setToolTipText(WorkbenchResources.getString("Previous_page"));
		item.setImage(imgRegistry.get(IMAGE_BACK));
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				webBrowser.back();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		// Add a button to navigate forward
		item = new ToolItem(bar, SWT.NONE);
		item.setToolTipText(WorkbenchResources.getString("Next_page"));
		item.setImage(imgRegistry.get(IMAGE_FORWARD));
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				webBrowser.forward();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		// Add a button to print
		item = new ToolItem(bar, SWT.NONE);
		item.setToolTipText(WorkbenchResources.getString("Print_page"));
		item.setImage(imgRegistry.get(IMAGE_PRINT));
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				webBrowser.print(true);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		try {
			webBrowser = new WebBrowser(composite);
		} catch (HelpWorkbenchException hwe) {
			Logger.logError(WorkbenchResources.getString("WE001"), hwe);
		}
		return composite;
	}
	/*
	* @see IBrowser#displayURL(String)
	*/
	public void displayURL(String url) {
		if(!opened){
			shell.open();
			opened=true;
		}
		// bring to front
		shell.setVisible(true);
		shell.setMinimized(false);
		shell.moveAbove(null);
		// e o bring to front
		webBrowser.navigate(url);
	}
	public boolean isDisposed() {
		if (shell == null)
			return false;
		return shell.isDisposed();
	}
	public void setLocation(int x, int y) {
		shell.setLocation(x, y);
	}
	public void setSize(int width, int height) {
		shell.setSize(width, height);
	}
}