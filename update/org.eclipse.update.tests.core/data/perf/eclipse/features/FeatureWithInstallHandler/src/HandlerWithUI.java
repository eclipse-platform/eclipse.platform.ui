import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
/*
 * Created on Oct 1, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author birsan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HandlerWithUI implements IInstallHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#completeConfigure()
	 */
	public void completeConfigure() throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("completeConfigure()");

	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#completeInstall(org.eclipse.update.core.IFeatureContentConsumer)
	 */
	public void completeInstall(IFeatureContentConsumer consumer)
			throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("completeInstall()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#completeUnconfigure()
	 */
	public void completeUnconfigure() throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("completeInstall()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#completeUninstall()
	 */
	public void completeUninstall() throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("completeUninstall()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#configureCompleted(boolean)
	 */
	public void configureCompleted(boolean success) throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("configureCompleted()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#configureInitiated()
	 */
	public void configureInitiated() throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("configureInitiated()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#initialize(int, org.eclipse.update.core.IFeature, org.eclipse.update.core.IInstallHandlerEntry, org.eclipse.update.core.InstallMonitor)
	 */
	public void initialize(int type, IFeature feature,
			IInstallHandlerEntry entry, InstallMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("initialize()");
		Display.getDefault().asyncExec( new Runnable() {
			public void run() {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), "InstallHandler", "This is a dummy message");
			}
		});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#installCompleted(boolean)
	 */
	public void installCompleted(boolean success) throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("installCompleted()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#installInitiated()
	 */
	public void installInitiated() throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("installInitiated()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#nonPluginDataDownloaded(org.eclipse.update.core.INonPluginEntry[], org.eclipse.update.core.IVerificationListener)
	 */
	public void nonPluginDataDownloaded(INonPluginEntry[] nonPluginData,
			IVerificationListener listener) throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("nonPluginDataDownloaded()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#pluginsDownloaded(org.eclipse.update.core.IPluginEntry[])
	 */
	public void pluginsDownloaded(IPluginEntry[] plugins) throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("pluginsDownloaded()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#unconfigureCompleted(boolean)
	 */
	public void unconfigureCompleted(boolean success) throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("unconfigureCompleted()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#unconfigureInitiated()
	 */
	public void unconfigureInitiated() throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("unconfigureInitiated()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#uninstallCompleted(boolean)
	 */
	public void uninstallCompleted(boolean success) throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("uninstallCompleted()");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.update.core.IInstallHandler#uninstallInitiated()
	 */
	public void uninstallInitiated() throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("uninstallInitiated()");
	}
}
