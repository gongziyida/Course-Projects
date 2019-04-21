/**
 * @author Ziyi Gong
 * A bidirectional graph for this network problem.
 */
public class Edge implements Comparable<Edge>{
	public final int V1;
	public final int V2;
	public final String TYPE;
	public final int BANDWIDTH;
	public final int LENGTH;
	public final int LATENCY;

	public Edge(int v, int w, String type, int bandwidth, int length){
		V1 = v;
		V2 = w;
		TYPE = type;
		BANDWIDTH = bandwidth;
		LENGTH = length;
		int speed = 0;
		if (type.equals("copper")) speed = 230000000;
		else if (type.equals("optical")) speed = 200000000;
		else throw new IllegalArgumentException("Unsupported link type!");
		LATENCY = LENGTH * 10^12 / speed; // latency in ps
	}

	/**
	 * Returns the vertex other than the input vertex
	 * @param v the input vertex
	 */
	public int other(int v){
		if (v == V1) return V2;
		else if (v == V2) return V1;
		return -1;
	}

	@Override
	public int compareTo(Edge e){
		return LATENCY - e.LATENCY;
	}

	public String toString(int v){
		String s = "--> [%d]: type = %s bandwidth = %d GB/s length = %d m\n";
		return String.format(s, other(v), TYPE, BANDWIDTH, LENGTH);
	}
}