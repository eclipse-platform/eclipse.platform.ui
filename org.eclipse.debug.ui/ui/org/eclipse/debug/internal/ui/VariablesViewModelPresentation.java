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
		StringTokenizer tokenizer= new StringTokenizer(super.getText(element), "\b\f\n\r\t\\", true); //$NON-NLS-1$
		String token;
		while (tokenizer.hasMoreTokens()) {
			token= tokenizer.nextToken();
			if (token.length() > 1) {
				string.append(token);
			} else {
				switch (token.charAt(0)) {
					case '\b':
						string.append("\\b"); //$NON-NLS-1$
						break;
					case '\f':
						string.append("\\f"); //$NON-NLS-1$
						break;
					case '\n':
						string.append("\\n"); //$NON-NLS-1$
						break;
					case '\r':
						string.append("\\r"); //$NON-NLS-1$
						break;
					case '\t':
						string.append("\\t"); //$NON-NLS-1$
						break;
					case '\\':
						string.append("\\\\"); //$NON-NLS-1$
						break;
					default:
						string.append(token);
				};
			}
		}
		return string.toString();
	}

}
