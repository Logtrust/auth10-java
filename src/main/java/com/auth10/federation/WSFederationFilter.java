package com.auth10.federation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class WSFederationFilter implements Filter {

    private static final String PRINCIPAL_SESSION_VARIABLE = "FederatedPrincipal";

    private String loginPage;
    private String loginOut;
    private String excludedUrlsRegex;

    public void init(FilterConfig config) throws ServletException {
        this.loginPage = config.getInitParameter("login-page-url");
        this.loginOut = config.getInitParameter("loginout-page-url");
        this.excludedUrlsRegex = config.getInitParameter("exclude-urls-regex");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                    ServletException {

        FederatedPrincipal principal = null;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // is the request is a token?
        if (this.isSignInResponse(httpRequest)) {
            principal = this.authenticateWithToken(httpRequest, httpResponse);
            this.writeSessionToken(httpRequest, principal);
            this.redirectToOriginalUrl(httpRequest, httpResponse);
        }
        
        // is the request is a token?
        if (this.isSignOutRequest(httpRequest)) {
           logOut(httpRequest, httpResponse);
           return;
        }

        // is principal in session?
        if (principal == null && this.sessionTokenExists(httpRequest)) {
            principal = this.authenticateWithSessionToken(httpRequest, httpResponse);
        }

        // if not authenticated at this point, redirect to login page
        boolean excludedUrl = httpRequest.getRequestURL().toString().contains(this.loginPage)
                        || (this.excludedUrlsRegex != null && !this.excludedUrlsRegex.isEmpty() && Pattern
                                        .compile(this.excludedUrlsRegex)
                                        .matcher(httpRequest.getRequestURL().toString()).find());        
        
       

        if (!excludedUrl && principal == null) {
            
            if (this.isRedirectoLoginInRequest(httpRequest)){
                this.redirectToIdentityProvider(httpRequest, httpResponse);
                return;
            }
            
            if (!FederatedConfiguration.getInstance(httpRequest).getEnableManualRedirect()) {
                this.redirectToIdentityProvider(httpRequest, httpResponse);
            } else {
                this.redirectToLoginPage(httpRequest, httpResponse);
            }
            return;
        }

        chain.doFilter(new FederatedHttpServletRequest(httpRequest, principal), response);
    }

    protected void redirectToLoginPage(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String encodedReturnUrl = URLUTF8Encoder.encode(getRequestPathAndQuery(httpRequest));
        String redirect = this.loginPage + "?returnUrl=" + encodedReturnUrl;
        httpResponse.setHeader("Location", redirect);
        httpResponse.setStatus(302);
    }
    
 
    protected void redirectToIdentityProvider(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String wctx = getRequestPathAndQuery(httpRequest);
        String redirect = FederatedLoginManager.getFederatedLoginUrl(httpRequest,wctx);

        httpResponse.setHeader("Location", redirect);
        httpResponse.setStatus(302);
    }

    protected void redirectToOriginalUrl(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String wctx = httpRequest.getParameter("wctx");
        if (wctx != null) {
            httpResponse.setHeader("Location", wctx);
            httpResponse.setStatus(302);
        }
    }

    
    protected Boolean isRedirectoLoginInRequest(HttpServletRequest request) {
        String _wa = request.getParameter("exwa");
        if (request.getMethod().equals("GET") && (_wa != null && _wa.equals("login")) ) {
            return true;
        }

        return false;
    }
    
    
    protected Boolean isSignInResponse(HttpServletRequest request) {
        if (request.getMethod().equals("POST") && request.getParameter("wa").equals("wsignin1.0")
                        && request.getParameter("wresult") != null) {
            return true;
        }

        return false;
    }
    
    
    protected Boolean isSignOutRequest(HttpServletRequest request) {
        String _wa = request.getParameter("wa");
        if (request.getMethod().equals("GET") && (_wa != null && _wa.equals("wsignoutcleanup1.0")) ) {
            return true;
        }

        return false;
    }
    
    private void logOut(HttpServletRequest request, HttpServletResponse httpResponse){
        try { 
            
            InputStream is = FederatedConfiguration.class.getResourceAsStream("/federation.properties");
            java.util.Properties props = new java.util.Properties(); 
            props.load(is);
            String className = props.getProperty(FederatedConfiguration.LOGOUT_CLASS, null);
            if (className != null){            
                IFederatedLogout _logoutClass = (IFederatedLogout)Class.forName(className).newInstance();               
                if (_logoutClass!= null){
                    _logoutClass.logout(request);
                    InputStream in = this.getClass().getResourceAsStream("logout_icon.png");                
                    ServletOutputStream out = httpResponse.getOutputStream();
                    byte[] buffer = new byte[1024];
                    int len = in.read(buffer);
                    while (len != -1) {
                        out.write(buffer, 0, len);
                        len = in.read(buffer);
                    }
                    out.close();
                    in.close();                
                }
            }                           
           
        } catch (Exception e) {
            System.err.println("Error Cerrando sesion Federacion " +  e);//cambiarlo a usar log4j o simil.
        }
    }
    

    protected Boolean sessionTokenExists(HttpServletRequest request) {
        // this could use signed cookies instead of sessions
        return request.getSession().getAttribute(PRINCIPAL_SESSION_VARIABLE) != null;
    }

    protected FederatedPrincipal authenticateWithSessionToken(HttpServletRequest request, HttpServletResponse response)
                    throws IOException {
        return (FederatedPrincipal) request.getSession().getAttribute(PRINCIPAL_SESSION_VARIABLE);
    }

    protected void writeSessionToken(HttpServletRequest request, FederatedPrincipal principal) throws IOException {
        request.getSession().setAttribute(PRINCIPAL_SESSION_VARIABLE, principal);
    }

    protected FederatedPrincipal authenticateWithToken(HttpServletRequest request, HttpServletResponse response)
                    throws IOException {
        String token = request.getParameter("wresult").toString();

        if (token == null) {
            response.sendError(400, "You were supposed to send a wresult parameter with a token");
        }

        FederatedLoginManager loginManager = FederatedLoginManager.fromRequest(request, null);

        try {
            FederatedPrincipal principal = loginManager.authenticate(token, response);
            return principal;
        } catch (FederationException e) {
            response.sendError(500, "Oops and error occurred validating the token.");
        }

        return null;
    }

    public void destroy() {
    }

    private static String getRequestPathAndQuery(HttpServletRequest req) {
        String reqUri = req.getRequestURI().toString();
        String queryString = req.getQueryString();
        if (queryString != null) {
            reqUri += "?" + queryString;
        }

        return reqUri;
    }
}
