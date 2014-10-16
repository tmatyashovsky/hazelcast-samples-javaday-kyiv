package com.lohika.morning.hazelcast.presentation.controller;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

import javax.annotation.Resource;

import com.hazelcast.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lohika.morning.hazelcast.presentation.task.HazelcastSimpleTask;
import com.lohika.morning.hazelcast.presentation.task.HazelcastSumTask;

/**
 * Controller that handles requests related to Hazelcast demo as in-memory data grid.
 *
 * @author taras.matyashovsky
 */
@RestController
public class HazelcastExecutorController {

    private final Logger logger = LoggerFactory.getLogger(HazelcastExecutorController.class);
    private Random random = new Random();

    @Resource
    private HazelcastInstance hazelcastInstance;

    @Resource(name = "executorService")
    private IExecutorService executorService;

    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    ResponseEntity<String> execute() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            logger.info("Producing task {}", i);
            this.executorService.execute(new HazelcastSimpleTask(String.valueOf(i)));
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "/sum/{count}", method = RequestMethod.GET)
    ResponseEntity<String> sum(@PathVariable int count) throws InterruptedException, ExecutionException {
        IMap<String, Double> cache = this.hazelcastInstance.getMap("sumDistributedCache");
        cache.destroy();
        cache = this.hazelcastInstance.getMap("sumDistributedCache");

        for (int i = 0; i < count; i++) {
            cache.put(UUID.randomUUID().toString(), random.nextDouble() * random.nextInt(10));
        }

        final long startTime = System.currentTimeMillis();

        MultiExecutionCallback callback = createExecutionCallback(startTime);

        this.executorService.submitToAllMembers(new HazelcastSumTask(), callback);

        return new ResponseEntity<String>(HttpStatus.OK);
    }

    private MultiExecutionCallback createExecutionCallback(final long startTime) {
        return new MultiExecutionCallback() {

            @Override
            public void onResponse(com.hazelcast.core.Member member, Object value) {
                logger.info("Member {} has responded with the result {}", member, value);
            }

            @Override
            public void onComplete(Map<Member, Object> values) {
                logger.info("All members have responded with the results, calculating ...");

                double sum = 0;

                for (Map.Entry<Member, Object> entry : values.entrySet()) {
                    sum += (Double) entry.getValue();
                }

                logger.info("Final result is {}", sum);

                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;

                logger.info("Elapsed time with {} member(s) is {} ms", values.size(), elapsedTime);
            }
        };
    }

}
