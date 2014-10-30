//-----------------------------------------------------------------------
// <copyright file="FederatedConfiguration.java" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
//
// 
//    Copyright 2012 Microsoft Corporation
//    All rights reserved.
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//      http://www.apache.org/licenses/LICENSE-2.0
//
// THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
// EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR 
// CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
//
// See the Apache Version 2.0 License for specific language governing 
// permissions and limitations under the License.
// </copyright>
//
// <summary>
//     
//
// </summary>
//----------------------------------------------------------------------------------------------

package com.auth10.federation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public class FederatedConfiguration {

    public static final String CLASS_PROPERTY = "federation.config.loader";
   
    public static IFederatedConfiguration getInstance(HttpServletRequest request) {      
        return load(request);
    }

    private static IFederatedConfiguration load(HttpServletRequest request) {
        java.util.Properties props = new java.util.Properties();
        IFederatedConfiguration configurator = null;
        try {
            InputStream is = FederatedConfiguration.class.getResourceAsStream("/federation.properties");
            props.load(is);
            String className = props.getProperty(CLASS_PROPERTY, "com.auth10.federation.BasicFileConfiguration");
            if (!StringUtils.isEmpty(className)){
                Constructor<?> declaredConstructor = Class.forName(className).getDeclaredConstructor(HttpServletRequest.class);
                configurator =  (IFederatedConfiguration) declaredConstructor.newInstance(request);
            }

        } catch (IOException e) {
            throw new RuntimeException("Configuration could not be loaded", e);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      
        /*
        if (configurator == null){
            //Utilizamos los valores por defecto
            configurator = new BasicFileConfiguration(request);
        }
        */

        return configurator;
    }

  

}
