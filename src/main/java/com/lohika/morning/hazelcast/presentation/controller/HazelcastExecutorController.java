package com.lohika.morning.hazelcast.presentation.controller;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

import javax.annotation.Resource;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiExecutionCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lohika.morning.hazelcast.presentation.task.HazelcastAverageTask;
import com.lohika.morning.hazelcast.presentation.task.HazelcastSimpleTask;

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

    @RequestMapping(value = "/average/{count}", method = RequestMethod.GET)
    ResponseEntity<String> average(@PathVariable int count) throws InterruptedException, ExecutionException {
        IMap<String, Double> cache = this.hazelcastInstance.getMap("averageDistributedCache");
        cache.destroy();
        cache = this.hazelcastInstance.getMap("averageDistributedCache");

        for (int i = 0; i < count; i++) {
            cache.put(UUID.randomUUID().toString(), random.nextDouble() * random.nextInt(10));
        }

        final long startTime = System.currentTimeMillis();

        MultiExecutionCallback callback =
            new MultiExecutionCallback() {

                @Override
                public void onResponse(com.hazelcast.core.Member member, Object value) {
                    logger.info("Member {} has responded with the result {}", member, value);
                }

                @Override
                public void onComplete(Map<com.hazelcast.core.Member, Object> values) {
                    logger.info("All members have responded with the results, calculating ...");

                    double sum = 0;

                    for (Map.Entry<com.hazelcast.core.Member, Object> entry : values.entrySet()) {
                        sum += (Double) entry.getValue();
                    }

                    logger.info("Final result is {}", sum / values.size());

                    long stopTime = System.currentTimeMillis();
                    long elapsedTime = stopTime - startTime;

                    logger.info("Elapsed time with {} member(s) is {} ms", values.size(), elapsedTime);
                }
            };

        this.executorService.submitToAllMembers(new HazelcastAverageTask(), callback);

        return new ResponseEntity<String>(HttpStatus.OK);
    }

}
