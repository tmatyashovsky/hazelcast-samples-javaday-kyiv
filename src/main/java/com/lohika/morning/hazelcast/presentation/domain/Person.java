package com.lohika.morning.hazelcast.presentation.domain;

import java.io.Serializable;

/**
 * Simple POJO used for distributed querying.
 *
 * @author taras.matyashovsky
 */
public class Person implements Serializable {

    private String name;

    public Person(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
