/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;
import java.io.InputStream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.*;
/**
 * ScrolledFormText is a control that is capable of scrolling an instance of
 * the FormText class. It should be created in a parent that will allow it to
 * use all the available area (for example, a shell, a view or an editor). The
 * form text can be created by the class itself, or set from outside. In the
 * later case, the form text instance must be a direct child of the
 * ScrolledFormText instance.
 * <p>
 * The class assumes that text to be rendered contains formatting tags. In case
 * of a string, it will enclose the text in 'form' root element if missing from
 * the text as a convinience. For example:
 * 
 * <pre>
 *  ftext.setText(&quot;&lt;p&gt;Some text here&lt;/&gt;&quot;);
 * </pre>
 * 
 * will not cause an error. The same behavior does not exist for content from
 * the input stream, however - it must be well formed in that case.
 * </p>

 * @since 3.0
 * @see FormText
 */
public class ScrolledFormText extends SharedScrolledComposite {
	private FormText content;
	private String text;
	/**
	 * Creates the new scrolled text instance in the provided parent
	 * 
	 * @param parent
	 *            the parent composite
	 * @param createFormText
	 *            if <code>true</code>, enclosing form text instance will be
	 *            created in this constructor.
	 */
	public ScrolledFormText(Composite parent, boolean createFormText) {
		this(parent, SWT.V_SCROLL | SWT.H_SCROLL, createFormText);
	}
	/**
	 * Creates the new scrolled text instance in the provided parent
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the style to pass to the scrolled composite
	 * @param createFormText
	 *            if <code>true</code>, enclosing form text instance will be
	 *            created in this constructor.
	 */
	public ScrolledFormText(Composite parent, int style, boolean createFormText) {
		super(parent, style);
		if (createFormText)
			setFormText(new FormText(this, SWT.NULL));
	}
	/**
	 * Sets the form text to be managed by this scrolled form text. The
	 * instance must be a direct child of this class. If this method is used,
	 * <code>false</code> must be passed in either of the constructors to
	 * avoid creating form text instance.
	 * 
	 * @param formText
	 *            the form text instance to use.
	 */
	public void setFormText(FormText formText) {
		this.content = formText;
		super.setContent(content);
		content.setMenu(getMenu());
		if (text != null)
			loadText(text);
	}
	/**
	 * Sets the foreground color of the scrolled form text.
	 * 
	 * @param fg
	 *            the foreground color
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (content != null)
			content.setForeground(fg);
	}
	/**
	 * Sets the background color of the scrolled form text.
	 * 
	 * @param bg
	 *            the background color
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (content != null)
			content.setBackground(bg);
	}
	/**
	 * The class sets the content widget. This method should not be called by
	 * classes that instantiate this widget.
	 * 
	 * @param c
	 *            content control
	 */
	public final void setContent(Control c) {
	}
	/**
	 * Sets the text to be rendered in the scrolled form text. The text must
	 * contain formatting tags.
	 * 
	 * @param text
	 *            the text to be rendered
	 */
	public void setText(String text) {
		this.text = text;
		loadText(text);
		reflow(true);
	}
	/**
	 * Sets the contents to rendered in the scrolled form text. The stream must
	 * contain formatting tags. The caller is responsible for closing the input
	 * stream. The call may be long running. For best results, call this method
	 * from another thread and call 'reflow' when done (but make both calls
	 * using 'Display.asyncExec' because these calls must be made in the event
	 * dispatching thread).
	 * 
	 * @param is
	 *            content input stream
	 */
	public void setContents(InputStream is) {
		loadContents(is);
	}
	/**
	 * Returns the instance of the form text.
	 * 
	 * @return the form text instance
	 */
	public FormText getFormText() {
		return content;
	}
	private void loadText(String text) {
		if (content != null) {
			String markup = text;
			if (!markup.startsWith("<form>")) //$NON-NLS-1$
				markup = "<form>" + text + "</form>";  //$NON-NLS-1$//$NON-NLS-2$
			content.setText(markup, true, false);
		}
	}
	private void loadContents(InputStream is) {
		if (content != null) {
			content.setContents(is, false);
		}
	}
}
