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
import net.rithms.riot.api.*;
import net.rithms.riot.constant.Region;
import net.rithms.riot.dto.Summoner.Summoner;
import net.sourceforge.tess4j.TesseractException;
import net.rithms.riot.dto.ChampionMastery.ChampionMastery;
import net.rithms.riot.dto.League.League;
import net.rithms.riot.dto.League.LeagueEntry;
import net.rithms.riot.dto.Static.*;
import net.rithms.riot.dto.Static.Champion;
import net.rithms.riot.dto.Stats.AggregatedStats;
import net.rithms.riot.dto.Stats.ChampionStats;
import net.rithms.riot.dto.Stats.PlayerStatsSummary;
import net.rithms.riot.dto.Stats.PlayerStatsSummaryList;
import net.rithms.riot.dto.Stats.RankedStats;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.constant.PlatformId;

public class LeagueData {
	private static final RiotApi api = new RiotApi("NEW_KEY");
	public ChampionList champsList = new ChampionList();
	public static AggregatedStats summonerStats = new AggregatedStats();
	public static Map<String, Champion> champsMap;
	public ArrayList<BufferedImage> champIcons; 
	public ArrayList<BufferedImage> champSplashArt; 
	public  ArrayList<String> champNames; // All champion names
	public  ArrayList<String> summonerChampNames;
	public  ArrayList<BufferedImage> summonerChampIcons;
	public  ArrayList<Integer> summonerChampIndices;
	public  ArrayList<Integer> summonerChampAccuracies;
	public  ArrayList<String> summonerNames;
	public static ScreenOCR ocr = new ScreenOCR();
	public static String riotDirectory;

	/** Initialize variables 
	 * @throws IOException 
	 * @throws TesseractException */
	public LeagueData(String riotDir) throws RiotApiException{
		riotDirectory = riotDir;
		this.champsList = 		api.getDataChampionList(); //get list of all champions
		LeagueData.champsMap =	champsList.getData();
		this.champIcons = 		getChampIcons();
		this.champSplashArt = 	getChampSplashArt();
		this.champNames = 		getChampOrder();
	}

