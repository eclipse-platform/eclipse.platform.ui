package org.eclipse.ui.internal.texteditor.stickyscroll;

import static org.eclipse.ui.editors.text.EditorsUI.PLUGIN_ID;
import static org.eclipse.ui.internal.texteditor.stickyscroll.StickyLinesProviderRegistry.STICKY_LINES_PROVIDERS_EXTENSION_POINT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;

import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLinesProvider;

import org.eclipse.ui.editors.tests.TestUtil;

public class StickyLinesProviderRegistryTest {

	private StickyLinesProviderDescriptor stickyLinesProviderDescriptor;
	private StickyLinesProviderRegistry cut;
	private ISourceViewer viewer;
	private ITextEditor editor;

	@Before
	public void setup() {
		IConfigurationElement[] configurationElement = { mock(IConfigurationElement.class) };
		stickyLinesProviderDescriptor = mock(StickyLinesProviderDescriptor.class);
		viewer = mock(ISourceViewer.class);
		editor = mock(ITextEditor.class);

		IExtensionRegistry extensionRegistry = mock(IExtensionRegistry.class);
		when(extensionRegistry.getConfigurationElementsFor(PLUGIN_ID, STICKY_LINES_PROVIDERS_EXTENSION_POINT))
				.thenReturn(configurationElement);

		cut = new StickyLinesProviderRegistry(extensionRegistry, e -> stickyLinesProviderDescriptor);
	}

	@After
	public void teardown() {
		TestUtil.cleanUp();
	}

	@Test
	public void testGetDefaultProviderIfNoMatch() {
		when(stickyLinesProviderDescriptor.matches(viewer, editor)).thenReturn(false);

		IStickyLinesProvider provider = cut.getProvider(viewer, editor);

		assertThat(provider, instanceOf(DefaultStickyLinesProvider.class));
	}

	@Test
	public void testGetProviderForMatch() {
		IStickyLinesProvider expProvider = mock(IStickyLinesProvider.class);
		when(stickyLinesProviderDescriptor.matches(viewer, editor)).thenReturn(true);
		when(stickyLinesProviderDescriptor.createStickyLinesProvider()).thenReturn(expProvider);

		IStickyLinesProvider provider = cut.getProvider(viewer, editor);

		assertThat(provider, is(expProvider));
	}

}
