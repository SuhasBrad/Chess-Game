package main;
import javax.swing.JFrame;

/**
 *
 * @author suhas
 */
public class Main {
    public static void main(String[] args)
    {
        JFrame window  = new JFrame("Chess");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        
        //Adding GamePanel to the window
        GamePanel gp = new GamePanel();
        window.add(gp);
        window.pack(); // To adjust GamePanel size with the window
        
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        
        gp.launchGame();
    }
    
    
}
