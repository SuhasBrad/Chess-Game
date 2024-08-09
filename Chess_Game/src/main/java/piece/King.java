package piece;

import main.GamePanel;
import main.Type;
public class King extends Piece {

    public King(int color, int col, int row) {
        super(color, col, row);

        type = Type.KING;
        if(color == GamePanel.WHITE){
            image = getImage("/piece/w-king");
        }
        else {
            image = getImage("/piece/b-king");
        }
    }

    public boolean canMove(int targetCol, int targetRow)
    {
        if(isWithinBoard(targetCol, targetRow))
        {
            if(Math.abs(targetCol - preCol) + Math.abs(targetRow - preRow) == 1 || // condition to check horizontal and vertical direction
                Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 1)// condition to check diagonal positions
            {
                if(isValidSquare(targetCol,targetRow)) {
                    return true;
                }
            }

            // Castling
            if(moved == false)
            {
                // Right Casting
                if(targetCol == preCol+2 && targetRow == preRow && pieceIsOnStraightLine(targetCol,targetRow) == false)
                {
                    for(Piece piece : GamePanel.simPieces)
                    {
                        if(piece.col == preCol+3 && piece.row == preRow && piece.moved == false)
                        {
                            GamePanel.castlingP = piece;
                            return true;
                        }
                    }
                }

                // Left Castling
                if(targetCol == preCol-2 && targetRow == preRow && pieceIsOnStraightLine(targetCol,targetRow) == false)
                {
                    Piece p[] = new Piece[2];
                    for(Piece piece: GamePanel.simPieces)
                    {
                        if(piece.col == preCol - 3 && piece.row == targetRow)
                        {
                            p[0] = piece;
                        }
                        if(piece.col == preCol - 4 && piece.row == targetRow)
                        {
                            p[1] = piece;
                        }

                        if(p[0] == null && p[1] != null && p[1].moved == false)
                        {
                            GamePanel.castlingP = p[1];
                            return true;
                        }
                    }
                }
            }
        }
        return false;

    }
}
