/*******************************************************************************
 * Steven Spungin <steven@spungin.tv> - Ongoing maintenance
 ******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
import org.eclipse.emf.databinding.internal.EMFValueProperty;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.edit.command.SetCommand;

/**
 * An updater to deal with unsettable primitive attributes on EMF objects. EMF's {@link EMFEditValueProperty}/
 * {@link EMFValueProperty} does an eGet to
 * retrieve the attribute value, with no regard as to whether the attribute is
 * unset. If the attribute is unset, then eGet() will return the default value.
 *
 * This implementation makes several assumptions:
 * <ul>
 * <li>Assumes that these unsettable attributes have a nonsensical default value that can be used to detect an eGet of
 * an unset attribute.</li>
 * <li>Assumes that we are using EMFEditObservables so that changes are described using {@link SetCommand}, such that we
 * can provide {@link SetCommand#UNSET_VALUE} to remove a value.</li>
 * </ul>
 * See the following discussions for background details:
 * <ul>
 * <li><a href="http://www.eclipse.org/forums/index.php?t=msg&th=165026/">Dynamic eGet for unsettable attributes</a></li>
 * <li><a href="http://www.eclipsezone.com/eclipse/forums/t114431.html?start=15"> Creating a ComboViewer for an
 * EReference</a> particularly the later postings from Tom Schindl on handling null values</li>
 * <li><a href="http://www.eclipse.org/forums/index.php?t=msg&th=174967/"> ObservableMapCellLabelProvider doesn't work
 * well with unsettable features</a></li>
 * </ul>
 */
public class UnsettableUpdateValueStrategy extends EMFUpdateValueStrategy {
	@Override
	protected IConverter createConverter(Object fromType, Object toType) {
		if (fromType == String.class) {
			if (toType instanceof EAttribute) {
				final EAttribute eAttribute = (EAttribute) toType;
				final EDataType eDataType = eAttribute.getEAttributeType();
				final EFactory eFactory = eDataType.getEPackage().getEFactoryInstance();
				return new Converter(fromType, toType) {
					@Override
					public Object convert(Object fromObject) {
						final String value = fromObject == null ? null : fromObject.toString();
						if (value == null || value.length() == 0) {
							return SetCommand.UNSET_VALUE;
						}
						if (eAttribute.isMany()) {
							final List<Object> result = new ArrayList<Object>();

							for (final String element : value.split(" ")) { //$NON-NLS-1$
								result.add(eFactory.createFromString(eDataType, element));

							}
							return result;
						}
						return eFactory.createFromString(eDataType, value);
					}
				};
			}
		} else if (toType == String.class) {
			if (fromType instanceof EAttribute) {
				final EAttribute eAttribute = (EAttribute) fromType;
				final EDataType eDataType = eAttribute.getEAttributeType();
				final EFactory eFactory = eDataType.getEPackage().getEFactoryInstance();
				return new Converter(fromType, toType) {
					@Override
					public Object convert(Object fromObject) {
						if (eAttribute.isMany()) {
							final StringBuilder result = new StringBuilder();
							for (final Object value : (List<?>) fromObject) {
								if (result.length() == 0) {
									result.append(' ');
								}
								result.append(eFactory.convertToString(eDataType, value));
							}
							return result.toString();
						}
						// If the value
						if (fromObject == SetCommand.UNSET_VALUE || fromObject == null
							|| fromObject.equals(eAttribute.getDefaultValue())) {
							return ""; //$NON-NLS-1$
						}
						return eFactory.convertToString(eDataType, fromObject);
					}
				};
			}
		}
		return super.createConverter(fromType, toType);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.databinding.UpdateValueStrategy#doSet(org.eclipse.core
	 * .databinding.observable.value.IObservableValue, java.lang.Object)
	 */
	@Override
	protected IStatus doSet(IObservableValue observableValue, Object value) {
		// TODO Auto-generated method stub
		return super.doSet(observableValue, value);
	}

}
