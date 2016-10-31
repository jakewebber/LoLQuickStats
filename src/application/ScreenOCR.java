package application;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.PixelGrabber;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.ReduceNoiseFilter;
import com.jhlabs.image.ThresholdFilter;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;

/** 
 * Class for performing OCR and other pixel comparisons on League screenshots.
 * @author Jacob Webber
 */
public class ScreenOCR {
	public static ITesseract tesseract = new Tesseract();  // JNA Interface Mapping
	public static ReduceNoiseFilter noise = new ReduceNoiseFilter();
	public static ThresholdFilter threshold = new ThresholdFilter();
	public static GrayscaleFilter grayscale = new GrayscaleFilter();

	ScreenOCR(){
		List<String> configs = new ArrayList<String>();
		configs.add("digits");
		
		File tessDataFolder = LoadLibs.extractTessResources("tessdata");
		tesseract.setDatapath(tessDataFolder.getAbsolutePath());
		tesseract.setConfigs(configs);
		tesseract.setPageSegMode(7); //  Treat the image as a single text line.
		tesseract.setTessVariable("load_system_dawg", "false");
		tesseract.setTessVariable("load_freq_dawg", "false");

	}

	/**
	 *  Returns summoner names from League screenshot. */
	public ArrayList<String> screenSummoners(Image imageFile) throws TesseractException, IOException{
		BufferedImage image = convertCMYK2RGB(toBufferedImage(imageFile));
		
		/* Remove RGB Value for Summoner Group Tag */
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);

		/* average values */
		int baser = 108;
		int baseg = 96;
		int baseb = 68;
		int baser2 = 216;
		int baseg2 = 182;
		int baseb2 = 120;

		int range = 10;

