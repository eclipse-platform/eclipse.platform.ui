/*
 * Created on Jun 18, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.webbrowser;

import org.eclipse.jface.action.*;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.welcome.internal.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WebBrowserEditorContributor
	extends EditorActionBarContributor
	implements IWebBrowserListener {
	private static final String KEY_NOT_AVAILABLE =
		"WebBrowserView.notAvailable";
	private static final String KEY_HOME = "WebBrowserView.home";
	private static final String KEY_BACKWARD = "WebBrowserView.backward";
	private static final String KEY_FORWARD = "WebBrowserView.forward";

	private Action homeAction;
	private Action backwardAction;
	private Action forwardAction;
	private WebBrowserEditor editor;
	
	public WebBrowserEditorContributor() {
		makeActions();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.welcome.internal.IWebBrowserListener#stateChanged()
	 */
	public void stateChanged() {
		updateActions();
	}

	private void makeActions() {
		homeAction = new Action() {
			public void run() {
				WebBrowserEditorInput input = (WebBrowserEditorInput)editor.getEditorInput();
				editor.openTo(input.getURL());
			}
		};
		homeAction.setEnabled(true);
		homeAction.setToolTipText(WelcomePortal.getString(KEY_HOME));
		homeAction.setImageDescriptor(
			WelcomePortalImages.DESC_HOME_NAV);
		homeAction.setDisabledImageDescriptor(
			WelcomePortalImages.DESC_HOME_NAV_D);
		homeAction.setHoverImageDescriptor(
			WelcomePortalImages.DESC_HOME_NAV_H);


		backwardAction = new Action() {
			public void run() {
				editor.back();
			}
		};
		backwardAction.setEnabled(false);
		backwardAction.setToolTipText(WelcomePortal.getString(KEY_BACKWARD));
		backwardAction.setImageDescriptor(
			WelcomePortalImages.DESC_BACKWARD_NAV);
		backwardAction.setDisabledImageDescriptor(
			WelcomePortalImages.DESC_BACKWARD_NAV_D);
		backwardAction.setHoverImageDescriptor(
			WelcomePortalImages.DESC_BACKWARD_NAV_H);

		forwardAction = new Action() {
			public void run() {
				editor.forward();
			}
		};
		forwardAction.setToolTipText(WelcomePortal.getString(KEY_FORWARD));
		forwardAction.setImageDescriptor(WelcomePortalImages.DESC_FORWARD_NAV);
		forwardAction.setDisabledImageDescriptor(
			WelcomePortalImages.DESC_FORWARD_NAV_D);
		forwardAction.setHoverImageDescriptor(
			WelcomePortalImages.DESC_FORWARD_NAV_H);
		forwardAction.setEnabled(false);
	}

	public void contributeToToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(new Separator());
		toolBarManager.add(homeAction);
		toolBarManager.add(backwardAction);
		toolBarManager.add(forwardAction);
	}
	public void setActiveEditor(IEditorPart targetEditor) {
		if (this.editor != null)
			this.editor.setListener(null);
		editor = (WebBrowserEditor) targetEditor;
		if (editor != null) {
			editor.setListener(this);
			updateActions();
		}
	}
	private void updateActions() {
		backwardAction.setEnabled(editor.isBackwardEnabled());
		forwardAction.setEnabled(editor.isForwardEnabled());
	}
}
