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
package board.printer;

import board.Piece;

public class DefaultPrinter implements BoardPrinter {
	
	private static DefaultPrinter singletonInstance = new DefaultPrinter();
	public static DefaultPrinter getSingletonInstance() {
		return singletonInstance;
	}
	
	private DefaultPrinter() { }
	
	public String draw(Piece[][] board) {
		StringBuilder b = new StringBuilder();
		for(int i=board.length-1;i>=0;i--) {
			
			//b.append((i+1)+" ");
			for(int j=0;j<board[i].length;j++) {
				b.append(board[i][j].getAbbreviation() + " ");
			}
			b.append("\n");
		}
		//b.append("  A B C D E F G H\n");
		return b.toString();
	}
}
