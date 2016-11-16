package application;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import net.sourceforge.tess4j.TesseractException;

import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.api.LoadPolicy;
import com.robrua.orianna.type.api.RateLimit;
import com.robrua.orianna.type.core.championmastery.ChampionMastery;
import com.robrua.orianna.type.core.league.League;
import com.robrua.orianna.type.core.league.LeagueEntry;
import com.robrua.orianna.type.core.staticdata.Champion;
import com.robrua.orianna.type.core.stats.AggregatedStats;
import com.robrua.orianna.type.core.stats.ChampionStats;
import com.robrua.orianna.type.core.stats.PlayerStatsSummary;
import com.robrua.orianna.type.core.stats.PlayerStatsSummaryType;
import com.robrua.orianna.type.core.summoner.Summoner;
import com.robrua.orianna.type.exception.APIException;

public class LeagueData {
	public List<Champion> champsList;
	public static AggregatedStats summonerStats;
	public static Map<String, Champion> champsMap;
	public ArrayList<BufferedImage> champIcons = new ArrayList<BufferedImage>(); 
	public ArrayList<BufferedImage> champSplashArt = new ArrayList<BufferedImage>(); 
	public static ArrayList<String> champNames; // All champion names
	public  ArrayList<String> summonerChampNames;
	public  ArrayList<BufferedImage> summonerChampIcons;
	public  ArrayList<Integer> summonerChampIndices;
	public  ArrayList<Integer> summonerChampAccuracies;
	public  ArrayList<String> summonerNames;
	public static ScreenOCR ocr = new ScreenOCR();
	public static String riotDirectory;


	/** Constructor - Initialize variables 
	 * @throws IOException 
	 * @throws TesseractException */
	public LeagueData(String riotDir, BufferedImage blankImg){
		RiotAPIKey.setKey();
		RiotAPI.setLoadPolicy(LoadPolicy.UPFRONT);
		RiotAPI.setRateLimit(new RateLimit(25, 10), new RateLimit(500, 600));
		riotDirectory = riotDir;
		LeagueData.champNames = 		getChampOrder();
		this.champsList = 		RiotAPI.getChampions(); //get list of all champions
		this.champIcons = 		getChampIcons(blankImg);
	}

	/** Updates summonerChampIcons and summonerNames with values from the screenshot 
	 * @return double average difference value in comparisons (lower = better) */
	@SuppressWarnings("static-access")
	public double screenImage(java.awt.Image screenshot) throws IOException, TesseractException{
		this.summonerChampIcons = 		ocr.screenChampions(screenshot);
		this.summonerNames = 			ocr.screenSummoners(screenshot);
		this.summonerChampNames = 		new  ArrayList<String>();
		this.summonerChampIndices = 	new  ArrayList<Integer>();
		this.summonerChampAccuracies = 	new  ArrayList<Integer>();


		/* Compare each summoner champion icon to the champIcons for best match */
		System.out.println("champ icons: " + summonerChampIcons.size());
		for(BufferedImage image : summonerChampIcons){ 
			int[] values = ScreenOCR.compareImages(champIcons, image);
			this.summonerChampIndices.add(values[0]);
			this.summonerChampAccuracies.add(values[1]);
		}
		for(int location : summonerChampIndices){
			if(location >= champNames.size()){ //empty value
				this.summonerChampNames.add(null);
			}else{
				this.summonerChampNames.add(champNames.get(location));
				System.out.println(champNames.get(location));
			}
		}

		double avgDiff = 0;
		for(double val : summonerChampAccuracies){ // get average image difference
			avgDiff+= val;
		}
		avgDiff = avgDiff / 5;	
		return avgDiff;
	}


