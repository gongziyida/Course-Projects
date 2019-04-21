/**
 * @author Ziyi Gong
 * Network analysis user interface
 */
import java.util.LinkedList;
import java.util.Scanner;

public class NetworkAnalysis{
	private static Scanner keyboard = new Scanner(System.in);
	private static LLP[] llps;

	/**
	 * Get the valid part of the user's input
	 */
	private static int input(String prompt){
		System.out.print(prompt);
		try{
			int in = keyboard.nextInt();
			return in;
		} catch (java.util.InputMismatchException e){
			keyboard.nextLine(); // line feed
			return -1;
		}
	}

	private static void isCopperOnly(ComputerNetwork cn){
		if (cn.DISCONNECTED){
			System.out.println("The network is originally disconnected.");
			if (cn.COPPER_ONLY)
				System.out.println("But all of its edges are copper.");
			else 
				System.out.println("And not all of its edges are copper.");
		} else
			System.out.println(cn.COPPER_ONLY);
	}

	/**
	 * Get the lowest latency path
	 */
	private static void getLLP(ComputerNetwork cn){
		int start = input("Start: ");
		int end = input("End: ");
		try{
			if (llps[start] == null) // find LLP for this starting point
				llps[start] = cn.lowestLatencyPath(start);
		} catch (Exception e) {
			System.out.println("No path between the two");
			return;
		}
		
		LinkedList<Edge> path = llps[start].to(end);
		if (path == null) {
			System.out.println("No path between the two");
			return;
		}

		int cur = start;
		int minBandwidth = Integer.MAX_VALUE;
		System.out.printf("\n[%d]", start);
		for (Edge e : path) { 
			System.out.println("\t" + e.toString(cur));
			if (minBandwidth > e.BANDWIDTH) minBandwidth = e.BANDWIDTH;
			cur = e.other(cur);
		}

		System.out.println("Bandwidth available: " + minBandwidth + "GB/s");
	}

	/**
	 * find articulation pairs
	 */
	private static void getArtiPairs(ComputerNetwork cn){
		LinkedList<Integer>[] aPair = cn.findArticulationPairs();
		boolean noPair = true;
		StringBuilder s = new StringBuilder();
		for (int v = 0; v < aPair.length; v++){
			if (aPair[v] == null) continue;
			for (Integer w : aPair[v]){
				s.append("(" + v + ", " + w + "); ");
				noPair = false;
			}
		}
		if (noPair) 
			System.out.println("No articulation pair found.");
		else {
			System.out.print("Articulation pairs: ");
			System.out.println(s.toString());
		}
	}

	public static void main(String[] args) throws java.io.FileNotFoundException{
		if (args.length != 1){
			System.out.println("Usage: java NetworkAnalysis <FILE_NAME>");
			return;
		}
		ComputerNetwork cn = new ComputerNetwork(args[0]); // mk network
		
		llps = new LLP[cn.V];

		final String MENU = "Computer Network Analysis" +
			"\n1. Find the lowest latency path between any two points" +
			"\n2. Whether the graph is copper-only connected" +
			"\n3. Find the minimum average latency spanning tree" +
			"\n4. Determine whether there is any articulation pair" + 
			"\n5. Exit\n>>> ";

		while (true){
			int in = 0;
			try {
				in = input(MENU);
			} catch (Exception e){
				keyboard.nextLine();
			}

			switch (in){
				case 1:
					getLLP(cn);
					break;
				case 2:
					isCopperOnly(cn);
					break;
				case 3:
					System.out.println(cn.getMST());
					break;
				case 4:
					getArtiPairs(cn);
					break;
				case 5:
					return;
				default: 
					System.out.println("Invalid choice!");
			}
		}
	}
}