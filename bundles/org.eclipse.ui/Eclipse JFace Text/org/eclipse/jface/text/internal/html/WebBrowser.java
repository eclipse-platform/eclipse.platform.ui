package org.eclipse.jface.text.internal.html;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * ActiveX based web browser control.
 * @deprecated will be removed - present just for test purposes
 */
public class WebBrowser implements IBrowser {
	// Generated from typelib filename: shdocvw.dll
	
	// The automation object and Control associated with the main OLE control
	private OleAutomation oleObject;
	private OleControlSite controlSite;

	// The OLE frame (there should only be one)
	private OleFrame controlFrame;

	
	public WebBrowser(Composite parent)  {
		
		controlFrame = new OleFrame(parent, SWT.NONE);
		int flags= GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL;
		controlFrame.setLayoutData(new GridData(flags));
		
		controlSite = new OleControlSite(controlFrame, SWT.NONE, "Shell.Explorer"); //$NON-NLS-1$
		oleObject = new OleAutomation(controlSite);
		
		// initialize control
		controlSite.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
	}
	
	public void dispose() {
		
		if (oleObject != null) {
			oleObject.dispose();
			oleObject = null;
		}
		
		if (controlSite != null) {
			controlSite.dispose();
			controlSite = null;
		}
		
		if (controlFrame != null) {
			controlFrame.dispose();
			controlFrame = null;
		}
	}
	
	public void setVisible(boolean visible) {
		controlSite.doVerb(visible ? OLE.OLEIVERB_SHOW : OLE.OLEIVERB_HIDE);
	}
	
	public Control getControl() {
		return controlFrame;
	}
	
	public int navigate(String url) {
		
		int[] rgdispid = oleObject.getIDsOfNames(new String[] { "Navigate", "URL" }); //$NON-NLS-1$ //$NON-NLS-2$
		int dispIdMember = rgdispid[0];

		Variant[] rgvarg = new Variant[] { new Variant(url) };
		int[] rgdispidNamedArgs = new int[] { rgdispid[1] }; // identifier of argument
		Variant pVarResult = oleObject.invoke(dispIdMember, rgvarg, rgdispidNamedArgs);
		
		if (pVarResult == null)
			return 0;
		
		return pVarResult.getInt();
	}
}
