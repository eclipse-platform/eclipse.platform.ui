package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.update.core.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.jface.action.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.program.Program;
import org.eclipse.update.internal.transform.*;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class DetailsView extends MultiPageView {
public static final String HOME_PAGE = "Home";
public static final String DETAILS_PAGE = "Details";
public static final String BROWSER_PAGE = "Browser";
public static final String HOME_URL = "update://index.html";

private Action homeAction;
private UpdateAction backAction;
private UpdateAction forwardAction;

abstract class UpdateAction extends Action implements IUpdate {
}

	/**
	 * The constructor.
	 */
	public DetailsView() {
	}
	
public void createPages() {
	firstPageId = HOME_PAGE;
	formWorkbook.setFirstPageSelected(false);
	MainPage mainPage =
		new MainPage(this, "Update Home");
	addPage(HOME_PAGE, mainPage);
	DetailsPage detailsPage = 
		new DetailsPage(this, "Details");
		addPage(DETAILS_PAGE, detailsPage);
	if (SWT.getPlatform().equals("win32")) {
		EmbeddedBrowser browser = 
			new EmbeddedBrowser(this);
	   	addPage(BROWSER_PAGE, browser);
	}
}
	
public void showURL(String url) {
	if (SWT.getPlatform().equals("win32")) {
		showPage(BROWSER_PAGE, url);
	}
	else {
		Program.launch(url);
	}
}

public void createPartControl(Composite parent) {
	super.createPartControl(parent);
	makeActions();
	fillActionBars();
}

private void showDetails(Object el) {
	showPage(DETAILS_PAGE, el);
}

private void showTransformedPage(Object el) {
	TransformManager tm = UpdateUIPlugin.getDefault().getTransformManager();
	String transURL = tm.getTransformedURL(el);
	if (transURL!=null) {
	   EmbeddedBrowser browser = 
  	 		(EmbeddedBrowser)getPage(BROWSER_PAGE);
  	   browser.setInput(el);
	   showPage(BROWSER_PAGE, transURL);
	}
}
	
public void selectionChanged(IWorkbenchPart part, ISelection sel) {
	if (part == this) return;
	if (sel instanceof IStructuredSelection) {
		IStructuredSelection ssel = (IStructuredSelection)sel;
		if (ssel.size()==1) {
			Object el = ssel.getFirstElement();
			if (el instanceof IFeature || el instanceof ChecklistJob) {
				showDetails(el);
				return;
			}
		}
	}
}

private void makeActions() {
	homeAction = new Action () {
		public void run() {
			performHome();
		}
	};
	homeAction.setToolTipText("Go to the Update Manager home page");
	homeAction.setImageDescriptor(UpdateUIPluginImages.DESC_HOME_NAV);
	homeAction.setHoverImageDescriptor(UpdateUIPluginImages.DESC_HOME_NAV_H);
	homeAction.setDisabledImageDescriptor(UpdateUIPluginImages.DESC_HOME_NAV_D);
	
	backAction = new UpdateAction () {
		public void run() {
			performBackward();
		}
		public void update() {
			setEnabled(canPeformBackward());
		}
	};
	backAction.setToolTipText("Go Back");
	backAction.setImageDescriptor(UpdateUIPluginImages.DESC_BACKWARD_NAV);
	backAction.setHoverImageDescriptor(UpdateUIPluginImages.DESC_BACKWARD_NAV_H);
	backAction.setDisabledImageDescriptor(UpdateUIPluginImages.DESC_BACKWARD_NAV_D);
	backAction.setEnabled(false);
	
	forwardAction = new UpdateAction () {
		public void run() {
			performForward();
		}
		public void update() {
			setEnabled(canPerformForward());
		}
	};
	forwardAction.setToolTipText("Go Forward");
	forwardAction.setImageDescriptor(UpdateUIPluginImages.DESC_FORWARD_NAV);
	forwardAction.setHoverImageDescriptor(UpdateUIPluginImages.DESC_FORWARD_NAV_H);
	forwardAction.setDisabledImageDescriptor(UpdateUIPluginImages.DESC_FORWARD_NAV_D);
	forwardAction.setEnabled(false);
/*	
	if (SWT.getPlatform().equals("win32")) {
		EmbeddedBrowser browser = 
  	 		(EmbeddedBrowser)getPage(BROWSER_PAGE);
  	 	browser.addUpdate(backAction);
  	 	browser.addUpdate(forwardAction);
	}
*/
}

private void fillActionBars() {
	IActionBars bars = getViewSite().getActionBars();
	IToolBarManager mng = bars.getToolBarManager();
	mng.add(homeAction);
	mng.add(backAction);
	mng.add(forwardAction);
}

private void performHome() {
   	showPage(HOME_PAGE);
}

private boolean canPeformBackward() {
	if (SWT.getPlatform().equals("win32")) {
		EmbeddedBrowser browser = 
  	 		(EmbeddedBrowser)getPage(BROWSER_PAGE);
  	 	return browser.canPerformBackward();
	}
	else return false;
}
	

private void performBackward() {
	if (SWT.getPlatform().equals("win32")) {
		EmbeddedBrowser browser = 
  	 		(EmbeddedBrowser)getPage(BROWSER_PAGE);
  	 	browser.performBackward();
	}
}

private void performForward() {
	if (SWT.getPlatform().equals("win32")) {
		EmbeddedBrowser browser = 
  	 		(EmbeddedBrowser)getPage(BROWSER_PAGE);
  	 	browser.performForward();
	}
}

private boolean canPerformForward() {
	if (SWT.getPlatform().equals("win32")) {
		EmbeddedBrowser browser = 
  	 		(EmbeddedBrowser)getPage(BROWSER_PAGE);
  	 	return browser.canPerformForward();
	}
	else return false;
}

}