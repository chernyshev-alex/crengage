package com.acme.crx.crengage;

import java.util.Map;

/**
 * Representation of the fat enterprise abstract service, aka  email or sms
 */
public interface IService {

    // Just for example !!! 
    
    // statuses
    public static final String STATUS_OK        = "OK";
    public static final String STATUS_FAILED    = "FAIL";
    
    // service types
    public static final String SERVICE_TYPE_EMAIL     =  "MAIL";
    public static final String SERVICE_TYPE_SMS       =  "SMS";
    
    /**
     * Call abstract service 
     * 
     * @param serviceCallParams
     * @return status 
     */
    public String callWith(Map<String, String> serviceCallParams);
    
    /**
     * @return  well-known service name
     */
    public String getServiceTag();
    
    public static IService getEmailServiceProvider() {
       return new IService() {
            @Override
            public String callWith(Map<String, String> serviceCallParams) {
                return STATUS_OK;
            }

           @Override
           public String getServiceTag() {
               return IService.SERVICE_TYPE_EMAIL;
           }
        };
    }
    
    public static IService getSmsServiceProvider() {
       return new IService() {
            @Override
            public String callWith(Map<String, String> serviceCallParams) {
                return STATUS_FAILED;
            }

           @Override
           public String getServiceTag() {
               return IService.SERVICE_TYPE_SMS;
           }
        };
    }
    
}
