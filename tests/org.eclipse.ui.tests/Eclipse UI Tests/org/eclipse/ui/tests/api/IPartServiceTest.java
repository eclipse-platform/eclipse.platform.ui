/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.tests.util.CallHistory;
import org.eclipse.ui.tests.util.EmptyPerspective;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests the IPartService, IPartListener and IPartListener2 interfaces.
 */
public class IPartServiceTest extends UITestCase {

    private IWorkbenchWindow fWindow;

    private IWorkbenchPage fPage;

    // Event state.
    private IWorkbenchPart eventPart;

    private IWorkbenchPartReference eventPartRef;

    class TestPartListener implements IPartListener {
        public void partActivated(IWorkbenchPart part) {
            history.add("partActivated");
            eventPart = part;
        }

        public void partBroughtToTop(IWorkbenchPart part) {
            history.add("partBroughtToTop");
            eventPart = part;
        }

        public void partClosed(IWorkbenchPart part) {
            history.add("partClosed");
            eventPart = part;
        }

        public void partDeactivated(IWorkbenchPart part) {
            history.add("partDeactivated");
            eventPart = part;
        }

        public void partOpened(IWorkbenchPart part) {
            history.add("partOpened");
            eventPart = part;
        }
    }

    class TestPartListener2 implements IPartListener2 {
        public void partActivated(IWorkbenchPartReference ref) {
            history2.add("partActivated");
            eventPartRef = ref;
        }

        public void partBroughtToTop(IWorkbenchPartReference ref) {
            history2.add("partBroughtToTop");
            eventPartRef = ref;
        }

        public void partClosed(IWorkbenchPartReference ref) {
            history2.add("partClosed");
            eventPartRef = ref;
        }

        public void partDeactivated(IWorkbenchPartReference ref) {
            history2.add("partDeactivated");
            eventPartRef = ref;
        }

        public void partOpened(IWorkbenchPartReference ref) {
            history2.add("partOpened");
            eventPartRef = ref;
        }

        public void partHidden(IWorkbenchPartReference ref) {
            history2.add("partHidden");
            eventPartRef = ref;
        }

        public void partVisible(IWorkbenchPartReference ref) {
            history2.add("partVisible");
            eventPartRef = ref;
        }

        public void partInputChanged(IWorkbenchPartReference ref) {
            history2.add("partInputChanged");
            eventPartRef = ref;
        }
    }

    private IPartListener partListener = new TestPartListener();

    private IPartListener2 partListener2 = new TestPartListener2();

    private CallHistory history = new CallHistory(partListener);

    private CallHistory history2 = new CallHistory(partListener2);

    public IPartServiceTest(String testName) {
        super(testName);
    }

    /**
     * Clear the event state.
     */
    private void clearEventState() {
        eventPart = null;
        eventPartRef = null;
        history.clear();
        history2.clear();
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        fWindow = openTestWindow();
        fPage = fWindow.getActivePage();
    }

    private IWorkbenchPartReference getRef(IWorkbenchPart part) {
        return ((PartSite) part.getSite()).getPartReference();
    }

    /**
     * Tests the addPartListener method on IWorkbenchPage's part service.
     */
    public void testAddPartListenerToPage() throws Throwable {
        // From Javadoc: "Adds the given listener for part lifecycle events.
        // Has no effect if an identical listener is already registered."
        fPage.addPartListener(partListener);
        fPage.addPartListener(partListener2);

        // Open a view.
        // Verify events are received.
        clearEventState();
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        assertTrue(history.verifyOrder(new String[] { "partOpened",
                "partActivated" }));
        assertEquals(view, eventPart);
        assertTrue(history2.verifyOrder(new String[] { "partOpened",
                "partVisible", "partActivated" }));
        assertEquals(getRef(view), eventPartRef);

        // Hide the view
        // Verify events are received.
        clearEventState();
        fPage.hideView(view);
        assertTrue(history.verifyOrder(new String[] { "partDeactivated",
                "partClosed" }));
        assertEquals(view, eventPart);
        assertTrue(history2.verifyOrder(new String[] { "partDeactivated",
                "partHidden", "partClosed" }));
        assertEquals(getRef(view), eventPartRef);
    }

