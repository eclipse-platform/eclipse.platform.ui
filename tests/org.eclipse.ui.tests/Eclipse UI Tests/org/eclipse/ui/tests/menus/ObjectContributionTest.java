/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.menus;

import java.io.ByteArrayInputStream;
import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.tests.menus.ObjectContributionClasses.ICommon;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests that object contributions are enabled and shown correctly in pop-up
 * menus depending on the state of the workbench. This test relies on the
 * <code>plugin.xml</code> file containing certain values. Please see the
 * appropriate section in that file for more information about the initial
 * set-up.
 * 
 * @since 3.0
 */
public final class ObjectContributionTest extends UITestCase {

    /**
     * Constructs a new instance of <code>ObjectContributionTest</code> with
     * the name of the test.
     * 
     * @param name
     *            The name of the test; may be <code>null</code>.
     */
    public ObjectContributionTest(final String name) {
        super(name);
    }

    /**
     * Tests whether the content-type object contribution works. This is testing
     * a use care familiar to Ant UI. The content-type scans an XML file to see
     * if its root element is <code>&lt;project&gt;</code>.
     * 
     * @throws CoreException
     *             If a problem occurs when creating the project or file, or if
     *             the project can't be opened.
     */
    public final void testObjectStateContentType() throws CoreException {
        // Create an XML file with <project> as its root element.
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject testProject = workspace.getRoot().getProject(
                "ObjectContributionTestProject");
        testProject.create(null);
        testProject.open(null);
        final IFile xmlFile = testProject.getFile("ObjectContributionTest.xml");
        final String contents = "<testObjectStateContentTypeElement></testObjectStateContentTypeElement>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(
                contents.getBytes());
        xmlFile.create(inputStream, true, null);
        final ISelection selection = new StructuredSelection(xmlFile);
        assertPopupMenus("1", new String[] {"org.eclipse.ui.tests.testObjectStateContentType"}, selection, null, true);
    }
    
    /**
     * This tests backwards compatibility support for adaptable IResource objectContributions. This
     * allows IResource adaptable contributions without an adapter factory and using
     * the IContributorResourceAdapter factory.
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
    	
    	assertPopupMenus("1",
    			new String[] {"IResource.1"}, 
    			new StructuredSelection(new Object[] {new ObjectContributionClasses.CResource()}),
				IResource.class,
				true
			);
    	assertPopupMenus("2",
    			new String[] {"IProject.1"}, 
    			new StructuredSelection(new Object[] {new ObjectContributionClasses.CFile()}),
				null,
				false
			);
    	assertPopupMenus("3", 
    			new String[] {"IFile.1"}, 
    			new StructuredSelection(new Object[] {new ObjectContributionClasses.CFile()}),
				IFile.class,
				true
			);
    	assertPopupMenus("4", 
    			new String[] {"IResource.1"}, 
    			new StructuredSelection(new Object[] {new ObjectContributionClasses.CFile(), new ObjectContributionClasses.CResource()}),
				IResource.class,
				true
			);
    	assertPopupMenus("5", 
    			new String[] {"IFile.1", "IProject.1"}, 
    			new StructuredSelection(new Object[] {new ObjectContributionClasses.CFile(), new ObjectContributionClasses.CResource()}),
				IResource.class,
				false
			);
    }
    
    /**
     * This tests adaptable contributions that are not IResource.
     * 
     * @since 3.1
     */
    public final void testAdaptables() throws CoreException {
    	assertPopupMenus("1", 
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.A()}),
				ICommon.class,
				true
			);  
    	assertPopupMenus("2", 
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.A(), 
						new ObjectContributionClasses.B()}),
				ICommon.class,
				true
			);      	
    	assertPopupMenus("3", 
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.A(), 
						new ObjectContributionClasses.B(), 
						new ObjectContributionClasses.C(),
						new ObjectContributionClasses.Common()
				}),
				ICommon.class,
				true
			);
    	assertPopupMenus("4", 
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.Common(), 
						new ObjectContributionClasses.C(), 
						new ObjectContributionClasses.B(),
						new ObjectContributionClasses.A()
				}),
				ICommon.class,
				true
			);
    	assertPopupMenus("5", 
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.Common(), 
						new ObjectContributionClasses.C(), 
						new ObjectContributionClasses.B(),
						new ObjectContributionClasses.C(), 
						new ObjectContributionClasses.A(),
						new ObjectContributionClasses.Common() 
				}),
				ICommon.class,
				true
			);
    	assertPopupMenus("6", 
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] { 
						new ObjectContributionClasses.C(),
						new ObjectContributionClasses.Common()
				}),
				ICommon.class,
				true
			);
    	assertPopupMenus("7", 
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] { 
						new Object()				
				}),
				ICommon.class,
				false
			);
    	assertPopupMenus("8", 
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] { 
    					new ObjectContributionClasses.C(),
						new Object()				
				}),
				ICommon.class,
				false
			);
    	assertPopupMenus("9", 
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] { 
    					new ObjectContributionClasses.C(),
    					new ObjectContributionClasses.A(),
						new Object()				
				}),
				ICommon.class,
				false
			);
    }
    
    /**
     * Ensure that there are no duplicate contributions.
     * 
     * @since 3.1
     */
    public final void testDuplicateAdaptables() throws CoreException {
    	assertPopupMenus("1",
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.D()}),
				ICommon.class,
				true
			);
    	// repeat test on purpose to ensure no double call duplicates.    	
    	assertPopupMenus("1",
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.D()}),
				ICommon.class,
				true
			);
    	assertPopupMenus("2",
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.D(),
						new ObjectContributionClasses.A()
						}),
				ICommon.class,
				true
			);
    	assertPopupMenus("3",
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.A(),
						new ObjectContributionClasses.D()
						}),
				ICommon.class,
				true
			);
    	assertPopupMenus("4",
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.Common(),
						new ObjectContributionClasses.D()
						}),
				ICommon.class,
				true
			);
    	assertPopupMenus("5",
    			new String[] {"ICommon.1"}, 
    			new StructuredSelection(new Object[] {    					
						new ObjectContributionClasses.D(),
						new ObjectContributionClasses.Common()
						}),
				ICommon.class,
				true
			);
    }
    
    /**
     * Test non-adaptable contributions
     * 
     * @since 3.1
     */
    public final void testNonAdaptableContributions() throws CoreException {
    	assertPopupMenus("1",
    			new String[] {"ICommon.2"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.A(),
						new ObjectContributionClasses.B()}),
				ICommon.class,
				false
			);
    	assertPopupMenus("2",
    			new String[] {"ICommon.2"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.D(),
						new ObjectContributionClasses.C(),
						new ObjectContributionClasses.Common()}),
				ICommon.class,
				true
			); 
    	assertPopupMenus("3",
    			new String[] {"Common.2"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.D(),
						new ObjectContributionClasses.C(),
						new ObjectContributionClasses.A()}),
				ICommon.class,
				false
			); 
    	assertPopupMenus("4",
    			new String[] {"Common.2"}, 
    			new StructuredSelection(new Object[] {
    					new ObjectContributionClasses.B(),
						new ObjectContributionClasses.C(),
						new ObjectContributionClasses.A()}),
				ICommon.class,
				false
			); 
    }
    
    /**
     * Helper class that will create a popup menu based on the given selection and
     * then ensure that the provided commandIds are added to the menu.
     * 
     * @param commandIds the command ids that should appear in the menu
     * @param selection the selection on which to contribute object contributions
     */
    public void assertPopupMenus(String name, String[] commandIds, final ISelection selection, Class selectionType, boolean existance) {
    	ISelectionProvider selectionProvider = new ISelectionProvider() {
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}
			public ISelection getSelection() {
				return selection;
			}
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}
			public void setSelection(ISelection selection) {
			}
		};
		
		// The popup extender needs a part to notify actions of the active part
        final WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final IWorkbenchPage page = window.getActivePage();
        IWorkbenchPart part = page.getActivePartReference().getPart(true);
    	
    	 // Create a fake PopupMenuExtender so we can get some data back.
        final MenuManager fakeMenuManager = new MenuManager();
        fakeMenuManager.add(new GroupMarker(
                org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
        final PopupMenuExtender extender = new PopupMenuExtender(null,
                fakeMenuManager, selectionProvider, part);
        
        

        /*
         * Pretend to show the pop-up menu -- looking to motivate the extender
         * to fill the menu based on the selection provider.
         * 
         * TODO This causes a big delay (in the order of a minute or more) while
         * trying to fill this menu. It seems to be loading a bunch of plug-ins,
         * and doing class loading.
         */
        extender.menuAboutToShow(fakeMenuManager);

        // Check to see if the appropriate object contributions are present.
        final IContributionItem[] items = fakeMenuManager.getItems();
        Set seenCommands = new HashSet(Arrays.asList(commandIds));
        List commands = new ArrayList(Arrays.asList(commandIds));
        for (int i = 0; i < items.length; i++) {
           IContributionItem contributionItem = items[i];
           // Step 1: test the selection
           if (selectionType != null) {
				IContributionItem item = contributionItem;
				if (item instanceof SubContributionItem) {
					item = ((SubContributionItem) contributionItem).getInnerItem();
				}
				if (item instanceof PluginActionContributionItem) {
					// Verify that the selection passed to the action has been
					// converted
					PluginActionContributionItem pa = (PluginActionContributionItem) item;
					ISelection s = null;
					if (s instanceof IStructuredSelection) {
						for (Iterator it = ((IStructuredSelection) s).iterator(); it.hasNext();) {
							Object element = (Object) it.next();
							assertTrue(name + " selection not converted", selectionType.isInstance(element));
						}
					}
				}
			}
           // Step 2: remember that we saw this element
           String id = contributionItem.getId();
           if(existance) {    		
           		boolean removed = commands.remove(id);	
           		if(seenCommands.contains(id) && ! removed) {
           			fail(name + " item duplicated in the context menu: " + id);
           		}           		
           } else {
           		assertTrue(name + " item should not be in the context menu", ! commands.contains(id));
           }
        }
        
        if(existance && ! commands.isEmpty()) {
        	fail(name + " Missing " + commands.toString() + " from context menu.");
        }
    }
}