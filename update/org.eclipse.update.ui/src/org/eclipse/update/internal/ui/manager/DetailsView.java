package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.swt.SWT;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.update.core.*;
import org.eclipse.update.ui.model.*;
import org.eclipse.jface.action.*;
import org.eclipse.update.internal.ui.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class DetailsView extends MultiPageView {
public static final String HOME_PAGE = "Home";
public static final String DETAILS_PAGE = "Details";

private Action homeAction;
private Action backAction;
private Action forwardAction;

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
	}
	
public void createPartControl(Composite parent) {
	super.createPartControl(parent);
	makeActions();
	fillActionBars();
}
	
public void selectionChanged(IWorkbenchPart part, ISelection sel) {
	if (part == this) return;
	if (sel instanceof IStructuredSelection) {
		IStructuredSelection ssel = (IStructuredSelection)sel;
		if (ssel.size()==1) {
			Object el = ssel.getFirstElement();
			if (el instanceof IFeature || el instanceof ChecklistJob) {
				showPage(DETAILS_PAGE, el);
				return;
			}
		}
	}
	/*
	if (getCurrentPage() != getPage(HOME_PAGE)) 
		// go back to the home page
		showPage(HOME_PAGE);
	*/
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
	
	backAction = new Action () {
		public void run() {
			performBackward();
		}
	};
	backAction.setToolTipText("Go Back");
	backAction.setImageDescriptor(UpdateUIPluginImages.DESC_BACKWARD_NAV);
	backAction.setHoverImageDescriptor(UpdateUIPluginImages.DESC_BACKWARD_NAV_H);
	backAction.setDisabledImageDescriptor(UpdateUIPluginImages.DESC_BACKWARD_NAV_D);
	
	forwardAction = new Action () {
		public void run() {
			performForward();
		}
	};
	forwardAction.setToolTipText("Go Forward");
	forwardAction.setImageDescriptor(UpdateUIPluginImages.DESC_FORWARD_NAV);
	forwardAction.setHoverImageDescriptor(UpdateUIPluginImages.DESC_FORWARD_NAV_H);
	forwardAction.setDisabledImageDescriptor(UpdateUIPluginImages.DESC_FORWARD_NAV_D);
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

private void performBackward() {
}

private void performForward() {
}

}