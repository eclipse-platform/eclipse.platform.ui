package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.StringMatcher;
import org.eclipse.team.internal.ccvs.core.util.SyncFileUtil;

/**
 * A FileNameMatcher associates a String with a String pattern
 * (e.g. a filename).
 * 
 * XXX How is this classed used, and is it a general .cvsignore
 * mechanism or a generic filename matcher?
 */
public class FileNameMatcher {
	
	private List matchers = new ArrayList();
	private List results = new ArrayList();
	private static final String TRUE = "true";
	private static final String IGNORE_FILE = ".cvsignore";
	
	
	public FileNameMatcher() {
	}
	
	public FileNameMatcher(String[] patterns) {
		register(patterns);
	}
		
	void register(String[] patterns) {
		for (int i = 0; i < patterns.length; i++) {
			register(patterns[i],TRUE);
		}
		register("CVS", TRUE);
	}
	
	public void register(String pattern, String result) {
		
		Assert.isTrue(matchers.size() == results.size());
		
		pattern = pattern.trim();
		
		// The empty pattern matches everything, but we want to match
		// nothing with it, so we just do not register anything
		if (pattern.length() == 0) {
			return;
		}
	
		matchers.add(new StringMatcher(pattern,false,false));
		results.add(result);
		
	}
	
	public String getMatch(String name) {
		StringMatcher stringMatcher;
		
		for (int i = 0; i < matchers.size(); i++) {
			stringMatcher = (StringMatcher) matchers.get(i);
			if (stringMatcher.match(name)) {
				return (String)results.get(i);
			}
		}
		
		return null;
	}
	
	public boolean match(String name) {
		return getMatch(name) != null;
	}
	
	/**
	 * Return a file name matcher build from the .cvsignore file
	 * in the provided directory or null if no such file exists
	 */
	public static FileNameMatcher getIgnoreMatcherFor(File folder) throws CVSException {		
		File cvsignore = new File(folder, IGNORE_FILE);
		if (!cvsignore.exists()) {
			return new FileNameMatcher(new String[0]);			
		} else {
			return new FileNameMatcher(SyncFileUtil.readLines(cvsignore));
		}
	}	
}