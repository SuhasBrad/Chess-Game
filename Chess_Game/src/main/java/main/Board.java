 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main;
import java.awt.Graphics2D;
import java.awt.Color;

/**
 *
 * @author suhas
 */
public class Board {
    
    final int MAX_COLUMN = 8;
    final int MAX_ROW = 8;
    public static final int SQUARE_SIZE = 100; // Individual Square size of the 64 squares
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE/2;
    
    public void draw(Graphics2D g2d)
    {
        int col_switch = 0; //flag to change the chess square color alternatively
        for(int row = 0; row < MAX_ROW ; row++)
        {
            for(int col = 0; col < MAX_COLUMN ; col++)
            {
                if(col_switch == 0)
                {
                    g2d.setColor(new Color(210,165,125)); // setting color with RGB
                    col_switch = 1;
                }
                else
                {
                    g2d.setColor(new Color(175,115,70));
                    col_switch = 0;
                }
                g2d.fillRect(col*SQUARE_SIZE,row*SQUARE_SIZE, SQUARE_SIZE,SQUARE_SIZE);
            }
            if(col_switch == 0) // condition to check and alternate color for each row
            {
                col_switch = 1;
            }
            else
            {
                col_switch = 0;
            }
        }
    }
    
}
