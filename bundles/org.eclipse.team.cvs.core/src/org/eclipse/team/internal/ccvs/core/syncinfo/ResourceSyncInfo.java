package org.eclipse.team.internal.ccvs.core.syncinfo;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.text.ParseException;
import java.util.Date;

import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.CVSDateFormatter;
import org.eclipse.team.internal.ccvs.core.util.EmptyTokenizer;

/**
 * Value (immutable) object that represents workspace state information about a resource contained in
 * a CVS repository. It is a specialized representation of a line in the CVS/Entry file with the addition of 
 * file permissions.
 * <p>
 * ResourceSyncInfo instances are created from entry lines from the CVS server and from the CVS/Entries
 * file. Although both entry lines have slightly different formats (e.g. timestamps) they can safely be passed
 * to the constructor.</p>
 * <p>
 * A class named <code>MutableResourceSyncInfo</code> can be used to modify an existing resource
 * sync or create sync info without an entry line.</p>
 * 
 * Example entry line from the CVS/Entry file:
 * 
 * /new.java/1.2/Fri Dec  7 00:17:52 2001/-kb/
 * D/src////
 *  
 * @see MutableResourceSyncInfo
 * @see ICVSResource#getSyncInfo()
 */
public class ResourceSyncInfo {
		
	public static final Date DUMMY_DATE = new Date(0);
	
	// safe default permissions. Permissions are saved separatly so that the correct permissions
	// can be sent back to the server on systems that don't save execute bits (e.g. windows).
	public static final String DEFAULT_PERMISSIONS = "u=rw,g=rw,o=r"; //$NON-NLS-1$
	// file sync information can be associated with a local resource that has been deleted. This is
	// noted by prefixing the revision with this character.
	// XXX Should this be private
	public static final String DELETED_PREFIX = "-"; //$NON-NLS-1$
	
	// a sync element with a revision of '0' is considered a new file that has
	// not been comitted to the repo. Is visible so that clients can create sync infos
	// for new files.
	public static final String ADDED_REVISION = "0"; //$NON-NLS-1$
	
	// Timestamp constants used to identify special cases
	protected static final int TYPE_REGULAR = 1;
	protected static final int TYPE_MERGED = 2;
	protected static final int TYPE_MERGED_WITH_CONFLICTS = 3;
	
	protected static final String TIMESTAMP_DUMMY = "dummy timestamp"; //$NON-NLS-1$
	protected static final String TIMESTAMP_MERGED = "Result of merge"; //$NON-NLS-1$
	protected static final String TIMESTAMP_MERGED_WITH_CONFLICT = TIMESTAMP_MERGED + "+"; //$NON-NLS-1$
	
	protected static final String TIMESTAMP_SERVER_MERGED = "+modified"; //$NON-NLS-1$
	protected static final String TIMESTAMP_SERVER_MERGED_WITH_CONFLICT = "+="; //$NON-NLS-1$
	
	// a directory sync info will have nothing more than a name
	protected boolean isDirectory = false;
	protected boolean isDeleted = false;
	
	// utility constants
	protected static final String DIRECTORY_PREFIX = "D/"; //$NON-NLS-1$
	protected static final String SEPERATOR = "/"; //$NON-NLS-1$
	
	// fields describing the synchronization of a resource in CVS parlance
	protected String name;
	protected String revision;
	protected Date timeStamp;
	protected KSubstOption keywordMode;
	protected CVSEntryLineTag tag;
	protected String permissions;
	
	// type of sync
	protected int syncType = TYPE_REGULAR;
	protected ResourceSyncInfo() {
	}
	/**
	 * Constructor to create a sync object from entry line formats. The entry lines are parsed by this class.
	 * The constructor can handle parsing entry lines from the server or from an entry file.
	 * 
	 * @param entryLine the entry line (e.g.  /new.java/1.2/Fri Dec 07 00:17:52 2001/-kb/)
	 * @param permissions the file permission (e.g. u=rw,g=rw,o=r). May be <code>null</code>.
	 * @param timestamp if not included in the entry line. May be <code>null</code>.
	 * 
	 * @exception CVSException is thrown if the entry cannot be parsed.
	 */
	public ResourceSyncInfo(String entryLine, String permissions, Date timestamp) throws CVSException {
		Assert.isNotNull(entryLine);
		setEntryLine(entryLine);
		
		if (permissions != null)  {
			this.permissions = permissions;
		}
		// override the timestamp that may of been in entryLine. In some cases the timestamp is not in the
		// entry line (e.g. receiving entry lines from the server versus reading them from the Entry file).
		if(timestamp!=null) {
			this.timeStamp = timestamp;
		}
	}
	