	/** Gets an ArrayList of all League champion Icons. Used to compare with screenshot */
	public static ArrayList<BufferedImage> getChampIcons(BufferedImage blankimg){
		String location = riotDirectory + "/League of Legends/RADS/projects/lol_air_client/releases/";
		File directory = new File(location);
		File[] filelist = directory.listFiles(); //get all version folders between projects and assets.
		int index = getLatestModified(filelist);
		if(index != -1){
			location = filelist[index] + "/deploy/assets/images/champions/";
		}else{
			System.out.println("version not found");
			return null;
		}
		System.out.println("Champ icons dir: " + location);
		ArrayList<BufferedImage> champIcons = new ArrayList<BufferedImage>();

		for (String entry : LeagueData.champNames) {
			entry = entry.replace("'", "").replace(" ", "").replace(".", "").replace("Wukong",  "MonkeyKing");
			BufferedImage image;
			try {
				image = ImageIO.read(new File(location + entry + "_Square_0.png"));
			} catch (IOException e) {
				System.out.println("Image file not found for " + entry + "_Square_0.png");
				System.out.println("Method: getChampIcons");
				return null;
			}
			champIcons.add(image);
		}
		champIcons.add(blankimg);
		return champIcons;
	}

	/**
	 * Return a bunch of info on one summoner and their champ stats.
	 * @param name
	 * @param champion
	 * @return SummonerInfo 				*/
	public SummonerInfo getSummonerData(String name, String champion){
		long startTime = System.currentTimeMillis();

		if(name.equals("") && champion.equals("")){ // Empty check
			System.out.println("getSummonerData: Nothing here");
			return null;
		}
		Summoner summoner = null;
		SummonerInfo summonerinfo = new SummonerInfo();

		Champion champ = null;
		for(Champion ch : champsList){
			if(ch.getName().equalsIgnoreCase(champion)){
				champ = ch;
			}
		}
		try{
			summoner = RiotAPI.getSummonerByName(name);
		}catch(APIException e){
			summonerinfo.errorcode = ((APIException) e).getStatus().name();
			return summonerinfo;
		}

		/* Get Ranked wins and losses */
		try{
			Map<PlayerStatsSummaryType, PlayerStatsSummary> statssummap = summoner.getStats();
			PlayerStatsSummary statsummary = statssummap.get(PlayerStatsSummaryType.RankedSolo5x5);
			summonerinfo.rankedtotalwins = statsummary.getWins();
			summonerinfo.rankedtotallosses = statsummary.getLosses();
		}catch(IllegalArgumentException e){
			e.printStackTrace();
			return summonerinfo;
		}

		/* Get summoner rank, division, and LP */
		List<League> leagues = null;
		try{
			leagues = summoner.getLeagueEntries();
		}catch(APIException e){
			summonerinfo.errorcode = ((APIException) e).getStatus().name();
			return summonerinfo;
		}
		League league = leagues.get(0);
		List<LeagueEntry> entries = league.getEntries();
		LeagueEntry entry = null;
		for(int i = 0; i < entries.size(); i++){
			if(Integer.valueOf((entries.get(i).getID()))== summoner.getID()){
				entry = entries.get(i);		
			}
		}
		summonerinfo.summonername = name;
		summonerinfo.champname = champion;
		summonerinfo.ranktier = league.getTier().name();
		summonerinfo.rankdivision = entry.getDivision();
		summonerinfo.ranklp = entry.getLeaguePoints();

		/* Get champion specific information on the summoner */
		try{
			if(!champion.equalsIgnoreCase("")){
				ChampionMastery mastery = null;
				try{
					mastery = summoner.getChampionMastery(champ);
					summonerinfo.champmasterylevel = mastery.getChampionLevel();
					summonerinfo.champmasterypoints = mastery.getChampionPoints();
				}catch(IllegalArgumentException e){
					summonerinfo.champmasterylevel = 0;
					summonerinfo.champmasterypoints = 0;
				}
			}else{
				return summonerinfo;
			}
		}catch(APIException e){
			summonerinfo.errorcode = ((APIException) e).getStatus().name();
			return summonerinfo;
		}

		Map<Champion, ChampionStats> stats = summoner.getRankedStats();
		ChampionStats champstats = stats.get(champ);
		if(champstats == null){
			return summonerinfo;
		}
		summonerinfo.champgameswon = champstats.getStats().getTotalWins();
		summonerinfo.champgameslost = champstats.getStats().getTotalLosses();
		summonerinfo.champgamestotal = champstats.getStats().getTotalGamesPlayed();
		summonerinfo.champavgassists = champstats.getStats().getAverageAssists();
		summonerinfo.champavgkills = champstats.getStats().getAverageKills();
		summonerinfo.champavgdeaths = champstats.getStats().getAverageDeaths();
		System.out.println("Successful summonerinfo");

		long endTime = System.currentTimeMillis();

		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
		System.out.println("Data Fetch: " + duration + " ms, " + duration / 1000 + "sec");
		return summonerinfo;
	}

