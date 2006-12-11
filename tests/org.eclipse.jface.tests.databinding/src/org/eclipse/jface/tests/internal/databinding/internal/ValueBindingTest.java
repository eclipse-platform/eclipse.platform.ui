/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Brad Reynolds - bug 164653
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal;

import junit.framework.TestCase;

import org.eclipse.core.databinding.DefaultBindSpec;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.IBindingListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.ValueBinding;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 * 
 */
public class ValueBindingTest extends TestCase {
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
    }
    
	/**
	 * Bug 152543.
	 * 
	 * @throws Exception
	 */
	public void testNoUpdateTargetFromModel() throws Exception {
		DefaultBindSpec spec = new DefaultBindSpec();
		spec.setUpdateModel(false);
		spec.setUpdateTarget(false);

		try {
			new ValueBinding(new DataBindingContext(),
					new ObservableValueStub(), new ObservableValueStub(), spec);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
    
    public void testValuePropagation() throws Exception {
        DefaultBindSpec spec = new DefaultBindSpec();
        String initialValue = "value";
        
        WritableValue target = new WritableValue(String.class);
        WritableValue model = new WritableValue(String.class, initialValue);
        
        DataBindingContext dbc = new DataBindingContext();
        
        assertFalse(model.getValue().equals(target.getValue()));
        new ValueBinding(dbc, target, model, spec);
        
        assertEquals(target.getValue(), model.getValue());
    }
    
    public void testBindingListeners() {
        final int[] calls = new int[] { 0, 0 };
        // this exact sequence of positions are not API and may change from
        // release to release.
        // This is just here to check that we got a sane sequence of pipeline
        // positions
        // and to catch when the sequence changes when we don't expect it to
        // change.
        //
        // See BindingEvent#pipelinePosition for details.
        final int[] pipelinePositions = new int[] { 0, 1, 2, 3, 4, 0, 2, 4, 1,
                0, 1, 2, 0, 2, 4, 1 };
        WritableValue settableValue1 = new WritableValue(Object.class);
        WritableValue settableValue2 = new WritableValue(Object.class);
        
        Object o1 = new Object();
        Object o2 = new Object();
        
        settableValue1.setValue(o1);
        settableValue2.setValue(o2);
        
        DataBindingContext dbc = new DataBindingContext();
        Binding binding = dbc.bindValue(settableValue1, settableValue2, null);
        binding.addBindingEventListener(new IBindingListener() {
            public IStatus bindingEvent(BindingEvent e) {
                // Make sure we get the right sequence of pipeline positions
                assertEquals("Unexpected pipeline position at call #"
                        + calls[0], pipelinePositions[calls[0]],
                        e.pipelinePosition);
                calls[0]++;
                return Status.OK_STATUS;
            }
        });
        binding.addBindingEventListener(new IBindingListener() {
            public IStatus bindingEvent(BindingEvent e) {
                calls[1]++;
                return Status.OK_STATUS;
            }
        });
        assertEquals(o2, settableValue1.getValue());
        assertEquals(
                "Both binding events should be called the same number of times",
                calls[0], calls[1]);
        settableValue1.setValue(o1);
        assertEquals(o1, settableValue2.getValue());
        assertEquals(
                "Both binding events should be called the same number of times",
                calls[0], calls[1]);
        settableValue2.setValue(o2);
        assertEquals(
                "Both binding events should be called the same number of times",
                calls[0], calls[1]);
        assertEquals(o2, settableValue1.getValue());

        // Now test forcing an error from the event handler...
        binding.addBindingEventListener(new IBindingListener() {
            public IStatus bindingEvent(BindingEvent e) {
                if (e.pipelinePosition == BindingEvent.PIPELINE_AFTER_CONVERT) {
                    return ValidationStatus.error("error");
                }
                return Status.OK_STATUS;
            }
        });
        settableValue1.setValue(o1);
        settableValue2.setValue(o2);
        assertEquals(
                "Both binding events should be called the same number of times",
                calls[0], calls[1]);
        assertEquals("binding events should be called at least once", true,
                calls[0] > 0);
    }

	private static class ObservableValueStub extends AbstractObservableValue {
		protected Object doGetValue() {
			// do nothing
			return null;
		}

		public Object getValueType() {
			// do nothing
			return null;
		}

        protected void doSetValue(Object value) {
            
        }
	}
}
