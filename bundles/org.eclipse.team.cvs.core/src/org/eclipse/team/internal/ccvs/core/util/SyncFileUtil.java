package org.eclipse.team.internal.ccvs.core.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;

public class SyncFileUtil {

	// All possible files available in the CVS subdir
	
	public static final String REPOSITORY = "Repository";
	public static final String ROOT = "Root";
	public static final String STATIC = "Entries.Static";	
	public static final String TAG = "Tag";	
	public static final String ENTRIES = "Entries";
	public static final String PERMISSIONS = "Permissions";
	public static final String ENTRIES_LOG="Entries.Log";

	// Some older CVS clients may of added a line to the entries file consisting
	// of only a 'D'. It is safe to ingnore these entries.	
	private static final String FOLDER_TAG="D";

	public static ResourceSyncInfo[] readEntriesFile(File parent) throws CVSException {
		
		if(!getCVSSubdirectory(parent).exists()) {
			return new ResourceSyncInfo[0];
		}
				
		Map infos = new TreeMap();
		String[] entries = getContents(parent, ENTRIES);
		String[] permissions = getContents(parent, PERMISSIONS);
		
		if (entries == null) {
			return new ResourceSyncInfo[0];
		}
		
		for (int i = 0; i < entries.length; i++) {
			String line = entries[i];
			if(!FOLDER_TAG.equals(line) && !"".equals(line)) {
				ResourceSyncInfo info = new ResourceSyncInfo(line, null);
				infos.put(info.getName(), info);			
			}
		}

		if (permissions != null) {
			for (int i = 0; i < permissions.length; i++) {
				if ("".equals(permissions[i])) {
					continue;
				}
				String line = permissions[i];
				String name = line.substring(1,line.indexOf("/",1));
				ResourceSyncInfo info = (ResourceSyncInfo) infos.get(name);
				if (info == null) {
					throw new CVSException("Entries-File modified");
				}
				info.setPermissionLine(line);
			}
		}
		
		return (ResourceSyncInfo[])infos.values().toArray(new ResourceSyncInfo[infos.size()]);
	}
	
	public static void writeEntriesFile(File entryFile, ResourceSyncInfo[] infos) throws CVSException {
		
		String[] entries = new String[infos.length];
		List permissions = new ArrayList(infos.length);
		
		for(int i = 0; i < infos.length; i++){
			entries[i] = infos[i].getEntryLine(true);
			if (!infos[i].isDirectory()) {
				permissions.add(infos[i].getPermissionLine());
			}
		}		
		FileUtil.writeLines(entryFile, entries);
		FileUtil.writeLines(new File(entryFile.getParentFile(),PERMISSIONS), (String[])permissions.toArray(new String[permissions.size()]));
	}
	
	public static FolderSyncInfo readFolderConfig(File folder) throws CVSException {
		
		if(!getCVSSubdirectory(folder).exists()) {
			return null;
		}
		
		String staticDir = readLine(folder, STATIC);
		String repo = readLine(folder, REPOSITORY);
		String root = readLine(folder, ROOT);
		String tag = readLine(folder, TAG);
							
		boolean isStatic = false;
		if (staticDir != null)
			isStatic = true;
			
		return new FolderSyncInfo(repo, root, tag, isStatic);		
	}
	
	public static File[] getEntrySyncFiles(File folder) {
		File cvsSubDir = getCVSSubdirectory(folder);
		return new File[] { new File(cvsSubDir, ENTRIES), new File(cvsSubDir, PERMISSIONS) };
	}
	
	public static File[] getFolderSyncFiles(File folder) {
		File cvsSubDir = getCVSSubdirectory(folder);
		return new File[] { new File(cvsSubDir, ROOT), new File(cvsSubDir, REPOSITORY), new File(cvsSubDir, STATIC), new File(cvsSubDir, TAG) };
	}
	