    /**
     * Tests the addPartListener method on IWorkbenchWindow's part service.
     */
    public void testAddPartListenerToWindow() throws Throwable {
        // From Javadoc: "Adds the given listener for part lifecycle events.
        // Has no effect if an identical listener is already registered."
        fWindow.getPartService().addPartListener(partListener);
        fWindow.getPartService().addPartListener(partListener2);

        // Open a view.
        // Verify events are received.
        clearEventState();
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        assertTrue(history.verifyOrder(new String[] { "partOpened",
                "partActivated" }));
        assertEquals(view, eventPart);
        assertTrue(history2.verifyOrder(new String[] { "partOpened",
                "partVisible", "partActivated" }));
        assertEquals(getRef(view), eventPartRef);

        // Hide the view
        // Verify events are received.
        clearEventState();
        fPage.hideView(view);
        assertTrue(history.verifyOrder(new String[] { "partDeactivated",
                "partClosed" }));
        assertEquals(view, eventPart);
        assertTrue(history2.verifyOrder(new String[] { "partDeactivated",
                "partHidden", "partClosed" }));
        assertEquals(getRef(view), eventPartRef);
    }

    /**
     * Tests the removePartListener method on IWorkbenchPage's part service.
     */
    public void testRemovePartListenerFromPage() throws Throwable {
        // From Javadoc: "Removes the given part listener.
        // Has no affect if an identical listener is not registered."

        // Add and remove listener.
        fPage.addPartListener(partListener);
        fPage.addPartListener(partListener2);
        fPage.removePartListener(partListener);
        fPage.removePartListener(partListener2);

        // Open a view.
        // Verify no events are received.
        clearEventState();
        fPage.showView(MockViewPart.ID);
        assertTrue(history.isEmpty());
        assertTrue(history2.isEmpty());
    }

    /**
     * Tests the removePartListener method on IWorkbenchWindow's part service.
     */
    public void testRemovePartListenerFromWindow() throws Throwable {
        // From Javadoc: "Removes the given part listener.
        // Has no affect if an identical listener is not registered."

        // Add and remove listener.
        fWindow.getPartService().addPartListener(partListener);
        fWindow.getPartService().addPartListener(partListener2);
        fWindow.getPartService().removePartListener(partListener);
        fWindow.getPartService().removePartListener(partListener2);

        // Open a view.
        // Verify no events are received.
        clearEventState();
        fPage.showView(MockViewPart.ID);
        assertTrue(history.isEmpty());
        assertTrue(history2.isEmpty());
    }

    /**
     * Tests the partHidden method by closing a view when it is not shared with another perspective.
     * Includes regression test for: 
     *   Bug 60039 [ViewMgmt] (regression) IWorkbenchPage#findView returns non-null value after part has been closed
     */
    public void testPartHiddenWhenClosedAndUnshared() throws Throwable {
        IPartListener2 listener = new TestPartListener2() {
            public void partHidden(IWorkbenchPartReference ref) {
                super.partHidden(ref);
                // ensure that the notification is for the view we closed
                assertEquals(MockViewPart.ID, ref.getId());
                // ensure that the view cannot be found
                assertNull(fPage.findView(MockViewPart.ID));
            }
        };
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        fPage.addPartListener(listener);
        clearEventState();
        fPage.hideView(view);
        assertTrue(history2.contains("partHidden"));
        assertEquals(getRef(view), eventPartRef);
    }

    /**
     * Tests the partHidden method by closing a view when it is shared with another perspective.
     * Includes regression test for: 
     *   Bug 60039 [ViewMgmt] (regression) IWorkbenchPage#findView returns non-null value after part has been closed
     */
    public void testPartHiddenWhenClosedAndShared() throws Throwable {
        IPartListener2 listener = new TestPartListener2() {
            public void partHidden(IWorkbenchPartReference ref) {
                super.partHidden(ref);
                // ensure that the notification is for the view we closed
                assertEquals(MockViewPart.ID, ref.getId());
                // ensure that the view cannot be found
                assertNull(fPage.findView(MockViewPart.ID));
            }
        };
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        IPerspectiveDescriptor emptyPerspDesc2 = fWindow.getWorkbench()
                .getPerspectiveRegistry().findPerspectiveWithId(
                        EmptyPerspective.PERSP_ID2);
        fPage.setPerspective(emptyPerspDesc2);
        MockViewPart view2 = (MockViewPart) fPage.showView(MockViewPart.ID);
        assertTrue(view == view2);
        fPage.addPartListener(listener);
        clearEventState();
        fPage.hideView(view);
        assertTrue(history2.contains("partHidden"));
        assertEquals(getRef(view), eventPartRef);
    }

