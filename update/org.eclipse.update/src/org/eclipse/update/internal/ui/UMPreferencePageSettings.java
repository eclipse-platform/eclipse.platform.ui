package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * 
 */
import org.eclipse.core.internal.boot.LaunchInfo;import org.eclipse.jface.preference.PreferencePage;import org.eclipse.swt.SWT;import org.eclipse.swt.events.ModifyEvent;import org.eclipse.swt.events.ModifyListener;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.layout.GridLayout;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Control;import org.eclipse.swt.widgets.Label;import org.eclipse.swt.widgets.Text;import org.eclipse.ui.IWorkbench;import org.eclipse.ui.IWorkbenchPreferencePage;import org.eclipse.update.internal.core.UpdateManagerStrings;

public class UMPreferencePageSettings extends PreferencePage implements IWorkbenchPreferencePage, ModifyListener {

	protected Text _textNumberOfHistoriesToKeep = null;
	/**
	 * UMPreferencePageSettings constructor comment.
	 */
	public UMPreferencePageSettings() {
		super();
	}
	/**
	 * Creates and returns the SWT control for the customized body 
	 * of this preference page under the given parent composite.
	 * <p>
	 * This framework method must be implemented by concrete
	 * subclasses.
	 * </p>
	 *
	 */
	protected Control createContents(Composite compositeParent) {

		// Content
		//--------
		Composite compositeContent = new Composite(compositeParent, SWT.NULL);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		compositeContent.setLayout(gridLayout);

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		compositeContent.setLayoutData(gridData);

		// Label: Number of installation histories to keep
		//------------------------------------------------
		Label label = new Label(compositeContent, SWT.LEFT);
		label.setText(UpdateManagerStrings.getString("S_Number_of_installation_histories_to_keep") + ":");
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		label.setLayoutData(gridData);

		// Text: Number of installation histories
		//---------------------------------------
		_textNumberOfHistoriesToKeep = new Text(compositeContent, SWT.BORDER);
		_textNumberOfHistoriesToKeep.setTextLimit(2);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		_textNumberOfHistoriesToKeep.setLayoutData(gridData);
		_textNumberOfHistoriesToKeep.addModifyListener(this);

		initializeContent();

		return compositeContent;
	}
	/**
	 * Initializes this preference page for the given workbench.
	 */
	public void init(IWorkbench workbench) {
	}
	/**
	 * 
	 */
	public void initializeContent() {

		// Initialize number of histories
		//-------------------------------
		if (LaunchInfo.getCurrent().getHistoryCount() < 1)
			LaunchInfo.getCurrent().setHistoryCount(1);

		_textNumberOfHistoriesToKeep.setText(Integer.toString(LaunchInfo.getCurrent().getHistoryCount()));
	}
	/**
	 * Sent when the text is modified.
	 *
	 * @param e an event containing information about the modify
	 */
	public void modifyText(org.eclipse.swt.events.ModifyEvent e) {

		String strText = _textNumberOfHistoriesToKeep.getText();

		boolean bEnable = false;

		int iNumber = LaunchInfo.getCurrent().getHistoryCount();

		if (strText.length() <= 2) {
			try {
				iNumber = Integer.decode(strText).intValue();

				if (iNumber > 0) {
					bEnable = true;
					LaunchInfo.getCurrent().setHistoryCount(iNumber);
				}
			}
			catch (NumberFormatException ex) {
			}
		}

		this.setValid(bEnable);
	}
	/**
	 * Performs special processing when this page's Apply button has been pressed.
	 */
	protected void performApply() {

		String strText = _textNumberOfHistoriesToKeep.getText();

		int iNumber = LaunchInfo.getCurrent().getHistoryCount();

		if (strText.length() <= 2) {
			try {
				iNumber = Integer.decode(strText).intValue();

				if (iNumber > 0) {
					LaunchInfo.getCurrent().setHistoryCount(iNumber);
				}
			}
			catch (NumberFormatException ex) {
			}
		}

		super.performApply();
	}
	/**
	 * Performs special processing when this page's Defaults button has been pressed.
	 */
	protected void performDefaults() {

		LaunchInfo.getCurrent().setHistoryCount(3);
		_textNumberOfHistoriesToKeep.setText("3");

		super.performDefaults();
	}
}