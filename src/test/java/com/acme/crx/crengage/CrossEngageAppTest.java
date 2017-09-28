package com.acme.crx.crengage;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.*;

public class CrossEngageAppTest {
    
    CrossEngageApp app = new CrossEngageApp();
    
     @Test
    public void testBatchSplitter() {
        int size = 1000;
        int batch_size = 100;
        Stream<List<String>> res = BatchSplitIterator.toBatchStream(getFakeSource(size), batch_size);
        assertEquals(size / batch_size, res.count());
    }
    
    @Test
    public void testCollector() {
        int size = 1000;
        int batch_size = 100;
        Stream<List<String>> res = BatchSplitIterator.toBatchStream(getFakeSource(size), batch_size);
        List<BatchJob> ls = CrossEngageApp.collectBatchJobs(res);
        assertEquals(size / batch_size, ls.size());
    }

    @Test
    public void testWasExecuted() throws InterruptedException {
        Stream<List<String>> res = BatchSplitIterator.toBatchStream(getFakeSource(100), 10);
        List<BatchJob> ls = CrossEngageApp.collectBatchJobs(res);
        List<BatchJob> result = CrossEngageApp.executeOnPool(ls, Executors.newWorkStealingPool());
        Thread.sleep(10L); // wait execution and check status
        assertEquals(0, result.get(0).getStatus());
    }
    
    private Stream<String> getFakeSource(long size) {
      return Stream.iterate(0, n -> n + 1).map(n -> n.toString()).limit(size);
    }
    
}
