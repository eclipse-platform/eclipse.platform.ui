package org.eclipse.help.internal.navigation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.util.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributions.*;

/**
 * Visitor class to walk the help structure and generate navigation xml
 */
public abstract class XMLGenerator extends HelpVisitor {

	protected File outputDir = null;
	protected PrintWriter out = null;
	protected char viewChar = 'a';
	private SequenceGenerator idGenerator = SequenceGenerator.getNewGenerator();
	protected InfoSet infoSet = null;

	/**
	 * HTMLGenerator constructor comment.
	 */
	public XMLGenerator(InfoSet infoSet, File outputDir) {
		super();

		this.infoSet = infoSet;
		this.outputDir = outputDir;
	}
	/**
	 */
	public void generate() {
		// The html generator is a visitor that needs to start from the view set
		// and will descend to children, etc....
		infoSet.accept(this);
	}
	/**
	 * Simplifies url path by removing "/.." with the parent directory from the path
	 * @return java.lang.String
	 * @param url java.lang.String
	 */
	protected static String reduceURL(String url) {
		if (url == null)
			return url;
		while (true) {
			int index = url.lastIndexOf("/../");
			if (index <= 0)
				break; //there is no "/../" or nothing before "/../" to simplify
			String part1 = url.substring(0, index);
			String part2 = url.substring(index + "/..".length());
			index = part1.lastIndexOf("/");
			if (index >= 0)
				url = part1.substring(0, index) + part2;
			else
				url = part2;
		}
		return url;
	}
}
