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

import java.math.BigDecimal;
import java.util.Random;

import board.PositionState;

public class RandomMoveGenerator implements ScoringAlgorithm {

	@Override
	public Move[] Score(PositionState positionState,boolean white) {
		// TODO Auto-generated method stub
		String[] moves = positionState.generateLegalMoves(white);
		
		Move[] arr = new Move[moves.length];
		final Random random = new Random();
		for(int i=0;i<moves.length;i++) {
			arr[i] = new Move();
			arr[i].setNotation(moves[i]);
			BigDecimal bd = new BigDecimal(random.nextDouble()*10);
			arr[i].setScore(bd.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
		}
		java.util.Arrays.sort(arr);
		return arr;
	}

}
