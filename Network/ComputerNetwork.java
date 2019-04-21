/**
 * @author Ziyi Gong
 * A bidirectional graph for this network problem.
 */
import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.util.HashSet;

public class ComputerNetwork{
	public final int V;	// number of vertices
    public final int E; // number of edges
    public final boolean COPPER_ONLY; // if the network is copper-only
    public final boolean DISCONNECTED;
    private MST minAvgLatencySpanningTree;
    private LinkedList<Edge>[] adj; // adjacency list
    private LinkedList<Integer>[] articulationPairs;
    private int dfsTraversed; // count how many nodes have been traversed by dfs

    /**
	 * Initiate the network with a text file of the standard format
	 */
	public ComputerNetwork(String fname) throws java.io.FileNotFoundException{
		dfsTraversed = 0;
		Scanner f = new Scanner(new File(fname));
		boolean copperOnly = true;

		try{
			V = f.nextInt();
			f.nextLine(); // feed line

			articulationPairs = (LinkedList<Integer>[]) new LinkedList[V];
			adj = (LinkedList<Edge>[]) new LinkedList[V];

			int count = 0;
			while (f.hasNextLine()){
				String[] line = f.nextLine().split(" ");
				if (line.length != 5) continue;
				int v1 = Integer.parseInt(line[0]);
				int v2 = Integer.parseInt(line[1]);
				String type = line[2];
				int bandwidth = Integer.parseInt(line[3]);
				int length = Integer.parseInt(line[4]);
				Edge e = new Edge(v1, v2, type, bandwidth, length);
				add(this.adj, v1, e);
				add(this.adj, v2, e);
				if (type.equals("optical")) copperOnly = false;

				count++;
			} 

			COPPER_ONLY = copperOnly;
			E = count;
		} catch(Exception e){
			throw new IllegalArgumentException(
				"Network file not in standard format!");
		}

		minAvgLatencySpanningTree = new MST(adj);
		DISCONNECTED = minAvgLatencySpanningTree.DISCONNECTED;
	}

	/**
	 * Helper method to add to the adjacency list
	 * @param e to add
	 */
	private <T> void add(LinkedList<T>[] li, int index, T e){
		if (li[index] == null) li[index] = new LinkedList<>();
		li[index].add(e);
	}

	/**
	 * Find the lowest latency path between any two points
	 * @param from starting vertex
	 * @return the lowest latency path between the two
	 */
	public LLP lowestLatencyPath(int from){
		return new LLP(adj, from);
	}

	public MST getMST(){
		return minAvgLatencySpanningTree;
	}

	/**
	 * Find articulation pairs by reducing the problem to find articulation
	 * points if each vertex is removed.
	 * @return an adjacency list of vertex pairs (no duplicate)
	 */
	public LinkedList<Integer>[] findArticulationPairs(){
		LinkedList<Edge> maskLinkedList = new LinkedList<>(); 
		for (int v = 0; v < V; v++){
			LinkedList<Edge> vEdges = adj[v];
			adj[v] = maskLinkedList; // mask v's edges
			dfsTraversed = 0; // reset
			findArticulationPt(v); 
			adj[v] = vEdges; // de-mask v's edges
		}
		return articulationPairs;
	}

	/**
	 * Find articulation points of the graph
	 * @param removedV the vertex being "removed" 
	   problem; = 0 if the original problem is to find an articulation point
	 */
    private void findArticulationPt(int removedV){
    	final int V = this.V - 1;
    	if (dfsTraversed == V) return; // if all found before

		int[][] mark = new int[2][V + 1];
		LinkedList<Integer>[] edgeFrom = (LinkedList<Integer>[]) 
										new LinkedList[V + 1];
		LinkedList<Integer>[] backEdgeFrom = (LinkedList<Integer>[]) 
										new LinkedList[V + 1];
    	while (dfsTraversed != V) {
			int start = 0;
			for (; start < V; start++){
				if (mark[0][start] == 0 && start != removedV) 
					break; // start at an unvisited vertex
			}
			// num(v) start from 1
			dfs(start, 1, removedV, mark, edgeFrom, backEdgeFrom); 
			// no child
			if (edgeFrom[start] == null) return;
			// root has more than one children, a.pt
			if (edgeFrom[start].size() > 1 && 
				! hasDuplicatePair(removedV, start) &&
				! hasDuplicatePair(start, removedV)) 
				add(articulationPairs, removedV, start);
    	}
    }

    /**
	 * To do a depth-first traversal starting from a given vertex
	 * @param v the current vertex
	 * @param numV the current num traversed
	 * @param mark the low and num of a vertex
	 * @param edgeFrom the edge from the current vertex v to some other vertices
	 * @param backEdgeFrom the backedge from the current vertex to others
	 */
    private void dfs(int v, int numV, int removedV, int[][] mark, 
    				LinkedList<Integer>[] edgeFrom, 
    				LinkedList<Integer>[] backEdgeFrom){
    	dfsTraversed++;

		mark[0][v] = numV;
    	for (Edge e : adj[v]){
    		int w = e.other(v);
    		if (w == removedV) continue; // irrelevant vertex
    		if (mark[0][w] != 0){ // has num(v) means visited
    			add(backEdgeFrom, v, w); // + backedges
    			continue;
    		}
    		dfs(w, numV+1, removedV, mark, edgeFrom, backEdgeFrom);
    		add(edgeFrom, v, w); // + edges
    	}
    	// find low number
    	int low = numV;
    	if (edgeFrom[v] != null){
	    	for (Integer i : edgeFrom[v]){ // low = num(direct edge child)
	    		if (mark[1][i] < low && i != removedV) low = mark[1][i]; 
	    	}
    	}
    	if (backEdgeFrom[v] != null){
	    	for (Integer i : backEdgeFrom[v]){ // low = num(direct backedge child)
	    		if (mark[0][i] < low && i != removedV) low = mark[0][i]; 
	    	}
    	}

    	mark[1][v] = low;

    	if (edgeFrom[v] == null) return; // it is an end point, not a.pt for sure
    	for (Integer i : edgeFrom[v]){
	    	if (mark[1][i] >= numV && numV != 1 && i != removedV && 
	    		! hasDuplicatePair(v, removedV) && 
	    		! hasDuplicatePair(removedV, v)) 
	    		add(articulationPairs, removedV, v); // low >= num, a.pt
    	}
    }

    /**
     * Check if there exist a v2 already paired to v1. Order matters.
     * @param v1 the vertex in the first dimension of the adjacency list
     * @param v2 the vertex in the second dimension
     * @return true if the pair already exists
     */
    private boolean hasDuplicatePair(int v1, int v2){
    	if (articulationPairs[v1] == null) return false;
    	return articulationPairs[v1].contains(v2);
    }

    public String toString(){
		StringBuilder s = new StringBuilder();
		s.append("Number of vertices: " + V + "\tNumber of edges: " + E + "\n");
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (Edge e : adj[v]) {
                s.append("\t" + e.toString(v));
            }
            s.append("------------\n");
        }
        return s.toString();
	}
}