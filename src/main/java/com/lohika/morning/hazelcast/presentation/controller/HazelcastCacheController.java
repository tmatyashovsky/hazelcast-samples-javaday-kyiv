package com.lohika.morning.hazelcast.presentation.controller;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller that handles all requests related to Hazelcast demo as distributed cache.
 *
 * @author taras.matyashovsky
 */
@RestController
public class HazelcastCacheController {

    @Resource(name = "distributedCache")
    private ConcurrentMap<String, String> cache;

    @RequestMapping(value = "/{key}", method = RequestMethod.GET)
    ResponseEntity<String> get(@PathVariable String key) {
        String value = this.cache.get(key);

        if (value != null) {
            return new ResponseEntity<String>(value, HttpStatus.OK);
        }

        return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/{key}", method = RequestMethod.PUT)
    ResponseEntity<String> put(@PathVariable String key, @RequestBody String value) {
        this.cache.put(key, value);

        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

}
