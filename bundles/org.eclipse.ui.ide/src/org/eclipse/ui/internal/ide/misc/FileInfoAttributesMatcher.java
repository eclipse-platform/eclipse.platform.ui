/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] initial API and implementation
 *     IBM Corporation - ongoing implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.misc;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.filtermatchers.AbstractFileInfoMatcher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.StringMatcher;

/**
 * A file info filter that matches different file and folder attributes.
 */
public class FileInfoAttributesMatcher extends AbstractFileInfoMatcher {

	public static String ID = "org.eclipse.ui.ide.multiFilter"; //$NON-NLS-1$

	public static String KEY_NAME 					= "name"; //$NON-NLS-1$
	public static String KEY_PROPJECT_RELATIVE_PATH = "projectRelativePath"; //$NON-NLS-1$
	public static String KEY_LOCATION 				= "location"; //$NON-NLS-1$
	public static String KEY_LAST_MODIFIED 			= "lastModified"; //$NON-NLS-1$
	public static String KEY_LENGTH 				= "length"; //$NON-NLS-1$
	public static String KEY_CREATED 				= "created"; //$NON-NLS-1$
	public static String KEY_IS_SYMLINK				= "isSymLink"; //$NON-NLS-1$
	public static String KEY_IS_READONLY			= "isReadOnly"; //$NON-NLS-1$

	public static String OPERATOR_NONE			= "none"; //$NON-NLS-1$
	public static String OPERATOR_LARGER_THAN	= "largerThan"; //$NON-NLS-1$
	public static String OPERATOR_SMALLER_THAN	= "smallerThan"; //$NON-NLS-1$
	public static String OPERATOR_EQUALS		= "equals"; //$NON-NLS-1$
	public static String OPERATOR_BEFORE		= "before"; //$NON-NLS-1$
	public static String OPERATOR_AFTER			= "after"; //$NON-NLS-1$
	public static String OPERATOR_WITHIN		= "within"; //$NON-NLS-1$
	public static String OPERATOR_MATCHES		= "matches"; //$NON-NLS-1$
	

	/**
	 * @param key
	 * @return
	 */
	public static String[] getOperatorsForKey(String key) {
		if (key.equals(KEY_NAME) || key.equals(KEY_PROPJECT_RELATIVE_PATH) || key.equals(KEY_LOCATION))
				return new String[] {OPERATOR_MATCHES};
		if (key.equals(KEY_IS_SYMLINK) || key.equals(KEY_IS_READONLY))
			return new String[] {OPERATOR_EQUALS};
		if (key.equals(KEY_LAST_MODIFIED) || key.equals(KEY_CREATED))
			return new String[] {OPERATOR_EQUALS, OPERATOR_BEFORE, OPERATOR_AFTER, OPERATOR_WITHIN};
		if (key.equals(KEY_LENGTH))
			return new String[] {OPERATOR_EQUALS, OPERATOR_LARGER_THAN, OPERATOR_SMALLER_THAN};
		
		return new String[] {OPERATOR_NONE};
	}
	
	/**
	 * @param key
	 * @param operator
	 * @return
	 */
	public static  Class getTypeForKey(String key, String operator) {
		if (key.equals(KEY_NAME) || key.equals(KEY_PROPJECT_RELATIVE_PATH) || key.equals(KEY_LOCATION))
			return String.class;
		if (key.equals(KEY_IS_SYMLINK) || key.equals(KEY_IS_READONLY))
			return Boolean.class;
		if (key.equals(KEY_LAST_MODIFIED) || key.equals(KEY_CREATED)) {
			if (operator.equals(OPERATOR_WITHIN))
				return Integer.class;
			return Date.class;
		}
		if (key.equals(KEY_LENGTH))
			return Integer.class;
		return String.class;
	}
	
	/**
	 * @return
	 */
	public static boolean supportCreatedKey() {
		if (Platform.getOS().equals(Platform.OS_WIN32) || Platform.getOS().equals(Platform.OS_MACOSX)) {
			String system = System.getProperty("java.version"); //$NON-NLS-1$
			double versionNumber = 0.0;
			int index = system.indexOf('.');
			if (index != -1) {
				versionNumber = Integer.decode(system.substring(0, index)).doubleValue();
				system = system.substring(index + 1);
				index = system.indexOf('.');
				if (index != -1) {
					versionNumber += Double.parseDouble(system.substring(0, index)) / 10.0;
				}
			}
			return versionNumber >= 1.7;
		}
		return false;
	}
	
	/**
	 * @since 3.6
	 *
	 */
	public static class Argument {
		public String key = KEY_NAME;
		public String pattern = new String();
		public String operator = OPERATOR_EQUALS;
		public boolean caseSensitive = false; 
		public boolean regularExpression = false;
	}
	
	/**
	 * @param argument
	 * @return
	 */
	public static String encodeArguments(Argument argument)  {
		return VERSION_IMPLEMENTATION + DELIMITER + 
				argument.key + DELIMITER + 
				argument.operator + DELIMITER + 
				Boolean.toString(argument.caseSensitive) + DELIMITER + 
				Boolean.toString(argument.regularExpression) + DELIMITER + 
				argument.pattern;
				
	}

