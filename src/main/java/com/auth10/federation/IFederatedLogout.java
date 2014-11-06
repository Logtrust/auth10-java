package com.auth10.federation;

import javax.servlet.http.HttpServletRequest;

public interface IFederatedLogout {
    
    public String logout(HttpServletRequest request);

}
