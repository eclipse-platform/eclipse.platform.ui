package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * 
 */
import org.eclipse.core.internal.boot.update.*;
import java.net.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.update.internal.core.*;
import org.eclipse.core.internal.boot.LaunchInfo;

public class UMApplicationUserInterface implements IPlatformRunnable {
	boolean _bRunEventLoop = true;

	// Command line options
	//---------------------
	boolean _bInstall           = false;
	boolean _bSilent            = false;
	String  _strManifestId      = null;
	String  _strInstall         = null;
	URL     _urlInstall         = null;

	// Command line option string constants
	//-------------------------------------
	private static final String STRING_ID                = "-id";
	private static final String STRING_INSTALL           = "-install";
	private static final String STRING_SILENT            = "-silent";
	private static final String STRING_URL               = "-url";
/**
 * UpdateManagerUI constructor comment.
 */
public UMApplicationUserInterface() {
	super();
}
/**
 * 
 */
private int checkCommandLineOptions() {

	// Command
	//--------
	if (_bInstall == false) {
		return UpdateManagerConstants.COMMAND_MISSING;
	}

	// Id
	//---
	if (_strManifestId == null || _strManifestId.length() == 0) {
		 return UpdateManagerConstants.ID_MISSING;
	}

	// URL
	//----
	if (_bInstall == true) {
		if (_strInstall == null || _strInstall.length() == 0) {
			return UpdateManagerConstants.URL_MISSING;
		}

		try {
			_urlInstall = new URL(_strInstall);

		}
		catch (Exception ex) {
			return UpdateManagerConstants.URL_INVALID;
		}
	}

	return UpdateManagerConstants.OK_TO_INSTALL;
}
/**
 * Installs the given component or configuration by displaying the
 * install wizard.
 */
private void doInstall() {
	
	Display display = new Display();
	Shell shell = new Shell(display);
	UMWizardProductComponentURLInstall wizard = new UMWizardProductComponentURLInstall(this);
	wizard.setInstallURL(_urlInstall);
	wizard.setInstallID(_strManifestId);

	WizardDialog dialog = new WizardDialog(shell, wizard);
	dialog.create();
	dialog.open();

	runEventLoop();

	return;
}
/**
 * Installs the given component or configuration without displaying any user interface.
 */
private void doInstallSilent() {

	UpdateManager updateManager = new UpdateManager();

	try {
		updateManager.initialize();

		IUMRegistry registry = updateManager.getRegistryAt(_urlInstall);
		IInstallable[] installables = new IInstallable[1];

		// Product
		//--------
		installables[0] = registry.getProductDescriptor(_strManifestId);

		// Component
		//----------
		if (installables[0] == null) {
			installables[0] = registry.getComponentDescriptor(_strInstall);
		}

		// Install it
		//-----------
		if (installables[0] != null) {
			UMSessionManagerSession session = updateManager.createSession(installables, false);
			updateManager.executeSession(session, null);

			// Undo
			//-----
			if (session.getStatus().equals(UpdateManagerConstants.STATUS_FAILED) == true) {
				updateManager.executeSessionUndo(session, null);
			}
		}
	}

	catch (UpdateManagerException ex) {
	}

	return;
}
/**
 * Handles a runtime exception or error which was caught in runEventLoop().
 */
private void handleExceptionInEventLoop(Throwable e) {
	// For the status object, use the exception's message, or the exception name if no message.
/*	
	String msg = e.getMessage() == null ? e.toString() : e.getMessage();
	WorkbenchPlugin.log("Unhandled exception caught in event loop.", new Status(IStatus.ERROR, IWorkbenchConstants.PLUGIN_ID, 0, msg, e));
	if (WorkbenchPlugin.DEBUG) {
		e.printStackTrace();
	}
	// Open an error dialog, but don't reveal the internal exception name.
	String msg2 = "An internal error has occurred";
	if (e.getMessage() != null)
		msg2 += ": " + e.getMessage();
	if (!msg2.endsWith("."))
		msg2 += ".";
	msg2 += "  See error log for more details.";
	MessageDialog.openError(null, "Internal error", msg2);
*/
}
/**
 * 
 */
private void parseCommandLineOptions(String[] straArgs) {

	for (int i = 0; i < straArgs.length; ++i) {

		// Command: Install
		//-----------------
		if (straArgs[i].equalsIgnoreCase(STRING_INSTALL) == true) {
			_bInstall = true;
		}
		
		// Option: Silent
		//---------------
		if (straArgs[i].equalsIgnoreCase(STRING_SILENT) == true) {
			_bSilent = true;
		}
		
		// Location to install from
		//-------------------------
		else if (straArgs[i].equalsIgnoreCase(STRING_URL) == true) {
			_strInstall = straArgs[++i];
		}

		// Manifest Identifier
		//--------------------
		else if (straArgs[i].equalsIgnoreCase(STRING_ID) == true) {
			_strManifestId = straArgs[++i];
		}
	}
}
/**
 * Runs the application given a URL input string.  This is called
 * by the Eclipse platform.
 */
public Object run(Object args) throws Exception {

	int iRC = UpdateManagerConstants.OK;

	try {
		String[] straArgs = (String[]) args;

		if (straArgs.length > 0) {

			parseCommandLineOptions(straArgs);

			iRC = checkCommandLineOptions();

			if (iRC == UpdateManagerConstants.OK) {

				// Install
				//--------	
				if (_bInstall == true) {
					if (_bSilent == false) {
						doInstall();
					}
					else{
	                    doInstallSilent();
					}
				}
			}
		}
	}
	catch (Exception ex) {
		return new Integer(UpdateManagerConstants.OTHER_ERROR);
	}

	return new Integer(iRC);
}
/**
 * run an event loop for the workbench.
 */
private void runEventLoop() {
	Display display = Display.getCurrent();
	
	while (_bRunEventLoop == true) {
		try {
			if (!display.readAndDispatch())
				display.sleep();
		}
		catch (RuntimeException e) {
			handleExceptionInEventLoop(e);
		}
		catch (ThreadDeath e) {
			// Don't catch ThreadDeath as this is a normal occurrence when the thread dies
			throw e;
		}
		catch (Error e) {
			handleExceptionInEventLoop(e);
		}
	}
}
/**
 * Turns off the run event loop flag.  This is only called by the
 * URL install wizard.
 */
protected void stopEventLoop() {
	_bRunEventLoop = false;
}
}
