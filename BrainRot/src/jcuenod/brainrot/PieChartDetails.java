package jcuenod.brainrot;

public class PieChartDetails {
	private int ranking;
	private int count;
	
	public PieChartDetails(int ranking, int count)
	{
		this.ranking = ranking;
		this.count = count;
	}

	protected int getRanking() {
		return ranking;
	}

	protected void setRanking(int ranking) {
		this.ranking = ranking;
	}

	protected int getCount() {
		return count;
	}

	protected void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "PieChartDetails [ranking=" + ranking + ", count=" + count + "]";
	}
}
