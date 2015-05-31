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
public class PlanQuadrat implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	private String geoCell;

	private List<Key<Line>> directLineKeys; // lines that pass through that PQ
	
	private List<Key<Line>> mainLineKeys; // lines relevant for search (subset of directLineKeys). The lines NOT contained in here are shorter versions of one of that lines and therefore left out of the search since they would not contribute to better results. Nevertheless they are needed above to be displayed as alternative possibilities.
	
	@Unindexed
	private List<Integer> indices; // the index of a point of a line in that PQ. index/position in list must match index in above list
	
	@Unindexed
	private List<Integer> indicesMLK; // like above, but now the positions match the MLK-list, not the DLK-list
	
	@Unindexed
	private List<Boolean> ignore; // true if all points of a certain line within that PQ are marked ignore. Index/position in this list has to mach mainLineKeys List
	
	@Unindexed
	private List<Boolean> twoway; // true falls das PQ in 2 richtungen befahren wird
	
	@SuppressWarnings("unused")
	@Parent
	private Key<RootEntity> rootEntity;
	
	public PlanQuadrat() { }
	
	public PlanQuadrat(String geoCell, Key<RootEntity> rootEntity) {
		this.id = geoCell;
		this.directLineKeys = new ArrayList<Key<Line>>();
		this.mainLineKeys = new ArrayList<Key<Line>>();
		this.indices = new ArrayList<Integer>();
		this.indicesMLK = new ArrayList<Integer>();
		this.ignore = new ArrayList<Boolean>();
		this.twoway = new ArrayList<Boolean>();
		this.geoCell = geoCell;
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
		PlanQuadrat other = (PlanQuadrat) obj;
		if (geoCell == null) {
			if (other.geoCell != null)
				return false;
		} else if (!geoCell.equals(other.geoCell))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PlanQuadrat [geoCell=" + geoCell + "]";
	}

	public void addDirectLineKey(Key<Line> k, Integer i) {
		if(!directLineKeys.contains(k)) {
			directLineKeys.add(k);
			indices.add(i);
		}
	}
	
	public void removeLine(Key<Line> k) {
		if(directLineKeys.contains(k)) {
			int pos = directLineKeys.indexOf(k);
			directLineKeys.remove(pos);
			indices.remove(pos);
			if(mainLineKeys != null && mainLineKeys.contains(k)) {
				pos = mainLineKeys.indexOf(k);
				mainLineKeys.remove(pos);
				ignore.remove(pos);
				twoway.remove(pos);
				indicesMLK.remove(pos);
			}
		}
	}

	public List<Key<Line>> getDirectLineKeys() {
		return directLineKeys;
	}
	
	public List<Integer> getIndices() {
		return indices;
	}

	public List<Boolean> getIgnore() {
		return ignore;
	}

	public void addMainLineKey(Key<Line> k, Boolean b, Boolean c) {
		if(this.mainLineKeys == null) {
			this.mainLineKeys = new ArrayList<Key<Line>>();
		}
		if(this.indicesMLK == null) {
			this.indicesMLK = new ArrayList<Integer>();
		}
		if(this.ignore == null) {
			this.ignore = new ArrayList<Boolean>();
		}
		if(this.twoway == null) {
			this.twoway = new ArrayList<Boolean>();
		}
		if(!mainLineKeys.contains(k)) {
			mainLineKeys.add(k);
			ignore.add(b);
			twoway.add(c);
			indicesMLK.add(indices.get(directLineKeys.indexOf(k)));
		}
	}

	public List<Key<Line>> getMainLineKeys() {
		if(this.mainLineKeys == null) {
			this.mainLineKeys = new ArrayList<Key<Line>>();
		}
		return mainLineKeys;
	}

	public List<Boolean> getTwoway() {
		return twoway;
	}

	public List<Integer> getIndicesMLK() {
		return indicesMLK;
	}

}
