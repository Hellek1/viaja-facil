package eu.hellek.gba.model;

import java.io.Serializable;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

@Cached
public class TrainNode implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	private Long id;
	
	@Unindexed
	private String geoCell;
	
	@Unindexed
	private String pointGeoCell; // necessary because the main geocell might be slightly moved to match the station (i.e. subte C constitucion and FFCC Roca might have slighlty different points there, therefore different geocells for the points. The main geocell is therefore moved to match the position
	
	private String uniqueName;

	@Unindexed
	private String name;
	
	private Key<Line> lineKey;
	
	@Unindexed
	private int lineType;
	
	@Unindexed
	private Key<TrainNode> nextNode;
	
	@Unindexed
	private int index;
	
	@SuppressWarnings("unused")
	@Parent
	private Key<RootEntity> rootEntity;
	
	public TrainNode() { }
	
	public TrainNode(String geoCell, String pointGeoCell, String uniqueName, String name, Key<Line> train, int type, int index, Key<RootEntity> rootEntity) {
		this.lineKey = train;
		this.name = name;
		this.geoCell = geoCell;
		this.pointGeoCell = pointGeoCell;
		this.uniqueName = uniqueName;
		this.lineType = type;
		this.index = index;
		this.rootEntity = rootEntity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGeoCell() {
		return geoCell;
	}

	public void setGeoCell(String geoCell) {
		this.geoCell = geoCell;
	}

	public String getPointGeoCell() {
		return pointGeoCell;
	}

	public void setPointGeoCell(String pointGeoCell) {
		this.pointGeoCell = pointGeoCell;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((geoCell == null) ? 0 : geoCell.hashCode());
		result = prime * result + ((lineKey == null) ? 0 : lineKey.hashCode());
		result = prime * result
				+ ((nextNode == null) ? 0 : nextNode.hashCode());
		result = prime * result
				+ ((uniqueName == null) ? 0 : uniqueName.hashCode());
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
		TrainNode other = (TrainNode) obj;
		if (geoCell == null) {
			if (other.geoCell != null)
				return false;
		} else if (!geoCell.equals(other.geoCell))
			return false;
		if (lineKey == null) {
			if (other.lineKey != null)
				return false;
		} else if (!lineKey.equals(other.lineKey))
			return false;
		if (nextNode == null) {
			if (other.nextNode != null)
				return false;
		} else if (!nextNode.equals(other.nextNode))
			return false;
		if (uniqueName == null) {
			if (other.uniqueName != null)
				return false;
		} else if (!uniqueName.equals(other.uniqueName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TrainNode [geoCell=" + geoCell + "]" + lineKey;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public Key<Line> getLineKey() {
		return lineKey;
	}

	public void setLineKey(Key<Line> lineKey) {
		this.lineKey = lineKey;
	}

	public Key<TrainNode> getNextNode() {
		return nextNode;
	}

	public void setNextNode(Key<TrainNode> nextNode) {
		this.nextNode = nextNode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLineType() {
		return lineType;
	}

	public void setLineType(int lineType) {
		this.lineType = lineType;
	}

	public int getIndex() {
		return index;
	}
	
}
