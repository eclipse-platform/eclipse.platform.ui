package org.eclipse.help.internal.xhtml;

import org.eclipse.help.internal.dynamic.DOMProcessorHandler;
import org.w3c.dom.Element;

/*
 * Converts the charset in XHTML meta tag to UTF-8. This is the encoding
 * output by the XMLProcessor, and we need the charset in the meta tags
 * to match, otherwise browsers will be confused.
 */
public class XHTMLCharsetHandler extends DOMProcessorHandler {

	private static final String ELEMENT_META = "meta"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CONTENT = "content"; //$NON-NLS-1$
	private static final String PREFIX_CHARSET = "text/html; charset="; //$NON-NLS-1$
	private static final String ENCODING_UTF8 = "UTF-8"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.dynamic.DOMProcessorHandler#handle(org.w3c.dom.Element, java.lang.String)
	 */
	public short handle(Element elem, String id) {
		if (ELEMENT_META.equals(elem.getNodeName())) {
			String content = elem.getAttribute(ATTRIBUTE_CONTENT);
			if (content.startsWith(PREFIX_CHARSET)) {
				elem.setAttribute(ATTRIBUTE_CONTENT, PREFIX_CHARSET + ENCODING_UTF8);
				return HANDLED_CONTINUE;
			}
		}
		return UNHANDLED;
	}
}
