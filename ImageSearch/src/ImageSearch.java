
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

/*path of the data set , and the size of search result could be changed here*/


public class ImageSearch extends JFrame
                              implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1466747296908473900L;
	JFileChooser fc;
	JPanel contentPane;

	int resultsize = 9;    //size of the searching result
	String datasetpath = "D:\\assignment_1\\Assignment1\\ImageData\\train\\data\\bear"; //the path of image dataset
    ColorHist colorhist = new ColorHist();
    JButton openButton, searchButton;
	BufferedImage bufferedimage;
    
	JLabel [] imageLabels = new JLabel [ resultsize ];
	
	File file = null;


    public ImageSearch() {
        
        openButton = new JButton("Select an image...",
                createImageIcon("images/Open16.gif"));
        openButton.addActionListener(this);
        
        searchButton = new JButton("Search");
        searchButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(searchButton);
        
		
    	JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(0,3));
        
        for (int i = 0; i<imageLabels.length;i++){
        	imageLabels[i] = new JLabel();
        	imagePanel.add(imageLabels[i]);
        }

		contentPane = (JPanel)this.getContentPane();
		setSize(800,900);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane.add(buttonPanel, BorderLayout.PAGE_START);
        contentPane.add(imagePanel, BorderLayout.CENTER);
        
        contentPane.setVisible(true);
		setVisible(true);
//        add(logScrollPane, BorderLayout.CENTER);
        
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ImageSearch.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        //Set up the file chooser.
        if (e.getSource() == openButton) {
        if (fc == null) {
            fc = new JFileChooser();

	    //Add a custom file filter and disable the default
	    //(Accept All) file filter.
            fc.addChoosableFileFilter(new ImageFilter());
            fc.setAcceptAllFileFilterUsed(false);

	    //Add custom icons for file types.
            fc.setFileView(new ImageFileView());

	    //Add the preview pane.
            fc.setAccessory(new ImagePreview(fc));
        } 
        

        //Show it.
        int returnVal = fc.showDialog(ImageSearch.this,
                                      "Select an image..");

        //Process the results.
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();

        }

        fc.setSelectedFile(null);
        }else if (e.getSource() == searchButton) {
        	
        	try {
				bufferedimage = ImageIO.read(file);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	BufferedImage [] imgs = null;
			try {
				imgs = colorhist.search (datasetpath, bufferedimage, resultsize);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
			for(int i = 0; i<imageLabels.length;i++)
				imageLabels[i].setIcon(new ImageIcon(imgs[i]));
        	
        }
    }

    public static void main(String[] args) {
    	
		@SuppressWarnings("unused")
		ImageSearch example = new ImageSearch();
    }
}
