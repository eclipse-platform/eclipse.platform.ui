package org.eclipse.search.tests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.zip.ZipFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;

/**
 * Plugin class for search tests.
 */
public class SearchTestPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static SearchTestPlugin plugin;
	
	public SearchTestPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static SearchTestPlugin getDefault() {
		return plugin;
	}
	
	public static void importFilesFromZip(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor) throws InvocationTargetException {		
		ZipFileStructureProvider structureProvider=	new ZipFileStructureProvider(srcZipFile);
		try {
			ImportOperation op= new ImportOperation(destPath, structureProvider.getRoot(), structureProvider, new IOverwriteQuery() {
				public String queryOverwrite(String pathString) {
					return ALL;
				}
			});
			op.run(monitor);
		} catch (InterruptedException e) {
			// should not happen
		}
	}

	public File getFileInPlugin(IPath path) {
		try {
			URL installURL= new URL(getDescriptor().getInstallURL(), path.toString());
			URL localURL= Platform.asLocalURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}
	
	public SearchView getSearchView() {
		return (SearchView) NewSearchUI.activateSearchResultView();
	}

}
