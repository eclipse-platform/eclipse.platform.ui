/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecorationResult;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.decorators.LightweightDecoratorManager;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.menus.ObjectContributionClasses;
import org.eclipse.ui.tests.menus.ObjectContributionClasses.ICommon;

public class DecoratorAdaptableTests extends UITestCase {

	public static TestSuite suite() {
		TestSuite ts = new TestSuite();
		ts.addTest(new DecoratorAdaptableTests("testAdaptables"));
		ts.addTest(new DecoratorAdaptableTests("testNonAdaptableContributions"));
		ts.addTest(new DecoratorAdaptableTests("testContributorResourceAdapter"));
		return ts;
	}
	
    public DecoratorAdaptableTests(String testName) {
        super(testName);
    }

    private DecoratorManager getDecoratorManager() {
        return WorkbenchPlugin.getDefault().getDecoratorManager();
    }
    
    private String getDecorationTextFor(Object object) {
        DecoratorManager dm = getDecoratorManager();
        LightweightDecoratorManager ldm = dm.getLightweightManager();
        DecorationResult result = ldm.getDecorationResult(object);
        return result.decorateWithText("Default label");
    }
    
    private void assertDecorated(String testSubName, String[] expectedSuffixes, Object[] elements, Class adaptedClass, boolean shouldHaveMatches) throws CoreException {
        for (int i = 0; i < elements.length; i++) {
            Object object = elements[i];
            String text = getDecorationTextFor(object);
            boolean allMatchesFound = true;
            for (int j = 0; j < expectedSuffixes.length; j++) {
                String suffix = expectedSuffixes[j];
                if (text.indexOf(suffix) == -1) {
                    allMatchesFound = false;
                }
            }
            assertTrue("Adaptable test " + testSubName + " has failed for object " + object.toString(), allMatchesFound == shouldHaveMatches);
        }
        
    }

    protected void doSetUp() throws Exception {
        PlatformUI.getWorkbench().getDecoratorManager().setEnabled(TestAdaptableDecoratorContributor.ID, true);
        PlatformUI.getWorkbench().getDecoratorManager().setEnabled(TestUnadaptableDecoratorContributor.ID, true);
        PlatformUI.getWorkbench().getDecoratorManager().setEnabled(TestResourceDecoratorContributor.ID, true);
        PlatformUI.getWorkbench().getDecoratorManager().setEnabled(TestResourceMappingDecoratorContributor.ID, true);
        super.doSetUp();
    }
    
    protected void doTearDown() throws Exception {
        PlatformUI.getWorkbench().getDecoratorManager().setEnabled(TestAdaptableDecoratorContributor.ID, false);
        PlatformUI.getWorkbench().getDecoratorManager().setEnabled(TestUnadaptableDecoratorContributor.ID, false);
        PlatformUI.getWorkbench().getDecoratorManager().setEnabled(TestResourceDecoratorContributor.ID, false);
        PlatformUI.getWorkbench().getDecoratorManager().setEnabled(TestResourceMappingDecoratorContributor.ID, false);
        super.doTearDown();
    }
    
    /**
     * This tests adaptable contributions that are not IResource.
     * 
     * @since 3.1
     */
    public final void testAdaptables() throws CoreException {
        // Assert that decorators contributed to ICommon are applied to the given object
        assertDecorated("1", 
                new String[] {TestAdaptableDecoratorContributor.SUFFIX}, 
                new Object[] {
                        new ObjectContributionClasses.Common(), 
                        new ObjectContributionClasses.C(), 
                        new ObjectContributionClasses.B(),
                        new ObjectContributionClasses.A()
                },
                ICommon.class,
                true
            );
        // Assert that decorators contributed to ICommon are not applied to the given object
        assertDecorated("2", 
                new String[] {TestAdaptableDecoratorContributor.SUFFIX}, 
                new Object[] { 
                        new Object()                
                },
                ICommon.class,
                false
            );
    }
    
    /**
     * Test non-adaptable contributions
     * 
     * @since 3.1
     */
    public final void testNonAdaptableContributions() throws CoreException {
        assertDecorated("1",
                new String[] {TestUnadaptableDecoratorContributor.SUFFIX}, 
                new Object[] {
                        new ObjectContributionClasses.A(),
                        new ObjectContributionClasses.B()},
                ICommon.class,
                false
            );
        assertDecorated("2",
                new String[] {TestUnadaptableDecoratorContributor.SUFFIX}, 
                new Object[] {
                        new ObjectContributionClasses.D(),
                        new ObjectContributionClasses.C(),
                        new ObjectContributionClasses.Common()},
                ICommon.class,
                true
            );
    }
    
    /**
     * This tests backwards compatibility support for adaptable IResource objectContributions. This
     * allows IResource adaptable contributions without an adapter factory and using
     * the IContributorResourceAdapter factory. In addition, test the ResourceMapping adaptations.
     * 
     * @since 3.1
     */
    public final void testContributorResourceAdapter() throws CoreException {
        
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject testProject = workspace.getRoot().getProject(ObjectContributionClasses.PROJECT_NAME);
        if(! testProject.exists()) {
            testProject.create(null);
        }
        if(! testProject.isOpen()) {
            testProject.open(null);
        }
        
        assertDecorated("1",
                new String[] {"IResource.1"}, 
                new Object[] {
                    new ObjectContributionClasses.CResource(), 
                    new ObjectContributionClasses.CFile()},
                IResource.class,
                true
            );

        assertDecorated("2", 
                new String[] {"ResourceMapping.1"}, 
                new Object[] {
                        new ObjectContributionClasses.CFile(), 
                        new ObjectContributionClasses.CResource()},
                ResourceMapping.class,
                true
            );
        assertDecorated("3", 
                new String[] {"ResourceMapping.1", "IResource.1"}, 
                new Object[] {
                    new ObjectContributionClasses.ModelElement()},
                ResourceMapping.class,
                true
            );
        // Ensure that the case where an object uses a contribution adapter that doesn't handle mappings
        // will still show the menus for resource mappings
        assertDecorated("4", 
                new String[] {"ResourceMapping.1", "IResource.1"}, 
                new Object[] {
                    new ObjectContributionClasses.CResourceOnly()},
                ResourceMapping.class,
                true
            );
    }
}
