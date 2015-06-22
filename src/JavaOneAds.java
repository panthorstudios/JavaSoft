import java.awt.*;
import java.applet.*;
import java.util.*;
import java.net.*;

/**
 * @author pjoiner
 *
 */
public class JavaOneAds extends Applet implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Thread thread = null;     // thread for animating fade
	protected Image imgMemory = null;   // memory image
	protected int dxApplet,dyApplet;    // height of applet
	protected int delay = 120;  // delay time in milliseconds
	protected int pause=1000;
	protected int runstate=-1;
	
	protected Image bgimage;                 
	protected int numimages,imgno=0;
	protected String[] itemURL;
	protected String[] itemName;
	protected int[] itemPause;
	protected boolean bgimageDrawn=false,mousein=false,loaded=false;

	public void init() {

		// get applet's size

		dxApplet = getSize().width;
		dyApplet = getSize().height;
		numimages = Integer.parseInt(getParameter("numimages"));
		delay =Integer.parseInt(getParameter("delay"));
		bgimage =getImage(getDocumentBase(), getParameter("image"));
		pause =Integer.parseInt(getParameter("pause"));
		itemURL=new String[numimages];
		itemName=new String[numimages];
		itemPause=new int[numimages];

		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(bgimage,1);

		for (int i=0;i<numimages; i++) {
			String[] fields = getParameter("image"+i).split("\\|"); // parse(getParameter("image"+i),"|");
			itemURL[i]=fields[0];
			itemName[i]=fields[1];
			itemPause[i]=Integer.parseInt(fields[2]);
		}

		try {
			tracker.waitForID(1);
		} catch (InterruptedException e){
			System.out.println("sorry...mediatracker error");
		};
		loaded=true;
		repaint(); 

		// start the animation

		thread = new Thread(this);
		thread.start();
	}



	public void run() {
		while (true) {
			try {
				if (loaded) {
					if (runstate<0) {
						for (int i = 0; i < numimages; i++) {
							//						System.out.println("running inside i loop, i="+i);
							if (runstate>-1) {
								imgno=runstate;
								break;
							}
							imgno=i;
							repaint();
							if (mousein) {
								getAppletContext().showStatus(itemName[imgno]);
							}
							if (itemPause[imgno]>0) {
								Thread.sleep(pause);
							} else {
								Thread.sleep(delay);
							}
						}
					} else {
						repaint();
						Thread.sleep(pause);
					}
				}
			} catch (InterruptedException e){
				System.out.println("sorry...mediatracker error");
			};
		}
	}


	public void paint(Graphics g) {
		bgimageDrawn = false;
		update(g);
	}

	public void update(Graphics g) {
		int y;

		if (bgimage != null) {
			y=dyApplet*imgno;
			g.clipRect(0,0,dxApplet,dyApplet);
			g.drawImage(bgimage,0,-y,this);
		}
	}

	// Resets the current menu and menu item when the mouse leaves the applet
	public boolean mouseExit(Event evt, int x, int y) {
		getAppletContext().showStatus("");
		mousein=false;
		return true;
	}

	// When the mouse enters the applet, register the move
	public boolean mouseEnter(Event evt, int x, int y) {
		getAppletContext().showStatus(itemName[imgno]);
		mousein=true;
		return true;
	}

	// This routine handles button clicks.
	public boolean mouseDown(Event evt, int x, int y) {
		URL newURL;

		try {
			if (itemURL[imgno]!=null && runstate<0) {
				runstate=imgno;
				newURL= new URL(getDocumentBase(),itemURL[imgno]);
				getAppletContext().showDocument(newURL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}


}