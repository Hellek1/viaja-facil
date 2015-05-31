package eu.hellek.gba.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

@Cached
public class PQA implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	private String geoCell;
	
	private List<Key<Line>> lineKeys;
	
	@Unindexed
	private List<Integer> indices;
	
	@Unindexed
	private List<Integer> i2; // combined list of ignore and twoway. first (higher bit or left bit) is 0 or 1 for ignore = false or true. right bit does the same for twoway
	
	@SuppressWarnings("unused")
	@Parent
	private Key<RootEntity> rootEntity;
	
	public PQA() { }
	
	public PQA(PlanQuadrat pq, Key<RootEntity> rootEntity) {
		this.id = pq.getGeoCell();
		this.lineKeys = pq.getMainLineKeys();
		this.indices = pq.getIndicesMLK();
		this.geoCell = pq.getGeoCell();
		this.rootEntity = rootEntity;
		this.i2 = new ArrayList<Integer>();
		for(int i = 0; i < pq.getIgnore().size(); i++) {
			boolean ignore = pq.getIgnore().get(i);
			boolean twoway = pq.getTwoway().get(i);
			int i2val = 0;
			if(ignore) {
				i2val+=2;
			}
			if(twoway) {
				i2val+=1;
			}
			i2.add(i2val);
		}
	}
	
	public PQA(String geoCell, Key<RootEntity> rootEntity) {
		this.id = geoCell;
		this.lineKeys = new ArrayList<Key<Line>>();
		this.indices = new ArrayList<Integer>();
		this.geoCell = geoCell;
		this.i2 = new ArrayList<Integer>();
		this.rootEntity = rootEntity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGeoCell() {
		return geoCell;
	}

	public void setGeoCell(String geoCell) {
		this.geoCell = geoCell;
	}

	@Override
	public int hashCode() {
		return geoCell.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PQA other = (PQA) obj;
		if (geoCell == null) {
			if (other.geoCell != null)
				return false;
		} else if (!geoCell.equals(other.geoCell))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PQA [geoCell=" + geoCell + "]";
	}
	
	public void removeLine(Key<Line> k) {
		if(lineKeys != null && lineKeys.contains(k)) {
			int pos = lineKeys.indexOf(k);
			lineKeys.remove(pos);
			i2.remove(pos);
			indices.remove(pos);
		}
	}

	public boolean getIgnore(int pos) {
		int i2val = i2.get(pos);
		if(i2val >= 2) {
			return true;
		} else {
			return false;
		}
	}

	public void addLine(Key<Line> k, int index, Boolean ignore, Boolean twoway) {
		if(!lineKeys.contains(k)) {
			lineKeys.add(k);
			indices.add(index);
			int i2val = 0;
			if(ignore) {
				i2val+=2;
			}
			if(twoway) {
				i2val+=1;
			}
			i2.add(i2val);
		}
	}

	public boolean getTwoway(int pos) {
		int i2val = i2.get(pos);
		if(i2val % 2 == 1) {
			return true;
		} else {
			return false;
		}
	}

	public List<Integer> getIndices() {
		return indices;
	}

	public List<Key<Line>> getLineKeys() {
		return lineKeys;
	}

}
