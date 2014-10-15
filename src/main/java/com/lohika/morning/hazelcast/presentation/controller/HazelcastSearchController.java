package com.lohika.morning.hazelcast.presentation.controller;

import java.util.Set;

import javax.annotation.Resource;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lohika.morning.hazelcast.presentation.domain.Person;

import static com.hazelcast.query.Predicates.like;

/**
 * Controller that handles requests related to Hazelcast demo as in-memory data grid.
 *
 * @author taras.matyashovsky
 */
@RestController
public class HazelcastSearchController {

    @Resource
    private HazelcastInstance hazelcastInstance;

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    ResponseEntity<String> fillPersonsMap() {
        IMap<String, Person> persons = hazelcastInstance.getMap("personsMap");

        persons.put("taras", new Person("matyashovsky"));
        persons.put("izzet", new Person("mustafaiev"));
        persons.put("vladimir", new Person("tsukur"));
        persons.put("oleg", new Person("tsal-tsalko"));
        persons.put("mikalai", new Person("alimenkou"));
        persons.put("baruch", new Person("sadogursky"));
        persons.put("anatoliy", new Person("sokolenko"));
        persons.put("evgeny", new Person("borisov"));
        persons.put("tomasz", new Person("borek"));
        persons.put("andriy", new Person("rodionov"));
        persons.put("nikolas", new Person("frankel"));
        persons.put("alexey", new Person("tokar"));

        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/search/{startsWith}", method = RequestMethod.GET)
    ResponseEntity<Set<Person>> searchPersonsLike(@PathVariable String startsWith) {
        IMap<String, Person> persons = hazelcastInstance.getMap("personsMap");

        Predicate likePredicate = like("name", startsWith + "%");
        Set<Person> matchingValues = (Set<Person>) persons.values(likePredicate);

        return new ResponseEntity<Set<Person>>(matchingValues, HttpStatus.OK);
    }

}
