/**
 * @author Ziyi Gong
 * A minimum average latency spanning tree (minimum spanning tree)
 */
import java.util.PriorityQueue;
import java.util.LinkedList;

public class MST{
	public final boolean DISCONNECTED;
	private LinkedList<Edge>[] adj;
	private boolean[] marked;
	private PriorityQueue<Edge> pq;

	/**
	 * @param adj the adjacency list of a graph
	 */
	public MST(LinkedList<Edge>[] adj){
		this.adj = (LinkedList<Edge>[]) new LinkedList[adj.length];
		for (int v = 0; v < adj.length; v++){
			this.adj[v] = new LinkedList<>();
		}

    	pq = new PriorityQueue<Edge>(adj.length, 
    		Edge::compareTo); // edges PQ
    	// marked[v] = true iff v on tree
    	marked = new boolean[adj.length]; 
    	int count = 0; // counting edges

        for (int v = 0; v < adj.length; v++){
            if (marked[v]) continue;
        	scan(adj, v);
            
            // add to the tree
            Edge e;
            while ((e = pq.poll()) != null){ // not empty
            	if (marked[e.V1] && marked[e.V2]) continue;
            	this.adj[e.V1].add(e);
		   		this.adj[e.V2].add(e);
		   		count++;
            	if (! marked[e.V1]) scan(adj, e.V1);
            	if (! marked[e.V2]) scan(adj, e.V2);
            }
        }

        if (count != adj.length - 1) DISCONNECTED = true;
        else DISCONNECTED = false;
	}

	/**
	 * Helper method to scan the edges of a vertex
	 * @param v the vertex to scan
	 */
	private void scan(LinkedList<Edge>[] adj, int v){
		marked[v] = true;
		for (Edge e : adj[v]){ // scan all edges for a vertex
            if (! marked[e.other(v)]) pq.add(e);
		}
	}

	public String toString(){
		if (DISCONNECTED) return "The graph is disconnected!"; 
		StringBuilder s = new StringBuilder();
		int avgLatency = 0;
		int count = 0;
        for (int v = 0; v < adj.length; v++) {
            s.append("[" + v + "]: ");
            for (Edge e : adj[v]) {
                s.append("\t" + e.toString(v));
                avgLatency += e.LATENCY;
            }
            s.append("------------\n");
            count += adj[v].size();
        }
        s.append("Average Latency: " + avgLatency/count + "E-12 s\n");
        return s.toString();
	}
}