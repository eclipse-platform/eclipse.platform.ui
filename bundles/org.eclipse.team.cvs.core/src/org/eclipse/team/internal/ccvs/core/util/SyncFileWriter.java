package org.eclipse.team.internal.ccvs.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
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
	private static final String REPOSITORY = "Repository"; //$NON-NLS-1$
	private static final String ROOT = "Root"; //$NON-NLS-1$
	private static final String STATIC = "Entries.Static";	 //$NON-NLS-1$
	private static final String TAG = "Tag";	 //$NON-NLS-1$
	private static final String ENTRIES = "Entries"; //$NON-NLS-1$
	//private static final String PERMISSIONS = "Permissions"; //$NON-NLS-1$
	private static final String ENTRIES_LOG="Entries.Log"; //$NON-NLS-1$
	
	// the local workspace file that contains pattern for ignored resources
	private static final String IGNORE_FILE = ".cvsignore"; //$NON-NLS-1$

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

	/**
	 * Reads the CVS/Entries, CVS/Entries.log and CVS/Permissions files from the
	 * specified folder and returns ResourceSyncInfo instances for the data stored therein.
	 * If the folder does not have a CVS subdirectory then <code>null</code> is returned.
	 */
	public static ResourceSyncInfo[] readAllResourceSync(ICVSFolder parent) throws CVSException {
		ICVSFolder cvsSubDir = getCVSSubdirectory(parent);
		if (! cvsSubDir.exists()) return null;

		// process Entries file contents
		String[] entries = readLines(cvsSubDir.getFile(ENTRIES));
		if (entries == null) return null;
		Map infos = new TreeMap();
		for (int i = 0; i < entries.length; i++) {
			String line = entries[i];
			if(!FOLDER_TAG.equals(line) && !"".equals(line)) { //$NON-NLS-1$
				ResourceSyncInfo info = new ResourceSyncInfo(line, null, null);
				infos.put(info.getName(), info);			
			}
		}
		
		// process Entries.log file contents
		String[] entriesLog = readLines(cvsSubDir.getFile(ENTRIES_LOG));
		if (entriesLog != null) {
			for (int i = 0; i < entriesLog.length; i++) {
				String line = entriesLog[i];
				if (line.startsWith(ADD_TAG)) {
					line = line.substring(ADD_TAG.length());
					ResourceSyncInfo info = new ResourceSyncInfo(line, null, null);
					infos.put(info.getName(), info);
				} else if (line.startsWith(REMOVE_TAG)) {
					line = line.substring(REMOVE_TAG.length());
					ResourceSyncInfo info = new ResourceSyncInfo(line, null, null);
					infos.remove(info.getName());
				}
			}
		}
		
		// XXX no longer processes CVS/Permissions (was never written) -- should we?
		
		return (ResourceSyncInfo[])infos.values().toArray(new ResourceSyncInfo[infos.size()]);
	}
	
	/**
	 * Writes the CVS/Entries, CVS/Entries.log and CVS/Permissions files to the
	 * specified folder using the data contained in the specified ResourceSyncInfo instance.
	 * If the folder does not have a CVS subdirectory then <code>null</code> is returned.
	 */
	public static void writeAllResourceSync(ICVSFolder parent, ResourceSyncInfo[] infos) throws CVSException {
		ICVSFolder cvsSubDir = getCVSSubdirectory(parent);
		if (!cvsSubDir.exists()) cvsSubDir.mkdir();
		
		// format file contents
		String[] entries = new String[infos.length];
		for (int i = 0; i < infos.length; i++) {
			ResourceSyncInfo info = infos[i];
			entries[i] = info.getEntryLine(true);
		}

		// write Entries
		writeLines(cvsSubDir.getFile(ENTRIES), entries);
		
		// delete Entries.log
		cvsSubDir.getFile(ENTRIES_LOG).delete();

		// XXX does not write CVS/Permissions -- should we?
	}
		
	/**
	 * Reads the CVS/Root, CVS/Repository, CVS/Tag, and CVS/Entries.static files from
	 * the specified folder and returns a FolderSyncInfo instance for the data stored therein.
	 * If the folder does not have a CVS subdirectory then <code>null</code> is returned.
	 */
	public static FolderSyncInfo readFolderSync(ICVSFolder folder) throws CVSException {
		ICVSFolder cvsSubDir = getCVSSubdirectory(folder);
		if (! cvsSubDir.exists()) return null;
		
		// read CVS/Root
		String root = readFirstLine(cvsSubDir.getFile(ROOT));
		if (root == null) return null;
		
		// read CVS/Repository
		String repository = readFirstLine(cvsSubDir.getFile(REPOSITORY));
		if (repository == null) return null;
		
		// read CVS/Tag
		String tag = readFirstLine(cvsSubDir.getFile(TAG));
		CVSTag cvsTag = (tag != null) ? new CVSEntryLineTag(tag) : null;

		// read Entries.Static
		String staticDir = readFirstLine(cvsSubDir.getFile(STATIC));
		boolean isStatic = (staticDir != null);
		
		// return folder sync
		return new FolderSyncInfo(repository, root, cvsTag, isStatic);		
	}
	
	/**
	 * Writes the CVS/Root, CVS/Repository, CVS/Tag, and CVS/Entries.static files to the
	 * specified folder using the data contained in the specified FolderSyncInfo instance.
	 */
	public static void writeFolderSync(ICVSFolder folder, FolderSyncInfo info) throws CVSException {
		ICVSFolder cvsSubDir = createCVSSubdirectory(folder);

		// write CVS/Root
		writeLines(cvsSubDir.getFile(ROOT), new String[] {info.getRoot()});
		
		// write CVS/Repository
		writeLines(cvsSubDir.getFile(REPOSITORY), new String[] {info.getRepository()});
		
		// write CVS/Tag
		ICVSFile tagFile = cvsSubDir.getFile(TAG);
		if (info.getTag() != null) {
			writeLines(tagFile, new String[] {info.getTag().toEntryLineFormat(false)});
		} else {
			if(tagFile.exists()) {
				tagFile.delete();
			}
		}
		
		// write CVS/Entries.Static
		ICVSFile staticFile = cvsSubDir.getFile(STATIC);
		if(info.getIsStatic()) {
			// the existance of the file is all that matters
			writeLines(staticFile, new String[] {""}); //$NON-NLS-1$
		} else {
			if(staticFile.exists()) {
				staticFile.delete();
			}
		}
	}

	/**
	 * Returns all .cvsignore entries for the specified folder.
	 */
	public static String[] readCVSIgnoreEntries(ICVSFolder folder) throws CVSException {
		ICVSFile ignoreFile = folder.getFile(IGNORE_FILE);
		if (ignoreFile != null) {
			return readLines(ignoreFile);
		}
		return null;
	}
	
	/**
	 * Adds a .cvsignore entry to the folder for the specified file.
	 */
	public static void addCVSIgnoreEntries(ICVSFolder folder, String[] patterns) throws CVSException {
		ICVSFile ignoreFile = folder.getFile(IGNORE_FILE);
		writeLines(ignoreFile, patterns);
	}	

	/**
	 * Returns the CVS subdirectory for this folder.
	 */
	public static ICVSFolder getCVSSubdirectory(ICVSFolder folder) throws CVSException {
		return folder.getFolder("CVS"); //$NON-NLS-1$
	}
	
	/**
	 * Creates and returns a CVS subdirectory in this folder.
	 */
	public static ICVSFolder createCVSSubdirectory(ICVSFolder folder) throws CVSException {
		ICVSFolder cvsSubDir = getCVSSubdirectory(folder);
		if (! cvsSubDir.exists()) {
			cvsSubDir.mkdir();
		}
		return cvsSubDir;
	}

	/*
	 * Reads the first line of the specified file.
	 * Returns null if the file does not exist, or the empty string if it is blank.
	 */
	private static String readFirstLine(ICVSFile file) throws CVSException {
		if (! file.exists()) return null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			try {
				String line = reader.readLine();
				if (line == null) return ""; //$NON-NLS-1$
				return line;
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Reads all lines of the specified file.
	 * Returns null if the file does not exist.
	 */
	private static String[] readLines(ICVSFile file) throws CVSException {
		if (! file.exists()) return null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			List fileContentStore = new ArrayList();
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					fileContentStore.add(line);
				}
				return (String[]) fileContentStore.toArray(new String[fileContentStore.size()]);
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Writes all lines to the specified file, using linefeed terminators for
	 * compatibility with other CVS clients.
	 */
	private static void writeLines(ICVSFile file, String[] contents) throws CVSException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		writeLinesToStreamAndClose(os, contents);
		file.setContents(new ByteArrayInputStream(os.toByteArray()), ICVSFile.UPDATED, false, Policy.monitorFor(null));
	}
	
	private static void writeLinesToStreamAndClose(OutputStream os, String[] contents)
		throws CVSException {
		try {
			try {
				for (int i = 0; i < contents.length; i++) {
					os.write(contents[i].getBytes()); // XXX should we specify a character encoding?
					os.write(0x0A); // newline byte
				}
			} finally {
				os.close();
			}
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
	}

	/*
	 * Appends lines to the specified file, using linefeed terminators.
	 */
	private static void appendLines(ICVSFile file, String[] contents) throws CVSException {
		OutputStream os = new BufferedOutputStream(file.getAppendingOutputStream());
		writeLinesToStreamAndClose(os, contents);
	}
}
