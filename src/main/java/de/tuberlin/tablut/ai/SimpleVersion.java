package de.tuberlin.tablut.ai;

public class SimpleVersion {
    public String name;
    public Double version;
    public Boolean isAlive;

    public SimpleVersion(String name, Double version, Boolean isAlive) {
        this.name = name;
        this.version = version;
        this.isAlive = isAlive;
    }

    @Override
    public String toString(){
        return name + " v"+version+" is"+(isAlive?"":" not")+" alive";
    }
}
