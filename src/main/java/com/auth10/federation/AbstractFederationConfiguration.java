package com.auth10.federation;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractFederationConfiguration implements IFederatedConfiguration {
    
    HttpServletRequest request;
    
    public AbstractFederationConfiguration(HttpServletRequest request){
        this.request = request;
    }    
   
}
