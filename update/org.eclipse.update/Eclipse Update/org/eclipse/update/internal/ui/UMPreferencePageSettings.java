package org.eclipse.update.internal.ui;

/**
 * 
 */
import org.eclipse.core.internal.boot.update.*;
import org.eclipse.core.internal.boot.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.update.internal.core.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import java.util.Vector;
import org.eclipse.swt.custom.*;
import org.eclipse.jface.dialogs.*;

public class UMPreferencePageSettings extends PreferencePage implements IWorkbenchPreferencePage, ModifyListener{

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
 * @param parent the parent composite
 * @return the new control
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
	_textNumberOfHistoriesToKeep.setLayoutData( gridData );
	_textNumberOfHistoriesToKeep.addModifyListener(this);

	initializeContent();
	
	return compositeContent;
}
/**
 * Initializes this preference page for the given workbench.
 * <p>
 * This method is called automatically as the preference page is being created
 * and initialized. Clients must not call this method.
 * </p>
 *
 * @param workbench the workbench
 */
public void init(IWorkbench workbench) {}
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

			if (iNumber > 0 ) {
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
 * <p>
 * This is a framework hook method for sublcasses to do special things when
 * the Apply button has been pressed.
 * The default implementation of this framework method simply calls
 * <code>performOk</code> to simulate the pressing of the page's OK button.
 * </p>
 * 
 * @see #performOk
 */
protected void performApply() {
	
	String strText = _textNumberOfHistoriesToKeep.getText();

	int iNumber = LaunchInfo.getCurrent().getHistoryCount();
	
	if (strText.length() <= 2) {
		try {
			iNumber = Integer.decode(strText).intValue();

			if (iNumber > 0 ) {
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
 * <p>
 * This is a framework hook method for sublcasses to do special things when
 * the Defaults button has been pressed.
 * Subclasses may override, but should call <code>super.performDefaults</code>.
 * </p>
 */
protected void performDefaults() {
	
	LaunchInfo.getCurrent().setHistoryCount(3);
	_textNumberOfHistoriesToKeep.setText("3");

	super.performDefaults();
}
}
