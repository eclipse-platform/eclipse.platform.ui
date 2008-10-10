/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.ProgressMonitorPart;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

public class RefactoringWizardDialog2 extends Dialog implements IWizardContainer {

	private RefactoringWizard fWizard;
	private IWizardPage fCurrentPage;
	private IWizardPage fVisiblePage;

	private boolean fMakeNextButtonDefault;

	private PageBook fPageContainer;
	private PageBook fStatusContainer;
	private MessageBox fMessageBox;
	private ProgressMonitorPart fProgressMonitorPart;
	private int fActiveRunningOperations;

	private static final int PREVIEW_ID= IDialogConstants.CLIENT_ID + 1;

	private int fPreviewWidth;
	private int fPreviewHeight;
	private IDialogSettings fSettings;
	private boolean fHasAdditionalPages;
	private Rectangle fInitialSize;

	private static final String DIALOG_SETTINGS= "RefactoringWizard.preview"; //$NON-NLS-1$
	private static final String WIDTH= "width"; //$NON-NLS-1$
	private static final String HEIGHT= "height"; //$NON-NLS-1$

	private static class MessageBox extends Composite {
		private Label fImage;
		private Label fText;
		public MessageBox(Composite parent, int style) {
			super(parent, style);
			GridLayout layout= new GridLayout();
			layout.numColumns= 2;
			setLayout(layout);
			fImage= new Label(this, SWT.NONE);
			fImage.setImage(RefactoringPluginImages.get(RefactoringPluginImages.IMG_OBJS_REFACTORING_INFO));
			Point size= fImage.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			GridData gd= new GridData();
			gd.verticalAlignment= SWT.TOP;
			gd.widthHint= size.x;
			gd.heightHint= size.y;
			fImage.setLayoutData(gd);
			fImage.setImage(null);
			fText= new Label(this, SWT.WRAP);
			fText.setText(" \n "); //$NON-NLS-1$
			size= fText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			gd= new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint= size.y;
			gd.verticalAlignment= SWT.TOP;
			fText.setLayoutData(gd);
		}
		public void setMessage(IWizardPage page) {
			String msg= page.getErrorMessage();
			int type= IMessageProvider.ERROR;
			if (msg == null || msg.length() == 0) {
				msg= page.getMessage();
				type= IMessageProvider.NONE;
			if (msg != null && page instanceof IMessageProvider)
				type = ((IMessageProvider)page).getMessageType();
			}
			Image image= null;
			switch (type) {
				case IMessageProvider.INFORMATION:
					image= RefactoringPluginImages.get(RefactoringPluginImages.IMG_OBJS_REFACTORING_INFO);
					break;
				case IMessageProvider.WARNING:
					image= RefactoringPluginImages.get(RefactoringPluginImages.IMG_OBJS_REFACTORING_WARNING);
					break;
				case IMessageProvider.ERROR:
					image= RefactoringPluginImages.get(RefactoringPluginImages.IMG_OBJS_REFACTORING_ERROR);
					break;
			}
			if (msg == null)
				msg= ""; //$NON-NLS-1$
			fText.setText(escapeAmpersands(msg));
			if (image == null && msg.length() > 0)
				image= RefactoringPluginImages.get(RefactoringPluginImages.IMG_OBJS_REFACTORING_INFO);
			fImage.setImage(image);
		}
		private String escapeAmpersands(String message) {
			StringBuffer result= new StringBuffer();
			for (int i= 0; i < message.length(); i++) {
				char ch= message.charAt(i);
				if (ch == '&') {
					result.append('&');
				}
				result.append(ch);
			}
			return result.toString();
		}
	}

	private static class PageBook extends Composite {
		private StackLayout fLayout;
		public PageBook(Composite parent, int style) {
			super(parent, style);
			fLayout= new StackLayout();
			setLayout(fLayout);
			fLayout.marginWidth= 5; fLayout.marginHeight= 5;
		}
		public void showPage(Control page) {
			fLayout.topControl= page;
			layout();
		}
		public Control getTopPage() {
			return fLayout.topControl;
		}
	}

