/**
 *     javachesslib - A Java Chess Library for multi-use applications.
 *     Copyright (C) 2012 http://code.google.com/p/javachesslib/
 *     
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package board.engine;

public class Move implements Comparable<Move> {
	private String notation;
	private double score;
	
	public void setNotation(String notation) {
		this.notation = notation;
	}
	public String getNotation() {
		return notation;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public double getScore() {
		return score;
	}
	@Override
	public int compareTo(Move o) {
		return new Double(o.score).compareTo(score);
	}
	
	@Override
	public String toString() {
		return notation+"["+score+"]";
	}
}
