package org.eclipse.update.internal.ui.views;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.internal.search.OptionalFeatureSearchCategory;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.MissingFeature;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.search.*;

/**
 * @author wassimm
 */
public class InstallOptionalFeatureAction extends Action {
	private static final String KEY_OPTIONAL_INSTALL_MESSAGE = "FeaturePage.optionalInstall.message"; //$NON-NLS-1$
	private static final String KEY_OPTIONAL_INSTALL_TITLE = "FeaturePage.optionalInstall.title"; //$NON-NLS-1$

	private MissingFeature missingFeature;
	private Shell shell;

	public InstallOptionalFeatureAction(Shell shell, String text) {
		super(text);
		this.shell = shell;
	}

	public void setFeature(MissingFeature feature) {
		this.missingFeature = feature;
	}

	public void run() {
		if (missingFeature == null)
			return;
		VersionedIdentifier vid = missingFeature.getVersionedIdentifier();
		URL originatingURL = missingFeature.getOriginatingSiteURL();

		UpdateSearchScope scope = new UpdateSearchScope();
		scope.addSearchSite(originatingURL.toString(), originatingURL, null);

		OptionalFeatureSearchCategory category = new OptionalFeatureSearchCategory();
		category.addVersionedIdentifier(vid);
		final UpdateSearchRequest searchRequest =
			new UpdateSearchRequest(category, scope);

		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				openWizard(searchRequest);
			}
		});
	}
	private void openWizard(UpdateSearchRequest searchRequest) {
		UnifiedInstallWizard wizard = new UnifiedInstallWizard(searchRequest);
		WizardDialog dialog = new ResizableWizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setText(
			UpdateUI.getString(KEY_OPTIONAL_INSTALL_TITLE));
		dialog.getShell().setSize(600, 500);
		dialog.open();
		if (wizard.isSuccessfulInstall())
			UpdateUI.informRestartNeeded();
	}
}
