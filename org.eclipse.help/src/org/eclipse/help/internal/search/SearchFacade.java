package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.zip.*;
import org.eclipse.core.boot.IPlatformRunnable;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.Resources;
/**
 * Launcher for standalone help system
 */
public class SearchFacade implements IPlatformRunnable {
	public final static String HELP_SEARCH_PLUGIN = "com.ibm.help.search";
	public final static int DATA_SIZE = 8192;
	/**
	 * StandaloneHelpSystem constructor comment.
	 */
	public SearchFacade() {
		super();
		// This may not be needed after all.
		/*
		try
		{
			HelpPlugin.getDefault().startup();
		}
		catch(Exception e)
		{
		}
		*/
	}
	public void exec(String[] args) {
		try {
			if (args == null)
				return;
			if (args.length == 0)
				return;
			String cmd = args[0];
			if (cmd.equals("index")) {
				index(args[1], args[2]);
			} else if (cmd.equals("zipIndex")) {
				zipIndex(args[1], args[2], args[3]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void index(String infoset, String lang) throws Exception {
		//HelpSystem.setDebugLevel(HelpSystem.LOG_DEBUG);
		HelpSystem.getSearchManager().updateIndex(new NullProgressMonitor(), lang);
	}
	public Object run(Object args) {
		return this;
	}
	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public void shutdown() throws CoreException {
		//HelpPlugin.getDefault().shutdown();
	}
	private void zipDirectory(File dir, ZipOutputStream zout, String base)
		throws Exception {
		String[] files = dir.list();
		if (files == null || files.length == 0)
			return;
		for (int i = 0; i < files.length; i++) {
			String newbase = base == null ? files[i] : base + "/" + files[i];
			File f = new File(dir, files[i]);
			if (f.isDirectory())
				zipDirectory(f, zout, newbase);
			else {
				ZipEntry zentry = new ZipEntry(newbase);
				zout.putNextEntry(zentry);
				FileInputStream inputStream = new FileInputStream(f);
				byte buffer[] = new byte[DATA_SIZE];
				int len;
				while ((len = inputStream.read(buffer)) != -1)
					zout.write(buffer, 0, len);
				inputStream.close();
				zout.flush();
				zout.closeEntry();
			}
		}
	}
	private void zipIndex(String infoset, String lang, String zipDest)
		throws Exception {
		// Prepare the target 
		String dirName = zipDest + File.separator + "nl" + File.separator + lang;
		File d = new File(dirName);
		if (!d.exists())
			d.mkdirs();
		ZipOutputStream zout =
			new ZipOutputStream(
				new FileOutputStream(dirName + File.separator + "index." + infoset + ".zip"));
		// Read the source
		String source =
			Platform.getPlugin(HELP_SEARCH_PLUGIN).getStateLocation().toOSString();
		source += File.separator
			+ infoset
			+ File.separator
			+ "nl"
			+ File.separator
			+ lang;
		File sourceDir = new File(source);
		if (!sourceDir.exists()) {
			System.out.println(Resources.getString("ES13", source));
			return;
		}
		File f = new File(sourceDir, SearchIndex.INDEXED_DOCS_FILE + ".ini");
		if (!f.exists()) {
			System.out.println(Resources.getString("ES15"));
			return;
		}
		zipDirectory(sourceDir, zout, null);
		zout.close();
	}
}