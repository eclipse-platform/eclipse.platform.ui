package org.eclipse.ui.tests.manual;

import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.tests.dialogs.*;
import org.eclipse.ui.tests.util.DialogCheck;

/**
 * The UIPreferencesManual is a test case that requires
 * the user to click OK on message dialog when it is
 * run
 */

public class UIPreferencesManual extends UIPreferencesAuto {
	
	public UIPreferencesManual(String name) {
		super(name);
	}

	/**
	 * Test the bad update preference page by generating all
	 * of the dialog errors.
	 */
	public void testBrokenListenerPref() {

		PreferenceDialogWrapper dialog = null;
		PreferenceManager manager =
			WorkbenchPlugin.getDefault().getPreferenceManager();
		if (manager != null) {
			dialog = new PreferenceDialogWrapper(getShell(), manager);
			dialog.create();

			for (Iterator iterator =
				manager.getElements(PreferenceManager.PRE_ORDER).iterator();
				iterator.hasNext();
				) {
				IPreferenceNode node = (IPreferenceNode) iterator.next();
				if (node
					.getId()
					.equals("org.eclipse.ui.tests.manual.BrokenUpdatePreferencePage")) {
					dialog.showPage(node);
					BrokenUpdatePreferencePage page =
						(BrokenUpdatePreferencePage) dialog.getPage(node);
					page.changeFont();
					page.changePluginPreference();
					break;
				}
			}
		}

	}

}
