package jcuenod.brainrot;

public class FlashCard {
	public static long [] PIMSLEUR_TIMINGS = {
		100, //0 seconds - you're supposed to get it right at least once to graduate...
		5 * 1000L, //5 seconds
		25 * 1000L, //25 seconds
		2 * 60 * 1000L, //2 minutes
		10 * 60 * 1000L, //10 minutes
		1 * 60 * 60 * 1000L, //1 hour
		5 * 60 * 60 * 1000L, //5 hours
		1 * 24 * 60 * 60 * 1000L, //1 day
		5 * 24 * 60 * 60 * 1000L, //5 days
		25 * 24 * 60 * 60 * 1000L, //25 days
		4 * 30 * 24 * 60 * 60 * 1000L, //4 months
		2 * 365 * 24 * 60 * 60 * 1000L //2 years
	};
	public static String [] PIMSLEUR_TIMINGS_STRING = {
		"",
		"5 seconds",
		"25 seconds",
		"2 minutes",
		"10 minutes",
		"1 hour",
		"5 hours",
		"1 day",
		"5 days",
		"25 days",
		"4 months",
		"2 years",
	};
	public static final int MAX_RANKING = PIMSLEUR_TIMINGS.length;
	
	private int cardId;
	private String sideOne;
	private String sideTwo;
	private int displayCount;
	private long lastSeen;
	private long nextDue;
	private int ranking;
	
	public FlashCard(int cardId, String sideOne, String sideTwo, int displayCount, long lastSeen, long nextDue, int ranking)
	{
		this.cardId = cardId;
		this.sideOne = sideOne;
		this.sideTwo = sideTwo;
		this.displayCount = displayCount;
		this.ranking = ranking;
		this.lastSeen = lastSeen;
		this.nextDue = nextDue;
	}

	protected int getCardId() {
		return cardId;
	}

	protected void setCardId(int cardId) {
		this.cardId = cardId;
	}

	protected String getSideOne() {
		return sideOne;
	}

	protected void setSideOne(String sideOne) {
		this.sideOne = sideOne;
	}

	protected String getSideTwo() {
		return sideTwo;
	}

	protected void setSideTwo(String sideTwo) {
		this.sideTwo = sideTwo;
	}

	protected int getDisplayCount() {
		return displayCount;
	}

	protected void setDisplayCount(int displayCount) {
		this.displayCount = displayCount;
	}
	public void incrementDisplayCount()
	{
		this.displayCount++;
	}

	protected int getRanking() {
		return ranking;
	}

	protected void setRanking(int ranking) {
		this.ranking = ranking;
	}
	
	protected void promote()
	{
		promote(true);
	}
	protected void promote(boolean promotion)
	{
		if ((ranking < (MAX_RANKING -1) && promotion) || (ranking > 0 && !promotion))
		{
			ranking += promotion ? +1 : -1;
		}
	}

	protected long getLastSeen() {
		return lastSeen;
	}

	protected void setLastSeen(long lastSeen) {
		this.lastSeen = lastSeen;
	}
	
	protected long getNextDue() {
		return nextDue;
	}

	protected void setNextDue(long nextDue) {
		this.nextDue = nextDue;
	}
}