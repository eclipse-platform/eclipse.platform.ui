/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;
import java.io.File;
import java.util.*;
import org.eclipse.help.internal.util.XMLGenerator;
import org.eclipse.help.topics.*;
/**
 * This generates the XML file for the help navigation.
 */
public class NavigationWriter extends XMLGenerator {
	protected ITopics topics;
	/**
	 * @param viewSet com.ibm.itp.ua.view.ViewSet
	 * @param outputDir java.io.File
	 */
	public NavigationWriter(ITopics topics, File outputFile) {
		super(outputFile);
		this.topics = topics;
	}
	/**
	 * Writes out navigation for the Topics to a file
	 */
	public void generate() {
		println(
			"<topics label=\""
				+ xmlEscape(topics.getLabel())
				+ "\" href=\""
				+ reduceURL(topics.getHref())
				+ "\">");
		ITopic[] subtopics = topics.getTopics();
		for (int i=0; i<subtopics.length; i++) {
			generate(subtopics[i]);
		}
		println("</topics>");
		super.close();
	}
	/**
	 * Generates part of navigation for a given Topic
	 * and it children Topic
	 */
	protected void generate(ITopic topic) {
		pad++;
		printPad();
		String href = topic.getHref();
		print(
			"<topic label=\""
				+ xmlEscape(topic.getLabel())
				+ "\""
				+ (href != null ? " href=\"" + reduceURL(href) + "\"" : ""));
		ITopic[] subtopics = topic.getSubtopics();
		if (subtopics.length > 0) {
			println(">");
			for (int i=0 ;i<subtopics.length; i++) {
				generate(subtopics[i]);
			}
			printPad();
			println("</topic>");
		} else {
			println(" />");
		}
		pad--;
	}
	/**
	 * Simplifies url path by removing "string/.." from the path
	 * @return reduced url String
	 * @param url String
	 */
	protected static String reduceURL(String url) {
		if (url == null)
			return url;
		while (true) {
			int index = url.indexOf("/..", 1);
			if (index <= 0)
				break; //there is no "/.." or nothing before "/.." to simplify
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