	/**
	 * @param argument
	 * @return
	 */
	public static Argument decodeArguments(String argument)  {
		Argument result = new Argument();
		if (argument == null)
			return result;
		
		int index = argument.indexOf(DELIMITER);
		if (index == -1)
			return result;
		
		String version = argument.substring(0, index);
		argument = argument.substring(index + 1);
		
		if (!version.equals(VERSION_IMPLEMENTATION))
			return result;
		
		index = argument.indexOf(DELIMITER);
		if (index == -1)
			return result;

		result.key = argument.substring(0, index);
		argument = argument.substring(index + 1);

		index = argument.indexOf(DELIMITER);
		if (index == -1)
			return result;

		result.operator = argument.substring(0, index);
		argument = argument.substring(index + 1);

		index = argument.indexOf(DELIMITER);
		if (index == -1)
			return result;

		result.caseSensitive = Boolean.valueOf(argument.substring(0, index)).booleanValue();
		argument = argument.substring(index + 1);
		
		index = argument.indexOf(DELIMITER);
		if (index == -1)
			return result;

		result.regularExpression = Boolean.valueOf(argument.substring(0, index)).booleanValue();
		result.pattern = argument.substring(index + 1);

		return result;
	}


	static private String DELIMITER = "-"; //$NON-NLS-1$
	static private String VERSION_IMPLEMENTATION = "1.0"; //$NON-NLS-1$

	/*
	 * return value in milliseconds since epoch(1970-01-01T00:00:00Z)
	 */
	private static long getFileCreationTime(String fullPath) {
		/*
		java.nio.file.FileSystem fs = java.nio.file.FileSystems.getDefault();
		java.nio.file.FileRef fileRef = fs.getPath(file);
		java.nio.file.attribute.BasicFileAttributes attributes = java.nio.file.attribute.Attributes.readBasicFileAttributes(fileRef, new java.nio.file.LinkOption[0]);
		return attributes.creationTime();
        */

		try {
			Class fileSystems = Class.forName("java.nio.file.FileSystems"); //$NON-NLS-1$
			Method getDefault = fileSystems.getMethod("getDefault", null); //$NON-NLS-1$
			Object fs = getDefault.invoke(null, null);
	
			Class fileRef = Class.forName("java.nio.file.FileRef"); //$NON-NLS-1$

			Class fileSystem = Class.forName("java.nio.file.FileSystem"); //$NON-NLS-1$
			Method getPath = fileSystem.getMethod("getPath", new Class[] {String.class}); //$NON-NLS-1$
			Object fileRefObj = getPath.invoke(fs, new Object[] {fullPath});
			
			Class attributes = Class.forName("java.nio.file.attribute.Attributes"); //$NON-NLS-1$
			Class linkOptions = Class.forName("java.nio.file.LinkOption"); //$NON-NLS-1$
			Object linkOptionsEmptyArray = Array.newInstance(linkOptions, 0);
			Method readBasicFileAttributes = attributes.getMethod("readBasicFileAttributes", new Class[] {fileRef, linkOptionsEmptyArray.getClass()}); //$NON-NLS-1$
			Object attributesObj = readBasicFileAttributes.invoke(null, new Object[] {fileRefObj, linkOptionsEmptyArray});
	
			Class basicAttributes = Class.forName("java.nio.file.attribute.BasicFileAttributes"); //$NON-NLS-1$
			Method creationTime = basicAttributes.getMethod("creationTime", null); //$NON-NLS-1$
			Object time = creationTime.invoke(attributesObj, null);
	
			Class fileTime = Class.forName("java.nio.file.attribute.FileTime"); //$NON-NLS-1$
			Method toMillis = fileTime.getMethod("toMillis", null); //$NON-NLS-1$
			Object result = toMillis.invoke(time, null);
			
			if (result instanceof Long)
				return ((Long) result).longValue();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} 
		return 0;
	}
	
	MatcherCache matcher = null;
	private boolean fSupportsCreatedKey;

	class MatcherCache {
		public MatcherCache(String arguments) {
			argument = decodeArguments(arguments);
			type = getTypeForKey(argument.key, argument.operator);
			if (type.equals(String.class)) {
				if (argument.regularExpression == false)
					stringMatcher = new StringMatcher(argument.pattern, argument.caseSensitive == false, false);
				else
					regExPattern = Pattern.compile(argument.pattern, argument.caseSensitive ? 0:Pattern.CASE_INSENSITIVE);
			}
		}
		

		Argument argument;
		Class type;
		StringMatcher stringMatcher = null;
		Pattern regExPattern = null;

