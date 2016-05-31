package com.auth10.saml;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;

public class SAMLMetaData {

  public static final String CONSUMER_PATH = "";
  private EntityDescriptor idpEntityDescriptor;

  public SAMLMetaData(EntityDescriptor idpEntityDescriptor) {
    this.idpEntityDescriptor = idpEntityDescriptor;
  }

  public EntityDescriptor getIdpEntityDescriptor() {
    return idpEntityDescriptor;
  }

  public String getRedirectURL() {
    String ret = null;
    // SSOServices
    SingleSignOnService redirectEndpoint = null;
    for (SingleSignOnService sss : idpEntityDescriptor
        .getIDPSSODescriptor(SAMLConstants.SAML20P_NS)
        .getSingleSignOnServices()) {
      if (sss.getBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
        redirectEndpoint = sss;
        ret = redirectEndpoint.getLocation();

      }
    }
    return ret;
  }
  
  
  public String getCertificate() {
    String ret = null;
    // SSOServices
    for (KeyDescriptor keyDesc : this.idpEntityDescriptor
        .getIDPSSODescriptor(SAMLConstants.SAML20P_NS).getKeyDescriptors()) {
      
      if ( keyDesc.getUse().compareTo(UsageType.SIGNING) == 0){
        X509Data x509Data = keyDesc.getKeyInfo().getX509Datas().get(0);
        X509Certificate x509Certificate = x509Data.getX509Certificates().get(0);
        ret = x509Certificate.getValue();
      }
      
    }
    return ret;
  }

}
