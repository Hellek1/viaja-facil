package eu.hellek.gba.shared;

import java.util.Comparator;

public class ConnectionProxyComparator implements Comparator<ConnectionProxy> {

	@Override
	public int compare(ConnectionProxy o1, ConnectionProxy o2) {
		return o1.getTime() - o2.getTime();
	}

}
