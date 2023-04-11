package org.eclipse.ui.tests.commands;

import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class E4CommandImageTest {

	@Test
	public void testE4CommandImage() {

		ICommandImageService commandImageService = PlatformUI.getWorkbench().getService(ICommandImageService.class);

		ImageDescriptor imageDescriptor = commandImageService
				.getImageDescriptor("org.eclipse.ui.tests.command.iconTest");

		assertNotNull(imageDescriptor);
	}
}
