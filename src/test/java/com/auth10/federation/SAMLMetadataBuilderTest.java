package com.auth10.federation;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.ConfigurationException;

import com.auth10.saml.SAMLMetaData;
import com.auth10.saml.SAMLMetadataBuilder;

public class SAMLMetadataBuilderTest {

  @Before
  public void setUp() throws Exception {
  }


  private void testIDPGeneric(String fqdn, File idpmetadataFile)
      throws ConfigurationException, MetadataProviderException {
    SAMLMetaData build = SAMLMetadataBuilder.build(fqdn, idpmetadataFile);

    System.out.println("Redirect URL = " + build.getRedirectURL());
    System.out.println("Certificate  = " + build.getCertificate());
  }
  
  @Test
  public void testMicrosoftOffice365IDP()
      throws ConfigurationException, MetadataProviderException {
    String fqdn = "urn:federation:MicrosoftOnline";
    File idpmetadataFile = new File(
        "/Users/joaquindiez/Downloads/federationmetadata.xml");
    testIDPGeneric(fqdn, idpmetadataFile);
  }
  
  
  //@Test
  public void testGoogleIDP()
      throws ConfigurationException, MetadataProviderException {
    String fqdn = "https://accounts.google.com/o/saml2?idpid=C012wwv4q";
    File idpmetadataFile = new File(
        "/Users/joaquindiez/Downloads/GoogleIDPMetadata-freesoullabs.com.xml");
    testIDPGeneric(fqdn, idpmetadataFile);

  }


  //@Test
  public void testSsocircleIDP()
      throws ConfigurationException, MetadataProviderException {
    String fqdn = "http://idp.ssocircle.com";
    File idpmetadataFile = new File(
        "/Users/joaquindiez/Downloads/idp.ssocircle.com.xml");
    testIDPGeneric(fqdn, idpmetadataFile);
  }

}
