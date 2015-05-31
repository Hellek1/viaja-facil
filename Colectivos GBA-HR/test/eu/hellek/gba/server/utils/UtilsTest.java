package eu.hellek.gba.server.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.beoui.geocell.GeocellUtils;
import com.beoui.geocell.model.BoundingBox;

public class UtilsTest {

	 @Test
	 public void geoCellDistanceTest() {
		 String center = "31bcb56fl";
		 String newcell;
		 int dist_diag = 78;
		 int dist_straight_ns = 60;
		 int dist_straight_we = 50;
		 int runs = 10;
		 
		 newcell = center;
		 for(int i = 0; i < runs; i++) { // nw
			 newcell = MyGeocellUtils.adjacent(newcell, new int[] {-1, 1});
		 }
		 assertEquals(dist_diag, Utils.distanceBetweenGeoCells(center, newcell));
		 
		 newcell = center;
		 for(int i = 0; i < runs; i++) { // sw
			 newcell = MyGeocellUtils.adjacent(newcell, new int[] {-1, -1});
		 }
		 assertEquals(dist_diag, Utils.distanceBetweenGeoCells(center, newcell));
		 
		 newcell = center;
		 for(int i = 0; i < runs; i++) { // ne
			 newcell = MyGeocellUtils.adjacent(newcell, new int[] {1, 1});
		 }
		 assertEquals(dist_diag, Utils.distanceBetweenGeoCells(center, newcell));
		 
		 newcell = center;
		 for(int i = 0; i < runs; i++) { // se
			 newcell = MyGeocellUtils.adjacent(newcell, new int[] {-1, 1});
		 }
		 assertEquals(dist_diag, Utils.distanceBetweenGeoCells(center, newcell));
		 
		 newcell = center;
		 for(int i = 0; i < runs; i++) { // n
			 newcell = MyGeocellUtils.adjacent(newcell, new int[] {0, 1});
		 }
		 assertEquals(dist_straight_ns, Utils.distanceBetweenGeoCells(center, newcell));
		 
		 newcell = center;
		 for(int i = 0; i < runs; i++) { // n
			 newcell = MyGeocellUtils.adjacent(newcell, new int[] {0, -1});
		 }
		 assertEquals(dist_straight_ns, Utils.distanceBetweenGeoCells(center, newcell));
		 
		 newcell = center;
		 for(int i = 0; i < runs; i++) { // e
			 newcell = MyGeocellUtils.adjacent(newcell, new int[] {1, 0});
		 }
		 assertEquals(dist_straight_we, Utils.distanceBetweenGeoCells(center, newcell));
		 
		 newcell = center;
		 for(int i = 0; i < runs; i++) { // e
			 newcell = MyGeocellUtils.adjacent(newcell, new int[] {-1, 0});
		 }
		 assertEquals(dist_straight_we, Utils.distanceBetweenGeoCells(center, newcell));
		 /*
		 for(int i = -1; i <= 1; i++) {
			 for(int j = -1; j <= 1; j++) {
				 newcell = center;
				 newcell = GeocellUtils.adjacent(newcell, new int[] {i, j});
				 System.err.print(i + "\t" + j);
				 System.err.println("\t" + Utils.distanceBetweenGeoCells(center, newcell));
			 }
		 }*/
	 }
	 
	 @Test
	 public void tempTest() {
		 String center = "31bcb56fr";
		 for(int i = -1; i <= 1; i++) { // 2 schleifen um alle 9 möglichen cells zu durchlaufen
				for(int j = -1; j<= 1; j++) {
					String neighbour = MyGeocellUtils.adjacent(center, new int [] {i, j});
					BoundingBox bb_center = MyGeocellUtils.computeBox(center);
					BoundingBox bb_neighbour = MyGeocellUtils.computeBox(neighbour);
					double distance = 0;
					if(Math.abs(i) == 1 && Math.abs(j) == 1) {
						distance = 3.9;
					} else if(Math.abs(i) == 0 && Math.abs(j) == 1) {
						distance = 3;
					} else if(Math.abs(i) == 1 && Math.abs(j) == 0) {
						distance = 2.5;
					} else { // selbe position
						distance = 0;
					}
//					System.err.println("Distance: " + distance + " or " + GeocellUtils.distance(bb_center.getNorthEast(), bb_neighbour.getNorthEast()));
					assertEquals(distance*100, GeocellUtils.distance(bb_center.getNorthEast(), bb_neighbour.getNorthEast()), 12);
				}
		 }
	 }

