package org.eclipse.e4.ui.model.internal;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.ETypeParameter;

public class ModelUtils {
	public static EClassifier getTypeArgument(EClass eClass,
			EGenericType eGenericType) {
		ETypeParameter eTypeParameter = eGenericType.getETypeParameter();

		if( eTypeParameter != null ) {
			for (EGenericType eGenericSuperType : eClass.getEAllGenericSuperTypes()) {
				EList<ETypeParameter> eTypeParameters = eGenericSuperType
						.getEClassifier().getETypeParameters();
				int index = eTypeParameters.indexOf(eTypeParameter);
				if (index != -1
						&& eGenericSuperType.getETypeArguments().size() > index) {
					return getTypeArgument(eClass, eGenericSuperType
							.getETypeArguments().get(index));
				}
			}
			return null;
		} else {
			return eGenericType.getEClassifier();
		}
	}

}
