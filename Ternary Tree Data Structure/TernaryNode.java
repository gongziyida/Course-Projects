public class TernaryNode<E> {
    protected enum Pos{LEFT, MIDDLE, RIGHT}
	private E data;
    private int height;
	@SuppressWarnings("unchecked")
	private TernaryNode<E>[] children = 
			(TernaryNode<E>[]) new TernaryNode<?>[3];
	private final int LEFT = 0, MIDDLE = 1, RIGHT = 2;

	public TernaryNode(){
		this(null);
	}

	public TernaryNode(E data){
		this(data, null, null, null);
	}

	public TernaryNode(E data, TernaryNode<E> leftChild, 
						TernaryNode<E> middleChild, 
						TernaryNode<E> rightChild){
		this.data = data;
		children[LEFT] = leftChild;
		children[MIDDLE] = middleChild;
		children[RIGHT] = rightChild;

        height = 1; 
        determineHeight();
	}

	/**
	 * @return the data stored in this node
	 */
	public E getData(){
		return this.data;
	}

	/**
	 * Sets the data in this node.
	 * @param data the data to overwrite into this node
	 */
	public void setData(E data){
		this.data = data;
	}

	/** 
     * @return the maximum number of nodes from this node to any of 
     * its leaf descendant
     */
	public int getHeight(){
		return height;
	}

    /**
     * Calculates the height of the tree rooted at this node
     */
    private void determineHeight(){
        int newHeight;
        for (TernaryNode<E> child : children){
            if (child != null){
                newHeight = child.getHeight() + 1;
                height = newHeight > height? newHeight : height;
            }
        }
    }

	/**
	 * @return the total number of nodes in the subtree rooted at
	 * this node
	 */
	public int getNumberOfNodes(){
		int numNodes = 1;
		for (TernaryNode<E> child : children){
			if (child != null) numNodes += child.getNumberOfNodes();
		}
		return numNodes;
	}

    /**
     * Checks if the node or its any descendants contains the element.
     * @param elem the element to be searched for
     * @return true if the element is in the tree, false otherwise
     */
    public boolean contains(E elem){
        if (data == elem) return true; 
        for (TernaryNode<E> child : children){
            if (child != null){
                if (child.contains(elem)) 
                    return true;
            }
        }
        return false;
    }

    /**
     * Determines if the tree rooted at this node is balanced
     * @return true if the tree is balanced, false otherwise
     */
    public boolean isBalanced(){
        boolean areChildrenBalanced = true;
        for (TernaryNode<E> child : children){
            if (child != null){
                if (height - child.getHeight() > 2) return false;
                else areChildrenBalanced = child.isBalanced();
            }
        }
        return areChildrenBalanced;
    }
    
	/** 
	 * Determines if this node has a left/ middle/ right child.
     * @param pos a flag indicating left, middle, or right child
     * @return true if the node has a left/ middle/ right child
     */
    public boolean hasChild(Pos pos){
        switch (pos){
            case LEFT: return children[LEFT] != null;
            case MIDDLE: return children[MIDDLE] != null;
            case RIGHT: return children[RIGHT] != null;
        }
        return false;
    }

	/** 
     * @param pos a flag indicating left, middle, or right child
     * @return this node’s left/ middle/ right child
     */
    public TernaryNode<E> getChild(Pos pos){
        switch (pos){
            case LEFT: return children[LEFT];
            case MIDDLE: return children[MIDDLE];
            case RIGHT: return children[RIGHT];
        }
        return null;
    }

    /**
     * Sets this node’s left/ middle/ right child to a given node.
     * @param newChild the node that will be the child
     * @param pos a flag indicating left, middle, or right child
     */
    public void setChild(TernaryNode<E> newChild, Pos pos){
        switch (pos){
            case LEFT: 
                children[LEFT] = newChild;
                break;
            case MIDDLE: 
                children[MIDDLE] = newChild;
                break;
            case RIGHT: 
                children[RIGHT] = newChild;
                break;
        }
        determineHeight();
    }

    /** 
     * Determines whether this node is a leaf.
     *  @return true if this node is a leaf 
     */
    public boolean isLeaf() {
        boolean notLeaf = false;
        for (TernaryNode<E> child : children){
        	if (child != null) notLeaf = true;
        }
        return !notLeaf;
    }

    /**
     * Copies the subtree rooted at this node.
     * @return the root of a copy of the subtree rooted at this node
     */
    public TernaryNode<E> copy(){
    	TernaryNode<E> newNode = new TernaryNode<>(this.data);
    	if (children[LEFT] != null) 
    		newNode.setChild(children[LEFT].copy(), Pos.LEFT);
    	if (children[MIDDLE] != null) 
    		newNode.setChild(children[MIDDLE].copy(), Pos.MIDDLE);
    	if (children[RIGHT] != null) 
    		newNode.setChild(children[RIGHT].copy(), Pos.RIGHT);
    	return newNode;
    }

    protected boolean hasSameRef(){
        for (int i = 0; i < 3; i++){
            if ((children[i] == children[(i+1)%3]) || 
                (children[i] == this))
                return true;
        }
        return false;
    }
}
