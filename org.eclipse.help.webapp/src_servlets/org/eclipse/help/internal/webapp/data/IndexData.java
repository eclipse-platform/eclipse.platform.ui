/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.index.IIndex;
import org.eclipse.help.internal.index.IIndexEntry;
import org.eclipse.help.internal.index.IIndexTopic;

/**
 * Helper class for searchView.jsp initialization
 */
public class IndexData extends ActivitiesData {
  private IIndex index;
  
  //Temporary storage for index generation
  int entryIndex;
  Writer out;
  String indent;
  StringWriter funcOut;
 
 /**
  * Constructs the data for the index page.
  * @param context
  * @param request
  */
 public IndexData(
   ServletContext context,
   HttpServletRequest request,
   HttpServletResponse response) {
   super(context, request, response);
   loadIndex();
 }

 /**
  * Loads help index
  */
 private void loadIndex() {
     this.index = HelpPlugin.getIndexManager().getIndex(Platform.getNL());
 }
 
 public void generateHrefs(Writer out) throws IOException{
	 out.write(funcOut.toString());
 }
 
 public void generateIndex(Writer out, String indent) throws IOException{
	 this.out = out;
	 this.entryIndex = 0;
	 this.indent = indent;
	 this.funcOut = new StringWriter();
	 
	 Iterator iter = index.getEntries().values().iterator();
	 while(iter.hasNext()) {
		 IIndexEntry entry = (IIndexEntry) iter.next();
		 generateIndexEntry(entry, 0, "");
	 }
 }
 
 private void generateIndexEntry( IIndexEntry entry, int depth, String parent) throws IOException{
	 List topics = entry.getTopics();
	 int size = topics.size();
	 String label = UrlUtil.htmlEncode(entry.getKeyword());
	
	 if(size == 1) {
		 IIndexTopic topic = (IIndexTopic) topics.get(0);
		 String href =  UrlUtil.getHelpURL(topic.getHref());
		 funcOut.write("case " + entryIndex + ": openTopic(\"" + href + "\"); break;\n");
	 }
	 else if (size == 0) {
		 funcOut.write("case " + entryIndex + ": alertEmpty(); break;\n");
	 }

	 out.write("<option value='"+ UrlUtil.htmlEncode(parent + label)+"'>");
	 for(int i=0; i<depth; i++) out.write(indent); 
	 out.write(UrlUtil.htmlEncode(label) + "</option>");
	 entryIndex++;
	 
	 Iterator iter = entry.getEntries().values().iterator();
	 if(iter.hasNext()) {
		 do {
			 IIndexEntry childEntry = (IIndexEntry) iter.next();
			 generateIndexEntry(childEntry, depth+1, parent+label+",");
		 } while(iter.hasNext());
	 }
 }
 
 public IIndexEntry getIndexEntry(String [] path) {
	 Map entries = index.getEntries();
	 IIndexEntry result = null;
	 for(int i = 0; i < path.length; i++) {
		 result = (IIndexEntry) entries.get(path[i]);
		 if(result == null)
			 return null;
		 else
			 entries = result.getEntries();
	 }
	 return result;
 }
}
