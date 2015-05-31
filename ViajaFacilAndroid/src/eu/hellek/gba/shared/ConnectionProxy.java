package eu.hellek.gba.shared;

import java.io.Serializable;
import java.util.List;

/*
 * represents a connection. several connections can be returned from the server
 * the SearchResultProxy class contains the list of these connections
 */
public class ConnectionProxy implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<LineProxy> lines; // path-segments of this connection (i.e. 1. element = walk, 2. element = a bus connection, 3. element = walk)
	private boolean expanded; // internal Android variable to save whether this connection is shown in expanded or collapsed mode in the list of results
	
	public ConnectionProxy() {
		expanded = false;
	}
	
	public ConnectionProxy(List<LineProxy> lines) {
		this.lines = lines;
		expanded = false;
	}

	public List<LineProxy> getLines() {
		return lines;
	}

	public void setLines(List<LineProxy> lines) {
		this.lines = lines;
	}

	public int getDistance() {
		int distance = 0;
		for(LineProxy lp : lines) {
			distance += lp.getDistance();
		}
		return distance;
	}
	
	public int getTime() {
		int time = 0;
		for(LineProxy lp : lines) {
			time += lp.getTime();
		}
		return time;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lines == null) ? 0 : lines.hashCode());
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
		ConnectionProxy other = (ConnectionProxy) obj;
		if (lines == null) {
			if (other.lines != null)
				return false;
		} else if (!lines.equals(other.lines))
			return false;
		return true;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

}
