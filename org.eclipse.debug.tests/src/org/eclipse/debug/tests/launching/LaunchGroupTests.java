package org.eclipse.debug.tests.launching;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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

	public LaunchGroupTests() {
		super("Launch Groups Test"); //$NON-NLS-1$
	}

	private ILaunchConfiguration createLaunchGroup(GroupLaunchElement... children) throws CoreException {
		ILaunchConfigurationWorkingCopy grp = getLaunchManager().getLaunchConfigurationType(GROUP_TYPE).newInstance(null, "Test Group"); //$NON-NLS-1$
		GroupLaunchConfigurationDelegate.storeLaunchElements(grp, Arrays.asList(children));
		return grp.doSave();
	}

	private GroupLaunchElement createLaunchGroupElement(ILaunchConfiguration source, GroupElementPostLaunchAction action, Object param) {
		GroupLaunchElement e = new GroupLaunchElement();

		e.name = source.getName();
		e.data = source;
		e.action = action;
		e.actionParam = param;
		e.mode = GroupLaunchElement.MODE_INHERIT;
		e.enabled = true;

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
		ILaunchConfiguration grp = createLaunchGroup(createLaunchGroupElement(t1, GroupElementPostLaunchAction.NONE, null), createLaunchGroupElement(t2, GroupElementPostLaunchAction.NONE, null));

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
		ILaunchConfiguration grp = createLaunchGroup(createLaunchGroupElement(t1, GroupElementPostLaunchAction.DELAY, 2), createLaunchGroupElement(t2, GroupElementPostLaunchAction.NONE, null));

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
		ILaunchConfiguration grp = createLaunchGroup(createLaunchGroupElement(t1, GroupElementPostLaunchAction.WAIT_FOR_TERMINATION, null), createLaunchGroupElement(t2, GroupElementPostLaunchAction.NONE, null));

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

}
