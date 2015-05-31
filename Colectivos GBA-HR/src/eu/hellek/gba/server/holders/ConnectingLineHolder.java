package eu.hellek.gba.server.holders;

import com.googlecode.objectify.Key;

import eu.hellek.gba.model.Line;

public class ConnectingLineHolder implements Comparable<ConnectingLineHolder> {

	private Key<Line> lineKey;
	
	private int index1;
	private int index2;
	
	public ConnectingLineHolder(Key<Line> lineKey, int index1, int index2) {
		this.lineKey = lineKey;
		this.index1 = index1;
		this.index2 = index2;
	}

	@Override
	public int compareTo(ConnectingLineHolder o) {
		return this.getLength() - o.getLength();
	}

	public Key<Line> getLineKey() {
		return lineKey;
	}

	public int getLength() {
		return index2 - index1;
	}

	public int getIndex1() {
		return index1;
	}

	public int getIndex2() {
		return index2;
	}

}
