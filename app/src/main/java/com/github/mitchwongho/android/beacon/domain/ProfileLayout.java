package com.github.mitchwongho.android.beacon.domain;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 *
 */
public class ProfileLayout extends RealmObject {

    private Long id;
    @Required
    @PrimaryKey
    private String name;
    @Required
    private String layout;

    public ProfileLayout() {
        //required empty construtor
    }

    public ProfileLayout(String layout, String name) {
        this.layout = layout;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
