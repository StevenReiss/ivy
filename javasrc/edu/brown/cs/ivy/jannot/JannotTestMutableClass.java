package edu.brown.cs.ivy.jannot;

@JannotTestImmutable
public class JannotTestMutableClass {
    private String name;

    public JannotTestMutableClass( final String name ) {
	this.name = name;
    }

    public String getName() {
	return name;
    }
}

