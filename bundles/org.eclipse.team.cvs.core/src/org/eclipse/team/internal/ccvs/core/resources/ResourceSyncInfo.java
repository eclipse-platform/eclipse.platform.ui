package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.EmptyTokenizer;

public class ResourceSyncInfo {
	
	private boolean isDirectory = false;
	
	private static final String DIRECTORY_PREFIX = "D/";
	public static final String BINARY_TAG = "-kb";
	private static final String SEPERATOR = "/";
	
	private static final String DEFAULT_PERMISSIONS = "u=rw,g=rw,o=r";
	
	private int type;
	private String name;
	private String revision;
	private String timeStamp;
	private String keywordMode;
	private CVSEntryLineTag tag;
	private String permissions;

	public ResourceSyncInfo(String entryLine, String permissions) throws CVSException {
		setEntryLine(entryLine);
		if (permissions != null) 
			setPermissions(permissions);			
	}
	
	public ResourceSyncInfo(String name, boolean isDirectory) {
		Assert.isNotNull(name);
		this.name = name;
		this.isDirectory = isDirectory;
	}

	public boolean isDirectory() {
		return isDirectory;
	}
	
	/**
	 * Cosntruct a CVS compatible entry line 
	 * that can be stored on disk.
	 * @return null if the entry line was not set or set to null
	 */
	public String getEntryLine(boolean includeTimeStamp) {
		
		if(name == null) {
			return null;
		}
		
		StringBuffer result = new StringBuffer();
		
		if(isDirectory) {
			result.append(DIRECTORY_PREFIX);
			result.append(name + "////");
		} else {
			result.append(SEPERATOR);
			result.append(name);
			result.append(SEPERATOR);
			result.append(revision);
			result.append(SEPERATOR);
			// in some cases the timestamp not include in entry lines
			if(includeTimeStamp) {
				result.append(timeStamp);
			}
			result.append(SEPERATOR);
			result.append(keywordMode);
			result.append(SEPERATOR);
			if (tag != null) {
				result.append(tag.toEntryLineFormat());
			}
		}

		return result.toString();
	}
	
	public String getPermissionLine() {
		if(isDirectory) {
			return null;
		} else {
			return SEPERATOR + name + SEPERATOR + permissions;
		}
	}
	
	/**
	 * Set the entry line 
	 * @throws CVSException if the entryLine is malformed
	 */
	public void setEntryLine(String entryLine) throws CVSException {

		Assert.isTrue(entryLine!=null);

//		if (entryLine == null) {
//			name = revision = timeStamp = tag = keywordMode = null;
//			return;
//		}

		//Assert.isLegal(entryLine.startsWith(seperator) &&
		//			   tokenizer.countTokens() == 5,
		//			   Policy.bind("FileProperties.invalidEntryLine"));

		if(entryLine.startsWith(DIRECTORY_PREFIX)) {
			isDirectory = true;
			entryLine = entryLine.substring(1);
		} else {
			isDirectory = false;
		}

		EmptyTokenizer tokenizer = new EmptyTokenizer(entryLine,SEPERATOR);

		name = tokenizer.nextToken();
		revision = tokenizer.nextToken();
		timeStamp = tokenizer.nextToken();
		keywordMode = tokenizer.nextToken();
		String tagEntry = tokenizer.nextToken();
		if(tagEntry.length()>0) {
			tag = new CVSEntryLineTag(tagEntry);
		}
	}

	/**
	 * Gets the permissions
	 * @return Returns a String
	 */
	public String getPermissions() {
		return permissions;

	}

	/**
	 * Sets the permissions
	 * 
	 *   /foo.java/u=rw,g=rw,o=rw
	 * 
	 * @param permissions The permissions to set
	 */
	public void setPermissionLine(String permissionLine) throws CVSException {

		if (permissionLine == null) {
			permissions = DEFAULT_PERMISSIONS;
			return;
		}

		EmptyTokenizer tokenizer;
		tokenizer = new EmptyTokenizer(permissionLine,SEPERATOR);
		String filename = tokenizer.nextToken();
		permissions = tokenizer.nextToken();
	}
	
	/**
	 * u=rw,g=rw,o=rw
	 */
	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	/**
	 * Gets the tag
	 * @return Returns a String
	 */
	public CVSTag getTag() {
		return tag;
	}
	/**
	 * Sets the tag for the resource. The provided tag must not be null.
	 * @param tag The tag to set 
	 */
	public void setTag(CVSTag tag) {
		if(tag!=null) {
			this.tag = new CVSEntryLineTag(tag);
		} else {
			this.tag = null;
		}					
	}

	/**
	 * Gets the timeStamp
	 * @return Returns a String usually in the format 
	            "Thu Oct 18 20:21:13 2001"
	 */
	public String getTimeStamp() {
		return timeStamp;
	}
	/**
	 * Sets the timeStamp
	 * 
	 * @param timeStamp The timeStamp to set
	 *         has the format "Thu Oct 18 20:21:13 2001" otherwise
	 *         isDirty is allways true
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Gets the version
	 * @return Returns a String
	 */
	public String getRevision() {
		return revision;
	}
	/**
	 * Sets the version
	 * @param version the version to set
	 */
	public void setRevision(String version) {
		this.revision = version;
	}

	/**
	 * Gets the name
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}
	/**
	 * Sets the name
	 * @param name The name to set
	 */
	public void setName(String name) {
		Assert.isTrue(name!=null);
		this.name = name;
	}

	/**
	 * Gets the keyword mode
	 * @return Returns a String
	 */
	public String getKeywordMode() {
		return keywordMode;
	}
	
	/**
	 * Sets the keyword mode
	 * @param keywordMode The keyword expansion mode (-kb, -ko, etc.)
	 */
	public void setKeywordMode(String keywordMode) {
		this.keywordMode = keywordMode;
	}
	
	public boolean equals(Object other) {
		if(other instanceof ResourceSyncInfo) {
			
			ResourceSyncInfo syncInfo = ((ResourceSyncInfo)other);
			
			// We have to avoid Null-Pointer-Exceptions, other and this are not null
			// for sure the rest has to be checked
			if(other == this) return true;
			if(getName() == syncInfo.getName()) return true;
			if ((getName()==null) != (syncInfo.getName()==null)) return false;
			return getName().equals(syncInfo.getName());
			
		} else {
			return false;
		}
	}
	
	public int hashCode() {
		return getName().hashCode();
	}
}