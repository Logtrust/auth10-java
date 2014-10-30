package com.auth10.federation;

public interface IFederatedConfiguration {

    public abstract String getStsUrl();

    public abstract String getStsFriendlyName();

    public abstract String getThumbprint();

    public abstract String getRealm();

    public abstract String getReply();

    public abstract String[] getTrustedIssuers();

    public abstract String[] getAudienceUris();

    public abstract Boolean getEnableManualRedirect();

}