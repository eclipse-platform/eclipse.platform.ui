package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/*
 * This is a helper class that knows the format of the CVS metafiles. It
 * provides a bridge between the CVS metafile formats and location to the
 * Eclipse CVS client ResourceSyncInfo and FolderSyncInfo types.
 */
public class SyncFileWriter {

	// CVS meta files located in the CVS subdirectory
	public static final String REPOSITORY = "Repository"; //$NON-NLS-1$
	public static final String ROOT = "Root"; //$NON-NLS-1$
	public static final String STATIC = "Entries.Static";	 //$NON-NLS-1$
	public static final String TAG = "Tag";	 //$NON-NLS-1$
	public static final String ENTRIES = "Entries"; //$NON-NLS-1$
	public static final String PERMISSIONS = "Permissions"; //$NON-NLS-1$
	public static final String ENTRIES_LOG="Entries.Log"; //$NON-NLS-1$
	
	// the local workspace file that contains pattern for ignored resources
	public static final String IGNORE_FILE = ".cvsignore"; //$NON-NLS-1$

	// Some older CVS clients may of added a line to the entries file consisting
	// of only a 'D'. It is safe to ingnore these entries.	
	private static final String FOLDER_TAG="D"; //$NON-NLS-1$
	
	// Command characters found in the Entries.log file
	private static final String ADD_TAG="A "; //$NON-NLS-1$
	private static final String REMOVE_TAG="R "; //$NON-NLS-1$