	public static void writeFolderConfig(File parent, FolderSyncInfo info) throws CVSException {
		
		if(!getCVSSubdirectory(parent).exists()) {
			getCVSSubdirectory(parent).mkdir();
		}

		writeLine(parent, ROOT, info.getRoot());
		if (info.getTag() != null)
			writeLine(parent, TAG, info.getTag().toEntryLineFormat());
		if(info.getIsStatic()) {
			writeLine(parent, STATIC, ""); // touch file
		}
		writeLine(parent, REPOSITORY, info.getRepository());
	}
						
	protected static void setContents(File parent, String filename, String[] contents) throws CVSException {
		
		File propertyFile = new File(getCVSSubdirectory(parent), filename);

		if (contents == null) {
			propertyFile.delete();
		} else {
			FileUtil.writeLines(propertyFile,contents);
		}
	}
	
	protected static void writeLine(File parent, String filename, String content) throws CVSException {
		if (content == null) {
			setContents(parent, filename, null);
		} else {
			setContents(parent, filename, new String[]{content});
		}
	}
	
	protected static String readLine(File parent, String filename) throws CVSException {
		String[] contents = getContents(parent, filename);
		if (contents == null) {
			return null;
		} else if (contents.length == 0) {
			return "";
		} else {
			return contents[0];
		}
	}
	
	protected static String[] getContents(File parent, String filename)	throws CVSException {
		
		File propertyFile = new File(getCVSSubdirectory(parent), filename);
		
		// If the property does not exsist we return null
		// this is specified
		if (propertyFile.exists()) {
			return FileUtil.readLines(propertyFile);
		} else {
			return null;
		} 
	}	
	
	public static File getCVSSubdirectory(File folder) {
		return new File(folder, "CVS");
	}
	
	public static void mergeEntriesLogFiles(File root) throws CVSException {
		
		String FOLDER_TAG="D";
		String ADD_TAG="A ";
		String REMOVE_TAG="R ";
		
		File[] dirs = root.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.isDirectory() && !file.getName().equals("CVS");
				}
			});
		
		for (int i = 0; i < dirs.length; i++) {
			mergeEntriesLogFiles(dirs[i]);
		}

		File logEntriesFile = new File(getCVSSubdirectory(root), ENTRIES_LOG);
		File entriesFile = new File(getCVSSubdirectory(root), ENTRIES);

		if (!logEntriesFile.exists()) {
			// If we do not have an Entries.Log file we are done because there is nothing
			// to merge (this includes the case where we do not have CVSDirectory)
			return;
		}
		
		// The map contains the name of the resource as the key and the entryLine as the 
		// value
		// "new ResourceSyncInfo(entryLine,null)).getName()" ist used to parse the name 
		// out of the entryLine and shoud maybe be replaced sometime
		
		Map mergedEntries = new HashMap();

		String[] entries = FileUtil.readLines(entriesFile);
		for (int i = 0; i < entries.length; i++) {
			if (!FOLDER_TAG.equals(entries[i])) {
				mergedEntries.put((new ResourceSyncInfo(entries[i],null)).getName(),entries[i]);
			}
		}
		
		String[] logEntries = FileUtil.readLines(logEntriesFile);
		for (int i = 0; i < logEntries.length; i++) {
			
			if (logEntries[i].startsWith(ADD_TAG)) {
				String newEntry = logEntries[i].substring(ADD_TAG.length());
				mergedEntries.put((new ResourceSyncInfo(newEntry,null)).getName(),newEntry);		
			} else if (logEntries[i].startsWith(REMOVE_TAG)) {
				String newEntry = logEntries[i].substring(REMOVE_TAG.length());
				mergedEntries.remove((new ResourceSyncInfo(newEntry,null)).getName());
			}
		}
		
		FileUtil.writeLines(entriesFile,(String[]) mergedEntries.values().toArray(new String[mergedEntries.size()]));
		logEntriesFile.delete();
	}
}