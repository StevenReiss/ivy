package edu.brown.cs.ivy.jannot;

@JannotTestImmutable
public class JannotTestMutableClass {
    private String class_name;

    public JannotTestMutableClass( final String name ) {
	class_name = name;
    }

    public String getName() {
	return class_name;
    }
}

