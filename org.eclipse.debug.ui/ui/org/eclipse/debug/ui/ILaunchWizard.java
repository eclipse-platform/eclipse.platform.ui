package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILauncher;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;

/**
 * A launch wizard provides a UI for a launcher. It allows users
 * to configure launch specific attributes. A launch wizard is
 * is contributed by a launcher extension, via the <code>"wizard"</code>
 * attribute, which specifies the fully qualified name of the class that
 * implements this interface. 
 * <p>
 * A launch wizard can be instantiated in two ways:
 * <ul>
 * <li>When the run or debug button is pressed and a default launcher can
 *   not be determined for the current selection of active editor, the
 *   debug UI presents a composite wizard that allows the user to select a launcher 
 *   to use. When the next button is pressed, the wizard specified for that
 *   launcher is instantiated and displayed.</li>
 * </ul>When a launcher delegate is invoked, it has the option of displaying its
 *   wizard if the launcher delegate requires user interaction to configure a launch.</li>
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.debug.core.ILauncher
 * @see org.eclipse.debug.core.model.ILauncherDelegate
 * @deprecated to be replaced with launch configuration tabs
 */

public interface ILaunchWizard extends IWizard {
	/**
	 * This lifecycle method is called by the debug UI plug-in after instantiating
	 * and before displaying a launch wizard, to allow the wizard to configure itself
	 * based on the mode in which it should launch, and the selection of elements
	 * which provide context on what should be launched.
	 *
	 * @param launcher the launcher being used - corresponds to the launcher delegate
	 *    that contributed this launch wizard
	 * @param mode run or debug
	 * @param selection the current selection in the workbench, from which this wizard
	 *    can obtain context on what may be launched
	 *
	 * @see org.eclipse.debug.core.ILaunchManager
	 * @see org.eclipse.debug.core.ILauncher
	 * @see org.eclipse.debug.core.model.ILauncherDelegate
	 */
	void init(ILauncher launcher, String mode, IStructuredSelection selection);
}