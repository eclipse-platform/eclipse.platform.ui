package org.eclipse.jface.tests.labelProviders;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.tests.viewers.StructuredViewerTest.TestLabelProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import junit.framework.TestCase;

public class LabelProviderLambdaTest extends TestCase {

	protected static Shell initializeShell() {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		return shell;
	}

	protected static StructuredViewer initializeViewer(Shell shell) {
		final TableViewer viewer = new TableViewer(shell);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(createModel());
		return viewer;
	}

	@SuppressWarnings("boxing")
	protected static Integer[] createModel() {
		Integer[] model = new Integer[10];
		for (int i = 0; i < 10; i++) {
			model[i] = i;
		}
		return model;
	}

	public void testCreateTextProvider() {
		Shell shell = initializeShell();
		StructuredViewer viewer = initializeViewer(shell);
		viewer.setLabelProvider(LabelProvider.createTextProvider(Object::toString));
		shell.open();
		Table table = (Table) viewer.getControl();
		String firstElementText = table.getItem(0).getText();
		assertEquals("rendered label", Integer.valueOf(0).toString(), firstElementText);
	}

	public void testCreateTextImageProvider() {
		Shell shell = initializeShell();
		StructuredViewer viewer = initializeViewer(shell);
		Image fgImage = ImageDescriptor.createFromFile(TestLabelProvider.class, "images/java.gif").createImage();
		viewer.setLabelProvider(LabelProvider.createTextImageProvider(Object::toString,
				inputElement -> fgImage));
		shell.open();
		Table table = (Table) viewer.getControl();
		String firstElementText = table.getItem(0).getText();
		LabelProvider provider = (LabelProvider) viewer.getLabelProvider();
		assertEquals("same label text", Integer.valueOf(0).toString(), firstElementText);
		assertEquals("same image", fgImage, provider.getImage(table.getItem(0)));

	}

	public void testCreateImageProvider() {
		Shell shell = initializeShell();
		StructuredViewer viewer = initializeViewer(shell);
		Image fgImage = ImageDescriptor.createFromFile(TestLabelProvider.class, "images/java.gif").createImage();
		viewer.setLabelProvider(LabelProvider.createImageProvider(inputElement -> fgImage));
		shell.open();
		Table table = (Table) viewer.getControl();
		LabelProvider provider = (LabelProvider) viewer.getLabelProvider();
		String firstElementText = table.getItem(0).getText();
		assertEquals("same label text", Integer.valueOf(0).toString(), firstElementText);
		assertEquals("same image", fgImage, provider.getImage(table.getItem(0)));
	}

}
