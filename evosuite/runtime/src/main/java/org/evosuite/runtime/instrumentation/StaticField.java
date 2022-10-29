/**
 * This class represents a static field that was declared in the current
 * visited class.
 *
 * @author galeotti
 */
package org.evosuite.runtime.instrumentation;

class StaticField {
    /**
     * Name of the owning class
     */
    String className;
    
    /**
     * Name of the static field
     */
    String name;
    
    /**
     * Field descriptor (ie type) of the static field
     */
    String desc;
    
    /**
     * Initial value (if any) for the static field
     */
    Object value;

    StaticField(String className, String name, String desc, Object value){
	this.className = className;
	this.name = name;
	this.desc = desc;
	this.value = value;
    }
    
    @Override
    public String toString() {
	return "StaticField [name=" + name + "]";
    }
}
