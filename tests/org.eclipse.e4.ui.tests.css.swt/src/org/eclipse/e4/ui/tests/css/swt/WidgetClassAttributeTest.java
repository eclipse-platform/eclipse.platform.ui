package org.eclipse.e4.ui.tests.css.swt;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.e4.ui.css.core.SACConstants;
import org.eclipse.e4.ui.css.core.dom.parsers.CSSParser;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.sac.ISACParserFactory;
import org.eclipse.e4.ui.css.core.sac.SACParserFactory;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.w3c.css.sac.Parser;

import junit.framework.TestCase;

public class WidgetClassAttributeTest extends TestCase {

	private static final Color COLOR_RED = Display.getCurrent().getSystemColor(
			SWT.COLOR_RED);
	
	protected String styleSheet = "Label {color:red}";

	protected Label createTestLabel() {

		Display display = Display.getCurrent();

		CSSEngine engine = new CSSSWTEngineImpl(display);
		
		try {
			engine.parseStyleSheet(new StringReader(styleSheet));
		} catch (IOException e) {
			fail(e.getMessage());
		}

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite panel1 = new Composite(shell, SWT.NONE);
		panel1.setLayout(new FillLayout());

		Label label = new Label(panel1, SWT.NONE);
		label.setText("Label");

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		shell.open();
		return label;
	}
	
	public void testLabelColor() throws Exception {
		Label label = createTestLabel();
		assertEquals(label.getBackground().getRGB(), COLOR_RED);
	}
}
