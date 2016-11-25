package org.eclipse.debug.tests.launching;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.core.groups.GroupLaunchConfigurationDelegate;
import org.eclipse.debug.internal.core.groups.GroupLaunchElement;
import org.eclipse.debug.internal.core.groups.GroupLaunchElement.GroupElementPostLaunchAction;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.IDebugUIConstants;

public class LaunchGroupTests extends AbstractLaunchTest {

	private static final String GROUP_TYPE = "org.eclipse.debug.core.groups.GroupLaunchConfigurationType"; //$NON-NLS-1$
	private static final String DEF_GRP_NAME = "Test Group"; //$NON-NLS-1$

	private final AtomicInteger launchCount = new AtomicInteger(0);
	private ILaunchConfiguration lcToCount = null;
	private ILaunchListener lcListener = new ILaunchListener() {
		@Override
		public void launchRemoved(ILaunch launch) {
		}

		@Override
		public void launchChanged(ILaunch launch) {
		}

		@Override
		public void launchAdded(ILaunch launch) {
			if (launch.getLaunchConfiguration().contentsEqual(lcToCount)) {
				launchCount.incrementAndGet();
			}
		}
	};

	public LaunchGroupTests() {
		super("Launch Groups Test"); //$NON-NLS-1$
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// reset count
		launchCount.set(0);
	}

	@Override
	protected void tearDown() throws Exception {
		// make sure listener is removed
		getLaunchManager().removeLaunchListener(lcListener);

		super.tearDown();
	}

	private ILaunchConfiguration createLaunchGroup(String name, GroupLaunchElement... children) throws CoreException {
		ILaunchConfigurationWorkingCopy grp = getLaunchManager().getLaunchConfigurationType(GROUP_TYPE).newInstance(null, name);
		GroupLaunchConfigurationDelegate.storeLaunchElements(grp, Arrays.asList(children));
		return grp.doSave();
	}

	private GroupLaunchElement createLaunchGroupElement(ILaunchConfiguration source, GroupElementPostLaunchAction action, Object param, boolean adopt) {
		GroupLaunchElement e = new GroupLaunchElement();

		e.name = source.getName();
		e.data = source;
		e.action = action;
		e.actionParam = param;
		e.mode = GroupLaunchElement.MODE_INHERIT;
		e.enabled = true;
		e.adoptIfRunning = adopt;

		return e;
	}

	private LaunchHistory getRunLaunchHistory() {
		LaunchHistory h = getLaunchConfigurationManager().getLaunchHistory(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);

		// clear the history
		for (ILaunchConfiguration c : h.getHistory()) {
			h.removeFromHistory(c);
		}

		return h;
	}

	public void testNone() throws Exception {
		ILaunchConfiguration t1 = getLaunchConfiguration("Test1"); //$NON-NLS-1$
		ILaunchConfiguration t2 = getLaunchConfiguration("Test2"); //$NON-NLS-1$
		ILaunchConfiguration grp = createLaunchGroup(DEF_GRP_NAME, createLaunchGroupElement(t1, GroupElementPostLaunchAction.NONE, null, false), createLaunchGroupElement(t2, GroupElementPostLaunchAction.NONE, null, false));

		// attention: need to do this before launching!
		LaunchHistory runHistory = getRunLaunchHistory();
		grp.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());

