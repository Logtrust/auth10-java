package com.auth10.federation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

public class BasicFileConfiguration extends AbstractFederationConfiguration {

    private Properties properties;

    private void load() {
        java.util.Properties props = new java.util.Properties();

        try {
            InputStream is = BasicFileConfiguration.class.getResourceAsStream("/federation.properties");
            props.load(is);
            properties = props;

        } catch (IOException e) {
            throw new RuntimeException("Configuration could not be loaded", e);
        }

    }

    public BasicFileConfiguration(HttpServletRequest request) {
        super(request);
        load();

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.auth10.federation.IFederatedConfiguration#getStsUrl()
     */
    @Override
    public String getStsUrl() {
        return this.properties.getProperty("federation.trustedissuers.issuer");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.auth10.federation.IFederatedConfiguration#getStsFriendlyName()
     */
    @Override
    public String getStsFriendlyName() {
        return this.properties.getProperty("federation.trustedissuers.friendlyname");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.auth10.federation.IFederatedConfiguration#getThumbprint()
     */
    @Override
    public String getThumbprint() {
        return this.properties.getProperty("federation.trustedissuers.thumbprint");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.auth10.federation.IFederatedConfiguration#getRealm()
     */
    @Override
    public String getRealm() {
        return this.properties.getProperty("federation.realm");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.auth10.federation.IFederatedConfiguration#getReply()
     */
    @Override
    public String getReply() {
        return this.properties.getProperty("federation.reply");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.auth10.federation.IFederatedConfiguration#getTrustedIssuers()
     */
    @Override
    public String[] getTrustedIssuers() {
        String trustedIssuers = this.properties.getProperty("federation.trustedissuers.subjectname");

        if (trustedIssuers != null)
            return trustedIssuers.split("\\|");
        else
            return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.auth10.federation.IFederatedConfiguration#getAudienceUris()
     */
    @Override
    public String[] getAudienceUris() {
        return this.properties.getProperty("federation.audienceuris").split("\\|");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.auth10.federation.IFederatedConfiguration#getEnableManualRedirect()
     */
    @Override
    public Boolean getEnableManualRedirect() {
        String manual = this.properties.getProperty("federation.enableManualRedirect");
        if (manual != null && Boolean.parseBoolean(manual)) {
            return true;
        }

        return false;
    }

}
