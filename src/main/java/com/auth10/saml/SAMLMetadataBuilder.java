package com.auth10.saml;

import java.io.File;

import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.parse.BasicParserPool;

/**
 * SAMLMetadataBuilder, builds a SAMLMetaData Object using the IDP metadata File
 * Provided.
 * 
 * @author joaquindiez
 */
public class SAMLMetadataBuilder {

  public static SAMLMetaData build(String fqdn, File idpmetadataFile)
      throws ConfigurationException, MetadataProviderException {
    SAMLMetaData ret = null;

    DefaultBootstrap.bootstrap();
    FilesystemMetadataProvider provider = new FilesystemMetadataProvider(
        idpmetadataFile);

    provider.setRequireValidMetadata(true);
    provider.setParserPool(new BasicParserPool());
    provider.initialize();

    EntityDescriptor idpEntityDescriptor = provider.getEntityDescriptor(fqdn);

    ret = new SAMLMetaData(idpEntityDescriptor);

    return ret;
  }
}
