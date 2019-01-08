import java.util.Iterator;
import java.util.NoSuchElementException;

public class TernaryTreeTestClient{
	private static final String[] dataSet = {
							"a", "c", "d", "#", "!", "q", "t", 
							"y", "i", ":", "b", "s", ">", "w", 
						   	"o", "m", "l", "j", "h", "r", "1",
						   	"@", "l", "*", "%", "$", "~", "k",
						   	"e", "n", "x", "z", "0", "|", "f",
						   	"$", "v", ")", "{", "u", "p", "g"};

	public static void testIterator(Iterator<String> it){
		try{
			while(it.hasNext()){
				System.out.print(it.next());
			}
			System.out.println("");
		} catch (NoSuchElementException e){
			System.out.println("Caught NosuchElementException! Codes have bugs.");
		}
	}
	
	public static void treeInfo(TernaryTree<String> tree, String contained){
		System.out.println("Root Data: " + tree.getRootData());
		System.out.println("Empty: " + tree.isEmpty());
		System.out.println("Contains " + contained + " (indeed contains): " + 
								tree.contains(contained));
		System.out.println("Contains '???' (never contains): " + tree.contains("???")); 
		System.out.println("Height: " + tree.getHeight());
		System.out.println("#Nodes: " + tree.getNumberOfNodes());
		System.out.println("Balanced: " + tree.isBalanced());
		System.out.println("-----------------------------------------------------");
	}

	public static void main(String[] args){
		// test non iterator methods
		System.out.println("Tree Constructor 1--no root data: ");
		TernaryTree<String> tree1 = new TernaryTree<>();
		System.out.println("Empty: " + tree1.isEmpty() + "\n");

		System.out.println("Tree Constructor 2--no child provided: ");
		TernaryTree<String> tree2 = new TernaryTree<>(dataSet[0]);
		treeInfo(tree2, dataSet[0]);

		System.out.println("Tree Constructor 3--children provided: ");
		System.out.println("Subtree: Left--tree2, Middle--tree1, Right--tree2.copy");
		TernaryTree<String> tree3 = new TernaryTree<>(dataSet[1], tree2, tree1, tree2);
		System.out.println("tree3 does not have two children with " +
			"the same reference: " + !tree3.hasSameRef());
		System.out.println("tree2 should be cleared after creating tree3");
		System.out.println("tree2 is empty: " + tree2.isEmpty() + "\nTree3 info: ");
		treeInfo(tree3, dataSet[0]);

		System.out.println("Setting tree1 (whose root was null) with setTree(E): ");
		tree1.setTree(dataSet[4]);
		treeInfo(tree1, dataSet[4]);

		System.out.println("Setting tree1 with setTree(E, tree, tree, tree): " +
							"(#Nodes should be 4)");
		tree1.setTree(dataSet[5], new TernaryTree<>(dataSet[8]), 
					new TernaryTree<>(dataSet[6]), new TernaryTree<>(dataSet[7]));
		treeInfo(tree1, dataSet[6]);

		System.out.println("Setting one of tree1's child to tree1--should make a copy: ");
		tree1.setTree(dataSet[4], tree1, new TernaryTree<>(dataSet[10]), 
						new TernaryTree<>(dataSet[11]));
		System.out.println("tree1 does not have a child with " +
			"the same reference as itself: " + !tree1.hasSameRef());
		treeInfo(tree1, dataSet[8]);

		System.out.println("Constructing an unbalanced tree");
		System.out.println("#Nodes should be 10, height be 4.");
		tree1.setTree(dataSet[12], new TernaryTree<>(dataSet[13]), 
						tree1, new TernaryTree<>(dataSet[14]));
		treeInfo(tree1, dataSet[7]);

		System.out.println("Constructing another unbalanced tree");
		System.out.println("Check if isBalanced() works fine recursively");
		tree1.setTree(dataSet[15], tree1, tree1, tree1);
		treeInfo(tree1, dataSet[6]);

		System.out.println("Constructing a balanced tree");
		System.out.println("Check if isBalanced() works fine recursively");
		tree2.setTree(dataSet[15], new TernaryTree<>(dataSet[10]), 
					new TernaryTree<>(dataSet[3]), new TernaryTree<>(dataSet[0]));
		tree2.setTree(dataSet[8], tree2, tree2, new TernaryTree<>(dataSet[7]));
		tree2.setTree(dataSet[5], tree2, tree2, tree2);
		System.out.println("#Nodes should be 31, height be 4.");
		treeInfo(tree2, dataSet[0]);

		// test iterators
		tree2.setTree(dataSet[15], new TernaryTree<>(dataSet[10]), 
					null, new TernaryTree<>(dataSet[0]));
		tree1.setTree(dataSet[5], new TernaryTree<>(dataSet[13]), 
					new TernaryTree<>(dataSet[4]), 
					new TernaryTree<>(dataSet[8]));
		tree2.setTree(dataSet[19], tree2, tree1, new TernaryTree<>(dataSet[3]));
		System.out.println("Setting a sample for testing iterators: ");
		treeInfo(tree2, dataSet[10]);

		System.out.println("Testing the level-order iterator");
		testIterator(tree2.getLevelOrderIterator());

		System.out.println("Testing the preorder iterator");
		testIterator(tree2.getPreorderIterator());

		System.out.println("Testing the postorder iterator");
		testIterator(tree2.getPostorderIterator());
	}
}
