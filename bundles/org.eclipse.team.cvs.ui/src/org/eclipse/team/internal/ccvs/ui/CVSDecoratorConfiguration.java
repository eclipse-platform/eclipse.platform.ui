package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSStatus;

public class CVSDecoratorConfiguration {

	// bindings for 
	public static final String RESOURCE_NAME = "name";
	public static final String RESOURCE_TAG = "tag";
	public static final String FILE_REVISION = "revision";
	public static final String FILE_KEYWORD = "keyword";
	
	// bindings for repository location
	public static final String REMOTELOCATION_METHOD = "method";
	public static final String REMOTELOCATION_USER = "user";
	public static final String REMOTELOCATION_HOST = "host";
	public static final String REMOTELOCATION_ROOT = "root";
	public static final String REMOTELOCATION_REPOSITORY = "repository";
	
	// bindings for resource states
	public static final String DIRTY_FLAG = "dirty_flag";
	public static final String ADDED_FLAG = "added_flag";
	public static final String DEFAULT_DIRTY_FLAG = ">";
	public static final String DEFAULT_ADDED_FLAG = "*";
	
	// default text decoration formats
	public static final String DEFAULT_FILETEXTFORMAT = "{name}  {revision} {tag}";
	public static final String DEFAULT_FOLDERTEXTFORMAT = "{name}  {tag}";
	public static final String DEFAULT_PROJECTTEXTFORMAT = "{name}  {tag} [{host}]";

	// prefix characters that can be removed if the following binding is not found
	private static final char KEYWORD_SEPSPACE = ' ';
	private static final char KEYWORD_SEPCOLON = ':';
	private static final char KEYWORD_SEPAT = '@';
	
	/**
	 *  Answers if the given format specification for a file is valid.
	 */
	public static IStatus validateFileTextFormat(String format) {
		if (format == null)
			return new CVSStatus(CVSStatus.ERROR, "format cannot be null");
		if (format.equals(""))
			return new CVSStatus(CVSStatus.ERROR, "format cannot be empty");		
		
		return new CVSStatus(IStatus.OK, "ok");
	}
	
	/**
	 *  Answers if the given format specification for a folder/project is valid.
	 */
	public static IStatus validateContainerTextFormat(String format) {
		if (format == null)
			return new CVSStatus(CVSStatus.ERROR, "format cannot be null");
		if (format.equals(""))
			return new CVSStatus(CVSStatus.ERROR, "format cannot be empty");		
		
		return new CVSStatus(IStatus.OK, "ok");
	}
	
	public static String bind(String format, Map bindings) {
		StringBuffer output = new StringBuffer(80);
		int length = format.length();
		int start = -1;
		int end = length;
		while (true) {
			if ((end = format.indexOf('{', start)) > -1) {
				output.append(format.substring(start + 1, end));
				if ((start = format.indexOf('}', end)) > -1) {
					String s = (String)bindings.get(format.substring(end + 1, start));
					if(s!=null) {
						output.append(s);
					} else {
						// support for removing prefix character if binding is null
						int curLength = output.length();
						if(curLength>0) {
							char c = output.charAt(curLength - 1);
							if(c == KEYWORD_SEPCOLON || c == KEYWORD_SEPSPACE || c == KEYWORD_SEPCOLON) {
								output.deleteCharAt(curLength - 1);							
							}
						}
					}
				} else {
					output.append(format.substring(end, length));
					break;
				}
			} else {
				output.append(format.substring(start + 1, length));
				break;
			}
		}
		return output.toString();
	}
}