	public RefactoringWizardDialog2(Shell shell, RefactoringWizard wizard) {
		super(shell);
		Assert.isNotNull(wizard);
		IDialogSettings settings= wizard.getDialogSettings();
		if (settings == null) {
			settings= RefactoringUIPlugin.getDefault().getDialogSettings();
			wizard.setDialogSettings(settings);
		}
		fWizard= wizard;
		fWizard.setContainer(this);
		fWizard.addPages();
		initSize(settings);
		fHasAdditionalPages= wizard.getPageCount() > 3;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 * @since 3.4
	 */
	protected boolean isResizable() {
		return true;
	}

	private void initSize(IDialogSettings settings) {
		fSettings= settings.getSection(DIALOG_SETTINGS);
		if (fSettings == null) {
			fSettings= new DialogSettings(DIALOG_SETTINGS);
			settings.addSection(fSettings);
			fSettings.put(WIDTH, 600);
			fSettings.put(HEIGHT, 400);
		}
		fPreviewWidth= 600;
		fPreviewHeight= 400;
		try {
			fPreviewWidth= fSettings.getInt(WIDTH);
			fPreviewHeight= fSettings.getInt(HEIGHT);
		} catch (NumberFormatException e) {
		}
	}

	private void saveSize() {
		if (fCurrentPage instanceof PreviewWizardPage) {
			Control control= fCurrentPage.getControl().getParent();
			Point size = control.getSize();
			fSettings.put(WIDTH, size.x);
			fSettings.put(HEIGHT, size.y);
		}
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#getButton(int)
	 */
	protected Button getButton(int id) {
		return super.getButton(id);
	}

	//---- IRefactoringWizardDialog ------------------------------------

	public void makeNextButtonDefault() {
		fMakeNextButtonDefault= true;
	}

	//---- IWizardContainer --------------------------------------------

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void showPage(IWizardPage page) {
		fCurrentPage= page;
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void updateButtons() {
		boolean previewPage= isPreviewPageActive();
		boolean ok= fWizard.canFinish();
		boolean canFlip= fCurrentPage.canFlipToNextPage();
		Button previewButton= getButton(PREVIEW_ID);
		Button defaultButton= null;
		if (previewButton != null && !previewButton.isDisposed()) {
			previewButton.setEnabled(!previewPage);
			if (!previewPage)
				previewButton.setEnabled(canFlip);
			if (previewButton.isEnabled())
				defaultButton= previewButton;
		}
		Button nextButton= getButton(IDialogConstants.NEXT_ID);
		if (nextButton != null && !nextButton.isDisposed()) {
			nextButton.setEnabled(!previewPage);
			if (!previewPage)
				nextButton.setEnabled(canFlip);
			if (nextButton.isEnabled())
				defaultButton= nextButton;
		}
		Button backButton= getButton(IDialogConstants.BACK_ID);
		if (backButton != null && !backButton.isDisposed())
			backButton.setEnabled(!isFirstPage());
		Button okButton= getButton(IDialogConstants.OK_ID);
		if (okButton != null && !okButton.isDisposed()) {
			okButton.setEnabled(ok);
			if (ok)
				defaultButton= okButton;
		}
		if (defaultButton != null) {
			defaultButton.getShell().setDefaultButton(defaultButton);
		}
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void updateMessage() {
		if (fStatusContainer == null || fStatusContainer.isDisposed())
			return;
		fStatusContainer.showPage(fMessageBox);
		fMessageBox.setMessage(fCurrentPage);
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void updateTitleBar() {
		// we don't have a title bar.
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public void updateWindowTitle() {
		String title= fWizard.getWindowTitle();
		if (title == null)
			title= ""; //$NON-NLS-1$
		getShell().setText(title);
	}

	/* (non-Javadoc)
	 * Method declared on IWizardContainer.
	 */
	public IWizardPage getCurrentPage() {
		return fCurrentPage;
	}

	//---- IRunnableContext --------------------------------------------

	/* (non-Javadoc)
	 * Method declared on IRunnableContext
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		if (fProgressMonitorPart == null) {
			ModalContext.run(runnable, false, new NullProgressMonitor(), getShell().getDisplay());
		} else {
			Object state = null;
			if(fActiveRunningOperations == 0)
				state = aboutToStart(fork && cancelable);

			fActiveRunningOperations++;
			try {
				ModalContext.run(runnable, fork, fProgressMonitorPart, getShell().getDisplay());
			} finally {
				fActiveRunningOperations--;
				//Stop if this is the last one
				if(state!= null)
					stopped(state);
			}
		}
	}

	private Object aboutToStart(boolean cancelable) {
		Map savedState = null;
		Shell shell= getShell();
		if (shell != null) {
			// Save focus control
			Control focusControl = getShell().getDisplay().getFocusControl();
			if (focusControl != null && focusControl.getShell() != getShell())
				focusControl = null;

			Button cancelButton= getButton(IDialogConstants.CANCEL_ID);
			// Set the busy cursor to all shells.
			Display d = getShell().getDisplay();
			setDisplayCursor(d, d.getSystemCursor(SWT.CURSOR_WAIT));

			// Set the arrow cursor to the cancel component.
			cancelButton.setCursor(d.getSystemCursor(SWT.CURSOR_ARROW));

			boolean hasProgressMonitor= fProgressMonitorPart != null;

			// Deactivate shell
			savedState= saveUIState(hasProgressMonitor && cancelable);
			if (focusControl != null)
				savedState.put("focus", focusControl); //$NON-NLS-1$

			if (hasProgressMonitor) {
				fProgressMonitorPart.attachToCancelComponent(cancelButton);
				fStatusContainer.showPage(fProgressMonitorPart);
			}
			// Update the status container since we are blocking the event loop right now.
			fStatusContainer.update();
		}
		return savedState;
	}

	private Map saveUIState(boolean keepCancelEnabled) {
		Map savedState= new HashMap(10);
		saveEnableStateAndSet(getButton(PREVIEW_ID), savedState, "preview", false); //$NON-NLS-1$
		saveEnableStateAndSet(getButton(IDialogConstants.OK_ID), savedState, "ok", false); //$NON-NLS-1$
		saveEnableStateAndSet(getButton(IDialogConstants.BACK_ID), savedState, "back", false); //$NON-NLS-1$
		saveEnableStateAndSet(getButton(IDialogConstants.NEXT_ID), savedState, "next", false); //$NON-NLS-1$
		saveEnableStateAndSet(getButton(IDialogConstants.CANCEL_ID), savedState, "cancel", keepCancelEnabled); //$NON-NLS-1$
		savedState.put("page", ControlEnableState.disable(fVisiblePage.getControl())); //$NON-NLS-1$
		return savedState;
	}

	private void saveEnableStateAndSet(Control w, Map h, String key, boolean enabled) {
		if (w != null) {
			h.put(key, Boolean.valueOf(w.getEnabled()));
			w.setEnabled(enabled);
		}
	}

	private void setDisplayCursor(Display d, Cursor c) {
		Shell[] shells= d.getShells();
		for (int i= 0; i < shells.length; i++)
			shells[i].setCursor(c);
	}

	private void stopped(Object savedState) {
		Shell shell= getShell();
		if (shell != null) {
			Button cancelButton= getButton(IDialogConstants.CANCEL_ID);

			if (fProgressMonitorPart != null)
				fProgressMonitorPart.removeFromCancelComponent(cancelButton);

			fStatusContainer.showPage(fMessageBox);
			Map state = (Map)savedState;
			restoreUIState(state);

			setDisplayCursor(shell.getDisplay(), null);
			cancelButton.setCursor(null);
			Control focusControl = (Control)state.get("focus"); //$NON-NLS-1$
			if (focusControl != null)
				focusControl.setFocus();
		}
	}

	private void restoreUIState(Map state) {
		restoreEnableState(getButton(PREVIEW_ID), state, "preview");//$NON-NLS-1$
		restoreEnableState(getButton(IDialogConstants.OK_ID), state, "ok");//$NON-NLS-1$
		restoreEnableState(getButton(IDialogConstants.BACK_ID), state, "back"); //$NON-NLS-1$
		restoreEnableState(getButton(IDialogConstants.NEXT_ID), state, "next"); //$NON-NLS-1$
		restoreEnableState(getButton(IDialogConstants.CANCEL_ID), state, "cancel");//$NON-NLS-1$
		ControlEnableState pageState = (ControlEnableState) state.get("page");//$NON-NLS-1$
		pageState.restore();
	}

	private void restoreEnableState(Control w, Map h, String key) {
		if (w != null) {
			Boolean b = (Boolean) h.get(key);
			if (b != null)
				w.setEnabled(b.booleanValue());
		}
	}

	//---- Dialog -----------------------------------------------------------

	public boolean close() {
		fWizard.dispose();
		return super.close();
	}

	protected void cancelPressed() {
		if (fActiveRunningOperations == 0)	{
			if (fWizard.performCancel())
				super.cancelPressed();
		}
	}

	protected void okPressed() {
		IWizardPage current= fCurrentPage;
		saveInitialSize();
		if (fWizard.performFinish()) {
			saveSize();
			super.okPressed();
			return;
		}
		if (fCurrentPage == current)
			return;
		Assert.isTrue(IErrorWizardPage.PAGE_NAME.equals(fCurrentPage.getName()));
		if (fHasAdditionalPages) {
			// Show error page as a normal page
			showCurrentPage();
		} else if (showErrorDialog((ErrorWizardPage) fCurrentPage)) {
			// Show error page as a dialog
			if (fWizard.performFinish()) {
				super.okPressed();
				return;
			}
		}
	}

	private void showCurrentPage() {
		if (fCurrentPage.getControl() == null)
			fCurrentPage.createControl(fPageContainer);
		resize();
		makeVisible(fCurrentPage);
		updateButtons();
	}

	protected void handleShellCloseEvent() {
		if (fActiveRunningOperations == 0)	{
			if (fWizard.performCancel())
				super.handleShellCloseEvent();
		}
	}

	private boolean isPreviewPageActive() {
		return IPreviewWizardPage.PAGE_NAME.equals(fCurrentPage.getName());
	}

	private void nextOrPreviewPressed() {
		IWizardPage current= fCurrentPage;
		saveInitialSize();
		fCurrentPage= fCurrentPage.getNextPage();
		if (current == fCurrentPage)
			return;
		if (!fHasAdditionalPages && IErrorWizardPage.PAGE_NAME.equals(fCurrentPage.getName())) {
			if (showErrorDialog((ErrorWizardPage)fCurrentPage)) {
				fCurrentPage= fCurrentPage.getNextPage();
			} else {
				return;
			}
		}

		showCurrentPage();
	}

	private void saveInitialSize() {
		if (isFirstPage()) {
			// Moving away from initial page;
			// store size (may have changed any time)
			fInitialSize= getShell().getBounds();
		}
	}

	private boolean isFirstPage() {
		IWizardPage[] pages= fWizard.getPages();
		return (fCurrentPage.equals(pages[0]));
	}

	private void backPressed() {
		IWizardPage current= fCurrentPage;
		fCurrentPage= fCurrentPage.getPreviousPage();
		if (current == fCurrentPage)
			return;

		showCurrentPage();
	}

	private boolean showErrorDialog(ErrorWizardPage page) {
		RefactoringStatusDialog dialog= new RefactoringStatusDialog(getShell(), page,
			fWizard.internalShowBackButtonOnStatusDialog(InternalAPI.INSTANCE));
		switch (dialog.open()) {
			case IDialogConstants.OK_ID:
				return true;
			case IDialogConstants.BACK_ID:
				fCurrentPage= fCurrentPage.getPreviousPage();
				break;
			case IDialogConstants.CANCEL_ID:
				cancelPressed();
		}
		return false;
	}

	private void resize() {

		if (isFirstPage()) {
			getShell().setBounds(fInitialSize);
			return;
		}

		Control control= fPageContainer.getTopPage();
		Point size= control.getSize();
		int dw= Math.max(0, fPreviewWidth - size.x);
		int dh= Math.max(0, fPreviewHeight - size.y);
		int dx = dw / 2;
		int dy= dh / 2;
		Shell shell= getShell();
		Rectangle rect= shell.getBounds();
		Rectangle clientRect= shell.getMonitor().getClientArea();
		rect.x= Math.max(clientRect.x, rect.x - dx);
		rect.y= Math.max(clientRect.y, rect.y - dy);

		// limit with and height to monitor
		rect.width= Math.min(rect.width + dw, clientRect.width);
		rect.height= Math.min(rect.height + dh, clientRect.height);

		// Reposition the dialog so that it will be fully visible.
		// Normalize x and y relative to the client area.
		int xe= (rect.x - clientRect.x) + rect.width;
		if (xe > clientRect.width) {
			rect.x-= xe - clientRect.width;
		}
		int ye= (rect.y - clientRect.y) + rect.height;
		if (ye > clientRect.height) {
			rect.y-= ye - clientRect.height;
		}

		shell.setBounds(rect);
	}

	//---- UI construction ---------------------------------------------------

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String title= fWizard.getDefaultPageTitle();
		if (title == null)
			title= ""; //$NON-NLS-1$
		newShell.setText(title);
		fWizard.getRefactoring().setValidationContext(newShell);
	}

	protected Control createContents(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0; layout.marginWidth= 0;
		layout.verticalSpacing= 0; layout.horizontalSpacing= 0;
		result.setLayout(layout);
		result.setLayoutData(new GridData(GridData.FILL_BOTH));

		// initialize the dialog units
		initializeDialogUnits(result);

		fPageContainer= new PageBook(result, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_BOTH);
		fPageContainer.setLayoutData(gd);
		fCurrentPage= fWizard.getStartingPage();
		dialogArea= fPageContainer;
		if (fCurrentPage instanceof PreviewWizardPage) {
			gd.widthHint= fPreviewWidth;
			gd.heightHint= fPreviewHeight;
		}

		fStatusContainer= new PageBook(result, SWT.NONE);
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(fWizard.getMessageLineWidthInChars());
		fStatusContainer.setLayoutData(gd);
		if (fWizard.needsProgressMonitor())
			createProgressMonitorPart();
		createMessageBox();
		fStatusContainer.showPage(fMessageBox);

		buttonBar= createButtonBar(result);

		if (fCurrentPage != null) {
			fCurrentPage.createControl(fPageContainer);
			makeVisible(fCurrentPage);
			updateMessage();
			updateButtons();
		}

		applyDialogFont(result);
		return result;
	}

	private void createProgressMonitorPart() {
		// Insert a progress monitor
		GridLayout pmlayout= new GridLayout();
		pmlayout.numColumns= 1;
		pmlayout.marginHeight= 0;
		fProgressMonitorPart= new ProgressMonitorPart(fStatusContainer, pmlayout);
	}

	private void createMessageBox() {
		fMessageBox= new MessageBox(fStatusContainer, SWT.NONE);
	}

	protected void createButtonsForButtonBar(Composite parent) {

		if (fHasAdditionalPages)
			createPreviousAndNextButtons(parent);
		else
			createPreviewButton(parent);

		String OK_LABEL= (fHasAdditionalPages) ? IDialogConstants.FINISH_LABEL : IDialogConstants.OK_LABEL;
		String CANCEL_LABEL= IDialogConstants.CANCEL_LABEL;
		if (fWizard.internalIsYesNoStyle(InternalAPI.INSTANCE)) {
			OK_LABEL= IDialogConstants.YES_LABEL;
			CANCEL_LABEL= IDialogConstants.NO_LABEL;
		}
		createButton(
			parent,
			IDialogConstants.OK_ID,
			OK_LABEL,
			true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			CANCEL_LABEL,
			false);
		Button okButton= getButton(IDialogConstants.OK_ID);
		okButton.setFocus();
	}

    private void createPreviewButton(Composite parent) {
		if (! (fCurrentPage instanceof PreviewWizardPage) && fWizard.internalHasPreviewPage(InternalAPI.INSTANCE)) {
			Button preview= createButton(parent, PREVIEW_ID, RefactoringUIMessages.RefactoringWizardDialog2_buttons_preview_label, false);
			if (fMakeNextButtonDefault) {
				preview.getShell().setDefaultButton(preview);
			}
			preview.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					nextOrPreviewPressed();
				}
			});
		}
	}

	private Composite createPreviousAndNextButtons(Composite parent) {
		// Copied from Wizard Dialog.

		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns+= 2; // parent is assumed to have a GridLayout (see javadoc of Dialog#createButton(..))
		Composite composite= new Composite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout= new GridLayout();
		layout.numColumns= 0; // will be incremented by createButton
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		layout.horizontalSpacing= 0;
		layout.verticalSpacing= 0;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());
		Button backButton= createButton(composite, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, false);
		backButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				backPressed();
			}
		});
		Button nextButton= createButton(composite, IDialogConstants.NEXT_ID, IDialogConstants.NEXT_LABEL, false);
		nextButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				nextOrPreviewPressed();
			}
		});

		GridData data= new GridData();
		int widthHint= convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize1= backButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point minSize2= nextButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint= Math.max(widthHint * 2, minSize1.x + minSize2.x);
		data.horizontalAlignment= SWT.END;
		data.horizontalSpan= 2;
		composite.setLayoutData(data);

		return composite;
	}

	private void makeVisible(IWizardPage page) {
		if (fVisiblePage == page)
			return;
		if (fVisiblePage != null)
			fVisiblePage.setVisible(false);
		fVisiblePage= page;
		fPageContainer.showPage(page.getControl());
		fVisiblePage.setVisible(true);
	}
}
