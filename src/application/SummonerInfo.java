package application;

/**
 * Holds data for a summoner and the champion they're playing.
 * @author Jacob Webber
 */
public class SummonerInfo {
	public String errorcode = "";
	public String summonername = "";
	public String champname = "";
	public String ranktier = "";
	public String rankdivision = "";
	public int ranklp = 0;
	public int champmasterylevel = 0;
	public long champmasterypoints = 0;
	public int champgameswon = 0;
	public int champgameslost = 0;
	public int champgamestotal = 0;
	public int champavgassists = 0;
	public int champavgkills = 0;
	public int champavgdeaths = 0;
	public int rankedtotalwins = 0;
	public int rankedtotallosses = 0;
	public SummonerInfo(){
		this.errorcode = "";
	}
	/** Print out the summoner variables */
	public void printinfo(){
		System.out.println("-----------------------------");
		System.out.println("name: " + summonername + "champ: " + champname);
		System.out.println("Rank: " + ranktier + " " + rankdivision + " " + ranklp);
		System.out.println("mastery: " + champmasterylevel + " " + champmasterypoints);
		System.out.println("games: " + champgameswon + " / " + champgameslost + " : " + champgamestotal);
		System.out.println("average kills: " + champavgkills + " deaths: " + champavgdeaths);
		System.out.println("total games: " + rankedtotalwins + " / " + rankedtotallosses);
		System.out.println("-----------------------------");
	}
}
