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

public class UMWizardPageComplete extends WizardPage 
{
	protected UMWizard _wizard   = null;
	protected Text     _textArea = null;
/**
 *
 */
public UMWizardPageComplete( UMWizard wizard, String strName, boolean bUpdate )
{
	super( strName );
	_wizard = wizard;

	if (bUpdate == true) {
		this.setTitle(UpdateManagerStrings.getString("S_New_Component_Updates"));
		this.setDescription(UpdateManagerStrings.getString("S_Updates_completed"));
	}
	else {
		this.setTitle(UpdateManagerStrings.getString("S_New_Components"));
		this.setDescription(UpdateManagerStrings.getString("S_Installation_completed"));
	}

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

	_textArea = new Text( compositeContent, SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.verticalAlignment   = GridData.FILL;
	gridData.grabExcessHorizontalSpace = true;
	gridData.grabExcessVerticalSpace   = true;
	_textArea.setLayoutData( gridData );
	
	setControl( compositeContent );
}
/**
 * Do not allow going back to a previous page
 * Setting to null disables the page		
 */
public IWizardPage getPreviousPage() {
	return null;
}
/**
 *
 */
public void initializeContent() {

	// Do the update/install operations
	//---------------------------------
	UMWizardPageInstalling pageInstalling = (UMWizardPageInstalling) _wizard.getPage("installing");

	if (pageInstalling != null) {
	    
		pageInstalling.doInstalls();

		// Display the status
		//-------------------
		UMSessionManagerSession session = pageInstalling.getSession();

		if (_textArea != null && session != null)
			_textArea.setText(session.getStatusString());
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
