package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;

/**
 * FOR USE BY TESTS ONLY!
 * <p>
 * Stub class that provides access to classes visible to the package
 * <code>org.eclipse.ui.views.navigator</code>.  For the purpose of
 * testing.
 * </p>
 * @private
 */

public class NavigatorTestStub {
	//Prevent instantiation
	private NavigatorTestStub(){}
	
	/**
	 * Gives access to an instance of GotoResourceDialog.
	 * @return GotoResourceDialog an instance of GotoResourceDialog.
	 */
	public static GotoResourceDialog newGotoResourceDialog(Shell parentShell,IResource[] resources) {
		return new GotoResourceDialog(parentShell, resources);
	}
}

