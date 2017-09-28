package com.acme.crx.crengage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrossEngageApp  {

    /**
     * Execute workflow  :  validateEmail, sendEmail, sendSms 
     * 
     * SendEmail and SendSms will operate on jobs batches
     * 
     * @param source - stream of user's data, mail and/or sms
     * @param batchSize - job size
     * @throws InterruptedException 
     */
    public void execute(Stream<String> source, int batchSize) throws InterruptedException {
        
        // only valid emails passed, but if we want to sent SMS also, then no email filtering
        Stream<String> filteredSource = source.parallel().filter(getMailValidator());
        
        // split to chunks with batchSize
        Stream<List<String>> bs = BatchSplitIterator.toBatchStream(filteredSource, batchSize);
        
        // prepare jobs b
        List<BatchJob> jobs = collectBatchJobs(bs);

        // execute jobs on threads pool
        ExecutorService pool = Executors.newWorkStealingPool();
        //List<BatchJob> result = executeOnPool(jobs, pool);
        List<BatchJob> result = executeOnPool(jobs, pool, IService.getEmailServiceProvider());
        
        // or if configure SMS  send SMS
        List<BatchJob> result2 = executeOnPool(jobs, pool, IService.getSmsServiceProvider());
        
        pool.awaitTermination(2, TimeUnit.SECONDS);
    }

    public static List<BatchJob> executeOnPool(List<BatchJob> jobs, ExecutorService pool, IService service) {
        jobs.forEach(job -> {
            CompletableFuture.supplyAsync(() -> {
                return job.executeWithService(service);
            }, pool);
        });
        return jobs;
    }
    
    /**
     * Execute one the other
     */
    public static List<BatchJob> executeOnPool(List<BatchJob> jobs, ExecutorService pool) {
        jobs.forEach(job -> {
            CompletableFuture.supplyAsync(() -> {
                return job.executeWithService(IService.getEmailServiceProvider());
            }, pool).whenCompleteAsync((smsJob, e) -> {
                        smsJob.executeWithService(IService.getSmsServiceProvider());
                     }, pool);
        });
        return jobs;
    }
    
    public static List<BatchJob> collectBatchJobs(Stream<List<String>> bs) {
        return bs.map((List<String> ls) -> new BatchJob(ls)).collect(Collectors.toList());
    }
    
    public static Predicate<String> getMailValidator() {
        return Pattern.compile(EMAIL_PATTERN).asPredicate();
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.out.println("input file name is expected");
            System.exit(-1);
        }
        
        UserRepository repository = new UserRepository(new File(args[0]));
        Stream<String> usersInfo = repository.getAllEmails().stream();

        new CrossEngageApp().execute(usersInfo, 1000);
    }

    private static final String EMAIL_PATTERN =
		"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
		+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    
}
