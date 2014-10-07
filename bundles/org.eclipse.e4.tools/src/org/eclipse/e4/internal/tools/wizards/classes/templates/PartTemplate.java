package org.eclipse.e4.internal.tools.wizards.classes.templates;

import org.eclipse.e4.internal.tools.wizards.classes.NewPartClassWizard.PartClass;
@SuppressWarnings("nls")
public class PartTemplate
{
  protected static String nl;
  public static synchronized PartTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    PartTemplate result = new PartTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = " ";
  protected final String TEXT_2 = NL + "package ";
  protected final String TEXT_3 = ";";
  protected final String TEXT_4 = NL + NL + "import javax.inject.Inject;";
  protected final String TEXT_5 = NL + "import javax.annotation.PostConstruct;" + NL + "import org.eclipse.swt.widgets.Composite;";
  protected final String TEXT_6 = NL + "import javax.annotation.PreDestroy;";
  protected final String TEXT_7 = NL + "import org.eclipse.e4.ui.di.Focus;";
  protected final String TEXT_8 = NL + "import org.eclipse.e4.ui.di.Persist;";
  protected final String TEXT_9 = NL + NL + "public class ";
  protected final String TEXT_10 = " {" + NL + "\t@Inject" + NL + "\tpublic ";
  protected final String TEXT_11 = "() {" + NL + "\t\t" + NL + "\t}" + NL + "\t";
  protected final String TEXT_12 = NL + "\t@PostConstruct" + NL + "\tpublic void ";
  protected final String TEXT_13 = "(Composite parent) {" + NL + "\t\t" + NL + "\t}" + NL + "\t";
  protected final String TEXT_14 = NL + "\t";
  protected final String TEXT_15 = NL + "\t@PreDestroy" + NL + "\tpublic void ";
  protected final String TEXT_16 = "() {" + NL + "\t\t" + NL + "\t}" + NL + "\t";
  protected final String TEXT_17 = NL + "\t";
  protected final String TEXT_18 = NL + "\t@Focus" + NL + "\tpublic void ";
  protected final String TEXT_19 = "() {" + NL + "\t\t" + NL + "\t}" + NL + "\t";
  protected final String TEXT_20 = NL + "\t";
  protected final String TEXT_21 = NL + "\t@Persist" + NL + "\tpublic void ";
  protected final String TEXT_22 = "() {" + NL + "\t\t" + NL + "\t}" + NL + "\t";
  protected final String TEXT_23 = NL + "}";

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
     PartClass domainClass = (PartClass)argument; 
    stringBuffer.append(TEXT_1);
     if( domainClass.getPackageFragment() != null && domainClass.getPackageFragment().getElementName().trim().length() > 0 ) { 
    stringBuffer.append(TEXT_2);
    stringBuffer.append( domainClass.getPackageFragment().getElementName() );
    stringBuffer.append(TEXT_3);
     } 
    stringBuffer.append(TEXT_4);
     if( domainClass.isUsePostConstruct() ) { 
    stringBuffer.append(TEXT_5);
     } 
     if( domainClass.isUsePredestroy() ) { 
    stringBuffer.append(TEXT_6);
     } 
     if( domainClass.isUseFocus() ) { 
    stringBuffer.append(TEXT_7);
     } 
     if( domainClass.isUsePersist() ) { 
    stringBuffer.append(TEXT_8);
     } 
    stringBuffer.append(TEXT_9);
    stringBuffer.append( domainClass.getName() );
    stringBuffer.append(TEXT_10);
    stringBuffer.append( domainClass.getName() );
    stringBuffer.append(TEXT_11);
     if( domainClass.isUsePostConstruct() ) { 
    stringBuffer.append(TEXT_12);
    stringBuffer.append( domainClass.getPostConstructMethodName()  );
    stringBuffer.append(TEXT_13);
     } 
    stringBuffer.append(TEXT_14);
     if( domainClass.isUsePredestroy() ) { 
    stringBuffer.append(TEXT_15);
    stringBuffer.append( domainClass.getPreDestroyMethodName()  );
    stringBuffer.append(TEXT_16);
     } 
    stringBuffer.append(TEXT_17);
     if( domainClass.isUseFocus() ) { 
    stringBuffer.append(TEXT_18);
    stringBuffer.append( domainClass.getFocusMethodName() );
    stringBuffer.append(TEXT_19);
     } 
    stringBuffer.append(TEXT_20);
     if( domainClass.isUsePersist() ) { 
    stringBuffer.append(TEXT_21);
    stringBuffer.append( domainClass.getPersistMethodName() );
    stringBuffer.append(TEXT_22);
     } 
    stringBuffer.append(TEXT_23);
    return stringBuffer.toString();
  }
}
