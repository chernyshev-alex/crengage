package com.acme.crx.crengage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BatchJob implements Serializable {

    private final List<String> tasks;
    private final List<String> failed = new ArrayList<>();
    private int   status = -1;
    
    public BatchJob(List<String> tasks) {
        this.tasks = tasks; 
    }
    
    public int getStatus() {
        return status;
    }

    /**
     * Client calls this method with service callback
     * Job prepares call and use this service
     * 
     * @param service - enterprise  service
     * @return self
     */
    public BatchJob executeWithService(IService service)  {
        for (Iterator<String> iterator = tasks.iterator(); iterator.hasNext();) {
            String next = iterator.next();
            // execute tasks here
            // call enterprise service : email or sms
            String result = service.callWith(configureForcall(service));
            if (!IService.STATUS_OK.equalsIgnoreCase(result)) {
                failed.add(next);
                tasks.remove(next);
            }
        }
        status = 0;  // some abstract status
        return this;
    }
    
    // stub 
    protected Map configureForcall(IService service) {
        if (service.getServiceTag() == IService.SERVICE_TYPE_EMAIL) {
            return Collections.EMPTY_MAP;
        } else if (service.getServiceTag() == IService.SERVICE_TYPE_SMS) {
            return Collections.EMPTY_MAP;
        }
        throw new UnsupportedOperationException("unknown service " + service.getServiceTag());
    }
    
}
