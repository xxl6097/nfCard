package com.sinpo.xnfc.model;

/**
 * Created by uuxia-mac on 15/10/26.
 */
public class TagModel {
    private String name;
    private String tag;
    private String save;
    private String time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String isSave() {
        return save;
    }

    public void setSave(String save) {
        this.save = save;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return name + "=" + tag + "=" + save +"=" + time+"\n";
    }

    public String toStrings(){
        return tag + "=" + save +"=" + time;
    }
}