		for (int i = 0; i < pixels.length; i++) {
			//int a = (pixels[i]>>24)		&0xFF;
			int r = (pixels[i]>>16)		&0xFF;
			int g = (pixels[i]>>8)		&0xFF;
			int b = (pixels[i]>>0)		&0xFF;
			if ( ((r > baser-range && r < baser+range) && (g > baseg-range && g < baseg+range) && (b > baseb-range && b < baseb+range)) ||
					(r > baser2-range && r < baser2+range) && (g > baseg2-range && g < baseg2+range) && (b > baseb2-range && b < baseb2+range)
					) {
				// We'll set the alpha value to 0 for to make it fully transparent.
				pixels[i] = 0xFF000000;
				pixels[i+1] = 0xFF000000;
				pixels[i+2] = 0xFF000000;
				pixels[i-1] = 0xFF000000;
				pixels[i-2] = 0xFF000000;

			}
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
		File newFile = new File("C:\\Users\\Jake\\Pictures\\league\\screennewnew.jpg");
		ImageIO.write(image, "jpg", newFile);
		/* Apply OCR Optimization to Image */
		RescaleOp rescaleOp = new RescaleOp(5f, -100, null);
		rescaleOp.filter(image, image);
		grayscale.filter(image, image);



		/* Test optimized image */

		newFile = new File("C:\\Users\\Jake\\Pictures\\league\\screenOCR.jpg");
		ImageIO.write(image, "jpg", newFile);

		/* Image pixel parsing definitions */
		double xIn = 11.636363636363636363636363636364; //Difference between width and text start
		double xOut = 5.08; //Difference between width and text end.
		double yInOne = 6.4; //Difference between height and text top. 
		double yOutOne = 5.5944055944055944055944055944056; //Difference between height and textbottom.
		double yDiffIn = height/yOutOne - height/yInOne;
		double yInTwo = 3.9024390243902439024390243902439; //Difference between height and 2nd text top.
		double yDiffOut = height/yInTwo - height/yInOne;

		/* Image summoner name coordinates */
		Rectangle first = 	new Rectangle((int) (width/xIn), (int)(height/yInOne), 
				(int) (width/xOut - width/xIn), (int)(height/yOutOne - height/yInOne));
		Rectangle second = 	new Rectangle((int) (width/xIn), (int)(height/yInTwo), 
				(int) (width/xOut - width/xIn), (int)yDiffIn);
		Rectangle third = 	new Rectangle((int) (width/xIn), (int)(height/yInTwo + yDiffOut), 
				(int) (width/xOut - width/xIn), (int)yDiffIn);
		Rectangle fourth = 	new Rectangle((int) (width/xIn), (int)(height/yInTwo + yDiffOut*2), 
				(int) (width/xOut - width/xIn), (int)yDiffIn);
		Rectangle fifth = 	new Rectangle((int) (width/xIn), (int)(height/yInTwo + yDiffOut*3), 
				(int) (width/xOut - width/xIn), (int)yDiffIn);
		newFile = new File("C:\\Users\\Jake\\Pictures\\league\\1.jpg");
		ImageIO.write(image.getSubimage(first.x, first.y, first.width, first.height), "jpg", newFile);
		newFile = new File("C:\\Users\\Jake\\Pictures\\league\\2.jpg");
		ImageIO.write(image.getSubimage(second.x, second.y, second.width, second.height), "jpg", newFile);
		newFile = new File("C:\\Users\\Jake\\Pictures\\league\\3.jpg");
		ImageIO.write(image.getSubimage(third.x, third.y, third.width, third.height), "jpg", newFile);
		newFile = new File("C:\\Users\\Jake\\Pictures\\league\\3.jpg");
		ImageIO.write(image.getSubimage(fourth.x, fourth.y, fourth.width, fourth.height), "jpg", newFile);
		newFile = new File("C:\\Users\\Jake\\Pictures\\league\\4.jpg");
		ImageIO.write(image.getSubimage(fifth.x, fifth.y, fifth.width, fifth.height), "jpg", newFile);

		/* Performing OCR on ingame screenshot */
		String result 	= tesseract.doOCR(image, first).replace("\n", ""); 
		String result2 	= tesseract.doOCR(image, second).replace("\n", ""); 
		String result3 	= tesseract.doOCR(image, third).replace("\n", ""); 
		String result4 	= tesseract.doOCR(image, fourth).replace("\n", ""); 
		String result5 	= tesseract.doOCR(image, fifth).replace("\n", ""); 
		/* Create ArrayList of parsed summoner names to return */
		ArrayList<String> summoners = new ArrayList<String>();
		summoners.add(result);
		summoners.add(result2);
		summoners.add(result3);
		summoners.add(result4);
		summoners.add(result5);
		System.out.println(result + "\n" + result2 + "\n" + result3 + "\n" + result4 + "\n" + result5);
		return summoners;
	}

