package org.eclipse.help.internal.search;

import java.io.IOException;
import java.net.URL;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.search.ISearchIndex;
import org.eclipse.help.search.LuceneSearchParticipant;


public class HTMLSearchParticipant extends LuceneSearchParticipant {

	private HTMLDocParser parser;
	private String indexPath;

	public HTMLSearchParticipant(String indexPath) {
		parser = new HTMLDocParser();
		this.indexPath = indexPath;
	}

	public IStatus addDocument(ISearchIndex index, String pluginId, String name, URL url, String id,
			Document doc) {
		try {
			try {
				try {
					parser.openDocument(url);
				} catch (IOException ioe) {
					return new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, IStatus.ERROR,
							"Help document " //$NON-NLS-1$
									+ name + " cannot be opened.", //$NON-NLS-1$
							null);
				}
				ParsedDocument parsed = new ParsedDocument(parser.getContentReader());
				doc.add(Field.Text("contents", parsed.newContentReader())); //$NON-NLS-1$
				doc.add(Field.Text("exact_contents", parsed //$NON-NLS-1$
						.newContentReader()));
				String title = parser.getTitle();
				doc.add(Field.UnStored("title", title)); //$NON-NLS-1$
				doc.add(Field.UnStored("exact_title", title)); //$NON-NLS-1$
				doc.add(Field.UnIndexed("raw_title", title)); //$NON-NLS-1$
				doc.add(Field.UnIndexed("summary", parser.getSummary())); //$NON-NLS-1$
			} finally {
				parser.closeDocument();
			}
		} catch (IOException e) {
			return new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, IStatus.ERROR,
					"IO exception occurred while adding document " + name //$NON-NLS-1$
							+ " to index " + indexPath + ".", //$NON-NLS-1$ //$NON-NLS-2$
					e);
		}
		return Status.OK_STATUS;
	}

}
