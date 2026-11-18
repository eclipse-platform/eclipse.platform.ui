package org.eclipse.jface.text.tests.codemining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMining;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.internal.editors.text.codemining.annotation.AnnotationCodeMiningProvider;

public class AnnotationCodeMiningProviderTest {

	@Test
	public void skipsWhitespaceOnlyFinalLine() throws Exception {
		assertNull(createMining(" \t  ", 0));
	}

	@Test
	public void skipsEmptyDocument() throws Exception {
		assertNull(createMining("", 0));
	}

	@Test
	public void skipsTrailingEmptyLine() throws Exception {
		assertNull(createMining("text\n", 5));
	}

	@Test
	public void retainsMiningOnNonemptyLine() throws Exception {
		AbstractCodeMining mining= createMining("  text", 2);
		assertNotNull(mining);
		try {
			assertEquals(2, mining.getPosition().offset);
			assertEquals(1, mining.getPosition().length);
			assertEquals("diagnostic", mining.getLabel());
		} finally {
			mining.dispose();
		}
	}

	private AbstractCodeMining createMining(String text, int annotationOffset) throws Exception {
		Document document= new Document(text);
		AnnotationModel model= new AnnotationModel();
		model.connect(document);
		AnnotationCodeMiningProvider provider= new AnnotationCodeMiningProvider();
		try {
			Annotation annotation= new Annotation("test", false, "diagnostic");
			model.addAnnotation(annotation, new Position(annotationOffset, 0));
			SourceViewer viewer= mock(SourceViewer.class);
			when(viewer.getDocument()).thenReturn(document);
			when(viewer.getVisualAnnotationModel()).thenReturn(model);
			setField(provider, "viewer", viewer);
			setField(provider, "annotationAccess", mock(IAnnotationAccessExtension.class));
			Method method= AnnotationCodeMiningProvider.class.getDeclaredMethod("createCodeMining", Annotation.class);
			method.setAccessible(true);
			return (AbstractCodeMining) method.invoke(provider, annotation);
		} finally {
			model.disconnect(document);
		}
	}

	private void setField(AnnotationCodeMiningProvider provider, String name, Object value) throws Exception {
		Field field= AnnotationCodeMiningProvider.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(provider, value);
	}
}
