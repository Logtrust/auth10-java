package com.auth10.federation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.impl.EntitiesDescriptorImpl;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.auth10.saml.SAMLMetaData;
import com.auth10.saml.SAMLUtil;

public class IDPTest {

  static String inCommonMDFile = "/Users/joaquindiez/Downloads/GoogleIDPMetadata-freesoullabs.com.xml";
  String redirectionUrl = "https://google.asdad/";
  String relayState = "token";

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testCreateSALMRequest() throws ClassNotFoundException, InstantiationException, ClassCastException {

    EntityDescriptor idpEntityDescriptor = this.buildIdpEntityDescriptor();
    SAMLMetaData samlMetadata = new SAMLMetaData(idpEntityDescriptor);
    try {
      AuthnRequest authRequest = this
          .generateAuthnRequest(samlMetadata);
      System.out.println(authRequest.getAssertionConsumerServiceURL());

      /*
      DOMImplementationRegistry registry = DOMImplementationRegistry
          .newInstance();

      DOMImplementationLS impl = (DOMImplementationLS) registry
          .getDOMImplementation("LS");
      LSSerializer writer = impl.createLSSerializer();
      generateAuthnRequest.releaseDOM();
      String samlRequestStr = writer
          .writeToString(generateAuthnRequest.getDOM());
*/
   // Now we must build our representation to put into the html form to be submitted to the idp
      Marshaller marshaller = org.opensaml.Configuration.getMarshallerFactory().getMarshaller(authRequest);
      org.w3c.dom.Element authDOM = marshaller.marshall(authRequest);
      StringWriter rspWrt = new StringWriter();
      XMLHelper.writeNode(authDOM, rspWrt);
      String messageXML = rspWrt.toString();
      
      System.out.println(messageXML);
      
      
    //delete this area
      //String temp = "<samlp:AuthnRequest  xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"  ID=\"71069679271a7cf36e0e02e48084798ea844fce23f\" Version=\"2.0\" IssueInstant=\"2010-03-09T10:46:23Z\" ForceAuthn=\"false\" IsPassive=\"false\" ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" AssertionConsumerServiceURL=\"http://saml20sp.abilityweb.us/spdbg/sp.php\"><saml:Issuer xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">http://saml20sp.abilityweb.us</saml:Issuer><samlp:NameIDPolicy  xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:persistent\" SPNameQualifier=\"http://saml20sp.abilityweb.us\" AllowCreate=\"true\"></samlp:NameIDPolicy><samlp:RequestedAuthnContext xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" Comparison=\"exact\"><saml:AuthnContextClassRef xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport</saml:AuthnContextClassRef></samlp:RequestedAuthnContext></samlp:AuthnRequest>";
      Deflater deflater = new Deflater(Deflater.DEFLATED, true);
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); 
      DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
      deflaterOutputStream.write(messageXML.getBytes()); 
      deflaterOutputStream.close();
      String samlResponse = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
      String outputString = new String(byteArrayOutputStream.toByteArray());
      //System.out.println("Compressed String: " + outputString);
      samlResponse = URLEncoder.encode(samlResponse);
      
      String actionURL = this.redirectionUrl;
      System.out.println("Converted AuthRequest: " + messageXML);
      System.out.println("samlResponse: " + samlResponse);
      //messageXML = messageXML.replace("<", "&lt;");
      //messageXML = messageXML.replace(">", "&gt;");
      
      String url = actionURL + "?SAMLRequest=" + samlResponse + "&RelayState=" + this.relayState;
      System.out.println(url);

      
    } catch (IllegalArgumentException | SecurityException
        | IllegalAccessException | NoSuchFieldException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MarshallingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  // @Test
  public EntityDescriptor buildIdpEntityDescriptor() {
    FilesystemMetadataProvider idpMetaDataProvider;
    try {
      // Initialize the library
      DefaultBootstrap.bootstrap();
      idpMetaDataProvider = new FilesystemMetadataProvider(
          new File(inCommonMDFile));
      
      idpMetaDataProvider.setRequireValidMetadata(true);
      idpMetaDataProvider.setParserPool(new BasicParserPool());
      idpMetaDataProvider.initialize();
      EntityDescriptor idpEntityDescriptor = idpMetaDataProvider
          .getEntityDescriptor(
              "https://accounts.google.com/o/saml2?idpid=C012wwv4q");

      // SSOServices
      SingleSignOnService redirectEndpoint = null;
      for (SingleSignOnService sss : idpEntityDescriptor
          .getIDPSSODescriptor(SAMLConstants.SAML20P_NS)
          .getSingleSignOnServices()) {
        if (sss.getBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
          redirectEndpoint = sss;
          System.out.println(
              "SAML2_REDIRECT_BINDING_URI: " + redirectEndpoint.getLocation());
          System.out.println("SAML2_REDIRECT_BINDING_URI responselocation: "
              + redirectEndpoint.getResponseLocation());
        }
      }

      String artifactResolutionServiceURL = "";
      // ArtifactResolutionService
      for (ArtifactResolutionService ars : idpEntityDescriptor
          .getIDPSSODescriptor(SAMLConstants.SAML20P_NS)
          .getArtifactResolutionServices()) {
        if (ars.getBinding().equals(SAMLConstants.SAML2_SOAP11_BINDING_URI)) {
          artifactResolutionServiceURL = ars.getLocation();
        }
        System.out.println(artifactResolutionServiceURL);
      }

      // ArtifactResolutionService
      for (SingleSignOnService ars : idpEntityDescriptor
          .getIDPSSODescriptor(SAMLConstants.SAML20P_NS)
          .getSingleSignOnServices()) {
        if (ars.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI)) {
          artifactResolutionServiceURL = ars.getLocation();
          System.out.println(
              "SAML2_POST_BINDING_URI: " + artifactResolutionServiceURL);
        }

      }

      // ArtifactResolutionService
      for (NameIDFormat ars : idpEntityDescriptor
          .getIDPSSODescriptor(SAMLConstants.SAML20P_NS).getNameIDFormats()) {
        System.out.println("NameIDFormat : " + ars.getFormat());
      }

      return idpEntityDescriptor;
    } catch (MetadataProviderException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;

  }

  // @Test
  public void test() throws ConfigurationException, FileNotFoundException,
      XMLParserException, UnmarshallingException {

    // Get parser pool manager
    BasicParserPool ppMgr = new BasicParserPool();
    ppMgr.setNamespaceAware(true);

    // Parse metadata file
    InputStream in = new FileInputStream(new File(inCommonMDFile));
    Document inCommonMDDoc = ppMgr.parse(in);
    Element metadataRoot = inCommonMDDoc.getDocumentElement();

    // Get apropriate unmarshaller
    UnmarshallerFactory unmarshallerFactory = Configuration
        .getUnmarshallerFactory();
    Unmarshaller unmarshaller = unmarshallerFactory
        .getUnmarshaller(metadataRoot);

    // Unmarshall using the document root element, an EntitiesDescriptor in this
    // case
    EntitiesDescriptorImpl inCommonMD = (EntitiesDescriptorImpl) unmarshaller
        .unmarshall(metadataRoot);

    System.out.println(inCommonMD.getID());
  }

  private String getDeployURL() {
    return "http://freesoullbas.logtrust.com/auth/saml";
  }

  private String getSPEntityId() {
    return "logtrust";
  }

  private AuthnRequest generateAuthnRequest(final SAMLMetaData metaData)
      throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {

    AuthnRequest authnRequest = SAMLUtil
        .buildSAMLObjectWithDefaultName(AuthnRequest.class);

    authnRequest.setForceAuthn(true);
    authnRequest.setIsPassive(false);
    authnRequest.setIssueInstant(new DateTime());
    for (SingleSignOnService sss : metaData.getIdpEntityDescriptor()
        .getIDPSSODescriptor(SAMLConstants.SAML20P_NS)
        .getSingleSignOnServices()) {
      if (sss.getBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) {
        authnRequest.setDestination(sss.getLocation());
      }
    }
    authnRequest.setProtocolBinding(SAMLConstants.SAML2_ARTIFACT_BINDING_URI);

    String deployURL = getDeployURL();
    if (deployURL.charAt(deployURL.length() - 1) == '/') {
      deployURL = deployURL.substring(0, deployURL.length() - 1);
    }
    authnRequest
        .setAssertionConsumerServiceURL(deployURL + SAMLMetaData.CONSUMER_PATH);

    authnRequest.setID(SAMLUtil.getSecureRandomIdentifier());

    Issuer issuer = SAMLUtil.buildSAMLObjectWithDefaultName(Issuer.class);
    issuer.setValue(getSPEntityId());
    authnRequest.setIssuer(issuer);

    NameIDPolicy nameIDPolicy = SAMLUtil
        .buildSAMLObjectWithDefaultName(NameIDPolicy.class);
    nameIDPolicy.setSPNameQualifier(getSPEntityId());
    nameIDPolicy.setAllowCreate(true);
    nameIDPolicy
        .setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");

    authnRequest.setNameIDPolicy(nameIDPolicy);

    RequestedAuthnContext requestedAuthnContext = SAMLUtil
        .buildSAMLObjectWithDefaultName(RequestedAuthnContext.class);
    requestedAuthnContext
        .setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);

    AuthnContextClassRef authnContextClassRef = SAMLUtil
        .buildSAMLObjectWithDefaultName(AuthnContextClassRef.class);
    authnContextClassRef.setAuthnContextClassRef(
        "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

    requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);
    authnRequest.setRequestedAuthnContext(requestedAuthnContext);

    return authnRequest;
  }

}
