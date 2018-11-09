package org.eclipse.e4.ui.tests.utilities;

import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.services.ContextServiceAddon;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class HeadlessApplicationRule implements TestRule {
	private IEclipseContext applicationContext;

	/**
	 * @return the applicationContext
	 */
	public IEclipseContext getApplicationContext() {
		return applicationContext;
	}

	@Override
    public Statement apply(Statement base, Description description) {
        return new MyStatement(base);
    }

    public class MyStatement extends Statement {
        private final Statement base;

        public MyStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
			applicationContext = createApplicationContext();
            try {
                base.evaluate();
            } finally {
				applicationContext.dispose();
            }
        }
    }


	protected IEclipseContext createApplicationContext() {
		final IEclipseContext appContext = E4Application.createDefaultContext();
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
		ContextInjectionFactory.make(ContextServiceAddon.class, appContext);
		return appContext;
	}
}