	/** 
	 * Extract champion images from screenshot.
	 * @return ArrayList<BufferedImage> 5 bufferedImages of cropped champion icons */
	public static ArrayList<BufferedImage> screenChampions(Image imageFile) throws IOException{
		BufferedImage screenshot = toBufferedImage(imageFile);
		screenshot = convertCMYK2RGB(screenshot);
		//ImageIO.write(screenshot, "jpg", new File("C:\\Users\\Jake\\Pictures\\league\\testingthis.jpg"));

		/* Image pixel parsing definitions */
		int width = screenshot.getWidth(); 
		int height = screenshot.getHeight();
		double xIn = 64;
		double xOut = 16.84210526315789;
		double yInOne = 7.017543859649123;
		double yOutOne = 4.705882352941176;
		double yDiffIn = height/yOutOne - height/yInOne;
		double yInTwo = 4.123711340206186; 
		double yDiffOut = height/yInTwo - height/yInOne;

		/* Champion Icon coordinates */
		Rectangle firstRect = 	new Rectangle(
				(int) (width/xIn), 
				(int) (height/yInOne), 
				(int) (width/xOut - width/xIn), 
				(int)(height/yOutOne - height/yInOne));
		Rectangle secondRect = 	new Rectangle(
				(int) (width/xIn), 
				(int) (height/yInTwo), 
				(int) (width/xOut - width/xIn), 
				(int) yDiffIn);
		Rectangle thirdRect = 	new Rectangle(
				(int) (width/xIn), 
				(int) (height/yInTwo + yDiffOut),
				(int) (width/xOut - width/xIn), 
				(int)yDiffIn);
		Rectangle fourthRect = 	new Rectangle(
				(int) (width/xIn), 
				(int) (height/yInTwo + yDiffOut*2), 
				(int) (width/xOut - width/xIn), 
				(int)yDiffIn);
		Rectangle fifthRect = 	new Rectangle(
				(int) (width/xIn), 
				(int) (height/yInTwo + yDiffOut*3), 
				(int) (width/xOut - width/xIn), 
				(int)yDiffIn);
		System.out.println("firstRect: " + firstRect.getX() + " " +  firstRect.getY() + " " + firstRect.getWidth() + " " + firstRect.getHeight());
		System.out.println("secondRect: " + secondRect.getX() + " " +  secondRect.getY() + " " + secondRect.getWidth() + " " + secondRect.getHeight());

		/* Crop the screenshots into champion icons, scale them to match League's champion images, add to ArrayList. */
		ArrayList<BufferedImage> summonerChampIcons = new ArrayList<BufferedImage>();
		summonerChampIcons.add(convertCMYK2RGB(toBufferedImage(cropImage(screenshot, firstRect).getScaledInstance(120, 120, Image.SCALE_DEFAULT))));
		summonerChampIcons.add(convertCMYK2RGB(toBufferedImage(cropImage(screenshot, secondRect).getScaledInstance(120, 120, Image.SCALE_DEFAULT))));
		summonerChampIcons.add(convertCMYK2RGB(toBufferedImage(cropImage(screenshot, thirdRect).getScaledInstance(120, 120, Image.SCALE_DEFAULT))));
		summonerChampIcons.add(convertCMYK2RGB(toBufferedImage(cropImage(screenshot, fourthRect).getScaledInstance(120, 120, Image.SCALE_DEFAULT))));
		summonerChampIcons.add(convertCMYK2RGB(toBufferedImage(cropImage(screenshot, fifthRect).getScaledInstance(120, 120, Image.SCALE_DEFAULT))));

		return summonerChampIcons;
	}


