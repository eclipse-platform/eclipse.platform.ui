package org.eclipse.ui.about;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.ui.services.IServiceLocator;

/**
 * An installation dialog page.
 * 
 * The counterpart, {@link IInstallationPageContainer}, may be accessed by the
 * page (via the provided service locator) to update the status message in the
 * hosting dialog.
 * 
 * <em>This API is experiemental and will change before 3.5 ships</em>
 * 
 * @since 3.5
 */
public abstract class InstallationPage extends DialogPage {

	public abstract void init(IServiceLocator locator);
}
