/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrador
 */
public class Main {
    static ViewForm form = new ViewForm();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException{
        form.setVisible(true);
        Thread.sleep(200);
        form.initTabs();
    }
}
