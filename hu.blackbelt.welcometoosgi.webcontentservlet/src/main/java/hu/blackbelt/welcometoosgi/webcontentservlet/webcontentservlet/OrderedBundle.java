package hu.blackbelt.welcometoosgi.webcontentservlet.webcontentservlet;

import org.osgi.framework.Bundle;

public class OrderedBundle implements Comparable<OrderedBundle> {
	int priority;
	Bundle bundle;

	public OrderedBundle(int priority, Bundle bundle) {
		super();
		this.priority = priority;
		this.bundle = bundle;
	}

	@Override
	public int compareTo(OrderedBundle o) {
		return new Integer(this.priority).compareTo(new Integer(o.priority));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundle == null) ? 0 : (int) bundle.getBundleId());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof OrderedBundle)) {
			return false;
		}
		return this.bundle.getBundleId() == ((OrderedBundle) obj).bundle.getBundleId();
	}
}
