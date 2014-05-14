package jcuenod.brainrot;

public class BubbleChartDetails {
	int ranking;
	int displayCount;
	int counter;
	public BubbleChartDetails(int ranking, int displayCount, int counter)
	{
		this.ranking = ranking;
		this.displayCount = displayCount;
		this.counter = counter;
	}
	protected int getRanking() {
		return ranking;
	}
	protected void setRanking(int ranking) {
		this.ranking = ranking;
	}
	protected int getDisplayCount() {
		return displayCount;
	}
	protected void setDisplayCount(int displayCount) {
		this.displayCount = displayCount;
	}
	protected int getCounter() {
		return counter;
	}
	protected void setCounter(int counter) {
		this.counter = counter;
	}
	
	@Override
	public String toString() {
		return "BubbleChartDetails [ranking=" + ranking + ", displayCount="
				+ displayCount + ", counter=" + counter + "]";
	}
}