	// file and folder patterns that are ignored by default by the CVS server on import.
	public static final String[] PREDEFINED_IGNORE_PATTERNS = {
		"CVS", ".#*", "#*", ",*", "_$*", "*~", "*$", "*.a", "*.bak", "*.BAK",  //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
		"*.elc", "*.exe", "*.ln", "*.o", "*.obj", "*.olb", "*.old", "*.orig", "*.rej", "*.so", //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
		"*.Z", ".del-*", ".make.state", ".nse_depinfo", "CVS.adm", //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		"cvslog.*", "RCS", "RCSLOG", "SCCS", "tags", "TAGS"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		
	// file and folder patterns that are ignored by default by the CVS server on import.
	public static final String[] BASIC_IGNORE_PATTERNS = {"CVS", ".#*"}; //$NON-NLS-1$ //$NON-NLS-2$

	/*
	 * Reads the CVS/Entry and CVS/Permissions files for the given folder. If the folder does not have a 
	 * CVS subdirectory then <code>null</code> is returned.
	 */
	public static ResourceSyncInfo[] readEntriesFile(ICVSFolder parent) throws CVSException {
		
		ICVSFolder cvsSubDir = getCVSSubdirectory(parent);
		
		if(!cvsSubDir.exists()) {
			return null;
		}
		
		// The Eclipse CVS client does not write to the Entries.log file. Thus
		// merging is required for external command line client compatibility.
		mergeEntriesLogFiles(parent);
				
		Map infos = new TreeMap();
		String[] entries = getContents(cvsSubDir.getFile(ENTRIES));
		String[] permissions = getContents(cvsSubDir.getFile(PERMISSIONS));
		
		if (entries == null) {
			return null;
		}
		
		for (int i = 0; i < entries.length; i++) {
			String line = entries[i];
			if(!FOLDER_TAG.equals(line) && !"".equals(line)) { //$NON-NLS-1$
				ResourceSyncInfo info = new ResourceSyncInfo(line, null, null);
				infos.put(info.getName(), info);			
			}
		}

		if (permissions != null) {
			for (int i = 0; i < permissions.length; i++) {
				if ("".equals(permissions[i])) { //$NON-NLS-1$
					continue;
				}
				String line = permissions[i];
				EmptyTokenizer tokenizer = new EmptyTokenizer(line,"/"); //$NON-NLS-1$
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
	
	public static void writeResourceSync(ICVSResource file, ResourceSyncInfo info) throws CVSException {
		writeEntriesLog(file, info, ADD_TAG);
	}
		
	/*
	 * Delete this file from Entries/Permissions file
	 */
	public static void deleteSync(ICVSResource file) throws CVSException {
		ICVSFolder parent = file.getParent();
		if(parent!=null && parent.exists()) {
			if(file.isFolder()) {
				writeEntriesLog(file, new ResourceSyncInfo(file.getName()), REMOVE_TAG);		
			} else {
				writeEntriesLog(file, new ResourceSyncInfo(file.getName(), "0", "", "", null, ""), REMOVE_TAG);		 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
	}
	
	public static void deleteCVSSubDirectory(IContainer folder) throws CoreException {
		IContainer cvsSubDir = folder.getFolder(new Path("CVS"));
		cvsSubDir.delete(false /*force*/, null);
	}
		
	/**
	 * Read folder sync info, returns <code>null</code> if the folder does not have
	 * a CVS subdirectory.
	 */
	public static FolderSyncInfo readFolderConfig(ICVSFolder folder) throws CVSException {
		
		ICVSFolder cvsSubDir = getCVSSubdirectory(folder);
		
		if(!cvsSubDir.exists()) {
			return null;
		}
		
		String staticDir = readLine(cvsSubDir.getFile(STATIC));
		String repo = readLine(cvsSubDir.getFile(REPOSITORY));
		String root = readLine(cvsSubDir.getFile(ROOT));
		String tag = readLine(cvsSubDir.getFile(TAG));
							
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
	
	public static void writeFolderConfig(ICVSFolder folder, FolderSyncInfo info) throws CVSException {
		
		ICVSFolder cvsSubDir = getCVSSubdirectory(folder);
		
		if(!cvsSubDir.exists()) {
			cvsSubDir.mkdir();
		}

		writeLines(cvsSubDir.getFile(ROOT), new String[] {info.getRoot()});
		ICVSFile tagFile = cvsSubDir.getFile(TAG);
		if (info.getTag() != null) {
			writeLines(tagFile, new String[] {info.getTag().toEntryLineFormat(false)});
		} else {
			if(tagFile.exists()) {
				tagFile.delete();
			}
		}
		ICVSFile staticFile = cvsSubDir.getFile(STATIC);
		if(info.getIsStatic()) {
			// the existance of the file is all that matters
			writeLines(staticFile, new String[] {""}); //$NON-NLS-1$
		} else {
			if(staticFile.exists()) {
				staticFile.delete();
			}
		}
		writeLines(cvsSubDir.getFile(REPOSITORY), new String[] {info.getRepository()});
	}
			
	protected static String readLine(ICVSFile file) throws CVSException {
		String[] contents = getContents(file);
		if (contents == null) {
			return null;
		} else if (contents.length == 0) {
			return ""; //$NON-NLS-1$
		} else {
			return contents[0];
		}
	}
	
	protected static String[] getContents(ICVSFile file) throws CVSException {		
		// If the property does not exsist we return null
		// this is specified
		if (file.exists()) {
			return readLines(file);
		} else {
			return null;
		} 
	}	
	
	public static ICVSFolder getCVSSubdirectory(ICVSFolder folder) throws CVSException {
		return folder.getFolder("CVS"); //$NON-NLS-1$
	}
	
	public static void mergeEntriesLogFiles(ICVSFolder root) throws CVSException {
		
		ICVSFile logEntriesFile = getCVSSubdirectory(root).getFile(ENTRIES_LOG);
		ICVSFile entriesFile = getCVSSubdirectory(root).getFile(ENTRIES);

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

		if(entriesFile.exists()) {
			String[] entries = readLines(entriesFile);
			for (int i = 0; i < entries.length; i++) {
				if (!FOLDER_TAG.equals(entries[i])) {
					mergedEntries.put((new ResourceSyncInfo(entries[i],null, null)).getName(),entries[i]);
				}
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
	
	public static String[] readLines(ICVSFile file) throws CVSException {
		BufferedReader fileReader;
		List fileContentStore = new ArrayList();
		String line;
		
		try {
			fileReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			while ((line = fileReader.readLine()) != null) {
				fileContentStore.add(line);
			}
			fileReader.close();
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
			
		return (String[]) fileContentStore.toArray(new String[fileContentStore.size()]);
	}
	
	/*
	 * To be compatible with other CVS clients meta files must be written with lines
	 * terminating with a carriage return only.
	 */
	private static void writeLines(ICVSFile file, String[] content) throws CVSException {
		
		BufferedWriter fileWriter;

		try {
			fileWriter = new BufferedWriter(new OutputStreamWriter(file.getOutputStream()));
			for (int i = 0; i<content.length; i++) {
				fileWriter.write(content[i] + "\n");				 //$NON-NLS-1$
			}
			fileWriter.close();
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	public static void addCvsIgnoreEntry(ICVSResource resource, String pattern) throws CVSException {
		OutputStream out = null;
		try {
			ICVSFile cvsignore = resource.getParent().getFile(IGNORE_FILE);
			String line = pattern == null ? resource.getName() : pattern;
			line += "\n"; //$NON-NLS-1$
			out = cvsignore.getAppendingOutputStream();
			out.write(line.getBytes());
		} catch(IOException e) {
			throw new CVSException(IStatus.ERROR, 0, Policy.bind("SyncFileUtil_Error_writing_to_.cvsignore_61"), e); //$NON-NLS-1$
		} finally {
			try {
				if(out!=null) {
					out.close();
				}
			} catch(IOException e) {
				throw new CVSException(IStatus.ERROR, 0, Policy.bind("SyncFileUtil_Cannot_close_.cvsignore_62"), e); //$NON-NLS-1$
			}
		}
	}
	
	/*
	 * Append to Entries.log file
	 */
	private static void writeEntriesLog(ICVSResource file, ResourceSyncInfo info, String prefix) throws CVSException {
		OutputStream out = null;
		try {
			ICVSFile entriesLogFile = getCVSSubdirectory(file.getParent()).getFile(ENTRIES_LOG);
			String line = prefix + info.getEntryLine(true) +"\n"; //$NON-NLS-1$
			out = entriesLogFile.getAppendingOutputStream();
			out.write(line.getBytes());
		} catch(IOException e) {
			throw new CVSException(IStatus.ERROR, 0, Policy.bind("SyncFileUtil_Error_writing_to_Entries.log_48"), e); //$NON-NLS-1$
		} finally {
			try {
				if(out!=null) {
					out.close();
				}
			} catch(IOException e) {
				throw new CVSException(IStatus.ERROR, 0, Policy.bind("SyncFileUtil_Cannot_close_Entries.log_49"), e); //$NON-NLS-1$
			}
		}
	}
}