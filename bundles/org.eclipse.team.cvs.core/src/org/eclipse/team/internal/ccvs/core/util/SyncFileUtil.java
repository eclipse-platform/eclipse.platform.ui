package org.eclipse.team.internal.ccvs.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

	// CVS meta files located in CVS sub-directory of folders that are managed by CVS. These 
	// files contain the workspace revision information for the resources that have been
	// checked out into a CVS workspace. See the CVS documentation for more details.
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

	/**
	 * Reads the CVS/Entry and CVS/Permissions files for the given folder. If the folder does not have a 
	 * CVS subdirectory then <code>null</code> is returned.
	 */
	public static ResourceSyncInfo[] readEntriesFile(File parent) throws CVSException {
		
		File cvsSubDir = getCVSSubdirectory(parent);
		
		if(!cvsSubDir.exists()) {
			return null;
		}
		
		// The Eclipse CVS client does not write to the Entries.log file. Thus
		// merging is required for external command line client compatibility.
		mergeEntriesLogFiles(parent);
				
		Map infos = new TreeMap();
		String[] entries = getContents(new File(cvsSubDir, ENTRIES));
		String[] permissions = getContents(new File(cvsSubDir, PERMISSIONS));
		
		if (entries == null) {
			return null;
		}
		
		for (int i = 0; i < entries.length; i++) {
			String line = entries[i];
			if(!FOLDER_TAG.equals(line) && !"".equals(line)) {
				ResourceSyncInfo info = new ResourceSyncInfo(line, null, null);
				infos.put(info.getName(), info);			
			}
		}

		if (permissions != null) {
			for (int i = 0; i < permissions.length; i++) {
				if ("".equals(permissions[i])) {
					continue;
				}
				String line = permissions[i];
				EmptyTokenizer tokenizer = new EmptyTokenizer(line,"/");
				String name = tokenizer.nextToken();
				String perms = tokenizer.nextToken();
				ResourceSyncInfo info = (ResourceSyncInfo) infos.get(name);
				// Running the command line tool will update the Entries file and thus cause 
				// the Permissions to be out-of-sync. 
				if (info != null) {
					infos.put(name, new ResourceSyncInfo(info.getEntryLine(true), perms, null));
				}
			}
		}		
		return (ResourceSyncInfo[])infos.values().toArray(new ResourceSyncInfo[infos.size()]);
	}
	
	/**
	 * Writes the given resource sync infos into the CVS/Entry and CVS/Permissions files.
	 */
	public static void writeEntriesFile(File entryFile, ResourceSyncInfo[] infos) throws CVSException {
		
		String[] entries = new String[infos.length];
		List permissions = new ArrayList(infos.length);
		
		for(int i = 0; i < infos.length; i++){
			entries[i] = infos[i].getEntryLine(true);
			if (!infos[i].isDirectory()) {
				permissions.add(infos[i].getPermissionLine());
			}
		}		
		writeLines(entryFile, entries);
		writeLines(new File(entryFile.getParentFile(),PERMISSIONS), (String[])permissions.toArray(new String[permissions.size()]));
	}
	
	/**
	 * Read folder sync info, returns <code>null</code> if the folder does not have
	 * a CVS subdirectory.
	 */
	public static FolderSyncInfo readFolderConfig(File folder) throws CVSException {
		
		File cvsSubDir = getCVSSubdirectory(folder);
		
		if(!cvsSubDir.exists()) {
			return null;
		}
		
		String staticDir = readLine(new File(cvsSubDir, STATIC));
		String repo = readLine(new File(cvsSubDir, REPOSITORY));
		String root = readLine(new File(cvsSubDir, ROOT));
		String tag = readLine(new File(cvsSubDir, TAG));
							
		boolean isStatic = false;
		if (staticDir != null)
			isStatic = true;
		
		if(root == null || repo == null) {
			return null;
		}
		
		CVSTag cvsTag = null;
		if(tag != null) {
			cvsTag = new CVSEntryLineTag(tag);
		}
			
		return new FolderSyncInfo(repo, root, cvsTag, isStatic);		
	}
	
	public static void writeFolderConfig(File parent, FolderSyncInfo info) throws CVSException {
		
		if(!getCVSSubdirectory(parent).exists()) {
			getCVSSubdirectory(parent).mkdir();
		}

		writeLine(parent, ROOT, info.getRoot());
		if (info.getTag() != null) {
			String tagString = info.getTag().toEntryLineFormat(false);
			if (tagString.length() > 0)
				writeLine(parent, TAG, tagString);
			else
				writeLine(parent, TAG, null);
		} else {
			writeLine(parent, TAG, null);
		}
		if(info.getIsStatic()) {
			writeLine(parent, STATIC, ""); // touch file
		} else {
			writeLine(parent, STATIC, null);
		}
		writeLine(parent, REPOSITORY, info.getRepository());
	}
	
	public static File[] getEntrySyncFiles(File folder) {
		File cvsSubDir = getCVSSubdirectory(folder);
		return new File[] { new File(cvsSubDir, ENTRIES), new File(cvsSubDir, PERMISSIONS) };
	}
	
	public static File[] getFolderSyncFiles(File folder) {
		File cvsSubDir = getCVSSubdirectory(folder);
		return new File[] {new File(cvsSubDir, ROOT), new File(cvsSubDir, REPOSITORY), new File(cvsSubDir, STATIC), new File(cvsSubDir, TAG)};
	}	
	
	public static File getCVSSubdirectory(File folder) {
		return new File(folder, "CVS");
	}
						
	protected static void setContents(File parent, String filename, String[] contents) throws CVSException {
		
		File propertyFile = new File(getCVSSubdirectory(parent), filename);

		if (contents == null) {
			propertyFile.delete();
		} else {
			writeLines(propertyFile,contents);
		}
	}
	
	protected static void writeLine(File parent, String filename, String content) throws CVSException {
		if (content == null) {
			setContents(parent, filename, null);
		} else {
			setContents(parent, filename, new String[]{content});
		}
	}
	
	protected static String readLine(File file) throws CVSException {
		String[] contents = getContents(file);
		if (contents == null) {
			return null;
		} else if (contents.length == 0) {
			return "";
		} else {
			return contents[0];
		}
	}
	
	protected static String[] getContents(File file)	throws CVSException {
		
		// If the property does not exsist we return null
		// this is specified
		if (file.exists()) {
			return readLines(file);
		} else {
			return null;
		} 
	}
	
	/**
	 * To be compatible with other CVS clients meta files must be written with lines
	 * terminating with a carriage return only.
	 */
	private static void writeLines(File file, String[] content) throws CVSException {
		
		BufferedWriter fileWriter;

		try {
			fileWriter = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i<content.length; i++) {
				fileWriter.write(content[i] + "\n");				
			}
			fileWriter.close();
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	public static String[] readLines(File file) throws CVSException {
		BufferedReader fileReader;
		List fileContentStore = new ArrayList();
		String line;
		
		try {
			fileReader = new BufferedReader(new FileReader(file));
			while ((line = fileReader.readLine()) != null) {
				fileContentStore.add(line);
			}
			fileReader.close();
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
			
		return (String[]) fileContentStore.toArray(new String[fileContentStore.size()]);
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
		
		//for (int i = 0; i < dirs.length; i++) {
		//	mergeEntriesLogFiles(dirs[i]);
		//}

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

		String[] entries = readLines(entriesFile);
		for (int i = 0; i < entries.length; i++) {
			if (!FOLDER_TAG.equals(entries[i])) {
				mergedEntries.put((new ResourceSyncInfo(entries[i],null, null)).getName(),entries[i]);
			}
		}
		
		String[] logEntries = readLines(logEntriesFile);
		for (int i = 0; i < logEntries.length; i++) {
			
			if (logEntries[i].startsWith(ADD_TAG)) {
				String newEntry = logEntries[i].substring(ADD_TAG.length());
				mergedEntries.put((new ResourceSyncInfo(newEntry,null, null)).getName(),newEntry);		
			} else if (logEntries[i].startsWith(REMOVE_TAG)) {
				String newEntry = logEntries[i].substring(REMOVE_TAG.length());
				mergedEntries.remove((new ResourceSyncInfo(newEntry,null, null)).getName());
			}
		}
		
		writeLines(entriesFile,(String[]) mergedEntries.values().toArray(new String[mergedEntries.size()]));
		logEntriesFile.delete();
	}
}