package com.auth10.saml;

import java.util.Random;

import javax.xml.namespace.QName;

import org.apache.commons.lang.RandomStringUtils;
import org.opensaml.Configuration;
import org.opensaml.xml.XMLObjectBuilderFactory;

public class SAMLUtil {
  
  
  public static <T> T buildSAMLObjectWithDefaultName(final Class<T> clazz) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
    
    QName defaultElementName = (QName)clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
    T object = (T)builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
    
   return object;
   }
  
  public static String getSecureRandomIdentifier(){
    return RandomStringUtils.randomAscii(10);
  }


}
