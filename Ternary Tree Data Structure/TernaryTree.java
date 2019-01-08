import java.util.Iterator;
import java.util.NoSuchElementException;
import cs445.StackAndQueuePackage.*;
import cs445.a5.TernaryNode.Pos;


public class TernaryTree<E> implements TernaryTreeInterface<E>,
									TernaryTreeBonus<E> {
	private TernaryNode<E> root;

	public TernaryTree(){
		root = null;
	}

	public TernaryTree(E rootData){
		root = new TernaryNode<>(rootData);
	}

	public TernaryTree(E rootData, TernaryTree<E> leftTree, 
					   TernaryTree<E> middleTree, 
					   TernaryTree<E> rightTree){
        setTree(rootData, leftTree, middleTree, rightTree);
	}

	/** 
	 * Returns the data in the root node
     * @return the data in the root node
     * @throws EmptyTreeException  if the tree is empty 
     */
	public E getRootData() throws EmptyTreeException {
		if (root == null)
			throw new EmptyTreeException();
		return root.getData();
	}

	/**
	 * Checks if the tree contains a given element.
	 * @param elem the element to be searched for
	 * @return true if the element is in the tree, false otherwise
	 */
	public boolean contains(E elem){
        return root.contains(elem);
	}

	/**
	 * Determines if the tree is balanced such that for any node in
     * the tree, the heights of its subtrees differ by no more
     * than one.
     * @return true if the tree is balanced, false otherwise
     */
	public boolean isBalanced(){
        return root.isBalanced();
	}

	/**
	 * Checks if the tree is empty
	 * @return true of the tree is an empty tree, false otherwise
	 */
	public boolean isEmpty(){
		return root == null;
	}

	/** 
     * @return the height of the tree 
     */
    public int getHeight(){
    	int height = 0;
    	if (root != null)
    		height = root.getHeight();
    	return height;
    }

    /** 
     * @return the total number of nodes in the tree 
     */
    public int getNumberOfNodes(){
    	int numNodes = 0;
    	if (root != null)
    		numNodes = root.getNumberOfNodes();
    	return numNodes;
    }

    /** 
     * Returns a new level-order iterator that traverses a tree
     * level by level, left to right.
     * @return a new level-order iterator
     */
    public Iterator<E> getLevelOrderIterator(){
    	return new LevelOrderIterator();
    }

    /** 
     * Returns a new pre-order iterator that traverses a tree 
     * in an order of root and then left to right children.
     * @return a new pre-order iterator
     */
    public Iterator<E> getPreorderIterator(){
    	return new PreorderIterator();
    }

    /** 
     * Returns a new post-order iterator that traverses a tree 
     * in an order of left to right children and then root.
     * @return a new post-order iterator
     */
    public Iterator<E> getPostorderIterator(){
    	return new PostorderIterator();
    }

    /** 
     * The method is intended to return an in-order iterator which is
     * yet not supported by this data structure. A tree of this data
     * structure can contains nodes at most having 3 children, and 
     * therefore "in-order" is undefined. A sequence of either "left
     * child - parent - middle child - right child" or "left child -
     * middle child - parent - right child" can be considered as
     * "in-order." That will create confusion.
     * @throws UnsupportedOperationException
     */
    public Iterator<E> getInorderIterator(){
    	throw new UnsupportedOperationException();
    }

    /**
     * Clears the tree.
     */
    public void clear(){
    	root = null;
    }

    /** 
     * Sets the ternary tree to a new one-node ternary tree with
     * the given data
     * @param rootData the data for the new tree's root node
     */
    public void setTree(E rootData){
		root = new TernaryNode<>(rootData);
    }

    /** 
     * Sets this ternary tree to a new ternary tree
     * @param rootData  the data for the new tree's root node
     * @param leftTree the leftTree subtree of the new tree
     * @param middleTree the middleTree subtree of the new tree
     * @param rightTree the rightTree subtree of the new tree
     */
    public void setTree(E rootData, TernaryTreeInterface<E> leftTree,
                        TernaryTreeInterface<E> middleTree,
                        TernaryTreeInterface<E> rightTree){
        setTree(rootData, (TernaryTree) leftTree,
                (TernaryTree) middleTree, (TernaryTree) rightTree);
    }

    /**
     * A helper method to construct the tree given a tree and flags.
     * @param rootData  the data for the new tree's root node
     * @param left the leftTree subtree of the new tree
     * @param middle the middleTree subtree of the new tree
     * @param right the rightTree subtree of the new tree
     */
    private void setTree(E rootData, TernaryTree<E> left,
                        TernaryTree<E> middle, TernaryTree<E> right){
        TernaryNode<E> newroot = new TernaryNode<>(rootData);
        TernaryTree<E>[] trees = (TernaryTree<E>[]) new TernaryTree<?>[3];
        trees[0] = left;
        trees[1] = middle;
        trees[2] = right;
        Pos[] pos = Pos.values();

        for (int i = 0; i < 3; i++){
            if (trees[i] == null) continue;
            if (!trees[i].isEmpty()){
                // not to have two edges between root and one child
                if (trees[i] == trees[(i+1)%3])
                    newroot.setChild(trees[i].root.copy(), pos[i]);
                else 
                    newroot.setChild(trees[i].root, pos[i]);
            
            }

        }
        this.root = newroot;

        for (TernaryTree<E> tree : trees){
            if (tree != null && tree != this) tree.clear();
        }
    }

    private class LevelOrderIterator implements Iterator<E> {
        private QueueInterface<TernaryNode<E>> nodeQueue;

        public LevelOrderIterator(){
            nodeQueue = new LinkedQueue<>();
            if (root != null) nodeQueue.enqueue(root);
        }

        public boolean hasNext(){
            return !nodeQueue.isEmpty();
        }

        public E next(){
            TernaryNode<E> nextNode, child;
            if (hasNext()){
                nextNode = nodeQueue.dequeue();
                for (Pos pos : Pos.values()){
                    child = nextNode.getChild(pos);
                    if (child != null) nodeQueue.enqueue(child);
                }
            } else throw new NoSuchElementException();
            return nextNode.getData();
        }

        public void remove(){
            throw new UnsupportedOperationException();
        }
    }

    private class PreorderIterator implements Iterator<E> {
        private StackInterface<TernaryNode<E>> nodeStack;

        public PreorderIterator(){
            nodeStack = new LinkedStack<>();
            if (root != null) nodeStack.push(root);
        }

        public boolean hasNext(){
            return !nodeStack.isEmpty();
        }

        public E next(){
            TernaryNode<E> nextNode, child;
            Pos[] pos = Pos.values();
            if (hasNext()){
                nextNode = nodeStack.pop();
                for (int i = 2; i >= 0; i--){
                    child = nextNode.getChild(pos[i]);
                    if (child != null) nodeStack.push(child);
                }
            } else throw new NoSuchElementException();
            return nextNode.getData();
        }

        public void remove(){
            throw new UnsupportedOperationException();
        }
    }

    private class PostorderIterator implements Iterator<E> {
        private StackInterface<TernaryNode<E>> nodeStack;
        private TernaryNode<E> currNode;

        public PostorderIterator(){
            nodeStack = new LinkedStack<>();
            currNode = root;
        }

        public boolean hasNext(){
            return currNode != null || !nodeStack.isEmpty();
        }

        public E next(){
            TernaryNode<E> child, nextNode = null;
            while(currNode != null){
                nodeStack.push(currNode);
                for (Pos pos : Pos.values()){
                    child = currNode.getChild(pos);
                    if (child != null){
                        currNode = child;
                        break;
                    }
                    if (pos == Pos.RIGHT) // no children
                        currNode = null;
                }
            }
            if (!nodeStack.isEmpty()){
                nextNode = nodeStack.pop();

                if (!nodeStack.isEmpty()){
                    TernaryNode<E> parent = nodeStack.peek();
                    // Go to the next sibling
                    boolean foundNext = false;
                    for (Pos pos : Pos.values()){
                        if (parent.getChild(pos) == null) continue;
                        if (foundNext){
                            currNode = parent.getChild(pos);
                            break;
                        }
                        if (parent.getChild(pos) == nextNode)
                            foundNext = true;
                    }
                }
            } else throw new NoSuchElementException();
            return nextNode.getData();
        }

        public void remove(){
            throw new UnsupportedOperationException();
        }
    }

    protected boolean hasSameRef(){
        return root.hasSameRef();
    }
}