	 @Test
	 public void interpolationTest() {
		 String cell1 = "31bcb57al";
		 String cell2 = "31bcb57ar";
		 String cell3 = "31bcb57bl";
		 assertEquals(MyGeocellUtils.interpolationCount(cell1, cell1), 1);
		 assertEquals(MyGeocellUtils.interpolationCount(cell2, cell2), 1);
		 assertEquals(MyGeocellUtils.interpolationCount(cell2, cell1), 2);
		 assertEquals(MyGeocellUtils.interpolationCount(cell3, cell2), 2);
		 assertEquals(MyGeocellUtils.interpolationCount(cell3, cell1), 3);
		 int dist_vert = 6;
		 int dist_hor = 5;
		 int dist_diag = 8;
		 for(int i = -2; i <= 2; i++) {
			 for(int j = -2; j <= 2; j++) {
				 String neighbour;
				 if (Math.abs(j) == 2 || Math.abs(i) == 2) {
					 if(Math.abs(i) == 2 && Math.abs(j) == 2) {
						 neighbour = MyGeocellUtils.adjacent(cell1, new int [] {i/2, j/2});
						 neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {i/2, j/2});
					 } else if(Math.abs(i) == 2) {
						 neighbour = MyGeocellUtils.adjacent(cell1, new int [] {i/2, j});
						 neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {i/2, 0});
					 } else if(Math.abs(j) == 2) {
						 neighbour = MyGeocellUtils.adjacent(cell1, new int [] {i, j/2});
						 neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {0, j/2});
					 } else {
						 System.err.println("AStar: this point should not get reached ever.");
						 neighbour = null;
					 }
				 } else {
					 neighbour = MyGeocellUtils.adjacent(cell1, new int [] {i, j});
				 }
				 int distance = 0;
				 if(i == 0 && j == 0) {
					 distance = 1;
				 } else if(i == 0 && Math.abs(j) != 0) {
					 distance = dist_vert * Math.abs(j);
				 } else if(j == 0 && Math.abs(i) != 0) {
					 distance = dist_hor * Math.abs(i);
				 } else if(i != 0 && Math.abs(i) == Math.abs(j)) {
					 distance = dist_diag * Math.abs(i);
				 } else if(i != 0 && j != 0 && Math.abs(i) + Math.abs(j) == 3) {
					 distance = dist_diag + (dist_diag / 2);
				 } else {
					 System.err.println("this should not happen");
					 distance = 99;
				 }
				 //System.err.println(i + "\t" +  j + "\t" + distance + "\t" + Utils.distanceBetweenGeoCells(cell1, neighbour));
				 assertEquals(distance, Utils.distanceBetweenGeoCells(cell1, neighbour), 1);
			 }
		 }
	 }
	 
	 @Test
	 public void adjacentTest() {
		 String cell1 = "31bcb5f3r";
		 String cell2 = "31bcb5f3l";
		 String cell3 = "31bcb5f9l";
		 assertEquals(MyGeocellUtils.adjacent(cell1, new int[] {-1, 0}), cell2);
		 assertEquals(MyGeocellUtils.adjacent(cell2, new int[] { 1, 0}), cell1);
		 assertEquals(MyGeocellUtils.adjacent(cell3, new int[] { 1, -1}), cell1);
		 assertEquals(MyGeocellUtils.adjacent(cell3, new int[] { 0, -1}), cell2);
	 }
	 
	 @Test
	 public void computeCellTest() {
		 String cell = MyGeocellUtils.compute(new com.beoui.geocell.model.Point(-34.60899,-58.380725), Utils.geoCellResolution);
		 assertEquals(cell, "31bcb57al");
//		 System.err.println(cell);
		 cell = MyGeocellUtils.compute(new com.beoui.geocell.model.Point(-34.608946,-58.378569), Utils.geoCellResolution);
		 assertEquals(cell, "31bcb57ar");
//		 System.err.println(cell);
		 cell = MyGeocellUtils.compute(new com.beoui.geocell.model.Point(-34.608804,-58.375715), Utils.geoCellResolution);
		 assertEquals(cell, "31bcb57bl");
//		 System.err.println(cell);
	 }
	 
	 @Test
	 public void computeBoxTest() {
		 String cell1 = "31bcb57al";
		 String cell2 = "31bcb57ar";
		 String cell3 = "31bcb57bl";
		 String cell4 = "31bcb57br";
		 String [] s = new String [] { cell1, cell2, cell3, cell4 };
		 for(String ss : s) {
			 BoundingBox bb_res = MyGeocellUtils.computeBox(ss);
			 System.out.print(bb_res.getNorth() + "," + bb_res.getEast() + ",");
			 System.out.print(bb_res.getSouth() + "," + bb_res.getWest() + ",");
		 }
		 System.err.println();
	 }
	 
	 @Test
	 public void boundingBoxSearchTest() {
		 BoundingBox bb = new BoundingBox(-34.66, -58.40, -34.67, -58.41);
		 List<String> cells = MyGeocellUtils.bestBboxSearchCells(bb, new MyCostFunction());
		 assertEquals(cells.size(), 20);
		 /*for(String s : cells) {
			 System.err.println(s);
			 /*BoundingBox bb_res = MyGeocellUtils.computeBox(s);
			 System.out.print(bb_res.getNorth() + "," + bb_res.getEast() + ",");
			 System.out.print(bb_res.getSouth() + "," + bb_res.getWest() + ",");*/
//		 }
	 }
	 
}
