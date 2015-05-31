package eu.hellek.gba.server.utils;

import java.util.LinkedList;
import java.util.List;

import com.beoui.geocell.GeocellManager;
import com.beoui.geocell.GeocellUtils;
import com.beoui.geocell.model.BoundingBox;
import com.beoui.geocell.model.CostFunction;
import com.beoui.geocell.model.Point;

public class MyGeocellUtils {

	public static List<String> bestBboxSearchCells(BoundingBox bb, CostFunction cf) {
		List<String> doubleCells = GeocellManager.bestBboxSearchCells(bb, cf);
		List<String> cells = new LinkedList<String>();
		for(String doubleCell : doubleCells) {
			double middle = getMiddle(doubleCell);
			if(middle > bb.getWest()) {
				cells.add(doubleCell + "l");
			}
			if(middle < bb.getEast()) {
				cells.add(doubleCell + "r");
			}
		}
		return cells;
	}
	
	/*
	 * linke zelle: bis exkl. mitte
	 * rechte zelle: ab inkl. mitte
	 */
	public static String compute(Point p, int resolution) {
		String doubleCell = GeocellUtils.compute(p, Utils.geoCellResolution);
		double middle = getMiddle(doubleCell);
		if(p.getLon() < middle) {
			return doubleCell + "l";
		} else {
			return doubleCell + "r";
		}
	}
	
	public static BoundingBox computeBox(String cell) {
		BoundingBox doubleBox = GeocellUtils.computeBox(cell.substring(0, cell.length()-1));
		if(cell.substring(cell.length()-1, cell.length()).equals("l")) {
			return new BoundingBox(doubleBox.getNorth(), (doubleBox.getWest() + doubleBox.getEast()) / 2, doubleBox.getSouth(), doubleBox.getWest());
		} else if(cell.substring(cell.length()-1, cell.length()).equals("r")) {
			return new BoundingBox(doubleBox.getNorth(), doubleBox.getEast(), doubleBox.getSouth(), (doubleBox.getWest() + doubleBox.getEast()) / 2);
		} else {
			System.err.println("Error in computeBox: cell neither right nor left: " + cell);
			return null;
		}
	}
	
	/*
	 * cells have to be in same row or column, cell1 has to be NE of cell2
	 */
	public static int interpolationCount(String cell1, String cell2) {
		String cell1Trimmed = cell1.substring(0, cell1.length()-1);
		String cell2Trimmed = cell2.substring(0, cell2.length()-1);
		BoundingBox box1 = GeocellUtils.computeBox(cell1Trimmed);
		BoundingBox box2 = GeocellUtils.computeBox(cell2Trimmed);
		if(box1.getNorth() == box2.getNorth()) { // on a horizontal line
			int count = GeocellUtils.interpolationCount(cell1Trimmed, cell2Trimmed) * 2;
			if(cell1.substring(cell1.length()-1, cell1.length()).equals("l")) {
				count--;
			}
			if(cell2.substring(cell2.length()-1, cell2.length()).equals("r")) {
				count--;
			}
			return count;
		} else if(box1.getWest() == box2.getWest()) { // on a vertical line
			return GeocellUtils.interpolationCount(cell1Trimmed, cell2Trimmed);
		} else {
			System.err.println("Error in interpolationCount: not in same row or column: " + cell1 + " and " + cell2);
			return -1;
		}
	}
	
	public static String adjacent(String cell, int [] dir) {
		int x = dir[0]; // east or west
		int y = dir[1]; // north or south
		String cellTrimmed = cell.substring(0, cell.length()-1);
		String rl = cell.substring(cell.length()-1, cell.length());
		if(x == 0) {
			return GeocellUtils.adjacent(cellTrimmed, dir)+rl;
		} else {
			if(rl.equals("r")) {
				if(x == 1) {
					return GeocellUtils.adjacent(cellTrimmed, dir) + "l";
				} else {
					return GeocellUtils.adjacent(cellTrimmed, new int[] {0, y}) + "l";
				}
			}
			if(rl.equals("l")) {
				if(x == -1) {
					return GeocellUtils.adjacent(cellTrimmed, dir) + "r";
				} else {
					return GeocellUtils.adjacent(cellTrimmed, new int[] {0, y}) + "r";
				}
			} else {
				System.err.println("Error in adjacent: cell is neither r nor l: " + cell + " " + dir);
				return null;
			}
		}
	}
	
	private static double getMiddle(String doubleCell) {
		BoundingBox bbDoubleCell = GeocellUtils.computeBox(doubleCell);
		double west = bbDoubleCell.getWest();
		double east = bbDoubleCell.getEast();
		double middle = (west + east)/2;
		return middle;
	}
	
}
