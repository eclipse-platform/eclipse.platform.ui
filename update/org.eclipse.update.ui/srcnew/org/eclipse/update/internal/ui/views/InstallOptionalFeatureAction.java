package org.eclipse.update.internal.ui.views;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.internal.operations.PendingOperation;
import org.eclipse.update.internal.operations.UpdateManager;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.MissingFeature;

/**
 * @author wassimm
 */
public class InstallOptionalFeatureAction extends Action {
	private static final String KEY_OPTIONAL_INSTALL_MESSAGE =
		"FeaturePage.optionalInstall.message"; //$NON-NLS-1$
	private static final String KEY_OPTIONAL_INSTALL_TITLE =
		"FeaturePage.optionalInstall.title"; //$NON-NLS-1$
	
	private MissingFeature missingFeature;

	public InstallOptionalFeatureAction(String text) {
		super(text);
	}
	
	public void setFeature(MissingFeature feature) {
		this.missingFeature = feature;
	}

	public void run() {
		if (missingFeature == null)
			return;

		IFeature feature =
			fetchFeatureFromServer(
				UpdateUI.getActiveWorkbenchShell(),
				missingFeature.getOriginatingSiteURL(),
				missingFeature.getVersionedIdentifier());

		if (feature != null) {
			IFeature parent = missingFeature.getParent();
			PendingOperation op = new PendingOperation(feature);
			op.setTargetSite((parent == null) ? null : parent.getSite().getCurrentConfiguredSite());
//			op.setVerificationListener(new JarVerificationService(UpdateUI.getActiveWorkbenchShell()));
			executeJob(
				UpdateUI.getActiveWorkbenchShell(),
				op,
				true);
		}
	}
	
	private IFeature fetchFeatureFromServer(
		Shell shell,
		final URL siteURL,
		final VersionedIdentifier vid) {
		// Locate remote site, find the optional feature
		// and install it

		final IFeature[] result = new IFeature[1];
		final boolean aborted[] = new boolean[1];
		aborted[0] = false;

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					monitor.beginTask("Locating the feature on the server...", 3);
					ISite site =
						SiteManager.getSite(siteURL, new SubProgressMonitor(monitor, 1));
					if (site == null || monitor.isCanceled()) {
						aborted[0] = true;
						return;
					}
					result[0] =
						findFeature(
							vid,
							site.getFeatureReferences(),
							new SubProgressMonitor(monitor, 1));
					if (result[0] != null) {
						monitor.setTaskName("Downloading feature info...");
						touchFeatureChildren(
							result[0],
							new SubProgressMonitor(monitor, 1));
					}
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};

		ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
		try {
			pmd.run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			UpdateUI.logException(e);
			return null;
		}

		if (result[0] == null && !aborted[0]) {
			String message =
				UpdateUI.getFormattedMessage(
					KEY_OPTIONAL_INSTALL_MESSAGE,
					siteURL.toString());
			ErrorDialog.openError(
				shell,
				UpdateUI.getString(KEY_OPTIONAL_INSTALL_TITLE),
				null,
				new Status(IStatus.ERROR, UpdateUI.PLUGIN_ID, IStatus.OK, message, null));
		}
		return result[0];
	}
	
	private IFeature findFeature(
		VersionedIdentifier vid,
		IFeatureReference[] refs,
		IProgressMonitor monitor) {
		
		monitor.beginTask("", refs.length*2); //$NON-NLS-1$
			
		for (int i = 0; i < refs.length; i++) {
			IFeatureReference ref = refs[i];
			try {
				if (ref.getVersionedIdentifier().equals(vid)) {
					return ref.getFeature(null);
				}
				monitor.worked(1);
				// Try children
				IFeature feature = ref.getFeature(null);
				IFeatureReference[] irefs =
					feature.getIncludedFeatureReferences();
				IFeature result = findFeature(vid, irefs, new SubProgressMonitor(monitor, 1));
				if (result != null)
					return result;
			} catch (CoreException e) {
			} finally {
				monitor.done();
			}
		}
		return null;
	}
	
	private void touchFeatureChildren(IFeature feature, IProgressMonitor monitor)
		throws CoreException {
		IFeatureReference[] irefs = feature.getIncludedFeatureReferences();
		if (irefs.length > 0) {
			monitor.beginTask("", irefs.length * 2); //$NON-NLS-1$
			for (int i = 0; i < irefs.length; i++) {
				IFeatureReference iref = irefs[i];
				IFeature child = iref.getFeature(null);
				monitor.worked(1);
				touchFeatureChildren(child, new SubProgressMonitor(monitor, 1));
			}
		} else {
			monitor.beginTask("", 1); //$NON-NLS-1$
			monitor.worked(1);
		}
	}
	
	private boolean executeJob(
		Shell shell,
		final PendingOperation job,
		final boolean needLicensePage) {
		
		IStatus validationStatus = UpdateManager.getValidator().validatePendingInstall(job.getOldFeature(), job.getFeature());
		if (validationStatus != null) {
			ErrorDialog.openError(
				UpdateUI.getActiveWorkbenchShell(),
				null,
				null,
				validationStatus);
			return false;
		}

		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				/*InstallWizard wizard = new InstallWizard(job, needLicensePage);
				WizardDialog dialog =
					new InstallWizardDialog(
						UpdateUI.getActiveWorkbenchShell(),
						wizard);
				dialog.create();
				dialog.getShell().setSize(600, 500);
				dialog.open();
				if (wizard.isSuccessfulInstall())
					UpdateUI.informRestartNeeded();*/
			}
		});
		return false;
	}

}
