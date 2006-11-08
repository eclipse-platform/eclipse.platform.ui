package org.eclipse.help.internal.dynamic;

import java.util.Iterator;

import org.eclipse.help.Node;

public class NodeWriter {

	private static final char SPECIAL_CHARS[] = { '&', '>', '<', '"', '\'' };
	private static final String ESCAPED_CHARS[] = { "&amp;", "&gt;", "&lt;", "&quot;", "&apos;" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	public void write(Node node, StringBuffer buf, boolean format, String indent, boolean escape) {
		if (node.getNodeName() != null) {
			if (format) {
				buf.append(indent);
			}
			buf.append('<');
			buf.append(node.getNodeName());
			
			Iterator iter = node.getAttributes().iterator();
			while (iter.hasNext()) {
				buf.append(' ');
				String name = (String)iter.next();
				String value = node.getAttribute(name);
				buf.append(name);
				buf.append('=');
				buf.append('"');
				buf.append(escape ? xmlEscape(value) : value);
				buf.append('"');
			}
			
			Node[] children = node.getChildNodes();
			boolean hasChildren = children.length > 0;
			if (hasChildren) {
				buf.append('>');
				if (format) {
					buf.append('\n');
				}
				String childIndent = indent + "   "; //$NON-NLS-1$
				for (int i=0;i<children.length;++i) {
					write(children[i], buf, format, childIndent, escape);
				}
				if (format) {
					buf.append(indent);
				}
				buf.append('<');
				buf.append('/');
				buf.append(node.getNodeName());
				buf.append('>');
			}
			else {
				buf.append('/');
				buf.append('>');
			}
			if (format) {
				buf.append('\n');
			}
		}
		else {
			if (format) {
				buf.append(indent);
			}
			buf.append(escape ? xmlEscape(node.getValue()) : node.getValue());
		}
	}
	
	public static String xmlEscape(String text) {
		StringBuffer buf = new StringBuffer(text.length());
		char[] array = text.toCharArray();
		for (int i=0;i<array.length;++i) {
			boolean escaped = false;
			for (int j=0;j<SPECIAL_CHARS.length;++j) {
				if (array[i] == SPECIAL_CHARS[j]) {
					buf.append(ESCAPED_CHARS[j]);
					escaped = true;
					break;
				}
			}
			if (!escaped) {
				buf.append(array[i]);
			}
		}
		return buf.toString();
	}
}
