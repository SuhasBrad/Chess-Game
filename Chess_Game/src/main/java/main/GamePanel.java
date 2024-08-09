package main;
import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import piece.*;

/**
 *
 * @author suhas
 */
public class GamePanel extends JPanel implements Runnable{ 
    
    public static final int WIDTH = 1200; // Chess window dimensions
    public static final int HEIGHT = 800;
    final int FPS = 60; //Screen refresh rate and update cycle count
    Thread gameThread; //Thread to run the game
    Board board = new Board();
    Mouse mouse = new Mouse();

    //PIECES
    public static ArrayList<Piece> pieces = new ArrayList<>(); // Default location of pieces
    public static ArrayList<Piece> simPieces = new ArrayList<>(); // Copy of pieces list to play
    ArrayList<Piece> promotionPieces = new ArrayList<>(); // list that stores promotion pieces
    Piece activeP,checkingP;// Active piece, checkPiece
    public static Piece castlingP;

    // COLOR
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    // Game booleans
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameover;
    boolean stalemate;

    public GamePanel()
    {
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse); // to detect mouse inputs and events
        addMouseListener(mouse);

        //setPieces();
        testPieces();
        copyPieces(pieces,simPieces);
    }
    
    public void launchGame()
    {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setPieces()
    {
        // White Pieces
        pieces.add(new Pawn(WHITE,0,6));
        pieces.add(new Pawn(WHITE,1,6));
        pieces.add(new Pawn(WHITE,2,6));
        pieces.add(new Pawn(WHITE,3,6));
        pieces.add(new Pawn(WHITE,4,6));
        pieces.add(new Pawn(WHITE,5,6));
        pieces.add(new Pawn(WHITE,6,6));
        pieces.add(new Pawn(WHITE,7,6));
        pieces.add(new Rook(WHITE,0,7));
        pieces.add(new Rook(WHITE,7,7));
        pieces.add(new Knight(WHITE,1,7));
        pieces.add(new Knight(WHITE,6,7));
        pieces.add(new Bishop(WHITE,2,7));
        pieces.add(new Bishop(WHITE,5,7));
        pieces.add(new King(WHITE,4,7));
        pieces.add(new Queen(WHITE,3,7));

        // Black Pieces
        pieces.add(new Pawn(BLACK,0,1));
        pieces.add(new Pawn(BLACK,1,1));
        pieces.add(new Pawn(BLACK,2,1));
        pieces.add(new Pawn(BLACK,3,1));
        pieces.add(new Pawn(BLACK,4,1));
        pieces.add(new Pawn(BLACK,5,1));
        pieces.add(new Pawn(BLACK,6,1));
        pieces.add(new Pawn(BLACK,7,1));
        pieces.add(new Rook(BLACK,0,0));
        pieces.add(new Rook(BLACK,7,0));
        pieces.add(new Knight(BLACK,1,0));
        pieces.add(new Knight(BLACK,6,0));
        pieces.add(new Bishop(BLACK,2,0));
        pieces.add(new Bishop(BLACK,5,0));
        pieces.add(new King(BLACK,4,0));
        pieces.add(new Queen(BLACK,3,0));
    }

    private void copyPieces(ArrayList<Piece> source , ArrayList<Piece> target) // method to copy the default pieces
    {
        target.clear();
        for(int i = 0; i < source.size(); i++){
            target.add(source.get(i));
        }
    }

    public void testPieces()
    {
        pieces.add(new Pawn(WHITE,5,5));
        pieces.add(new Queen(WHITE,3,3));
        pieces.add(new King(BLACK,4,1));
//        pieces.add(new Knight(WHITE,7,7));
//        pieces.add(new Bishop(WHITE,7,6));
//        pieces.add(new Rook(WHITE,7,0));
//        pieces.add(new Queen(BLACK,6,4));
        pieces.add(new Pawn(WHITE,1,6));
        pieces.add(new Pawn(BLACK,2,1));
//        pieces.add(new Pawn(WHITE,3,6));
//        pieces.add(new Pawn(WHITE,0,6));
//        pieces.add(new Pawn(BLACK,2,1));
//        pieces.add(new Pawn(BLACK,6,1));
    }
    
    @Override
    public void run()
    { // Game loop creation 
       double drawInterval = 1000000000 /FPS; // set to 1/60th of a second
       double delta = 0; // Time difference
       long lastTime = System.nanoTime();
       long currentTime;
       
       while (gameThread != null)
       {
           currentTime = System.nanoTime();
           
           delta += (currentTime - lastTime)/drawInterval;
           lastTime = currentTime;
           
           if(delta >=1)
           {
               update();
               repaint();
               delta--; // resetting delta to 0
           }
       }
    }
    
    private void update()
    {  //Handles all the updates of the game such as x,y positions, piece count etc.

        if(promotion){
            promoting();
        }else if (gameover == false && stalemate == false){
            // Mouse button pressed
            if(mouse.pressed)
            {
                if(activeP == null)
                {
                    // if the activeP is null, check if a piece can be picked
                    for(Piece piece : simPieces)
                    {
                        // if the mouse is on an ally piece, pick up as activeP
                        if(piece.color == currentColor &&
                                piece.col == mouse.x/Board.SQUARE_SIZE &&
                                piece.row == mouse.y/Board.SQUARE_SIZE)
                        {
                            activeP = piece;
                        }
                    }
                }
                else
                {
                    // if the player is holding a piece, simulate the move
                    simulate();
                }
            }

            // Mouse button released
            if (mouse.pressed == false)
            {
                if(activeP != null){
                    if(validSquare){
                        // Move confirmed
                        //update the piece list if a piece has been captured and removed after simulation
                        copyPieces(simPieces,pieces);
                        activeP.updatePosition();
                        if(castlingP != null)
                        {
                            castlingP.updatePosition();
                        }

                        if(isKingInCheck() && isCheckMate()){
                            // Possible Game Over
                            gameover = true;
                        }else if(isStalemate() && isKingInCheck() == false){
                            stalemate = true;
                        }
                        else{ // Game is not over
                            if(canPromote())
                            {
                                promotion = true;
                            }
                            else {
                                changePlayer();
                            }
                        }
                    }else {
                        // the move is not valid so reset everything
                        copyPieces(pieces,simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }

    }

    private void simulate()
    {
        canMove = false;
        validSquare = false;

        // Rest the piece list in every loop to restore the removed piece during simulation
        copyPieces(pieces,simPieces);

        //Reset the castling piece's position
        if(castlingP != null)
        {
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }
        // if a piece is being held, update the position
        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activeP.col = activeP.getCol(activeP.x); // to adjust the current square position of the piece if there is overlap
        activeP.row = activeP.getRow(activeP.y);

        // check if the piece id hovering over a valid square
        if (activeP.canMove(activeP.col, activeP.row))
        {
            canMove = true;

            // if hitting a piece, remove it from the board
            if(activeP.hittingP != null)
            {
                simPieces.remove(activeP.hittingP.getIndex());
            }
            checkCastling();

            if(isIllegal(activeP) == false && opponentCanCaptureKing() == false)
            {
                validSquare = true;
            }
        }
    }

    private boolean isIllegal(Piece king)
    {
        if(king.type == Type.KING)
        {
            for(Piece piece : simPieces)
            {
                if(piece != king && piece.color != king.color && piece.canMove(king.col,king.row))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing()
    {
        Piece king = getKing(false);
        for(Piece piece : simPieces)
        {
            if(piece.color != king.color && piece.canMove(king.col,king.row)){
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck()
    {
        Piece king = getKing(true);
        if(activeP.canMove(king.col,king.row)){
            checkingP = activeP;
            return true;
        }else {
            checkingP = null;
        }
        return false;
    }

    private Piece getKing(boolean opponent)
    {
        Piece king = null;
        for(Piece piece : simPieces)
        {
            if(piece.type == Type.KING && piece.color != currentColor)
            {
                king = piece;
            }else{
                if(piece.type == Type.KING && piece.color == currentColor)
                {
                    king = piece;
                }
            }
        }
        return king;
    }

    private boolean isCheckMate()
    {
        Piece king = getKing(true);
        if(kingCanMove(king))
        {
            return false;
        }
        else{
            // check if the attack can be blocked with ally pieces
            // check the position of the checking piece and the king in check
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if(colDiff == 0) // attack from vertically
            {
                if(checkingP.row < king.row)
                {
                    // checking piece if above the king
                    for(int row = checkingP.row; row < king.row; row++)
                    {
                        for(Piece piece : simPieces)
                        {
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col,row)){
                                return false;
                            }
                        }
                    }
                }
                if (checkingP.row > king.row){
                    // checking piece is below the king
                    for(int row = checkingP.row; row > king.row; row--)
                    {
                        for(Piece piece : simPieces)
                        {
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col,row)){
                                return false;
                            }
                        }
                    }
                }
            }
            else if(rowDiff == 0) // attack from horizontally
            {
                if(checkingP.col < king.col){
                    // checking piece is to the left
                    for(int col = checkingP.col; col < king.col; col++)
                    {
                        for(Piece piece : simPieces)
                        {
                            if(piece != king && piece.color != currentColor && piece.canMove(col,checkingP.row)){
                                return false;
                            }
                        }
                    }
                }


                if(checkingP.col > king.col)
                {   // checking piece is to the right
                    for(int col = checkingP.col; col > king.col; col--)
                    {
                        for(Piece piece : simPieces)
                        {
                            if(piece != king && piece.color != currentColor && piece.canMove(col,checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
            }
            else if(colDiff == rowDiff) // attack from diagonally
            {
                if(checkingP.row < king.row) {
                    // The checking piece is above the king
                    if(checkingP.col < king.col) {
                        // the checking piece is in the upper left
                        for(int col = checkingP.col, row = checkingP.row; col < king.col; col++,row++){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)) {
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > king.col) {
                        // the checking piece is in the upper right
                        for(int col = checkingP.col, row = checkingP.row; col > king.col; col--,row++){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(checkingP.row > king.row) {
                    // The checking piece is below the king
                    if(checkingP.col < king.col) {
                        // the checking piece is  in the lower left
                        for(int col = checkingP.col, row = checkingP.row; col < king.col; col++,row--){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)) {
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > king.col){
                        // the checking piece is in the lower right
                        for(int col = checkingP.col, row = checkingP.row; col > king.col; col--,row--){
                            for(Piece piece : simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)) {
                                    return false;
                                }
                            }
                        }
                    }
                }

            }
            else{ // checking piece is Knight
            }
        }

        return true;
    }
    private boolean kingCanMove(Piece king)
    {
        // Simulate if there are any squares where king can move
        if(isValidMove(king,-1,-1)) {return true;}
        if(isValidMove(king,0,-1)) {return true;}
        if(isValidMove(king,1,-1)) {return true;}
        if(isValidMove(king,-1,0)) {return true;}
        if(isValidMove(king,1,0)) {return true;}
        if(isValidMove(king,-1,1)) {return true;}
        if(isValidMove(king,0,1)) {return true;}
        if(isValidMove(king,1,1)) {return true;}

        return false;
    }
    private boolean isValidMove(Piece king,int colPlus,int rowPlus)
    {
        boolean isValidMove = false;

        //Update the king's position for a second
        king.col += colPlus;
        king.row += rowPlus;

        if(king.canMove(king.col,king.row)) {
            if (king.hittingP != null) {
                simPieces.remove(king.hittingP.getIndex());
            }
            if (isIllegal(king) == false) {
                isValidMove = true;
            }
        }
        // reset the king's position and restore the removed piece
        king.resetPosition();
        copyPieces(pieces,simPieces);

        return isValidMove;
    }

    private boolean isStalemate()
    {
        int count = 0;
        // Count the number of pieces
        for(Piece piece : simPieces) {
            if(piece.color != currentColor){
                count++;
            }
        }

        // if only one piece (the king) is left
        if(count == 1)
            if(kingCanMove(getKing(true)) == false){
                return true;
            }
        return false;
    }

    private void checkCastling()
    {
        if(castlingP != null){
            if(castlingP.col == 0)
            {
                castlingP.col += 3;
            }
            else if(castlingP.col == 7)
            {
                castlingP.col -= 2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }
    }

    private void changePlayer()
    {
        if(currentColor == WHITE)
        {
            currentColor = BLACK;
            // Reset black's two stepped status
            for(Piece piece: pieces)
            {
                if(piece.color == BLACK)
                {
                    piece.twoStepped = false;
                }
            }
        }
        else {
            currentColor = WHITE;
            // Reset white's two stepped status
            for(Piece piece: pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }
        activeP = null;
    }

    private boolean canPromote()
    {
        if(activeP.type == Type.PAWN)
        {
            if(currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7)
            {
                promotionPieces.clear();
                promotionPieces.add(new Rook(currentColor,9,2));
                promotionPieces.add(new Knight(currentColor,9,3));
                promotionPieces.add(new Bishop(currentColor,9,4));
                promotionPieces.add(new Queen(currentColor,9,5));
                return true;
            }
        }
        return false;
    }

    public void promoting()
    {
        if(mouse.pressed)
        {
            for(Piece piece : promotionPieces)
            {
                if(piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE)
                {
                    switch(piece.type){
                        case ROOK: simPieces.add(new Rook(currentColor,activeP.col,activeP.row)); break;
                        case KNIGHT: simPieces.add(new Knight(currentColor,activeP.col,activeP.row)); break;
                        case BISHOP: simPieces.add(new Bishop(currentColor,activeP.col,activeP.row)); break;
                        case QUEEN: simPieces.add(new Queen(currentColor,activeP.col,activeP.row)); break;
                        default: break;
                    }
                    simPieces.remove(activeP.getIndex()); // to remove the pawn and replace with promotion piece
                    copyPieces(simPieces, pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }

    }
    public void paintComponent(Graphics g) // method to draw all the components
    {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g; // Converting Graphics object to Graphics2D object
        // Draw the board
        board.draw(g2d);

        // Draw the pieces
        for (Piece p : simPieces)
            p.draw(g2d);

        // Active square indicator
        if (activeP != null) {
            if(canMove) {
                if (isIllegal(activeP) || opponentCanCaptureKing()) {
                    g2d.setColor(Color.orange);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2d.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                } else {
                    g2d.setColor(Color.white);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2d.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }
            // Draw the active piece in the end, so it won't be hidden by the board or colored square
            activeP.draw(g2d);
        }
        // Status Message
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
        g2d.setColor(Color.white);

        if (promotion) {
            g2d.drawString("Promote to: ", 840, 150);
            for (Piece piece : promotionPieces) {
                g2d.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row),
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        } else {
            if (currentColor == WHITE) {
                g2d.drawString("White's Turn", 840, 550);
                if(checkingP != null && checkingP.color == BLACK)
                {
                    g2d.setColor(Color.red);
                    g2d.drawString("The King",840,650);
                    g2d.drawString("is in Check!",840,700);
                }
            } else {
                g2d.drawString("Black's Turn", 840, 250);
                if(checkingP != null && checkingP.color == WHITE)
                {
                    g2d.setColor(Color.red);
                    g2d.drawString("The King",840,100);
                    g2d.drawString("is in Check!",840,150);
                }
            }
        }
        if(gameover)
        {
            String s = "";
            if(currentColor == WHITE){
                s = "White Wins!. Congrats!";
            }else{
                s = "Black Wins!. Congrats!";
            }
            g2d.setFont(new Font("Helvetica", Font.PLAIN,90));
            g2d.setColor(Color.green);
            g2d.drawString(s,200,420);
        }
        if(stalemate)
        {
            g2d.setFont(new Font("Helvetica", Font.PLAIN,90));
            g2d.setColor(Color.lightGray);
            g2d.drawString("Stalemate",200,420);
        }
    }
}
