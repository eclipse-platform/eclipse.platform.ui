package org.eclipse.ant.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Reference;

import java.io.*;
import java.util.Enumeration;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Named collection of include/exclude tags.  This is largely a copy of the original
 * Ant PatternSet data type.  Unfortunately, that datatype used both the comma and
 * the space character as a token to delimit pattern entries.  This makes it virtually
 * impossible to use for files which have spaces in their names.  The <code>CommaPatternSet</code>
 * is not particularly usable but it is easier than trying to deal with the original.
 * <p>
 * A complete copy was done to ensure that the behavior is correct.  Otherwise there is a
 * risk of getting some values from the private slots of the superclass and some from this
 * class.  Unfortunately, there is no way to link this support into <code>FileSet</code>.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 * @see PatternSet
 */
public class CommaPatternSet extends PatternSet {
	private Vector includeList = new Vector();
	private Vector excludeList = new Vector();
	
	private File incl = null;
	private File excl = null;

	public CommaPatternSet() {
		super();
	}
	/**
	 * add a name entry to the given list
	 */
	private NameEntry addPatternToList(Vector list) {
		NameEntry result = new NameEntry();
		list.addElement(result);
		return result;
	}
	/**
	 * Adds the patterns of the other instance to this set.
	 */
	public void append(PatternSet other, Project p) {
		if (isReference()) {
			throw new BuildException(Policy.bind("exception.cannotAppendToReference"));
		}

		String[] incl = other.getIncludePatterns(p);
		if (incl != null) {
			for (int i=0; i<incl.length; i++) {
				createInclude().setName(incl[i]);
			}
		}
		
		String[] excl = other.getExcludePatterns(p);
		if (excl != null) {
			for (int i=0; i<excl.length; i++) {
				createExclude().setName(excl[i]);
			}
		}
	}
	/**
	 * add a name entry on the exclude list
	 */
	public NameEntry createExclude() {
		if (isReference()) {
			throw noChildrenAllowed();
		}
		return addPatternToList(excludeList);
	}
	/**
	 * add a name entry on the include list
	 */
	public NameEntry createInclude() {
		if (isReference()) {
			throw noChildrenAllowed();
		}
		return addPatternToList(includeList);
	}
	/**
	 * Returns the filtered include patterns.
	 */
	public String[] getExcludePatterns(Project p) {
		if (isReference()) {
			return getRef(p).getExcludePatterns(p);
		} else {
			readFiles(p);
			return makeArray(excludeList, p);
		}
	}
	/**
	 * Returns the filtered include patterns.
	 */
	public String[] getIncludePatterns(Project p) {
		if (isReference()) {
			return getRef(p).getIncludePatterns(p);
		} else {
			readFiles(p);
			return makeArray(includeList, p);
		}
	}
	/**
	 * Performs the check for circular references and returns the
	 * referenced PatternSet.  
	 */
	private PatternSet getRef(Project p) {
		if (!checked) {
			Stack stk = new Stack();
			stk.push(this);
			dieOnCircularReference(stk, p);
		}
		
		Object o = ref.getReferencedObject(p);
		if (!(o instanceof PatternSet))
			throw new BuildException(Policy.bind("exception.notAPatternSet",ref.getRefId()));
		else
			return (PatternSet) o;
	}
	/**
	 * helper for FileSet.
	 */
	boolean hasPatterns() {
		return incl != null || excl != null
			|| includeList.size() > 0 || excludeList.size() > 0;
	}
	/**
	 * Convert a vector of NameEntry elements into an array of Strings.
	 */
	private String[] makeArray(Vector list, Project p) {
		if (list.size() == 0) return null;

		Vector tmpNames = new Vector();
		for (Enumeration e = list.elements() ; e.hasMoreElements() ;) {
			NameEntry ne = (NameEntry)e.nextElement();
			String pattern = ne.evalName(p);
			if (pattern != null && pattern.length() > 0) {
				tmpNames.addElement(pattern);
			}
		}

		String result[] = new String[tmpNames.size()];
		tmpNames.copyInto(result);
		return result;
	}
	/**
	 * Read includefile ot excludefile if not already done so.
	 */
	private void readFiles(Project p) {
		if (incl != null) {
			readPatterns(incl, includeList, p);
			incl = null;
		}
		if (excl != null) {
			readPatterns(excl, excludeList, p);
			excl = null;
		}
	}
	/**
	 *  Reads path matching patterns from a file and adds them to the
	 *  includes or excludes list (as appropriate).  
	 */
	private void readPatterns(File patternfile, Vector patternlist, Project p)
		throws BuildException {
		
		try {
			// Get a FileReader
			BufferedReader patternReader = 
				new BufferedReader(new FileReader(patternfile)); 
		
			// Create one NameEntry in the appropriate pattern list for each 
			// line in the file.
			String line = patternReader.readLine();
			while (line != null) {
				if (line.length() > 0) {
					line = ProjectHelper.replaceProperties(p, line,
														   p.getProperties());
					addPatternToList(patternlist).setName(line);
				}
				line = patternReader.readLine();
			}
		} catch(IOException ioe)  {
			throw new BuildException(Policy.bind("exception.patternFile",patternfile.toString()),ioe);
		}
	}
	/**
	 * Sets the set of exclude patterns. Patterns may be separated by a comma
	 * or a space.
	 *
	 * @param excludes the string containing the exclude patterns
	 */
	public void setExcludes(String excludes) {
		if (isReference()) {
			throw tooManyAttributes();
		}
		if (excludes != null && excludes.length() > 0) {
			StringTokenizer tok = new StringTokenizer(excludes, ",", false);
			while (tok.hasMoreTokens()) {
				createExclude().setName(tok.nextToken());
			}
		}
	}
	/**
	 * Sets the name of the file containing the excludes patterns.
	 *
	 * @param excl The file to fetch the exclude patterns from.  
	 */
	 public void setExcludesfile(File excl) throws BuildException {
		 if (isReference())
			 throw tooManyAttributes();
		 if (!excl.exists())
			 throw new BuildException(Policy.bind("exception.missingExcludesFile",excl.getAbsolutePath()));
		 this.excl = excl;
	 }
public void setId(String value) {
}
	/**
	 * Sets the set of include patterns. Patterns may be separated by a comma
	 * or a space.
	 *
	 * @param includes the string containing the include patterns
	 */
	public void setIncludes(String includes) {
		if (isReference()) {
			throw tooManyAttributes();
		}
		if (includes != null && includes.length() > 0) {
			StringTokenizer tok = new StringTokenizer(includes, ",", false);
			while (tok.hasMoreTokens()) {
				createInclude().setName(tok.nextToken());
			}
		}
	}
	/**
	 * Sets the name of the file containing the includes patterns.
	 *
	 * @param incl The file to fetch the include patterns from.  
	 */
	 public void setIncludesfile(File incl) throws BuildException {
		 if (isReference())
			 throw tooManyAttributes();
		 if (!incl.exists())
			 throw new BuildException(Policy.bind("exception.missingIncludesFile",incl.getAbsolutePath()));
		 this.incl = incl;
	 }
	/**
	 * Makes this instance in effect a reference to another PatternSet
	 * instance.
	 *
	 * <p>You must not set another attribute or nest elements inside
	 * this element if you make it a reference.</p> 
	 */
	public void setRefid(Reference r) throws BuildException {
		if (!includeList.isEmpty() || !excludeList.isEmpty()) {
			throw tooManyAttributes();
		}
		super.setRefid(r);
	}
}
