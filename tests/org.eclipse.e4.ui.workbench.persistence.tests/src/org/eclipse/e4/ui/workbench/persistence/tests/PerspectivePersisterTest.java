package org.eclipse.e4.ui.workbench.persistence.tests;

import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState;
import org.eclipse.e4.ui.workbench.persistence.PerspectivePersister;
import org.eclipse.e4.ui.workbench.persistence.common.CommonUtil;
import org.eclipse.e4.ui.workbench.persistence.tests.util.TestPerspective;
import org.eclipse.e4.ui.workbench.persistence.tests.util.TestWorkbenchAdvisor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
class PerspectivePersisterTest {
	
	private Display display = null;
	
	@BeforeEach
	void setUp() throws Exception {
		display = PlatformUI.createDisplay();
		
	}

	@AfterEach
	void tearDown() throws Exception {
		display.dispose();
	}

	@Test
	void testSave() throws InterruptedException, ExecutionException {
		CompletableFuture<String> future = new CompletableFuture<>();
		TestWorkbenchAdvisor testWorkbenchAdvisor = new TestWorkbenchAdvisor() {

			@Override
			public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
				super.createWorkbenchWindowAdvisor(configurer);
				String serializedPerspective = PerspectivePersister.serializePerspectiveAndPartStates(TestPerspective.ID);
				future.complete(serializedPerspective);
				PlatformUI.getWorkbench().close();			
			}
			
		};
		PlatformUI.createAndRunWorkbench(display, testWorkbenchAdvisor);
		String serializedPerspective = future.get();
		assertTrue(serializedPerspective.contains("<perspective elementId=\""+TestPerspective.ID+"\""));
		assertTrue(serializedPerspective.contains("MyText=&quot;INITIAL TEXT!&quot;"));
		
	}
	
	@Test
	void testRestore() throws InterruptedException, ExecutionException  {
		final ResourceSet resourceSet = new ResourceSetImpl();
		final Resource resource = resourceSet.getResource(URI.createURI(Paths.get("resources/workbench_restore.persistence").toUri().toString()), true); //$NON-NLS-1$
		IWorkbenchState state = (IWorkbenchState) resource.getContents().get(0);

		CompletableFuture<String> future = new CompletableFuture<>();
		TestWorkbenchAdvisor testWorkbenchAdvisor = new TestWorkbenchAdvisor() {

			@Override
			public void postWindowOpen(IWorkbenchWindowConfigurer configurer) {
				super.createWorkbenchWindowAdvisor(configurer);
				CommonUtil.getCurrentMainWindow().getContext().activate();
				PerspectivePersister.restoreWorkbenchState(state);
				String serializedPerspective = PerspectivePersister.serializePerspectiveAndPartStates(TestPerspective.ID);
				future.complete(serializedPerspective);
				PlatformUI.getWorkbench().close();
			}
			
		};
		PlatformUI.createAndRunWorkbench(display, testWorkbenchAdvisor);
		String serializedPerspective = future.get();
		assertTrue(serializedPerspective.contains("<perspective elementId=\""+TestPerspective.ID+"\""));
		assertTrue(serializedPerspective.contains("MyText=&quot;RESTORED TEXT!&quot;"));
		
	}

}
