package bioinformatics.suffixtree;


import java.util.*;

/**
 * Represents a node of the generalized suffix tree graph
 * @see GeneralizedSuffixTree
 */
class Node {

    /**
     * The payload array used to store the data (indexes) associated with this node.
     * In this case, it is used to store all property indexes.
     *
     * As it is handled, it resembles an ArrayList: when it becomes full it
     * is copied to another bigger array (whose size is equals to data.length +
     * INCREMENT).
     *
     * Originally it was a List<Integer> but it took too much memory, changing
     * it to int[] take less memory because indexes are stored using native
     * types.
     */
    private final Map<Integer,Integer> data;
    /**
     * Represents index of the last position used in the data int[] array.
     *
     * It should always be less than data.length
     */
    private int lastIdx = 0;
    /**
     * The starting size of the int[] array containing the payload
     */
    private static final int START_SIZE = 0;
    /**
     * The increment in size used when the payload array is full
     */
    private static final int INCREMENT = 1;
    /**
     * The set of edges starting from this node
     */
    private final Map<Character, Edge> edges;
    /**
     * The suffix link as described in Ukkonen's paper.
     * if str is the string denoted by the path from the root to this, this.suffix
     * is the node denoted by the path that corresponds to str without the first char.
     */
    private Node suffix;
    /**
     * The total number of <em>different</em> results that are stored in this
     * node and in underlying ones (i.e. nodes that can be reached through paths
     * starting from <tt>this</tt>.
     *
     * This must be calculated explicitly using computeAndCacheCount
     * @see Node#computeAndCacheCount()
     */
    private int resultCount = -1;

    /**
     * Creates a new Node
     */
    Node() {
        edges = new EdgeBag();
        suffix = null;
        data = new HashMap<Integer, Integer>();
    }

    /**
     * Returns all the indexes associated to this node and its children.
     * @return all the indexes associated to this node and its children
     */
    Set<Integer> getData() {
        Set<Integer> ret = new HashSet<Integer>();
        for (int num : data.keySet()) {
            ret.add(num);
        }

        for (Edge e : edges.values()) {
            for (int num : e.getDest().getData()) {
                ret.add(num);
            }
        }
        return ret;
    }

    /**
     * Adds the given <tt>index</tt> to the set of indexes associated with <tt>this</tt>
     */
    void addRef(int index, int pos) {
        if (contains(index)) {
            return;
        }
        int sum = index + pos;
        for(int key : data.keySet()) {
            if(key + data.get(key) == sum) {
                System.out.println("found already!");
                return;
            }
        }
       //System.out.println("adding index: "+index+" pos: "+pos);
        addIndex(index, pos);

        // add this reference to all the suffixes as well
        Node iter = this.suffix;
        while (iter != null) {
            if (iter.contains(index)) {
                break;
            }
            iter.addRef(index, pos);
            iter = iter.suffix;
        }

    }

    /**
     * Tests whether a node contains a reference to the given index.
     *
     * <b>IMPORTANT</b>: it works because the array is sorted by construction
     *
     * @param index the index to look for
     * @return true <tt>this</tt> contains a reference to index
     */
    private boolean contains(int index) {
        return data.containsKey(index);
        // Java 5 equivalent to
        // return java.util.Arrays.binarySearch(data, 0, lastIdx, index) >= 0;
    }

    /**
     * Computes the number of results that are stored on this node and on its
     * children, and caches the result.
     *
     * Performs the same operation on subnodes as well
     * @return the number of results
     */
    protected int computeAndCacheCount() {
        computeAndCacheCountRecursive();
        return resultCount;
    }

    private Set<Integer> computeAndCacheCountRecursive() {
        Set<Integer> ret = new HashSet<Integer>();
        for (int num : data.keySet()) {
            ret.add(num);
        }
        for (Edge e : edges.values()) {
            for (int num : e.getDest().computeAndCacheCountRecursive()) {
                ret.add(num);
            }
        }

        resultCount = ret.size();
        return ret;
    }

    /**
     * Returns the number of results that are stored on this node and on its
     * children.
     * Should be called after having called computeAndCacheCount.
     *
     * @throws IllegalStateException when this method is called without having called
     * computeAndCacheCount first
     * @see Node#computeAndCacheCount()
     * @todo this should raise an exception when the subtree is changed but count
     * wasn't updated
     */
    public int getResultCount() throws IllegalStateException {
        if (-1 == resultCount) {
            throw new IllegalStateException("getResultCount() shouldn't be called without calling computeCount() first");
        }

        return resultCount;
    }

    void addEdge(char ch, Edge e) {
        edges.put(ch, e);
    }

    Edge getEdge(char ch) {
        return edges.get(ch);
    }

    Map<Character, Edge> getEdges() {
        return edges;
    }

    Node getSuffix() {
        return suffix;
    }

    void setSuffix(Node suffix) {
        this.suffix = suffix;
    }

    private void addIndex(int index, int pos) {
        data.put(index, pos);
    }
}

