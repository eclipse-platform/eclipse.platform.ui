package org.eclipse.ant.core;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import java.io.File;
import java.util.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;

/**
 * Scanner for Eclipse workspaces
 *
 * Based on the DirectoryScanner in Ant 1.3 by 
 * Arnout J. Kuiper <a href="mailto:ajkuiper@wxs.nl">ajkuiper@wxs.nl</a>
 */
public class WorkspaceScanner extends DirectoryScanner {

	/**
	 * Patterns that should be excluded by default.
	 *
	 * @see #addDefaultExcludes()
	 */
	private final static String[] DEFAULTEXCLUDES= {};
	/**
	 * Convert the list of results to have the correct Eclipse platform path separators.
	 * Do nothing if the current File.separatorChar is the same as the platform
	 * char (i.e., '/').
	 *
	 * @return the names of the files
	 */
	protected String[] convertResults(String[] results) {
		if (File.separatorChar == '/')
			return results;
		for (int i= 0; i < results.length; i++)
			results[i]= results[i].replace('\\', File.separatorChar);
		return results;
	}
	/**
	 * Get the names of the directories that matched at least one of the include
	 * patterns, an matched also at least one of the exclude patterns.
	 * The names are relative to the basedir.
	 *
	 * @return the names of the directories
	 */
	public String[] getExcludedDirectories() {
		return convertResults(super.getExcludedDirectories());
	}
	/**
	 * Get the names of the files that matched at least one of the include
	 * patterns, an matched also at least one of the exclude patterns.
	 * The names are relative to the basedir.
	 *
	 * @return the names of the files
	 */
	public String[] getExcludedFiles() {
		return convertResults(super.getExcludedFiles());
	}
	/**
	 * Get the names of the directories that matched at least one of the include
	 * patterns, an matched none of the exclude patterns.
	 * The names are relative to the basedir.
	 *
	 * @return the names of the directories
	 */
	public String[] getIncludedDirectories() {
		return convertResults(super.getIncludedDirectories());
	}
	/**
	 * Get the names of the files that matched at least one of the include
	 * patterns, an matched none of the exclude patterns.
	 * The names are relative to the basedir.
	 *
	 * @return the names of the files
	 */
	public String[] getIncludedFiles() {
		return convertResults(super.getIncludedFiles());
	}
	/**
	 * Get the names of the directories that matched at none of the include
	 * patterns.
	 * The names are relative to the basedir.
	 *
	 * @return the names of the directories
	 */
	public String[] getNotIncludedDirectories() {
		return convertResults(super.getNotIncludedDirectories());
	}
	/**
	 * Get the names of the files that matched at none of the include patterns.
	 * The names are relative to the basedir.
	 *
	 * @return the names of the files
	 */
	public String[] getNotIncludedFiles() {
		return convertResults(super.getNotIncludedFiles());
	}
	/**
	 * Matches a string against a pattern. The pattern contains two special
	 * characters:
	 * '*' which means zero or more characters,
	 * '?' which means one and only one character.
	 *
	 * @param pattern the (non-null) pattern to match against
	 * @param str     the (non-null) string that must be matched against the
	 *                pattern
	 *
	 * @return <code>true</code> when the string matches against the pattern,
	 *         <code>false</code> otherwise.
	 */
	protected static boolean match(String pattern, String str) {
		return DirectoryScanner.match(pattern, str);
	}
	/**
	 * Scans the base directory for files that match at least one include
	 * pattern, and don't match any exclude patterns.
	 *
	 * @exception IllegalStateException when basedir was set incorrecly
	 */
	public void scan() {
		if (basedir == null) {
			throw new IllegalStateException("No basedir set");
		}
		IResource base= ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(basedir.toString()));
		if (base != null) {
			throw new IllegalStateException("basedir does not exist");
		}
		if (!(base instanceof IContainer)) {
			throw new IllegalStateException("basedir is not a container");
		}

		if (includes == null) {
			// No includes supplied, so set it to 'matches all'
			includes= new String[1];
			includes[0]= "**";
		}
		if (excludes == null) {
			excludes= new String[0];
		}

		filesIncluded= new Vector();
		filesNotIncluded= new Vector();
		filesExcluded= new Vector();
		dirsIncluded= new Vector();
		dirsNotIncluded= new Vector();
		dirsExcluded= new Vector();

		scandir((IContainer) base, "", true);
	}
	/**
	 * Scans the passed dir for files and directories. Found files and
	 * directories are placed in their respective collections, based on the
	 * matching of includes and excludes. When a directory is found, it is
	 * scanned recursively.
	 *
	 * @param dir   the directory to scan
	 * @param vpath the path relative to the basedir (needed to prevent
	 *              problems with an absolute path when using dir)
	 *
	 * @see #filesIncluded
	 * @see #filesNotIncluded
	 * @see #filesExcluded
	 * @see #dirsIncluded
	 * @see #dirsNotIncluded
	 * @see #dirsExcluded
	 */
	protected void scandir(IContainer base, String vpath, boolean fast) {
		IResource[] members= null;
		try {
			members= base.members();
		} catch (CoreException e) {
			throw new BuildException("Base does not exist: " + base.getFullPath(), e);
		}
		for (int i= 0; i < members.length; i++) {
			IResource resource= members[i];
			String name= vpath + resource.getName();
			if (resource instanceof IContainer) {
				if (isIncluded(name)) {
					if (!isExcluded(name)) {
						dirsIncluded.addElement(name);
						if (fast) {
							scandir((IContainer) resource, name + File.separator, fast);
						}
					} else {
						dirsExcluded.addElement(name);
					}
				} else {
					dirsNotIncluded.addElement(name);
					if (fast && couldHoldIncluded(name)) {
						scandir((IContainer) resource, name + File.separator, fast);
					}
				}
				if (!fast) {
					scandir((IContainer) resource, name + File.separator, fast);
				}
			} else if (isIncluded(name)) {
				if (!isExcluded(name)) {
					filesIncluded.addElement(name);
				} else {
					filesExcluded.addElement(name);
				}
			} else {
				filesNotIncluded.addElement(name);
			}
		}
	}
}