	/** Gets an ArrayList of all League splash arts. Used for GUI visuals 
	public static ArrayList<BufferedImage> getChampSplashArt(){
		String location = riotDirectory + "/League of Legends/RADS/projects/lol_air_client/releases/";
		File directory = new File(location);
		File[] filelist = directory.listFiles(); //get dynamic version folder between projects and assets.
		int index = getLatestModified(filelist);
		if(index != -1){
			location = filelist[index] + "/deploy/assets/images/champions/";
		}else{
			System.out.println("version not found");
			return null;
		}

		ArrayList<BufferedImage> champIcons = new ArrayList<BufferedImage>();
		for (String name : LeagueData.champNames) {
			BufferedImage image;
			try {
				image = ImageIO.read(new File(location + name.replace(" ", "") + "_Splash_Centered_0.jpg"));
				image = convertCMYK2RGB(toBufferedImage(image.getSubimage( 200, 150, 800, 100).getScaledInstance(800, 100, Image.SCALE_SMOOTH)));

			} catch (IOException e) {
				System.out.println("Error: Image file not found for " + name.replace(" ", "") + "_Splash_Centered_0.jpg");
				System.out.println("Method: getChampSplashArt");
				return null;
			}
			champIcons.add(image);
		}
		return champIcons;
	}*/

	/** Return a splash art image for the given champion */
	public static javafx.scene.image.Image getChampSplashArt(String champion){
		/* Format champion String for Riot file name conventions */
		champion = champion.replace("'", "").replace(" ", "").replace(".", "");
		if(champion.equalsIgnoreCase("wukong")){
			champion = "MonkeyKing";
		}
		String location = riotDirectory + "/League of Legends/RADS/projects/lol_air_client/releases/";
		File directory = new File(location);
		File[] filelist = directory.listFiles(); //get dynamic version folder between projects and assets.
		if(filelist != null){
			location = "file:" + filelist[0] + "/deploy/assets/images/champions/";
		}else{
			return null;
		}
		String url = location + champion + "_Splash_Centered_0.jpg";
		System.out.println(url);
		javafx.scene.image.Image image;
		image = new javafx.scene.image.Image (url);

		WritableImage croppedImage = new WritableImage(image.getPixelReader(), 100, 150, 1100, 175);
		croppedImage = scale(croppedImage, 800, 100, false);
		return croppedImage;
	}

	/** Return an arraylist of the champion names */
	public static ArrayList<String> getChampOrder(){
		ArrayList<String> champOrder = new ArrayList<String>();
		for (Champion champ : RiotAPI.getChampions()) {
			String name = champ.getName();
			champOrder.add(name);
		}

		champOrder.sort(String::compareToIgnoreCase); //
		return champOrder;
	}

	/** Determine the index in an array of files[] of the most recently modified file
	 * by comparing the lastModified property from all files in the array.
	 * @param  fileList Array of files from a desired directory
	 * @return Integer index of most recently modified file/directory.
	 * Returns -1 if the File[] array is null. */
	public static int getLatestModified(File[] fileList){
		if(fileList!= null){
			long latestVersion = 0;
			int index = 0;
			for(int i = 0; i < fileList.length; i++){
				File currentDir = fileList[i];
				if(currentDir.lastModified() > latestVersion){
					latestVersion = currentDir.lastModified();
					index = i;
				}
			}
			return index;
		}else{
			System.out.println("Error: fileList is null.");
			return -1;
		}
	}

