package org.eclipse.debug.internal.ui;

import java.util.StringTokenizer;

public class VariablesViewModelPresentation extends DelegatingModelPresentation {

	/**
	 * @see DelegatingModelPresentation#getText(Object)
	 * 
	 * Strips out control characters and replaces them with string representations
	 */
	public String getText(Object element) {
		StringBuffer string= new StringBuffer();
		StringTokenizer tokenizer= new StringTokenizer(super.getText(element), "\b\f\n\r\t\\", true);
		String token;
		while (tokenizer.hasMoreTokens()) {
			token= tokenizer.nextToken();
			if (token.length() > 1) {
				string.append(token);
			} else {
				switch (token.charAt(0)) {
					case '\b':
						string.append("\\b");
						break;
					case '\f':
						string.append("\\f");
						break;
					case '\n':
						string.append("\\n");
						break;
					case '\r':
						string.append("\\r");
						break;
					case '\t':
						string.append("\\t");
						break;
					case '\\':
						string.append("\\\\");
						break;
					default:
						string.append(token);
				};
			}
		}
		return string.toString();
	}

}
