package org.eclipse.update.internal.ui;

/**
 * Presents status of just completed product and component installations
 */
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.update.internal.core.*;
import java.net.*;
import java.util.Vector;
import org.eclipse.swt.graphics.*;
import java.util.Iterator;
import java.util.TreeSet;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.custom.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;

public class UMWizardPageLaunchHistoryComplete extends WizardPage 
{
	protected UMWizard _wizard   = null;
	protected Text     _textArea = null;
/**
 *
 */
public UMWizardPageLaunchHistoryComplete( UMWizard wizard, String strName )
{
	super( strName );
	_wizard = wizard;

	this.setTitle(UpdateManagerStrings.getString("S_Configuration_Change"));
	this.setDescription("");
		
	setPageComplete(false);
}
/**
 *
 */
public void createControl( Composite compositeParent )
{
	// Content
	//--------
	Composite compositeContent = new Composite( compositeParent, SWT.NULL );

	GridLayout layout= new GridLayout();
	compositeContent.setLayout( layout );
	
	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment   = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace   = true;
	compositeContent.setLayoutData( gridData );

	_textArea = new Text( compositeContent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP );
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment   = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace   = true;
	_textArea.setLayoutData( gridData );
	
	setControl( compositeContent );
}
/**
 *
 */
public void initializeContent() {

	if (_textArea != null) {

		// Obtain the name of the selected installation
		//---------------------------------------------
		UMWizardPageLaunchHistory pageHistory = (UMWizardPageLaunchHistory) _wizard.getPage("history");

		if (pageHistory != null) {

			StringBuffer strb = new StringBuffer();
			strb.append(UpdateManagerStrings.getString("S_The_following_installation_will_become_active_when_the_workbench_is_restarted"));
			strb.append("\n\n    ");
			strb.append(pageHistory.getSelectedInstallation());

			_textArea.setText(strb.toString());
		}
	}

	_wizard.setRestartMessageRequired(true);
}
/**
 * 
 */
public void setVisible(boolean bVisible) {

	// Do installations before setting this page visible
	//--------------------------------------------------
	if (bVisible == true) {
		initializeContent();
	}
	
	setPageComplete(bVisible);

	super.setVisible(bVisible);
}
}
