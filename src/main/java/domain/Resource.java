package domain;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Resource {
	private List<AttributeConfig> attributes;	

	public List<AttributeConfig> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeConfig> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {		
		return ToStringBuilder.reflectionToString(this);
	}
	
}