		public boolean match(IContainer parent, IFileInfo fileInfo) {
			if (type.equals(String.class)) {
				String value = new String();
				if (argument.key.equals(KEY_NAME))
					value = fileInfo.getName();
				if (argument.key.equals(KEY_PROPJECT_RELATIVE_PATH))
					value = parent.getProjectRelativePath().append(fileInfo.getName()).toPortableString();
				if (argument.key.equals(KEY_LOCATION))
					value = parent.getLocation().append(fileInfo.getName()).toOSString();
				
				if (stringMatcher != null)
					return stringMatcher.match(value);
				if (regExPattern != null) {
					Matcher m = regExPattern.matcher(value);
					return m.matches();
				}
			}
			if (type.equals(Integer.class)) {
				int amount;
				try {
					amount = Integer.parseInt(argument.pattern);
				} catch (NumberFormatException e) {
					amount = 0;
				}
				if (argument.key.equals(KEY_LAST_MODIFIED) || argument.key.equals(KEY_CREATED)) {
					// OPERATOR_WITHIN
					long time = 0;
					if (argument.key.equals(KEY_LAST_MODIFIED)) {
						IFileInfo info = fetchInfo(parent, fileInfo);
						if (!info.exists())
							return false;
						time = info.getLastModified();
					}
					if (argument.key.equals(KEY_CREATED)) {
						if (!fSupportsCreatedKey)
							return false;
						time = getFileCreationTime(parent.getLocation().append(fileInfo.getName()).toOSString());
					}
					GregorianCalendar gc = new GregorianCalendar();
					gc.add(Calendar.SECOND,-amount);
					Date when = gc.getTime();
					Date then = new Date(time);
					return then.after(when);
				}
				if (argument.key.equals(KEY_LENGTH)) {
					IFileInfo info = fetchInfo(parent, fileInfo);
					if (!info.exists())
						return false;
					if (argument.operator.equals(OPERATOR_EQUALS))
						return info.getLength() == amount;
					if (argument.operator.equals(OPERATOR_LARGER_THAN))
						return info.getLength() > amount;
					if (argument.operator.equals(OPERATOR_SMALLER_THAN))
						return info.getLength() < amount;
				}
			}
			if (type.equals(Date.class)) {
				long parameter = Long.parseLong(argument.pattern);
				if (argument.key.equals(KEY_LAST_MODIFIED) || argument.key.equals(KEY_CREATED)) {
					long time = 0;
					if (argument.key.equals(KEY_LAST_MODIFIED)) {
						IFileInfo info = fetchInfo(parent, fileInfo);
						if (!info.exists())
							return false;
						time = info.getLastModified();
					}
					if (argument.key.equals(KEY_CREATED)) {
						if (!fSupportsCreatedKey)
							return false;
						time = getFileCreationTime(parent.getLocation().append(fileInfo.getName()).toOSString());
					}
					Date when = new Date(parameter);
					Date then = new Date(time);
					if (argument.operator.equals(OPERATOR_EQUALS))
						return roundToOneDay(time) == roundToOneDay(parameter); 
					if (argument.operator.equals(OPERATOR_BEFORE))
						return then.before(when);
					if (argument.operator.equals(OPERATOR_AFTER))
						return then.after(when);
				}
			}
			if (type.equals(Boolean.class)) {
				boolean parameter = Boolean.valueOf(argument.pattern).booleanValue();
				if (argument.key.equals(KEY_IS_READONLY)) {
					IFileInfo info = fetchInfo(parent, fileInfo);
					if (!info.exists())
						return false;
					return info.getAttribute(EFS.ATTRIBUTE_READ_ONLY) == parameter;
				}
				if (argument.key.equals(KEY_IS_SYMLINK)) {
					IFileInfo info = fetchInfo(parent, fileInfo);
					if (!info.exists())
						return false;
					return info.getAttribute(EFS.ATTRIBUTE_SYMLINK) == parameter;
				}
			}
			return false;
		}

		private long roundToOneDay(long parameter) {
			return parameter / (1000 * 60 * 60 * 24); // 1000 ms in 1 sec, 60 sec in 1 min, 60 min in 1 hour, 24 hours in 1 day 
		}

		private IFileInfo fetchInfo(IContainer parent, IFileInfo fileInfo) {
			IFileStore fileStore;
			try {
				fileStore = EFS.getStore(parent.getLocationURI());
			} catch (CoreException e) {
				return fileInfo;
			}
			IFileStore store = fileStore.getChild(fileInfo.getName());
			return store.fetchInfo();
		}
	}
	/**
	 * Creates a new factory for this filter type.
	 */
	public FileInfoAttributesMatcher() {
		fSupportsCreatedKey = supportCreatedKey();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.AbstractFileInfoMatcher#initialize(org.eclipse.core.resources.IProject, java.lang.Object)
	 */
	public void initialize(IProject project, Object arguments) throws CoreException {
		try {
			if ((arguments instanceof String) && ((String) arguments).length() > 0)
				matcher = new MatcherCache((String) arguments);
		} catch (PatternSyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, Platform.PLUGIN_ERROR, e.getMessage(), e));
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, Platform.PLUGIN_ERROR, e.getMessage(), e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.AbstractFileInfoMatcher#matches(org.eclipse.core.filesystem.IFileInfo)
	 */
	public boolean matches(IContainer parent, IFileInfo fileInfo) throws CoreException {
		if (matcher != null) {
			return matcher.match(parent, fileInfo);
		}
		return false;
	}
}
