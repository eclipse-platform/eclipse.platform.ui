/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.dialogs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;

import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A class to select elements out of a list of elements, organized on multiple
 * pages.
 * 
 * @since 2.0
 */
public class MultiElementListSelectionDialog extends AbstractElementListSelectionDialog {

	private static class Page {
		public Object[] elements;
		public String filter;
		public boolean okState= false;		
		
		public Page(Object[] elements) {
			this.elements= elements;
		}
	};
	
	private Page[] fPages;		
	private int fCurrentPage;
	private int fNumberOfPages;
	
	private Button fFinishButton;
	private Button fBackButton;
	private Button fNextButton;
	
	private Label fPageInfoLabel;
	private String fPageInfoMessage= WorkbenchMessages.getString("MultiElementListSelectionDialog.pageInfoMessage"); //$NON-NLS-1$;
	
	/**
	 * Constructs a multi-page list selection dialog.
	 * @param renderer           the label renderer.
	 * @param ignoreCase         specifies if sorting and filtering ignores cases.
	 * @param multipleSelection  specifies if multiple selection is allowed.
	 */
	public MultiElementListSelectionDialog(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
	}
	
	/**
	 * Sets message shown in the right top corner. Use {0} and {1} as placeholders
	 * for the current and the total number of pages.
	 * @param message the message.
	 */
	public void setPageInfoMessage(String message) {
		fPageInfoMessage= message;
	}
	
	/**
	 * Sets the elements to be displayed in the dialog.
	 * @param elements an array of pages holding arrays of elements
	 */
	public void setElements(Object[][] elements) {
		fNumberOfPages= elements.length;			
		fPages= new Page[fNumberOfPages];
		for (int i= 0; i != fNumberOfPages; i++)
			fPages[i]= new Page(elements[i]);
		
		initializeResult(fNumberOfPages);
	}

	/*
	 * @see Window#open()
	 */
	public int open() {
		List selection= getInitialSelections();
		if (selection == null) {
			setInitialSelections(new Object[fNumberOfPages]);
			selection= getInitialSelections();
		}
			
		Assert.isTrue(selection.size() == fNumberOfPages);
		
		return super.open();
	}
		 
	/**
	 * @see Dialog#createDialogArea(Composite)
	 */	
	protected Control createDialogArea(Composite parent) {
		Composite contents= (Composite) super.createDialogArea(parent);

		createMessageArea(contents);
		createFilterText(contents);
		createFilteredList(contents);

		fCurrentPage= 0;
		setPageData();
		
		return contents;
	}

	/**
	 * @see Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fBackButton= createButton(parent, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, false);
		fNextButton= createButton(parent, IDialogConstants.NEXT_ID, IDialogConstants.NEXT_LABEL, true);
		fFinishButton= createButton(parent, IDialogConstants.OK_ID, IDialogConstants.FINISH_LABEL, false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	/**
	 * @see SelectionDialog#createMessageArea(Composite)
	 */
	protected Label createMessageArea(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);

		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.horizontalSpacing= 5;
		layout.numColumns= 2;
		composite.setLayout(layout);
		
		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		composite.setLayoutData(data);
		
		Label messageLabel= super.createMessageArea(composite);
		
		fPageInfoLabel= new Label(composite, SWT.NULL);
		fPageInfoLabel.setText(getPageInfoMessage());
		
		data= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalAlignment= data.END;
		fPageInfoLabel.setLayoutData(data);
		
		return messageLabel;
	}
	
	/**
	 * @see SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
		setResult(fCurrentPage, getSelectedElements());
	}
		
	/**
	 * @see Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.BACK_ID) {
			turnPage(false);
		} else if (buttonId == IDialogConstants.NEXT_ID) {
			turnPage(true);
		} else {
			super.buttonPressed(buttonId);
		}			
	}
	
	/**
	 * @see AbstractElementListSelectionDialog#handleDefaultSelected() 
	 */
	protected void handleDefaultSelected() {
		if (validateCurrentSelection()) {
			if (fCurrentPage == fNumberOfPages - 1) {
				buttonPressed(IDialogConstants.OK_ID);
			} else {
				buttonPressed(IDialogConstants.NEXT_ID);
			}
		}
	}
	
	/**
	 * @see AbstractElementListSelectionDialog#updateButtonsEnableState(IStatus)
	 */
	protected void updateButtonsEnableState(IStatus status) {
		boolean isOK= !status.matches(IStatus.ERROR);
		fPages[fCurrentPage].okState= isOK;

		fNextButton.setEnabled(isOK && (fCurrentPage < fNumberOfPages - 1));
		fBackButton.setEnabled(fCurrentPage != 0);
		
		boolean isAllOK= isOK;
		for (int i= 0; i != fNumberOfPages; i++)
			isAllOK = isAllOK && fPages[i].okState;
		
		fFinishButton.setEnabled(isAllOK);
	}

	private void turnPage(boolean toNextPage) {
		Page page= fPages[fCurrentPage];
		
		// store filter		
		String filter= getFilter();
		if (filter == null)
			filter= ""; //$NON-NLS-1$
		page.filter= filter;

		// store selection
		Object[] selectedElements= getSelectedElements();
		List list= getInitialSelections();
		list.set(fCurrentPage, selectedElements);

		// store result
		setResult(fCurrentPage, getSelectedElements());

		if (toNextPage) {
			if (fCurrentPage + 1 >= fNumberOfPages)
				return;
			
			fCurrentPage++;
		} else {
			if (fCurrentPage - 1 < 0)
				return;

			fCurrentPage--;
		}
		
		if (fPageInfoLabel != null && !fPageInfoLabel.isDisposed())
			fPageInfoLabel.setText(getPageInfoMessage());
		
		setPageData();		
		
		validateCurrentSelection();
	}
	
	private void setPageData() {
		Page page= fPages[fCurrentPage];
		
		// 1. set elements
		setListElements(page.elements);
		
		// 2. apply filter
		String filter= page.filter;
		if (filter == null)
			filter= ""; //$NON-NLS-1$
		setFilter(filter);
		
		// 3. select elements
		Object[] selectedElements= (Object[]) getInitialSelections().get(fCurrentPage);
		setSelection(selectedElements);
	}
	
	private String getPageInfoMessage() {
		if (fPageInfoMessage == null)
			return ""; //$NON-NLS-1$
			
		String[] args= new String[] { Integer.toString(fCurrentPage + 1), Integer.toString(fNumberOfPages) };	
		return MessageFormat.format(fPageInfoMessage, args);
	}
		
	private void initializeResult(int length) {
		List result= new ArrayList(length);
		for (int i= 0; i != length; i++)
			result.add(null);

		setResult(result);
	}
	
	/**
	 * Gets the current Page.
	 * @return Returns a int
	 */
	public int getCurrentPage() {
		return fCurrentPage;
	}

}