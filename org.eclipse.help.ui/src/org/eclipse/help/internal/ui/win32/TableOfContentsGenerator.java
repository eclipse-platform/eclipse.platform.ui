package org.eclipse.help.internal.ui.win32;

import java.io.*;
import org.eclipse.help.topics.*;
public class TableOfContentsGenerator {
	File file;
	ITopic rootTopic;
	public TableOfContentsGenerator(String filename, ITopic rootTopic){
		this.rootTopic = rootTopic;
		this.file = new File(filename);
	}
	public void generate(){
//		PrintWriter writer=new PrintWriter(
//					new BufferedWriter(
//						new OutputStreamWriter(
//							new FileOutputStream(outFile),
//							"UTF8")),false /* no aotoFlush */);
//
//		tableOfContents.append("<html>");
//		generateHeader(tableOfContents);
//
//		tableOfContents.append("<body>");
//		tableOfContents.append("<h1 ALIGN=CENTER>");
//		tableOfContents.append(Resources.getString("Table_Of_Contents"));
//		tableOfContents.append("</h1>");
//		tableOfContents.append("<h3>");
//
//			tableOfContents.append("<ol>");
//			for (int i = 0; i < topicList.length; i++) {
//				tableOfContents.append("<li>");
//				tableOfContents.append(topicList[i].getLabel());
//				tableOfContents.append("</li>");
//			}
//			tableOfContents.append("<ol>");
//			tableOfContents.append("</h3>");
//
//			tableOfContents.append("</body></html>");
//			byte[] bytes = tableOfContents.toString().getBytes("UTF-8");
//
//			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
//			return inputStream;
//
	}
		
}