	/** Compare part of the screenshot with League's champion icons. 
	 * Returns the int index of the best match from champIcons. 
	 * @param img2 locked-in champion icon. 
	 * @param champiIcons icons for all champions from Riot directory. 
	 * @return int[0] = index, int[1] = difference %*/
	public static int[] compareImages(ArrayList<BufferedImage> champIcons, BufferedImage img2){
		double bestDiff = 100;
		int position = 0;
		RescaleOp rescaleOp = new RescaleOp(2f, -20, null);
		BufferedImage img3 = copyImage(img2); //duplicate buffered image
		//Hovered champion icon version (need to increase brightness)
		rescaleOp.filter(img3, img3);
		File newFile = new File("C:\\Users\\Jake\\Pictures\\league\\ICON.jpg");
		try {
			ImageIO.write(img3, "jpg", newFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* Champion lock comparison (img1 and img2) */
		for(int i = 0; i < champIcons.size(); i++){
			BufferedImage img1 = champIcons.get(i);
			int width1 = img1.getWidth();
			int width2 = img2.getWidth();
			int height1 = img1.getHeight();
			int height2 = img2.getHeight();
			if ((width1 != width2) || (height1 != height2)) {
				System.err.println("Error: Images dimensions mismatch at " + i);
				System.exit(1);
			}
			long diff = 0;
			for (int y = 0; y < height1; y++) {
				for (int x = 0; x < width1; x++) {
					int rgb1 = img1.getRGB(x, y);
					int rgb2 = img2.getRGB(x, y);
					int r1 = (rgb1 >> 16) & 0xff;
					int g1 = (rgb1 >>  8) & 0xff;
					int b1 = (rgb1      ) & 0xff;
					int r2 = (rgb2 >> 16) & 0xff;
					int g2 = (rgb2 >>  8) & 0xff;
					int b2 = (rgb2      ) & 0xff;
					diff += Math.abs(r1 - r2);
					diff += Math.abs(g1 - g2);
					diff += Math.abs(b1 - b2);
				}
			}
			double n = width1 * height1 * 3;
			double p = diff / n / 255.0;
			if(p*100 < bestDiff){
				bestDiff = p*100;
				position = i;
			}
		}
		
		/* Champion hovered comparison (img1 and img3) */
		for(int i = 0; i < champIcons.size(); i++){
			BufferedImage img1 = champIcons.get(i);
			int width1 = img1.getWidth();
			int width2 = img3.getWidth();
			int height1 = img1.getHeight();
			int height2 = img3.getHeight();
			if ((width1 != width2) || (height1 != height2)) {
				System.err.println("Error: Images dimensions mismatch at " + i);
				System.exit(1);
			}
			long diff = 0;
			for (int y = 0; y < height1; y++) {
				for (int x = 0; x < width1; x++) {
					int rgb1 = img1.getRGB(x, y);
					int rgb2 = img3.getRGB(x, y);
					int r1 = (rgb1 >> 16) & 0xff;
					int g1 = (rgb1 >>  8) & 0xff;
					int b1 = (rgb1      ) & 0xff;
					int r2 = (rgb2 >> 16) & 0xff;
					int g2 = (rgb2 >>  8) & 0xff;
					int b2 = (rgb2      ) & 0xff;
					diff += Math.abs(r1 - r2);
					diff += Math.abs(g1 - g2);
					diff += Math.abs(b1 - b2);
				}
			}
			double n = width1 * height1 * 3;
			double p = diff / n / 255.0;
			if(p*100 < bestDiff){
				bestDiff = p*100;
				position = i;
			}
		}
		System.out.println("best difference " + bestDiff + " at " + position);
		int values[] = new int[2];
		values[0] = position;
		values[1] = (int) bestDiff;
		return values;
	}

	/** Crop a BufferedImage.
	 * @param src - source image to be cropped.
	 * @param rect - The crop coordinates.
	 * @return The cropped BufferedImage. */
	public static BufferedImage cropImage(BufferedImage src, Rectangle rect) {
		BufferedImage dest = src.getSubimage((int) rect.getX(), (int) rect.getY(), rect.width, rect.height);
		return dest; 
	}
	/** Convert an Image into BufferedImage (since casting not possible). 
	 * Posted by alpha02 at http://www.dreamincode.net/code/snippet1076.htm  */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage)
			return (BufferedImage)image;

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;

			if (hasAlpha == true)
				transparency = Transparency.BITMASK;

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();

			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) { } //No screen

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;

			if (hasAlpha == true) {type = BufferedImage.TYPE_INT_ARGB;}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}
	
	/** Create a copy of the given BufferedImage */
	public static BufferedImage copyImage(BufferedImage source){
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return b;
	}

	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage)
			return ((BufferedImage)image).getColorModel().hasAlpha();

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) { }

		// Get the image's color model
		return pg.getColorModel().hasAlpha();
	}

	/**This should hanldle the conversions wtih bufferedImages green tints from
	 * incorrect encoding. 
	 * http://stackoverflow.com/questions/2408613/unable-to-read-jpeg-image-using-imageio-readfile-file
	 * @param image in CMYK encoding
	 * @return image in RGB encoding 
	 * @throws IOException  */
	private static BufferedImage convertCMYK2RGB(BufferedImage image) throws IOException{
		//Create a new RGB image
		BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_3BYTE_BGR);
		// then do a funky color convert
		ColorConvertOp op = new ColorConvertOp(null);
		op.filter(image, rgbImage);
		return rgbImage;
	}


	public static void main(String[] args) throws TesseractException, IOException {
		ScreenOCR ocr = new ScreenOCR();
		File newFile = new File("C:\\Users\\Jake\\Pictures\\league\\untitled2.png");
		Image screenshot = ImageIO.read(newFile);
		ocr.screenSummoners(screenshot);
	}
}