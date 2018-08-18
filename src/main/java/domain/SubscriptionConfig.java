package domain;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class SubscriptionConfig {
	private String name;
	List<Resource> resources;

	public SubscriptionConfig(String name, List<Resource> resources) {
		super();
		this.name = name;
		this.resources = resources;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	@Override
	public String toString() {		
		return ToStringBuilder.reflectionToString(this);
	}
		
}
