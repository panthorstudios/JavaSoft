import java.applet.*;
import java.awt.*;
import java.util.*;
import java.net.*;

public class JavaSoftMenu extends Applet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//Variable definitions
	Image image,pics;
	Image bbuf;
	Graphics bbufG;
	Rectangle[] hotSpot;
	Color bgColor,fgColor;
	String[][] itemUrl;
	String[][] item;
	String baseURL;
	Rectangle[] dotCoords;
	Rectangle menuPict;
	int indent;
	int fontHeight;
	Font f;
	FontMetrics fm;
	boolean bgimageDrawn,loaded=false;
	Rectangle[] menuItemRect;
	Thread  bgloader=null;
	int curMenu;
	int curMenuItem;
	int log=0;

	public String getHTMLParameter(String name, String defaultParam) {
		String param=getParameter(name);
		if (param!=null) {
			return param;
		} else {
			return defaultParam;
		}
	}
	// This routine initializes the applet

	public void init() {
		int[] coords;

		/*
		 <param name="bgimage" value="bg2.jpg">
		<param name="pics" value="pics.jpg">
		<param name="indent" value="2">
		<param name="baseURL" value="http://java.sun.com/">
		<param name="fontsize" value="15">
		<param name="fontface" value="Helvetica">
		<param name="fontstyle" value="plain">
		<param name="bgcolor" value="207 152 207">
		<param name="fgcolor" value="0 0 0">
		<param name="menupict" value="418 207 100 100">
		<param name="menu0" value="186 121 113 024|277 126 013 013|Briefcase|item.cgi?1+970210115411|Mug|item.cgi?2+970210115411">
		<param name="menu1" value="186 143 113 024|277 148 013 013|Baseball Cap|item.cgi?5+970210115411|Watch|item.cgi?6+970210115411">
		<param name="menu2" value="186 165 113 024|277 170 013 013|Denim Cap|item.cgi?7+970210115411|Baby denim cap|item.cgi?8+970210115411|Youth Tee|item.cgi?9+970210115411|Duke toy|item.cgi?10+970210115411">
		<param name="menu3" value="186 189 113 024|277 192 013 013|Color Block Polo|item.cgi?11+970210115411|T-Shirt|item.cgi?12+970210115411|Denim Shirt|item.cgi?13+970210115411|Leather Jacket|item.cgi?14+970210115411">
		 */


		image = getImage(getDocumentBase(), getHTMLParameter("bgimage","bg2.jpg")); // background image
		pics = getImage(getDocumentBase(), getHTMLParameter("pics","pics.jpg"));     // store items image
		baseURL = getParameter("baseURL");
		//		tracker  =  new  MediaTracker(this); 
		//		tracker.addImage(image,0);
		//		tracker.addImage(pics,1);
		//		tracker.checkID(0,true);

		indent = Integer.parseInt(getHTMLParameter("indent","2"));
		coords = parseInt(getHTMLParameter("bgcolor","207 152 207"), " ");
		bgColor= new Color(coords[0],coords[1],coords[2]);
		coords = parseInt(getHTMLParameter("fgcolor","0 0 0"), " ");
		fgColor= new Color(coords[0],coords[1],coords[2]);
		coords = parseInt(getHTMLParameter("menupict","418 207 100 100"), " ");
		menuPict = new Rectangle(coords[0],coords[1],coords[2],coords[3]);
		bbuf = createImage(getSize().width, getSize().height);
		bbufG = bbuf.getGraphics();



		// This adjusts the font size from a pixel value to a point size, since
		//   Java only deals in point sizes. It keeps decrementing the given point
		//   size until the font height is equal to or below the desired size.


		int fh = Integer.parseInt(getHTMLParameter("fontsize","15"));
		int i = fh;
		boolean adjusted=false;
		while (!adjusted && i>10) {
			f = new Font(getHTMLParameter("fontface","Helvetica"), Font.PLAIN, i);
			fm = getFontMetrics(f);
			if (fm.getHeight() <= fh) {
				adjusted=true;
			}
			else {
				i--;
			}
		}
		fontHeight = fm.getHeight();
		for (i=0; ; i++) {
			if (getParameter("menu"+i) == null) {
				hotSpot = new Rectangle[i];
				dotCoords=new Rectangle[i];
				itemUrl = new String[i][];
				item = new String[i][];
				break;
			}
		}


		for (i=0; i<hotSpot.length; i++) {
			String[] fields = parse(getParameter("menu"+i),"|");
			coords = parseInt(fields[0], " ");
			hotSpot[i] = new Rectangle(coords[0], coords[1], coords[2], coords[3]);
			coords = parseInt(fields[1], " ");
			dotCoords[i] = new Rectangle(coords[0], coords[1], coords[2], coords[3]);

			item[i] = new String[(fields.length-2)/2];
			itemUrl[i] = new String[(fields.length-2)/2];
			for (int j=0; j < item[i].length; j++) {
				item[i][j] = fields[j*2+2];
				itemUrl[i][j] = fields[j*2+3];
			}
		}
	}

	// This routine parses an input parameter into an array of strings
	String[] parse(String s, String sep) {
		StringTokenizer st = new StringTokenizer(s, sep);
		String result[] = new String[st.countTokens()];

		for (int i=0; i<result.length; i++) {
			result[i] = st.nextToken();
		}
		return result;
	}

	// This routine parses an input parameter into an array of integers
	int[] parseInt(String s, String sep) {
		StringTokenizer st = new StringTokenizer(s, sep);
		int[] result = new int[st.countTokens()];

		for (int i=0; i<result.length; i++) {
			result[i] = Integer.parseInt(st.nextToken());
		}
		return result;
	}


	// The paint routine; causes the background image to be redrawn
	public void paint(Graphics g) {
		bgimageDrawn = false;
		update(g);
	}

	// The update routine; draws all of the applet
	public void update(Graphics g) {
		Graphics g2;
		int x,y;

		if (!bgimageDrawn) {
			bgimageDrawn = g.drawImage(image, 0, 0, this);
			return;
		}

		bbufG.drawImage(image, 0, 0, this);

		// This draws the item picture in the lower right corner (if necessary and if
		//   the image has been loaded).
		if (curMenu >= 0) {
			if (curMenuItem>=0) { //&& tracker.checkID(1)) {
				y=curMenu*menuPict.height;
				x=curMenuItem*menuPict.width;
				g2 = bbuf.getGraphics();
				g2.clipRect(menuPict.x,menuPict.y,menuPict.width,menuPict.height);
				g2.drawImage(pics,menuPict.x-x,menuPict.y-y,this);
				g2.dispose();
			}
			g2 = bbuf.getGraphics();
			g2.setColor(fgColor);
			g2.fillOval(dotCoords[curMenu].x,dotCoords[curMenu].y,dotCoords[curMenu].width,dotCoords[curMenu].height);
			g2.dispose();


			// This draws the menu items for the selected menu

			if (menuItemRect!=null) {
				g2 = bbuf.getGraphics();



				for (int i=0; i<menuItemRect.length; i++) {
					drawMenuItem(g2, i);
				}
				g2.dispose();

			}

		}
		g.drawImage(bbuf, 0, 0, this);


	}


	// This routine draws an individual (text) menu item
	void drawMenuItem(Graphics g, int i) {

		if (i == curMenuItem) {
			g.setColor(bgColor);
			g.fillRect(menuItemRect[i].x+ indent, menuItemRect[i].y,
					menuItemRect[i].width, menuItemRect[i].height);

		} else {
			g.setColor(Color.white);
			g.fillRect(menuItemRect[i].x+ indent, menuItemRect[i].y,
					menuItemRect[i].width, menuItemRect[i].height);
		}
		g.setFont(f);
		int y = menuItemRect[i].y;
		g.setColor(fgColor);
		g.drawString(item[curMenu][i],menuItemRect[i].x + indent,y + fm.getAscent());


	}

	// Resets the current menu and menu item when the mouse leaves the applet
	public boolean mouseExit(Event evt, int x, int y) {
		curMenuItem = curMenu = -1;
		repaint();
		return true;
	}

	// When the mouse enters the applet, register the move
	public boolean mouseEnter(Event evt, int x, int y) {
		return mouseMove(evt, x, y);
	}

	// This routine handles button clicks.

	public boolean mouseDown(Event evt, int x, int y) {
		URL newURL;

		try {
			String u = null;
			if ((curMenuItem >= 0) && (itemUrl[curMenu].length) > 0 && ((menuItemRect[curMenuItem].contains(x, y)) || (menuPict.contains(x, y)))) {
// Go to URL corresponding to selected menu item
				u = itemUrl[curMenu][curMenuItem];
				newURL= new URL(getDocumentBase(),u);
				getAppletContext().showDocument(newURL);
			}

		} catch (Exception e) {
			System.out.println("curMenu = " + String.valueOf(curMenu) + "; curMenuItem=" + String.valueOf(curMenuItem));
			e.printStackTrace();
		}
		return true;
	}

	// This routine determines which menu and/or menu item to draw, then calls update()
	public boolean mouseMove(Event evt, int x, int y) {
		if (curMenu >= 0) {
			int sm = inMenu(menuItemRect, x, y);

			if (curMenuItem != sm && sm !=-1) {
				curMenuItem = sm;
				repaint();
			}
			if (sm >= 0) {
				return true;
			}
		}

		int m = inMenu(hotSpot, x, y);
		if (m != curMenu && m!=-1) {
			curMenu = m;
			curMenuItem=-1;
			if (m >= 0) {
				int maxWidth = 50;
				menuItemRect = new Rectangle[item[curMenu].length];
				for (int i=0; i<menuItemRect.length; i++) {
					int w = fm.stringWidth(item[curMenu][i]);
					if (w > maxWidth) {
						maxWidth = w;
					}

					menuItemRect[i] = new Rectangle();
					menuItemRect[i].height = fm.getHeight();
				}

				y = dotCoords[curMenu].y + dotCoords[curMenu].height - (fm.getHeight()*menuItemRect.length);
				x = hotSpot[curMenu].x + hotSpot[curMenu].width;
				for (int i=0; i<item[curMenu].length; i++) {
					menuItemRect[i].x = x;
					menuItemRect[i].y = y;
					menuItemRect[i].width = maxWidth;
					y += menuItemRect[i].height;
				}

			}
			repaint();
		}
		return true;
	}

	// This routine determines which hotspot the mouse pointer is in, given an array of hotspots (rectangles).
	final int inMenu(Rectangle[] testrect, int x, int y) {
		if (testrect != null) {
			for (int i=0; i<testrect.length; i++) {
				if (testrect[i].contains(x, y)) {
					return i;
				}
			}
		}
		return -1;
	}
}
