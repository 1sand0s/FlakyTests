package org.evosuite.runtime.instrumentation;

import java.util.ArrayList;

class StaticFieldList {
    ArrayList<StaticField> staticField;

    StaticFieldList(){
	staticField = new ArrayList<>();
    }

    void add(StaticField staticField){
	this.staticField.add(staticField);
    }

    StaticField get(int j){
	return staticField.get(j);
    }
}
