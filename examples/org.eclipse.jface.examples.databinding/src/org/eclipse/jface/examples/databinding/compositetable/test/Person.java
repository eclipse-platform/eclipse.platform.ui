package org.eclipse.jface.examples.databinding.compositetable.test;

public class Person {
	
	public String name = "";
	public String address = "";
	public String city = "Wheaton";
	public String state = "IL";
	
	public Person(String name, String address, String city, String state) {
		this.name = name;
		this.address = address;
		this.city = city;
		this.state = state;
	}
	
	public Person() {}
}
