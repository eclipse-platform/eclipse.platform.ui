/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;
import java.io.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.util.XMLGenerator;

/**
 * This generates the XML file for the help navigation.
 */
public class TocWriter extends XMLGenerator {
	/**
	 * @param toc Toc
	 * @param writer java.io.Writer
	 */
	public TocWriter(Writer writer) {
		super(writer);
	}
	/**
	 * @param toc Toc
	 * @param outputFile java.io.File
	 */
	public TocWriter(File outputFile) {
		super(outputFile);
	}
	/**
	 * Writes out xml data for a toc
	 */
	public void generate(IToc toc, boolean genTopics)
	{
		// get the topic description
		String topicDescription = "";
		ITopic topic = toc.getTopic(null);
		if (topic != null)
			topicDescription = topic.getHref();
			
		println(
			"<toc label=\""
				+ xmlEscape(toc.getLabel())
				+ "\" href=\""
				+ reduceURL(toc.getHref())
				+ "\" topic=\""
				+ reduceURL(topicDescription)
				+ "\">");
		if (genTopics) {
			ITopic[] topics = toc.getTopics();
			for (int i = 0; i < topics.length; i++) {
				generate(topics[i]);
			}
		}
		println("</toc>");
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
			for (int i = 0; i < subtopics.length; i++) {
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