package org.eclipse.jface.tests.labelProviders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.tests.viewers.StructuredViewerTest.TestLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

public class ColumnLabelProviderLambdaTest {

	@Test
	public void testCreateTextProvider() {
		Shell shell = LabelProviderLambdaTest.initializeShell();
		TableViewer viewer = (TableViewer) LabelProviderLambdaTest.initializeViewer(shell);
		TableViewerColumn columnViewer = new TableViewerColumn(viewer, SWT.NONE, 0);
		columnViewer.setLabelProvider(ColumnLabelProvider.createTextProvider(Object::toString));
		shell.open();
		Integer[] model = (Integer[]) columnViewer.getViewer().getInput();
		ColumnLabelProvider provider = (ColumnLabelProvider) columnViewer.getViewer().getLabelProvider(0);
		String firstElementText = provider.getText(model[0]);
		assertEquals(Integer.valueOf(0).toString(), firstElementText, "same label text");
	}

	@Test
	public void testCreateImageProvider() {
		Shell shell = LabelProviderLambdaTest.initializeShell();
		TableViewer viewer = (TableViewer) LabelProviderLambdaTest.initializeViewer(shell);
		Image fgImage = ImageDescriptor.createFromFile(TestLabelProvider.class, "images/java.gif").createImage();
		TableViewerColumn columnViewer = new TableViewerColumn(viewer, SWT.NONE, 0);
		columnViewer.setLabelProvider(ColumnLabelProvider.createImageProvider(inputElement -> fgImage));
		shell.open();
		Integer[] model = (Integer[]) columnViewer.getViewer().getInput();
		ColumnLabelProvider provider = (ColumnLabelProvider) columnViewer.getViewer().getLabelProvider(0);
		assertEquals(fgImage, provider.getImage(model[0]), "same image");
	}

	@Test
	public void testCreateTextImageProvider() {
		Shell shell = LabelProviderLambdaTest.initializeShell();
		TableViewer viewer = (TableViewer) LabelProviderLambdaTest.initializeViewer(shell);
		Image fgImage = ImageDescriptor.createFromFile(TestLabelProvider.class, "images/java.gif").createImage();
		TableViewerColumn columnViewer = new TableViewerColumn(viewer, SWT.NONE, 0);
		columnViewer.setLabelProvider(
				ColumnLabelProvider.createTextImageProvider(Object::toString, inputElement -> fgImage));
		shell.open();
		Integer[] model = (Integer[]) columnViewer.getViewer().getInput();
		ColumnLabelProvider provider = (ColumnLabelProvider) columnViewer.getViewer().getLabelProvider(0);
		String firstElementText = provider.getText(model[0]);
		assertEquals(Integer.valueOf(0).toString(), firstElementText, "same label text");
		assertEquals(fgImage, provider.getImage(model[0]), "same image");
	}

}