	/** Convert a java.awt.image into a java.awt.bufferedimage */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage)
			return (BufferedImage)image;		// This code ensures that all the pixels in the image are loaded

		image = new ImageIcon(image).getImage(); 		// Determine if the image has transparent pixels
		boolean hasAlpha = hasAlpha(image);
		BufferedImage bimage = null;		// Create a buffered image with a format that's compatible with the screen
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

		Graphics g = bimage.createGraphics();		// Copy image to buffered image
		g.drawImage(image, 0, 0, null); 		// Paint the image onto the buffered image
		g.dispose();
		return bimage;
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
	@SuppressWarnings("unused")
	private static BufferedImage convertCMYK2RGB(BufferedImage image) throws IOException{
		//Create a new RGB image
		BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_3BYTE_BGR);
		// then do a funky color convert
		ColorConvertOp op = new ColorConvertOp(null);
		op.filter(image, rgbImage);
		return rgbImage;
	}

	/**
	 * Scale a java scene image
	 * @param source
	 * @param targetWidth
	 * @param targetHeight 
	 * @param preserveRatio preserve param for imageview 	 */
	public static WritableImage scale(WritableImage source, int targetWidth, int targetHeight, boolean preserveRatio) {
		ImageView imageView = new ImageView(source);
		imageView.setSmooth(true);
		imageView.setPreserveRatio(preserveRatio);
		imageView.setFitWidth(targetWidth);
		imageView.setFitHeight(targetHeight);
		return imageView.snapshot(null, null);
	}


	/** Return an integer value (low rank to high rank) for a rank string. */
	public int tierToInt(String rank){
		int n = -1;
		if(rank.equalsIgnoreCase("provisional")){
			n = 0;
		}else if(rank.equalsIgnoreCase("bronze")){
			n = 1;
		}else if(rank.equalsIgnoreCase("silver")){
			n = 2;
		}else if(rank.equalsIgnoreCase("gold")){
			n = 3;
		}else if(rank.equalsIgnoreCase("platinum")){
			n = 4;
		}else if(rank.equalsIgnoreCase("diamond")){
			n = 5;
		}else if(rank.equalsIgnoreCase("master")){
			n = 6;
		}else if(rank.equalsIgnoreCase("challenger")){
			n = 7;
		}
		return n;
	}
	/** Return an string value (low rank to high rank) for a int rank (1-7). */
	public String intToTier(int n){
		String rank = "";
		if(n == 0){
			rank = "provisional";
		}else if(n == 1){
			rank = "bronze";
		}else if(n == 2){
			rank = "silver";
		}else if(n == 3){
			rank = "gold";
		}else if(n == 4){
			rank = "platinum";
		}else if(n == 5){
			rank = "diamond";
		}else if(n == 6){
			rank = "master";
		}else if(n == 7){
			rank = "challenger";
		}
		return rank;
	}
	/** Return an integer value (1-5) from roman numeral rank. */
	public int romanToInt(String roman){
		int n = 0;
		if(roman.equalsIgnoreCase("i")){
			n = 1;
		}else if(roman.equalsIgnoreCase("ii")){
			n = 2;
		}else if(roman.equalsIgnoreCase("iii")){
			n = 3;
		}else if(roman.equalsIgnoreCase("iv")){
			n = 4;
		}else if(roman.equalsIgnoreCase("v")){
			n = 5;
		}
		return n;
	}

	/** Return a roman numeral rank from integer value (1-5). */
	public String intToRoman(int n){
		String roman = "";
		if(n == 1){
			roman = "I";
		}else if(n == 2){
			roman = "II";
		}else if(n == 3){
			roman = "III";
		}else if(n == 4){
			roman = "IV";
		}else if(n == 5){
			roman = "V";
		}
		return roman;
	}
}