	/** Updates summonerChampIcons and summonerNames with values from the screenshot */
	@SuppressWarnings("static-access")
	public void screenImage(java.awt.Image screenshot) throws IOException, TesseractException{
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
			this.summonerChampAccuracies.add(100 - values[1]);
		}
		for(int location : summonerChampIndices){
			this.summonerChampNames.add(champNames.get(location));
			System.out.println(champNames.get(location));
		}
	}


	/** Gets an ArrayList of all League champion Icons. Used to compare with screenshot */
	public static ArrayList<BufferedImage> getChampIcons(){
		String location = riotDirectory + "/League of Legends/RADS/projects/lol_air_client/releases/";
		File directory = new File(location);
		File[] filelist = directory.listFiles(); //get dynamic version folder between projects and assets.
		if(filelist != null){
			location = filelist[0] + "/deploy/assets/images/champions/";
		}else{
			return null;
		}
		ArrayList<BufferedImage> champIcons = new ArrayList<BufferedImage>();

		for (Map.Entry<String, Champion> entry : champsMap.entrySet()) {
			BufferedImage image;
			try {
				image = ImageIO.read(new File(location + entry.getKey() + "_Square_0.png"));
			} catch (IOException e) {
				System.out.print("Error: Image file not found for " + entry.getKey());
				return null;
			}
			champIcons.add(image);
		}
		return champIcons;
	}

	/**
	 * Return a bunch of info on one summoner and their champ stats.
	 * @param name
	 * @param champion
	 * @return SummonerInfo 				*/
	public SummonerInfo getSummonerData(String name, String champion){
		if(name.equals("") && champion.equals("")){ // Empty check
			return null;
		}
		PlatformId platformid = PlatformId.NA;
		if(api.getRegion().toString().equalsIgnoreCase("na")){
			platformid = PlatformId.NA;
		}else if(api.getRegion().toString().equalsIgnoreCase("euw")){
			platformid = PlatformId.EUW;
		}else if(api.getRegion().toString().equalsIgnoreCase("eune")){
			platformid = PlatformId.EUNE;
		}else if(api.getRegion().toString().equalsIgnoreCase("kr")){
			platformid = PlatformId.KR;
		}else if(api.getRegion().toString().equalsIgnoreCase("lan")){
			platformid = PlatformId.LAN;
		}else if(api.getRegion().toString().equalsIgnoreCase("las")){
			platformid = PlatformId.LAS;
		}else if(api.getRegion().toString().equalsIgnoreCase("oce")){
			platformid = PlatformId.OCE;
		}else if(api.getRegion().toString().equalsIgnoreCase("pbe")){
			platformid = PlatformId.PBE;
		}else if(api.getRegion().toString().equalsIgnoreCase("ru")){
			platformid = PlatformId.RU;
		}else if(api.getRegion().toString().equalsIgnoreCase("tr")){
			platformid = PlatformId.TR;
		}
		Champion champ = champsMap.get(champion);
		Summoner summoner;
		SummonerInfo summonerinfo = new SummonerInfo();
		try{
			summoner = api.getSummonerByName(name);
		}catch(RiotApiException e){
			System.out.println("Riot api exception: " + e.getErrorCode() + e.getMessage());
			summonerinfo.errorcode = e.getErrorCode();
			e.printStackTrace();
			return summonerinfo; // SUMMONER NOT FOUND
		}
		//RankedStats stats1 = api.getRankedStats(summoner.getId());
		/* Get summoner rank, division, and LP */
		List<League> leagues;
		try{
			leagues = api.getLeagueBySummoner(summoner.getId());

		}catch(RiotApiException e){
			summonerinfo.errorcode = e.getErrorCode();
			return summonerinfo; 
		}
		League league = leagues.get(0);
		List<LeagueEntry> entries = league.getEntries();
		LeagueEntry entry = new LeagueEntry();
		for(int i = 0; i < entries.size(); i++){
			if(Integer.valueOf((entries.get(i).getPlayerOrTeamId()))== summoner.getId()){
				entry = entries.get(i);		
			}
		}
		summonerinfo.summonername = name;
		summonerinfo.champname = champion;
		summonerinfo.ranktier = league.getTier();
		summonerinfo.rankdivision = entry.getDivision();
		summonerinfo.ranklp = entry.getLeaguePoints();
		System.out.println(league.getTier() + " " + entry.getDivision() + " " + entry.getLeaguePoints());
		try{
			ChampionMastery mastery = api.getChampionMastery(platformid, summoner.getId(), champ.getId());
			summonerinfo.champmasterylevel = mastery.getChampionLevel();
			summonerinfo.champmasterypoints = mastery.getChampionPoints();

		}catch(RiotApiException e){
			System.out.println("masteries not found?" + e.getErrorCode());
			e.printStackTrace();
			summonerinfo.errorcode = e.getErrorCode();
			return summonerinfo; 
		}catch(NullPointerException e){
			summonerinfo.champmasterylevel = 0;
			summonerinfo.champmasterypoints = 0;
		}
		try{
			AggregatedStats champstats = new AggregatedStats();
			RankedStats stats = api.getRankedStats(summoner.getId());
			List<ChampionStats> championStats = stats.getChampions();
			for(ChampionStats champstat: championStats){
				if(champstat.getId() == champ.getId()){
					champstats = champstat.getStats();
				}
			}
			summonerinfo.champgameswon = champstats.getTotalSessionsWon();
			summonerinfo.champgameslost = champstats.getTotalSessionsLost();
			summonerinfo.champgamestotal = champstats.getTotalSessionsPlayed();
			summonerinfo.champavgassists = champstats.getAverageAssists();
			summonerinfo.champavgkills = champstats.getAverageChampionsKilled();
			summonerinfo.champavgdeaths = champstats.getAverageNumDeaths();
		}catch(RiotApiException e){
			System.out.println("api exception for champstats " + e.getErrorCode());
			e.printStackTrace();
			summonerinfo.errorcode = e.getErrorCode();
			return summonerinfo; 
		}
		try{
			PlayerStatsSummaryList statssumlist = api.getPlayerStatsSummary(summoner.getId());
			List<PlayerStatsSummary> stats = statssumlist.getPlayerStatSummaries();
			PlayerStatsSummary statsum = null;
			for(PlayerStatsSummary s : stats){
				if(s.getPlayerStatSummaryType().equalsIgnoreCase("RankedSolo5x5")){
					statsum = s;
				}
			}
			summonerinfo.rankedtotalwins = statsum.getWins();
			summonerinfo.rankedtotallosses = statsum.getLosses();
			AggregatedStats rankedstats = statsum.getAggregatedStats();
		}catch(RiotApiException e){
			System.out.println("api exception for statsumlist " +e.getErrorCode());
			e.printStackTrace();
			summonerinfo.errorcode = e.getErrorCode();
			return summonerinfo; 
		}
		System.out.println("Successful summonerinfo");
		return summonerinfo;
	}

	/** Gets an ArrayList of all League splash arts. Used for GUI visuals */
	public static ArrayList<BufferedImage> getChampSplashArt(){
		String location = riotDirectory + "/League of Legends/RADS/projects/lol_air_client/releases/";
		File directory = new File(location);
		File[] filelist = directory.listFiles(); //get dynamic version folder between projects and assets.
		if(filelist != null){
			location = filelist[0] + "/deploy/assets/images/champions/";
		}else{
			return null;
		}

		ArrayList<BufferedImage> champIcons = new ArrayList<BufferedImage>();
		for (Map.Entry<String, Champion> entry : champsMap.entrySet()) {
			BufferedImage image;
			try {
				image = ImageIO.read(new File(location + entry.getKey() + "_Splash_Centered_0.jpg"));
				image = convertCMYK2RGB(toBufferedImage(image.getSubimage( 200, 150, 800, 150).getScaledInstance(600, 100, Image.SCALE_SMOOTH)));

			} catch (IOException e) {
				System.out.print("Error: Image file not found for " + entry.getKey());
				return null;
			}
			champIcons.add(image);
		}
		return champIcons;
	}

	public static javafx.scene.image.Image getChampSplashArt(String champion){
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

		//image = convertCMYK2RGB(toBufferedImage(image.getSubimage( 200, 150, 800, 150).getScaledInstance(600, 100, Image.SCALE_SMOOTH)));
		WritableImage croppedImage = new WritableImage(image.getPixelReader(), 200, 150, 800, 200);
		croppedImage = scale(croppedImage, 600, 136, false);
		return croppedImage;
	}

	/** Return an arraylist of the champion names */
	public static ArrayList<String> getChampOrder(){
		ArrayList<String> champOrder = new ArrayList<String>();
		for (Map.Entry<String, Champion> entry : champsMap.entrySet()) {
			champOrder.add(entry.getKey());
		}
		return champOrder;
	}

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

	/**
	 * Scale a java scene image
	 * @param source
	 * @param targetWidth
	 * @param targetHeight 
	 * @param preserveRatio preserve param for imageview
	 * @return
	 */
	public static WritableImage scale(WritableImage source, int targetWidth, int targetHeight, boolean preserveRatio) {
		ImageView imageView = new ImageView(source);
		imageView.setSmooth(true);
		imageView.setPreserveRatio(preserveRatio);
		imageView.setFitWidth(targetWidth);
		imageView.setFitHeight(targetHeight);
		return imageView.snapshot(null, null);
	}


}
