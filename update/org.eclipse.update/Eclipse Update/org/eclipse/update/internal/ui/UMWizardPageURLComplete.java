package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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

public class UMWizardPageURLComplete extends WizardPage 
{
	protected UMWizard _wizard   = null;
	protected Text     _textArea = null;
/**
 *
 */
public UMWizardPageURLComplete(UMWizard wizard, String strName) {
	super(strName);
	_wizard = wizard;

	this.setTitle(UpdateManagerStrings.getString("S_Install_Components"));
	this.setDescription(UpdateManagerStrings.getString("S_Installation_completed"));

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
 *
 */
public void initializeContent() {

	// Do the update/install operations
	//---------------------------------
	UMWizardPageURLInstalling pageInstalling = (UMWizardPageURLInstalling) _wizard.getPage("installing");

	if (pageInstalling != null) {
	    
		pageInstalling.doInstalls();

		// Display the status
		//-------------------
		UMSessionManagerSession session = pageInstalling.getSession();

		if (_textArea != null && session != null)
			_textArea.setText(session.getStatusString());

		// Do not allow going back to a previous page
		// All previous information may be out of date
		//--------------------------------------------
		this.setPreviousPage(this);
	}
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
