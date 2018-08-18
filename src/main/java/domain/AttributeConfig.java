package domain;

import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AttributeConfig {
	
	public AttributeConfig(String name, String is, Pattern pattern) {
		super();
		this.name = name;
		this.is = is;
		this.pattern = pattern;
	}

	private String name;
	private String is;
	private Pattern pattern;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIs() {
		return is;
	}
	public void setIs(String is) {
		this.is = is;
	}
	public Pattern getPattern() {
		return pattern;
	}
	
	@Override
	public String toString() {		
		return ToStringBuilder.reflectionToString(this);
	}
	
}