    /**
     * Tests the partHidden method by activating another view in the same folder.
     */
    public void testPartHiddenWhenObscured() throws Throwable {
        final boolean[] eventReceived = { false };
        IPartListener2 listener = new TestPartListener2() {
            public void partHidden(IWorkbenchPartReference ref) {
                super.partHidden(ref);
                // ensure that the notification is for the view that was obscured
                assertEquals(MockViewPart.ID, ref.getId());
                // ensure that the view can still be found
                assertNotNull(fPage.findView(MockViewPart.ID));
                eventReceived[0] = true;
            }
        };
        MockViewPart view2 = (MockViewPart) fPage.showView(MockViewPart.ID2);
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        assertEquals(view, fPage.getActivePart());
        fPage.addPartListener(listener);
        clearEventState();
        fPage.activate(view2);
        assertTrue(eventReceived[0]);
    }

    /**
     * Tests the partVisible method by showing a view when it is not
     * open in any other perspectives.
     */
    public void testPartVisibleWhenOpenedUnshared() throws Throwable {
        final boolean[] eventReceived = { false };
        IPartListener2 listener = new TestPartListener2() {
            public void partVisible(IWorkbenchPartReference ref) {
                super.partVisible(ref);
                // ensure that the notification is for the view we opened
                assertEquals(MockViewPart.ID, ref.getId());
                // ensure that the view can be found
                assertNotNull(fPage.findView(MockViewPart.ID));
                eventReceived[0] = true;
            }
        };
        fPage.addPartListener(listener);
        clearEventState();
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        assertEquals(view, fPage.getActivePart());
        assertTrue(eventReceived[0]);
    }

    /**
     * Tests the partVisible method by showing a view when it is already
     * open in another perspective.
     */
    public void testPartVisibleWhenOpenedShared() throws Throwable {
        final boolean[] eventReceived = { false };
        IPartListener2 listener = new TestPartListener2() {
            public void partVisible(IWorkbenchPartReference ref) {
                super.partVisible(ref);
                // ensure that the notification is for the view we opened
                assertEquals(MockViewPart.ID, ref.getId());
                // ensure that the view can be found
                assertNotNull(fPage.findView(MockViewPart.ID));
                eventReceived[0] = true;
            }
        };
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        IPerspectiveDescriptor emptyPerspDesc2 = fWindow.getWorkbench()
                .getPerspectiveRegistry().findPerspectiveWithId(
                        EmptyPerspective.PERSP_ID2);
        fPage.setPerspective(emptyPerspDesc2);
        fPage.addPartListener(listener);
        clearEventState();
        MockViewPart view2 = (MockViewPart) fPage.showView(MockViewPart.ID);
        assertTrue(view == view2);
        assertEquals(view2, fPage.getActivePart());
        assertTrue(eventReceived[0]);
    }

    /**
     * Tests the partVisible method by activating a view obscured by
     * another view in the same folder.
     */
    public void testPartVisibleWhenObscured() throws Throwable {
        final boolean[] eventReceived = { false };
        IPartListener2 listener = new TestPartListener2() {
            public void partVisible(IWorkbenchPartReference ref) {
                super.partVisible(ref);
                // ensure that the notification is for the view we revealed
                assertEquals(MockViewPart.ID, ref.getId());
                // ensure that the view can still be found
                assertNotNull(fPage.findView(MockViewPart.ID));
                eventReceived[0] = true;
            }
        };
        MockViewPart view = (MockViewPart) fPage.showView(MockViewPart.ID);
        MockViewPart view2 = (MockViewPart) fPage.showView(MockViewPart.ID2);
        assertEquals(view2, fPage.getActivePart());
        fPage.addPartListener(listener);
        clearEventState();
        fPage.activate(view);
        assertTrue(eventReceived[0]);
    }

}