	/**
	 * Constructor to create a resource sync object for a folder.
	 * 
	 * @param name of the resource for which this sync state is associatied, cannot be <code>null</code>.
	 */
	public ResourceSyncInfo(String name) {
		Assert.isNotNull(name);
		this.name = name;
		this.isDirectory = true;
	}
	/**
	 * Answers if this sync information is for a folder in which case only a name is
	 * available.
	 * 
	 * @return <code>true</code> if the sync information is for a folder and <code>false</code>
	 * if it is for a file.
	 */
	public boolean isDirectory() {
		return isDirectory;
	}
	
	/**
	 * Answers if this sync information is for a resource that has been merged by the cvs server with
	 * conflicts and has not been modified yet relative to the given timestamp.
	 * 
	 * @param otherTimestamp is the timestamp of the file associated with this resource sync
	 * @return <code>true</code> if the sync information is for a file that has been merged and
	 * <code>false</code> for folders and for files that have not been merged.
	 */
	public boolean isNeedsMerge(Date otherTimestamp) {
		return syncType == TYPE_MERGED_WITH_CONFLICTS && timeStamp.equals(otherTimestamp);
	}
	
	/**
	 * Answers if this sync information is for a resource that has been merged with conflicts by the 
	 * cvs server.
	 * 
	 * @return <code>true</code> if the sync information is for a file that has been merged and
	 * <code>false</code> for folders and for files that have not been merged.
	 */
	public boolean isMergedWithConflicts() {
		return syncType == TYPE_MERGED_WITH_CONFLICTS;
	}
	
	/**
	 * Answers if this sync information is for a resource that has been merged by the cvs server.
	 * 
	 * @return <code>true</code> if the sync information is for a file that has been merged and
	 * <code>false</code> for folders and for files that have not been merged.
	 */
	public boolean isMerged() {
		return syncType == TYPE_MERGED || isMergedWithConflicts();
	}
	
	/**
	 * Answers if this sync information is for a file that has been added but not comitted
	 * to the CVS repository yet.
	 * 
	 * @return <code>true</code> if the sync information is new or <code>false</code> if 
	 * the sync is for an file that exists remotely. For folder sync info this returns
	 * <code>false</code>.
	 */
	public boolean isAdded() {
		if(!isDirectory) {
			return getRevision().equals(ADDED_REVISION);
		} else {
			return false;
		}
	}
	
	/**
	 * Answers if this sync information is for a file that is scheduled to be deleted
	 * from the repository but the deletion has not yet been comitted.
	 * 
	 * @return <code>true</code> if the sync information is deleted or <code>false</code> if 
	 * the sync is for an file that exists remotely.
	 */
	public boolean isDeleted() {
		return isDeleted;
	}
	
	/**
	 * Returns an entry line that can be saved in the CVS/Entries file. For sending entry lines to the
	 * server use <code>getServerEntryLine</code>.
	 * 
	 * @return a file or folder entry line reflecting the state of this sync object.
	 */
	public String getEntryLine() {
		return getEntryLine(true /*include timestamps*/, null /*no timestamp override*/);
	}
		
	/**
	 * Same as <code>getEntryLine</code> except it considers merged files in entry line timestamp format. 
	 * This is only valid for sending the file to the server.
	 * 
	 * @param fileTimestamp is timestamp of the resource associated with this sync info.
	 * @return a file or folder entry line reflecting the state of this sync object.
	 */
	public String getServerEntryLine(Date fileTimestamp) {
		String serverTimestamp;
		if(fileTimestamp != null && (isMerged() || isMergedWithConflicts())) {
			if(isNeedsMerge(fileTimestamp)) {
				serverTimestamp = TIMESTAMP_SERVER_MERGED_WITH_CONFLICT;
			} else {
				serverTimestamp = TIMESTAMP_SERVER_MERGED;
			}
			return getEntryLine(true, serverTimestamp);
		} else {
			return getEntryLine(false, null);
		}		
	}
	
