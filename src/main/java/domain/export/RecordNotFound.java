package domain.export;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RecordNotFound {
	private String subscriptionName;
	private String instanceId;

	public RecordNotFound(String subscriptionName, String instanceId) {
		super();
		this.subscriptionName = subscriptionName;
		this.instanceId = instanceId;
	}

	public String getSubscriptionName() {
		return subscriptionName;
	}

	public void setSubscriptionName(String subscriptionName) {
		this.subscriptionName = subscriptionName;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
		result = prime * result + ((subscriptionName == null) ? 0 : subscriptionName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecordNotFound other = (RecordNotFound) obj;
		if (instanceId == null) {
			if (other.instanceId != null)
				return false;
		} else if (!instanceId.equals(other.instanceId))
			return false;
		if (subscriptionName == null) {
			if (other.subscriptionName != null)
				return false;
		} else if (!subscriptionName.equals(other.subscriptionName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, new ToStringStyle() {
			{
				this.setFieldSeparator(", ");
				this.setUseClassName(false);
				this.setUseIdentityHashCode(false);
				this.setUseShortClassName(false);
			}
		});
	}

}
