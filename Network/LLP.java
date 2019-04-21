/**
 * @author Ziyi Gong
 * A lowest latency path (weighted shortest path)
 */
import java.util.PriorityQueue;
import java.util.LinkedList;

public class LLP{
	public final int START;
	private int[] latencyTo;
	private int[] via;
	private LinkedList<Edge>[] adj;

	/**
	 * @param adj the adjacency list of a graph
	 * @param start the starting vertex
	 */
	public LLP(LinkedList<Edge>[] adj, int start){
		if (start > adj.length || start < 0) 
			throw new IllegalArgumentException();

		START = start;
		this.adj = adj;

		int cur = start;
		// make the latency to all unvisited vertices maximum
		latencyTo = new int[adj.length];
		for (int i = 0; i < latencyTo.length; i++) 
			latencyTo[i] = Integer.MAX_VALUE;
		latencyTo[start] = 0;

		via = new int[adj.length];
		via[start] = start;
		
		boolean[] marked = new boolean[adj.length]; // whether visited or not

		PriorityQueue<int[]> pq = new PriorityQueue<>(adj.length, 
			(int[] a, int[] b) -> a[1] - b[1]); // lazy PQ

		//int[] tuple; // (vertex, latency to the vertex from the start)
		while (true){
			marked[cur] = true;
			for (Edge e : adj[cur]){
				int w = e.other(cur);
				assert w != -1;
				if (marked[w]) continue;
				if (latencyTo[w] > latencyTo[cur] + e.LATENCY){
					latencyTo[w] = latencyTo[cur] + e.LATENCY;
					via[w] = cur;
					int[] tuple = {w, latencyTo[w]};
					pq.add(tuple);
				}
			}

			try{
				int[] tuple;
				do {
					tuple = pq.poll();
				} while (marked[tuple[0]]);
				cur = tuple[0]; // next vertex
			} catch (NullPointerException e){ 
				return; // no more vertex in the connected part
			}
		}
	}

	/**
	 * Find the shortest path to the given vertex
	 * @param v end vertex
	 * @return an array of vertex, from start to end
	 */
	public LinkedList<Edge> to(int v){
		if (! isInPath(v)) return null;
		
		LinkedList<Edge> path = new LinkedList<>();
		int cur = v;

		do{
			int from = via[cur];
			for (Edge e : adj[cur]) {
				if (e.other(cur) == from) // find the edge
					path.addFirst(e);
			}
		} while ((cur = via[cur]) != START); // else, has added all edges

		return path;
	}

	/**
	 * Helper method to check if the vertex is in this LLP
	 * @return true if it is in, false otherwise
	 */
	private boolean isInPath(int v){
		if (v >= adj.length || v < 0) return false;
		return latencyTo[v] != Integer.MAX_VALUE;
	}

	public String toString(){
		StringBuilder s = new StringBuilder();
		s.append("Start from " + START + "\n------------\n");
        for (int v = 0; v < adj.length; v++){
        	if (v == START) continue;
            LinkedList<Edge> pathTo = to(v);
            s.append("To [" + v + "]: ");
            int cur = START;
            for (Edge e : pathTo) {
                s.append("\t" + e.toString(cur));
            	cur = e.other(cur);
            }
            s.append("------------\n");
        }
        return s.toString();
	}
}