		ILaunchConfiguration[] history = runHistory.getHistory();
		assertTrue("history should be size 3", history.length == 3); //$NON-NLS-1$
		assertTrue("history[0] should be Test Group", history[0].contentsEqual(grp)); //$NON-NLS-1$
		assertTrue("history[1] should be Test2", history[1].contentsEqual(t2)); //$NON-NLS-1$
		assertTrue("history[2] should be Test1", history[2].contentsEqual(t1)); //$NON-NLS-1$
	}

	public void testDelay() throws Exception {
		ILaunchConfiguration t1 = getLaunchConfiguration("Test1"); //$NON-NLS-1$
		ILaunchConfiguration t2 = getLaunchConfiguration("Test2"); //$NON-NLS-1$
		ILaunchConfiguration grp = createLaunchGroup(DEF_GRP_NAME, createLaunchGroupElement(t1, GroupElementPostLaunchAction.DELAY, 2, false), createLaunchGroupElement(t2, GroupElementPostLaunchAction.NONE, null, false));

		long start = System.currentTimeMillis();
		// attention: need to do this before launching!
		LaunchHistory runHistory = getRunLaunchHistory();
		grp.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());

		assertTrue("delay was not awaited", (System.currentTimeMillis() - start) > 2000); //$NON-NLS-1$

		ILaunchConfiguration[] history = runHistory.getHistory();
		assertTrue("history should be size 3", history.length == 3); //$NON-NLS-1$
		assertTrue("history[0] should be Test Group", history[0].contentsEqual(grp)); //$NON-NLS-1$
		assertTrue("history[1] should be Test2", history[1].contentsEqual(t2)); //$NON-NLS-1$
		assertTrue("history[2] should be Test1", history[2].contentsEqual(t1)); //$NON-NLS-1$
	}

	public void testTerminated() throws Exception {
		final ILaunchConfiguration t1 = getLaunchConfiguration("Test1"); //$NON-NLS-1$
		final ILaunchConfiguration t2 = getLaunchConfiguration("Test2"); //$NON-NLS-1$
		ILaunchConfiguration grp = createLaunchGroup(DEF_GRP_NAME, createLaunchGroupElement(t1, GroupElementPostLaunchAction.WAIT_FOR_TERMINATION, null, false), createLaunchGroupElement(t2, GroupElementPostLaunchAction.NONE, null, false));

		long start = System.currentTimeMillis();
		new Thread("Terminate Test1") { //$NON-NLS-1$
			@Override
			public void run() {
				try {
					// wait for some time
					Thread.sleep(2000);

					// now find and nuke Test1
					for (ILaunch l : getLaunchManager().getLaunches()) {
						if (l.getLaunchConfiguration().contentsEqual(t1)) {
							// add a dummy process, otherwise the launch never
							// terminates...
							InvocationHandler handler = new InvocationHandler() {
								@Override
								public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
									String name = method.getName();
									if (name.equals("equals")) { //$NON-NLS-1$
										return args.length == 1 && proxy == args[0];
									}
									if (name.equals("getStreamsProxy")) { //$NON-NLS-1$
										return null;
									}
									return Boolean.TRUE;
								}
							};
							IProcess process = (IProcess) Proxy.newProxyInstance(LaunchGroupTests.class.getClassLoader(), new Class[] {
									IProcess.class,
									IDisconnect.class }, handler);
							l.addProcess(process);
							l.terminate();
						}
					}
				} catch (Exception e) {
					// uh oh
					e.printStackTrace();
				}
			}
		}.start();

		// attention: need to do this before launching!
		LaunchHistory runHistory = getRunLaunchHistory();
		grp.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());

		assertTrue("returned before termination of Test1", (System.currentTimeMillis() - start) > 2000); //$NON-NLS-1$

		// is there a way to assert that the group waited for test1 to
		// terminate? don't think so - at least run the code path to have it
		// covered.
		ILaunchConfiguration[] history = runHistory.getHistory();
		assertTrue("history should be size 3", history.length == 3); //$NON-NLS-1$
		assertTrue("history[0] should be Test Group", history[0].contentsEqual(grp)); //$NON-NLS-1$
		assertTrue("history[1] should be Test2", history[1].contentsEqual(t2)); //$NON-NLS-1$
		assertTrue("history[2] should be Test1", history[2].contentsEqual(t1)); //$NON-NLS-1$
	}

	public void testAdopt() throws Exception {
		final ILaunchConfiguration t1 = getLaunchConfiguration("Test1"); //$NON-NLS-1$
		final ILaunchConfiguration grp = createLaunchGroup(DEF_GRP_NAME, createLaunchGroupElement(t1, GroupElementPostLaunchAction.NONE, null, false), createLaunchGroupElement(t1, GroupElementPostLaunchAction.NONE, null, true));

		// attention: need to do this before launching!
		LaunchHistory runHistory = getRunLaunchHistory();

		lcToCount = t1;
		getLaunchManager().addLaunchListener(lcListener);
		grp.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());

		ILaunchConfiguration[] history = runHistory.getHistory();
		assertTrue("history should be size 2", history.length == 2); //$NON-NLS-1$
		assertTrue("history[0] should be Test Group", history[0].contentsEqual(grp)); //$NON-NLS-1$
		assertTrue("history[1] should be Test1", history[1].contentsEqual(t1)); //$NON-NLS-1$
		assertTrue("Test1 should be launched only once", launchCount.get() == 1); //$NON-NLS-1$
	}

	public void testAdoptComplex() throws Exception {
		final ILaunchConfiguration t1 = getLaunchConfiguration("Test1"); //$NON-NLS-1$

		// Group 1 has Test1 (adopt = false)
		// Group 2 has Group 1
		// Group 3 has Group 2 and Test1 (adopt = true)

		final ILaunchConfiguration grp = createLaunchGroup(DEF_GRP_NAME, createLaunchGroupElement(t1, GroupElementPostLaunchAction.NONE, null, false));
		final ILaunchConfiguration grp2 = createLaunchGroup("Group 2", createLaunchGroupElement(grp, GroupElementPostLaunchAction.NONE, null, false)); //$NON-NLS-1$
		final ILaunchConfiguration grp3 = createLaunchGroup("Group 3", createLaunchGroupElement(grp2, GroupElementPostLaunchAction.NONE, null, false), createLaunchGroupElement(t1, GroupElementPostLaunchAction.NONE, null, true)); //$NON-NLS-1$

		// attention: need to do this before launching!
		LaunchHistory runHistory = getRunLaunchHistory();

		lcToCount = t1;
		getLaunchManager().addLaunchListener(lcListener);
		grp3.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());

		ILaunchConfiguration[] history = runHistory.getHistory();
		assertTrue("history should be size 4", history.length == 4); //$NON-NLS-1$
		assertTrue("history[0] should be Group 3", history[0].contentsEqual(grp3)); //$NON-NLS-1$
		assertTrue("history[1] should be Group 2", history[1].contentsEqual(grp2)); //$NON-NLS-1$
		assertTrue("history[2] should be Group 1", history[2].contentsEqual(grp)); //$NON-NLS-1$
		assertTrue("history[3] should be Test1", history[3].contentsEqual(t1)); //$NON-NLS-1$
		assertTrue("Test1 should be launched only once", launchCount.get() == 1); //$NON-NLS-1$
	}

}
