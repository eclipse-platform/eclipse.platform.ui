package org.eclipse.team.internal.ccvs.core.resources.api;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Set;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.EmptyTokenizer;

/**
 * A FileProperties bundles all the CVS information stored on disk
 * about a file such as the name, revision, sever timestamp, etc. 
 * It does not contain information about the physical file in the local file system.
 */
public class FileProperties extends CVSProperties {
	
	private String name;
	
	private String version;
	private String timeStamp;
	private String keywordMode;
	private String tag;

	public static final String BINARY_TAG = "-kb";
	
	public static final String ENTRIES = "Entries";
	public static final String PERMISSIONS = "Permissions";
	public static final String seperator = "/";
	
	/** 
	 * Construct the FileProperties
	 */
	public FileProperties() {
		super(new String[]{ENTRIES,PERMISSIONS});
	}
	
	/** 
	 * Construct the FileProperties
	 */
	public FileProperties(String entryLine, String permissions) throws CVSException {
		this();
		setEntryLine(entryLine);
		setPermissions(permissions);
	}

	/**
	 * Cosntruct a CVS compatible entry line 
	 * that can be stored on disk.
	 * @return null if the entry line was not set or set to null
	 */
	public String getEntryLine() {
		
		if (name == null) {
			return null;
		}
		
		StringBuffer result = new StringBuffer();
		
		result.append(seperator);
		result.append(name);
		result.append(seperator);
		result.append(version);
		result.append(seperator);
		result.append(timeStamp);
		result.append(seperator);
		result.append(keywordMode);
		result.append(seperator);
		result.append(tag);

		return result.toString();
	}
	
	/**
	 * Cosntruct an entry line for the client that can be sent to the server.
	 * For this the timestamp is not included.
	 * @return null if the entry line was not set or set to null
	 */
	public String getEntryLineForServer() {

		if (name == null) {
			return null;
		}
				
		StringBuffer result = new StringBuffer();
		
		result.append(seperator);
		result.append(name);
		result.append(seperator);
		result.append(version);
		result.append(seperator);
		result.append(seperator);
		result.append(keywordMode);
		result.append(seperator);
		result.append(tag);

		return result.toString();
	}
			
	/**
	 * Set the entry line 
	 * @throws CVSException if the entryLine is malformed
	 */
	public void setEntryLine(String entryLine) throws 
			IllegalArgumentException {
		
		EmptyTokenizer tokenizer;
		
		if (entryLine == null) {
			name = null;
			return;
		}

		tokenizer = new EmptyTokenizer(entryLine,seperator);
		
		Assert.isLegal(entryLine.startsWith(seperator) &&
					   tokenizer.countTokens() == 5,
					   Policy.bind("FileProperties.invalidEntryLine"));
		
		name = tokenizer.nextToken();
		version = tokenizer.nextToken();
		timeStamp = tokenizer.nextToken();
		keywordMode = tokenizer.nextToken();
		tag = tokenizer.nextToken();
	}

	/**
	 * Gets the permissions
	 * @return Returns a String
	 */
	public String getPermissions() {
		return getProperty(PERMISSIONS);

	}
	/**
	 * Sets the permissions
	 * @param permissions The permissions to set
	 */
	public void setPermissions(String permissions) {
		putProperty(PERMISSIONS,permissions);
	}

	/**
	 * Gets the tag
	 * @return Returns a String
	 */
	public String getTag() {
		return tag;
	}
	/**
	 * Sets the tag
	 * @param tag The tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
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
	          has the format "Thu Oct 18 20:21:13 2001" otherwise
	          isDirty is allways true
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}


	/**
	 * Gets the version
	 * @return Returns a String
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * Sets the version
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
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

	/**
	 * Special handling for the entries added.
	 * @see CVSProperties#getProperty(String)
	 */
	public String getProperty(String key) {
		
		String data;
		
		if (ENTRIES.equals(key)) {
			
			return getEntryLine();
			
		} else {
			data = super.getProperty(key);
			
			if (data == null) {
				return null;
			}
			
			if (data.substring(0,1).equals(seperator)) {
				data = data.substring(data.indexOf(seperator,1)+1);
			}
			return data;
		}
	}

	/**
	 * Special handling for the entries added.
	 * @see CVSProperties#putProperty(String,String)
	 */
	public String putProperty(String key, String value) 
			throws IllegalArgumentException {

		if (ENTRIES.equals(key)) {
			setEntryLine(value);
			return value;
		} else {
			return super.putProperty(key,value);
		}
	}
	
	/**
	 * Put the entries into the entries-properties. This is
	 * done before equals.
	 */
	private void putEntries() {
		super.putProperty(ENTRIES,getEntryLine());
	}
	
	public boolean equals(Object o) {
		
		if (o instanceof FileProperties) {
			putEntries();
			((FileProperties)o).putEntries();
		}
		
		return super.equals(o);
	}
}

