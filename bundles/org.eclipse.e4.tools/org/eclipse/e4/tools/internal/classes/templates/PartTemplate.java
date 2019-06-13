package org.eclipse.e4.tools.internal.classes.templates;

import org.eclipse.e4.tools.internal.classes.NewPartClassWizard.PartClass;

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
  protected final String TEXT_1 = " " + NL + "package ";
  protected final String TEXT_2 = ";" + NL + "" + NL + "import javax.inject.Inject;";
  protected final String TEXT_3 = NL + "import javax.annotation.PostConstruct;";
  protected final String TEXT_4 = NL + "import javax.annotation.PreDestroy;";
  protected final String TEXT_5 = NL + "import org.eclipse.e4.ui.di.Focus;";
  protected final String TEXT_6 = NL + "import org.eclipse.e4.ui.di.Persist;";
  protected final String TEXT_7 = NL + NL + "public class ";
  protected final String TEXT_8 = " {" + NL + "\t@Inject" + NL + "\tpublic ";
  protected final String TEXT_9 = "() {" + NL + "\t\t//TODO Your code here" + NL + "\t}" + NL + "\t";
  protected final String TEXT_10 = NL + "\t@PostConstruct" + NL + "\tpublic void ";
  protected final String TEXT_11 = "() {" + NL + "\t\t//TODO Your code here" + NL + "\t}" + NL + "\t";
  protected final String TEXT_12 = NL + "\t";
  protected final String TEXT_13 = NL + "\t@PreDestroy" + NL + "\tpublic void ";
  protected final String TEXT_14 = "() {" + NL + "\t\t//TODO Your code here" + NL + "\t}" + NL + "\t";
  protected final String TEXT_15 = NL + "\t";
  protected final String TEXT_16 = NL + "\t@Focus" + NL + "\tpublic void ";
  protected final String TEXT_17 = "() {" + NL + "\t\t//TODO Your code here" + NL + "\t}" + NL + "\t";
  protected final String TEXT_18 = NL + "\t";
  protected final String TEXT_19 = NL + "\t@Persist" + NL + "\tpublic void ";
  protected final String TEXT_20 = "() {" + NL + "\t\t//TODO Your code here" + NL + "\t}" + NL + "\t";
  protected final String TEXT_21 = NL + "}";

  public String generate(Object argument)
  {
    final StringBuilder stringBuffer = new StringBuilder();
     PartClass domainClass = (PartClass)argument; 
    stringBuffer.append(TEXT_1);
    stringBuffer.append( domainClass.getPackageFragment().getElementName() );
    stringBuffer.append(TEXT_2);
     if( domainClass.isUsePostConstruct() ) { 
    stringBuffer.append(TEXT_3);
     } 
     if( domainClass.isUsePredestroy() ) { 
    stringBuffer.append(TEXT_4);
     } 
     if( domainClass.isUseFocus() ) { 
    stringBuffer.append(TEXT_5);
     } 
     if( domainClass.isUsePersist() ) { 
    stringBuffer.append(TEXT_6);
     } 
    stringBuffer.append(TEXT_7);
    stringBuffer.append( domainClass.getName() );
    stringBuffer.append(TEXT_8);
    stringBuffer.append( domainClass.getName() );
    stringBuffer.append(TEXT_9);
     if( domainClass.isUsePostConstruct() ) { 
    stringBuffer.append(TEXT_10);
    stringBuffer.append( domainClass.getPostConstructMethodName()  );
    stringBuffer.append(TEXT_11);
     } 
    stringBuffer.append(TEXT_12);
     if( domainClass.isUsePredestroy() ) { 
    stringBuffer.append(TEXT_13);
    stringBuffer.append( domainClass.getPreDestroyMethodName()  );
    stringBuffer.append(TEXT_14);
     } 
    stringBuffer.append(TEXT_15);
     if( domainClass.isUseFocus() ) { 
    stringBuffer.append(TEXT_16);
    stringBuffer.append( domainClass.getFocusMethodName() );
    stringBuffer.append(TEXT_17);
     } 
    stringBuffer.append(TEXT_18);
     if( domainClass.isUsePersist() ) { 
    stringBuffer.append(TEXT_19);
    stringBuffer.append( domainClass.getPersistMethodName() );
    stringBuffer.append(TEXT_20);
     } 
    stringBuffer.append(TEXT_21);
    return stringBuffer.toString();
  }
}