	/**
	 * Anwsers a compatible permissions line for files.
	 * 
	 * @return a permission line for files and <code>null</code> if this sync object is
	 * a directory.
	 */
	public String getPermissionLine() {
		if(isDirectory) {
			return null;
		} else {
			String permissions = this.permissions;
			if (permissions == null)
				permissions = DEFAULT_PERMISSIONS;
			return SEPERATOR + name + SEPERATOR + permissions;
		}
	}
	
	/**
	 * Gets the permissions or <code>null</code> if permissions are not available.
	 * 
	 * @return a string of the format "u=rw,g=rw,o=r"
	 */
	public String getPermissions() {
		if(isDirectory) {
			return null;
		} else {
			if(permissions==null) {
				return DEFAULT_PERMISSIONS;
			} else {
				return permissions;
			}
		}
	}
	/**
	 * Gets the tag or <code>null</code> if a tag is not available.
	 * 
	 * @return Returns a String
	 */
	public CVSTag getTag() {
		return tag;
	}
	/**
	 * Gets the timeStamp or <code>null</code> if a timestamp is not available.
	 * 
	 * @return a date instance representing the timestamp
	 */
	public Date getTimeStamp() {
		return timeStamp;
	}
	/**
	 * Gets the version or <code>null</code> if this is a folder sync info. The returned
	 * revision will never include the DELETED_PREFIX. To found out if this sync info is
	 * for a deleted resource call isDeleted().
	 * 
	 * @return Returns a String
	 */
	public String getRevision() {
		return revision;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}
	/**
	 * Gets the keyword mode.
	 * @return the keyword substitution option
	 */
	public KSubstOption getKeywordMode() {
		return keywordMode;
	}
	
