package eu.hellek.gba.tools;

import eu.hellek.gba.server.utils.MyGeocellUtils;
import eu.hellek.gba.server.utils.Utils;

public class Bs {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int j_min = -1;
		int j_max = 1;
		int i_min = -1;
		int i_max = 1;
		j_min = -3;
		j_max = 3;
		i_min = -3;
		i_max = 3;
		int counter = 0;
		String cell = "31bcbc44r";
		
		for(int i = i_min; i <= i_max; i++) { // 2 schleifen um alle 49 möglichen cells zu durchlaufen
			for(int j = j_min; j<= j_max; j++) {
				String neighbour;
				counter++;
				if (Math.abs(j) > 1 || Math.abs(i) > 1) {
					/*int i_temp = i;
					int j_temp = j;*/
					neighbour = cell;
					for(int m = Math.abs(i); m > 0; m--) {
						if(i > 0) {
							neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {1, 0});
						} else {
							neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {-1, 0});
						}
					}
					for(int m = Math.abs(j); m > 0; m--) {
						if(j > 0) {
							neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {0, 1});
						} else {
							neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {0, -1});
						}
					}
				} else {
					neighbour = MyGeocellUtils.adjacent(cell, new int [] {i, j});
				}
//				System.err.println(counter + "\t\"" + neighbour + "\"");
//				System.err.print("\"" + neighbour + "\", ");
				
				int distance;
				if(i == 0 && j == 0) { // gleiche cell
					distance = 1;
				} else if(i == 0 && Math.abs(j) != 0) { // nur vertikal
					distance = 19 * Math.abs(j);
				} else if(j == 0 && Math.abs(i) != 0) { // nur horizontal
					distance = 16 * Math.abs(i);
				} else if(i != 0 && Math.abs(i) == Math.abs(j)) { // diagonal x=y
					distance = 25 * Math.abs(i);
				} else if(i != 0 && j != 0 && Math.abs(i) != Math.abs(j)) {
					distance = (int)Math.round(Math.sqrt(Math.pow(19 * Math.abs(j), 2) + Math.pow(16 * Math.abs(i), 2)) + 0.5);
				} else {
					System.err.println("this should not happen");
					distance = 99;
				}
				System.err.println(i + "\t" + j + "\t" + distance + "\t" + Utils.distanceBetweenGeoCells(cell, neighbour, true));
			}
		}
	}

}