	/**
	 * Name equality between resource sync info objects.
	 */
	public boolean equals(Object other) {
		if(other instanceof ResourceSyncInfo) {
			ResourceSyncInfo syncInfo = ((ResourceSyncInfo)other);
			if(other == this) return true;
			if(getName() == syncInfo.getName()) return true;
			return getName().equals(syncInfo.getName());
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return getName().hashCode();
	}
	
	/*
	 * @see Object#toString()
	 */
	public String toString() {
		return getEntryLine(true, null /*no timestamp override*/);
	}
	public MutableResourceSyncInfo cloneMutable() {
		MutableResourceSyncInfo newSync = new MutableResourceSyncInfo(this);
		return newSync;
	}
	/**
	 * Sets the tag for the resource.
	 */
	protected void setTag(CVSTag tag) {
		if(tag!=null) {
			this.tag = new CVSEntryLineTag(tag);
		} else {
			this.tag = null;
		}					
	}
	
		
	/*
	 * Sets the sync type
	 */
	protected void setSyncType(int syncType) {
		this.syncType = syncType;
	}
	/**
	 * Sets the version and decides if the revision is for a deleted resource the revision field
	 * will not include the deleted prefix '-'.
	 * 
	 * @param version the version to set
	 */
	protected void setRevision(String revision) {
		if(revision.startsWith(DELETED_PREFIX)) {
			this.revision = revision.substring(DELETED_PREFIX.length());
			isDeleted = true;
		} else {
			if(revision.equals(ADDED_REVISION)) {
				this.timeStamp = DUMMY_DATE;
			}
			this.revision = revision;
			isDeleted = false;
		}
	}
	
	/**
	 * Set the entry line 
	 * 
	 * @throws CVSException if the entryLine is malformed
	 */
	private void setEntryLine(String entryLine) throws CVSException {
		if(entryLine.startsWith(DIRECTORY_PREFIX)) {
			isDirectory = true;
			entryLine = entryLine.substring(1);
		} else {
			isDirectory = false;
		}
		EmptyTokenizer tokenizer = new EmptyTokenizer(entryLine,SEPERATOR);
		if(tokenizer.countTokens() != 5) {
			throw new CVSException(Policy.bind("Malformed_entry_line___11") + entryLine); //$NON-NLS-1$
		}
		
		name = tokenizer.nextToken();
		
		if(name.length()==0) {
			throw new CVSException(Policy.bind("Malformed_entry_line,_missing_name___12") + entryLine); //$NON-NLS-1$
		}
		
		String rev = tokenizer.nextToken();
		
		if(rev.length()==0 && !isDirectory()) {
			throw new CVSException(Policy.bind("Malformed_entry_line,_missing_revision___13") + entryLine); //$NON-NLS-1$
		} else {
			setRevision(rev);
		}
	
		String date = tokenizer.nextToken();
		
		// possible timestamps are:
		// from server: "+=" and "+modified"
		// from entry line: "Result of Merge+Thu May 25 12:33:33 2002"
		//							 "Result of Merge"
		//							"Thu May 25 12:33:33 2002"
		//
		// The server will send a timestamp of "+=" if
		// the file was merged with conflicts. The '+' indicates that there are conflicts and the
		// '=' indicate that the timestamp for the file should be used. If the merge does not
		// have conflicts, simply add a text only timestamp and the file will be regarded as
		// having outgoing changes.
		// The purpose for having the two different timestamp options for merges is to 
		// dissallow commit of files that have conflicts until they have been manually edited.			
		if(date.indexOf(ResourceSyncInfo.TIMESTAMP_SERVER_MERGED) != -1) {
			syncType = TYPE_MERGED;
			date = null;
		} else if(date.indexOf(ResourceSyncInfo.TIMESTAMP_SERVER_MERGED_WITH_CONFLICT) != -1) {
			syncType = TYPE_MERGED_WITH_CONFLICTS;
			date = null;
		} else if(date.indexOf(TIMESTAMP_MERGED_WITH_CONFLICT)!=-1) {
			date = date.substring(date.indexOf("+") + 1); //$NON-NLS-1$
			syncType = TYPE_MERGED_WITH_CONFLICTS;
		} else if(date.indexOf(TIMESTAMP_MERGED)!=-1) {
			syncType = TYPE_MERGED;
			date = null;
		}
		
		if(date==null || "".equals(date)) { //$NON-NLS-1$
			timeStamp = null;	
		} else {
			try {	
				timeStamp = CVSDateFormatter.entryLineToDate(date);
			} catch(ParseException e) {
				// something we don't understand, just make this sync have no timestamp and
				// never be in sync with the server.
				timeStamp = DUMMY_DATE;
			}
		}
		keywordMode = KSubstOption.fromMode(tokenizer.nextToken());
		String tagEntry = tokenizer.nextToken();
						
		if(tagEntry.length()>0) {
			tag = new CVSEntryLineTag(tagEntry);
		} else {
			tag = null;
		}
	}
	
	private String getEntryLine(boolean includeTimeStamp, String timestampOverride) {
		StringBuffer result = new StringBuffer();
		
		if(isDirectory) {
			result.append(DIRECTORY_PREFIX);
			result.append(name + "////"); //$NON-NLS-1$
		} else {
			result.append(SEPERATOR);
			result.append(name);
			result.append(SEPERATOR);
			
			if(isDeleted){
				result.append(DELETED_PREFIX); 
			}
			result.append(revision);
			result.append(SEPERATOR);
			if(includeTimeStamp) {
				String entryLineTimestamp = ""; //$NON-NLS-1$
				if(timestampOverride!=null) {
					entryLineTimestamp = timestampOverride;
				} else {					
					switch(syncType) {
						case TYPE_REGULAR:
							if(timeStamp==DUMMY_DATE || timeStamp==null) {
								entryLineTimestamp = TIMESTAMP_DUMMY;
							} else {
								entryLineTimestamp = CVSDateFormatter.dateToEntryLine(timeStamp);
							} break;
						case TYPE_MERGED:
							entryLineTimestamp = TIMESTAMP_MERGED; break;
						case TYPE_MERGED_WITH_CONFLICTS:
							entryLineTimestamp = TIMESTAMP_MERGED_WITH_CONFLICT + CVSDateFormatter.dateToEntryLine(timeStamp); break;
					}						
				}
				result.append(entryLineTimestamp);
			}
			result.append(SEPERATOR);
			if (keywordMode != null) result.append(keywordMode.toMode());
			result.append(SEPERATOR);
			if (tag != null) {
				result.append(tag.toEntryLineFormat(true));
			}
		}
		return result.toString();
	}
}